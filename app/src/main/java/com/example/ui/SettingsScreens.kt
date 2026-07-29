package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class SettingsSubScreen {
    DataAndStorage,
    ThemeAndLanguage,
    Export,
    FaqAndHelp
}

// ==========================================
// 1️⃣ DATA & STORAGE SCREEN (Reference Image 2)
// ==========================================
@Composable
fun DataAndStorageScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val storageSize by viewModel.storageSize.collectAsStateWithLifecycle()
    val dataSize by viewModel.dataSize.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refreshUsageData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Surface(
            color = SleekSurface,
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, SleekBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = SleekTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Data and Storage",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Main Disk and Network Usage Card (Matching Reference Image 2)
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header text matching blue style from reference image 2
                    Text(
                        text = "Disk and network usage",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF0284C7), // Vibrant blue
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Row 1: Storage Usage (Blue Ring Icon)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB)), // Solid blue squircle/circle matching image
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Autorenew,
                                    contentDescription = "Storage",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = "Storage Usage",
                                style = MaterialTheme.typography.bodyLarge,
                                color = SleekTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = storageSize,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(
                        color = SleekBorder.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Row 2: Data Usage (Green Bar Chart Icon)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16A34A)), // Solid green squircle/circle matching image
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.BarChart,
                                    contentDescription = "Data Usage",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = "Data Usage",
                                style = MaterialTheme.typography.bodyLarge,
                                color = SleekTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = dataSize,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Storage Details & Maintenance Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Local Offline Storage",
                        style = MaterialTheme.typography.titleSmall,
                        color = SleekPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "All receipts, transactions, and categories are encrypted and cached locally on this device. No data is shared externally without your consent.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SleekTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.refreshUsageData()
                            Toast.makeText(context, "Storage and network usage refreshed", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = SleekOnPrimaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Refresh Disk Usage Stats", color = SleekOnPrimaryContainer, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(300.dp))
        }
    }
}

// ==========================================
// 2️⃣ THEME AND LANGUAGE SCREEN
// ==========================================
@Composable
fun ThemeAndLanguageScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val themeIndex by viewModel.themeIndex.collectAsStateWithLifecycle()
    val customHue by viewModel.customThemeHue.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    var darkThemeState by remember { mutableStateOf(isDarkModeActive) }
    val context = LocalContext.current

    val languages = listOf(
        Triple("English", "English", "🇬🇧"),
        Triple("Español", "Spanish", "🇪🇸"),
        Triple("हिंदी", "Hindi", "🇮🇳"),
        Triple("Français", "French", "🇫🇷"),
        Triple("Deutsch", "German", "🇩🇪"),
        Triple("日本語", "Japanese", "🇯🇵")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Surface(
            color = SleekSurface,
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, SleekBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = SleekTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Theme and Language",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Theme Section
            Text(
                text = "Appearance & Dark Mode",
                style = MaterialTheme.typography.titleSmall,
                color = SleekPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.toggleDarkMode()
                        darkThemeState = isDarkModeActive
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SleekPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (darkThemeState) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = SleekPrimary
                            )
                        }
                        Column {
                            Text(
                                text = "Dark Theme Mode",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Text(
                                text = if (darkThemeState) "Dark mode enabled" else "Light mode enabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = SleekTextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = darkThemeState,
                        onCheckedChange = {
                            viewModel.toggleDarkMode()
                            darkThemeState = isDarkModeActive
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SleekPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Color Palette Presets",
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
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Custom Spectrum Hue",
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

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = SleekBorder)
            Spacer(modifier = Modifier.height(20.dp))

            // Language Section
            Text(
                text = "Language Preference",
                style = MaterialTheme.typography.titleSmall,
                color = SleekPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    languages.forEachIndexed { index, (nativeName, engName, flag) ->
                        val isSelected = selectedLanguage.equals(engName, ignoreCase = true) || selectedLanguage.equals(nativeName, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SleekPrimaryContainer.copy(alpha = 0.25f) else Color.Transparent)
                                .clickable {
                                    viewModel.updateLanguage(engName)
                                    LanguageManager.applyAppLocale(context, engName)
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = flag, fontSize = 22.sp)
                                Column {
                                    Text(
                                        text = nativeName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = SleekTextPrimary
                                    )
                                    Text(
                                        text = engName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SleekTextSecondary
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = SleekPrimary
                                )
                            }
                        }

                        if (index < languages.size - 1) {
                            HorizontalDivider(
                                color = SleekBorder.copy(alpha = 0.4f),
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(300.dp))
        }
    }
}

// ==========================================
// 3️⃣ EXPORTING SCREEN
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportDataScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val allExpensesList by viewModel.expenses.collectAsStateWithLifecycle()
    val allCategoriesList by viewModel.allCategories.collectAsStateWithLifecycle()
    val savingsGoalsList by viewModel.savingsGoals.collectAsStateWithLifecycle()
    val monthlyBudget by viewModel.monthlyBudget.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val totalAmountSaved = remember(savingsGoalsList) { savingsGoalsList.sumOf { it.currentAmount } }
    val monthsOverBudgetCount = remember(allExpensesList, monthlyBudget) {
        if (monthlyBudget <= 0) 0
        else {
            allExpensesList.filter { it.type != "INCOME" }
                .groupBy {
                    val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                    "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
                }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
                .count { (_, monthTotal) -> monthTotal > monthlyBudget }
        }
    }

    // Export Scope Options: ANALYTICS_ONLY, ANALYTICS_AND_ALL, ALL, INCOME, EXPENSE
    var exportOption by remember { mutableStateOf("ANALYTICS_AND_ALL") }
    var selectedExportCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    val dateSdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var exportStartDate by remember {
        mutableStateOf(Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -30)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis)
    }
    var exportEndDate by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis)
    }
    var showDateRangeModal by remember { mutableStateOf(false) }

    if (showDateRangeModal) {
        DateRangeReportModalDialog(
            initialStartDate = exportStartDate,
            initialEndDate = exportEndDate,
            onDismiss = { showDateRangeModal = false },
            onConfirm = { selStart, selEnd ->
                exportStartDate = selStart
                exportEndDate = selEnd
                showDateRangeModal = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Surface(
            color = SleekSurface,
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, SleekBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = SleekTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Data Export & Reports",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Export Configuration",
                style = MaterialTheme.typography.titleSmall,
                color = SleekPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 1️⃣ EXPORT TYPE / SCOPE OPTION
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "1. Export Scope",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "Choose what financial data to include in your export",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val exportScopes = listOf(
                        "ANALYTICS_ONLY" to ("Analytics" to "Key KPI statistics, budgets, savings & category summary"),
                        "ANALYTICS_AND_ALL" to ("Analytics + Transactions" to "Comprehensive report with full statistics & complete ledger entries"),
                        "ALL" to ("Both Income and Expense" to "All income and expense transaction entries"),
                        "INCOME" to ("Only Income" to "Salary, freelance, investments & cash inflows"),
                        "EXPENSE" to ("Only Expense" to "Daily bills, shopping, purchases & cash outflows")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        exportScopes.forEach { (key, info) ->
                            val (title, description) = info
                            val isSelected = exportOption == key
                            val activeBorderColor = if (isSelected) SleekPrimary else SleekBorder
                            val activeBgColor = if (isSelected) SleekPrimaryContainer.copy(alpha = 0.25f) else SleekBg

                            Surface(
                                onClick = { exportOption = key },
                                shape = RoundedCornerShape(14.dp),
                                color = activeBgColor,
                                border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, activeBorderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { exportOption = key },
                                        colors = RadioButtonDefaults.colors(selectedColor = SleekPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = SleekTextPrimary
                                        )
                                        Text(
                                            text = description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SleekTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2️⃣ DATE RANGE & PRESETS (JUST BELOW EXPORT SCOPE)
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "2. Date Range Filter",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "Select time period for exported statements",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Start to End Day Custom Picker Button
                    Surface(
                        onClick = { showDateRangeModal = true },
                        shape = RoundedCornerShape(12.dp),
                        color = SleekBg,
                        border = BorderStroke(1.dp, SleekBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${dateSdf.format(Date(exportStartDate))} — ${dateSdf.format(Date(exportEndDate))}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekTextPrimary
                                    )
                                    Text(
                                        text = "Start to End Day (Tap to customize)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SleekPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Icon(Icons.Default.Edit, contentDescription = null, tint = SleekTextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Quick Presets:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Date Presets Row
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presetItems = listOf(
                            "Today" to 0,
                            "Last 7 days" to 7,
                            "This Month" to -1,
                            "Last 30 days" to 30,
                            "All Time" to -999
                        )

                        presetItems.forEach { (label, dayCode) ->
                            AssistChip(
                                onClick = {
                                    val now = Calendar.getInstance()
                                    now.set(Calendar.HOUR_OF_DAY, 23)
                                    now.set(Calendar.MINUTE, 59)
                                    now.set(Calendar.SECOND, 59)
                                    now.set(Calendar.MILLISECOND, 999)
                                    exportEndDate = now.timeInMillis

                                    val start = Calendar.getInstance()
                                    when (dayCode) {
                                        0 -> {
                                            start.set(Calendar.HOUR_OF_DAY, 0)
                                            start.set(Calendar.MINUTE, 0)
                                            start.set(Calendar.SECOND, 0)
                                            start.set(Calendar.MILLISECOND, 0)
                                        }
                                        -1 -> {
                                            start.set(Calendar.DAY_OF_MONTH, 1)
                                            start.set(Calendar.HOUR_OF_DAY, 0)
                                            start.set(Calendar.MINUTE, 0)
                                            start.set(Calendar.SECOND, 0)
                                            start.set(Calendar.MILLISECOND, 0)
                                        }
                                        -999 -> {
                                            start.set(2020, Calendar.JANUARY, 1, 0, 0, 0)
                                        }
                                        else -> {
                                            start.add(Calendar.DAY_OF_MONTH, -dayCode)
                                            start.set(Calendar.HOUR_OF_DAY, 0)
                                            start.set(Calendar.MINUTE, 0)
                                            start.set(Calendar.SECOND, 0)
                                            start.set(Calendar.MILLISECOND, 0)
                                        }
                                    }
                                    exportStartDate = start.timeInMillis
                                },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, SleekBorder)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3️⃣ CATEGORY FILTER CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3. Category Filter",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        if (selectedExportCategories.isNotEmpty()) {
                            TextButton(
                                onClick = { selectedExportCategories = emptySet() },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Clear Selection", fontSize = 11.sp, color = SleekPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allCategoriesList.forEach { cat ->
                            val isSelected = selectedExportCategories.contains(cat)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedExportCategories = if (isSelected) {
                                        selectedExportCategories - cat
                                    } else {
                                        selectedExportCategories + cat
                                    }
                                },
                                label = { Text(cat, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SleekPrimaryContainer,
                                    selectedLabelColor = SleekOnPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4️⃣ EXPORT ACTIONS (CSV, PDF, IMAGE)
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    val filteredToExport = remember(allExpensesList, exportOption, selectedExportCategories, exportStartDate, exportEndDate) {
                        val startCal = Calendar.getInstance().apply {
                            timeInMillis = exportStartDate
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val endCal = Calendar.getInstance().apply {
                            timeInMillis = exportEndDate
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }

                        allExpensesList.filter { item ->
                            val inDateRange = item.date in startCal.timeInMillis..endCal.timeInMillis
                            val matchesType = when (exportOption) {
                                "INCOME" -> item.type == "INCOME"
                                "EXPENSE" -> item.type != "INCOME"
                                else -> true
                            }
                            val matchesCat = if (selectedExportCategories.isEmpty()) true else selectedExportCategories.contains(item.category)
                            inDateRange && matchesType && matchesCat
                        }.sortedByDescending { it.date }
                    }

                    val dateRangeLabel = "${dateSdf.format(Date(exportStartDate))} - ${dateSdf.format(Date(exportEndDate))}"
                    val typeLabel = when (exportOption) {
                        "ANALYTICS_ONLY" -> "Analytics Summary"
                        "ANALYTICS_AND_ALL" -> "Analytics + Transactions"
                        "ALL" -> "Both Income & Expense"
                        "INCOME" -> "Only Income"
                        "EXPENSE" -> "Only Expense"
                        else -> "Custom Export"
                    }
                    val catLabel = if (selectedExportCategories.isEmpty()) "All Categories" else selectedExportCategories.joinToString(", ")

                    Text(
                        text = "4. Select Export Format",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "Ready to export ${filteredToExport.size} entries ($dateRangeLabel)",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // CSV Export Button
                    Button(
                        onClick = {
                            DataExporter.exportToCSV(
                                context = context,
                                expenses = filteredToExport,
                                dateRangeStr = dateRangeLabel,
                                typeFilterStr = typeLabel,
                                categoryFilterStr = catLabel
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export CSV File", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // PDF Export Button
                    OutlinedButton(
                        onClick = {
                            val includeTxns = exportOption != "ANALYTICS_ONLY"
                            DataExporter.sharePdfReport(
                                context = context,
                                expenses = filteredToExport,
                                dateRangeStr = dateRangeLabel,
                                typeFilterStr = typeLabel,
                                categoryFilterStr = catLabel,
                                amountSaved = totalAmountSaved,
                                monthsOverBudget = monthsOverBudgetCount,
                                includeDetailedTxns = includeTxns
                            )
                        },
                        border = BorderStroke(1.dp, SleekPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = SleekPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export PDF Document", color = SleekPrimary, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Image Export Button
                    OutlinedButton(
                        onClick = {
                            DataExporter.shareImageReport(
                                context = context,
                                expenses = filteredToExport,
                                dateRangeStr = dateRangeLabel,
                                typeFilterStr = typeLabel,
                                categoryFilterStr = catLabel
                            )
                        },
                        border = BorderStroke(1.dp, SleekBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Photo, contentDescription = null, tint = SleekTextPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Image Statement", color = SleekTextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(300.dp))
        }
    }
}

// ==========================================
// 4️⃣ FAQ & HELP SCREEN
// ==========================================
@Composable
fun FaqAndHelpScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val faqs = remember {
        listOf(
            "How is my total bank balance calculated?" to "Your available balance is calculated by taking your total logged Income transactions and subtracting all Expense transactions logged to date.",
            "Are my scanned receipts saved locally on device?" to "Yes! All camera and gallery uploaded receipt images are stored in your application's secure private directory. No photos leave your phone.",
            "How do I filter transactions by custom date range?" to "Go to the Dashboard tab or Calendar tab, tap the Date Range filter button, and select your custom start and end dates.",
            "Can I track both Income and Expense items?" to "Yes! When tapping the + button to add a transaction, toggle between 'Expense' and 'Income' to track salaries, freelance work, and investments.",
            "How do I export my transaction history?" to "Open the Settings drawer, tap 'Data Export & Reports', choose CSV or PDF format, and share or save your statement file.",
            "How do I customize theme colors?" to "Open Settings -> 'Theme and Language' to switch between Dark Mode and 16 vibrant color theme palettes."
        )
    }

    val filteredFaqs = remember(searchQuery) {
        if (searchQuery.isBlank()) faqs else faqs.filter {
            it.first.contains(searchQuery, ignoreCase = true) || it.second.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Surface(
            color = SleekSurface,
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, SleekBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = SleekTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FAQ & Help Support",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search help topics...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SleekTextSecondary) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SleekPrimary,
                    unfocusedBorderColor = SleekBorder,
                    focusedContainerColor = SleekSurface,
                    unfocusedContainerColor = SleekSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            filteredFaqs.forEach { (question, answer) ->
                var expanded by remember { mutableStateOf(false) }

                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { expanded = !expanded }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = SleekPrimary
                            )
                        }

                        AnimatedVisibility(visible = expanded) {
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = SleekBorder.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = answer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SleekTextSecondary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(300.dp))
        }
    }
}
