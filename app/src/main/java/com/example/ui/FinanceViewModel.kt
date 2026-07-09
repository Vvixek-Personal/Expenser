package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.api.GeminiClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class FinanceViewModel(
    application: Application,
    private val repository: FinanceRepository
) : AndroidViewModel(application) {

    // Database states
    val expenses = repository.allExpenses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Selected Date Range (Long: start timestamp, Long: end timestamp)
    private val _selectedDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val selectedDateRange: StateFlow<Pair<Long, Long>?> = _selectedDateRange.asStateFlow()

    fun setDateRange(start: Long?, end: Long?) {
        _selectedDateRange.value = if (start != null && end != null) Pair(start, end) else null
    }

    // Filtered Expenses based on date range
    val filteredExpenses = combine(expenses, _selectedDateRange) { list, range ->
        if (range == null) {
            list
        } else {
            // Include dates inside the inclusive range
            list.filter { it.date >= range.first && it.date <= range.second }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val accounts = repository.allAccounts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val transactions = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val budgets = repository.allBudgets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val savingsGoals = repository.allSavingsGoals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Chat and AI states
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Hello! I am your AI Financial Advisor. Ask me anything about budgeting, savings strategies, or request a complete 'AI Financial Audit' of your current finances using the dashboard button!",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _aiAuditReport = MutableStateFlow<String?>(null)
    val aiAuditReport: StateFlow<String?> = _aiAuditReport.asStateFlow()

    private val _isAuditLoading = MutableStateFlow(false)
    val isAuditLoading: StateFlow<Boolean> = _isAuditLoading.asStateFlow()

    // SharedPreferences for local configuration
    private val _themeIndex = MutableStateFlow(0)
    val themeIndex: StateFlow<Int> = _themeIndex.asStateFlow()

    private val _customThemeHue = MutableStateFlow(200f)
    val customThemeHue: StateFlow<Float> = _customThemeHue.asStateFlow()

    private val sharedPrefs = getApplication<Application>().getSharedPreferences("finance_prefs", android.content.Context.MODE_PRIVATE)

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _monthlyBudget = MutableStateFlow(25000.0)
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    private val _customCategories = MutableStateFlow<List<String>>(emptyList())
    val customCategories: StateFlow<List<String>> = _customCategories.asStateFlow()

    val defaultCategories = listOf("Food", "Travel", "Rent", "Utilities", "Entertainment", "Shopping", "Persons", "Others")

    val allCategories = _customCategories.map { custom ->
        (defaultCategories + custom).distinct()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultCategories)

    init {
        _userName.value = sharedPrefs.getString("user_name", null)
        _monthlyBudget.value = sharedPrefs.getFloat("monthly_budget", 25000.0f).toDouble()
        val savedCats = sharedPrefs.getStringSet("custom_categories", emptySet()) ?: emptySet()
        _customCategories.value = savedCats.toList().sorted()
        
        _themeIndex.value = sharedPrefs.getInt("theme_index", 0)
        _customThemeHue.value = sharedPrefs.getFloat("custom_theme_hue", 200f)
        com.example.ui.theme.updateThemeColors(_themeIndex.value, _customThemeHue.value)
    }

    fun updateTheme(index: Int) {
        sharedPrefs.edit().putInt("theme_index", index).apply()
        _themeIndex.value = index
        com.example.ui.theme.updateThemeColors(index, _customThemeHue.value)
    }

    fun updateCustomThemeHue(hue: Float) {
        sharedPrefs.edit().putFloat("custom_theme_hue", hue).apply()
        _customThemeHue.value = hue
        com.example.ui.theme.updateThemeColors(_themeIndex.value, hue)
    }

    fun saveUserName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) {
            sharedPrefs.edit().putString("user_name", trimmed).apply()
            _userName.value = trimmed
        }
    }

    fun updateMonthlyBudget(newLimit: Double) {
        if (newLimit > 0.0) {
            sharedPrefs.edit().putFloat("monthly_budget", newLimit.toFloat()).apply()
            _monthlyBudget.value = newLimit
        }
    }

    fun addCustomCategory(category: String) {
        val trimmed = category.trim()
        if (trimmed.isEmpty()) return
        val current = sharedPrefs.getStringSet("custom_categories", emptySet()) ?: emptySet()
        val updated = current + trimmed
        sharedPrefs.edit().putStringSet("custom_categories", updated).apply()
        _customCategories.value = updated.toList().sorted()
    }

    // DB Operations
    fun addExpense(amount: Double, category: String, date: Long, note: String?) {
        viewModelScope.launch {
            repository.insertExpense(
                Expense(
                    amount = amount,
                    category = category,
                    date = date,
                    note = note
                )
            )
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun deleteExpenses(expensesList: List<Expense>) {
        viewModelScope.launch {
            expensesList.forEach { repository.deleteExpense(it) }
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun addAccount(name: String, balance: Double, type: String) {
        viewModelScope.launch {
            repository.insertAccount(Account(name = name, balance = balance, type = type))
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    fun addTransaction(title: String, amount: Double, type: String, category: String, accountId: Int) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    timestamp = System.currentTimeMillis(),
                    accountId = accountId
                )
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addBudget(category: String, amountLimit: Double) {
        viewModelScope.launch {
            repository.insertBudget(Budget(category = category, amountLimit = amountLimit, monthYear = "07-2026"))
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    fun addSavingsGoal(name: String, targetAmount: Double, targetDate: Long) {
        viewModelScope.launch {
            repository.insertSavingsGoal(SavingsGoal(name = name, targetAmount = targetAmount, currentAmount = 0.0, targetDate = targetDate))
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
        }
    }

    fun allocateToGoal(goal: SavingsGoal, account: Account, amount: Double) {
        viewModelScope.launch {
            repository.allocateToSavingsGoal(goal, account, amount)
        }
    }

    fun withdrawFromGoal(goal: SavingsGoal, account: Account, amount: Double) {
        viewModelScope.launch {
            repository.withdrawFromSavingsGoal(goal, account, amount)
        }
    }

    // AI Advisor Interface
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(text = text, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg

        _isChatLoading.value = true
        viewModelScope.launch {
            // Include summary of current financial standing to make the chat context-aware!
            val contextSummary = buildFinancialContextSummary()
            val fullPrompt = "$contextSummary\n\nUser Question: $text\n\nRemember to keep your advice friendly, brief (1-3 sentences or clear bullet points), and highly practical."

            val aiResponse = GeminiClient.getFinancialAdvice(fullPrompt)
            _chatMessages.value = _chatMessages.value + ChatMessage(text = aiResponse, isUser = false)
            _isChatLoading.value = false
        }
    }

    // AI Financial Audit Generator
    fun generateAiAuditReport() {
        _isAuditLoading.value = true
        viewModelScope.launch {
            val contextSummary = buildFinancialContextSummary()
            val prompt = """
                Perform a professional, encouraging, and detailed 'AI Financial Audit' of my finances.
                Here is my current transaction history and budgeting data:
                $contextSummary
                
                Please provide the report using this specific layout:
                1. 📊 **Financial Standing Review**: A quick summary of my current assets vs spending.
                2. 📈 **Budget Performance**: Analyze how well I'm sticking to my category budgets. Highlight any exceeded budgets.
                3. 🚀 **3 Smart Steps to Save**: 3 highly specific, creative, and action-oriented tips based on my actual transactions (e.g., shopping or grocery patterns).
                4. 🎯 **Savings Milestones**: A word of encouragement regarding my active goals.
                
                Keep it concise but highly engaging and professional. Use dollar signs ($) for money formatting.
            """.trimIndent()

            val response = GeminiClient.getFinancialAdvice(
                prompt = prompt,
                systemPrompt = "You are an expert Certified Financial Planner (CFP) AI. You analyze a user's transaction data to compile actionable, encouraging, and highly specific financial audits. Be clear, objective, and supportive."
            )
            _aiAuditReport.value = response
            _isAuditLoading.value = false
        }
    }

    fun clearAuditReport() {
        _aiAuditReport.value = null
    }

    private fun buildFinancialContextSummary(): String {
        val currentAccounts = accounts.value
        val currentTransactions = transactions.value
        val currentBudgets = budgets.value
        val currentGoals = savingsGoals.value

        val accountsText = currentAccounts.joinToString("\n") { "Account: ${it.name} (${it.type}) - Balance: $${it.balance}" }
        
        // Sum expenses by category
        val expensesByCategory = currentTransactions
            .filter { it.type == "EXPENSE" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val expensesText = expensesByCategory.entries.joinToString("\n") { "- ${it.key}: Spent $${it.value}" }
        
        val budgetsText = currentBudgets.joinToString("\n") { "- Category '${it.category}': Limit $${it.amountLimit}" }
        val goalsText = currentGoals.joinToString("\n") { "- Goal '${it.name}': Saved $${it.currentAmount} of $${it.targetAmount}" }

        val recentTxText = currentTransactions.take(10).joinToString("\n") { 
            "  * [${it.type}] ${it.title} - $${it.amount} (${it.category})" 
        }

        return """
            --- FINANCIAL STANDING CONTEXT ---
            
            ACCOUNTS:
            $accountsText
            
            BUDGET LIMITS:
            $budgetsText
            
            SAVINGS GOALS:
            $goalsText
            
            ACTUAL SPENDING BY CATEGORY:
            $expensesText
            
            RECENT TRANSACTIONS (Last 10):
            $recentTxText
            
            ----------------------------------
        """.trimIndent()
    }
}

class FinanceViewModelFactory(
    private val application: Application,
    private val repository: FinanceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
