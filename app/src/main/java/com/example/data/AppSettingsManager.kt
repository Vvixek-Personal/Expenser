package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Centralized Application Settings & State Manager.
 * 
 * Architecture Pattern: Clean Architecture + MVI/MVVM State Synchronization.
 * Implements thread-safe, reactive StateFlow streams that couple Sidebar UI choices
 * directly with core application logic (Language, Currency conversion, Theme engine,
 * Security PIN lock, Bills & Reminders, Budget thresholds, Data persistence).
 */
data class AppSettingsState(
    // Preferences & Appearance
    val language: String = "English",
    val themeIndex: Int = 0,
    val themeMode: String = "light",
    val customThemeHue: Float = 200f,
    val isFollowDeviceColors: Boolean = false,
    val textSizeOption: String = "Medium",
    val isCompactLayout: Boolean = false,
    val isAnimationEnabled: Boolean = true,

    // Currency & Rates
    val currencyCode: String = "INR",
    val currencySymbol: String = "₹",
    val currencyName: String = "Indian Rupee",
    val statsCurrencyCode: String = "INR",
    val statsCurrencySymbol: String = "₹",
    val statsCurrencyName: String = "Indian Rupee",
    val autoExchangeRateUpdate: Boolean = true,
    val lastRateUpdateTimestamp: String = "Current",

    // Financial Management - Bills & Reminders
    val billReminderTiming: String = "1 Day Before",
    val billAutoMarkPaid: Boolean = false,
    val billOverdueAlert: Boolean = true,
    val billDefaultRecurrence: String = "Monthly",

    // Financial Management - Budgets & Goals
    val monthlyBudget: Double = 25000.0,
    val budgetWarning80: Boolean = true,
    val budgetWarning90: Boolean = true,
    val budgetWarning100: Boolean = true,

    // Security & Privacy
    val appPin: String? = null,
    val isAppLocked: Boolean = false,
    val biometricEnabled: Boolean = false,
    val autoLockDuration: String = "Immediately",
    val hideSensitiveAmounts: Boolean = false,
    val privacyModeEnabled: Boolean = false,

    // Data Management & Sync
    val autoBackupEnabled: Boolean = true,
    val lastBackupTimestamp: String = "Never"
)

sealed interface AppSettingsIntent {
    data class UpdateLanguage(val language: String) : AppSettingsIntent
    data class UpdateTheme(val themeIndex: Int, val hue: Float, val mode: String) : AppSettingsIntent
    data class UpdateCurrency(val code: String, val symbol: String, val name: String, val convertExisting: Boolean) : AppSettingsIntent
    data class UpdateStatsCurrency(val code: String, val symbol: String, val name: String) : AppSettingsIntent
    data class SetSecurityPin(val pin: String?) : AppSettingsIntent
    data class SetPrivacyMode(val enabled: Boolean) : AppSettingsIntent
    data class SetHideSensitiveAmounts(val hide: Boolean) : AppSettingsIntent
    data class UpdateMonthlyBudget(val amount: Double) : AppSettingsIntent
    data class ToggleAutoBackup(val enabled: Boolean) : AppSettingsIntent
    object TriggerBackupSnapshot : AppSettingsIntent
}

sealed interface AppSettingsEffect {
    data class ShowToast(val message: String) : AppSettingsEffect
    data class LanguageChanged(val newLanguage: String) : AppSettingsEffect
    data class CurrencyConverted(val from: String, val to: String) : AppSettingsEffect
    object RebindUiRequested : AppSettingsEffect
}

class AppSettingsManager private constructor(context: Context) {

    private val sharedPrefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow(loadStateFromPrefs())
    val state: StateFlow<AppSettingsState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AppSettingsEffect>()
    val effects: SharedFlow<AppSettingsEffect> = _effects.asSharedFlow()

    companion object {
        private const val PREFS_NAME = "finance_prefs"

        @Volatile
        private var INSTANCE: AppSettingsManager? = null

        fun getInstance(context: Context): AppSettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppSettingsManager(context).also { INSTANCE = it }
            }
        }
    }

    private fun loadStateFromPrefs(): AppSettingsState {
        return AppSettingsState(
            language = sharedPrefs.getString("selected_language", "English") ?: "English",
            themeIndex = sharedPrefs.getInt("theme_index", 0),
            themeMode = sharedPrefs.getString("theme_mode", "light") ?: "light",
            customThemeHue = sharedPrefs.getFloat("custom_theme_hue", 200f),
            isFollowDeviceColors = sharedPrefs.getBoolean("follow_device_colors", false),
            textSizeOption = sharedPrefs.getString("text_size_option", "Medium") ?: "Medium",
            isCompactLayout = sharedPrefs.getBoolean("compact_layout", false),
            isAnimationEnabled = sharedPrefs.getBoolean("animation_enabled", true),

            currencyCode = sharedPrefs.getString("selected_currency_code", "INR") ?: "INR",
            currencySymbol = sharedPrefs.getString("selected_currency_symbol", "₹") ?: "₹",
            currencyName = sharedPrefs.getString("selected_currency_name", "Indian Rupee") ?: "Indian Rupee",
            statsCurrencyCode = sharedPrefs.getString("stats_currency_code", "INR") ?: "INR",
            statsCurrencySymbol = sharedPrefs.getString("stats_currency_symbol", "₹") ?: "₹",
            statsCurrencyName = sharedPrefs.getString("stats_currency_name", "Indian Rupee") ?: "Indian Rupee",
            autoExchangeRateUpdate = sharedPrefs.getBoolean("auto_exchange_rate_update", true),
            lastRateUpdateTimestamp = sharedPrefs.getString("last_exchange_rate_update", "Current") ?: "Current",

            billReminderTiming = sharedPrefs.getString("bill_reminder_timing", "1 Day Before") ?: "1 Day Before",
            billAutoMarkPaid = sharedPrefs.getBoolean("bill_auto_mark_paid", false),
            billOverdueAlert = sharedPrefs.getBoolean("bill_overdue_alert", true),
            billDefaultRecurrence = sharedPrefs.getString("bill_default_recurrence", "Monthly") ?: "Monthly",

            monthlyBudget = sharedPrefs.getFloat("monthly_budget", 25000.0f).toDouble(),
            budgetWarning80 = sharedPrefs.getBoolean("budget_warning_80", true),
            budgetWarning90 = sharedPrefs.getBoolean("budget_warning_90", true),
            budgetWarning100 = sharedPrefs.getBoolean("budget_warning_100", true),

            appPin = sharedPrefs.getString("app_pin", null),
            isAppLocked = sharedPrefs.getString("app_pin", null) != null,
            biometricEnabled = sharedPrefs.getBoolean("biometric_enabled", false),
            autoLockDuration = sharedPrefs.getString("auto_lock_duration", "Immediately") ?: "Immediately",
            hideSensitiveAmounts = sharedPrefs.getBoolean("hide_sensitive_amounts", false),
            privacyModeEnabled = sharedPrefs.getBoolean("privacy_mode_enabled", false),

            autoBackupEnabled = sharedPrefs.getBoolean("auto_backup_enabled", true),
            lastBackupTimestamp = sharedPrefs.getString("last_backup_timestamp", "Never") ?: "Never"
        )
    }

    fun dispatch(intent: AppSettingsIntent) {
        scope.launch {
            handleIntent(intent)
        }
    }

    private suspend fun handleIntent(intent: AppSettingsIntent) = withContext(Dispatchers.IO) {
        when (intent) {
            is AppSettingsIntent.UpdateLanguage -> {
                sharedPrefs.edit().putString("selected_language", intent.language).apply()
                _state.value = _state.value.copy(language = intent.language)
                _effects.emit(AppSettingsEffect.LanguageChanged(intent.language))
            }

            is AppSettingsIntent.UpdateTheme -> {
                sharedPrefs.edit()
                    .putInt("theme_index", intent.themeIndex)
                    .putFloat("custom_theme_hue", intent.hue)
                    .putString("theme_mode", intent.mode)
                    .apply()
                _state.value = _state.value.copy(
                    themeIndex = intent.themeIndex,
                    customThemeHue = intent.hue,
                    themeMode = intent.mode
                )
                _effects.emit(AppSettingsEffect.RebindUiRequested)
            }

            is AppSettingsIntent.UpdateCurrency -> {
                val oldCode = _state.value.currencyCode
                sharedPrefs.edit()
                    .putString("selected_currency_code", intent.code)
                    .putString("selected_currency_symbol", intent.symbol)
                    .putString("selected_currency_name", intent.name)
                    .apply()
                _state.value = _state.value.copy(
                    currencyCode = intent.code,
                    currencySymbol = intent.symbol,
                    currencyName = intent.name
                )
                if (intent.convertExisting) {
                    _effects.emit(AppSettingsEffect.CurrencyConverted(oldCode, intent.code))
                }
                _effects.emit(AppSettingsEffect.ShowToast("Currency updated to ${intent.name} (${intent.symbol})"))
            }

            is AppSettingsIntent.UpdateStatsCurrency -> {
                sharedPrefs.edit()
                    .putString("stats_currency_code", intent.code)
                    .putString("stats_currency_symbol", intent.symbol)
                    .putString("stats_currency_name", intent.name)
                    .apply()
                _state.value = _state.value.copy(
                    statsCurrencyCode = intent.code,
                    statsCurrencySymbol = intent.symbol,
                    statsCurrencyName = intent.name
                )
                _effects.emit(AppSettingsEffect.ShowToast("Statistics currency updated to ${intent.symbol}"))
            }

            is AppSettingsIntent.SetSecurityPin -> {
                if (intent.pin.isNullOrBlank()) {
                    sharedPrefs.edit().remove("app_pin").apply()
                    _state.value = _state.value.copy(appPin = null, isAppLocked = false)
                    _effects.emit(AppSettingsEffect.ShowToast("Security PIN removed."))
                } else {
                    sharedPrefs.edit().putString("app_pin", intent.pin).apply()
                    _state.value = _state.value.copy(appPin = intent.pin, isAppLocked = true)
                    _effects.emit(AppSettingsEffect.ShowToast("Security PIN set successfully!"))
                }
            }

            is AppSettingsIntent.SetPrivacyMode -> {
                sharedPrefs.edit().putBoolean("privacy_mode_enabled", intent.enabled).apply()
                _state.value = _state.value.copy(privacyModeEnabled = intent.enabled)
                _effects.emit(AppSettingsEffect.ShowToast(if (intent.enabled) "Privacy Mode Enabled" else "Privacy Mode Disabled"))
            }

            is AppSettingsIntent.SetHideSensitiveAmounts -> {
                sharedPrefs.edit().putBoolean("hide_sensitive_amounts", intent.hide).apply()
                _state.value = _state.value.copy(hideSensitiveAmounts = intent.hide)
            }

            is AppSettingsIntent.UpdateMonthlyBudget -> {
                sharedPrefs.edit().putFloat("monthly_budget", intent.amount.toFloat()).apply()
                _state.value = _state.value.copy(monthlyBudget = intent.amount)
                _effects.emit(AppSettingsEffect.ShowToast("Monthly budget limit set to ${intent.amount}"))
            }

            is AppSettingsIntent.ToggleAutoBackup -> {
                sharedPrefs.edit().putBoolean("auto_backup_enabled", intent.enabled).apply()
                _state.value = _state.value.copy(autoBackupEnabled = intent.enabled)
            }

            is AppSettingsIntent.TriggerBackupSnapshot -> {
                val timestamp = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                sharedPrefs.edit().putString("last_backup_timestamp", timestamp).apply()
                _state.value = _state.value.copy(lastBackupTimestamp = timestamp)
                _effects.emit(AppSettingsEffect.ShowToast("Local snapshot updated at $timestamp"))
            }
        }
    }
}
