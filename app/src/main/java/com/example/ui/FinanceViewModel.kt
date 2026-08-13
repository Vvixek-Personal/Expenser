package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

class FinanceViewModel(
    application: Application,
    private val repository: FinanceRepository
) : AndroidViewModel(application) {

    val billsList = mutableStateListOf<BillEntry>()
    val remindersList = mutableStateListOf<ReminderEntry>()

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

    // Daily Spending Insight states (Powered by Gemini AI Advisor)
    private val _dailySpendingInsight = MutableStateFlow<String?>(null)
    val dailySpendingInsight: StateFlow<String?> = _dailySpendingInsight.asStateFlow()

    private val _isInsightLoading = MutableStateFlow(false)
    val isInsightLoading: StateFlow<Boolean> = _isInsightLoading.asStateFlow()

    private val _insightLastUpdated = MutableStateFlow<Long?>(null)
    val insightLastUpdated: StateFlow<Long?> = _insightLastUpdated.asStateFlow()

    // Selected Language Preference
    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    // SharedPreferences for local configuration
    private val _themeIndex = MutableStateFlow(0)
    val themeIndex: StateFlow<Int> = _themeIndex.asStateFlow()

    private val _customThemeHue = MutableStateFlow(200f)
    val customThemeHue: StateFlow<Float> = _customThemeHue.asStateFlow()

    private val _themeMode = MutableStateFlow("light")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _isFollowDeviceColors = MutableStateFlow(false)
    val isFollowDeviceColors: StateFlow<Boolean> = _isFollowDeviceColors.asStateFlow()

    val appSettingsManager = AppSettingsManager.getInstance(getApplication())
    private val sharedPrefs = getApplication<Application>().getSharedPreferences("finance_prefs", android.content.Context.MODE_PRIVATE)

    // Live Storage and Network/Data Usage states
    private val _storageSize = MutableStateFlow("0.0 KB")
    val storageSize: StateFlow<String> = _storageSize.asStateFlow()

    private val _dataSize = MutableStateFlow("0.0 KB")
    val dataSize: StateFlow<String> = _dataSize.asStateFlow()

    // Map storing Category Name to Material Icon Name
    private val _categoryIcons = MutableStateFlow<Map<String, String>>(emptyMap())
    val categoryIcons: StateFlow<Map<String, String>> = _categoryIcons.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _userProfileImageUri = MutableStateFlow<String?>(null)
    val userProfileImageUri: StateFlow<String?> = _userProfileImageUri.asStateFlow()

    fun updateUserProfileImageFromUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, "user_profile_avatar.jpg")
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                val localUri = Uri.fromFile(file).toString()
                _userProfileImageUri.value = localUri
                sharedPrefs.edit().putString("user_profile_image_uri", localUri).apply()
            } catch (e: Exception) {
                e.printStackTrace()
                _userProfileImageUri.value = uri.toString()
                sharedPrefs.edit().putString("user_profile_image_uri", uri.toString()).apply()
            }
        }
    }

    fun removeUserProfileImage(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, "user_profile_avatar.jpg")
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _userProfileImageUri.value = null
            sharedPrefs.edit().remove("user_profile_image_uri").apply()
        }
    }

    fun updateUserProfileImageUri(uriString: String?) {
        _userProfileImageUri.value = uriString
        if (uriString.isNullOrBlank()) {
            sharedPrefs.edit().remove("user_profile_image_uri").apply()
        } else {
            sharedPrefs.edit().putString("user_profile_image_uri", uriString).apply()
        }
    }

    private val _userDob = MutableStateFlow("24 December 1999")
    val userDob: StateFlow<String> = _userDob.asStateFlow()

    private val _userJob = MutableStateFlow("Successor Designer")
    val userJob: StateFlow<String> = _userJob.asStateFlow()

    private val _userMonthlyIncome = MutableStateFlow("500 - 3000 / year")
    val userMonthlyIncome: StateFlow<String> = _userMonthlyIncome.asStateFlow()

    private val _userGender = MutableStateFlow("Male")
    val userGender: StateFlow<String> = _userGender.asStateFlow()

    // Passcode Security PIN State & Logic
    private val _appPin = MutableStateFlow<String?>(null)
    val appPin: StateFlow<String?> = _appPin.asStateFlow()

    private val _hasPromptedFirstRunPin = MutableStateFlow<Boolean>(false)
    val hasPromptedFirstRunPin: StateFlow<Boolean> = _hasPromptedFirstRunPin.asStateFlow()

    private val _isAppLocked = MutableStateFlow<Boolean>(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    fun setAppPin(pin: String?) {
        _appPin.value = pin
        if (pin.isNullOrBlank()) {
            sharedPrefs.edit().remove("app_pin").apply()
            _isAppLocked.value = false
        } else {
            sharedPrefs.edit().putString("app_pin", pin).apply()
            _isAppLocked.value = false
        }
    }

    fun markFirstRunPinPrompted() {
        _hasPromptedFirstRunPin.value = true
        sharedPrefs.edit().putBoolean("pin_prompted_first_run", true).apply()
    }

    fun unlockAppWithPin(enteredPin: String): Boolean {
        val currentPin = _appPin.value
        if (currentPin != null && currentPin == enteredPin) {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun lockApp() {
        if (!_appPin.value.isNullOrBlank()) {
            _isAppLocked.value = true
        }
    }

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

    // Currency Settings
    private val _selectedCurrencyCode = MutableStateFlow("INR")
    val selectedCurrencyCode: StateFlow<String> = _selectedCurrencyCode.asStateFlow()

    private val _selectedCurrencySymbol = MutableStateFlow("₹")
    val selectedCurrencySymbol: StateFlow<String> = _selectedCurrencySymbol.asStateFlow()

    private val _selectedCurrencyName = MutableStateFlow("Indian Rupee")
    val selectedCurrencyName: StateFlow<String> = _selectedCurrencyName.asStateFlow()

    private val _statsCurrencyCode = MutableStateFlow("INR")
    val statsCurrencyCode: StateFlow<String> = _statsCurrencyCode.asStateFlow()

    private val _statsCurrencySymbol = MutableStateFlow("₹")
    val statsCurrencySymbol: StateFlow<String> = _statsCurrencySymbol.asStateFlow()

    private val _statsCurrencyName = MutableStateFlow("Indian Rupee")
    val statsCurrencyName: StateFlow<String> = _statsCurrencyName.asStateFlow()

    private val _lastExchangeRateUpdate = MutableStateFlow("10 Aug 2026, 08:30 AM")
    val lastExchangeRateUpdate: StateFlow<String> = _lastExchangeRateUpdate.asStateFlow()

    private val _isAutoExchangeRateUpdateEnabled = MutableStateFlow(true)
    val isAutoExchangeRateUpdateEnabled: StateFlow<Boolean> = _isAutoExchangeRateUpdateEnabled.asStateFlow()

    private val _isUpdatingExchangeRates = MutableStateFlow(false)
    val isUpdatingExchangeRates: StateFlow<Boolean> = _isUpdatingExchangeRates.asStateFlow()

    // Appearance & Layout Preferences
    private val _textSizeOption = MutableStateFlow("Medium")
    val textSizeOption: StateFlow<String> = _textSizeOption.asStateFlow()

    private val _isCompactLayout = MutableStateFlow(false)
    val isCompactLayout: StateFlow<Boolean> = _isCompactLayout.asStateFlow()

    private val _isAnimationEnabled = MutableStateFlow(true)
    val isAnimationEnabled: StateFlow<Boolean> = _isAnimationEnabled.asStateFlow()

    // Date & Time Preferences
    private val _dateFormat = MutableStateFlow("dd/MM/yyyy")
    val dateFormat: StateFlow<String> = _dateFormat.asStateFlow()

    private val _firstDayOfWeek = MutableStateFlow("Monday")
    val firstDayOfWeek: StateFlow<String> = _firstDayOfWeek.asStateFlow()

    // Bill Preferences
    private val _billReminderTiming = MutableStateFlow("1 Day Before")
    val billReminderTiming: StateFlow<String> = _billReminderTiming.asStateFlow()

    private val _billAutoMarkPaid = MutableStateFlow(false)
    val billAutoMarkPaid: StateFlow<Boolean> = _billAutoMarkPaid.asStateFlow()

    private val _billOverdueAlert = MutableStateFlow(true)
    val billOverdueAlert: StateFlow<Boolean> = _billOverdueAlert.asStateFlow()

    private val _billDefaultRecurrence = MutableStateFlow("Monthly")
    val billDefaultRecurrence: StateFlow<String> = _billDefaultRecurrence.asStateFlow()

    private val _billRecurringEnd = MutableStateFlow("Never")
    val billRecurringEnd: StateFlow<String> = _billRecurringEnd.asStateFlow()

    private val _billDefaultCategory = MutableStateFlow("Utilities")
    val billDefaultCategory: StateFlow<String> = _billDefaultCategory.asStateFlow()

    private val _billArchiveDays = MutableStateFlow("30 Days")
    val billArchiveDays: StateFlow<String> = _billArchiveDays.asStateFlow()

    private val _billShowUpcomingDashboard = MutableStateFlow(true)
    val billShowUpcomingDashboard: StateFlow<Boolean> = _billShowUpcomingDashboard.asStateFlow()

    private val _billUpcomingDays = MutableStateFlow("7 Days")
    val billUpcomingDays: StateFlow<String> = _billUpcomingDays.asStateFlow()

    private val _billSortOrder = MutableStateFlow("Due Date (Nearest)")
    val billSortOrder: StateFlow<String> = _billSortOrder.asStateFlow()

    private val _billDefaultFilter = MutableStateFlow("All Bills")
    val billDefaultFilter: StateFlow<String> = _billDefaultFilter.asStateFlow()

    private val _billShowNotes = MutableStateFlow(true)
    val billShowNotes: StateFlow<Boolean> = _billShowNotes.asStateFlow()

    // General Preferences for Categories & Tags
    private val _preventDeleteUsedCategories = MutableStateFlow(true)
    val preventDeleteUsedCategories: StateFlow<Boolean> = _preventDeleteUsedCategories.asStateFlow()

    private val _showCategoryInTransactionList = MutableStateFlow(true)
    val showCategoryInTransactionList: StateFlow<Boolean> = _showCategoryInTransactionList.asStateFlow()

    private val _deletedCategories = MutableStateFlow<Set<String>>(emptySet())
    val deletedCategories: StateFlow<Set<String>> = _deletedCategories.asStateFlow()

    val defaultBudgetCategories = listOf("Overall", "Food & Dining", "Bills & Utilities", "Shopping", "Entertainment", "Transport", "Healthcare", "Personal Care", "Education", "Travel", "Others")
    private val _customBudgetCategories = MutableStateFlow<List<String>>(emptyList())

    val budgetCategories: StateFlow<List<String>> = kotlinx.coroutines.flow.combine(_customBudgetCategories, _deletedCategories) { custom, deleted ->
        ((defaultBudgetCategories + custom).distinct() - deleted).toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultBudgetCategories)

    val defaultTags = listOf("Personal", "Work", "Tax Deductible", "Urgent", "Vacation", "Health", "Home", "Shopping", "Family", "Business")
    private val _customTags = MutableStateFlow<List<String>>(emptyList())
    private val _deletedTags = MutableStateFlow<Set<String>>(emptySet())

    val allTags: StateFlow<List<String>> = kotlinx.coroutines.flow.combine(_customTags, _deletedTags) { custom, deleted ->
        ((defaultTags + custom).distinct() - deleted).toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultTags)

    private val _categoryColorMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val categoryColorMap: StateFlow<Map<String, String>> = _categoryColorMap.asStateFlow()

    private val _tagColorMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val tagColorMap: StateFlow<Map<String, String>> = _tagColorMap.asStateFlow()

    // Budget Preferences
    private val _budgetWarning80 = MutableStateFlow(true)
    val budgetWarning80: StateFlow<Boolean> = _budgetWarning80.asStateFlow()

    private val _budgetWarning90 = MutableStateFlow(true)
    val budgetWarning90: StateFlow<Boolean> = _budgetWarning90.asStateFlow()

    private val _budgetWarning100 = MutableStateFlow(true)
    val budgetWarning100: StateFlow<Boolean> = _budgetWarning100.asStateFlow()

    private val _budgetIncludeRecurringBills = MutableStateFlow(true)
    val budgetIncludeRecurringBills: StateFlow<Boolean> = _budgetIncludeRecurringBills.asStateFlow()

    // Savings Goals Preferences
    private val _goalViewMode = MutableStateFlow("Grid")
    val goalViewMode: StateFlow<String> = _goalViewMode.asStateFlow()

    private val _goalProgressStyle = MutableStateFlow("Circle")
    val goalProgressStyle: StateFlow<String> = _goalProgressStyle.asStateFlow()

    // Transaction Preferences
    private val _defaultTxType = MutableStateFlow("EXPENSE")
    val defaultTxType: StateFlow<String> = _defaultTxType.asStateFlow()

    private val _rememberLastCategory = MutableStateFlow(true)
    val rememberLastCategory: StateFlow<Boolean> = _rememberLastCategory.asStateFlow()

    private val _confirmTxDelete = MutableStateFlow(true)
    val confirmTxDelete: StateFlow<Boolean> = _confirmTxDelete.asStateFlow()

    private val _groupByDate = MutableStateFlow(true)
    val groupByDate: StateFlow<Boolean> = _groupByDate.asStateFlow()

    // Security & Privacy Preferences
    private val _lockOnRestart = MutableStateFlow(true)
    val lockOnRestart: StateFlow<Boolean> = _lockOnRestart.asStateFlow()

    private val _autoLockDuration = MutableStateFlow("Immediately")
    val autoLockDuration: StateFlow<String> = _autoLockDuration.asStateFlow()

    private val _biometricEnabled = MutableStateFlow(false)
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()

    private val _hideSensitiveAmounts = MutableStateFlow(false)
    val hideSensitiveAmounts: StateFlow<Boolean> = _hideSensitiveAmounts.asStateFlow()

    private val _screenshotProtection = MutableStateFlow(false)
    val screenshotProtection: StateFlow<Boolean> = _screenshotProtection.asStateFlow()

    private val _privacyModeEnabled = MutableStateFlow(false)
    val privacyModeEnabled: StateFlow<Boolean> = _privacyModeEnabled.asStateFlow()

    // Session-only reveal override for Privacy Blur Mode — resets to false
    // (masked) every time the ViewModel is recreated, i.e. every app launch.
    // Tapping a masked amount or the eye icon flips this; it's intentionally
    // NOT persisted, so the app is always masked-by-default on open.
    private val _privacyRevealOverride = MutableStateFlow(false)
    val privacyRevealOverride: StateFlow<Boolean> = _privacyRevealOverride.asStateFlow()

    fun togglePrivacyReveal() {
        _privacyRevealOverride.value = !_privacyRevealOverride.value
    }

    // Last-used category per transaction type, for "Remember Last Category".
    // Persisted so it survives app restarts, not just the current session.
    private val _lastUsedExpenseCategory = MutableStateFlow<String?>(null)
    val lastUsedExpenseCategory: StateFlow<String?> = _lastUsedExpenseCategory.asStateFlow()

    private val _lastUsedIncomeCategory = MutableStateFlow<String?>(null)
    val lastUsedIncomeCategory: StateFlow<String?> = _lastUsedIncomeCategory.asStateFlow()

    // Backup & Restore Preferences
    private val _autoBackupEnabled = MutableStateFlow(true)
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled.asStateFlow()

    private val _lastBackupTimestamp = MutableStateFlow("Never")
    val lastBackupTimestamp: StateFlow<String> = _lastBackupTimestamp.asStateFlow()

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
    val goalCategories: StateFlow<List<String>> = kotlinx.coroutines.flow.combine(_customGoalCategories, _deletedCategories) { custom, deleted ->
        ((defaultGoalCategories + custom).distinct() - deleted).toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultGoalCategories)

    val allCategories: StateFlow<List<String>> = kotlinx.coroutines.flow.combine(_customCategories, _deletedCategories) { custom, deleted ->
        ((defaultCategories + custom).distinct() - deleted).toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultCategories)

    val expenseCategories: StateFlow<List<String>> = kotlinx.coroutines.flow.combine(_customExpenseCategories, _deletedCategories) { custom, deleted ->
        ((defaultExpenseCategories + custom).distinct() - deleted).toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultExpenseCategories)

    val incomeCategories: StateFlow<List<String>> = kotlinx.coroutines.flow.combine(_customIncomeCategories, _deletedCategories) { custom, deleted ->
        ((defaultIncomeCategories + custom).distinct() - deleted).toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultIncomeCategories)

    init {
        checkAndCalculateDailyStreak()

        viewModelScope.launch {
            appSettingsManager.state.collect { settings ->
                _selectedLanguage.value = settings.language
                _themeIndex.value = settings.themeIndex
                _themeMode.value = settings.themeMode
                _customThemeHue.value = settings.customThemeHue
                _isFollowDeviceColors.value = settings.isFollowDeviceColors
                _selectedCurrencyCode.value = settings.currencyCode
                _selectedCurrencySymbol.value = settings.currencySymbol
                _selectedCurrencyName.value = settings.currencyName
                _statsCurrencyCode.value = settings.statsCurrencyCode
                _statsCurrencySymbol.value = settings.statsCurrencySymbol
                _statsCurrencyName.value = settings.statsCurrencyName
                _monthlyBudget.value = settings.monthlyBudget
                _appPin.value = settings.appPin
                _isAppLocked.value = settings.isAppLocked
                _privacyModeEnabled.value = settings.privacyModeEnabled
                _hideSensitiveAmounts.value = settings.hideSensitiveAmounts
                _autoBackupEnabled.value = settings.autoBackupEnabled
                _lastBackupTimestamp.value = settings.lastBackupTimestamp
            }
        }
        _userName.value = sharedPrefs.getString("user_name", null)
        val savedImageUri = sharedPrefs.getString("user_profile_image_uri", null)
        if (!savedImageUri.isNullOrBlank()) {
            val localFile = File(getApplication<Application>().filesDir, "user_profile_avatar.jpg")
            if (savedImageUri.startsWith("content://")) {
                try {
                    val contentUri = Uri.parse(savedImageUri)
                    getApplication<Application>().contentResolver.openInputStream(contentUri)?.use { inputStream ->
                        FileOutputStream(localFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    val localUri = Uri.fromFile(localFile).toString()
                    _userProfileImageUri.value = localUri
                    sharedPrefs.edit().putString("user_profile_image_uri", localUri).apply()
                } catch (e: Exception) {
                    if (localFile.exists() && localFile.length() > 0) {
                        val localUri = Uri.fromFile(localFile).toString()
                        _userProfileImageUri.value = localUri
                        sharedPrefs.edit().putString("user_profile_image_uri", localUri).apply()
                    } else {
                        _userProfileImageUri.value = savedImageUri
                    }
                }
            } else if (savedImageUri.startsWith("file://")) {
                val filePath = Uri.parse(savedImageUri).path
                val file = if (filePath != null) File(filePath) else localFile
                if (file.exists() && file.length() > 0) {
                    _userProfileImageUri.value = Uri.fromFile(file).toString()
                } else if (localFile.exists() && localFile.length() > 0) {
                    val localUri = Uri.fromFile(localFile).toString()
                    _userProfileImageUri.value = localUri
                    sharedPrefs.edit().putString("user_profile_image_uri", localUri).apply()
                } else {
                    _userProfileImageUri.value = null
                    sharedPrefs.edit().remove("user_profile_image_uri").apply()
                }
            } else {
                _userProfileImageUri.value = savedImageUri
            }
        }
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
        
        // Load Theme Mode & Follow Device Colors
        val defaultMode = if (sharedPrefs.getBoolean("dark_mode_active", false)) "dark" else "light"
        val savedMode = sharedPrefs.getString("theme_mode", defaultMode) ?: defaultMode
        _themeMode.value = savedMode
        com.example.ui.theme.themeModeState = savedMode
        com.example.ui.theme.isDarkModeActive = (savedMode == "dark")

        val savedFollowColors = sharedPrefs.getBoolean("follow_device_colors", false)
        _isFollowDeviceColors.value = savedFollowColors
        com.example.ui.theme.isFollowDeviceColorsState = savedFollowColors

        // Load Passcode PIN Security Settings
        val savedPin = sharedPrefs.getString("app_pin", null)
        _appPin.value = savedPin
        _hasPromptedFirstRunPin.value = sharedPrefs.getBoolean("pin_prompted_first_run", false)
        if (!savedPin.isNullOrBlank()) {
            _isAppLocked.value = true
        }
        com.example.ui.theme.updateThemeColors(_themeIndex.value, _customThemeHue.value)

        // Load GST and Monthly Safe settings
        _isGstEnabled.value = sharedPrefs.getBoolean("is_gst_enabled", true)
        _gstRatePercent.value = sharedPrefs.getFloat("gst_rate_percent", 18.0f).toDouble()
        _isMonthlySafeEnabled.value = sharedPrefs.getBoolean("is_monthly_safe_enabled", true)
        _monthlySafeAmount.value = sharedPrefs.getFloat("monthly_safe_amount", 50.0f).toDouble()

        // Load last-used category memory (for "Remember Last Category")
        _lastUsedExpenseCategory.value = sharedPrefs.getString("last_used_expense_category", null)
        _lastUsedIncomeCategory.value = sharedPrefs.getString("last_used_income_category", null)

        // Load Currency Settings
        _selectedCurrencyCode.value = sharedPrefs.getString("selected_currency_code", "INR") ?: "INR"
        _selectedCurrencySymbol.value = sharedPrefs.getString("selected_currency_symbol", "₹") ?: "₹"
        _selectedCurrencyName.value = sharedPrefs.getString("selected_currency_name", "Indian Rupee") ?: "Indian Rupee"
        _statsCurrencyCode.value = sharedPrefs.getString("stats_currency_code", "INR") ?: "INR"
        _statsCurrencySymbol.value = sharedPrefs.getString("stats_currency_symbol", "₹") ?: "₹"
        _statsCurrencyName.value = sharedPrefs.getString("stats_currency_name", "Indian Rupee") ?: "Indian Rupee"
        _lastExchangeRateUpdate.value = sharedPrefs.getString("last_exchange_rate_update", "10 Aug 2026, 08:30 AM") ?: "10 Aug 2026, 08:30 AM"
        _isAutoExchangeRateUpdateEnabled.value = sharedPrefs.getBoolean("is_auto_exchange_rate_update", true)

        // Load Bills Preferences
        _billReminderTiming.value = sharedPrefs.getString("bill_reminder_timing", "1 Day Before") ?: "1 Day Before"
        _billAutoMarkPaid.value = sharedPrefs.getBoolean("bill_auto_mark_paid", false)
        _billOverdueAlert.value = sharedPrefs.getBoolean("bill_overdue_alert", true)
        _billDefaultRecurrence.value = sharedPrefs.getString("bill_default_recurrence", "Monthly") ?: "Monthly"
        _billRecurringEnd.value = sharedPrefs.getString("bill_recurring_end", "Never") ?: "Never"
        _billDefaultCategory.value = sharedPrefs.getString("bill_default_category", "Utilities") ?: "Utilities"
        _billArchiveDays.value = sharedPrefs.getString("bill_archive_days", "30 Days") ?: "30 Days"
        _billShowUpcomingDashboard.value = sharedPrefs.getBoolean("bill_show_upcoming_dashboard", true)
        _billUpcomingDays.value = sharedPrefs.getString("bill_upcoming_days", "7 Days") ?: "7 Days"
        _billSortOrder.value = sharedPrefs.getString("bill_sort_order", "Due Date (Nearest)") ?: "Due Date (Nearest)"
        _billDefaultFilter.value = sharedPrefs.getString("bill_default_filter", "All Bills") ?: "All Bills"
        _billShowNotes.value = sharedPrefs.getBoolean("bill_show_notes", true)

        _budgetIncludeRecurringBills.value = sharedPrefs.getBoolean("budget_include_recurring_bills", true)

        // Load Category & Tag Preferences
        _preventDeleteUsedCategories.value = sharedPrefs.getBoolean("prevent_delete_used_categories", true)
        _showCategoryInTransactionList.value = sharedPrefs.getBoolean("show_category_in_transaction_list", true)
        _deletedCategories.value = sharedPrefs.getStringSet("deleted_categories", emptySet()) ?: emptySet()
        _customBudgetCategories.value = (sharedPrefs.getStringSet("custom_budget_categories", emptySet()) ?: emptySet()).toList().sorted()
        _customTags.value = (sharedPrefs.getStringSet("custom_tags", emptySet()) ?: emptySet()).toList().sorted()
        _deletedTags.value = sharedPrefs.getStringSet("deleted_tags", emptySet()) ?: emptySet()

        // Load custom category icons
        val iconsMap = mutableMapOf<String, String>()
        savedCats.forEach { cat ->
            iconsMap[cat] = sharedPrefs.getString("cat_icon_$cat", "Star") ?: "Star"
        }
        _categoryIcons.value = iconsMap

        // Compute initial storage & network values
        refreshUsageData()
    }

    fun updateLanguage(language: String) {
        _selectedLanguage.value = language
        sharedPrefs.edit().putString("selected_language", language).apply()
        LanguageManager.applyAppLocale(getApplication(), language)
        appSettingsManager.dispatch(AppSettingsIntent.UpdateLanguage(language))
    }

    fun updateThemeMode(mode: String) {
        _themeMode.value = mode
        sharedPrefs.edit().putString("theme_mode", mode).apply()
        com.example.ui.theme.themeModeState = mode
        com.example.ui.theme.isDarkModeActive = (mode == "dark")
        com.example.ui.theme.updateThemeColors(_themeIndex.value, _customThemeHue.value)
        appSettingsManager.dispatch(AppSettingsIntent.UpdateTheme(_themeIndex.value, _customThemeHue.value, mode))
    }

    fun toggleFollowDeviceColors(enabled: Boolean) {
        _isFollowDeviceColors.value = enabled
        sharedPrefs.edit().putBoolean("follow_device_colors", enabled).apply()
        com.example.ui.theme.isFollowDeviceColorsState = enabled
    }

    fun toggleDarkMode() {
        val nextMode = if (com.example.ui.theme.isDarkModeActive) "light" else "dark"
        updateThemeMode(nextMode)
    }

    fun refreshUsageData() {
        val context = getApplication<Application>()
        
        viewModelScope.launch(Dispatchers.IO) {
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
        appSettingsManager.dispatch(AppSettingsIntent.UpdateTheme(index, _customThemeHue.value, _themeMode.value))
    }

    fun updateCustomThemeHue(hue: Float) {
        sharedPrefs.edit().putFloat("custom_theme_hue", hue).apply()
        _customThemeHue.value = hue
        com.example.ui.theme.updateThemeColors(_themeIndex.value, hue)
        appSettingsManager.dispatch(AppSettingsIntent.UpdateTheme(_themeIndex.value, hue, _themeMode.value))
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

    fun updateTextSizeOption(option: String) {
        _textSizeOption.value = option
        sharedPrefs.edit().putString("text_size_option", option).apply()
    }

    fun toggleCompactLayout(enabled: Boolean) {
        _isCompactLayout.value = enabled
        sharedPrefs.edit().putBoolean("is_compact_layout", enabled).apply()
    }

    fun toggleAnimationEnabled(enabled: Boolean) {
        _isAnimationEnabled.value = enabled
        sharedPrefs.edit().putBoolean("is_animation_enabled", enabled).apply()
    }

    fun toggleAutoExchangeRateUpdate(enabled: Boolean) {
        _isAutoExchangeRateUpdateEnabled.value = enabled
        sharedPrefs.edit().putBoolean("is_auto_exchange_rate_update", enabled).apply()
        _toastMessage.value = if (enabled) {
            "Automatic exchange rate updates turned ON"
        } else {
            "Automatic exchange rate updates turned OFF"
        }
    }

    fun refreshExchangeRates() {
        viewModelScope.launch {
            _isUpdatingExchangeRates.value = true
            try {
                val apiResult = GeminiClient.fetchExchangeRates()
                val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                val nowFormatted = sdf.format(Date())
                _lastExchangeRateUpdate.value = nowFormatted
                sharedPrefs.edit().putString("last_exchange_rate_update", nowFormatted).apply()
                _toastMessage.value = "Exchange rates updated via Google API successfully"
            } catch (e: Exception) {
                val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                val nowFormatted = sdf.format(Date())
                _lastExchangeRateUpdate.value = nowFormatted
                sharedPrefs.edit().putString("last_exchange_rate_update", nowFormatted).apply()
                _toastMessage.value = "Exchange rates updated from cached online rates"
            } finally {
                _isUpdatingExchangeRates.value = false
            }
        }
    }

    fun updateDefaultCurrency(code: String, symbol: String, name: String, convertExisting: Boolean = false) {
        val oldCode = _selectedCurrencyCode.value
        _selectedCurrencyCode.value = code
        _selectedCurrencySymbol.value = symbol
        _selectedCurrencyName.value = name
        appSettingsManager.dispatch(AppSettingsIntent.UpdateCurrency(code, symbol, name, convertExisting))
        if (convertExisting && oldCode != code) {
            viewModelScope.launch {
                val currentList = expenses.value
                currentList.forEach { exp ->
                    val convertedAmount = CurrencyManager.convert(exp.amount, oldCode, code)
                    repository.updateExpense(exp.copy(amount = convertedAmount))
                }
            }
            _toastMessage.value = "Currency changed to $code ($symbol) and existing transactions converted using exchange rates"
        } else {
            _toastMessage.value = "Default currency updated to $code ($symbol)"
        }
    }

    fun updateStatsCurrency(code: String, symbol: String, name: String) {
        _statsCurrencyCode.value = code
        _statsCurrencySymbol.value = symbol
        _statsCurrencyName.value = name
        appSettingsManager.dispatch(AppSettingsIntent.UpdateStatsCurrency(code, symbol, name))
        _toastMessage.value = "Statistics currency updated to $code ($symbol)"
    }

    fun resetCurrencySettings() {
        updateDefaultCurrency("INR", "₹", "Indian Rupee", false)
        updateStatsCurrency("INR", "₹", "Indian Rupee")
        _lastExchangeRateUpdate.value = "10 Aug 2026, 08:30 AM"
        sharedPrefs.edit().putString("last_exchange_rate_update", "10 Aug 2026, 08:30 AM").apply()
        _toastMessage.value = "Currency settings restored to default"
    }

    fun updateDateFormat(fmt: String) {
        _dateFormat.value = fmt
        sharedPrefs.edit().putString("date_format", fmt).apply()
    }

    fun updateFirstDayOfWeek(day: String) {
        _firstDayOfWeek.value = day
        sharedPrefs.edit().putString("first_day_of_week", day).apply()
    }

    fun updateBillReminderTiming(timing: String) {
        _billReminderTiming.value = timing
        sharedPrefs.edit().putString("bill_reminder_timing", timing).apply()
    }

    fun toggleBillOverdueAlert(enabled: Boolean) {
        _billOverdueAlert.value = enabled
        sharedPrefs.edit().putBoolean("bill_overdue_alert", enabled).apply()
    }

    fun toggleBillAutoMarkPaid(enabled: Boolean) {
        _billAutoMarkPaid.value = enabled
        sharedPrefs.edit().putBoolean("bill_auto_mark_paid", enabled).apply()
    }

    fun updateBillDefaultRecurrence(recurrence: String) {
        _billDefaultRecurrence.value = recurrence
        sharedPrefs.edit().putString("bill_default_recurrence", recurrence).apply()
    }

    fun updateBillRecurringEnd(end: String) {
        _billRecurringEnd.value = end
        sharedPrefs.edit().putString("bill_recurring_end", end).apply()
    }

    fun updateBillDefaultCategory(category: String) {
        _billDefaultCategory.value = category
        sharedPrefs.edit().putString("bill_default_category", category).apply()
    }

    fun updateBillArchiveDays(days: String) {
        _billArchiveDays.value = days
        sharedPrefs.edit().putString("bill_archive_days", days).apply()
    }

    fun toggleBillShowUpcomingDashboard(enabled: Boolean) {
        _billShowUpcomingDashboard.value = enabled
        sharedPrefs.edit().putBoolean("bill_show_upcoming_dashboard", enabled).apply()
    }

    fun updateBillUpcomingDays(days: String) {
        _billUpcomingDays.value = days
        sharedPrefs.edit().putString("bill_upcoming_days", days).apply()
    }

    fun updateBillSortOrder(order: String) {
        _billSortOrder.value = order
        sharedPrefs.edit().putString("bill_sort_order", order).apply()
    }

    fun updateBillDefaultFilter(filter: String) {
        _billDefaultFilter.value = filter
        sharedPrefs.edit().putString("bill_default_filter", filter).apply()
    }

    fun toggleBillShowNotes(enabled: Boolean) {
        _billShowNotes.value = enabled
        sharedPrefs.edit().putBoolean("bill_show_notes", enabled).apply()
    }

    fun togglePreventDeleteUsedCategories(enabled: Boolean) {
        _preventDeleteUsedCategories.value = enabled
        sharedPrefs.edit().putBoolean("prevent_delete_used_categories", enabled).apply()
    }

    fun toggleShowCategoryInTransactionList(enabled: Boolean) {
        _showCategoryInTransactionList.value = enabled
        sharedPrefs.edit().putBoolean("show_category_in_transaction_list", enabled).apply()
    }

    fun addCategoryWithType(name: String, type: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        when (type.uppercase()) {
            "EXPENSE" -> addCustomCategory(trimmed, "EXPENSE")
            "INCOME" -> addCustomCategory(trimmed, "INCOME")
            "SAVINGS", "GOAL" -> addGoalCategory(trimmed)
            "BUDGET" -> {
                val current = sharedPrefs.getStringSet("custom_budget_categories", emptySet()) ?: emptySet()
                val updated = (current + trimmed).sorted()
                sharedPrefs.edit().putStringSet("custom_budget_categories", updated.toSet()).apply()
                _customBudgetCategories.value = updated
            }
            else -> addCustomCategory(trimmed, "EXPENSE")
        }
    }

    fun deleteAnyCategory(categoryName: String): Boolean {
        val trimmed = categoryName.trim()
        if (trimmed.isBlank()) return false

        val updatedDeleted = _deletedCategories.value + trimmed
        _deletedCategories.value = updatedDeleted
        sharedPrefs.edit().putStringSet("deleted_categories", updatedDeleted).apply()

        deleteCustomCategory(trimmed)
        deleteGoalCategory(trimmed)

        val currentBudget = sharedPrefs.getStringSet("custom_budget_categories", emptySet()) ?: emptySet()
        if (currentBudget.contains(trimmed)) {
            val updated = currentBudget - trimmed
            sharedPrefs.edit().putStringSet("custom_budget_categories", updated).apply()
            _customBudgetCategories.value = updated.toList().sorted()
        }

        return true
    }

    fun renameAnyCategory(oldName: String, newName: String, type: String = "EXPENSE") {
        val trimmedOld = oldName.trim()
        val trimmedNew = newName.trim()
        if (trimmedNew.isBlank() || trimmedOld == trimmedNew) return

        renameCustomCategory(trimmedOld, trimmedNew)

        if (defaultCategories.contains(trimmedOld) || defaultGoalCategories.contains(trimmedOld) || defaultBudgetCategories.contains(trimmedOld)) {
            val updatedDeleted = _deletedCategories.value + trimmedOld
            _deletedCategories.value = updatedDeleted
            sharedPrefs.edit().putStringSet("deleted_categories", updatedDeleted).apply()
            addCategoryWithType(trimmedNew, type)
        }
    }

    fun addTag(tagName: String) {
        val trimmed = tagName.trim()
        if (trimmed.isBlank()) return
        val current = sharedPrefs.getStringSet("custom_tags", emptySet()) ?: emptySet()
        val updated = (current + trimmed).sorted()
        sharedPrefs.edit().putStringSet("custom_tags", updated.toSet()).apply()
        _customTags.value = updated
    }

    fun renameTag(oldName: String, newName: String) {
        val trimmedOld = oldName.trim()
        val trimmedNew = newName.trim()
        if (trimmedNew.isBlank() || trimmedOld == trimmedNew) return

        deleteTag(trimmedOld)
        addTag(trimmedNew)
    }

    fun deleteTag(tagName: String) {
        val trimmed = tagName.trim()
        if (trimmed.isBlank()) return

        val updatedDeleted = _deletedTags.value + trimmed
        _deletedTags.value = updatedDeleted
        sharedPrefs.edit().putStringSet("deleted_tags", updatedDeleted).apply()

        val currentCustom = sharedPrefs.getStringSet("custom_tags", emptySet()) ?: emptySet()
        if (currentCustom.contains(trimmed)) {
            val updated = currentCustom - trimmed
            sharedPrefs.edit().putStringSet("custom_tags", updated).apply()
            _customTags.value = updated.toList().sorted()
        }
    }

    fun updateCategoryColor(category: String, hexColor: String) {
        val map = _categoryColorMap.value.toMutableMap()
        map[category] = hexColor
        _categoryColorMap.value = map
        sharedPrefs.edit().putString("cat_color_$category", hexColor).apply()
    }

    fun updateTagColor(tag: String, hexColor: String) {
        val map = _tagColorMap.value.toMutableMap()
        map[tag] = hexColor
        _tagColorMap.value = map
        sharedPrefs.edit().putString("tag_color_$tag", hexColor).apply()
    }

    fun toggleBudgetWarning(percentage: Int, enabled: Boolean) {
        when (percentage) {
            80 -> {
                _budgetWarning80.value = enabled
                sharedPrefs.edit().putBoolean("budget_warning_80", enabled).apply()
            }
            90 -> {
                _budgetWarning90.value = enabled
                sharedPrefs.edit().putBoolean("budget_warning_90", enabled).apply()
            }
            100 -> {
                _budgetWarning100.value = enabled
                sharedPrefs.edit().putBoolean("budget_warning_100", enabled).apply()
            }
        }
    }

    fun toggleBudgetIncludeRecurringBills(enabled: Boolean) {
        _budgetIncludeRecurringBills.value = enabled
        sharedPrefs.edit().putBoolean("budget_include_recurring_bills", enabled).apply()
    }

    fun updateGoalViewMode(mode: String) {
        _goalViewMode.value = mode
        sharedPrefs.edit().putString("goal_view_mode", mode).apply()
    }

    fun updateGoalProgressStyle(style: String) {
        _goalProgressStyle.value = style
        sharedPrefs.edit().putString("goal_progress_style", style).apply()
    }

    fun updateDefaultTxType(type: String) {
        _defaultTxType.value = type
        sharedPrefs.edit().putString("default_tx_type", type).apply()
    }

    fun toggleRememberLastCategory(enabled: Boolean) {
        _rememberLastCategory.value = enabled
        sharedPrefs.edit().putBoolean("remember_last_category", enabled).apply()
    }

    fun toggleConfirmTxDelete(enabled: Boolean) {
        _confirmTxDelete.value = enabled
        sharedPrefs.edit().putBoolean("confirm_tx_delete", enabled).apply()
    }

    fun toggleGroupByDate(enabled: Boolean) {
        _groupByDate.value = enabled
        sharedPrefs.edit().putBoolean("group_by_date", enabled).apply()
    }

    fun markBackupPerformed() {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val now = sdf.format(Date())
        _lastBackupTimestamp.value = now
        sharedPrefs.edit().putString("last_backup_timestamp", now).apply()
        _toastMessage.value = "Backup created successfully"
    }

    fun toggleAutoBackupEnabled(enabled: Boolean) {
        _autoBackupEnabled.value = enabled
        sharedPrefs.edit().putBoolean("auto_backup_enabled", enabled).apply()
    }

    fun clearAllData() {
        viewModelScope.launch {
            expenses.value.forEach { repository.deleteExpense(it) }
            accounts.value.forEach { repository.deleteAccount(it) }
            transactions.value.forEach { repository.deleteTransaction(it) }
            budgets.value.forEach { repository.deleteBudget(it) }
            savingsGoals.value.forEach { repository.deleteSavingsGoal(it) }
            _toastMessage.value = "All data cleared successfully"
        }
    }

    fun toggleLockOnRestart(enabled: Boolean) {
        _lockOnRestart.value = enabled
        sharedPrefs.edit().putBoolean("lock_on_restart", enabled).apply()
    }

    fun toggleBiometricEnabled(enabled: Boolean) {
        _biometricEnabled.value = enabled
        sharedPrefs.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    fun toggleHideSensitiveAmounts(enabled: Boolean) {
        _hideSensitiveAmounts.value = enabled
        sharedPrefs.edit().putBoolean("hide_sensitive_amounts", enabled).apply()
    }

    fun togglePrivacyModeEnabled(enabled: Boolean) {
        _privacyModeEnabled.value = enabled
        sharedPrefs.edit().putBoolean("privacy_mode_enabled", enabled).apply()
        // Reset session reveal whenever the feature itself is toggled, so
        // turning it off-then-on again doesn't leave amounts pre-revealed.
        _privacyRevealOverride.value = false
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
        val updated = (current - trimmed).toSet()
        sharedPrefs.edit().putStringSet("custom_goal_categories", updated).apply()
        _customGoalCategories.value = updated.toList().sorted()

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
            
            val updatedIcons = _categoryIcons.value.toMutableMap()
            updatedIcons.remove(trimmedOld)
            updatedIcons[trimmedNew] = savedIcon
            _categoryIcons.value = updatedIcons
            
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

    fun getAvailableNetBalance(): Double {
        val allExp = expenses.value
        val totalInc = allExp.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExp = allExp.filter { it.type != "INCOME" }.sumOf { it.amount }
        val goalsMoney = savingsGoals.value.sumOf { it.currentAmount }
        val rawBalance = (totalInc - totalExp) + goalsMoney

        // GST Auto-Tax Reserve: set aside a % of all-time income for tax,
        // so it never shows as "available" to spend.
        val gstReserve = if (_isGstEnabled.value) totalInc * (_gstRatePercent.value / 100.0) else 0.0

        // Monthly Safe Amount: a fixed emergency buffer that's never counted
        // as available, regardless of income/expense flow.
        val safeBuffer = if (_isMonthlySafeEnabled.value) _monthlySafeAmount.value else 0.0

        return rawBalance - gstReserve - safeBuffer
    }

    // Raw balance before GST reserve / Safe Amount are subtracted — useful
    // for showing "Total Net Balance" on the dashboard as-is, separate from
    // what's actually safe/available to spend.
    fun getRawNetBalance(): Double {
        val allExp = expenses.value
        val totalInc = allExp.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExp = allExp.filter { it.type != "INCOME" }.sumOf { it.amount }
        val goalsMoney = savingsGoals.value.sumOf { it.currentAmount }
        return (totalInc - totalExp) + goalsMoney
    }

    fun getGstReserveAmount(): Double {
        if (!_isGstEnabled.value) return 0.0
        val totalInc = expenses.value.filter { it.type == "INCOME" }.sumOf { it.amount }
        return totalInc * (_gstRatePercent.value / 100.0)
    }

    fun addExpense(
        amount: Double,
        category: String,
        date: Long,
        note: String?,
        imagePath: String? = null,
        type: String = "EXPENSE"
    ) {
        if (type == "EXPENSE") {
            val available = getAvailableNetBalance()
            if (amount > available) {
                _toastMessage.value = "🔒 Total Balance Locked: Cannot expense ₹%,.2f! Exceeds available net balance (₹%,.2f)".format(amount, available.coerceAtLeast(0.0))
                return
            }
        }
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

        // Remember this category as the last-used one for its type, so the
        // next Add Transaction dialog can pre-select it (if the "Remember
        // Last Selected Category" setting is on).
        if (type == "INCOME") {
            _lastUsedIncomeCategory.value = category
            sharedPrefs.edit().putString("last_used_income_category", category).apply()
        } else {
            _lastUsedExpenseCategory.value = category
            sharedPrefs.edit().putString("last_used_expense_category", category).apply()
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun deleteExpenseById(id: Int) {
        viewModelScope.launch {
            repository.deleteExpenseById(id)
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

    fun addTransaction(title: String, amount: Double, type: String, category: String, accountId: Long) {
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
        setCategoryBudget(category, amountLimit)
    }

    fun setCategoryBudget(category: String, amountLimit: Double) {
        viewModelScope.launch {
            val mYear = SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(Date())
            val existing = budgets.value.find { it.category.equals(category, ignoreCase = true) }
            if (existing != null) {
                repository.insertBudget(existing.copy(amountLimit = amountLimit, monthYear = mYear))
            } else {
                repository.insertBudget(Budget(category = category, amountLimit = amountLimit, monthYear = mYear))
            }
        }
    }

    fun deleteCategoryBudget(category: String) {
        viewModelScope.launch {
            val existing = budgets.value.filter { it.category.equals(category, ignoreCase = true) }
            existing.forEach { repository.deleteBudget(it) }
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

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
        }
    }

    fun quickDepositToGoal(goal: SavingsGoal, amount: Double) {
        if (amount <= 0) return
        val available = getAvailableNetBalance()
        if (amount > available) {
            _toastMessage.value = "🔒 Goal Deposit Locked: Cannot deposit ₹%,.2f! Exceeds available net balance (₹%,.2f)".format(amount, available.coerceAtLeast(0.0))
            return
        }
        viewModelScope.launch {
            val updated = goal.copy(currentAmount = goal.currentAmount + amount)
            repository.updateSavingsGoal(updated)

            repository.insertExpense(
                Expense(
                    amount = amount,
                    category = "Locked Savings",
                    date = System.currentTimeMillis(),
                    note = "🔒 Saved & locked in ${goal.name}",
                    type = "EXPENSE"
                )
            )

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

            repository.insertExpense(
                Expense(
                    amount = deductAmount,
                    category = "Goal Withdrawal",
                    date = System.currentTimeMillis(),
                    note = "🔓 Deducted/unlocked from ${goal.name}",
                    type = "INCOME"
                )
            )

            val primaryAccount = accounts.value.firstOrNull()
            if (primaryAccount != null) {
                val updatedAcc = primaryAccount.copy(balance = primaryAccount.balance + deductAmount)
                repository.updateAccount(updatedAcc)
            }
        }
    }

    fun sendChatMessage(userMessageText: String) {
        if (userMessageText.isBlank()) return
        val userMsg = ChatMessage(text = userMessageText, isUser = true)
        _chatMessages.update { it + userMsg }
        _isChatLoading.value = true

        viewModelScope.launch {
            val advice = try {
                val allExp = expenses.value
                val totalInc = allExp.filter { it.type == "INCOME" }.sumOf { it.amount }
                val totalExp = allExp.filter { it.type != "INCOME" }.sumOf { it.amount }
                val context = "User Income: ₹$totalInc, Total Expenses: ₹$totalExp, Categories: ${allCategories.value.joinToString()}"
                
                GeminiClient.getFinancialAdvice(
                    prompt = userMessageText,
                    systemPrompt = "You are an expert AI Financial Advisor inside a personal finance app. Context: $context"
                )
            } catch (e: Exception) {
                "I am having trouble connecting to AI services right now. Please check your network."
            }
            _chatMessages.update { it + ChatMessage(text = advice, isUser = false) }
            _isChatLoading.value = false
        }
    }

    fun requestFinancialAudit() {
        _isAuditLoading.value = true
        _aiAuditReport.value = null

        viewModelScope.launch {
            val report = try {
                val allExp = expenses.value
                val totalInc = allExp.filter { it.type == "INCOME" }.sumOf { it.amount }
                val totalExp = allExp.filter { it.type != "INCOME" }.sumOf { it.amount }
                val net = totalInc - totalExp
                val topCat = allExp.filter { it.type != "INCOME" }.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }.maxByOrNull { it.value }

                val prompt = "Perform an AI Financial Audit for the user. Income: ₹$totalInc, Expenses: ₹$totalExp, Net Balance: ₹$net, Top Spending Category: ${topCat?.key ?: "None"} (₹${topCat?.value ?: 0.0}). Provide 3 actionable tips."
                GeminiClient.getFinancialAdvice(prompt = prompt, systemPrompt = "You are a senior financial auditor.")
            } catch (e: Exception) {
                "Unable to generate audit at this time."
            }
            _aiAuditReport.value = report
            _isAuditLoading.value = false
        }
    }

    fun generateDailySpendingInsight(forceRefresh: Boolean = false) {
        if (_isInsightLoading.value && !forceRefresh) return
        val now = System.currentTimeMillis()
        val lastUpdated = _insightLastUpdated.value
        if (!forceRefresh && _dailySpendingInsight.value != null && lastUpdated != null && (now - lastUpdated < 4 * 3600 * 1000L)) {
            return
        }

        _isInsightLoading.value = true
        viewModelScope.launch {
            val insightText = try {
                val allExp = expenses.value
                val startOfToday = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val todayExpenses = allExp.filter { it.date >= startOfToday && it.type != "INCOME" }
                val todayTotal = todayExpenses.sumOf { it.amount }

                val startOf7Days = System.currentTimeMillis() - 7 * 24 * 3600 * 1000L
                val weekExpenses = allExp.filter { it.date >= startOf7Days && it.type != "INCOME" }
                val weekTotal = weekExpenses.sumOf { it.amount }
                val weekIncome = allExp.filter { it.date >= startOf7Days && it.type == "INCOME" }.sumOf { it.amount }

                val topCategoryWeek = weekExpenses.groupBy { it.category }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }
                    .maxByOrNull { it.value }

                val prompt = """
                    Analyze the user's spending data and generate a 2-3 sentence 'Daily Spending Insight' summary:
                    - Today's Total Expense: ₹${"%.2f".format(todayTotal)} (${todayExpenses.size} transactions today)
                    - Last 7 Days Total Expense: ₹${"%.2f".format(weekTotal)}
                    - Last 7 Days Total Income: ₹${"%.2f".format(weekIncome)}
                    - Top Expense Category (7D): ${topCategoryWeek?.key ?: "General"} (₹${"%.2f".format(topCategoryWeek?.value ?: 0.0)})
                    Provide a friendly, concise, encouraging financial observation or warning for today. Highlight spending pace, savings tip, or notable trend. Avoid long intros or markdown headers.
                """.trimIndent()

                GeminiClient.getFinancialAdvice(
                    prompt = prompt,
                    systemPrompt = "You are an intelligent Gemini AI financial advisor. Return a concise, high-value 2-sentence daily spending insight summary for the user's analytics dashboard. Be direct and insightful."
                )
            } catch (e: Exception) {
                "Unable to generate daily spending insight right now. Please check your network or Gemini API Key."
            }
            _dailySpendingInsight.value = insightText
            _insightLastUpdated.value = System.currentTimeMillis()
            _isInsightLoading.value = false
        }
    }

    // ==========================================
    // 📦 FULL APP DATA BACKUP & RESTORE ENGINE (PORTABLE CODE)
    // ==========================================
    suspend fun generateFullBackupCode(): String = withContext(Dispatchers.IO) {
        val json = JSONObject()
        json.put("app", "AIStudioFinance")
        json.put("version", 1)
        json.put("exportedAt", System.currentTimeMillis())

        // Profile
        val profileObj = JSONObject()
        profileObj.put("userName", _userName.value ?: "")
        profileObj.put("userProfileImageUri", _userProfileImageUri.value ?: "")
        profileObj.put("userDob", _userDob.value)
        profileObj.put("userJob", _userJob.value)
        profileObj.put("userMonthlyIncome", _userMonthlyIncome.value)
        profileObj.put("userGender", _userGender.value)
        json.put("profile", profileObj)

        // Accounts
        val accountsArr = JSONArray()
        val accountList = repository.allAccounts.firstOrNull() ?: emptyList()
        accountList.forEach { acc ->
            val accObj = JSONObject()
            accObj.put("id", acc.id)
            accObj.put("name", acc.name)
            accObj.put("balance", acc.balance)
            accObj.put("type", acc.type)
            accountsArr.put(accObj)
        }
        json.put("accounts", accountsArr)

        // Transactions
        val transactionsArr = JSONArray()
        val transactionList = repository.allTransactions.firstOrNull() ?: emptyList()
        transactionList.forEach { tx ->
            val txObj = JSONObject()
            txObj.put("id", tx.id)
            txObj.put("title", tx.title)
            txObj.put("amount", tx.amount)
            txObj.put("type", tx.type)
            txObj.put("category", tx.category)
            txObj.put("timestamp", tx.timestamp)
            txObj.put("accountId", tx.accountId)
            txObj.put("note", tx.note ?: "")
            txObj.put("imagePath", tx.imagePath ?: "")
            transactionsArr.put(txObj)
        }
        json.put("transactions", transactionsArr)

        // Expenses
        val expensesArr = JSONArray()
        val expenseList = repository.allExpenses.firstOrNull() ?: emptyList()
        expenseList.forEach { exp ->
            val expObj = JSONObject()
            expObj.put("id", exp.id)
            expObj.put("amount", exp.amount)
            expObj.put("category", exp.category)
            expObj.put("date", exp.date)
            expObj.put("note", exp.note ?: "")
            expObj.put("imagePath", exp.imagePath ?: "")
            expObj.put("type", exp.type)
            expensesArr.put(expObj)
        }
        json.put("expenses", expensesArr)

        // Budgets
        val budgetsArr = JSONArray()
        val budgetList = repository.allBudgets.firstOrNull() ?: emptyList()
        budgetList.forEach { b ->
            val bObj = JSONObject()
            bObj.put("id", b.id)
            bObj.put("category", b.category)
            bObj.put("amountLimit", b.amountLimit)
            bObj.put("monthYear", b.monthYear)
            budgetsArr.put(bObj)
        }
        json.put("budgets", budgetsArr)

        // Savings Goals
        val goalsArr = JSONArray()
        val goalList = repository.allSavingsGoals.firstOrNull() ?: emptyList()
        goalList.forEach { g ->
            val gObj = JSONObject()
            gObj.put("id", g.id)
            gObj.put("name", g.name)
            gObj.put("targetAmount", g.targetAmount)
            gObj.put("currentAmount", g.currentAmount)
            gObj.put("targetDate", g.targetDate)
            gObj.put("frequency", g.frequency)
            gObj.put("contributionAmount", g.contributionAmount)
            gObj.put("isAutoGap", g.isAutoGap)
            gObj.put("iconTag", g.iconTag)
            gObj.put("category", g.category)
            gObj.put("imageUri", g.imageUri ?: "")
            goalsArr.put(gObj)
        }
        json.put("savingsGoals", goalsArr)

        // Bills
        val billsArr = JSONArray()
        billsList.forEach { bill ->
            val bObj = JSONObject()
            bObj.put("id", bill.id)
            bObj.put("title", bill.title)
            bObj.put("amount", bill.amount)
            bObj.put("dueDate", bill.dueDate)
            billsArr.put(bObj)
        }
        json.put("bills", billsArr)

        // Reminders
        val remindersArr = JSONArray()
        remindersList.forEach { rem ->
            val rObj = JSONObject()
            rObj.put("id", rem.id)
            rObj.put("text", rem.text)
            rObj.put("dueDate", rem.dueDate)
            rObj.put("isCompleted", rem.isCompleted)
            rObj.put("isEnabled", rem.isEnabled)
            remindersArr.put(rObj)
        }
        json.put("reminders", remindersArr)

        // Settings
        val settingsObj = JSONObject()
        settingsObj.put("monthlyBudget", _monthlyBudget.value)
        settingsObj.put("selectedCurrencyCode", _selectedCurrencyCode.value)
        settingsObj.put("selectedCurrencySymbol", _selectedCurrencySymbol.value)
        settingsObj.put("selectedCurrencyName", _selectedCurrencyName.value)
        settingsObj.put("statsCurrencyCode", _statsCurrencyCode.value)
        settingsObj.put("statsCurrencySymbol", _statsCurrencySymbol.value)
        settingsObj.put("statsCurrencyName", _statsCurrencyName.value)
        settingsObj.put("selectedLanguage", _selectedLanguage.value)
        settingsObj.put("appPin", _appPin.value ?: "")
        settingsObj.put("themeIndex", _themeIndex.value)
        settingsObj.put("customThemeHue", _customThemeHue.value)
        settingsObj.put("themeMode", _themeMode.value)

        settingsObj.put("customCategories", JSONArray(_customCategories.value))
        settingsObj.put("customExpenseCategories", JSONArray(_customExpenseCategories.value))
        settingsObj.put("customIncomeCategories", JSONArray(_customIncomeCategories.value))
        settingsObj.put("customGoalCategories", JSONArray(_customGoalCategories.value))
        settingsObj.put("deletedCategories", JSONArray(_deletedCategories.value.toList()))
        settingsObj.put("customTags", JSONArray(_customTags.value))
        settingsObj.put("deletedTags", JSONArray(_deletedTags.value.toList()))
        json.put("settings", settingsObj)

        val jsonString = json.toString()
        val base64 = android.util.Base64.encodeToString(jsonString.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
        "AISTUDIO_BACKUP_V1:$base64"
    }

    suspend fun restoreFromBackupCode(codeString: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val trimmed = codeString.trim()
            if (trimmed.isBlank()) return@withContext Result.failure(IllegalArgumentException("Backup code cannot be empty."))

            val jsonString = if (trimmed.startsWith("AISTUDIO_BACKUP_V1:")) {
                val rawB64 = trimmed.substring("AISTUDIO_BACKUP_V1:".length)
                String(android.util.Base64.decode(rawB64, android.util.Base64.DEFAULT), Charsets.UTF_8)
            } else if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                trimmed
            } else {
                String(android.util.Base64.decode(trimmed, android.util.Base64.DEFAULT), Charsets.UTF_8)
            }

            val json = JSONObject(jsonString)

            // Parse Accounts
            val newAccounts = mutableListOf<Account>()
            val accountsArr = json.optJSONArray("accounts") ?: JSONArray()
            for (i in 0 until accountsArr.length()) {
                val o = accountsArr.getJSONObject(i)
                newAccounts.add(
                    Account(
                        id = o.optLong("id", 0L),
                        name = o.optString("name", "Account"),
                        balance = o.optDouble("balance", 0.0),
                        type = o.optString("type", "CASH")
                    )
                )
            }

            // Parse Transactions
            val newTransactions = mutableListOf<Transaction>()
            val txArr = json.optJSONArray("transactions") ?: JSONArray()
            for (i in 0 until txArr.length()) {
                val o = txArr.getJSONObject(i)
                newTransactions.add(
                    Transaction(
                        id = o.optLong("id", 0L),
                        title = o.optString("title", "Transaction"),
                        amount = o.optDouble("amount", 0.0),
                        type = o.optString("type", "EXPENSE"),
                        category = o.optString("category", "General"),
                        timestamp = o.optLong("timestamp", System.currentTimeMillis()),
                        accountId = o.optLong("accountId", 1L),
                        note = o.optString("note", null).ifBlank { null },
                        imagePath = o.optString("imagePath", null).ifBlank { null }
                    )
                )
            }

            // Parse Expenses
            val newExpenses = mutableListOf<Expense>()
            val expArr = json.optJSONArray("expenses") ?: JSONArray()
            for (i in 0 until expArr.length()) {
                val o = expArr.getJSONObject(i)
                newExpenses.add(
                    Expense(
                        id = o.optInt("id", 0),
                        amount = o.optDouble("amount", 0.0),
                        category = o.optString("category", "General"),
                        date = o.optLong("date", System.currentTimeMillis()),
                        note = o.optString("note", null).ifBlank { null },
                        imagePath = o.optString("imagePath", null).ifBlank { null },
                        type = o.optString("type", "EXPENSE")
                    )
                )
            }

            // Parse Budgets
            val newBudgets = mutableListOf<Budget>()
            val bArr = json.optJSONArray("budgets") ?: JSONArray()
            for (i in 0 until bArr.length()) {
                val o = bArr.getJSONObject(i)
                newBudgets.add(
                    Budget(
                        id = o.optLong("id", 0L),
                        category = o.optString("category", "General"),
                        amountLimit = o.optDouble("amountLimit", 0.0),
                        monthYear = o.optString("monthYear", "")
                    )
                )
            }

            // Parse Savings Goals
            val newGoals = mutableListOf<SavingsGoal>()
            val gArr = json.optJSONArray("savingsGoals") ?: JSONArray()
            for (i in 0 until gArr.length()) {
                val o = gArr.getJSONObject(i)
                newGoals.add(
                    SavingsGoal(
                        id = o.optLong("id", 0L),
                        name = o.optString("name", "Goal"),
                        targetAmount = o.optDouble("targetAmount", 0.0),
                        currentAmount = o.optDouble("currentAmount", 0.0),
                        targetDate = o.optLong("targetDate", 0L),
                        frequency = o.optString("frequency", "WEEKLY"),
                        contributionAmount = o.optDouble("contributionAmount", 0.0),
                        isAutoGap = o.optBoolean("isAutoGap", true),
                        iconTag = o.optString("iconTag", "🎮"),
                        category = o.optString("category", "Saving"),
                        imageUri = o.optString("imageUri", null).ifBlank { null }
                    )
                )
            }

            // Execute DB restore
            repository.restoreAllData(newExpenses, newAccounts, newTransactions, newBudgets, newGoals)

            // Restore Profile
            val prof = json.optJSONObject("profile")
            if (prof != null) {
                val name = prof.optString("userName", "")
                val img = prof.optString("userProfileImageUri", "")
                val dob = prof.optString("userDob", "24 December 1999")
                val job = prof.optString("userJob", "Successor Designer")
                val inc = prof.optString("userMonthlyIncome", "500 - 3000 / year")
                val gen = prof.optString("userGender", "Male")

                _userName.value = if (name.isNotBlank()) name else null
                _userProfileImageUri.value = if (img.isNotBlank()) img else null
                _userDob.value = dob
                _userJob.value = job
                _userMonthlyIncome.value = inc
                _userGender.value = gen

                sharedPrefs.edit().apply {
                    putString("user_name", _userName.value)
                    putString("user_profile_image_uri", _userProfileImageUri.value)
                    putString("user_dob", dob)
                    putString("user_job", job)
                    putString("user_monthly_income", inc)
                    putString("user_gender", gen)
                    apply()
                }
            }

            // Restore Bills & Reminders
            val billsArr = json.optJSONArray("bills")
            if (billsArr != null) {
                withContext(Dispatchers.Main) {
                    billsList.clear()
                    for (i in 0 until billsArr.length()) {
                        val o = billsArr.getJSONObject(i)
                        billsList.add(
                            BillEntry(
                                id = o.optString("id", System.currentTimeMillis().toString()),
                                title = o.optString("title", "Bill"),
                                amount = o.optDouble("amount", 0.0),
                                dueDate = o.optString("dueDate", "")
                            )
                        )
                    }
                }
            }

            val remArr = json.optJSONArray("reminders")
            if (remArr != null) {
                withContext(Dispatchers.Main) {
                    remindersList.clear()
                    for (i in 0 until remArr.length()) {
                        val o = remArr.getJSONObject(i)
                        remindersList.add(
                            ReminderEntry(
                                id = o.optString("id", System.currentTimeMillis().toString()),
                                text = o.optString("text", "Reminder"),
                                dueDate = o.optString("dueDate", ""),
                                isCompleted = o.optBoolean("isCompleted", false),
                                isEnabled = o.optBoolean("isEnabled", true)
                            )
                        )
                    }
                }
            }

            // Restore Settings
            val set = json.optJSONObject("settings")
            if (set != null) {
                val mb = set.optDouble("monthlyBudget", 25000.0)
                val cc = set.optString("selectedCurrencyCode", "INR")
                val cs = set.optString("selectedCurrencySymbol", "₹")
                val cn = set.optString("selectedCurrencyName", "Indian Rupee")
                val scc = set.optString("statsCurrencyCode", "INR")
                val scs = set.optString("statsCurrencySymbol", "₹")
                val scn = set.optString("statsCurrencyName", "Indian Rupee")
                val lang = set.optString("selectedLanguage", "English")

                _monthlyBudget.value = mb
                _selectedCurrencyCode.value = cc
                _selectedCurrencySymbol.value = cs
                _selectedCurrencyName.value = cn
                _statsCurrencyCode.value = scc
                _statsCurrencySymbol.value = scs
                _statsCurrencyName.value = scn
                _selectedLanguage.value = lang

                sharedPrefs.edit().apply {
                    putFloat("monthly_budget", mb.toFloat())
                    putString("selected_currency_code", cc)
                    putString("selected_currency_symbol", cs)
                    putString("selected_currency_name", cn)
                    putString("stats_currency_code", scc)
                    putString("stats_currency_symbol", scs)
                    putString("stats_currency_name", scn)
                    putString("selected_language", lang)
                    apply()
                }

                LanguageManager.applyAppLocale(getApplication(), lang)
            }

            markBackupPerformed()
            Result.success("Restored ${newTransactions.size} transactions, ${newGoals.size} goals, profile, and settings successfully!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

class FinanceViewModelFactory(
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
