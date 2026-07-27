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
                                .clickable { viewModel.updateLanguage(engName) }
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
    val context = LocalContext.current

    var exportTransactionType by remember { mutableStateOf("ALL") }
    var selectedExportCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    val dateSdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

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
                text = "Export Ledger Statements",
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
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Transaction Type",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val types = listOf(
                            "ALL" to "Full Ledger",
                            "INCOME" to "Income Only",
                            "EXPENSE" to "Expense Only"
                        )
                        types.forEach { (typeKey, label) ->
                            val isSelected = exportTransactionType == typeKey
                            val activeColor = when (typeKey) {
                                "INCOME" -> Color(0xFF10B981)
                                "EXPENSE" -> Color(0xFFEF4444)
                                else -> SleekPrimary
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) activeColor.copy(alpha = 0.18f) else SleekSurface)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) activeColor else SleekBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { exportTransactionType = typeKey }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) activeColor else SleekTextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Filter Categories",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekTextSecondary
                    )
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

            // Export Actions
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    val filteredToExport = remember(allExpensesList, exportTransactionType, selectedExportCategories) {
                        allExpensesList.filter { item ->
                            val matchesType = when (exportTransactionType) {
                                "INCOME" -> item.type == "INCOME"
                                "EXPENSE" -> item.type != "INCOME"
                                else -> true
                            }
                            val matchesCat = if (selectedExportCategories.isEmpty()) true else selectedExportCategories.contains(item.category)
                            matchesType && matchesCat
                        }
                    }

                    Text(
                        text = "Ready to Export: ${filteredToExport.size} Records",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val startCal = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                            }
                            DataExporter.exportToCSV(
                                context = context,
                                expenses = filteredToExport,
                                dateRangeStr = "All Time",
                                typeFilterStr = exportTransactionType,
                                categoryFilterStr = if (selectedExportCategories.isEmpty()) "All Categories" else selectedExportCategories.joinToString(", ")
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export CSV Ledger", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            DataExporter.sharePdfReport(
                                context = context,
                                expenses = filteredToExport,
                                dateRangeStr = "All Time",
                                typeFilterStr = exportTransactionType,
                                categoryFilterStr = if (selectedExportCategories.isEmpty()) "All Categories" else selectedExportCategories.joinToString(", ")
                            )
                        },
                        border = BorderStroke(1.dp, SleekPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = SleekPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Formatted PDF/Text Report", color = SleekPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
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
        }
    }
}
