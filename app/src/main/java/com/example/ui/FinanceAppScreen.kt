package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Expense
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Expenses : Screen("expenses", "Expenses", Icons.Default.ReceiptLong)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.PieChart)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)
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

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Food" -> Icons.Default.Restaurant
        "Travel" -> Icons.Default.DirectionsCar
        "Rent" -> Icons.Default.Home
        "Utilities" -> Icons.Default.Bolt
        "Entertainment" -> Icons.Default.Movie
        "Shopping" -> Icons.Default.ShoppingCart
        "Persons" -> Icons.Default.Person
        else -> Icons.Default.Category
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceAppScreen(viewModel: FinanceViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val filteredExpenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val selectedDateRange by viewModel.selectedDateRange.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val monthlyBudget by viewModel.monthlyBudget.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var prefilledDateForAddDialog by remember { mutableStateOf<Long?>(null) }
    var showAiCoachDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = SleekBg,
                modifier = Modifier.width(320.dp).fillMaxHeight()
            ) {
                SidebarDrawerContent(
                    viewModel = viewModel,
                    onCloseDrawer = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SleekSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                // Symmetric 5-slot bottom bar layout
                // Slot 1: Dashboard
                val isDash = currentScreen == Screen.Dashboard
                NavigationBarItem(
                    selected = isDash,
                    onClick = { currentScreen = Screen.Dashboard },
                    icon = {
                        Icon(
                            imageVector = Screen.Dashboard.icon,
                            contentDescription = Screen.Dashboard.title,
                            tint = if (isDash) SleekPrimary else SleekTextSecondary
                        )
                    },
                    label = {
                        Text(
                            text = Screen.Dashboard.title,
                            color = if (isDash) SleekTextPrimary else SleekTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isDash) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = SleekPrimaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_dashboard")
                )

                // Slot 2: Expenses Ledger
                val isLedger = currentScreen == Screen.Expenses
                NavigationBarItem(
                    selected = isLedger,
                    onClick = { currentScreen = Screen.Expenses },
                    icon = {
                        Icon(
                            imageVector = Screen.Expenses.icon,
                            contentDescription = Screen.Expenses.title,
                            tint = if (isLedger) SleekPrimary else SleekTextSecondary
                        )
                    },
                    label = {
                        Text(
                            text = Screen.Expenses.title,
                            color = if (isLedger) SleekTextPrimary else SleekTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isLedger) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = SleekPrimaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_expenses")
                )

                // Slot 3: CENTER ADD BUTTON
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        prefilledDateForAddDialog = null
                        showAddExpenseDialog = true
                    },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SleekPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Expense",
                                tint = Color.White
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "Add",
                            color = SleekPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.testTag("nav_item_add")
                )

                // Slot 4: Analytics
                val isAnalytics = currentScreen == Screen.Analytics
                NavigationBarItem(
                    selected = isAnalytics,
                    onClick = { currentScreen = Screen.Analytics },
                    icon = {
                        Icon(
                            imageVector = Screen.Analytics.icon,
                            contentDescription = Screen.Analytics.title,
                            tint = if (isAnalytics) SleekPrimary else SleekTextSecondary
                        )
                    },
                    label = {
                        Text(
                            text = Screen.Analytics.title,
                            color = if (isAnalytics) SleekTextPrimary else SleekTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isAnalytics) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = SleekPrimaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_analytics")
                )

                // Slot 5: Live Calendar
                val isCalendar = currentScreen == Screen.Calendar
                NavigationBarItem(
                    selected = isCalendar,
                    onClick = { currentScreen = Screen.Calendar },
                    icon = {
                        Icon(
                            imageVector = Screen.Calendar.icon,
                            contentDescription = Screen.Calendar.title,
                            tint = if (isCalendar) SleekPrimary else SleekTextSecondary
                        )
                    },
                    label = {
                        Text(
                            text = Screen.Calendar.title,
                            color = if (isCalendar) SleekTextPrimary else SleekTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isCalendar) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = SleekPrimaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_calendar")
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
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    Screen.Dashboard -> DashboardTab(
                        expenses = filteredExpenses,
                        userName = userName,
                        monthlyBudget = monthlyBudget,
                        onUpdateBudget = { viewModel.updateMonthlyBudget(it) },
                        onUpdateName = { viewModel.saveUserName(it) },
                        onAddExpenseClick = {
                            prefilledDateForAddDialog = null
                            showAddExpenseDialog = true
                        },
                        onNavigateToExpenses = { currentScreen = Screen.Expenses },
                        onAiCoachClick = { showAiCoachDialog = true },
                        onProfileClick = { scope.launch { drawerState.open() } }
                    )
                    Screen.Expenses -> ExpensesTab(
                        viewModel = viewModel,
                        onAddExpenseClick = {
                            prefilledDateForAddDialog = null
                            showAddExpenseDialog = true
                        },
                        onEditExpenseClick = { editingExpense = it }
                    )
                    Screen.Analytics -> AnalyticsTab(
                        viewModel = viewModel
                    )
                    Screen.Calendar -> CalendarTab(
                        expenses = expenses,
                        onAddExpenseForDate = { date ->
                            prefilledDateForAddDialog = date
                            showAddExpenseDialog = true
                        },
                        onEditExpense = { editingExpense = it },
                        onDeleteExpense = { viewModel.deleteExpense(it) },
                        onAiCoachClick = { showAiCoachDialog = true }
                    )
                }
            }

            if (showAddExpenseDialog) {
                AddExpenseDialog(
                    prefilledDate = prefilledDateForAddDialog,
                    categories = allCategories,
                    onAddCategory = { viewModel.addCustomCategory(it) },
                    onDismiss = { showAddExpenseDialog = false },
                    onConfirm = { amount, category, date, note ->
                        viewModel.addExpense(amount, category, date, note)
                        showAddExpenseDialog = false
                    }
                )
            }

            if (editingExpense != null) {
                EditExpenseDialog(
                    expense = editingExpense!!,
                    categories = allCategories,
                    onAddCategory = { viewModel.addCustomCategory(it) },
                    onDismiss = { editingExpense = null },
                    onConfirm = { updatedExpense ->
                        viewModel.updateExpense(updatedExpense)
                        editingExpense = null
                    }
                )
            }

            // Floating / Dialog Chat Overlay for AI Coach to preserve the feature perfectly
            if (showAiCoachDialog) {
                Dialog(onDismissRequest = { showAiCoachDialog = false }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SleekBg),
                        border = BorderStroke(1.dp, SleekBorder),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AdvisorTab(viewModel = viewModel)
                            IconButton(
                                onClick = { showAiCoachDialog = false },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close AI Coach",
                                    tint = SleekTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

    if (userName.isNullOrBlank()) {
        OnboardingNameDialog(onSave = { viewModel.saveUserName(it) })
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
    onUpdateBudget: (Double) -> Unit,
    onUpdateName: (String) -> Unit,
    onAddExpenseClick: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onAiCoachClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val currentCalendar = Calendar.getInstance()
    val currentMonth = currentCalendar.get(Calendar.MONTH)
    val currentYear = currentCalendar.get(Calendar.YEAR)

    // Filter current month expenses
    val thisMonthExpenses = expenses.filter {
        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
        cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
    }
    val thisMonthTotal = thisMonthExpenses.sumOf { it.amount }

    // Last month expenses
    val lastMonthCalendar = Calendar.getInstance().apply {
        add(Calendar.MONTH, -1)
    }
    val lastMonth = lastMonthCalendar.get(Calendar.MONTH)
    val lastMonthYear = lastMonthCalendar.get(Calendar.YEAR)

    val lastMonthExpenses = expenses.filter {
        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
        cal.get(Calendar.MONTH) == lastMonth && cal.get(Calendar.YEAR) == lastMonthYear
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
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium,
                        color = SleekOnPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
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

            // Quick Add & AI Sparkles Header Action Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onAiCoachClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SleekPrimaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Coach",
                        tint = SleekPrimary
                    )
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

        // Total Balance Card (Sleek UI Gradient Style)
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
                    Text(
                        text = "MONTHLY TOTAL EXPENSE",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = String.format("₹%,.2f", thisMonthTotal),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

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
                        text = String.format("vs last month (₹%,.0f)", lastMonthTotal),
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Category distribution preview indicators
        Text(
            text = "Categories Summary",
            style = MaterialTheme.typography.titleMedium,
            color = SleekTextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val uniqueCats = listOf("Food", "Travel", "Rent", "Persons")
            uniqueCats.forEach { cat ->
                val catSum = thisMonthExpenses.filter { it.category == cat }.sumOf { it.amount }
                val catColor = categoryColors[cat] ?: SleekPrimary
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(catColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(cat),
                                contentDescription = cat,
                                tint = catColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall,
                            color = SleekTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = String.format("₹%,.0f", catSum),
                            style = MaterialTheme.typography.titleSmall,
                            color = SleekTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Budget Limit & Goal Section
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdjustBudgetDialog = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Monthly Budget Cap",
                            style = MaterialTheme.typography.titleSmall,
                            color = SleekTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap to adjust limit",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format("₹%,.0f Limit", monthlyBudget),
                            style = MaterialTheme.typography.titleMedium,
                            color = SleekPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Budget",
                            tint = SleekPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val budgetProgress = if (thisMonthTotal < monthlyBudget) {
                    (thisMonthTotal / monthlyBudget).toFloat()
                } else {
                    1.0f
                }

                LinearProgressIndicator(
                    progress = { budgetProgress },
                    color = if (budgetProgress > 0.85f) ExpenseRed else SleekPrimary,
                    trackColor = SleekNeutralLight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format("₹%,.0f spent", thisMonthTotal),
                        style = MaterialTheme.typography.labelSmall,
                        color = SleekTextSecondary
                    )
                    val remaining = monthlyBudget - thisMonthTotal
                    Text(
                        text = if (remaining >= 0) String.format("₹%,.0f remaining", remaining) else "Limit exceeded!",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (remaining >= 0) SleekTextSecondary else ExpenseRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Expenses List Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Expenses",
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
                    text = "No expenses recorded yet. Tap + to add!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SleekTextSecondary
                )
            }
        } else {
            // Show top 3 recent items
            expenses.take(3).forEach { expense ->
                RecentExpenseRow(expense = expense)
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

@Composable
fun RecentExpenseRow(expense: Expense) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(expense.date))
    val catColor = categoryColors[expense.category] ?: SleekPrimary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                    imageVector = getCategoryIcon(expense.category),
                    contentDescription = expense.category,
                    tint = catColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.note ?: "Expense",
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
                text = String.format("-₹%,.2f", expense.amount),
                style = MaterialTheme.typography.bodyMedium,
                color = ExpenseRed,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==========================================
// 2️⃣ ALL EXPENSES TAB (With Filters & Deletes)
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

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    
    val rawCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val categories = remember(rawCategories) { listOf("All") + rawCategories }

    var selectedExpenseIds by remember { mutableStateOf(setOf<Int>()) }
    var showDateRangePickerDialog by remember { mutableStateOf(false) }

    // Filtered expenses list
    val filteredExpenses = expenses.filter {
        val matchesSearch = it.note?.contains(searchQuery, ignoreCase = true) == true ||
                it.category.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == "All" || it.category == selectedCategoryFilter
        matchesSearch && matchesCategory
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Expense Ledger",
                        style = MaterialTheme.typography.headlineSmall,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hold a row to select/edit. Offline safe.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary
                    )
                }

                // Date Range Button with Calendar Icon
                IconButton(
                    onClick = { showDateRangePickerDialog = true },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (selectedDateRange != null) SleekPrimaryContainer else SleekSurface)
                        .border(1.dp, if (selectedDateRange != null) SleekPrimary else SleekBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Filter Date Range",
                        tint = if (selectedDateRange != null) SleekPrimary else SleekTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date Range Active Indicator Bar
            if (selectedDateRange != null) {
                val sStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDateRange!!.first))
                val eStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDateRange!!.second))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SleekPrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter: $sStr to $eStr",
                        style = MaterialTheme.typography.labelMedium,
                        color = SleekPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Date Filter",
                        tint = SleekPrimary,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { viewModel.setDateRange(null, null) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Search Ledger
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search description or category...", color = SleekTextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SleekTextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SleekPrimary,
                    unfocusedBorderColor = SleekBorder,
                    focusedContainerColor = SleekSurface,
                    unfocusedContainerColor = SleekSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Category Tab Filter
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val selected = selectedCategoryFilter == cat
                    val chipBg = if (selected) SleekPrimary else SleekSurface
                    val chipText = if (selected) Color.White else SleekTextSecondary
                    val chipBorder = if (selected) Color.Transparent else SleekBorder

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(chipBg)
                            .border(1.dp, chipBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedCategoryFilter = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            color = chipText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expenses match your filters.",
                        color = SleekTextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredExpenses, key = { it.id }) { expense ->
                        val isSelected = selectedExpenseIds.contains(expense.id)
                        InteractiveLedgerRow(
                            expense = expense,
                            isSelected = isSelected,
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
                                }
                            },
                            onDeleteClick = { viewModel.deleteExpense(expense) }
                        )
                    }
                }
            }
        }

        // Floating selected expense actions bar
        if (selectedExpenseIds.isNotEmpty()) {
            val selectedExpenses = filteredExpenses.filter { selectedExpenseIds.contains(it.id) }
            if (selectedExpenses.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selected: ${selectedExpenses.size} items",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextPrimary,
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
                                        .background(SleekPrimary.copy(alpha = 0.1f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SleekPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                            IconButton(
                                onClick = {
                                    viewModel.deleteExpenses(selectedExpenses)
                                    selectedExpenseIds = emptySet()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ExpenseRed.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = ExpenseRed, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { selectedExpenseIds = emptySet() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(SleekBorder, CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Deselect All", tint = SleekTextSecondary, modifier = Modifier.size(16.dp))
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InteractiveLedgerRow(
    expense: Expense,
    isSelected: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(expense.date))
    val catColor = categoryColors[expense.category] ?: SleekPrimary
    var showConfirmDelete by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SleekPrimary.copy(alpha = 0.05f) else SleekSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) SleekPrimary else SleekBorder
        ),
        shape = RoundedCornerShape(20.dp),
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(catColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(expense.category),
                    contentDescription = expense.category,
                    tint = catColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.note ?: "No Note",
                    style = MaterialTheme.typography.titleMedium,
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

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = String.format("-₹%,.2f", expense.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ExpenseRed,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(
                    onClick = { showConfirmDelete = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ExpenseRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Delete Expense?") },
            text = { Text("Are you sure you want to permanently delete this expense: \"${expense.note}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showConfirmDelete = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("Cancel")
                }
            },
            containerColor = SleekSurface
        )
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

// ==========================================
// 3️⃣ ANALYTICS TAB (Custom Canvas charts)
// ==========================================
@Composable
fun AnalyticsTab(viewModel: FinanceViewModel) {
    val expenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val allExpenses by viewModel.expenses.collectAsStateWithLifecycle()
    val selectedDateRange by viewModel.selectedDateRange.collectAsStateWithLifecycle()

    var monthOffset by remember { mutableStateOf(0) }

    // Reset offset if date range filter is active
    LaunchedEffect(selectedDateRange) {
        if (selectedDateRange != null) {
            monthOffset = 0
        }
    }

    // Determine target expenses for category distribution
    val distributionExpenses = if (selectedDateRange != null) {
        expenses
    } else {
        val targetCal = Calendar.getInstance()
        if (monthOffset != 0) {
            targetCal.add(Calendar.MONTH, monthOffset)
        }
        val targetMonth = targetCal.get(Calendar.MONTH)
        val targetYear = targetCal.get(Calendar.YEAR)
        allExpenses.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.date }
            cal.get(Calendar.MONTH) == targetMonth && cal.get(Calendar.YEAR) == targetYear
        }
    }

    val distributionTotal = distributionExpenses.sumOf { it.amount }

    // Grouping by category
    val categorySums = distributionExpenses
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    val activePeriodLabel = if (selectedDateRange != null) {
        val sStr = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(selectedDateRange!!.first))
        val eStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDateRange!!.second))
        "$sStr - $eStr"
    } else {
        val targetCal = Calendar.getInstance()
        if (monthOffset != 0) {
            targetCal.add(Calendar.MONTH, monthOffset)
        }
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(targetCal.time)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Analytics Dashboard",
                style = MaterialTheme.typography.headlineSmall,
                color = SleekTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Dynamic Category Pie & Month comparison charts",
                style = MaterialTheme.typography.bodySmall,
                color = SleekTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 📊 PIE / DONUT CHART CARD
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Category Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    text = "Breakdown for $activePeriodLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (distributionTotal == 0.0) {
                    Box(
                        modifier = Modifier
                            .height(180.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No spending in this period.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SleekTextSecondary
                        )
                    }
                } else {
                    // Custom Donut/Pie Chart drawn on Canvas
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            var startAngle = -90f
                            categorySums.forEach { (cat, sum) ->
                                val sweepAngle = ((sum / distributionTotal) * 360f).toFloat()
                                val color = categoryColors[cat] ?: SleekPrimary
                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    size = Size(size.width, size.height),
                                    style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round)
                                )
                                startAngle += sweepAngle
                            }
                        }

                        // Text in center of Donut Chart
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Total Spent",
                                style = MaterialTheme.typography.labelSmall,
                                color = SleekTextSecondary
                            )
                            Text(
                                text = String.format("₹%,.0f", distributionTotal),
                                style = MaterialTheme.typography.titleLarge,
                                color = SleekTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Legend & category list
                    categorySums.forEach { (cat, sum) ->
                        val catColor = categoryColors[cat] ?: SleekPrimary
                        val catPct = (sum / distributionTotal) * 100

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(catColor)
                                )
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SleekTextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format("₹%,.2f", sum),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SleekTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format("%.1f%%", catPct),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary,
                                    modifier = Modifier.width(48.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 📊 BAR CHART CARD (Monthly Expense Comparison with Navigation!)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Monthly Spending Trend",
                    style = MaterialTheme.typography.titleMedium,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Historical comparisons (3-month window)",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Get sums for 3 months centered around monthOffset
                val cal = Calendar.getInstance()
                if (monthOffset != 0) {
                    cal.add(Calendar.MONTH, monthOffset)
                }

                // Month 3 (Latest in window)
                val m3Name = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(cal.time)
                val m3Sum = allExpenses.filter {
                    val c = Calendar.getInstance().apply { timeInMillis = it.date }
                    c.get(Calendar.MONTH) == cal.get(Calendar.MONTH) && c.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                }.sumOf { it.amount }

                // Month 2 (Middle in window)
                cal.add(Calendar.MONTH, -1)
                val m2Name = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(cal.time)
                val m2Sum = allExpenses.filter {
                    val c = Calendar.getInstance().apply { timeInMillis = it.date }
                    c.get(Calendar.MONTH) == cal.get(Calendar.MONTH) && c.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                }.sumOf { it.amount }

                // Month 1 (Earliest in window)
                cal.add(Calendar.MONTH, -1)
                val m1Name = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(cal.time)
                val m1Sum = allExpenses.filter {
                    val c = Calendar.getInstance().apply { timeInMillis = it.date }
                    c.get(Calendar.MONTH) == cal.get(Calendar.MONTH) && c.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                }.sumOf { it.amount }

                val maxVal = maxOf(m1Sum, m2Sum, m3Sum, 100.0)

                // Navigation Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { monthOffset-- },
                        modifier = Modifier
                            .size(36.dp)
                            .background(SleekBorder, CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Earlier Months", tint = SleekPrimary, modifier = Modifier.size(18.dp))
                    }

                    Text(
                        text = "Window: $m1Name - $m3Name",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { if (monthOffset < 0) monthOffset++ },
                        enabled = monthOffset < 0,
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (monthOffset < 0) SleekBorder else SleekBorder.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Later Months",
                            tint = if (monthOffset < 0) SleekPrimary else SleekTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Layout custom canvas bar graph
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val months = listOf(
                        Triple(m1Name.split(" ").first(), m1Sum, SleekPrimary.copy(alpha = 0.5f)),
                        Triple(m2Name.split(" ").first(), m2Sum, SleekPrimary.copy(alpha = 0.75f)),
                        Triple(m3Name.split(" ").first(), m3Sum, SleekPrimary)
                    )

                    months.forEach { (name, total, color) ->
                        val pct = (total / maxVal).toFloat()
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = String.format("₹%,.0f", total),
                                style = MaterialTheme.typography.labelSmall,
                                color = SleekTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(44.dp)
                                    .fillMaxHeight(pct.coerceAtLeast(0.01f))
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelMedium,
                                color = SleekTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4️⃣ AI ADVISOR COACH TAB (Gemini-powered)
// ==========================================
@Composable
fun AdvisorTab(viewModel: FinanceViewModel) {
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    var inputMessage by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    // Auto scroll chat to bottom when message list changes
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AI Financial Coach",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gemini-powered budget analyzer",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SleekPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Audit Generator Quick Action
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SleekPrimaryContainer),
            border = BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Instant Expense Audit",
                        style = MaterialTheme.typography.titleSmall,
                        color = SleekOnPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Get real-time feedback on your category limits.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekOnPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.sendChatMessage("Give me a comprehensive audit of my current category expenses and tell me how I can optimize my food and travel budgets.") },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Analyze", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chat Message Log
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages) { message ->
                ChatBubble(message = message)
            }

            if (isChatLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SleekPrimary, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat input container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                placeholder = { Text("Ask Coach about your expenses...", color = SleekTextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SleekPrimary,
                    unfocusedBorderColor = SleekBorder,
                    focusedContainerColor = SleekSurface,
                    unfocusedContainerColor = SleekSurface
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_coach_chat_input")
            )

            IconButton(
                onClick = {
                    if (inputMessage.isNotBlank()) {
                        viewModel.sendChatMessage(inputMessage)
                        inputMessage = ""
                        focusManager.clearFocus()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SleekPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val bubbleColor = if (message.isUser) SleekPrimary else SleekSurface
    val textColor = if (message.isUser) Color.White else SleekTextPrimary
    val align = if (message.isUser) Alignment.End else Alignment.Start
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 0.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Card(
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            border = if (!message.isUser) BorderStroke(1.dp, SleekBorder) else null,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

// ==========================================
// 5️⃣ ADD EXPENSE DIALOG
// ==========================================
@Composable
fun AddExpenseDialog(
    prefilledDate: Long?,
    categories: List<String>,
    onAddCategory: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, category: String, date: Long, note: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.firstOrNull() ?: "Food") }
    var note by remember { mutableStateOf("") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }

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
                    "Add New Expense",
                    style = MaterialTheme.typography.titleLarge,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )

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
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface,
                        focusedLabelColor = SleekPrimary,
                        unfocusedLabelColor = SleekTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selector
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { categoryDropdownExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekNeutralLight),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category, color = SleekTextPrimary, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SleekTextSecondary)
                        }
                    }

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false },
                        modifier = Modifier.background(SleekSurface)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = SleekTextPrimary, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    category = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                        HorizontalDivider(color = SleekBorder)
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("+ Add Custom...", color = SleekPrimary, fontWeight = FontWeight.Bold)
                                }
                            },
                            onClick = {
                                categoryDropdownExpanded = false
                                showCreateCategoryDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Note description input (Mandatory)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Description *", color = SleekTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface,
                        focusedLabelColor = SleekPrimary,
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
                                    note.trim()
                                )
                            }
                        },
                        enabled = note.trim().isNotEmpty() && (amountStr.toDoubleOrNull() ?: 0.0) > 0.0,
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
    onAddCategory: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Expense) -> Unit
) {
    var amountStr by remember { mutableStateOf(expense.amount.toString()) }
    var category by remember { mutableStateOf(expense.category) }
    var note by remember { mutableStateOf(expense.note ?: "") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }

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
                    "Edit Expense",
                    style = MaterialTheme.typography.titleLarge,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )

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
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface,
                        focusedLabelColor = SleekPrimary,
                        unfocusedLabelColor = SleekTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selector
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { categoryDropdownExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekNeutralLight),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category, color = SleekTextPrimary, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SleekTextSecondary)
                        }
                    }

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false },
                        modifier = Modifier.background(SleekSurface)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = SleekTextPrimary, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    category = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                        HorizontalDivider(color = SleekBorder)
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("+ Add Custom...", color = SleekPrimary, fontWeight = FontWeight.Bold)
                                }
                            },
                            onClick = {
                                categoryDropdownExpanded = false
                                showCreateCategoryDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Note description input (Mandatory)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Description *", color = SleekTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface,
                        focusedLabelColor = SleekPrimary,
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
                                        note = note.trim()
                                    )
                                )
                            }
                        },
                        enabled = note.trim().isNotEmpty() && (amountStr.toDoubleOrNull() ?: 0.0) > 0.0,
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
// 7️⃣ LIVE CALENDAR TAB
// ==========================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarTab(
    expenses: List<Expense>,
    onAddExpenseForDate: (Long) -> Unit,
    onEditExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    onAiCoachClick: () -> Unit
) {
    val today = Calendar.getInstance()
    var navigatedCalendar by remember { mutableStateOf(Calendar.getInstance()) }

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
                    text = "Live Ledger Calendar",
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

            IconButton(
                onClick = onAiCoachClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SleekPrimaryContainer)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Coach", tint = SleekPrimary, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth()
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
                                    val dayTotal = dayExpenses.sumOf { it.amount }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                when {
                                                    isSelected -> SleekPrimary
                                                    isToday -> SleekPrimaryContainer.copy(alpha = 0.5f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = when {
                                                    isSelected -> SleekPrimary
                                                    isToday -> SleekPrimary
                                                    else -> Color.Transparent
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .combinedClickable(
                                                onClick = { selectedDayOfMonth = dayNum },
                                                onDoubleClick = {
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
                                                    isToday -> SleekPrimary
                                                    else -> SleekTextPrimary
                                                }
                                            )
                                            if (dayTotal > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(top = 2.dp)
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) Color.White else SleekPrimary)
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
                text = "Expenses on $selectedDateStr",
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
                    text = "No expenses logged for this day.",
                    color = SleekTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                selectedDayExpenses.forEach { expense ->
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
                        val catColor = categoryColors[expense.category] ?: SleekPrimary
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(catColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(expense.category),
                                contentDescription = expense.category,
                                tint = catColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = expense.note ?: "Expense",
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
                            text = String.format("-₹%,.2f", expense.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExpenseRed,
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
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newCatName by remember { mutableStateOf("") }
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
                OutlinedTextField(
                    value = newCatName,
                    onValueChange = { newCatName = it },
                    label = { Text("Category Name", color = SleekTextSecondary) },
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
                            if (newCatName.trim().isNotEmpty()) {
                                onConfirm(newCatName.trim())
                            }
                        },
                        enabled = newCatName.trim().isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
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
fun SidebarDrawerContent(
    viewModel: FinanceViewModel,
    onCloseDrawer: () -> Unit
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val themeIndex by viewModel.themeIndex.collectAsStateWithLifecycle()
    val customHue by viewModel.customThemeHue.collectAsStateWithLifecycle()
    
    var showEditNameDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Header Row (Close Button)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onCloseDrawer) {
                Icon(Icons.Default.Close, contentDescription = "Close Drawer", tint = SleekTextSecondary)
            }
        }
        
        // 1. Name Profile Section
        val initials = if (!userName.isNullOrBlank()) {
            userName!!.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").take(2)
        } else "U"
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SleekPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleLarge,
                    color = SleekOnPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName ?: "Guest User",
                    style = MaterialTheme.typography.titleLarge,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Offline Ledger Account",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary
                )
            }
            IconButton(
                onClick = { showEditNameDialog = true },
                modifier = Modifier
                    .size(36.dp)
                    .background(SleekBorder.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Edit Name",
                    tint = SleekPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = SleekBorder)
        Spacer(modifier = Modifier.height(16.dp))
        
        // 2. Data and Storage Section
        Text(
            text = "Disk and network usage",
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFF1E3A8A), // Blue theme color header
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column {
                // Storage Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0E7FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Autorenew,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Storage Usage",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SleekTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "176.3 MB",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                HorizontalDivider(color = SleekBorder.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                
                // Data Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BarChart,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Data Usage",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SleekTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "10.35 GB",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF16A34A),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        HorizontalDivider(color = SleekBorder)
        Spacer(modifier = Modifier.height(16.dp))
        
        // 3. Colour Themes Section
        Text(
            text = "Select Theme Palette",
            style = MaterialTheme.typography.titleSmall,
            color = SleekPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        ColorThemeGrid(
            selectedThemeIndex = themeIndex,
            customHue = customHue,
            onThemeSelected = { index -> viewModel.updateTheme(index) }
        )
        
        if (themeIndex == 15) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Adjust Custom Hue",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "${customHue.toInt()}°",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = customHue,
                        onValueChange = { viewModel.updateCustomThemeHue(it) },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = SleekPrimary,
                            activeTrackColor = SleekPrimary,
                            inactiveTrackColor = SleekBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = SleekBorder)
        Spacer(modifier = Modifier.height(16.dp))
        
        // 4. FAQ & Help Section
        Text(
            text = "FAQ & Help",
            style = MaterialTheme.typography.titleSmall,
            color = SleekPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        FaqAccordion()
        
        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider(color = SleekBorder)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Footer: Version & Credits
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Version 1.2.0 (Offline Stable)",
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
                    text = "by Vivek with Love",
                    style = MaterialTheme.typography.labelMedium,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
    
    if (showEditNameDialog) {
        EditNameDialog(
            currentName = userName ?: "",
            onDismiss = { showEditNameDialog = false },
            onConfirm = {
                viewModel.saveUserName(it)
                showEditNameDialog = false
            }
        )
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
fun FaqAccordion() {
    val faqs = listOf(
        "Is my financial data secure?" to "Yes, all data is stored offline locally on your device and never uploaded to any servers.",
        "How do I set a monthly budget?" to "Click the pencil icon on the monthly card on the Dashboard to set your budget limit.",
        "What are custom categories?" to "Select '+ Add Custom...' in the Category dropdown when adding/editing an expense to add new categories.",
        "How do I delete or edit transactions?" to "On the Ledger tab, long press or tap on any expense row to select it, then use the floating actions bar to edit or delete."
    )
    
    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    
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
