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
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class BillEntry(val id: String, val title: String, val amount: Double, val dueDate: String)
data class ReminderEntry(
    val id: String,
    val text: String,
    val dueDate: String,
    val isCompleted: Boolean = false,
    val isEnabled: Boolean = true
)

class FinanceViewModel(
    application: Application,
    private val repository: FinanceRepository
) : AndroidViewModel(application) {

    val billsList = androidx.compose.runtime.mutableStateListOf<BillEntry>()

    val remindersList = androidx.compose.runtime.mutableStateListOf<ReminderEntry>()

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

    // Selected Language Preference
    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    // SharedPreferences for local configuration
    private val _themeIndex = MutableStateFlow(0)
    val themeIndex: StateFlow<Int> = _themeIndex.asStateFlow()

    private val _customThemeHue = MutableStateFlow(200f)
    val customThemeHue: StateFlow<Float> = _customThemeHue.asStateFlow()

    private val sharedPrefs = getApplication<Application>().getSharedPreferences("finance_prefs", android.content.Context.MODE_PRIVATE)

    // Live Storage and Network/Data Usage states
    private val _storageSize = MutableStateFlow("0.0 KB")
    val storageSize: StateFlow<String> = _storageSize.asStateFlow()

    private val _dataSize = MutableStateFlow("0.0 KB")
    val dataSize: StateFlow<String> = _dataSize.asStateFlow()

    // Map storing Category Name to Material Icon Name (e.g. "Restaurant", "Star", "Pending" during load)
    private val _categoryIcons = MutableStateFlow<Map<String, String>>(emptyMap())
    val categoryIcons: StateFlow<Map<String, String>> = _categoryIcons.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _userProfileImageUri = MutableStateFlow<String?>(null)
    val userProfileImageUri: StateFlow<String?> = _userProfileImageUri.asStateFlow()

    fun updateUserProfileImageUri(uriString: String?) {
        _userProfileImageUri.value = uriString
        sharedPrefs.edit().putString("user_profile_image_uri", uriString).apply()
    }

    private val _userDob = MutableStateFlow("24 December 1999")
    val userDob: StateFlow<String> = _userDob.asStateFlow()

    private val _userJob = MutableStateFlow("Successor Designer")
    val userJob: StateFlow<String> = _userJob.asStateFlow()

    private val _userMonthlyIncome = MutableStateFlow("500 - 3000 / year")
    val userMonthlyIncome: StateFlow<String> = _userMonthlyIncome.asStateFlow()

    private val _userGender = MutableStateFlow("Male")
    val userGender: StateFlow<String> = _userGender.asStateFlow()

    // Daily Streak State & Logic
    private val _currentStreak = MutableStateFlow(1)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _showStreakDialog = MutableStateFlow(false)
    val showStreakDialog: StateFlow<Boolean> = _showStreakDialog.asStateFlow()

    fun dismissStreakDialog() {
        _showStreakDialog.value = false
    }

    fun triggerShowStreakDialog() {
        _showStreakDialog.value = true
    }

    private fun checkAndCalculateDailyStreak() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())
        val lastStreakDate = sharedPrefs.getString("last_streak_date", null)
        var streak = sharedPrefs.getInt("current_streak", 0)

        if (lastStreakDate == null) {
            streak = 1
            sharedPrefs.edit()
                .putString("last_streak_date", todayStr)
                .putInt("current_streak", streak)
                .apply()
            _currentStreak.value = streak
            _showStreakDialog.value = true
        } else if (lastStreakDate == todayStr) {
            _currentStreak.value = if (streak < 1) 1 else streak
            _showStreakDialog.value = false
        } else {
            try {
                val lastDate = sdf.parse(lastStreakDate)
                val todayDate = sdf.parse(todayStr)
                if (lastDate != null && todayDate != null) {
                    val diffInMillis = todayDate.time - lastDate.time
                    val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()

                    if (diffInDays == 1) {
                        streak += 1
                    } else if (diffInDays > 1) {
                        streak = 1
                    } else {
                        if (streak < 1) streak = 1
                    }
                } else {
                    streak = 1
                }
            } catch (e: Exception) {
                streak = 1
            }

            sharedPrefs.edit()
                .putString("last_streak_date", todayStr)
                .putInt("current_streak", streak)
                .apply()
            _currentStreak.value = streak
            _showStreakDialog.value = true
        }
    }

    private val _monthlyBudget = MutableStateFlow(25000.0)
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    private val _customCategories = MutableStateFlow<List<String>>(emptyList())
    val customCategories: StateFlow<List<String>> = _customCategories.asStateFlow()

    private val _customExpenseCategories = MutableStateFlow<List<String>>(emptyList())
    val customExpenseCategories: StateFlow<List<String>> = _customExpenseCategories.asStateFlow()

    private val _customIncomeCategories = MutableStateFlow<List<String>>(emptyList())
    val customIncomeCategories: StateFlow<List<String>> = _customIncomeCategories.asStateFlow()

    // GST Auto-Tax Reserve State
    private val _isGstEnabled = MutableStateFlow(true)
    val isGstEnabled: StateFlow<Boolean> = _isGstEnabled.asStateFlow()

    private val _gstRatePercent = MutableStateFlow(18.0)
    val gstRatePercent: StateFlow<Double> = _gstRatePercent.asStateFlow()

    // Monthly ₹50 Safe Vault State
    private val _isMonthlySafeEnabled = MutableStateFlow(true)
    val isMonthlySafeEnabled: StateFlow<Boolean> = _isMonthlySafeEnabled.asStateFlow()

    private val _monthlySafeAmount = MutableStateFlow(50.0)
    val monthlySafeAmount: StateFlow<Double> = _monthlySafeAmount.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    fun toggleGstEnabled(enabled: Boolean) {
        _isGstEnabled.value = enabled
        sharedPrefs.edit().putBoolean("is_gst_enabled", enabled).apply()
    }

    fun updateGstRate(rate: Double) {
        if (rate in 0.0..100.0) {
            _gstRatePercent.value = rate
            sharedPrefs.edit().putFloat("gst_rate_percent", rate.toFloat()).apply()
        }
    }

    fun toggleMonthlySafeEnabled(enabled: Boolean) {
        _isMonthlySafeEnabled.value = enabled
        sharedPrefs.edit().putBoolean("is_monthly_safe_enabled", enabled).apply()
    }

    fun updateMonthlySafeAmount(amount: Double) {
        if (amount > 0) {
            _monthlySafeAmount.value = amount
            sharedPrefs.edit().putFloat("monthly_safe_amount", amount.toFloat()).apply()
        }
    }

    val defaultExpenseCategories = listOf("Food", "Travel", "Rent", "Utilities", "Entertainment", "Shopping", "Home", "Others")
    val defaultIncomeCategories = listOf("Salary", "Freelance", "Investments", "Gifts", "Others")
    val defaultCategories = (defaultExpenseCategories + defaultIncomeCategories).distinct()

    val defaultGoalCategories = listOf(
        "Saving", "Investment", "Expenditure", "Travel", "Tech",
        "Shopping", "Vehicle", "Education", "Emergency"
    )

    private val _customGoalCategories = MutableStateFlow<List<String>>(emptyList())
    val goalCategories: StateFlow<List<String>> = _customGoalCategories.map { custom ->
        (defaultGoalCategories + custom).distinct()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultGoalCategories)

    val allCategories = _customCategories.map { custom ->
        (defaultCategories + custom).distinct()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultCategories)

    val expenseCategories = _customExpenseCategories.map { custom ->
        (defaultExpenseCategories + custom).distinct()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultExpenseCategories)

    val incomeCategories = _customIncomeCategories.map { custom ->
        (defaultIncomeCategories + custom).distinct()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultIncomeCategories)

    init {
        checkAndCalculateDailyStreak()
        _userName.value = sharedPrefs.getString("user_name", null)
        _userProfileImageUri.value = sharedPrefs.getString("user_profile_image_uri", null)
        _userDob.value = sharedPrefs.getString("user_dob", "24 December 1999") ?: "24 December 1999"
        _userJob.value = sharedPrefs.getString("user_job", "Successor Designer") ?: "Successor Designer"
        _userMonthlyIncome.value = sharedPrefs.getString("user_monthly_income", "500 - 3000 / year") ?: "500 - 3000 / year"
        _userGender.value = sharedPrefs.getString("user_gender", "Male") ?: "Male"
        _monthlyBudget.value = sharedPrefs.getFloat("monthly_budget", 25000.0f).toDouble()
        val savedCats = sharedPrefs.getStringSet("custom_categories", emptySet()) ?: emptySet()
        val savedExpenseCats = sharedPrefs.getStringSet("custom_expense_categories", null)
        val savedIncomeCats = sharedPrefs.getStringSet("custom_income_categories", null)

        val expSet = savedExpenseCats ?: (savedCats - defaultIncomeCategories.toSet())
        val incSet = savedIncomeCats ?: emptySet()

        _customExpenseCategories.value = expSet.toList().sorted()
        _customIncomeCategories.value = incSet.toList().sorted()
        _customCategories.value = (expSet + incSet + savedCats).distinct().sorted()

        val savedGoalCats = sharedPrefs.getStringSet("custom_goal_categories", emptySet()) ?: emptySet()
        _customGoalCategories.value = savedGoalCats.toList().sorted()
        
        _themeIndex.value = sharedPrefs.getInt("theme_index", 0)
        _customThemeHue.value = sharedPrefs.getFloat("custom_theme_hue", 200f)
        _selectedLanguage.value = sharedPrefs.getString("selected_language", "English") ?: "English"
        LanguageManager.applyAppLocale(getApplication(), _selectedLanguage.value)
        
        // Load Dark Mode setting
        com.example.ui.theme.isDarkModeActive = sharedPrefs.getBoolean("dark_mode_active", false)
        com.example.ui.theme.updateThemeColors(_themeIndex.value, _customThemeHue.value)

        // Load GST and Monthly Safe settings
        _isGstEnabled.value = sharedPrefs.getBoolean("is_gst_enabled", true)
        _gstRatePercent.value = sharedPrefs.getFloat("gst_rate_percent", 18.0f).toDouble()
        _isMonthlySafeEnabled.value = sharedPrefs.getBoolean("is_monthly_safe_enabled", true)
        _monthlySafeAmount.value = sharedPrefs.getFloat("monthly_safe_amount", 50.0f).toDouble()

        // Load custom category icons
        val iconsMap = mutableMapOf<String, String>()
        savedCats.forEach { cat ->
            iconsMap[cat] = sharedPrefs.getString("cat_icon_$cat", "Star") ?: "Star"
        }
        _categoryIcons.value = iconsMap

        // Compute initial storage & network values
        refreshUsageData()

        // Ensure no prefilled default goals are added automatically
        val hasSeeded = sharedPrefs.getBoolean("has_seeded_savings_goals", false)
        if (!hasSeeded) {
            sharedPrefs.edit().putBoolean("has_seeded_savings_goals", true).apply()
        }
    }



    fun updateLanguage(language: String) {
        _selectedLanguage.value = language
        sharedPrefs.edit().putString("selected_language", language).apply()
        LanguageManager.applyAppLocale(getApplication(), language)
    }

    fun toggleDarkMode() {
        val newMode = !com.example.ui.theme.isDarkModeActive
        com.example.ui.theme.isDarkModeActive = newMode
        sharedPrefs.edit().putBoolean("dark_mode_active", newMode).apply()
        // Re-trigger theme color updates so custom tinter runs
        com.example.ui.theme.updateThemeColors(_themeIndex.value, _customThemeHue.value)
    }

    fun refreshUsageData() {
        val context = getApplication<Application>()
        
        // 1. Storage Size
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val dbFile = context.getDatabasePath("finance_database")
            var bytes = if (dbFile.exists()) dbFile.length() else 0L
            val dbJournal = context.getDatabasePath("finance_database-journal")
            if (dbJournal.exists()) bytes += dbJournal.length()
            val dbWal = context.getDatabasePath("finance_database-wal")
            if (dbWal.exists()) bytes += dbWal.length()
            val dbShm = context.getDatabasePath("finance_database-shm")
            if (dbShm.exists()) bytes += dbShm.length()

            fun getFolderSize(dir: java.io.File?): Long {
                if (dir == null || !dir.exists()) return 0
                if (dir.isFile) return dir.length()
                var sum = 0L
                dir.listFiles()?.forEach { sum += getFolderSize(it) }
                return sum
            }
            bytes += getFolderSize(context.filesDir)
            bytes += getFolderSize(context.cacheDir)

            val kb = bytes / 1024.0
            val formatted = if (kb < 1024.0) {
                String.format(Locale.getDefault(), "%.2f KB", kb)
            } else {
                String.format(Locale.getDefault(), "%.2f MB", kb / 1024.0)
            }
            _storageSize.value = formatted
        }

        // 2. Data/Network Usage
        val uid = android.os.Process.myUid()
        val rx = android.net.TrafficStats.getUidRxBytes(uid)
        val tx = android.net.TrafficStats.getUidTxBytes(uid)
        val netBytes = (if (rx == android.net.TrafficStats.UNSUPPORTED.toLong()) 0L else rx) +
                       (if (tx == android.net.TrafficStats.UNSUPPORTED.toLong()) 0L else tx)
        
        val netKb = netBytes / 1024.0
        val formattedNet = if (netKb < 1024.0) {
            String.format(Locale.getDefault(), "%.2f KB", netKb)
        } else {
            String.format(Locale.getDefault(), "%.2f MB", netKb / 1024.0)
        }
        _dataSize.value = formattedNet
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

    fun saveUserProfile(name: String, dob: String, job: String, income: String, gender: String) {
        val trimmedName = name.trim()
        if (trimmedName.isNotEmpty()) {
            sharedPrefs.edit()
                .putString("user_name", trimmedName)
                .putString("user_dob", dob)
                .putString("user_job", job)
                .putString("user_monthly_income", income)
                .putString("user_gender", gender)
                .apply()
            _userName.value = trimmedName
            _userDob.value = dob
            _userJob.value = job
            _userMonthlyIncome.value = income
            _userGender.value = gender
        }
    }

    fun updateMonthlyBudget(newLimit: Double) {
        if (newLimit > 0.0) {
            sharedPrefs.edit().putFloat("monthly_budget", newLimit.toFloat()).apply()
            _monthlyBudget.value = newLimit
        }
    }

    fun addCustomCategory(category: String, type: String = "EXPENSE") {
        val trimmed = category.trim()
        if (trimmed.isEmpty()) return

        val prefKey = if (type == "INCOME") "custom_income_categories" else "custom_expense_categories"
        val currentTyped = sharedPrefs.getStringSet(prefKey, emptySet()) ?: emptySet()
        val updatedTyped = currentTyped + trimmed

        val currentLegacy = sharedPrefs.getStringSet("custom_categories", emptySet()) ?: emptySet()
        val updatedLegacy = currentLegacy + trimmed

        sharedPrefs.edit()
            .putStringSet(prefKey, updatedTyped)
            .putStringSet("custom_categories", updatedLegacy)
            .apply()

        if (type == "INCOME") {
            _customIncomeCategories.value = updatedTyped.toList().sorted()
        } else {
            _customExpenseCategories.value = updatedTyped.toList().sorted()
        }
        _customCategories.value = updatedLegacy.toList().sorted()

        // Set pending icon state in-memory first
        _categoryIcons.value = _categoryIcons.value + (trimmed to "Pending")

        // Map best Material Icon in background using Gemini API
        viewModelScope.launch {
            val prompt = """
                For a personal finance category named "$trimmed", what is the single best Material Icon name from this list of exact names?
                List of options:
                Home, ShoppingCart, DirectionsCar, Restaurant, LocalHospital, School, Work, Flight, SportsEsports, CardGiftcard, MonetizationOn, Settings, Pets, Star, Construction, Fastfood, Movie, FlightTakeoff, Coffee, ElectricBolt, WaterDrop, Checkroom, DirectionsBus, LocalGasStation, FitnessCenter, Event, Spa, Healing
                
                Reply with ONLY the exact name of the selected icon from the options, nothing else. No punctuation, no markdown.
            """.trimIndent()
            
            val chosenIcon = try {
                GeminiClient.getFinancialAdvice(
                    prompt = prompt,
                    systemPrompt = "You are a system adapter that maps category keywords to standard Material Icon names. Always output exactly one name."
                ).trim()
            } catch (e: Exception) {
                "Star"
            }

            val validIcons = listOf(
                "Home", "ShoppingCart", "DirectionsCar", "Restaurant", "LocalHospital", "School", "Work", "Flight", 
                "SportsEsports", "CardGiftcard", "MonetizationOn", "Settings", "Pets", "Star", "Construction", "Fastfood", 
                "Movie", "FlightTakeoff", "Coffee", "ElectricBolt", "WaterDrop", "Checkroom", "DirectionsBus", "LocalGasStation",
                "FitnessCenter", "Event", "Spa", "Healing"
            )
            val finalIcon = if (validIcons.contains(chosenIcon)) chosenIcon else "Star"

            // Save matched icon
            sharedPrefs.edit().putString("cat_icon_$trimmed", finalIcon).apply()
            _categoryIcons.value = _categoryIcons.value + (trimmed to finalIcon)
        }
    }

    fun addGoalCategory(category: String) {
        val trimmed = category.trim()
        if (trimmed.isBlank()) return
        val current = sharedPrefs.getStringSet("custom_goal_categories", emptySet()) ?: emptySet()
        val updated = (current + trimmed).sorted()
        sharedPrefs.edit().putStringSet("custom_goal_categories", updated.toSet()).apply()
        _customGoalCategories.value = updated
    }

    fun deleteGoalCategory(category: String) {
        val trimmed = category.trim()
        if (trimmed.isBlank()) return
        val current = sharedPrefs.getStringSet("custom_goal_categories", emptySet()) ?: emptySet()
        val updated = (current - trimmed).sorted()
        sharedPrefs.edit().putStringSet("custom_goal_categories", updated.toSet()).apply()
        _customGoalCategories.value = updated

        // Reassign any goals using deleted category to "Saving"
        viewModelScope.launch {
            val allGoals = repository.allSavingsGoals.first()
            allGoals.forEach { g ->
                if (g.category.equals(trimmed, ignoreCase = true)) {
                    repository.updateSavingsGoal(g.copy(category = "Saving"))
                }
            }
        }
    }

    fun deleteCustomCategory(category: String) {
        val trimmed = category.trim()
        val current = sharedPrefs.getStringSet("custom_categories", emptySet()) ?: emptySet()
        val updated = current - trimmed
        sharedPrefs.edit()
            .putStringSet("custom_categories", updated)
            .remove("cat_icon_$trimmed")
            .apply()
        _customCategories.value = updated.toList().sorted()
        _categoryIcons.value = _categoryIcons.value - trimmed
        
        // Update all existing expenses/transactions that belong to the deleted category to "Others"
        viewModelScope.launch {
            val allExpensesList = repository.allExpenses.first()
            allExpensesList.forEach { exp ->
                if (exp.category == trimmed) {
                    repository.updateExpense(exp.copy(category = "Others"))
                }
            }
        }
    }

    fun renameCustomCategory(oldName: String, newName: String) {
        val trimmedOld = oldName.trim()
        val trimmedNew = newName.trim()
        if (trimmedNew.isEmpty() || trimmedOld == trimmedNew) return
        
        val current = sharedPrefs.getStringSet("custom_categories", emptySet()) ?: emptySet()
        if (current.contains(trimmedOld)) {
            val updated = current - trimmedOld + trimmedNew
            val savedIcon = sharedPrefs.getString("cat_icon_$trimmedOld", "Star") ?: "Star"
            sharedPrefs.edit()
                .putStringSet("custom_categories", updated)
                .remove("cat_icon_$trimmedOld")
                .putString("cat_icon_$trimmedNew", savedIcon)
                .apply()
            _customCategories.value = updated.toList().sorted()
            
            // Update in categoryIcons map
            val updatedIcons = _categoryIcons.value.toMutableMap()
            updatedIcons.remove(trimmedOld)
            updatedIcons[trimmedNew] = savedIcon
            _categoryIcons.value = updatedIcons
            
            // Update all existing expenses/transactions that belong to the old category to the new category name
            viewModelScope.launch {
                val allExpensesList = repository.allExpenses.first()
                allExpensesList.forEach { exp ->
                    if (exp.category == trimmedOld) {
                        repository.updateExpense(exp.copy(category = trimmedNew))
                    }
                }
            }
        }
    }

    // DB Operations
    fun addExpense(
        amount: Double,
        category: String,
        date: Long,
        note: String?,
        imagePath: String? = null,
        type: String = "EXPENSE"
    ) {
        viewModelScope.launch {
            repository.insertExpense(
                Expense(
                    amount = amount,
                    category = category,
                    date = date,
                    note = note,
                    imagePath = imagePath,
                    type = type
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

    fun addSavingsGoal(
        name: String,
        targetAmount: Double,
        initialAmount: Double = 0.0,
        targetDate: Long = 0L,
        frequency: String = "WEEKLY",
        contributionAmount: Double = 0.0,
        isAutoGap: Boolean = true,
        iconTag: String = "🎯",
        category: String = "Saving",
        imageUri: String? = null
    ) {
        viewModelScope.launch {
            repository.insertSavingsGoal(
                SavingsGoal(
                    name = name,
                    targetAmount = targetAmount,
                    currentAmount = initialAmount,
                    targetDate = targetDate,
                    frequency = frequency,
                    contributionAmount = contributionAmount,
                    isAutoGap = isAutoGap,
                    iconTag = iconTag,
                    category = category,
                    imageUri = imageUri
                )
            )
            if (initialAmount > 0) {
                repository.insertExpense(
                    Expense(
                        amount = initialAmount,
                        category = "Locked Savings",
                        date = System.currentTimeMillis(),
                        note = "🔒 Initial savings locked in goal: $name",
                        type = "EXPENSE"
                    )
                )
            }
        }
    }

    fun updateSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.updateSavingsGoal(goal)
        }
    }

    fun quickDepositToGoal(goal: SavingsGoal, amount: Double) {
        if (amount <= 0) return
        viewModelScope.launch {
            val updated = goal.copy(currentAmount = goal.currentAmount + amount)
            repository.updateSavingsGoal(updated)

            // Insert Locked Savings transaction to deduct from spendable balance
            repository.insertExpense(
                Expense(
                    amount = amount,
                    category = "Locked Savings",
                    date = System.currentTimeMillis(),
                    note = "🔒 Saved & locked in ${goal.name}",
                    type = "EXPENSE"
                )
            )

            // Deduct from primary bank account if exists
            val primaryAccount = accounts.value.firstOrNull()
            if (primaryAccount != null) {
                val updatedAcc = primaryAccount.copy(balance = (primaryAccount.balance - amount).coerceAtLeast(0.0))
                repository.updateAccount(updatedAcc)
            }
        }
    }

    fun quickDeductFromGoal(goal: SavingsGoal, amount: Double) {
        if (amount <= 0 || goal.currentAmount <= 0) return
        val deductAmount = amount.coerceAtMost(goal.currentAmount)
        viewModelScope.launch {
            val updated = goal.copy(currentAmount = (goal.currentAmount - deductAmount).coerceAtLeast(0.0))
            repository.updateSavingsGoal(updated)

            // Insert Goal Withdrawal transaction to add back to spendable balance
            repository.insertExpense(
                Expense(
                    amount = deductAmount,
                    category = "Goal Withdrawal",
                    date = System.currentTimeMillis(),
                    note = "🔓 Deducted/unlocked from ${goal.name}",
                    type = "INCOME"
                )
            )

            // Add back to primary bank account if exists
            val primaryAccount = accounts.value.firstOrNull()
            if (primaryAccount != null) {
                val updatedAcc = primaryAccount.copy(balance = primaryAccount.balance + deductAmount)
                repository.updateAccount(updatedAcc)
            }
        }
    }

    // Automated Safe Vault (₹50/Month Auto-Reserve Safe)
    fun processMonthlySafeVault(monthlyRate: Double = 50.0) {
        val currentMonth = SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(Date())
        val lastProcessedMonth = sharedPrefs.getString("last_safe_vault_month", "")

        viewModelScope.launch {
            val currentGoals = repository.allSavingsGoals.first()
            var safeGoal = currentGoals.find { it.name.contains("Safe Vault", ignoreCase = true) || it.name.contains("Monthly Safe", ignoreCase = true) }

            if (safeGoal == null) {
                val newId = repository.insertSavingsGoal(
                    SavingsGoal(
                        name = "Safe Vault 🔒",
                        targetAmount = 1200.0,
                        currentAmount = monthlyRate,
                        frequency = "MONTHLY",
                        contributionAmount = monthlyRate,
                        isAutoGap = false,
                        iconTag = "🔐"
                    )
                )
                sharedPrefs.edit().putString("last_safe_vault_month", currentMonth).apply()
                repository.insertExpense(
                    Expense(
                        amount = monthlyRate,
                        category = "Locked Savings",
                        date = System.currentTimeMillis(),
                        note = "🔒 Monthly Safe Vault Deposit (₹%,.0f auto-reserved)".format(monthlyRate),
                        type = "EXPENSE"
                    )
                )
            } else if (lastProcessedMonth != currentMonth) {
                val updated = safeGoal.copy(currentAmount = safeGoal.currentAmount + monthlyRate)
                repository.updateSavingsGoal(updated)
                sharedPrefs.edit().putString("last_safe_vault_month", currentMonth).apply()
                repository.insertExpense(
                    Expense(
                        amount = monthlyRate,
                        category = "Locked Savings",
                        date = System.currentTimeMillis(),
                        note = "🔒 Monthly Safe Vault Deposit (₹%,.0f auto-reserved for $currentMonth)".format(monthlyRate),
                        type = "EXPENSE"
                    )
                )
            }
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
        }
    }

    fun allocateToGoal(goal: SavingsGoal, account: Account, amount: Double) {
        if (amount <= 0) return
        viewModelScope.launch {
            val success = repository.allocateToSavingsGoal(goal, account, amount)
            if (success) {
                repository.insertExpense(
                    Expense(
                        amount = amount,
                        category = "Locked Savings",
                        date = System.currentTimeMillis(),
                        note = "🔒 Saved & locked in ${goal.name}",
                        type = "EXPENSE"
                    )
                )
            }
        }
    }

    fun withdrawFromGoal(goal: SavingsGoal, account: Account, amount: Double) {
        if (amount <= 0 || goal.currentAmount < amount) return
        viewModelScope.launch {
            val success = repository.withdrawFromSavingsGoal(goal, account, amount)
            if (success) {
                repository.insertExpense(
                    Expense(
                        amount = amount,
                        category = "Goal Withdrawal",
                        date = System.currentTimeMillis(),
                        note = "Unlocked from goal: ${goal.name}",
                        type = "INCOME"
                    )
                )
            }
        }
    }

    fun deductFromSavingsGoal(goal: SavingsGoal, amountToDeduct: Double, accountId: Int? = null) {
        if (amountToDeduct <= 0 || goal.currentAmount <= 0) return
        val actualDeduction = amountToDeduct.coerceAtMost(goal.currentAmount)
        viewModelScope.launch {
            val updatedGoal = goal.copy(currentAmount = goal.currentAmount - actualDeduction)
            repository.updateSavingsGoal(updatedGoal)

            repository.insertExpense(
                Expense(
                    amount = actualDeduction,
                    category = "Goal Withdrawal",
                    date = System.currentTimeMillis(),
                    note = "🔓 Unlocked ₹%,.0f from goal: ${goal.name}".format(actualDeduction),
                    type = "INCOME"
                )
            )

            if (accountId != null) {
                val accList = accounts.value
                val acc = accList.find { it.id == accountId }
                if (acc != null) {
                    repository.updateAccount(acc.copy(balance = acc.balance + actualDeduction))
                }
            }

            _toastMessage.value = "🔓 Deducted ₹%,.0f from ${goal.name}".format(actualDeduction)
        }
    }

    fun depositToSafe(amount: Double) {
        if (amount <= 0) return
        viewModelScope.launch {
            val allGoals = savingsGoals.first()
            var safeGoal = allGoals.find { it.name.contains("Monthly Safe", ignoreCase = true) || it.name.contains("Safe Vault", ignoreCase = true) }
            if (safeGoal == null) {
                safeGoal = SavingsGoal(
                    name = "Monthly Safe Vault 🔐",
                    targetAmount = 600.0,
                    currentAmount = amount,
                    frequency = "MONTHLY",
                    contributionAmount = 50.0,
                    isAutoGap = true,
                    iconTag = "🔐"
                )
                repository.insertSavingsGoal(safeGoal)
            } else {
                repository.updateSavingsGoal(safeGoal.copy(currentAmount = safeGoal.currentAmount + amount))
            }

            repository.insertExpense(
                Expense(
                    amount = amount,
                    category = "Locked Savings",
                    date = System.currentTimeMillis(),
                    note = "🔐 Manual deposit to Safe Vault (₹%,.0f)".format(amount),
                    type = "EXPENSE"
                )
            )
            _toastMessage.value = "🔐 Deposited ₹%,.0f to Safe Vault".format(amount)
        }
    }

    fun withdrawFromSafe(amount: Double) {
        if (amount <= 0) return
        viewModelScope.launch {
            val allGoals = savingsGoals.first()
            val safeGoal = allGoals.find { it.name.contains("Monthly Safe", ignoreCase = true) || it.name.contains("Safe Vault", ignoreCase = true) }
            if (safeGoal != null && safeGoal.currentAmount > 0) {
                val actual = amount.coerceAtMost(safeGoal.currentAmount)
                repository.updateSavingsGoal(safeGoal.copy(currentAmount = safeGoal.currentAmount - actual))

                repository.insertExpense(
                    Expense(
                        amount = actual,
                        category = "Goal Withdrawal",
                        date = System.currentTimeMillis(),
                        note = "🔓 Unlocked ₹%,.0f from Safe Vault".format(actual),
                        type = "INCOME"
                    )
                )
                _toastMessage.value = "🔓 Unlocked ₹%,.0f from Safe Vault".format(actual)
            }
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
