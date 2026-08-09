package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// --- Data Models ---

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class BillEntry(
    val id: String,
    val title: String,
    val amount: Double,
    val dueDate: String
)

data class ReminderEntry(
    val id: String,
    val text: String,
    val dueDate: String,
    val isCompleted: Boolean = false,
    val isEnabled: Boolean = true
)

data class FinancialSummaryState(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netBalance: Double = 0.0,
    val activeBudgetsCount: Int = 0,
    val totalSavings: Double = 0.0,
    val isLoading: Boolean = true
)

// --- Helper Utilities ---

private object DateUtils {
    val monthYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-yyyy", Locale.getDefault())
    val isoDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
}

// --- ViewModel Implementation ---

class FinanceViewModel(
    application: Application,
    private val repository: FinanceRepository
) : AndroidViewModel(application) {

    // --- Core Reactive Flow Inputs from Repository ---
    val expenses: StateFlow<List<Expense>> = repository.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<Budget>> = repository.getBudgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoals: StateFlow<List<SavingsGoal>> = repository.getSavingsGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ViewModel Local State Flows ---
    private val _billsList = MutableStateFlow<List<BillEntry>>(emptyList())
    val billsList: StateFlow<List<BillEntry>> = _billsList.asStateFlow()

    private val _remindersList = MutableStateFlow<List<ReminderEntry>>(emptyList())
    val remindersList: StateFlow<List<ReminderEntry>> = _remindersList.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hello! I am your AI financial assistant. How can I help you manage your money today?", isUser = false))
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // --- Combined Unified Dashboard State ---
    val financialSummary: StateFlow<FinancialSummaryState> = combine(
        expenses,
        transactions,
        budgets,
        savingsGoals
    ) { currentExpenses, currentTransactions, currentBudgets, currentGoals ->
        val income = currentTransactions.filter { it.type.equals("INCOME", ignoreCase = true) }.sumOf { it.amount }
        val spent = currentExpenses.sumOf { it.amount }
        val savings = currentGoals.sumOf { it.currentAmount }

        FinancialSummaryState(
            totalIncome = income,
            totalExpenses = spent,
            netBalance = income - spent,
            activeBudgetsCount = currentBudgets.size,
            totalSavings = savings,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummaryState())

    // --- Expenses & Transactions Operations ---
    fun addExpense(title: String, amount: Double, category: String, date: String = LocalDate.now().format(DateUtils.isoDateFormatter)) {
        viewModelScope.launch(Dispatchers.IO) {
            val expense = Expense(
                title = title,
                amount = amount,
                category = category,
                date = date
            )
            repository.insertExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteExpense(expense)
        }
    }

    fun addTransaction(title: String, amount: Double, type: String, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = Transaction(
                title = title,
                amount = amount,
                type = type,
                category = category,
                date = LocalDate.now().format(DateUtils.isoDateFormatter)
            )
            repository.insertTransaction(transaction)
        }
    }

    // --- Budgets Operations ---
    fun addBudget(category: String, targetAmount: Double) {
        val currentMonthYear = YearMonth.now().format(DateUtils.monthYearFormatter)
        viewModelScope.launch(Dispatchers.IO) {
            val newBudget = Budget(
                category = category,
                targetAmount = targetAmount,
                monthYear = currentMonthYear
            )
            repository.insertBudget(newBudget)
        }
    }

    // --- Savings Goals Operations ---
    fun addSavingsGoal(title: String, targetAmount: Double, targetDate: String, category: String = "General") {
        viewModelScope.launch(Dispatchers.IO) {
            val goal = SavingsGoal(
                title = title,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                targetDate = targetDate,
                category = category
            )
            repository.insertSavingsGoal(goal)
        }
    }

    fun contributeToGoal(goal: SavingsGoal, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedGoal = goal.copy(currentAmount = goal.currentAmount + amount)
            repository.updateSavingsGoal(updatedGoal)
        }
    }

    fun resetGoalCategoryBatch(oldCategory: String, newCategory: String = "General") {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedGoals = savingsGoals.value
                .filter { it.category == oldCategory }
                .map { it.copy(category = newCategory) }

            if (updatedGoals.isNotEmpty()) {
                repository.updateSavingsGoalsBatch(updatedGoals)
            }
        }
    }

    // --- Bills & Reminders Operations ---
    fun addBill(title: String, amount: Double, dueDate: String) {
        val newBill = BillEntry(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            amount = amount,
            dueDate = dueDate
        )
        _billsList.update { it + newBill }
    }

    fun removeBill(id: String) {
        _billsList.update { list -> list.filterNot { it.id == id } }
    }

    fun addReminder(text: String, dueDate: String) {
        val newReminder = ReminderEntry(
            id = java.util.UUID.randomUUID().toString(),
            text = text,
            dueDate = dueDate
        )
        _remindersList.update { it + newReminder }
    }

    fun toggleReminder(id: String) {
        _remindersList.update { list ->
            list.map { if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it }
        }
    }

    // --- AI Chat Assistant Operations ---
    fun sendChatMessage(userText: String, geminiClient: GeminiClient) {
        if (userText.isBlank()) return

        val userMessage = ChatMessage(text = userText, isUser = true)
        _chatMessages.update { it + userMessage }
        _isAiLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contextSummary = buildFinancialContextSummary()
                val prompt = "User Context:\n$contextSummary\n\nUser Question: $userText"
                val responseText = geminiClient.generateContent(prompt) ?: "I am currently unable to generate advice. Please try again."

                _chatMessages.update { it + ChatMessage(text = responseText, isUser = false) }
            } catch (e: Exception) {
                _chatMessages.update { it + ChatMessage(text = "Error getting response: ${e.localizedMessage}", isUser = false) }
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    private fun buildFinancialContextSummary(): String {
        val summary = financialSummary.value
        return """
            Total Income: $${summary.totalIncome}
            Total Spent: $${summary.totalExpenses}
            Net Available Balance: $${summary.netBalance}
            Active Budgets Count: ${summary.activeBudgetsCount}
            Total Savings: $${summary.totalSavings}
        """.trimIndent()
    }

    // --- ViewModel Factory ---
    class Factory(
        private val application: Application,
        private val repository: FinanceRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
                return FinanceViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}