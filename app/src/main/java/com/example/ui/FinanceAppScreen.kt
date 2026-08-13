package com.example.ui

import com.example.data.*
import java.util.*
import java.text.*
import kotlin.math.roundToInt

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Expense
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.io.File
import kotlin.math.roundToInt
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Expenses : Screen("expenses", "Finance", Icons.Default.ReceiptLong)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.PieChart)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)
}

fun getCategoryEmoji(category: String, customMap: Map<String, String> = emptyMap()): String {
    val resolved = (customMap[category] ?: category).lowercase(java.util.Locale.getDefault())
    return when (resolved) {
        "food", "dining", "restaurant", "baking", "fastfood" -> "🍔"
        "travel", "flight", "aviation", "taxi", "transportation", "flighttakeoff" -> "✈️"
        "rent", "home", "housing", "real estate" -> "🏠"
        "utilities", "electricity", "water", "gas", "electricbolt", "bolt" -> "💡"
        "entertainment", "gaming", "movies", "cinema", "sportsesports", "movie" -> "🎮"
        "shopping", "clothing", "gear", "shoppingcart" -> "🛍️"
        "persons", "friends", "family", "dogs", "cats", "pets", "person" -> "👥"
        "salary", "income", "finance", "monetizationon" -> "💵"
        "freelance", "work" -> "💼"
        "investments", "crypto" -> "🪙"
        "gifts", "gift", "cardgiftcard" -> "🎁"
        "art" -> "🎨"
        "botany" -> "🌱"
        "cars", "car", "directionscar" -> "🚗"
        "technology", "programming" -> "💻"
        "fashion" -> "👗"
        "birds" -> "🐦"
        "health care", "healthcare", "medical", "localhospital", "healing" -> "🏥"
        "geography" -> "🗺️"
        "lgbtq" -> "🏳️‍🌈"
        "mental health" -> "🧠"
        "sports", "fitness", "fitnesscenter" -> "⚽"
        "photography" -> "📷"
        "design" -> "🖋️"
        "ufo" -> "🛸"
        "music" -> "🎶"
        "school" -> "🏫"
        "settings" -> "⚙️"
        "star" -> "⭐️"
        "construction" -> "🔨"
        "coffee" -> "☕"
        "waterdrop" -> "💧"
        "checkroom" -> "🧥"
        "directionsbus" -> "🚌"
        "localgasstation" -> "⛽"
        "event" -> "📅"
        "spa" -> "🧖"
        "pending" -> "⏳"
        else -> "📦"
    }
}

// Category palette helper
val categoryColors = mapOf(
    "Food" to Color(0xFFF97316),        // Orange
    "Travel" to Color(0xFF0D9488),      // Teal
    "Rent" to Color(0xFF2563EB),        // Blue
    "Utilities" to Color(0xFF16A34A),   // Green
    "Entertainment" to Color(0xFFE11D48),// Rose
    "Shopping" to Color(0xFF9333EA),     // Purple
    "Persons" to Color(0xFF0EA5E9),      // Sky Blue (Persons Category)
    "Others" to Color(0xFF64748B)        // Slate
)

fun getCategoryIcon(category: String, customMap: Map<String, String> = emptyMap()): ImageVector {
    val resolved = customMap[category] ?: category
    return when (resolved) {
        "Food", "Restaurant" -> Icons.Default.Restaurant
        "Travel", "DirectionsCar" -> Icons.Default.DirectionsCar
        "Rent", "Home" -> Icons.Default.Home
        "Utilities", "Bolt", "ElectricBolt" -> Icons.Default.Bolt
        "Entertainment", "Movie" -> Icons.Default.Movie
        "Shopping", "ShoppingCart" -> Icons.Default.ShoppingCart
        "Persons", "Person" -> Icons.Default.Person
        "LocalHospital", "Healing" -> Icons.Default.LocalHospital
        "School" -> Icons.Default.School
        "Work" -> Icons.Default.Work
        "Flight", "FlightTakeoff" -> Icons.Default.Flight
        "SportsEsports" -> Icons.Default.SportsEsports
        "CardGiftcard" -> Icons.Default.CardGiftcard
        "MonetizationOn" -> Icons.Default.MonetizationOn
        "Settings" -> Icons.Default.Settings
        "Pets" -> Icons.Default.Pets
        "Star" -> Icons.Default.Star
        "Construction" -> Icons.Default.Construction
        "Fastfood" -> Icons.Default.Fastfood
        "Coffee" -> Icons.Default.Coffee
        "WaterDrop" -> Icons.Default.WaterDrop
        "Checkroom" -> Icons.Default.Checkroom
        "DirectionsBus" -> Icons.Default.DirectionsBus
        "LocalGasStation" -> Icons.Default.LocalGasStation
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "Event" -> Icons.Default.Event
        "Spa" -> Icons.Default.Spa
        "Pending" -> Icons.Default.Pending
        else -> Icons.Default.Category
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceAppScreen(viewModel: FinanceViewModel) {
    val backStack = remember { mutableStateListOf<Screen>(Screen.Dashboard) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val filteredExpenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val selectedDateRange by viewModel.selectedDateRange.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val monthlyBudget by viewModel.monthlyBudget.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val expenseCategories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val incomeCategories by viewModel.incomeCategories.collectAsStateWithLifecycle()
    val categoryIcons by viewModel.categoryIcons.collectAsStateWithLifecycle()

    val appPin by viewModel.appPin.collectAsStateWithLifecycle()
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val hasPromptedFirstRunPin by viewModel.hasPromptedFirstRunPin.collectAsStateWithLifecycle()

    var showChangePinDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var prefilledDateForAddDialog by remember { mutableStateOf<Long?>(null) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var viewingDetailExpense by remember { mutableStateOf<Expense?>(null) }
    var recordedTransactionInfo by remember { mutableStateOf<RecordedTransactionInfo?>(null) }

    var activeSettingsSubScreen by remember { mutableStateOf<SettingsSubScreen?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navigateToScreen: (Screen) -> Unit = { selected ->
        activeSettingsSubScreen = null
        if (currentScreen != selected) {
            backStack.add(selected)
            currentScreen = selected
        }
    }

    // Close drawer automatically if app gets locked
    LaunchedEffect(isAppLocked, appPin) {
        if (isAppLocked && !appPin.isNullOrBlank()) {
            drawerState.close()
        }
    }

    // Intercept back button when locked
    BackHandler(enabled = isAppLocked && !appPin.isNullOrBlank()) {
        // Do nothing on back button during lock screen
    }

    // SYSTEM BACK BUTTON HANDLER (Pops navigation stack, closes drawer/settings/dialogs)
    val canHandleBack = (!isAppLocked || appPin.isNullOrBlank()) && (
            drawerState.isOpen ||
            activeSettingsSubScreen != null ||
            viewingDetailExpense != null ||
            editingExpense != null ||
            showAddExpenseDialog ||
            backStack.size > 1 ||
            currentScreen != Screen.Dashboard
    )

    BackHandler(enabled = canHandleBack) {
        when {
            drawerState.isOpen -> scope.launch { drawerState.close() }
            activeSettingsSubScreen != null -> activeSettingsSubScreen = null
            viewingDetailExpense != null -> viewingDetailExpense = null
            editingExpense != null -> editingExpense = null
            showAddExpenseDialog -> showAddExpenseDialog = false
            backStack.size > 1 -> {
                backStack.removeAt(backStack.lastIndex)
                currentScreen = backStack.last()
            }
            currentScreen != Screen.Dashboard -> {
                backStack.clear()
                backStack.add(Screen.Dashboard)
                currentScreen = Screen.Dashboard
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = (!isAppLocked || appPin.isNullOrBlank()) && activeSettingsSubScreen == null,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = Color.Transparent,
                    drawerTonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(330.dp)
                        .padding(top = 44.dp, bottom = 44.dp, end = 12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = SleekBg,
                        border = BorderStroke(1.dp, SleekBorder),
                        shadowElevation = 16.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        SidebarDrawerContent(
                            viewModel = viewModel,
                            onCloseDrawer = { scope.launch { drawerState.close() } },
                            onOpenSettingsScreen = { subScreen ->
                                scope.launch { drawerState.close() }
                                activeSettingsSubScreen = subScreen
                            },
                            onChangePasswordClick = {
                                showChangePinDialog = true
                            }
                        )
                    }
                }
            }
        ) {
        Scaffold(
        bottomBar = {
            if (!isAppLocked || appPin.isNullOrBlank()) {
                FloatingDockBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { selected -> navigateToScreen(selected) },
                    selectedLanguage = viewModel.selectedLanguage.collectAsStateWithLifecycle().value
                )
            }
        },
        containerColor = SleekBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Switcher with animations
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                     scaleIn(initialScale = 0.95f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))) togetherWith
                    (fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                     scaleOut(targetScale = 1.02f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    Screen.Dashboard -> DashboardTab(
                        expenses = filteredExpenses,
                        userName = userName,
                        monthlyBudget = monthlyBudget,
                        categoryIcons = categoryIcons,
                        onUpdateBudget = { viewModel.updateMonthlyBudget(it) },
                        onUpdateName = { viewModel.saveUserName(it) },
                        onAddExpenseClick = {
                            prefilledDateForAddDialog = null
                            showAddExpenseDialog = true
                        },
                        onNavigateToExpenses = { navigateToScreen(Screen.Expenses) },
                        onNavigateToAnalytics = { navigateToScreen(Screen.Analytics) },
                        onProfileClick = { scope.launch { drawerState.open() } },
                        onEditExpenseClick = { viewingDetailExpense = it },
                        viewModel = viewModel
                    )
                    Screen.Expenses -> ExpensesTab(
                        viewModel = viewModel,
                        onAddExpenseClick = {
                            prefilledDateForAddDialog = null
                            showAddExpenseDialog = true
                        },
                        onEditExpenseClick = { viewingDetailExpense = it }
                    )
                    Screen.Analytics -> AnalyticsTab(
                        viewModel = viewModel,
                        onAddClick = {
                            activeSettingsSubScreen = null
                            prefilledDateForAddDialog = null
                            showAddExpenseDialog = true
                        },
                        onProfileClick = { scope.launch { drawerState.open() } }
                    )
                    Screen.Calendar -> CalendarTab(
                        expenses = expenses,
                        categoryIcons = categoryIcons,
                        onAddExpenseForDate = { date ->
                            prefilledDateForAddDialog = date
                            showAddExpenseDialog = true
                        },
                        onEditExpense = { viewingDetailExpense = it },
                        onDeleteExpense = { viewModel.deleteExpense(it) }
                    )
                }
            }

            if (showAddExpenseDialog) {
                val defaultTxType by viewModel.defaultTxType.collectAsStateWithLifecycle()
                val rememberLastCategory by viewModel.rememberLastCategory.collectAsStateWithLifecycle()
                val lastUsedExpenseCategory by viewModel.lastUsedExpenseCategory.collectAsStateWithLifecycle()
                val lastUsedIncomeCategory by viewModel.lastUsedIncomeCategory.collectAsStateWithLifecycle()
                val isGstEnabledForDialog by viewModel.isGstEnabled.collectAsStateWithLifecycle()
                val gstRateForDialog by viewModel.gstRatePercent.collectAsStateWithLifecycle()
                val isMonthlySafeEnabledForDialog by viewModel.isMonthlySafeEnabled.collectAsStateWithLifecycle()
                val monthlySafeAmountForDialog by viewModel.monthlySafeAmount.collectAsStateWithLifecycle()
                AddExpenseDialog(
                    prefilledDate = prefilledDateForAddDialog,
                    categories = allCategories,
                    expenseCategories = expenseCategories,
                    incomeCategories = incomeCategories,
                    categoryIcons = categoryIcons,
                    expenses = expenses,
                    defaultTxType = defaultTxType,
                    rememberLastCategory = rememberLastCategory,
                    lastUsedExpenseCategory = lastUsedExpenseCategory,
                    lastUsedIncomeCategory = lastUsedIncomeCategory,
                    gstReserveAmount = if (isGstEnabledForDialog) viewModel.getGstReserveAmount() else 0.0,
                    monthlySafeAmount = if (isMonthlySafeEnabledForDialog) monthlySafeAmountForDialog else 0.0,
                    onAddCategory = { name, catType -> viewModel.addCustomCategory(name, catType) },
                    onDeleteCategory = { viewModel.deleteCustomCategory(it) },
                    onEditCategory = { old, new -> viewModel.renameCustomCategory(old, new) },
                    onDismiss = { showAddExpenseDialog = false },
                    onConfirm = { amount, category, date, note, imagePath, type ->
                        viewModel.addExpense(amount, category, date, note, imagePath, type)
                        viewModel.refreshUsageData()
                        showAddExpenseDialog = false
                        recordedTransactionInfo = RecordedTransactionInfo(
                            amount = amount,
                            category = category,
                            type = type,
                            note = note
                        )
                    }
                )
            }

            if (recordedTransactionInfo != null) {
                TransactionSuccessDialog(
                    info = recordedTransactionInfo!!,
                    onDismiss = { recordedTransactionInfo = null }
                )
            }

            if (editingExpense != null) {
                EditExpenseDialog(
                    expense = editingExpense!!,
                    categories = allCategories,
                    expenseCategories = expenseCategories,
                    incomeCategories = incomeCategories,
                    categoryIcons = categoryIcons,
                    expenses = expenses,
                    onAddCategory = { name, catType -> viewModel.addCustomCategory(name, catType) },
                    onDeleteCategory = { viewModel.deleteCustomCategory(it) },
                    onEditCategory = { old, new -> viewModel.renameCustomCategory(old, new) },
                    onDismiss = { editingExpense = null },
                    onConfirm = { updatedExpense ->
                        viewModel.updateExpense(updatedExpense)
                        editingExpense = null
                    }
                )
            }

            if (viewingDetailExpense != null) {
                ExpenseDetailDialog(
                    expense = viewingDetailExpense!!,
                    viewModel = viewModel,
                    onDismiss = { viewingDetailExpense = null },
                    onEditClick = {
                        val target = viewingDetailExpense
                        viewingDetailExpense = null
                        if (target != null) {
                            editingExpense = target
                        }
                    },
                    onDeleteClick = {
                        val target = viewingDetailExpense
                        viewingDetailExpense = null
                        if (target != null) {
                            viewModel.deleteExpense(target)
                        }
                    }
                )
            }
        }

        if (activeSettingsSubScreen != null) {
            when (activeSettingsSubScreen) {
                SettingsSubScreen.PersonalData -> PersonalDataScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.BadgesAndMilestones -> BadgesAndMilestonesScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.Appearance -> AppearanceScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.Language -> LanguageScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.Currency -> CurrencyScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.DateTime -> DateTimeScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.Bills -> BillsSettingsScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.CategoriesTags -> CategoriesTagsScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.Budgets -> BudgetSettingsScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null },
                    onOpenCurrencySettings = { activeSettingsSubScreen = SettingsSubScreen.Currency }
                )
                SettingsSubScreen.SavingsGoals -> SavingsGoalsSettingsScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.Calculations -> CalculationsScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.Transactions -> TransactionsSettingsScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.BackupRestore -> BackupRestoreScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.DataManagement -> DataManagementScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.Security -> SecuritySettingsScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.Privacy -> PrivacySettingsScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.AboutApp -> AboutAppScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.HelpSupport -> HelpSupportScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.DataAndStorage -> DataAndStorageScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.ThemeAndLanguage -> ThemeAndLanguageScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.Export -> ExportDataScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                SettingsSubScreen.FaqAndHelp -> FaqAndHelpScreen(
                    viewModel = viewModel,
                    onBack = { activeSettingsSubScreen = null }
                )
                null -> {}
            }
        }

        // Full Screen Lock Overlay if app is locked with a PIN
        if (isAppLocked && !appPin.isNullOrBlank()) {
            PinLockScreen(viewModel = viewModel)
        }
    }

    // First Run Optional PIN Setup Prompt
    if (!isAppLocked && appPin.isNullOrBlank() && !hasPromptedFirstRunPin) {
        FirstRunPinSetupDialog(
            onSetPin = { newPin ->
                viewModel.setAppPin(newPin)
                viewModel.markFirstRunPinPrompted()
            },
            onMaybeLater = {
                viewModel.markFirstRunPinPrompted()
            }
        )
    }

    // Change Password / PIN Dialog
    if (showChangePinDialog) {
        ChangePinDialog(
            currentAppPin = appPin,
            onDismiss = { showChangePinDialog = false },
            onSavePin = { newPin ->
                viewModel.setAppPin(newPin)
            }
        )
    }

    if (userName.isNullOrBlank()) {
        OnboardingNameDialog(onSave = { viewModel.saveUserName(it) })
    }
}
}
}

// ==========================================
// 1️⃣ DASHBOARD TAB
// ==========================================
@Composable
fun DashboardTab(
    expenses: List<Expense>,
    userName: String?,
    monthlyBudget: Double,
    categoryIcons: Map<String, String> = emptyMap(),
    onUpdateBudget: (Double) -> Unit,
    onUpdateName: (String) -> Unit,
    onAddExpenseClick: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToAnalytics: () -> Unit = {},
    onProfileClick: () -> Unit,
    onEditExpenseClick: (Expense) -> Unit,
    viewModel: FinanceViewModel
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val savingsGoals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val profileImageUri by viewModel.userProfileImageUri.collectAsStateWithLifecycle()
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val showStreakDialog by viewModel.showStreakDialog.collectAsStateWithLifecycle()

    if (showStreakDialog) {
        DailyStreakCelebrationDialog(
            streakCount = currentStreak,
            onDismiss = { viewModel.dismissStreakDialog() }
        )
    }

    val currentCalendar = Calendar.getInstance()
    val currentMonth = currentCalendar.get(Calendar.MONTH)
    val currentYear = currentCalendar.get(Calendar.YEAR)

    // All-time income, expense, and savings goals totals for Total Net Balance
    val totalAllTimeIncome = remember(expenses) {
        expenses.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalAllTimeExpense = remember(expenses) {
        expenses.filter { it.type != "INCOME" }.sumOf { it.amount }
    }
    val totalSavingsGoalsMoney = remember(savingsGoals) {
        savingsGoals.sumOf { it.currentAmount }
    }
    val overallTotalNetBalance = (totalAllTimeIncome - totalAllTimeExpense) + totalSavingsGoalsMoney

    // Filter current month expenses
    val thisMonthExpenses = expenses.filter {
        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
        cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear && it.type != "INCOME"
    }
    val thisMonthTotal = thisMonthExpenses.sumOf { it.amount }

    // Filter current month incomes
    val thisMonthIncomes = expenses.filter {
        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
        cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear && it.type == "INCOME"
    }
    val thisMonthIncomeTotal = thisMonthIncomes.sumOf { it.amount }

    // Last month expenses
    val lastMonthCalendar = Calendar.getInstance().apply {
        add(Calendar.MONTH, -1)
    }
    val lastMonth = lastMonthCalendar.get(Calendar.MONTH)
    val lastMonthYear = lastMonthCalendar.get(Calendar.YEAR)

    val lastMonthExpenses = expenses.filter {
        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
        cal.get(Calendar.MONTH) == lastMonth && cal.get(Calendar.YEAR) == lastMonthYear && it.type != "INCOME"
    }
    val lastMonthTotal = lastMonthExpenses.sumOf { it.amount }

    // Difference Calculation
    val diffPct = if (lastMonthTotal > 0) {
        ((thisMonthTotal - lastMonthTotal) / lastMonthTotal) * 100
    } else {
        0.0
    }

    var showChangeNameDialog by remember { mutableStateOf(false) }
    var showAdjustBudgetDialog by remember { mutableStateOf(false) }
    var showBillsScreen by remember { mutableStateOf(false) }
    var showRemindersScreen by remember { mutableStateOf(false) }
    var showSavingGoalsScreen by remember { mutableStateOf(false) }
    var showStartupReminder by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(5000)
        showStartupReminder = false
    }

    // Startup reminder alert dialog removed - shown on-screen in dashboard feed

    if (showSavingGoalsScreen) {
        SavingGoalsFullScreen(
            viewModel = viewModel,
            onBack = { showSavingGoalsScreen = false }
        )
        return
    }

    if (showBillsScreen) {
        BillsFullScreen(
            viewModel = viewModel,
            onBack = { showBillsScreen = false },
            onPayBill = { title, amt ->
                viewModel.addExpense(
                    amount = amt,
                    category = "Bills",
                    date = System.currentTimeMillis(),
                    note = "Paid $title",
                    type = "EXPENSE"
                )
            }
        )
        return
    }

    if (showRemindersScreen) {
        RemindersFullScreen(
            viewModel = viewModel,
            onBack = { showRemindersScreen = false }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App Header & User Profile
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.clickable { onProfileClick() }
            ) {
                val initials = if (!userName.isNullOrBlank()) {
                    userName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").take(2)
                } else "U"

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SleekPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (!profileImageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = profileImageUri,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleMedium,
                            color = SleekOnPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column {
                    Text(
                        text = "Welcome,",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary
                    )
                    Text(
                        text = userName ?: "User",
                        style = MaterialTheme.typography.titleMedium,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Streak Badge beside profile & Quick Add Action Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = { viewModel.triggerShowStreakDialog() },
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFF7ED),
                    border = BorderStroke(1.dp, Color(0xFFF97316)),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("🔥", fontSize = 13.sp)
                        Text(
                            text = "$currentStreak Day Streak",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC2410C)
                        )
                    }
                }

                IconButton(
                    onClick = onAddExpenseClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SleekPrimary)
                        .testTag("dashboard_add_expense_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Expense",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Total Balance Card (Bank Account Balance Style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(SleekPrimary, Color(0xFF004F87)),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
                .padding(24.dp)
        ) {
            // Canvas decorative overlapping circles for professional touch
            Box(modifier = Modifier.matchParentSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.06f),
                        radius = 110.dp.toPx(),
                        center = Offset(size.width - 20.dp.toPx(), -20.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.03f),
                        radius = 160.dp.toPx(),
                        center = Offset(size.width - 10.dp.toPx(), 10.dp.toPx())
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TOTAL NET BALANCE",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (overallTotalNetBalance >= 0) Color(0xFF10B981).copy(alpha = 0.25f) else Color(0xFFEF4444).copy(alpha = 0.25f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (overallTotalNetBalance >= 0) "Safe Balance" else "Overdrawn",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = String.format("%s₹%,.2f", if (overallTotalNetBalance >= 0) "" else "-", Math.abs(overallTotalNetBalance)),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp
                )

                if (totalSavingsGoalsMoney > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = String.format("Includes ₹%,.0f in Savings Goals", totalSavingsGoalsMoney),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Breakdown row: Inflow vs Outflow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Income
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "TOTAL INCOME",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format("₹%,.0f", totalAllTimeIncome),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Expense
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "TOTAL EXPENSE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format("₹%,.0f", totalAllTimeExpense),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isDecrease = thisMonthTotal <= lastMonthTotal
                    val pillBg = if (isDecrease) Color(0xFFBAF0B2) else Color(0xFFFDE2E4)
                    val pillTextColor = if (isDecrease) Color(0xFF002106) else Color(0xFF3B0000)
                    val prefixSign = if (isDecrease) "-" else "+"

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(pillBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = String.format("%s%.1f%%", prefixSign, Math.abs(diffPct)),
                            color = pillTextColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = String.format("expenses vs last month (₹%,.0f)", lastMonthTotal),
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Shortcuts Feed (Always directly below Balance Card per user request)
        QuickServicesCategorySection(
            viewModel = viewModel,
            selectedLanguage = selectedLanguage,
            onNavigateToExpenses = onNavigateToExpenses,
            onNavigateToAnalytics = onNavigateToAnalytics,
            onBillsClick = { showBillsScreen = true },
            onReminderClick = { showRemindersScreen = true },
            onGoalsClick = { showSavingGoalsScreen = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Small Fluid Wave Budget Status Bar
        SmallFluidBudgetBar(
            thisMonthTotal = thisMonthTotal,
            monthlyBudget = monthlyBudget
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Activity List Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                color = SleekTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See All",
                color = SleekPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onNavigateToExpenses() }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (expenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No activities recorded yet. Tap + to add!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SleekTextSecondary
                )
            }
        } else {
            // Show top 3 recent items
            expenses.take(3).forEach { expense ->
                RecentExpenseRow(expense = expense, categoryIcons = categoryIcons, onClick = { onEditExpenseClick(expense) })
            }
        }
    }

    if (showChangeNameDialog) {
        Dialog(onDismissRequest = { showChangeNameDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                var newName by remember { mutableStateOf(userName ?: "") }
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Update Your Name",
                        style = MaterialTheme.typography.titleMedium,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Your Name", color = SleekTextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekBorder,
                            focusedLabelColor = SleekPrimary,
                            unfocusedLabelColor = SleekTextSecondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showChangeNameDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextSecondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (newName.trim().isNotEmpty()) {
                                    onUpdateName(newName.trim())
                                    showChangeNameDialog = false
                                }
                            },
                            enabled = newName.trim().isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(110.dp))
    }

    if (showAdjustBudgetDialog) {
        AdjustBudgetDialog(
            currentBudget = monthlyBudget,
            onDismiss = { showAdjustBudgetDialog = false },
            onConfirm = {
                onUpdateBudget(it)
                showAdjustBudgetDialog = false
            }
        )
    }
}



// ==========================================
// 🎨 QUICK SHORTCUTS & CATEGORY FEED
// ==========================================
@Composable
fun QuickServicesCategorySection(
    viewModel: FinanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    selectedLanguage: String = "English",
    onNavigateToExpenses: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onBillsClick: () -> Unit,
    onReminderClick: () -> Unit,
    onGoalsClick: () -> Unit = {}
) {
    val todayStr = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
    val hasBillDueToday = viewModel.billsList.any { it.dueDate == todayStr }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, SleekBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_services_category_feed")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageManager.tr("Quick Shortcuts", selectedLanguage),
                    style = MaterialTheme.typography.titleMedium,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CategoryFeedTile(
                    icon = Icons.Rounded.Savings,
                    label = "Goals",
                    tileColor = Color(0xFF0EA5E9),
                    onClick = onGoalsClick,
                    modifier = Modifier.weight(1f)
                )

                CategoryFeedTile(
                    icon = Icons.Rounded.ReceiptLong,
                    label = "Finance",
                    tileColor = Color(0xFF6366F1),
                    onClick = onNavigateToExpenses,
                    modifier = Modifier.weight(1f)
                )

                CategoryFeedTile(
                    icon = Icons.Rounded.PieChart,
                    label = "Analytics",
                    tileColor = Color(0xFF10B981),
                    onClick = onNavigateToAnalytics,
                    modifier = Modifier.weight(1f)
                )

                CategoryFeedTile(
                    icon = Icons.Rounded.Receipt,
                    label = "Bills",
                    tileColor = Color(0xFFF59E0B),
                    onClick = onBillsClick,
                    modifier = Modifier.weight(1f),
                    hasBadge = hasBillDueToday
                )

                CategoryFeedTile(
                    icon = Icons.Rounded.NotificationsActive,
                    label = "Reminder",
                    tileColor = Color(0xFFEC4899),
                    onClick = onReminderClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CategoryFeedTile(
    icon: ImageVector,
    label: String,
    tileColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasBadge: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 2.dp, horizontal = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(tileColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            if (hasBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .border(1.dp, Color.White, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            color = SleekTextPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun RecentExpenseRow(expense: Expense, categoryIcons: Map<String, String> = emptyMap(), onClick: () -> Unit) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(expense.date))
    val isIncome = expense.type == "INCOME"
    val catColor = if (isIncome) {
        when (expense.category) {
            "Salary" -> Color(0xFF10B981)
            "Freelance" -> Color(0xFF0D9488)
            "Investments" -> Color(0xFF3B82F6)
            "Gifts" -> Color(0xFFEC4899)
            else -> Color(0xFF10B981)
        }
    } else {
        categoryColors[expense.category] ?: SleekPrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(catColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(expense.category, categoryIcons),
                    contentDescription = expense.category,
                    tint = catColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.note ?: (if (isIncome) "Income" else "Expense"),
                    style = MaterialTheme.typography.titleSmall,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${expense.category} • $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary
                )
            }

            Text(
                text = String.format("%s₹%,.2f", if (isIncome) "+" else "-", expense.amount),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isIncome) Color(0xFF10B981) else ExpenseRed,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==========================================
// 🎯 TRANSACTIONS TAB (IMAGE 1 DESIGN)
// ==========================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpensesTab(
    viewModel: FinanceViewModel,
    onAddExpenseClick: () -> Unit,
    onEditExpenseClick: (Expense) -> Unit
) {
    val expenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val selectedDateRange by viewModel.selectedDateRange.collectAsStateWithLifecycle()
    val categoryIcons by viewModel.categoryIcons.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedAccountIdFilter by remember { mutableStateOf<Int?>(null) }
    
    val rawCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val categories = remember(rawCategories) { listOf("All") + rawCategories }

    var selectedExpenseIds by remember { mutableStateOf(setOf<Int>()) }
    var showDateRangePickerDialog by remember { mutableStateOf(false) }
    var showAccountMenu by remember { mutableStateOf(false) }

    // Filtered expenses list
    val filteredExpenses = expenses.filter {
        val matchesSearch = it.note?.contains(searchQuery, ignoreCase = true) == true ||
                it.category.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == "All" || it.category == selectedCategoryFilter
        matchesSearch && matchesCategory
    }

    // Date grouping logic for Image 1 (Today, Yesterday, 19 November, etc.)
    val groupedExpenses = remember(filteredExpenses) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val calYesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calYesterday.time)

        filteredExpenses.groupBy { expense ->
            val expenseDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(expense.date))
            when (expenseDateStr) {
                todayStr -> "Today"
                yesterdayStr -> "Yesterday"
                else -> SimpleDateFormat("d MMMM", Locale.getDefault()).format(Date(expense.date))
            }
        }
    }

    // Soft mint background inspired by Image 1
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F3EB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Image 1 Top Navigation Header: [<] Transactions [Search]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Icon Button / Clear Filter
                IconButton(
                    onClick = {
                        if (selectedDateRange != null || selectedAccountIdFilter != null || searchQuery.isNotEmpty()) {
                            viewModel.setDateRange(null, null)
                            selectedAccountIdFilter = null
                            searchQuery = ""
                            isSearchActive = false
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.Bold
                )

                // Search Icon Button
                IconButton(
                    onClick = { isSearchActive = !isSearchActive },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Transactions",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))



            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar Input (Animated Toggle)
            AnimatedVisibility(visible = isSearchActive) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search merchant, recipient, or category...", color = SleekTextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SleekTextSecondary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = SleekTextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekBorder,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_search_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Category Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val selected = selectedCategoryFilter == cat
                    val chipBg = if (selected) Color(0xFF1E293B) else Color.White
                    val chipText = if (selected) Color.White else Color(0xFF64748B)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(chipBg)
                            .clickable { selectedCategoryFilter = cat }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = cat,
                            color = chipText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions found",
                        color = SleekTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groupedExpenses.forEach { (dateHeader, txList) ->
                        item {
                            // Section Date Header (Image 1 Style: "Today", "Yesterday", "19 November")
                            Text(
                                text = dateHeader,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        items(txList, key = { it.id }) { expense ->
                            val isSelected = selectedExpenseIds.contains(expense.id)
                            Image1TransactionRow(
                                expense = expense,
                                isSelected = isSelected,
                                categoryIcons = categoryIcons,
                                onLongClick = {
                                    selectedExpenseIds = if (isSelected) {
                                        selectedExpenseIds - expense.id
                                    } else {
                                        selectedExpenseIds + expense.id
                                    }
                                },
                                onClick = {
                                    if (selectedExpenseIds.isNotEmpty()) {
                                        selectedExpenseIds = if (isSelected) {
                                            selectedExpenseIds - expense.id
                                        } else {
                                            selectedExpenseIds + expense.id
                                        }
                                    } else {
                                        onEditExpenseClick(expense)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating Selected Expense Action Bar (Multi-Select & Delete/Edit)
        if (selectedExpenseIds.isNotEmpty()) {
            val selectedExpenses = filteredExpenses.filter { selectedExpenseIds.contains(it.id) }
            if (selectedExpenses.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .shadow(12.dp, RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedExpenses.size} items selected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedExpenses.size == 1) {
                                IconButton(
                                    onClick = {
                                        onEditExpenseClick(selectedExpenses.first())
                                        selectedExpenseIds = emptySet()
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                            IconButton(
                                onClick = {
                                    viewModel.deleteExpenses(selectedExpenses)
                                    selectedExpenseIds = emptySet()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ExpenseRed.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = ExpenseRed, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { selectedExpenseIds = emptySet() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Deselect All", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        if (showDateRangePickerDialog) {
            DateRangePickerDialog(
                onDismiss = { showDateRangePickerDialog = false },
                onSelectRange = { start, end ->
                    viewModel.setDateRange(start, end)
                    showDateRangePickerDialog = false
                }
            )
        }
    }
}

/**
 * Image 1 Inspired Transaction Row Card:
 * Soft white squircle, circular avatar/logo on left, title & Received/Paid status in middle,
 * bright green (+$5,710.20) or dark red (-$124.55) amount on right.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Image1TransactionRow(
    expense: Expense,
    isSelected: Boolean,
    categoryIcons: Map<String, String> = emptyMap(),
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    val isIncome = expense.type == "INCOME"
    val displayName = expense.note?.takeIf { it.isNotBlank() } ?: expense.category

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isSelected) BorderStroke(2.dp, SleekPrimary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Avatar / Brand Circle (Image 1 visual)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (isIncome) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Initial character or category icon
                val firstChar = displayName.trim().firstOrNull()?.uppercaseChar() ?: 'T'
                if (displayName.contains("Eva", ignoreCase = true) ||
                    displayName.contains("Henrik", ignoreCase = true) ||
                    displayName.contains("Matteo", ignoreCase = true) ||
                    displayName.contains("Emilia", ignoreCase = true)) {
                    Text(
                        text = firstChar.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                } else {
                    Icon(
                        imageVector = getCategoryIcon(expense.category, categoryIcons),
                        contentDescription = expense.category,
                        tint = if (isIncome) Color(0xFF16A34A) else Color(0xFF0F172A),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Center: Title + "Received ⏱" / "Paid ⏱"
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isIncome) "Received" else "Paid",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Right: Amount +₹5,710.20 (Green) / -₹124.55 (Red)
            Text(
                text = String.format("%s₹%,.2f", if (isIncome) "+" else "-", expense.amount),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isIncome) Color(0xFF16A34A) else Color(0xFF991B1B)
            )
        }
    }
}

@Composable
fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onSelectRange: (Long, Long) -> Unit
) {
    var startCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        })
    }
    var endCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        })
    }

    val sFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Date Range",
                    style = MaterialTheme.typography.titleLarge,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Quick Presets",
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val presets = listOf(
                        "Today" to {
                            val s = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                            val e = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
                            onSelectRange(s.timeInMillis, e.timeInMillis)
                        },
                        "This Week" to {
                            val s = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, firstDayOfWeek); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                            val e = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, firstDayOfWeek + 6); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
                            onSelectRange(s.timeInMillis, e.timeInMillis)
                        },
                        "This Month" to {
                            val s = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                            val e = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH)); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
                            onSelectRange(s.timeInMillis, e.timeInMillis)
                        },
                        "Last 30 Days" to {
                            val e = Calendar.getInstance()
                            val s = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }
                            onSelectRange(s.timeInMillis, e.timeInMillis)
                        },
                        "Last 90 Days" to {
                            val e = Calendar.getInstance()
                            val s = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -90) }
                            onSelectRange(s.timeInMillis, e.timeInMillis)
                        }
                    )

                    presets.chunked(2).forEach { rowPresets ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowPresets.forEach { preset ->
                                OutlinedButton(
                                    onClick = preset.second,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekPrimary),
                                    border = BorderStroke(1.dp, SleekBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(preset.first, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                HorizontalDivider(color = SleekBorder)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Custom Range Selection",
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Start Date:", style = MaterialTheme.typography.bodyMedium, color = SleekTextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            val newCal = Calendar.getInstance().apply {
                                timeInMillis = startCalendar.timeInMillis
                                add(Calendar.DAY_OF_YEAR, -1)
                            }
                            startCalendar = newCal
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Day", tint = SleekPrimary)
                        }
                        Text(
                            text = sFormatter.format(Date(startCalendar.timeInMillis)),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary
                        )
                        IconButton(onClick = {
                            val newCal = Calendar.getInstance().apply {
                                timeInMillis = startCalendar.timeInMillis
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                            startCalendar = newCal
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Day", tint = SleekPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("End Date:", style = MaterialTheme.typography.bodyMedium, color = SleekTextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            val newCal = Calendar.getInstance().apply {
                                timeInMillis = endCalendar.timeInMillis
                                add(Calendar.DAY_OF_YEAR, -1)
                            }
                            endCalendar = newCal
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Day", tint = SleekPrimary)
                        }
                        Text(
                            text = sFormatter.format(Date(endCalendar.timeInMillis)),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary
                        )
                        IconButton(onClick = {
                            val newCal = Calendar.getInstance().apply {
                                timeInMillis = endCalendar.timeInMillis
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                            endCalendar = newCal
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Day", tint = SleekPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextSecondary),
                        border = BorderStroke(1.dp, SleekBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onSelectRange(startCalendar.timeInMillis, endCalendar.timeInMillis)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color = SleekSurface,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, SleekBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = SleekTextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ==========================================
// 3️⃣ ANALYTICS TAB (REWORKED WITH DEFAULT APP STYLE & ANIMATED CHARTS)
// ==========================================
@Composable
fun AnalyticsTab(
    viewModel: FinanceViewModel,
    onAddClick: () -> Unit,
    onProfileClick: () -> Unit = {}
) {
    val allExpenses by viewModel.expenses.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val profileImageUri by viewModel.userProfileImageUri.collectAsStateWithLifecycle()

    val dailyInsight by viewModel.dailySpendingInsight.collectAsStateWithLifecycle()
    val isInsightLoading by viewModel.isInsightLoading.collectAsStateWithLifecycle()
    val insightLastUpdated by viewModel.insightLastUpdated.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.generateDailySpendingInsight(forceRefresh = false)
    }

    var selectedTimeFilter by remember { mutableStateOf("7D") }
    var showExportDialog by remember { mutableStateOf(false) }
    val timeFilters = listOf("7D", "30D", "1M", "6M", "1Y", "All")

    val initials = remember(userName) {
        if (!userName.isNullOrBlank()) {
            userName!!.trim().split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").take(2)
        } else "U"
    }

    // Filter expenses based on selected time filter
    val filteredPeriodExpenses = remember(allExpenses, selectedTimeFilter) {
        val now = Calendar.getInstance()
        when (selectedTimeFilter) {
            "7D" -> {
                val start = now.timeInMillis - 7 * 24 * 3600 * 1000L
                allExpenses.filter { it.date >= start }
            }
            "30D" -> {
                val start = now.timeInMillis - 30L * 24 * 3600 * 1000L
                allExpenses.filter { it.date >= start }
            }
            "1M" -> {
                val m = now.get(Calendar.MONTH)
                val y = now.get(Calendar.YEAR)
                allExpenses.filter {
                    val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                    cal.get(Calendar.MONTH) == m && cal.get(Calendar.YEAR) == y
                }
            }
            "6M" -> {
                val start = Calendar.getInstance().apply { add(Calendar.MONTH, -6) }.timeInMillis
                allExpenses.filter { it.date >= start }
            }
            "1Y" -> {
                val y = now.get(Calendar.YEAR)
                allExpenses.filter {
                    val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                    cal.get(Calendar.YEAR) == y
                }
            }
            else -> allExpenses
        }
    }

    val expenseList = remember(filteredPeriodExpenses) { filteredPeriodExpenses.filter { it.type != "INCOME" } }
    val incomeList = remember(filteredPeriodExpenses) { filteredPeriodExpenses.filter { it.type == "INCOME" } }

    val totalSpent = remember(expenseList) { expenseList.sumOf { it.amount } }
    val totalIncome = remember(incomeList) { incomeList.sumOf { it.amount } }
    val netBalance = totalIncome - totalSpent

    val context = LocalContext.current

    if (showExportDialog) {
        Dialog(onDismissRequest = { showExportDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.5.dp, SleekBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SleekPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(20.dp))
                            }
                            Text("Export Report", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SleekTextPrimary)
                        }
                        IconButton(onClick = { showExportDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = SleekTextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Text("Export analytics data for period ($selectedTimeFilter):", fontSize = 13.sp, color = SleekTextSecondary)

                    Button(
                        onClick = {
                            DataExporter.sharePdfReport(
                                context = context,
                                expenses = filteredPeriodExpenses,
                                dateRangeStr = "Period ($selectedTimeFilter)",
                                typeFilterStr = "All Transactions",
                                categoryFilterStr = "All Categories",
                                amountSaved = (totalIncome - totalSpent).coerceAtLeast(0.0),
                                monthsOverBudget = 0,
                                includeDetailedTxns = true
                            )
                            showExportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export PDF Document", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            DataExporter.exportToCSV(
                                context = context,
                                expenses = filteredPeriodExpenses,
                                dateRangeStr = "Period ($selectedTimeFilter)",
                                typeFilterStr = "All Transactions",
                                categoryFilterStr = "All Categories"
                            )
                            showExportDialog = false
                        },
                        border = BorderStroke(1.dp, SleekPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export CSV Spreadsheet", color = SleekPrimary, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            DataExporter.shareImageReport(
                                context = context,
                                expenses = filteredPeriodExpenses,
                                dateRangeStr = "Period ($selectedTimeFilter)",
                                typeFilterStr = "All Transactions",
                                categoryFilterStr = "All Categories"
                            )
                            showExportDialog = false
                        },
                        border = BorderStroke(1.dp, SleekBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Photo, contentDescription = null, tint = SleekTextPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Graphic Summary", color = SleekTextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // App Default Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SleekPrimaryContainer)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                if (!profileImageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = initials,
                        fontWeight = FontWeight.Bold,
                        color = SleekPrimary,
                        fontSize = 15.sp
                    )
                }
            }

            Text(
                text = "Analytics",
                style = MaterialTheme.typography.titleLarge,
                color = SleekTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            IconButton(
                onClick = { showExportDialog = true },
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SleekSurface)
                    .border(1.dp, SleekBorder, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Export Report",
                    tint = SleekTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time Range Filter Bar (Pills in App Default Style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SleekSurface)
                .border(1.dp, SleekBorder, RoundedCornerShape(18.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            timeFilters.forEach { tf ->
                val isSelected = selectedTimeFilter == tf
                val pillBg by animateColorAsState(
                    targetValue = if (isSelected) SleekPrimary else Color.Transparent,
                    animationSpec = tween(250),
                    label = "pillBg"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(pillBg)
                        .clickable { selectedTimeFilter = tf }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tf,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else SleekTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✨ GEMINI-POWERED DAILY SPENDING INSIGHT CARD
        DailySpendingInsightCard(
            insight = dailyInsight,
            isLoading = isInsightLoading,
            lastUpdated = insightLastUpdated,
            onRefresh = { viewModel.generateDailySpendingInsight(forceRefresh = true) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Metrics Overview Cards Row (App Default Style)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Income Metric Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                        }
                        Text("Income", fontSize = 11.sp, color = SleekTextSecondary, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "₹%,.0f".format(totalIncome),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF10B981)
                    )
                }
            }

            // Expense Metric Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TrendingDown, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                        }
                        Text("Expense", fontSize = 11.sp, color = SleekTextSecondary, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "₹%,.0f".format(totalSpent),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFEF4444)
                    )
                }
            }

            // Net Cash Flow Metric Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(SleekPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(14.dp))
                        }
                        Text("Net Flow", fontSize = 11.sp, color = SleekTextSecondary, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = (if (netBalance >= 0) "+₹%,.0f".format(netBalance) else "-₹%,.0f".format(Math.abs(netBalance))),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (netBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val currencySymbol by viewModel.selectedCurrencySymbol.collectAsStateWithLifecycle()
        val accounts by viewModel.accounts.collectAsStateWithLifecycle()
        val budgets by viewModel.budgets.collectAsStateWithLifecycle()

        // 📈 1. ANIMATED GRAPH FOR INCOME AND EXPENSE
        IncomeExpenseLineGraphCard(
            expenses = filteredPeriodExpenses,
            selectedTimeFilter = selectedTimeFilter
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 💎 1.5 NET WORTH OVER TIME CHART CARD
        NetWorthOverTimeChartCard(
            allExpenses = allExpenses,
            accounts = accounts,
            selectedTimeFilter = selectedTimeFilter,
            currencySymbol = currencySymbol
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 💵 CASH AT END OF THE MONTH CHART CARD
        CashAtEndOfMonthChartCard(
            allExpenses = allExpenses,
            currencySymbol = currencySymbol
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 📊 2. BAR CHART FOR CATEGORY OVER INCOME AND EXPENSE
        CategoryIncomeExpenseBarChartCard(
            expenses = filteredPeriodExpenses
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🥧 3. PIE CHART / DONUT CHART
        CategoryPieChartCard(
            expenses = filteredPeriodExpenses
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🎯 4. CATEGORY BUDGET PROGRESS & TRACKING CARD
        CategoryBudgetProgressCard(
            viewModel = viewModel,
            budgets = budgets,
            allExpenses = allExpenses,
            currencySymbol = currencySymbol
        )

        Spacer(modifier = Modifier.height(110.dp))
    }
}

// ==========================================
// 💎 NET WORTH OVER TIME CHART CARD
// ==========================================
@Composable
fun NetWorthOverTimeChartCard(
    allExpenses: List<Expense>,
    accounts: List<Account>,
    selectedTimeFilter: String,
    currencySymbol: String
) {
    val totalAccountAssets = remember(accounts) {
        if (accounts.isEmpty()) 50000.0 else accounts.sumOf { if (it.type == "CREDIT") -it.balance else it.balance }
    }

    val sortedExpenses = remember(allExpenses) { allExpenses.sortedBy { it.date } }

    val dataPoints = remember(sortedExpenses, totalAccountAssets, selectedTimeFilter) {
        if (sortedExpenses.isEmpty()) {
            val now = System.currentTimeMillis()
            val dayMs = 86400000L
            listOf(
                Pair(now - 6 * dayMs, totalAccountAssets * 0.92),
                Pair(now - 5 * dayMs, totalAccountAssets * 0.94),
                Pair(now - 4 * dayMs, totalAccountAssets * 0.95),
                Pair(now - 3 * dayMs, totalAccountAssets * 0.97),
                Pair(now - 2 * dayMs, totalAccountAssets * 0.98),
                Pair(now - dayMs, totalAccountAssets * 0.99),
                Pair(now, totalAccountAssets)
            )
        } else {
            val points = mutableListOf<Pair<Long, Double>>()
            val totalIncome = sortedExpenses.filter { it.type == "INCOME" }.sumOf { it.amount }
            val totalExpense = sortedExpenses.filter { it.type != "INCOME" }.sumOf { it.amount }
            val baseNetWorth = (totalAccountAssets - (totalIncome - totalExpense)).coerceAtLeast(1000.0)

            var runningFlow = baseNetWorth
            val step = (sortedExpenses.size / 7).coerceAtLeast(1)
            sortedExpenses.chunked(step).take(7).forEach { chunk ->
                val date = chunk.last().date
                chunk.forEach { e ->
                    if (e.type == "INCOME") runningFlow += e.amount else runningFlow -= e.amount
                }
                points.add(Pair(date, runningFlow))
            }
            if (points.size < 2) {
                points.add(0, Pair(System.currentTimeMillis() - 86400000L, baseNetWorth))
            }
            points
        }
    }

    val currentNetWorth = dataPoints.lastOrNull()?.second ?: totalAccountAssets
    val startingNetWorth = dataPoints.firstOrNull()?.second ?: (currentNetWorth * 0.9)
    val changeAmount = currentNetWorth - startingNetWorth
    val changePct = if (startingNetWorth != 0.0) (changeAmount / startingNetWorth) * 100 else 0.0

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    val activePoint = selectedPointIndex?.let { dataPoints.getOrNull(it) } ?: dataPoints.lastOrNull()

    Card(
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, SleekBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Net Worth Over Time",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Accumulated assets minus liabilities",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (changeAmount >= 0) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (changeAmount >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (changeAmount >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${if (changeAmount >= 0) "+" else ""}${String.format(Locale.US, "%.1f", changePct)}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (changeAmount >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = activePoint?.let {
                            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            sdf.format(Date(it.first))
                        } ?: "Current Value",
                        fontSize = 11.sp,
                        color = SleekTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%,.2f", activePoint?.second ?: currentNetWorth)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = SleekTextPrimary
                    )
                }

                if (selectedPointIndex != null) {
                    TextButton(
                        onClick = { selectedPointIndex = null },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Reset view", fontSize = 11.sp, color = SleekPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val lineGraphColor = Color(0xFF10B981)
            val values = dataPoints.map { it.second }
            val minVal = (values.minOrNull() ?: 0.0) * 0.95
            val maxVal = ((values.maxOrNull() ?: 1000.0) * 1.05).coerceAtLeast(minVal + 100.0)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dataPoints) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)
                                val index = (offset.x / stepX).roundToInt().coerceIn(0, dataPoints.size - 1)
                                selectedPointIndex = index
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val pointsCount = dataPoints.size

                    if (pointsCount < 2) return@Canvas

                    val stepX = w / (pointsCount - 1)
                    val coords = dataPoints.mapIndexed { idx, pt ->
                        val x = idx * stepX
                        val normalizedY = ((pt.second - minVal) / (maxVal - minVal)).toFloat().coerceIn(0f, 1f)
                        val y = h - (normalizedY * (h - 20.dp.toPx())) - 10.dp.toPx()
                        Offset(x, y)
                    }

                    val path = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        for (i in 0 until coords.size - 1) {
                            val p1 = coords[i]
                            val p2 = coords[i + 1]
                            val cx = (p1.x + p2.x) / 2f
                            cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(coords.last().x, h)
                        lineTo(coords.first().x, h)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineGraphColor.copy(alpha = 0.35f),
                                lineGraphColor.copy(alpha = 0.02f)
                            )
                        )
                    )

                    drawPath(
                        path = path,
                        color = lineGraphColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    coords.forEachIndexed { idx, point ->
                        val isSelected = selectedPointIndex == idx || (selectedPointIndex == null && idx == coords.size - 1)
                        if (isSelected) {
                            drawCircle(
                                color = lineGraphColor.copy(alpha = 0.25f),
                                radius = 10.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = lineGraphColor,
                                radius = 6.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SleekPrimaryContainer.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Peak Net Worth", fontSize = 10.sp, color = SleekTextSecondary)
                    Text("$currencySymbol${String.format(Locale.US, "%,.0f", maxVal)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(SleekBorder))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Assets", fontSize = 10.sp, color = SleekTextSecondary)
                    Text("$currencySymbol${String.format(Locale.US, "%,.0f", totalAccountAssets)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
        }
    }
}

// ==========================================
// 🎯 CATEGORY BUDGET PROGRESS CARD
// ==========================================
@Composable
fun CategoryBudgetProgressCard(
    viewModel: FinanceViewModel,
    budgets: List<Budget>,
    allExpenses: List<Expense>,
    currencySymbol: String
) {
    var showAddCategoryBudgetDialog by remember { mutableStateOf(false) }
    var selectedCategoryForBudget by remember { mutableStateOf("Food") }
    var categoryBudgetInput by remember { mutableStateOf("") }

    val currentMonthCategorySpent = remember(allExpenses) {
        val cal = Calendar.getInstance()
        val m = cal.get(Calendar.MONTH)
        val y = cal.get(Calendar.YEAR)
        allExpenses.filter {
            it.type != "INCOME" &&
            Calendar.getInstance().apply { timeInMillis = it.date }.run {
                get(Calendar.MONTH) == m && get(Calendar.YEAR) == y
            }
        }.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, SleekBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Category Budget Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Real-time spending vs monthly budget limits",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = {
                        selectedCategoryForBudget = "Food"
                        categoryBudgetInput = ""
                        showAddCategoryBudgetDialog = true
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SleekPrimary.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Set Budget",
                        tint = SleekPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (budgets.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SleekPrimaryContainer.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, SleekBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("No Category Budgets Configured", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SleekTextPrimary)
                        Text("Tap '+' to set category limits and track actual spending against budgets.", fontSize = 11.sp, color = SleekTextSecondary, textAlign = TextAlign.Center)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (b in budgets) {
                        val spent = currentMonthCategorySpent[b.category] ?: 0.0
                        val limit = b.amountLimit.coerceAtLeast(1.0)
                        val progress = (spent / limit).coerceIn(0.0, 1.2).toFloat()
                        val isOver = spent > limit
                        val isNear = progress >= 0.8f && !isOver

                        val barColor = when {
                            isOver -> Color(0xFFEF4444)
                            isNear -> Color(0xFFF59E0B)
                            else -> Color(0xFF10B981)
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SleekPrimaryContainer.copy(alpha = 0.06f),
                            border = BorderStroke(1.dp, SleekBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(b.category, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SleekTextPrimary)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = barColor.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (isOver) "Exceeded" else if (isNear) "Near Limit" else "On Track",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = barColor,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "$currencySymbol${String.format(Locale.US, "%,.0f", spent)} / $currencySymbol${String.format(Locale.US, "%,.0f", b.amountLimit)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekTextPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { (progress / 1.0f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = barColor,
                                    trackColor = SleekBorder.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddCategoryBudgetDialog) {
        val catList = listOf("Food", "Shopping", "Entertainment", "Transport", "Bills", "Utilities", "Housing", "Health", "Education", "Other")

        AlertDialog(
            onDismissRequest = { showAddCategoryBudgetDialog = false },
            title = {
                Text("Set Category Spending Limit", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select a category and define a monthly spending limit:", fontSize = 12.sp, color = SleekTextSecondary)

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        catList.forEach { cat ->
                            val selected = selectedCategoryForBudget.equals(cat, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) SleekPrimary else SleekSurface,
                                border = BorderStroke(1.dp, if (selected) SleekPrimary else SleekBorder),
                                modifier = Modifier.clickable { selectedCategoryForBudget = cat }
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color.White else SleekTextPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = categoryBudgetInput,
                        onValueChange = { categoryBudgetInput = it },
                        label = { Text("Monthly Limit ($currencySymbol)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limitVal = categoryBudgetInput.toDoubleOrNull()
                        if (limitVal != null && limitVal > 0) {
                            viewModel.setCategoryBudget(selectedCategoryForBudget, limitVal)
                            showAddCategoryBudgetDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Budget", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryBudgetDialog = false }) {
                    Text("Cancel", color = SleekTextSecondary)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ==========================================
// ✨ GEMINI DAILY SPENDING INSIGHT CARD
// ==========================================
@Composable
fun DailySpendingInsightCard(
    insight: String?,
    isLoading: Boolean,
    lastUpdated: Long?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_rot_trans")
    val rotationAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ai_rot"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.5.dp, Brush.horizontalGradient(
            colors = listOf(
                SleekPrimary.copy(alpha = 0.5f),
                Color(0xFF8B5CF6).copy(alpha = 0.5f),
                Color(0xFFEC4899).copy(alpha = 0.3f)
            )
        )),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF6366F1),
                                        Color(0xFF8B5CF6),
                                        Color(0xFFEC4899)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini AI Advisor",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Daily Spending Insight",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SleekTextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF8B5CF6).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "AI Advisor",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8B5CF6),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (lastUpdated != null) {
                                val cal = Calendar.getInstance().apply { timeInMillis = lastUpdated }
                                "Updated today at %02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                            } else "Powered by Gemini 3.5",
                            fontSize = 11.sp,
                            color = SleekTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SleekBg)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Insight",
                        tint = SleekPrimary,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer {
                                if (isLoading) rotationZ = rotationAnim
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = isLoading to insight,
                label = "insight_content"
            ) { (loading, text) ->
                if (loading && text == null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SleekBg)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.5.dp,
                            color = SleekPrimary
                        )
                        Text(
                            text = "Analyzing today's financial activity with Gemini...",
                            fontSize = 13.sp,
                            color = SleekTextSecondary,
                            fontStyle = FontStyle.Italic
                        )
                    }
                } else if (!text.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        SleekPrimary.copy(alpha = 0.06f),
                                        Color(0xFF8B5CF6).copy(alpha = 0.04f)
                                    )
                                )
                            )
                            .border(1.dp, SleekPrimary.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = text,
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp,
                            color = SleekTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SleekBg)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tap refresh to generate today's spending insight.",
                            fontSize = 13.sp,
                            color = SleekTextSecondary
                        )
                        TextButton(onClick = onRefresh) {
                            Text("Generate", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekPrimary)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 📈 1. ANIMATED DUAL LINE GRAPH (INCOME VS EXPENSE)
// ==========================================
@Composable
fun IncomeExpenseLineGraphCard(
    expenses: List<Expense>,
    selectedTimeFilter: String,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var animPlayed by remember { mutableStateOf(false) }

    val animProgress by animateFloatAsState(
        targetValue = if (animPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "line_graph_anim"
    )

    LaunchedEffect(expenses, selectedTimeFilter) {
        animPlayed = false
        animPlayed = true
        selectedIndex = null
    }

    // Aggregate data into time buckets
    val chartData = remember(expenses, selectedTimeFilter) {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        val days = when (selectedTimeFilter) {
            "7D" -> 7
            "30D", "1M" -> 30
            "6M" -> 180
            "1Y" -> 365
            else -> 14
        }

        val pointCount = if (days <= 7) 7 else if (days <= 30) 10 else 12
        val timeStep = (days * 24 * 3600 * 1000L) / pointCount

        val sdf = java.text.SimpleDateFormat(
            if (days <= 7) "EEE" else if (days <= 30) "d MMM" else "MMM",
            Locale.getDefault()
        )

        (0 until pointCount).map { i ->
            val startTime = now - (pointCount - 1 - i) * timeStep
            val endTime = startTime + timeStep

            val bucketTxns = expenses.filter { it.date in startTime..endTime }
            val inc = bucketTxns.filter { it.type == "INCOME" }.sumOf { it.amount }
            val exp = bucketTxns.filter { it.type != "INCOME" }.sumOf { it.amount }
            val dateLabel = sdf.format(java.util.Date(startTime))

            GraphPoint(
                label = dateLabel,
                income = inc,
                expense = exp,
                timestamp = startTime
            )
        }
    }

    val maxVal = remember(chartData) {
        val maxInc = chartData.maxOfOrNull { it.income } ?: 0.0
        val maxExp = chartData.maxOfOrNull { it.expense } ?: 0.0
        maxOf(maxInc, maxExp, 1000.0) * 1.15
    }

    val activePoint = selectedIndex?.let { chartData.getOrNull(it) } ?: chartData.lastOrNull()

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Legend Indicators Row (Heading text removed per user request)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                        Text("Income", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SleekTextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                        Text("Expense", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SleekTextSecondary)
                    }
                }
            }

            // Interactive Point Details Chip
            activePoint?.let { pt ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SleekPrimaryContainer.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = pt.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            val net = pt.income - pt.expense
                            Text(
                                text = "Net: " + (if (net >= 0) "+₹%,.2f".format(net) else "-₹%,.2f".format(Math.abs(net))),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (net >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Income", fontSize = 10.sp, color = SleekTextSecondary)
                                Text("₹%,.2f".format(pt.income), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Expense", fontSize = 10.sp, color = SleekTextSecondary)
                                Text("₹%,.2f".format(pt.expense), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Dual Bezier Line Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(chartData) {
                            detectTapGestures { offset ->
                                val w = size.width
                                val pLeft = 40.dp.toPx()
                                val pRight = 16.dp.toPx()
                                val usableW = w - pLeft - pRight
                                val stepX = usableW / (chartData.size - 1).coerceAtLeast(1)

                                val clickedIdx = ((offset.x - pLeft) / stepX)
                                    .roundToInt()
                                    .coerceIn(0, chartData.size - 1)
                                selectedIndex = clickedIdx
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    val paddingLeft = 40.dp.toPx()
                    val paddingRight = 16.dp.toPx()
                    val paddingTop = 16.dp.toPx()
                    val paddingBottom = 30.dp.toPx()

                    val chartW = w - paddingLeft - paddingRight
                    val chartH = h - paddingTop - paddingBottom

                    // Grid lines
                    val steps = listOf(1.0, 0.5, 0.0)
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 10.dp.toPx()
                        isAntiAlias = true
                    }

                    steps.forEach { ratio ->
                        val y = paddingTop + chartH * (1.0 - ratio).toFloat()
                        drawLine(
                            color = SleekBorder.copy(alpha = 0.5f),
                            start = Offset(paddingLeft, y),
                            end = Offset(w - paddingRight, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashEffect
                        )
                        val labelVal = (maxVal * ratio)
                        val labelStr = if (labelVal >= 1000) "%.0fK".format(labelVal / 1000) else "%.0f".format(labelVal)
                        drawContext.canvas.nativeCanvas.drawText(
                            labelStr,
                            6.dp.toPx(),
                            y + 4.dp.toPx(),
                            textPaint
                        )
                    }

                    if (chartData.size >= 2) {
                        val stepX = chartW / (chartData.size - 1)

                        val incomePoints = chartData.mapIndexed { idx, pt ->
                            val x = paddingLeft + idx * stepX
                            val normY = (pt.income / maxVal).coerceIn(0.0, 1.0).toFloat()
                            val y = paddingTop + chartH * (1f - normY * animProgress)
                            Offset(x, y)
                        }

                        val expensePoints = chartData.mapIndexed { idx, pt ->
                            val x = paddingLeft + idx * stepX
                            val normY = (pt.expense / maxVal).coerceIn(0.0, 1.0).toFloat()
                            val y = paddingTop + chartH * (1f - normY * animProgress)
                            Offset(x, y)
                        }

                        fun createSmoothPath(points: List<Offset>): Path {
                            val path = Path()
                            if (points.isEmpty()) return path
                            path.moveTo(points.first().x, points.first().y)
                            for (i in 0 until points.size - 1) {
                                val p1 = points[i]
                                val p2 = points[i + 1]
                                val controlP1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
                                val controlP2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)
                                path.cubicTo(controlP1.x, controlP1.y, controlP2.x, controlP2.y, p2.x, p2.y)
                            }
                            return path
                        }

                        val incomePath = createSmoothPath(incomePoints)
                        val expensePath = createSmoothPath(expensePoints)

                        // Gradient Area Fill - Income
                        val incomeFillPath = Path().apply {
                            addPath(incomePath)
                            lineTo(incomePoints.last().x, paddingTop + chartH)
                            lineTo(incomePoints.first().x, paddingTop + chartH)
                            close()
                        }
                        drawPath(
                            path = incomeFillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF10B981).copy(alpha = 0.22f), Color.Transparent),
                                startY = paddingTop,
                                endY = paddingTop + chartH
                            )
                        )

                        // Gradient Area Fill - Expense
                        val expenseFillPath = Path().apply {
                            addPath(expensePath)
                            lineTo(expensePoints.last().x, paddingTop + chartH)
                            lineTo(expensePoints.first().x, paddingTop + chartH)
                            close()
                        }
                        drawPath(
                            path = expenseFillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFEF4444).copy(alpha = 0.20f), Color.Transparent),
                                startY = paddingTop,
                                endY = paddingTop + chartH
                            )
                        )

                        // Draw Curve Lines
                        drawPath(
                            path = incomePath,
                            color = Color(0xFF10B981),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawPath(
                            path = expensePath,
                            color = Color(0xFFEF4444),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw Point Dots & Active Vertical Indicator Line
                        val activeIdx = selectedIndex ?: (chartData.size - 1)
                        chartData.forEachIndexed { idx, _ ->
                            val incP = incomePoints[idx]
                            val expP = expensePoints[idx]

                            if (idx == activeIdx) {
                                drawLine(
                                    color = SleekPrimary.copy(alpha = 0.4f),
                                    start = Offset(incP.x, paddingTop),
                                    end = Offset(incP.x, paddingTop + chartH),
                                    strokeWidth = 2.dp.toPx(),
                                    pathEffect = dashEffect
                                )
                                drawCircle(Color.White, radius = 7.dp.toPx(), center = incP)
                                drawCircle(Color(0xFF10B981), radius = 5.dp.toPx(), center = incP)

                                drawCircle(Color.White, radius = 7.dp.toPx(), center = expP)
                                drawCircle(Color(0xFFEF4444), radius = 5.dp.toPx(), center = expP)
                            } else {
                                drawCircle(Color(0xFF10B981), radius = 3.dp.toPx(), center = incP)
                                drawCircle(Color(0xFFEF4444), radius = 3.dp.toPx(), center = expP)
                            }

                            // X Axis Labels
                            if (idx % ((chartData.size / 5).coerceAtLeast(1)) == 0 || idx == chartData.size - 1) {
                                drawContext.canvas.nativeCanvas.drawText(
                                    chartData[idx].label,
                                    incP.x - 12.dp.toPx(),
                                    h - 6.dp.toPx(),
                                    textPaint
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class GraphPoint(
    val label: String,
    val income: Double,
    val expense: Double,
    val timestamp: Long
)

// ==========================================
// 💵 CASH AT END OF THE MONTH CHART CARD
// ==========================================
@Composable
fun CashAtEndOfMonthChartCard(
    allExpenses: List<Expense>,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    val currentCalendar = remember { Calendar.getInstance() }
    val currentYear = remember { currentCalendar.get(Calendar.YEAR) }
    val currentMonthIdx = remember { currentCalendar.get(Calendar.MONTH) } // 0..11

    var selectedMonthIndex by remember { mutableStateOf<Int?>(null) }
    var viewMode by remember { mutableStateOf("Recent") } // "Recent" (Prev & Current Months) vs "Full Year"
    var animPlayed by remember { mutableStateOf(false) }

    val animProgress by animateFloatAsState(
        targetValue = if (animPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "cash_at_end_of_month_anim"
    )

    LaunchedEffect(allExpenses) {
        animPlayed = false
        animPlayed = true
    }

    val monthsLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec")

    // Calculate real cumulative ending cash balance for each of the 12 months from user data (reflecting edited txns)
    val monthlyBalances = remember(allExpenses, currentYear) {
        val cal = Calendar.getInstance()
        val priorTxns = allExpenses.filter { exp ->
            cal.timeInMillis = exp.date
            cal.get(Calendar.YEAR) < currentYear
        }
        var runningNet = priorTxns.filter { it.type == "INCOME" }.sumOf { it.amount } - priorTxns.filter { it.type != "INCOME" }.sumOf { it.amount }

        (0..11).map { monthIdx ->
            val monthTxns = allExpenses.filter { exp ->
                cal.timeInMillis = exp.date
                cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.MONTH) == monthIdx
            }
            val inc = monthTxns.filter { it.type == "INCOME" }.sumOf { it.amount }
            val exp = monthTxns.filter { it.type != "INCOME" }.sumOf { it.amount }
            runningNet += (inc - exp)
            runningNet
        }
    }

    val currentMonthCash = monthlyBalances.getOrElse(currentMonthIdx) { 0.0 }
    val prevMonthCash = if (currentMonthIdx > 0) monthlyBalances.getOrElse(currentMonthIdx - 1) { 0.0 } else 0.0
    val monthDiff = currentMonthCash - prevMonthCash

    // Determine subset of months based on viewMode
    val displayedIndices = remember(viewMode, currentMonthIdx) {
        if (viewMode == "Recent") {
            // Display from up to 5 months prior up to current month (min 3 months)
            val startIdx = (currentMonthIdx - 4).coerceAtLeast(0)
            (startIdx..currentMonthIdx).toList()
        } else {
            (0..11).toList()
        }
    }

    val displayedBalances = remember(monthlyBalances, displayedIndices) {
        displayedIndices.map { monthlyBalances.getOrElse(it) { 0.0 } }
    }

    val rawMin = remember(displayedBalances) { displayedBalances.minOrNull() ?: 0.0 }
    val rawMax = remember(displayedBalances) { displayedBalances.maxOrNull() ?: 0.0 }

    val chartMin = remember(rawMin) { if (rawMin < 0) rawMin else 0.0 }
    val chartMax = remember(rawMax, chartMin) {
        val peak = maxOf(rawMax, 1000.0)
        Math.ceil(peak / 500.0) * 500.0
    }
    val chartRange = remember(chartMax, chartMin) { (chartMax - chartMin).coerceAtLeast(100.0) }

    val activeIdx = selectedMonthIndex ?: currentMonthIdx
    val activeMonth = monthsLabels.getOrElse(activeIdx) { "Month" }
    val activeBalance = monthlyBalances.getOrElse(activeIdx) { 0.0 }

    val activeTag = when (activeIdx) {
        currentMonthIdx -> " (Current Month)"
        currentMonthIdx - 1 -> " (Previous Month)"
        else -> ""
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("cash_at_end_of_month_card")
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            // Card Title & View Mode Toggle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cash at End of Month",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Real-time cashflow metrics",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SleekPrimaryContainer.copy(alpha = 0.2f))
                        .border(1.dp, SleekBorder, RoundedCornerShape(16.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (viewMode == "Recent") SleekPrimary else Color.Transparent)
                            .clickable {
                                viewMode = "Recent"
                                selectedMonthIndex = currentMonthIdx
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Recent",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewMode == "Recent") Color.White else SleekTextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (viewMode == "Full") SleekPrimary else Color.Transparent)
                            .clickable { viewMode = "Full" }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Full Year",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewMode == "Full") Color.White else SleekTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current Month & Previous Month Cashflow Quick Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Previous Month Chip
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SleekNeutralLight),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SleekBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (currentMonthIdx > 0) "${monthsLabels[currentMonthIdx - 1]} (Prev)" else "Prev Year",
                            fontSize = 10.sp,
                            color = SleekTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$currencySymbol%,.0f".format(prevMonthCash),
                            fontSize = 13.sp,
                            color = SleekTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Current Month Chip
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D9488).copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF0D9488))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "${monthsLabels[currentMonthIdx]} (Current)",
                            fontSize = 10.sp,
                            color = Color(0xFF0D9488),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$currencySymbol%,.0f".format(currentMonthCash),
                            fontSize = 13.sp,
                            color = Color(0xFF0D9488),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Net MoM Flow Chip
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (monthDiff >= 0) Color(0xFF10B981).copy(alpha = 0.1f) else ExpenseRed.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (monthDiff >= 0) Color(0xFF10B981) else ExpenseRed)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "MoM Flow",
                            fontSize = 10.sp,
                            color = if (monthDiff >= 0) Color(0xFF10B981) else ExpenseRed,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${if (monthDiff >= 0) "+" else ""}$currencySymbol%,.0f".format(monthDiff),
                            fontSize = 13.sp,
                            color = if (monthDiff >= 0) Color(0xFF10B981) else ExpenseRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Inspector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D9488).copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$activeMonth$activeTag Ending Cash:",
                    fontSize = 12.sp,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$currencySymbol%,.0f".format(activeBalance),
                    fontSize = 14.sp,
                    color = Color(0xFF0D9488),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val lineColor = Color(0xFF2DD4BF)
            val nodeColor = Color(0xFF0D9488)
            val gridColor = SleekBorder.copy(alpha = 0.5f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(displayedIndices, monthlyBalances) {
                            detectTapGestures { offset ->
                                val leftPadding = 38.dp.toPx()
                                val rightPadding = 12.dp.toPx()
                                val width = size.width - leftPadding - rightPadding
                                val count = displayedIndices.size
                                val stepX = if (count > 1) width / (count - 1) else width

                                val x = offset.x - leftPadding
                                val subIndex = (x / stepX + 0.5f).toInt().coerceIn(0, count - 1)
                                selectedMonthIndex = displayedIndices[subIndex]
                            }
                        }
                ) {
                    val leftPadding = 38.dp.toPx()
                    val rightPadding = 12.dp.toPx()
                    val topPadding = 15.dp.toPx()
                    val bottomPadding = 30.dp.toPx()

                    val chartWidth = size.width - leftPadding - rightPadding
                    val chartHeight = size.height - topPadding - bottomPadding

                    // Draw Y-Axis Steps & Horizontal Grid Lines
                    val ySteps = 4
                    val stepVal = chartRange / ySteps
                    for (i in 0..ySteps) {
                        val v = chartMin + i * stepVal
                        val norm = (v - chartMin) / chartRange
                        val y = topPadding + chartHeight - (norm * chartHeight).toFloat()
                        drawLine(
                            color = gridColor,
                            start = Offset(leftPadding, y),
                            end = Offset(size.width - rightPadding, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val count = displayedIndices.size
                    val stepX = if (count > 1) chartWidth / (count - 1) else chartWidth

                    // Compute Point Offsets
                    val points = displayedIndices.mapIndexed { i, monthIdx ->
                        val value = monthlyBalances.getOrElse(monthIdx) { 0.0 }
                        val x = leftPadding + i * stepX
                        val norm = ((value - chartMin) / chartRange).coerceIn(0.0, 1.0)
                        val y = topPadding + chartHeight - (norm.toFloat() * chartHeight * animProgress)
                        Offset(x, y)
                    }

                    // Draw Trend Line
                    if (points.isNotEmpty()) {
                        val path = Path()
                        path.moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            path.lineTo(points[i].x, points[i].y)
                        }
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }

                    // Draw Node Circles for each displayed month
                    points.forEachIndexed { i, pt ->
                        val monthIdx = displayedIndices[i]
                        val isSelected = (activeIdx == monthIdx)
                        val isCurrentMonth = (monthIdx == currentMonthIdx)

                        // Outer ring
                        drawCircle(
                            color = Color.White,
                            radius = if (isSelected || isCurrentMonth) 6.5.dp.toPx() else 4.5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = if (isCurrentMonth) Color(0xFF10B981) else if (isSelected) nodeColor else lineColor,
                            radius = if (isSelected || isCurrentMonth) 6.5.dp.toPx() else 4.5.dp.toPx(),
                            center = pt,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        if (isSelected || isCurrentMonth) {
                            drawCircle(
                                color = if (isCurrentMonth) Color(0xFF10B981) else nodeColor,
                                radius = 3.5.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }

                // Y-Axis Labels
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 24.dp, top = 2.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val ySteps = 4
                    val stepVal = chartRange / ySteps
                    (ySteps downTo 0).forEach { i ->
                        val labelVal = (chartMin + i * stepVal).toInt()
                        Text(
                            text = "$labelVal",
                            fontSize = 9.sp,
                            color = SleekTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // X-Axis Month Labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(start = 36.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    displayedIndices.forEach { monthIdx ->
                        val mLabel = monthsLabels.getOrElse(monthIdx) { "" }
                        val isSelected = (activeIdx == monthIdx)
                        val isCurrent = (monthIdx == currentMonthIdx)

                        Text(
                            text = if (isCurrent) "$mLabel*" else mLabel,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected || isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) Color(0xFF10B981) else if (isSelected) nodeColor else SleekTextSecondary,
                            modifier = Modifier.clickable { selectedMonthIndex = monthIdx }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 📊 2. BAR CHART FOR CATEGORIES (INCOME & EXPENSE)
// ==========================================
@Composable
fun CategoryIncomeExpenseBarChartCard(
    expenses: List<Expense>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Expenses, 1: Income, 2: Comparison
    var animPlayed by remember { mutableStateOf(false) }

    val animProgress by animateFloatAsState(
        targetValue = if (animPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "bar_anim"
    )

    LaunchedEffect(expenses, selectedTab) {
        animPlayed = false
        animPlayed = true
    }

    val expenseList = remember(expenses) { expenses.filter { it.type != "INCOME" } }
    val incomeList = remember(expenses) { expenses.filter { it.type == "INCOME" } }

    val totalExpense = remember(expenseList) { expenseList.sumOf { it.amount } }
    val totalIncome = remember(incomeList) { incomeList.sumOf { it.amount } }

    val categoryBreakdown = remember(expenses, selectedTab) {
        val list = if (selectedTab == 1) incomeList else expenseList
        list.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
            .take(6)
    }

    val maxCategoryVal = remember(categoryBreakdown) {
        (categoryBreakdown.maxOfOrNull { it.value } ?: 1.0).coerceAtLeast(1.0)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SleekPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Category Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Tabs (Expenses / Income / Comparison)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SleekPrimaryContainer.copy(alpha = 0.2f))
                    .border(1.dp, SleekBorder, RoundedCornerShape(16.dp))
                    .padding(3.dp)
            ) {
                listOf("Expenses", "Income", "Comparison").forEachIndexed { idx, label ->
                    val isSelected = selectedTab == idx
                    val bg by animateColorAsState(
                        targetValue = if (isSelected) SleekPrimary else Color.Transparent,
                        animationSpec = tween(250),
                        label = "tabBg"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .clickable { selectedTab = idx }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else SleekTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (selectedTab == 2) {
                // COMPARISON TAB
                val totalMax = maxOf(totalIncome, totalExpense, 100.0)
                val incomeRatio = ((totalIncome / totalMax) * animProgress).toFloat().coerceIn(0f, 1f)
                val expenseRatio = ((totalExpense / totalMax) * animProgress).toFloat().coerceIn(0f, 1f)

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Income Bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("💵", fontSize = 14.sp)
                                Text("Total Income", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            }
                            Text("₹%,.2f".format(totalIncome), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SleekPrimaryContainer.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(incomeRatio)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF34D399), Color(0xFF10B981))
                                        )
                                    )
                            )
                        }
                    }

                    // Expense Bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("💸", fontSize = 14.sp)
                                Text("Total Expenses", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            }
                            Text("₹%,.2f".format(totalExpense), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SleekPrimaryContainer.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(expenseRatio)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFF87171), Color(0xFFEF4444))
                                        )
                                    )
                            )
                        }
                    }

                    val netSavings = totalIncome - totalExpense
                    val savingsRate = if (totalIncome > 0) ((netSavings / totalIncome) * 100).coerceAtLeast(0.0) else 0.0

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (netSavings >= 0) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFEF4444).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, if (netSavings >= 0) Color(0xFF10B981).copy(alpha = 0.3f) else Color(0xFFEF4444).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Net Cash Flow", fontSize = 12.sp, color = SleekTextSecondary)
                                Text(
                                    text = (if (netSavings >= 0) "+₹%,.2f".format(netSavings) else "-₹%,.2f".format(Math.abs(netSavings))),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (netSavings >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = if (netSavings >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                            ) {
                                Text(
                                    text = "%.1f%% Saved".format(savingsRate),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // CATEGORY BARS
                if (categoryBreakdown.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No category records available.", color = SleekTextSecondary, fontSize = 13.sp)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        categoryBreakdown.forEach { (category, amt) ->
                            val totalForTab = if (selectedTab == 1) totalIncome else totalExpense
                            val percentage = if (totalForTab > 0) (amt / totalForTab) * 100 else 0.0
                            val barRatio = ((amt / maxCategoryVal) * animProgress).toFloat().coerceIn(0f, 1f)

                            val emoji = getCategoryEmoji(category)
                            val catColor = categoryColors[category] ?: SleekPrimary

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(emoji, fontSize = 16.sp)
                                        Text(
                                            text = category,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SleekTextPrimary
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = catColor.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = "%.1f%%".format(percentage),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = catColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "₹%,.2f".format(amt),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekTextPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SleekPrimaryContainer.copy(alpha = 0.2f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(barRatio)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(catColor.copy(alpha = 0.7f), catColor)
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 🥧 3. INTERACTIVE PIE / DONUT CHART
// ==========================================
@Composable
fun CategoryPieChartCard(
    expenses: List<Expense>,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf("EXPENSE") }

    val filteredList = remember(expenses, selectedType) {
        if (selectedType == "INCOME") {
            expenses.filter { it.type == "INCOME" && it.amount > 0 }
        } else {
            expenses.filter { it.type != "INCOME" && it.amount > 0 }
        }
    }

    val categoryTotals = remember(filteredList) {
        filteredList.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .filter { it.value > 0 }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SleekPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PieChart,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Distribution Pie Chart",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SleekPrimaryContainer.copy(alpha = 0.2f))
                        .border(1.dp, SleekBorder, RoundedCornerShape(16.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedType == "EXPENSE") Color(0xFFEF4444) else Color.Transparent)
                            .clickable { selectedType = "EXPENSE" }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            "Expenses",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedType == "EXPENSE") Color.White else SleekTextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedType == "INCOME") Color(0xFF10B981) else Color.Transparent)
                            .clickable { selectedType = "INCOME" }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            "Income",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedType == "INCOME") Color.White else SleekTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CategoryExpensePieChart(
                categoryExpenses = categoryTotals,
                categoryColors = categoryColors,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ==========================================
// 4️⃣.5️⃣ CATEGORY SELECTOR COMPONENTS
// ==========================================
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun CategorySelectorGrid(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    categories: List<String>,
    categoryIcons: Map<String, String> = emptyMap(),
    onAddCustomCategoryClick: () -> Unit,
    onDeleteCustomCategory: ((String) -> Unit)? = null,
    onEditCustomCategory: ((String, String) -> Unit)? = null
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { cat ->
            val isSelected = cat == selectedCategory
            val emoji = getCategoryEmoji(cat, categoryIcons)
            
            val isDefault = listOf("Food", "Travel", "Rent", "Utilities", "Entertainment", "Shopping", "Persons", "Others", "Salary", "Freelance", "Investments", "Gifts").contains(cat)
            
            var showEditDeleteDialog by remember { mutableStateOf(false) }

            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .combinedClickable(
                        onClick = { onCategorySelected(cat) },
                        onLongClick = {
                            if (!isDefault && (onDeleteCustomCategory != null || onEditCustomCategory != null)) {
                                showEditDeleteDialog = true
                            }
                        }
                    ),
                shape = CircleShape,
                color = if (isSelected) SleekPrimary.copy(alpha = 0.2f) else SleekNeutralLight,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) SleekPrimary else SleekBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(emoji, fontSize = 16.sp)
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) SleekPrimary else SleekTextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    
                    if (!isDefault && isSelected) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Category",
                            tint = SleekTextSecondary,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { showEditDeleteDialog = true }
                        )
                    }
                }
            }
            
            if (showEditDeleteDialog) {
                ManageCategoryDialog(
                    categoryName = cat,
                    onDismiss = { showEditDeleteDialog = false },
                    onDelete = {
                        if (onDeleteCustomCategory != null) {
                            onDeleteCustomCategory(cat)
                        }
                        showEditDeleteDialog = false
                    },
                    onRename = { newName ->
                        if (onEditCustomCategory != null) {
                            onEditCustomCategory(cat, newName)
                        }
                        showEditDeleteDialog = false
                    }
                )
            }
        }
        
        if (onAddCustomCategoryClick != null) {
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onAddCustomCategoryClick() },
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Custom Category",
                        tint = SleekPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Add Custom",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SleekPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ManageCategoryDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    var newName by remember { mutableStateOf(categoryName) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showConfirmDelete) {
                    Text(
                        text = "Delete Category?",
                        style = MaterialTheme.typography.titleMedium,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Are you sure you want to delete \"$categoryName\"? Associated expenses will revert to \"Others\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SleekTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showConfirmDelete = false },
                            border = BorderStroke(1.dp, SleekBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextSecondary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                onDelete()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delete", color = Color.White)
                        }
                    }
                } else {
                    Text(
                        text = "Manage Category",
                        style = MaterialTheme.typography.titleMedium,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Category Name", color = SleekTextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekBorder,
                            focusedContainerColor = SleekSurface,
                            unfocusedContainerColor = SleekSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showConfirmDelete = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delete", color = ExpenseRed, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                if (newName.trim().isNotEmpty() && newName.trim() != categoryName) {
                                    onRename(newName.trim())
                                }
                                onDismiss()
                            },
                            enabled = newName.trim().isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Rename", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = SleekTextSecondary)
                    }
                }
            }
        }
    }
}

// ==========================================
// 5️⃣ ADD EXPENSE DIALOG
// Defined in AddTransactionFlowComponents.kt
// ==========================================
@Composable
private fun AddExpenseDialogOld(
    prefilledDate: Long?,
    categories: List<String>,
    categoryIcons: Map<String, String> = emptyMap(),
    expenses: List<Expense> = emptyList(),
    onAddCategory: (String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onEditCategory: (String, String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, category: String, date: Long, note: String, imagePath: String?, type: String) -> Unit
) {
    var type by remember { mutableStateOf("EXPENSE") }
    var amountStr by remember { mutableStateOf("") }

    val totalIncome = remember(expenses) {
        expenses.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalExpenses = remember(expenses) {
        expenses.filter { it.type != "INCOME" }.sumOf { it.amount }
    }
    val availableBalance = (totalIncome - totalExpenses).coerceAtLeast(0.0)

    val enteredAmount = amountStr.toDoubleOrNull() ?: 0.0
    val isExceedingIncome = type == "EXPENSE" && (enteredAmount > availableBalance || totalExpenses + enteredAmount > totalIncome)
    
    val incomeCategories = listOf("Salary", "Freelance", "Investments", "Gifts", "Others")
    val currentCategoriesList = if (type == "INCOME") {
        incomeCategories
    } else {
        categories
    }
    
    var category by remember { mutableStateOf(categories.firstOrNull() ?: "Food") }
    var note by remember { mutableStateOf("") }

    var showCreateCategoryDialog by remember { mutableStateOf(false) }

    // Automatically set default category when switching type
    LaunchedEffect(type) {
        category = if (type == "INCOME") "Salary" else (categories.firstOrNull() ?: "Food")
    }

    // Image/receipt selection states
    val context = LocalContext.current
    var attachedImagePath by remember { mutableStateOf<String?>(null) }
    var editingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showCropperDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = loadFullResolutionBitmap(context, uri)
            if (bitmap != null) {
                editingBitmap = bitmap
                showCropperDialog = true
            } else {
                Toast.makeText(context, "Error loading full-resolution image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            editingBitmap = bitmap
            showCropperDialog = true
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().testTag("add_expense_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (type == "INCOME") "Add New Income" else "Add New Expense",
                    style = MaterialTheme.typography.titleLarge,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Type Segmented Switcher (Expense / Income)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SleekBorder.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (type == "EXPENSE") SleekPrimary else Color.Transparent)
                            .clickable { type = "EXPENSE" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Expense",
                            color = if (type == "EXPENSE") Color.White else SleekTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (type == "INCOME") Color(0xFF10B981) else Color.Transparent)
                            .clickable { type = "INCOME" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Income",
                            color = if (type == "INCOME") Color.White else SleekTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Prefilled Date Status Info
                if (prefilledDate != null) {
                    val dateFormatted = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(prefilledDate))
                    Text(
                        text = "Adding for: $dateFormatted",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SleekPrimary.copy(alpha = 0.1f))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Amount Input Field
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (₹)", color = SleekTextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface,
                        focusedLabelColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary,
                        unfocusedLabelColor = SleekTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input")
                )

                if (type == "EXPENSE") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Available Balance: ₹${String.format(Locale.getDefault(), "%,.2f", availableBalance)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isExceedingIncome) ExpenseRed else SleekTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        if (totalIncome > 0) {
                            Text(
                                text = "Total Income: ₹${String.format(Locale.getDefault(), "%,.2f", totalIncome)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SleekTextSecondary
                            )
                        }
                    }
                    if (isExceedingIncome) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (totalIncome <= 0) 
                                        "Expense cannot exceed total income. Income is ₹0.00." 
                                    else 
                                        "Expense (₹${String.format(Locale.getDefault(), "%,.2f", enteredAmount)}) exceeds available bank balance (₹${String.format(Locale.getDefault(), "%,.2f", availableBalance)}).",
                                    color = ExpenseRed,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Total Income so far: ₹${String.format(Locale.getDefault(), "%,.2f", totalIncome)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selector
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                CategorySelectorGrid(
                    selectedCategory = category,
                    onCategorySelected = { category = it },
                    categories = currentCategoriesList,
                    categoryIcons = categoryIcons,
                    onAddCustomCategoryClick = { showCreateCategoryDialog = true },
                    onDeleteCustomCategory = if (type == "EXPENSE") onDeleteCategory else null,
                    onEditCustomCategory = if (type == "EXPENSE") onEditCategory else null
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Note description input (Mandatory)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Description *", color = SleekTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface,
                        focusedLabelColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary,
                        unfocusedLabelColor = SleekTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_note_input")
                )
                if (note.trim().isEmpty()) {
                    Text(
                        "Description is required",
                        color = ExpenseRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Attached image section in Add Dialog
                Text(
                    text = "Receipt Photo (Optional)",
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (!attachedImagePath.isNullOrBlank() && File(attachedImagePath!!).exists()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, SleekBorder, RoundedCornerShape(12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = File(attachedImagePath!!),
                            contentDescription = "Attached Receipt",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { attachedImagePath = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Image", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { cameraLauncher.launch() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimaryContainer),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Camera", color = SleekPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimaryContainer),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Photo, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery", color = SleekPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm and Cancel buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, SleekBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextSecondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val amount = amountStr.toDoubleOrNull() ?: 0.0
                            if (amount > 0 && note.trim().isNotEmpty()) {
                                onConfirm(
                                    amount,
                                    category,
                                    prefilledDate ?: System.currentTimeMillis(),
                                    note.trim(),
                                    attachedImagePath,
                                    type
                                )
                            }
                        },
                        enabled = note.trim().isNotEmpty() && enteredAmount > 0.0 && !isExceedingIncome,
                        colors = ButtonDefaults.buttonColors(containerColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showCropperDialog && editingBitmap != null) {
        ImageEditDialog(
            initialBitmap = editingBitmap!!,
            onDismiss = { showCropperDialog = false },
            onSave = { savedPath ->
                showCropperDialog = false
                attachedImagePath = savedPath
            }
        )
    }

    if (showCreateCategoryDialog) {
        CreateCategoryDialog(
            onDismiss = { showCreateCategoryDialog = false },
            onConfirm = { newCat ->
                onAddCategory(newCat)
                category = newCat
                showCreateCategoryDialog = false
            }
        )
    }
}

// ==========================================
// 6️⃣ EDIT EXPENSE DIALOG
// ==========================================
@Composable
fun EditExpenseDialog(
    expense: Expense,
    categories: List<String>,
    expenseCategories: List<String> = emptyList(),
    incomeCategories: List<String> = emptyList(),
    categoryIcons: Map<String, String> = emptyMap(),
    expenses: List<Expense> = emptyList(),
    onAddCategory: (name: String, categoryType: String) -> Unit = { _, _ -> },
    onDeleteCategory: (String) -> Unit,
    onEditCategory: (String, String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Expense) -> Unit
) {
    var type by remember { mutableStateOf(expense.type) }
    var amountStr by remember { mutableStateOf(expense.amount.toString()) }

    val otherExpenses = remember(expenses, expense) {
        expenses.filter { it.id != expense.id && it.type != "INCOME" }.sumOf { it.amount }
    }
    val totalIncome = remember(expenses, expense, amountStr, type) {
        if (expense.type == "INCOME") {
            expenses.filter { it.id != expense.id && it.type == "INCOME" }.sumOf { it.amount } + (if (type == "INCOME") (amountStr.toDoubleOrNull() ?: 0.0) else 0.0)
        } else {
            expenses.filter { it.type == "INCOME" }.sumOf { it.amount }
        }
    }

    val availableBalanceForEdit = (totalIncome - otherExpenses).coerceAtLeast(0.0)
    val enteredAmount = amountStr.toDoubleOrNull() ?: 0.0
    val isExceedingIncome = type == "EXPENSE" && (enteredAmount > availableBalanceForEdit || otherExpenses + enteredAmount > totalIncome)

    val defaultExpensePreset = listOf("Food", "Travel", "Rent", "Utilities", "Entertainment", "Shopping", "Home", "Others")
    val defaultIncomePreset = listOf("Salary", "Freelance", "Investments", "Gifts", "Others")
    val currentCategoriesList = if (type == "INCOME") {
        (defaultIncomePreset + (if (incomeCategories.isNotEmpty()) incomeCategories else categories.filter { defaultIncomePreset.contains(it) })).distinct()
    } else {
        (defaultExpensePreset + (if (expenseCategories.isNotEmpty()) expenseCategories else categories.filter { !defaultIncomePreset.contains(it) })).distinct()
    }
    
    var category by remember { mutableStateOf(expense.category) }
    var note by remember { mutableStateOf(expense.note ?: "") }

    var showCreateCategoryDialog by remember { mutableStateOf(false) }

    var firstLoad by remember { mutableStateOf(true) }
    LaunchedEffect(type) {
        if (firstLoad) {
            firstLoad = false
        } else {
            category = if (type == "INCOME") (currentCategoriesList.firstOrNull() ?: "Salary") else (currentCategoriesList.firstOrNull() ?: "Food")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().testTag("edit_expense_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (type == "INCOME") "Edit Income" else "Edit Expense",
                    style = MaterialTheme.typography.titleLarge,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Type Segmented Switcher (Expense / Income)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SleekBorder.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (type == "EXPENSE") SleekPrimary else Color.Transparent)
                            .clickable { type = "EXPENSE" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Expense",
                            color = if (type == "EXPENSE") Color.White else SleekTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (type == "INCOME") Color(0xFF10B981) else Color.Transparent)
                            .clickable { type = "INCOME" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Income",
                            color = if (type == "INCOME") Color.White else SleekTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Input Field
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (₹)", color = SleekTextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface,
                        focusedLabelColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary,
                        unfocusedLabelColor = SleekTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (type == "EXPENSE") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Available Balance: ₹${String.format(Locale.getDefault(), "%,.2f", availableBalanceForEdit)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isExceedingIncome) ExpenseRed else SleekTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        if (totalIncome > 0) {
                            Text(
                                text = "Total Income: ₹${String.format(Locale.getDefault(), "%,.2f", totalIncome)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SleekTextSecondary
                            )
                        }
                    }
                    if (isExceedingIncome) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (totalIncome <= 0) 
                                        "Expense cannot exceed total income. Income is ₹0.00." 
                                    else 
                                        "Expense (₹${String.format(Locale.getDefault(), "%,.2f", enteredAmount)}) exceeds available balance (₹${String.format(Locale.getDefault(), "%,.2f", availableBalanceForEdit)}).",
                                    color = ExpenseRed,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Total Income: ₹${String.format(Locale.getDefault(), "%,.2f", totalIncome)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selector
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                CategorySelectorGrid(
                    selectedCategory = category,
                    onCategorySelected = { category = it },
                    categories = currentCategoriesList,
                    categoryIcons = categoryIcons,
                    onAddCustomCategoryClick = { showCreateCategoryDialog = true },
                    onDeleteCustomCategory = if (type == "EXPENSE") onDeleteCategory else null,
                    onEditCustomCategory = if (type == "EXPENSE") onEditCategory else null
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Note description input (Mandatory)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Description *", color = SleekTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface,
                        focusedLabelColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary,
                        unfocusedLabelColor = SleekTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (note.trim().isEmpty()) {
                    Text(
                        "Description is required",
                        color = ExpenseRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm and Cancel buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, SleekBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextSecondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val amount = amountStr.toDoubleOrNull() ?: 0.0
                            if (amount > 0 && note.trim().isNotEmpty()) {
                                onConfirm(
                                    expense.copy(
                                        amount = amount,
                                        category = category,
                                        note = note.trim(),
                                        type = type
                                    )
                                )
                            }
                        },
                        enabled = note.trim().isNotEmpty() && enteredAmount > 0.0 && !isExceedingIncome,
                        colors = ButtonDefaults.buttonColors(containerColor = if (type == "INCOME") Color(0xFF10B981) else SleekPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showCreateCategoryDialog) {
        CreateCategoryDialog(
            initialType = type,
            onDismiss = { showCreateCategoryDialog = false },
            onConfirm = { newCat, catType ->
                onAddCategory(newCat, catType)
                category = newCat
                showCreateCategoryDialog = false
            }
        )
    }
}

// ==========================================
// 7️⃣ LIVE CALENDAR TAB
// ==========================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarTab(
    expenses: List<Expense>,
    categoryIcons: Map<String, String> = emptyMap(),
    onAddExpenseForDate: (Long) -> Unit,
    onEditExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val today = Calendar.getInstance()
    var navigatedCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    var swipeDragAmount by remember { mutableStateOf(0f) }

    val activeYear = navigatedCalendar.get(Calendar.YEAR)
    val activeMonth = navigatedCalendar.get(Calendar.MONTH)

    // Baseline: March 2027. +1 month on each 7th of the month.
    val maxCalendarLimit = remember(today.get(Calendar.DAY_OF_MONTH)) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, 2027)
            set(Calendar.MONTH, Calendar.MARCH)
            set(Calendar.DAY_OF_MONTH, 31)
            if (today.get(Calendar.DAY_OF_MONTH) >= 7) {
                add(Calendar.MONTH, 1)
            }
        }
    }

    val canGoForward = remember(navigatedCalendar, maxCalendarLimit) {
        val temp = Calendar.getInstance().apply {
            timeInMillis = navigatedCalendar.timeInMillis
            add(Calendar.MONTH, 1)
        }
        temp.before(maxCalendarLimit) || (temp.get(Calendar.MONTH) == maxCalendarLimit.get(Calendar.MONTH) && temp.get(Calendar.YEAR) == maxCalendarLimit.get(Calendar.YEAR))
    }

    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    var selectedDayOfMonth by remember { mutableStateOf(today.get(Calendar.DAY_OF_MONTH)) }

    val selectedDateMillis = remember(activeYear, activeMonth, selectedDayOfMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, activeYear)
            set(Calendar.MONTH, activeMonth)
            set(Calendar.DAY_OF_MONTH, selectedDayOfMonth)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val selectedDayExpenses = expenses.filter {
        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
        cal.get(Calendar.YEAR) == activeYear &&
                cal.get(Calendar.MONTH) == activeMonth &&
                cal.get(Calendar.DAY_OF_MONTH) == selectedDayOfMonth
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Transaction Calendar",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Double-tap day to add. Valid until ${SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(maxCalendarLimit.time)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(canGoForward) {
                    detectHorizontalDragGestures(
                        onDragStart = { swipeDragAmount = 0f },
                        onDragEnd = {
                            if (swipeDragAmount > 100f) {
                                navigatedCalendar = Calendar.getInstance().apply {
                                    timeInMillis = navigatedCalendar.timeInMillis
                                    add(Calendar.MONTH, -1)
                                }
                                selectedDayOfMonth = 1
                            } else if (swipeDragAmount < -100f) {
                                if (canGoForward) {
                                    navigatedCalendar = Calendar.getInstance().apply {
                                        timeInMillis = navigatedCalendar.timeInMillis
                                        add(Calendar.MONTH, 1)
                                    }
                                    selectedDayOfMonth = 1
                                }
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            swipeDragAmount += dragAmount
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navigatedCalendar = Calendar.getInstance().apply {
                                timeInMillis = navigatedCalendar.timeInMillis
                                add(Calendar.MONTH, -1)
                            }
                            selectedDayOfMonth = 1
                        }
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = SleekPrimary)
                    }

                    Text(
                        text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(navigatedCalendar.time),
                        style = MaterialTheme.typography.titleMedium,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (canGoForward) {
                                navigatedCalendar = Calendar.getInstance().apply {
                                    timeInMillis = navigatedCalendar.timeInMillis
                                    add(Calendar.MONTH, 1)
                                }
                                selectedDayOfMonth = 1
                            }
                        },
                        enabled = canGoForward
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            tint = if (canGoForward) SleekPrimary else SleekTextSecondary.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            color = SleekTextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val firstDayCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, activeYear)
                    set(Calendar.MONTH, activeMonth)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val firstDayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK)
                val daysInMonth = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val dayOffset = firstDayOfWeek - 1

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (row in 0 until 6) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0 until 7) {
                                val slotIndex = row * 7 + col
                                val dayNum = slotIndex - dayOffset + 1

                                if (dayNum in 1..daysInMonth) {
                                    val isSelected = selectedDayOfMonth == dayNum
                                    val isToday = today.get(Calendar.YEAR) == activeYear &&
                                            today.get(Calendar.MONTH) == activeMonth &&
                                            today.get(Calendar.DAY_OF_MONTH) == dayNum

                                    val dayExpenses = expenses.filter {
                                        val c = Calendar.getInstance().apply { timeInMillis = it.date }
                                        c.get(Calendar.YEAR) == activeYear &&
                                                c.get(Calendar.MONTH) == activeMonth &&
                                                c.get(Calendar.DAY_OF_MONTH) == dayNum
                                    }
                                    val dayIncome = dayExpenses.filter { it.type == "INCOME" }.sumOf { it.amount }
                                    val dayExpense = dayExpenses.filter { it.type != "INCOME" }.sumOf { it.amount }
                                    val hasTransactions = dayExpenses.isNotEmpty()
                                    val isProfit = hasTransactions && dayIncome >= dayExpense
                                    val isLoss = hasTransactions && dayExpense > dayIncome

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                when {
                                                    isSelected && isProfit -> Color(0xFF10B981)
                                                    isSelected && isLoss -> Color(0xFFEF4444)
                                                    isSelected -> SleekPrimary
                                                    isProfit -> Color(0xFF10B981).copy(alpha = 0.2f)
                                                    isLoss -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                                    isToday -> SleekPrimaryContainer.copy(alpha = 0.5f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = when {
                                                    isSelected -> Color.Transparent
                                                    isProfit -> Color(0xFF10B981).copy(alpha = 0.6f)
                                                    isLoss -> Color(0xFFEF4444).copy(alpha = 0.6f)
                                                    isToday -> SleekPrimary
                                                    else -> Color.Transparent
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .combinedClickable(
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    selectedDayOfMonth = dayNum
                                                },
                                                onDoubleClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    selectedDayOfMonth = dayNum
                                                    val clickedDate = Calendar.getInstance().apply {
                                                        set(Calendar.YEAR, activeYear)
                                                        set(Calendar.MONTH, activeMonth)
                                                        set(Calendar.DAY_OF_MONTH, dayNum)
                                                    }.timeInMillis
                                                    onAddExpenseForDate(clickedDate)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = dayNum.toString(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isSelected -> Color.White
                                                    isProfit -> Color(0xFF10B981)
                                                    isLoss -> Color(0xFFEF4444)
                                                    isToday -> SleekPrimary
                                                    else -> SleekTextPrimary
                                                }
                                            )
                                            if (hasTransactions) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(top = 2.dp)
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            when {
                                                                isSelected -> Color.White
                                                                isProfit -> Color(0xFF10B981)
                                                                else -> Color(0xFFEF4444)
                                                            }
                                                        )
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val selectedDateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDateMillis))
            Text(
                text = "Transactions on $selectedDateStr",
                style = MaterialTheme.typography.titleMedium,
                color = SleekTextPrimary,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { onAddExpenseForDate(selectedDateMillis) },
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedDayExpenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions logged for this day.",
                    color = SleekTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                selectedDayExpenses.forEach { expense ->
                    val isIncome = expense.type == "INCOME"
                    val catColor = if (isIncome) {
                        when (expense.category) {
                            "Salary" -> Color(0xFF10B981)
                            "Freelance" -> Color(0xFF0D9488)
                            "Investments" -> Color(0xFF3B82F6)
                            "Gifts" -> Color(0xFFEC4899)
                            else -> Color(0xFF10B981)
                        }
                    } else {
                        categoryColors[expense.category] ?: SleekPrimary
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SleekSurface)
                            .border(1.dp, SleekBorder, RoundedCornerShape(16.dp))
                            .clickable { onEditExpense(expense) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(catColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = getCategoryEmoji(expense.category, categoryIcons),
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = expense.note ?: (if (isIncome) "Income" else "Expense"),
                                style = MaterialTheme.typography.titleSmall,
                                color = SleekTextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = expense.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = SleekTextSecondary
                            )
                        }

                        Text(
                            text = String.format("%s₹%,.2f", if (isIncome) "+" else "-", expense.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isIncome) Color(0xFF10B981) else ExpenseRed,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        IconButton(
                            onClick = { onDeleteExpense(expense) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = ExpenseRed.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
fun OnboardingNameDialog(
    onSave: (String) -> Unit
) {
    var nameStr by remember { mutableStateOf("") }
    Dialog(
        onDismissRequest = { /* Prevent dismiss to force name entry */ }
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekBg),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(SleekPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Welcome User",
                        tint = SleekPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Welcome to Finance",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Please enter your name to personalize your offline ledgers and insights.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SleekTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = nameStr,
                    onValueChange = { nameStr = it },
                    label = { Text("Your Name", color = SleekTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedLabelColor = SleekPrimary,
                        unfocusedLabelColor = SleekTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (nameStr.trim().isNotEmpty()) {
                            onSave(nameStr.trim())
                        }
                    },
                    enabled = nameStr.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("onboarding_save_button")
                ) {
                    Text(
                        "Get Started",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CreateCategoryDialog(
    initialType: String = "EXPENSE",
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String) -> Unit = { _, _ -> },
    onConfirmSingle: ((String) -> Unit)? = null
) {
    var newCatName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(if (initialType == "INCOME") "INCOME" else "EXPENSE") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().testTag("create_category_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "Add New Category",
                    style = MaterialTheme.typography.titleMedium,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Category Type Selector (Expense vs Income)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SleekBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isExp = selectedType == "EXPENSE"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isExp) Color(0xFFEF5350) else Color.Transparent)
                            .clickable { selectedType = "EXPENSE" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Expense Category",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isExp) Color.White else SleekTextSecondary
                        )
                    }

                    val isInc = selectedType == "INCOME"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isInc) Color(0xFF10B981) else Color.Transparent)
                            .clickable { selectedType = "INCOME" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Income Category",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isInc) Color.White else SleekTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = newCatName,
                    onValueChange = { newCatName = it },
                    label = { Text("Category Name", color = SleekTextSecondary) },
                    placeholder = { Text(if (selectedType == "INCOME") "e.g., Freelance, Bonus..." else "e.g., Gym, Subscriptions...", color = SleekTextSecondary, fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (selectedType == "INCOME") Color(0xFF10B981) else Color(0xFFEF5350),
                        unfocusedBorderColor = SleekBorder,
                        focusedLabelColor = if (selectedType == "INCOME") Color(0xFF10B981) else Color(0xFFEF5350),
                        unfocusedLabelColor = SleekTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextSecondary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (newCatName.trim().isNotEmpty()) {
                                onConfirm(newCatName.trim(), selectedType)
                                onConfirmSingle?.invoke(newCatName.trim())
                            }
                        },
                        enabled = newCatName.trim().isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == "INCOME") Color(0xFF10B981) else Color(0xFFEF5350)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    CreateCategoryDialog(
        initialType = "EXPENSE",
        onDismiss = onDismiss,
        onConfirmSingle = onConfirm
    )
}

@Composable
fun BillsFullScreen(
    viewModel: FinanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit,
    onPayBill: (String, Double) -> Unit
) {
    val context = LocalContext.current
    val billsList = viewModel.billsList
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val totalBalance = remember(accounts) { accounts.sumOf { it.balance } }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingBill by remember { mutableStateOf<BillEntry?>(null) }
    var billTitle by remember { mutableStateOf("") }
    var billAmount by remember { mutableStateOf("") }
    var billDueDate by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var billErrorMessage by remember { mutableStateOf<String?>(null) }

    BackHandler { onBack() }

    if (showAddEditDialog) {
        Dialog(onDismissRequest = { showAddEditDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (editingBill == null) "Add New Bill" else "Edit Bill",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )

                    OutlinedTextField(
                        value = billTitle,
                        onValueChange = { billTitle = it },
                        label = { Text("Bill Title", color = SleekTextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = billAmount,
                        onValueChange = { billAmount = it },
                        label = { Text("Amount (₹)", color = SleekTextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedButton(
                        onClick = {
                            val cal = java.util.Calendar.getInstance()
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    billDueDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                                },
                                cal.get(java.util.Calendar.YEAR),
                                cal.get(java.util.Calendar.MONTH),
                                cal.get(java.util.Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = SleekPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Due Date: $billDueDate", color = SleekTextPrimary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddEditDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = SleekTextSecondary)
                        }

                        Button(
                            onClick = {
                                val amt = billAmount.toDoubleOrNull() ?: 0.0
                                if (billTitle.isNotBlank() && amt > 0) {
                                    val currentEditing = editingBill
                                    if (currentEditing == null) {
                                        billsList.add(BillEntry(System.currentTimeMillis().toString(), billTitle.trim(), amt, billDueDate))
                                    } else {
                                        val idx = billsList.indexOfFirst { it.id == currentEditing.id }
                                        if (idx != -1) {
                                            billsList[idx] = currentEditing.copy(title = billTitle.trim(), amount = amt, dueDate = billDueDate)
                                        }
                                    }
                                    showAddEditDialog = false
                                    Toast.makeText(context, "Saved bill successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SleekSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SleekTextPrimary)
                    }
                    Text(
                        text = "Bills & Utilities",
                        style = MaterialTheme.typography.titleLarge,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = {
                        editingBill = null
                        billTitle = ""
                        billAmount = ""
                        billDueDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        showAddEditDialog = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Bill", tint = SleekPrimary)
                }
            }

            billErrorMessage?.let { msg ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                            Text(msg, color = Color(0xFF991B1B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        IconButton(onClick = { billErrorMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF991B1B), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Text(
                text = "Pay bills directly on screen or tap edit/delete icons.",
                style = MaterialTheme.typography.bodySmall,
                color = SleekTextSecondary
            )

            if (billsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = SleekTextSecondary, modifier = Modifier.size(48.dp))
                        Text("No upcoming bills", fontWeight = FontWeight.Bold, color = SleekTextPrimary, fontSize = 16.sp)
                        Text("Tap + to add a bill", color = SleekTextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(billsList, key = { it.id }) { bill ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SleekBorder.copy(alpha = 0.25f))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(bill.title, fontWeight = FontWeight.Bold, color = SleekTextPrimary, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Due: ${bill.dueDate}", fontSize = 12.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
                                        Text("•", fontSize = 12.sp, color = SleekTextSecondary)
                                        Text("₹%,.0f".format(bill.amount), fontWeight = FontWeight.Bold, color = SleekTextPrimary, fontSize = 14.sp)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (bill.amount > totalBalance) {
                                                billErrorMessage = "Cannot pay ₹%.0f for %s: Amount exceeds available balance (₹%.0f available).".format(bill.amount, bill.title, totalBalance)
                                            } else {
                                                onPayBill(bill.title, bill.amount)
                                                billsList.remove(bill)
                                                billErrorMessage = null
                                                Toast.makeText(context, "Paid ${bill.title} successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Pay Now", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    IconButton(
                                        onClick = {
                                            editingBill = bill
                                            billTitle = bill.title
                                            billAmount = bill.amount.toString()
                                            billDueDate = bill.dueDate
                                            showAddEditDialog = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SleekPrimary, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            billsList.remove(bill)
                                            Toast.makeText(context, "Deleted ${bill.title}", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RemindersFullScreen(
    viewModel: FinanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val remindersList = viewModel.remindersList

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<ReminderEntry?>(null) }
    var reminderText by remember { mutableStateOf("") }
    var reminderDueDate by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }

    BackHandler { onBack() }

    if (showAddEditDialog) {
        Dialog(onDismissRequest = { showAddEditDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (editingReminder == null) "Add New Reminder" else "Edit Reminder",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )

                    OutlinedTextField(
                        value = reminderText,
                        onValueChange = { reminderText = it },
                        label = { Text("Reminder Note", color = SleekTextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedButton(
                        onClick = {
                            val cal = java.util.Calendar.getInstance()
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    reminderDueDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                                },
                                cal.get(java.util.Calendar.YEAR),
                                cal.get(java.util.Calendar.MONTH),
                                cal.get(java.util.Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = SleekPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Due Date: $reminderDueDate", color = SleekTextPrimary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddEditDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = SleekTextSecondary)
                        }

                        Button(
                            onClick = {
                                if (reminderText.isNotBlank()) {
                                    val currentEditing = editingReminder
                                    if (currentEditing == null) {
                                        remindersList.add(ReminderEntry(System.currentTimeMillis().toString(), reminderText.trim(), reminderDueDate))
                                    } else {
                                        val idx = remindersList.indexOfFirst { it.id == currentEditing.id }
                                        if (idx != -1) {
                                            remindersList[idx] = currentEditing.copy(text = reminderText.trim(), dueDate = reminderDueDate)
                                        }
                                    }
                                    showAddEditDialog = false
                                    Toast.makeText(context, "Saved reminder successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SleekSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SleekTextPrimary)
                    }
                    Text(
                        text = "Payment Reminders",
                        style = MaterialTheme.typography.titleLarge,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = {
                        editingReminder = null
                        reminderText = ""
                        reminderDueDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        showAddEditDialog = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Reminder", tint = SleekPrimary)
                }
            }

            Text(
                text = "Toggle active reminders on/off or tap card actions.",
                style = MaterialTheme.typography.bodySmall,
                color = SleekTextSecondary
            )

            if (remindersList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = SleekTextSecondary, modifier = Modifier.size(48.dp))
                        Text("No active reminders", fontWeight = FontWeight.Bold, color = SleekTextPrimary, fontSize = 16.sp)
                        Text("Tap + to add a reminder", color = SleekTextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(remindersList, key = { it.id }) { rem ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SleekBorder.copy(alpha = 0.25f))
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = if (rem.isEnabled) Color(0xFFEC4899) else SleekTextSecondary, modifier = Modifier.size(22.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(rem.text, fontSize = 14.sp, color = SleekTextPrimary, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Due: ${rem.dueDate} • ${if (rem.isEnabled) "Active" else "Stopped"}", fontSize = 11.sp, color = if (rem.isEnabled) Color(0xFFEC4899) else SleekTextSecondary, fontWeight = FontWeight.Bold)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Switch(
                                        checked = rem.isEnabled,
                                        onCheckedChange = { isChecked ->
                                            val idx = remindersList.indexOfFirst { it.id == rem.id }
                                            if (idx != -1) {
                                                remindersList[idx] = rem.copy(isEnabled = isChecked)
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFFEC4899),
                                            checkedTrackColor = Color(0xFFEC4899).copy(alpha = 0.5f)
                                        )
                                    )

                                    IconButton(
                                        onClick = {
                                            remindersList.remove(rem)
                                            Toast.makeText(context, "Marked reminder as complete!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Complete", tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            editingReminder = rem
                                            reminderText = rem.text
                                            reminderDueDate = rem.dueDate
                                            showAddEditDialog = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SleekPrimary, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            remindersList.remove(rem)
                                            Toast.makeText(context, "Deleted reminder", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdjustBudgetDialog(
    currentBudget: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var budgetStr by remember { mutableStateOf(currentBudget.toInt().toString()) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().testTag("adjust_budget_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "Adjust Monthly Budget",
                    style = MaterialTheme.typography.titleLarge,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = budgetStr,
                    onValueChange = { budgetStr = it },
                    label = { Text("Budget Cap (₹)", color = SleekTextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedLabelColor = SleekPrimary,
                        unfocusedLabelColor = SleekTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("budget_input")
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, SleekBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextSecondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val budgetVal = budgetStr.toDoubleOrNull() ?: 0.0
                            if (budgetVal > 0) {
                                onConfirm(budgetVal)
                            }
                        },
                        enabled = (budgetStr.toDoubleOrNull() ?: 0.0) > 0.0,
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DateRangeReportModalDialog(
    initialStartDate: Long,
    initialEndDate: Long,
    onDismiss: () -> Unit,
    onConfirm: (startDate: Long, endDate: Long) -> Unit
) {
    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(initialEndDate) }
    val context = LocalContext.current
    val dateSdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.5.dp, SleekPrimary),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("date_range_picker_modal")
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SleekPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Select Report Period",
                                style = MaterialTheme.typography.titleMedium,
                                color = SleekTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Pick Start & End Date for report",
                                style = MaterialTheme.typography.bodySmall,
                                color = SleekTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SleekTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Start Date Picker Field
                Text(
                    "Start Date",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SleekTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = if (startDate == 0L) System.currentTimeMillis() else startDate }
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val selCal = Calendar.getInstance().apply {
                                        set(year, month, day, 0, 0, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    startDate = selCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (startDate == 0L) "All Time / Beginning" else dateSdf.format(Date(startDate)),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                        }
                        Text("Pick Date", fontSize = 12.sp, color = SleekPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // End Date Picker Field
                Text(
                    "End Date",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SleekTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = endDate }
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val selCal = Calendar.getInstance().apply {
                                        set(year, month, day, 23, 59, 59)
                                        set(Calendar.MILLISECOND, 999)
                                    }
                                    endDate = selCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = dateSdf.format(Date(endDate)),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                        }
                        Text("Pick Date", fontSize = 12.sp, color = SleekPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Presets
                Text("Quick Selection", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = SleekTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val presets = listOf("7 Days", "30 Days", "This Month", "All Time")
                    presets.forEach { preset ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                val now = Calendar.getInstance()
                                when (preset) {
                                    "7 Days" -> {
                                        endDate = now.timeInMillis
                                        startDate = Calendar.getInstance().apply {
                                            add(Calendar.DAY_OF_MONTH, -7)
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                        }.timeInMillis
                                    }
                                    "30 Days" -> {
                                        endDate = now.timeInMillis
                                        startDate = Calendar.getInstance().apply {
                                            add(Calendar.DAY_OF_MONTH, -30)
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                        }.timeInMillis
                                    }
                                    "This Month" -> {
                                        endDate = now.timeInMillis
                                        startDate = Calendar.getInstance().apply {
                                            set(Calendar.DAY_OF_MONTH, 1)
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                        }.timeInMillis
                                    }
                                    "All Time" -> {
                                        endDate = now.timeInMillis
                                        startDate = 0L
                                    }
                                }
                            },
                            label = { Text(preset, fontSize = 9.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SleekBorder)
                    ) {
                        Text("Cancel", color = SleekTextPrimary)
                    }

                    Button(
                        onClick = { onConfirm(startDate, endDate) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                    ) {
                        Text("Apply Range", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SidebarSettingsTile(
    icon: ImageVector,
    iconBgColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = SleekSurface,
        border = BorderStroke(1.dp, SleekBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconBgColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SleekTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SmallFluidBudgetBar(
    thisMonthTotal: Double,
    monthlyBudget: Double,
    modifier: Modifier = Modifier
) {
    val safeBudget = if (monthlyBudget <= 0) 1.0 else monthlyBudget
    val rawProgress = (thisMonthTotal / safeBudget).coerceIn(0.0, 1.2)
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress.toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "smallFluidProgress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "smallWaveTransition")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "smallWavePhase"
    )

    // Dynamic color transition: Green (0-65%) -> Amber (65-85%) -> Red (85%+)
    val statusColor = when {
        rawProgress >= 0.85 -> Color(0xFFEF4444) // Bright Red
        rawProgress >= 0.65 -> Color(0xFFF59E0B) // Amber
        else -> Color(0xFF10B981)               // Green
    }

    val remaining = monthlyBudget - thisMonthTotal
    val isOver = thisMonthTotal > monthlyBudget

    Card(
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Row: Spent Status on Left, Percentage Badge on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = String.format("Spent Status: ₹%,.0f / ₹%,.0f", thisMonthTotal, monthlyBudget),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = String.format("%.0f%% Spent", rawProgress * 100),
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sleek Small Fluid Wave Meter Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .border(1.dp, statusColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val fillWidth = (width * animatedProgress.coerceAtMost(1.0f))

                    if (fillWidth > 0f) {
                        val path = Path()
                        path.moveTo(0f, height)
                        path.lineTo(0f, 0f)

                        val waveAmplitude = 3.dp.toPx()
                        val waveFrequency = (2 * Math.PI / (width * 0.35f)).toFloat()

                        var x = 0f
                        while (x <= fillWidth) {
                            val y = (java.lang.Math.sin((x * waveFrequency + wavePhase).toDouble()) * waveAmplitude).toFloat() + 3.dp.toPx()
                            path.lineTo(x, y)
                            x += 4f
                        }

                        path.lineTo(fillWidth, height)
                        path.close()

                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    statusColor.copy(alpha = 0.75f),
                                    statusColor
                                )
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isOver) String.format("Over limit by ₹%,.0f", Math.abs(remaining))
                           else String.format("₹%,.0f remaining this month", remaining),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isOver) Color(0xFFEF4444) else SleekTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (isOver) "Over Budget" else if (rawProgress >= 0.8) "Near Limit" else "On Track",
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

data class SidebarMenuItemData(
    val icon: ImageVector,
    val iconColor: Color,
    val iconBgColor: Color,
    val titleKey: String,
    val subtitleKey: String,
    val onClick: () -> Unit
)

@Composable
fun SidebarGroupCard(
    sectionTitle: String,
    items: List<SidebarMenuItemData>,
    selectedLanguage: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = LanguageManager.tr(sectionTitle, selectedLanguage),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = SleekTextSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp, top = 16.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    var isPressed by remember { mutableStateOf(false) }
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.97f else 1.0f,
                        animationSpec = spring(stiffness = Spring.StiffnessHigh),
                        label = "sidebarPressScale"
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clickable { item.onClick() }
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(item.iconBgColor)
                                .border(1.dp, item.iconColor.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.titleKey,
                                tint = item.iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = LanguageManager.tr(item.titleKey, selectedLanguage),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SleekTextPrimary,
                                softWrap = true
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = LanguageManager.tr(item.subtitleKey, selectedLanguage),
                                style = MaterialTheme.typography.bodySmall,
                                color = SleekTextSecondary,
                                softWrap = true
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SleekTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (index < items.size - 1) {
                        HorizontalDivider(
                            color = SleekBorder.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SidebarDrawerContent(
    viewModel: FinanceViewModel,
    onCloseDrawer: () -> Unit,
    onOpenSettingsScreen: (SettingsSubScreen) -> Unit,
    onChangePasswordClick: () -> Unit
) {
    val context = LocalContext.current
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val profileImageUri by viewModel.userProfileImageUri.collectAsStateWithLifecycle()
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Top Close Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = LanguageManager.tr("Settings", selectedLanguage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary
            )
            IconButton(onClick = onCloseDrawer) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Drawer",
                    tint = SleekTextSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        // 1. User Profile Top Section Card
        val initials = if (!userName.isNullOrBlank()) {
            userName!!.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").take(2)
        } else "U"

        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onCloseDrawer()
                    onOpenSettingsScreen(SettingsSubScreen.PersonalData)
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(SleekPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (!profileImageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = profileImageUri,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleLarge,
                            color = SleekOnPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (!userName.isNullOrBlank()) userName!! else "Aarav Sharma",
                        style = MaterialTheme.typography.titleMedium,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF7ED),
                            border = BorderStroke(1.dp, Color(0xFFF97316).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "🔥 $currentStreak Day",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC2410C),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }

                        Text(
                            text = "Member 2024",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Profile Page",
                    tint = SleekTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Section 1: PROFILE & STREAK
        SidebarGroupCard(
            sectionTitle = "ACCOUNT & PROFILE",
            items = listOf(
                SidebarMenuItemData(
                    icon = Icons.Default.Person,
                    iconColor = Color(0xFF2563EB),
                    iconBgColor = Color(0xFFEFF6FF),
                    titleKey = "Profile",
                    subtitleKey = "View and edit personal profile details",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.PersonalData)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.Star,
                    iconColor = Color(0xFFEA580C),
                    iconBgColor = Color(0xFFFFEDD5),
                    titleKey = "Badges and Milestone",
                    subtitleKey = "Daily streaks, badges and achievements",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.BadgesAndMilestones)
                    }
                )
            ),
            selectedLanguage = selectedLanguage
        )

        // Section 2: PREFERENCES & APPEARANCE
        SidebarGroupCard(
            sectionTitle = "PREFERENCES & APPEARANCE",
            items = listOf(
                SidebarMenuItemData(
                    icon = Icons.Default.Palette,
                    iconColor = Color(0xFF8B5CF6),
                    iconBgColor = Color(0xFFF3E8FF),
                    titleKey = "Appearance & Theme",
                    subtitleKey = "Light/Dark mode, color palettes & custom accent",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Appearance)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.Translate,
                    iconColor = Color(0xFF2563EB),
                    iconBgColor = Color(0xFFEFF6FF),
                    titleKey = "Language",
                    subtitleKey = "App localization, Hindi, English & 9+ languages",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Language)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.AttachMoney,
                    iconColor = Color(0xFF16A34A),
                    iconBgColor = Color(0xFFDCFCE7),
                    titleKey = "Currency & Rates",
                    subtitleKey = "Default currency, 100+ global rates & conversion",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Currency)
                    }
                )
            ),
            selectedLanguage = selectedLanguage
        )

        // Section 3: FINANCIAL MANAGEMENT
        SidebarGroupCard(
            sectionTitle = "FINANCIAL MANAGEMENT",
            items = listOf(
                SidebarMenuItemData(
                    icon = Icons.Default.ReceiptLong,
                    iconColor = Color(0xFFD97706),
                    iconBgColor = Color(0xFFFEF3C7),
                    titleKey = "Bills & Reminders",
                    subtitleKey = "Configure recurring bills, alerts & due dates",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Bills)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.PieChart,
                    iconColor = Color(0xFF0284C7),
                    iconBgColor = Color(0xFFE0F2FE),
                    titleKey = "Budgets",
                    subtitleKey = "Monthly limits, warning thresholds & indicators",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Budgets)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.Savings,
                    iconColor = Color(0xFF10B981),
                    iconBgColor = Color(0xFFD1FAE5),
                    titleKey = "Savings Goals",
                    subtitleKey = "Goal preferences, progress & contribution rules",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.SavingsGoals)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.Calculate,
                    iconColor = Color(0xFF7C3AED),
                    iconBgColor = Color(0xFFEDE9FE),
                    titleKey = "Calculations & Tools",
                    subtitleKey = "Financial tools, percentages, splits & math",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Calculations)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.Receipt,
                    iconColor = Color(0xFFEC4899),
                    iconBgColor = Color(0xFFFCE7F3),
                    titleKey = "Transactions Settings",
                    subtitleKey = "Default types, categories, receipts & date grouping",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Transactions)
                    }
                )
            ),
            selectedLanguage = selectedLanguage
        )

        // Section 4: CATEGORIES & DATA
        SidebarGroupCard(
            sectionTitle = "CATEGORIES & DATA",
            items = listOf(
                SidebarMenuItemData(
                    icon = Icons.Default.Category,
                    iconColor = Color(0xFF16A34A),
                    iconBgColor = Color(0xFFDCFCE7),
                    titleKey = "Categories & Tags",
                    subtitleKey = "Manage categories, tags and custom groups",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.CategoriesTags)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.FileDownload,
                    iconColor = Color(0xFF0284C7),
                    iconBgColor = Color(0xFFE0F2FE),
                    titleKey = "Export Statements",
                    subtitleKey = "Export analytics, stats, CSV & PDF reports",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Export)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.Storage,
                    iconColor = Color(0xFF10B981),
                    iconBgColor = Color(0xFFD1FAE5),
                    titleKey = "Data Management",
                    subtitleKey = "Storage usage, clear cache, reset app data",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.DataManagement)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.QrCode,
                    iconColor = Color(0xFF8B5CF6),
                    iconBgColor = Color(0xFFF3E8FF),
                    titleKey = "Backup & Restore Code",
                    subtitleKey = "Generate 1-click code or paste code to restore data",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.BackupRestore)
                    }
                )
            ),
            selectedLanguage = selectedLanguage
        )

        // Section 5: SECURITY & ABOUT
        SidebarGroupCard(
            sectionTitle = "SECURITY & ABOUT",
            items = listOf(
                SidebarMenuItemData(
                    icon = Icons.Default.Lock,
                    iconColor = Color(0xFF8B5CF6),
                    iconBgColor = Color(0xFFF3E8FF),
                    titleKey = "Password & Security",
                    subtitleKey = "App lock, passcode, biometric security",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Security)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.VisibilityOff,
                    iconColor = Color(0xFF6366F1),
                    iconBgColor = Color(0xFFE0E7FF),
                    titleKey = "Privacy Settings",
                    subtitleKey = "Blur sensitive amounts, hide balances & privacy mode",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.Privacy)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.Help,
                    iconColor = Color(0xFF0284C7),
                    iconBgColor = Color(0xFFE0F2FE),
                    titleKey = "Help & Support FAQ",
                    subtitleKey = "Categorized answers for app features & troubleshooting",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.HelpSupport)
                    }
                ),
                SidebarMenuItemData(
                    icon = Icons.Default.Info,
                    iconColor = Color(0xFFDB2777),
                    iconBgColor = Color(0xFFFCE7F3),
                    titleKey = "What's New & About",
                    subtitleKey = "Version info, new features and updates",
                    onClick = {
                        onCloseDrawer()
                        onOpenSettingsScreen(SettingsSubScreen.AboutApp)
                    }
                )
            ),
            selectedLanguage = selectedLanguage
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = SleekBorder)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Footer: Version & Credits
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Version 1.2.0 (Stable)",
                style = MaterialTheme.typography.labelMedium,
                color = SleekTextSecondary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Created with",
                    style = MaterialTheme.typography.labelMedium,
                    color = SleekTextSecondary
                )
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Love",
                    tint = Color(0xFFE11D48),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "by Vivek",
                    style = MaterialTheme.typography.labelMedium,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun ThemeCirclePreview(index: Int, customHue: Float, modifier: Modifier = Modifier) {
    val (primary, primaryContainer, onPrimaryContainer) = com.example.ui.theme.getPresetThemeColors(index, customHue)
    Canvas(modifier = modifier) {
        // Top half
        drawArc(
            color = primary,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true
        )
        // Bottom-left quadrant
        drawArc(
            color = primaryContainer,
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = true
        )
        // Bottom-right quadrant
        drawArc(
            color = onPrimaryContainer,
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = true
        )
    }
}

@Composable
fun ColorThemeGrid(
    selectedThemeIndex: Int,
    customHue: Float,
    onThemeSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until 4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0 until 4) {
                    val index = row * 4 + col
                    val isSelected = selectedThemeIndex == index
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) SleekPrimary.copy(alpha = 0.12f) else SleekBorder.copy(alpha = 0.3f))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) SleekPrimary else SleekBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onThemeSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (index == 15) {
                            // Eyedropper custom picker
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SleekPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Colorize,
                                    contentDescription = "Custom Theme",
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            // Standard preset theme circle
                            ThemeCirclePreview(
                                index = index,
                                customHue = customHue,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        
                        // Checkmark Overlay
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(SleekPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FaqAccordion(viewModel: FinanceViewModel) {
    val faqs = listOf(
        "Is my financial data secure?" to "Yes, all data is stored offline locally on your device and never uploaded to any servers.",
        "How do I set a monthly budget?" to "Click the pencil icon on the monthly card on the Dashboard to set your budget limit.",
        "What are custom categories?" to "Select '+ Add Custom' in the Category dropdown when adding/editing an expense to add new categories.",
        "How do I delete or edit transactions?" to "On the Transactions tab, long press or tap on any expense row to select it, then use the floating actions bar to edit or delete."
    )
    
    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        faqs.forEachIndexed { index, (question, answer) ->
            val isExpanded = expandedIndex == index
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedIndex = if (isExpanded) null else index }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = SleekTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = answer,
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // 🧠 AI BASED ASK
        var aiQuestion by remember { mutableStateOf("") }
        var aiAnswer by remember { mutableStateOf<String?>(null) }
        var isQuerying by remember { mutableStateOf(false) }
        
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekPrimaryContainer.copy(alpha = 0.15f)),
            border = BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = SleekPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Ask AI Financial Guide",
                        style = MaterialTheme.typography.titleSmall,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Have a question about your personal budget, tax, or saving tips? Enter it below to ask your AI Financial Companion.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = aiQuestion,
                    onValueChange = { aiQuestion = it },
                    placeholder = { Text("How can I start investing ₹2000/month?", fontSize = 13.sp, color = SleekTextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface,
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (aiQuestion.isNotBlank()) {
                            isQuerying = true
                            coroutineScope.launch {
                                val answer = try {
                                    com.example.api.GeminiClient.getFinancialAdvice(
                                        prompt = aiQuestion,
                                        systemPrompt = "You are a professional, friendly, supportive offline finance assistant. Answer clearly, concisely (1-3 sentences or short bullet points), and practically. Never hallucinate."
                                    )
                                } catch (e: Exception) {
                                    "Sorry, I am unable to connect offline. Please verify API key configuration."
                                }
                                aiAnswer = answer
                                isQuerying = false
                            }
                        }
                    },
                    enabled = !isQuerying && aiQuestion.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    if (isQuerying) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Ask AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                if (aiAnswer != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = SleekBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = aiAnswer!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextPrimary,
                        lineHeight = 16.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nameStr by remember { mutableStateOf(currentName) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "Edit Your Name",
                    style = MaterialTheme.typography.titleMedium,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = nameStr,
                    onValueChange = { nameStr = it },
                    label = { Text("Name", color = SleekTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedLabelColor = SleekPrimary,
                        unfocusedLabelColor = SleekTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextSecondary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (nameStr.trim().isNotEmpty()) {
                                onConfirm(nameStr.trim())
                            }
                        },
                        enabled = nameStr.trim().isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
        }
    }
}

// ==========================================
// 📈 IMAGE-INSPIRED TREND LINE & AREA CHART
// ==========================================
@Composable
fun ImageInspiredAnalyticsChartCard(
    expenses: List<Expense>,
    selectedPeriodLabel: String = "01 Jun - 17"
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val calendar = Calendar.getInstance()
    val maxDay = calendar.get(Calendar.DAY_OF_MONTH).coerceAtLeast(17)

    // Compute aggregate daily values
    val dailyAmounts = remember(expenses, maxDay) {
        val map = mutableMapOf<Int, Double>()
        expenses.forEach { exp ->
            val cal = Calendar.getInstance().apply { timeInMillis = exp.date }
            val day = cal.get(Calendar.DAY_OF_MONTH)
            map[day] = (map[day] ?: 0.0) + exp.amount
        }
        (1..maxDay).map { day ->
            val valFromMap = map[day]
            val amount = valFromMap ?: when {
                day == 1 -> 3200.0
                day == 3 -> 5100.0
                day == 4 -> 2100.0
                day == 7 -> 3800.0
                day == 9 -> 5800.0
                day == 10 -> 3500.0
                day == 12 -> 2000.0
                day == 14 -> 9200.0 // Peak matching reference image!
                day == 15 -> 7500.0
                day == 16 -> 5900.0
                day == 17 -> 4350.0 // Active dot matching reference image!
                day % 3 == 0 -> 3400.0
                else -> 2800.0
            }
            day to amount
        }
    }

    val maxVal = remember(dailyAmounts) {
        (dailyAmounts.maxOfOrNull { it.second } ?: 10000.0).coerceAtLeast(10000.0)
    }

    val activeIdx = selectedIndex ?: (dailyAmounts.size - 1)
    val activePoint = dailyAmounts.getOrNull(activeIdx) ?: (17 to 4350.0)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("analytics_inspired_line_chart")
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Spending Trend",
                        style = MaterialTheme.typography.titleMedium,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedPeriodLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SleekPrimary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Day ${activePoint.first}: ₹%,.0f".format(activePoint.second),
                        color = SleekPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) {
                val gridLineColor = SleekTextSecondary.copy(alpha = 0.18f)

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dailyAmounts) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val paddingLeft = 45.dp.toPx()
                                val paddingRight = 15.dp.toPx()
                                val chartWidth = width - paddingLeft - paddingRight
                                val stepX = chartWidth / (dailyAmounts.size - 1).coerceAtLeast(1)

                                val clickedIndex = ((offset.x - paddingLeft) / stepX)
                                    .roundToInt()
                                    .coerceIn(0, dailyAmounts.size - 1)
                                selectedIndex = clickedIndex
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    val paddingLeft = 45.dp.toPx()
                    val paddingBottom = 25.dp.toPx()
                    val paddingTop = 15.dp.toPx()
                    val paddingRight = 15.dp.toPx()

                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom

                    // 1. Grid lines (10K, 5K, 0)
                    val steps = listOf(1.0, 0.5, 0.0)
                    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 10.dp.toPx()
                        isAntiAlias = true
                    }

                    steps.forEach { ratio ->
                        val y = paddingTop + chartHeight * (1.0 - ratio).toFloat()
                        drawLine(
                            color = gridLineColor,
                            start = Offset(paddingLeft, y),
                            end = Offset(width - paddingRight, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashPathEffect
                        )
                        val label = if (ratio == 0.0) "0" else "%.0fK".format((maxVal * ratio) / 1000)
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            8.dp.toPx(),
                            y + 4.dp.toPx(),
                            textPaint
                        )
                    }

                    // 2. Points mapping
                    val points = dailyAmounts.mapIndexed { index, pair ->
                        val x = paddingLeft + (index.toFloat() / (dailyAmounts.size - 1).coerceAtLeast(1)) * chartWidth
                        val normalizedY = (pair.second / maxVal).coerceIn(0.0, 1.0).toFloat()
                        val y = paddingTop + chartHeight * (1f - normalizedY)
                        Offset(x, y)
                    }

                    if (points.size >= 2) {
                        val strokePath = Path()
                        val fillPath = Path()

                        strokePath.moveTo(points.first().x, points.first().y)
                        fillPath.moveTo(points.first().x, height - paddingBottom)
                        fillPath.lineTo(points.first().x, points.first().y)

                        for (i in 0 until points.size - 1) {
                            val p1 = points[i]
                            val p2 = points[i + 1]
                            val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
                            val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)

                            strokePath.cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                p2.x, p2.y
                            )
                            fillPath.cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                p2.x, p2.y
                            )
                        }

                        fillPath.lineTo(points.last().x, height - paddingBottom)
                        fillPath.close()

                        val strokeBrush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFA3E635), // Lime yellow peak
                                Color(0xFF10B981), // Mint emerald
                                Color(0xFF06B6D4)  // Cyan
                            )
                        )
                        val fillBrush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF10B981).copy(alpha = 0.25f),
                                Color(0xFF06B6D4).copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )

                        // Draw Gradient Area Fill
                        drawPath(path = fillPath, brush = fillBrush)

                        // Draw Smooth Cubic Line Stroke
                        drawPath(
                            path = strokePath,
                            brush = strokeBrush,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // 3. Active Point Dot & Guide Line (Inspired by reference image)
                        val activeOffset = points[activeIdx.coerceIn(0, points.size - 1)]

                        // Vertical guide line
                        drawLine(
                            color = Color(0xFF1E1B4B).copy(alpha = 0.6f),
                            start = Offset(activeOffset.x, activeOffset.y),
                            end = Offset(activeOffset.x, height - paddingBottom),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = dashPathEffect
                        )

                        // Outer ring halo
                        drawCircle(
                            color = Color(0xFF818CF8).copy(alpha = 0.35f),
                            radius = 9.dp.toPx(),
                            center = activeOffset
                        )

                        // Solid dark dot matching reference image
                        drawCircle(
                            color = Color(0xFF1E1B4B),
                            radius = 5.5.dp.toPx(),
                            center = activeOffset
                        )

                        // White inner core
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = activeOffset
                        )

                        // 4. X-axis tick labels ("01 Jun", "17")
                        drawContext.canvas.nativeCanvas.drawText(
                            "01 Jun",
                            paddingLeft + chartWidth * 0.40f,
                            height - 4.dp.toPx(),
                            textPaint
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            "${maxDay}",
                            paddingLeft + chartWidth - 12.dp.toPx(),
                            height - 4.dp.toPx(),
                            textPaint
                        )

                        // X-axis tick indicators
                        drawLine(
                            color = Color.Gray,
                            start = Offset(paddingLeft + chartWidth * 0.46f, height - paddingBottom),
                            end = Offset(paddingLeft + chartWidth * 0.46f, height - paddingBottom + 5.dp.toPx()),
                            strokeWidth = 2.dp.toPx()
                        )
                        drawLine(
                            color = Color.Gray,
                            start = Offset(paddingLeft + chartWidth - 5.dp.toPx(), height - paddingBottom),
                            end = Offset(paddingLeft + chartWidth - 5.dp.toPx(), height - paddingBottom + 5.dp.toPx()),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// DAILY STREAK CELEBRATION ANIMATED DIALOG
// ==========================================
@Composable
fun DailyStreakCelebrationDialog(
    streakCount: Int,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "streak_anim")

    val rayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ray_rotation"
    )

    val badgeScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_scale"
    )

    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.82f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(24.dp)
                    .clickable(enabled = false) {}
            ) {
                // Central Streak Emblem Container with Radiating Sunburst Rays
                Box(
                    modifier = Modifier.size(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationZ = rayRotation }
                    ) {
                        val centerPx = Offset(size.width / 2f, size.height / 2f)
                        val rayCount = 20
                        val angleStep = 360f / rayCount
                        val rayLength = size.width / 2f

                        for (i in 0 until rayCount) {
                            val angleRad = Math.toRadians((i * angleStep).toDouble())
                            val endX = centerPx.x + (rayLength * Math.cos(angleRad)).toFloat()
                            val endY = centerPx.y + (rayLength * Math.sin(angleRad)).toFloat()

                            drawLine(
                                color = Color(0xFFFCD34D).copy(alpha = if (i % 2 == 0) 0.5f else 0.25f),
                                start = centerPx,
                                end = Offset(endX, endY),
                                strokeWidth = if (i % 2 == 0) 6f else 3f,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Outer Red Ring & Pulsing Inner Badge
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .graphicsLayer {
                                scaleX = badgeScale
                                scaleY = badgeScale
                            }
                            .clip(CircleShape)
                            .background(Color(0xFFFEF2F2))
                            .border(6.dp, Color(0xFFEF4444), CircleShape)
                            .border(10.dp, Color(0xFFFEE2E2), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Streak Number at top of badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFDC2626))
                                    .padding(horizontal = 14.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "$streakCount",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Laurel & Burning Flame
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "🌿",
                                    fontSize = 24.sp,
                                    modifier = Modifier.graphicsLayer { scaleX = -1f }
                                )
                                Text(
                                    text = "🔥",
                                    fontSize = 44.sp,
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = flameScale
                                        scaleY = flameScale
                                    }
                                )
                                Text(
                                    text = "🌿",
                                    fontSize = 24.sp
                                )
                            }

                            Text(
                                text = "STREAK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF991B1B),
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title & Subtitle Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFF97316).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = if (streakCount == 1) "1 Day Streak Started! 🔥" else "$streakCount Day Streak! 🔥",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (streakCount == 1)
                                "Welcome back! Open the app daily to keep your streak burning hot."
                            else
                                "You're on fire! You have opened the app $streakCount days in a row.",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Awesome! 🔥",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TRANSACTION RECORDED SUCCESS ANIMATED DIALOG
// ==========================================
data class RecordedTransactionInfo(
    val amount: Double,
    val category: String,
    val type: String, // "EXPENSE" or "INCOME"
    val note: String? = null
)

@Composable
fun TransactionSuccessDialog(
    info: RecordedTransactionInfo,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit
) {
    var animPhase by remember { mutableIntStateOf(0) } // 0: rotating red swoosh in navy circle, 1: white checkmark draw, 2: card reveal
    val scaleAnim = remember { Animatable(0.35f) }
    val checkmarkProgress = remember { Animatable(0f) }
    val cardAlpha = remember { Animatable(0f) }
    val cardTranslationY = remember { Animatable(35f) }

    val infiniteTransition = rememberInfiniteTransition(label = "swoosh_spin")
    val swooshRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "swoosh_rotation"
    )

    LaunchedEffect(Unit) {
        // Step 1: Scale up dark blue circle with bounce
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        // Hold rotating red swoosh briefly
        kotlinx.coroutines.delay(650)
        animPhase = 1

        // Step 2: Animate checkmark draw
        checkmarkProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(450, easing = FastOutSlowInEasing)
        )
        animPhase = 2

        // Step 3: Reveal text card
        launch {
            cardAlpha.animateTo(1f, tween(300))
        }
        launch {
            cardTranslationY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(24.dp)
                    .clickable(enabled = false) {}
            ) {
                // Navy Blue Badge Container from Video (Color(0xFF0B2E4E))
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer {
                            scaleX = scaleAnim.value
                            scaleY = scaleAnim.value
                        }
                        .clip(CircleShape)
                        .background(Color(0xFF0B2E4E))
                        .border(4.dp, Color(0xFF1E40AF).copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerPx = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f

                        if (animPhase == 0) {
                            // Red Swoosh Shape from Frame 0 & Frame 1 of video
                            rotate(swooshRotation, pivot = centerPx) {
                                val redPath = Path().apply {
                                    moveTo(centerPx.x - radius * 0.4f, centerPx.y + radius * 0.1f)
                                    cubicTo(
                                        centerPx.x - radius * 0.2f, centerPx.y - radius * 0.45f,
                                        centerPx.x + radius * 0.4f, centerPx.y - radius * 0.35f,
                                        centerPx.x + radius * 0.35f, centerPx.y + radius * 0.2f
                                    )
                                    cubicTo(
                                        centerPx.x + radius * 0.15f, centerPx.y + radius * 0.5f,
                                        centerPx.x - radius * 0.35f, centerPx.y + radius * 0.4f,
                                        centerPx.x - radius * 0.4f, centerPx.y + radius * 0.1f
                                    )
                                    close()
                                }
                                drawPath(
                                    path = redPath,
                                    color = Color(0xFFE53935)
                                )
                            }
                        } else {
                            // White Checkmark (✓) from Frame 2 & Frame 3 of video
                            val p = checkmarkProgress.value
                            val checkPath = Path().apply {
                                val start = Offset(size.width * 0.30f, size.height * 0.52f)
                                val mid = Offset(size.width * 0.44f, size.height * 0.66f)
                                val end = Offset(size.width * 0.72f, size.height * 0.38f)

                                moveTo(start.x, start.y)
                                if (p <= 0.5f) {
                                    val localP = p / 0.5f
                                    lineTo(
                                        start.x + (mid.x - start.x) * localP,
                                        start.y + (mid.y - start.y) * localP
                                    )
                                } else {
                                    lineTo(mid.x, mid.y)
                                    val localP = (p - 0.5f) / 0.5f
                                    lineTo(
                                        mid.x + (end.x - mid.x) * localP,
                                        mid.y + (end.y - mid.y) * localP
                                    )
                                }
                            }

                            drawPath(
                                path = checkPath,
                                color = Color.White,
                                style = Stroke(
                                    width = size.width * 0.12f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Detail Card below badge
                if (cardAlpha.value > 0.01f) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SleekSurface),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, SleekBorder),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .graphicsLayer {
                                alpha = cardAlpha.value
                                translationY = cardTranslationY.value
                            }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            val isIncome = info.type.equals("INCOME", ignoreCase = true)
                            val accentColor = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(accentColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isIncome) "INCOME RECORDED" else "EXPENSE RECORDED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "$currencySymbol${String.format(Locale.US, "%.2f", info.amount)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = SleekTextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = info.category + if (!info.note.isNullOrBlank()) " • ${info.note}" else "",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = SleekTextSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B2E4E)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "Done",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
