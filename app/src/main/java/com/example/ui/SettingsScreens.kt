package com.example.ui

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.theme.*

enum class SettingsSubScreen {
    PersonalData,
    Appearance,
    Language,
    Currency,
    DateTime,
    Bills,
    CategoriesTags,
    Budgets,
    SavingsGoals,
    Calculations,
    Transactions,
    BackupRestore,
    DataManagement,
    Security,
    Privacy,
    AboutApp,
    HelpSupport,
    DataAndStorage,
    ThemeAndLanguage,
    Export,
    FaqAndHelp
}

@Composable
fun SettingsHeaderTitle(title: String, onBack: () -> Unit) {
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary
            )
        }
    }
}

// ==========================================
// 0️⃣ PERSONAL DATA SCREEN
// ==========================================
@Composable
fun PersonalDataScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val currentName by viewModel.userName.collectAsStateWithLifecycle()
    val currentDob by viewModel.userDob.collectAsStateWithLifecycle()
    val currentJob by viewModel.userJob.collectAsStateWithLifecycle()
    val currentIncome by viewModel.userMonthlyIncome.collectAsStateWithLifecycle()
    val currentGender by viewModel.userGender.collectAsStateWithLifecycle()
    val profileImageUri by viewModel.userProfileImageUri.collectAsStateWithLifecycle()

    var nameText by remember { mutableStateOf(currentName ?: "William John Malik") }
    var dobText by remember { mutableStateOf(currentDob) }
    var jobText by remember { mutableStateOf(currentJob) }
    var incomeText by remember { mutableStateOf(currentIncome) }
    var genderOption by remember { mutableStateOf(currentGender) }

    val context = LocalContext.current
    var showPhotoOptionSheet by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateUserProfileImageUri(uri.toString())
            Toast.makeText(context, "Profile photo updated!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Personal Data", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture Circle
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .clickable { showPhotoOptionSheet = true }
            ) {
                if (!profileImageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(SleekPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = SleekPrimary,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Edit photo",
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                label = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = jobText,
                onValueChange = { jobText = it },
                label = { Text("Occupation / Job") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = dobText,
                onValueChange = { dobText = it },
                label = { Text("Date of Birth") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = incomeText,
                onValueChange = { incomeText = it },
                label = { Text("Income Range") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.saveUserProfile(nameText, dobText, jobText, incomeText, genderOption)
                    Toast.makeText(context, "Personal data saved successfully!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }

    if (showPhotoOptionSheet) {
        AlertDialog(
            onDismissRequest = { showPhotoOptionSheet = false },
            title = { Text("Change Profile Photo") },
            text = { Text("Select photo from device gallery.") },
            confirmButton = {
                TextButton(onClick = {
                    showPhotoOptionSheet = false
                    imagePickerLauncher.launch("image/*")
                }) { Text("Choose from Gallery") }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoOptionSheet = false }) { Text("Cancel") }
            }
        )
    }
}

// ==========================================
// 1️⃣ APPEARANCE SCREEN
// ==========================================
@Composable
fun AppearanceScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isFollowDeviceColors by viewModel.isFollowDeviceColors.collectAsStateWithLifecycle()
    val themeIndex by viewModel.themeIndex.collectAsStateWithLifecycle()
    val customThemeHue by viewModel.customThemeHue.collectAsStateWithLifecycle()
    val textSize by viewModel.textSizeOption.collectAsStateWithLifecycle()
    val isCompact by viewModel.isCompactLayout.collectAsStateWithLifecycle()
    val isAnim by viewModel.isAnimationEnabled.collectAsStateWithLifecycle()

    var showCustomHuePicker by remember { mutableStateOf(false) }
    var tempHue by remember(customThemeHue) { mutableStateOf(customThemeHue) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Appearance Settings", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // 1. Top Mode Selector Segmented Control (Light / Dark / Device)
            Surface(
                shape = CircleShape,
                color = SleekSurface,
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        Triple("light", "Light", Icons.Default.LightMode),
                        Triple("dark", "Dark", Icons.Default.DarkMode),
                        Triple("device", "Device", Icons.Default.Tv)
                    ).forEach { (modeKey, label, icon) ->
                        val isSelected = themeMode == modeKey
                        Surface(
                            onClick = { viewModel.updateThemeMode(modeKey) },
                            shape = CircleShape,
                            color = if (isSelected) Color(0xFF1967D2) else Color.Transparent,
                            contentColor = if (isSelected) Color.White else SleekTextPrimary,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = if (isSelected && modeKey == "light") Icons.Default.Check else icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. 4x4 Theme Swatches Grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (row in 0 until 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (col in 0 until 4) {
                            val index = row * 4 + col
                            val isSelected = themeIndex == index
                            val preview = getThemePalettePreview(index, customThemeHue)

                            Card(
                                onClick = {
                                    viewModel.updateTheme(index)
                                    if (index == 15) {
                                        showCustomHuePicker = true
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (com.example.ui.theme.isDarkModeActive) Color(0xFF23252B) else Color(0xFFF0F2F5)
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) Color(0xFF1967D2) else Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(76.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (index < 15) {
                                        Box(
                                            modifier = Modifier.size(46.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                drawArc(color = preview.topLeft, startAngle = 180f, sweepAngle = 90f, useCenter = true)
                                                drawArc(color = preview.topRight, startAngle = 270f, sweepAngle = 90f, useCenter = true)
                                                drawArc(color = preview.bottomRight, startAngle = 0f, sweepAngle = 90f, useCenter = true)
                                                drawArc(color = preview.bottomLeft, startAngle = 90f, sweepAngle = 90f, useCenter = true)
                                            }

                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .size(18.dp)
                                                        .background(Color(0xFF1967D2), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier.size(46.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Custom Hue",
                                                tint = SleekTextPrimary,
                                                modifier = Modifier.size(22.dp)
                                            )

                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .size(18.dp)
                                                        .background(Color(0xFF1967D2), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
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
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = SleekBorder)
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Follow Device Colors Switch Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Follow device colors",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SleekTextPrimary
                )
                Switch(
                    checked = isFollowDeviceColors,
                    onCheckedChange = { viewModel.toggleFollowDeviceColors(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF1967D2)
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text("TEXT SIZE PREFERENCE", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Small", "Medium", "Large").forEach { option ->
                    val selected = textSize == option
                    OutlinedButton(
                        onClick = { viewModel.updateTextSizeOption(option) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selected) SleekPrimary.copy(alpha = 0.15f) else SleekSurface,
                            contentColor = if (selected) SleekPrimary else SleekTextPrimary
                        ),
                        border = BorderStroke(1.dp, if (selected) SleekPrimary else SleekBorder)
                    ) {
                        Text(option, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text("LAYOUT & ANIMATIONS", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

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
                        Column {
                            Text("Compact Dashboard Layout", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Displays tighter card padding for more data visibility", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = isCompact, onCheckedChange = { viewModel.toggleCompactLayout(it) })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SleekBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Enable Fluid Animations", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Smooth transitions and fluid budget indicators", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = isAnim, onCheckedChange = { viewModel.toggleAnimationEnabled(it) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }

    if (showCustomHuePicker) {
        AlertDialog(
            onDismissRequest = { showCustomHuePicker = false },
            title = { Text("Custom Theme Color Accent", color = SleekTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.hsv(tempHue, 0.75f, 0.65f), CircleShape)
                            .border(1.dp, SleekBorder, CircleShape)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Hue: ${tempHue.toInt()}°", color = SleekTextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = tempHue,
                        onValueChange = {
                            tempHue = it
                            viewModel.updateCustomThemeHue(it)
                        },
                        valueRange = 0f..360f
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCustomHuePicker = false }) {
                    Text("Done", color = SleekPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ==========================================
// 2️⃣ LANGUAGE SCREEN
// ==========================================
@Composable
fun LanguageScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    var searchLanguage by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filteredLangs = remember(searchLanguage) {
        LanguageManager.supportedLanguages.filter {
            it.nativeName.contains(searchLanguage, ignoreCase = true) ||
            it.engName.contains(searchLanguage, ignoreCase = true) ||
            it.code.contains(searchLanguage, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Language Settings", onBack)

        Column(modifier = Modifier.padding(20.dp)) {
            OutlinedTextField(
                value = searchLanguage,
                onValueChange = { searchLanguage = it },
                placeholder = { Text("Search language...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SleekTextSecondary) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredLangs) { lang ->
                    val isSelected = selectedLanguage.equals(lang.nativeName, ignoreCase = true) || selectedLanguage.equals(lang.engName, ignoreCase = true)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) SleekPrimary.copy(alpha = 0.12f) else SleekSurface),
                        border = BorderStroke(1.dp, if (isSelected) SleekPrimary else SleekBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.updateLanguage(lang.nativeName)
                                Toast.makeText(context, "Language changed to ${lang.nativeName}", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(lang.flag, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(lang.nativeName, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                                    Text(lang.engName, style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                                }
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.updateLanguage(lang.nativeName) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3️⃣ CURRENCY SCREEN
// ==========================================
@Composable
fun CurrencyScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val selectedCode by viewModel.selectedCurrencyCode.collectAsStateWithLifecycle()
    val selectedSymbol by viewModel.selectedCurrencySymbol.collectAsStateWithLifecycle()
    val selectedName by viewModel.selectedCurrencyName.collectAsStateWithLifecycle()
    val lastUpdate by viewModel.lastExchangeRateUpdate.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var convertExistingOption by remember { mutableStateOf(false) } // False = Option A (Keep), True = Option B (Convert)
    var showConvertDialog by remember { mutableStateOf<CurrencyItem?>(null) }

    val filteredCurrencies = remember(searchQuery) {
        CurrencyManager.currencies.filter {
            it.country.contains(searchQuery, ignoreCase = true) ||
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.code.contains(searchQuery, ignoreCase = true) ||
            it.symbol.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Currency Settings", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("CURRENT DEFAULT CURRENCY", style = MaterialTheme.typography.labelSmall, color = SleekTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$selectedName ($selectedCode)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                    Text("Symbol: $selectedSymbol", style = MaterialTheme.typography.bodyMedium, color = SleekPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = SleekBorder)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Exchange Rates Cache: $lastUpdate", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.refreshExchangeRates() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Refresh Cached Exchange Rates")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("CONVERSION BEHAVIOR WHEN CHANGING CURRENCY", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { convertExistingOption = false },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = !convertExistingOption, onClick = { convertExistingOption = false })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Option A — Keep Existing Transactions", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Existing transactions keep their stored currency values.", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { convertExistingOption = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = convertExistingOption, onClick = { convertExistingOption = true })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Option B — Convert Existing Transactions", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Converts existing stored transactions using real exchange rates safely.", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("SEARCH & SELECT DEFAULT CURRENCY (100+ AVAILABLE)", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by country, name, code (e.g. India, INR, ₹)...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SleekTextSecondary) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            filteredCurrencies.take(30).forEach { item ->
                val isSelected = item.code == selectedCode
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) SleekPrimary.copy(alpha = 0.12f) else SleekSurface),
                    border = BorderStroke(1.dp, if (isSelected) SleekPrimary else SleekBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable {
                            if (convertExistingOption && !isSelected) {
                                showConvertDialog = item
                            } else {
                                viewModel.updateDefaultCurrency(item.code, item.symbol, item.name, false)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${item.country} (${item.code})", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("${item.name} • Symbol: ${item.symbol}", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        RadioButton(selected = isSelected, onClick = {
                            if (convertExistingOption && !isSelected) {
                                showConvertDialog = item
                            } else {
                                viewModel.updateDefaultCurrency(item.code, item.symbol, item.name, false)
                            }
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }

    val dialogTarget = showConvertDialog
    if (dialogTarget != null) {
        AlertDialog(
            onDismissRequest = { showConvertDialog = null },
            title = { Text("Convert Existing Transactions?") },
            text = { Text("Are you sure you want to convert all existing transaction values from $selectedCode to ${dialogTarget.code} using the exchange rate?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateDefaultCurrency(dialogTarget.code, dialogTarget.symbol, dialogTarget.name, true)
                    showConvertDialog = null
                }) { Text("Convert & Save") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConvertDialog = null }) { Text("Cancel") }
            }
        )
    }
}

// ==========================================
// 4️⃣ DATE & TIME SCREEN
// ==========================================
@Composable
fun DateTimeScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val dateFormat by viewModel.dateFormat.collectAsStateWithLifecycle()
    val firstDay by viewModel.firstDayOfWeek.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Date & Time Settings", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("DATE FORMAT", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            listOf("dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd").forEach { fmt ->
                val selected = dateFormat == fmt
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (selected) SleekPrimary.copy(alpha = 0.12f) else SleekSurface),
                    border = BorderStroke(1.dp, if (selected) SleekPrimary else SleekBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { viewModel.updateDateFormat(fmt) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(fmt, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        RadioButton(selected = selected, onClick = { viewModel.updateDateFormat(fmt) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("FIRST DAY OF THE WEEK", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            listOf("Monday", "Sunday", "Saturday").forEach { day ->
                val selected = firstDay == day
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (selected) SleekPrimary.copy(alpha = 0.12f) else SleekSurface),
                    border = BorderStroke(1.dp, if (selected) SleekPrimary else SleekBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { viewModel.updateFirstDayOfWeek(day) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(day, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        RadioButton(selected = selected, onClick = { viewModel.updateFirstDayOfWeek(day) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

// ==========================================
// 5️⃣ BILLS & REMINDERS SCREEN
// ==========================================
@Composable
fun BillsSettingsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val reminderTiming by viewModel.billReminderTiming.collectAsStateWithLifecycle()
    val autoMarkPaid by viewModel.billAutoMarkPaid.collectAsStateWithLifecycle()
    val overdueAlert by viewModel.billOverdueAlert.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Bills & Reminders Settings", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("DEFAULT REMINDER TIMING", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            listOf("Same Day", "1 Day Before", "2 Days Before", "1 Week Before").forEach { option ->
                val selected = reminderTiming == option
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (selected) SleekPrimary.copy(alpha = 0.12f) else SleekSurface),
                    border = BorderStroke(1.dp, if (selected) SleekPrimary else SleekBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { viewModel.updateBillReminderTiming(option) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(option, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        RadioButton(selected = selected, onClick = { viewModel.updateBillReminderTiming(option) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("AUTOMATION & ALERTS", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

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
                        Column {
                            Text("Overdue Bill Notifications", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Receive persistent warning for unpaid overdue bills", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = overdueAlert, onCheckedChange = { viewModel.toggleBillOverdueAlert(it) })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SleekBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto Mark Paid on Due Date", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Automatically deduct recurring bills from main account on due date", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = autoMarkPaid, onCheckedChange = { viewModel.toggleBillAutoMarkPaid(it) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

// ==========================================
// 6️⃣ CATEGORIES & TAGS SCREEN
// ==========================================
@Composable
fun CategoriesTagsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val customCategories by viewModel.customCategories.collectAsStateWithLifecycle()
    var newCatName by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Categories & Tags", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("ADD NEW CATEGORY", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newCatName,
                    onValueChange = { newCatName = it },
                    placeholder = { Text("Category name...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newCatName.isNotBlank()) {
                            viewModel.addCustomCategory(newCatName.trim())
                            newCatName = ""
                            Toast.makeText(context, "Category added!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                ) {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("MANAGED CUSTOM CATEGORIES", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            customCategories.forEach { cat ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = SleekPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(cat, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        }
                        IconButton(onClick = {
                            viewModel.deleteCustomCategory(cat)
                            Toast.makeText(context, "Deleted '$cat'. Associated transactions reassigned safely.", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

// ==========================================
// 7️⃣ BUDGET SETTINGS SCREEN
// ==========================================
@Composable
fun BudgetSettingsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val warn80 by viewModel.budgetWarning80.collectAsStateWithLifecycle()
    val warn90 by viewModel.budgetWarning90.collectAsStateWithLifecycle()
    val warn100 by viewModel.budgetWarning100.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Budget Settings", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("BUDGET WARNING INDICATORS & ALERTS", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

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
                        Column {
                            Text("80% Budget Threshold Indicator (Yellow)", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Show caution warning when spending hits 80%", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = warn80, onCheckedChange = { viewModel.toggleBudgetWarning(80, it) })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SleekBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("90% Critical Threshold Indicator (Orange)", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Show critical warning when spending hits 90%", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = warn90, onCheckedChange = { viewModel.toggleBudgetWarning(90, it) })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SleekBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("100% Exceeded Alert (Red)", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Show high-priority red alert when budget is exceeded", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = warn100, onCheckedChange = { viewModel.toggleBudgetWarning(100, it) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

// ==========================================
// 8️⃣ SAVINGS GOALS SETTINGS SCREEN
// ==========================================
@Composable
fun SavingsGoalsSettingsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val viewMode by viewModel.goalViewMode.collectAsStateWithLifecycle()
    val progressStyle by viewModel.goalProgressStyle.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Savings Goals Settings", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("DISPLAY VIEW MODE", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Grid", "List").forEach { mode ->
                    val selected = viewMode == mode
                    OutlinedButton(
                        onClick = { viewModel.updateGoalViewMode(mode) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selected) SleekPrimary.copy(alpha = 0.15f) else SleekSurface,
                            contentColor = if (selected) SleekPrimary else SleekTextPrimary
                        ),
                        border = BorderStroke(1.dp, if (selected) SleekPrimary else SleekBorder)
                    ) {
                        Text(mode, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("PROGRESS VISUALIZATION STYLE", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            listOf("Circle", "Linear Bar").forEach { style ->
                val selected = progressStyle == style
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (selected) SleekPrimary.copy(alpha = 0.12f) else SleekSurface),
                    border = BorderStroke(1.dp, if (selected) SleekPrimary else SleekBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { viewModel.updateGoalProgressStyle(style) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(style, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        RadioButton(selected = selected, onClick = { viewModel.updateGoalProgressStyle(style) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

// ==========================================
// 9️⃣ CALCULATIONS & FINANCIAL TOOLS SCREEN
// ==========================================
@Composable
fun CalculationsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    var valA by remember { mutableStateOf("1000") }
    var valB by remember { mutableStateOf("18") }
    var calcResult by remember { mutableStateOf("Result: ₹1,180.00 (with 18% GST)") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Calculations & Financial Tools", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("QUICK PERCENTAGE & GST CALCULATOR", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = valA,
                        onValueChange = { valA = it },
                        label = { Text("Base Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = valB,
                        onValueChange = { valB = it },
                        label = { Text("Percentage / Rate (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val a = valA.toDoubleOrNull() ?: 0.0
                                val b = valB.toDoubleOrNull() ?: 0.0
                                val gst = a * (b / 100.0)
                                calcResult = "Result: ₹%,.2f (Tax: ₹%,.2f)".format(a + gst, gst)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Add % (GST)")
                        }

                        Button(
                            onClick = {
                                val a = valA.toDoubleOrNull() ?: 0.0
                                val b = valB.toDoubleOrNull() ?: 0.0
                                val diff = a * (b / 100.0)
                                calcResult = "Result: ₹%,.2f (Discount: ₹%,.2f)".format(a - diff, diff)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Subtract %")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(calcResult, fontWeight = FontWeight.Bold, color = SleekPrimary, style = MaterialTheme.typography.titleSmall)
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

// ==========================================
// 🔟 TRANSACTIONS SETTINGS SCREEN
// ==========================================
@Composable
fun TransactionsSettingsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val defaultType by viewModel.defaultTxType.collectAsStateWithLifecycle()
    val rememberCat by viewModel.rememberLastCategory.collectAsStateWithLifecycle()
    val confirmDelete by viewModel.confirmTxDelete.collectAsStateWithLifecycle()
    val groupByDate by viewModel.groupByDate.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Transaction Settings", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("DEFAULT TRANSACTION TYPE", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("EXPENSE", "INCOME").forEach { type ->
                    val selected = defaultType == type
                    OutlinedButton(
                        onClick = { viewModel.updateDefaultTxType(type) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selected) SleekPrimary.copy(alpha = 0.15f) else SleekSurface,
                            contentColor = if (selected) SleekPrimary else SleekTextPrimary
                        ),
                        border = BorderStroke(1.dp, if (selected) SleekPrimary else SleekBorder)
                    ) {
                        Text(type, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("BEHAVIOR & SAFETY PREFERENCES", style = MaterialTheme.typography.labelMedium, color = SleekTextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

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
                        Column {
                            Text("Remember Last Selected Category", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Auto-select last category when creating new transaction", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = rememberCat, onCheckedChange = { viewModel.toggleRememberLastCategory(it) })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SleekBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Confirm Before Delete", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Show dialog confirmation before deleting transactions", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = confirmDelete, onCheckedChange = { viewModel.toggleConfirmTxDelete(it) })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SleekBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Group Transactions by Date", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Display dates headers in history feed", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = groupByDate, onCheckedChange = { viewModel.toggleGroupByDate(it) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

// ==========================================
// 1️⃣1️⃣ BACKUP & RESTORE SCREEN
// ==========================================
@Composable
fun BackupRestoreScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val autoBackup by viewModel.autoBackupEnabled.collectAsStateWithLifecycle()
    val lastBackup by viewModel.lastBackupTimestamp.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Backup & Restore", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("LOCAL OFFLINE BACKUP", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                    Text("Your financial database is backed up safely on device storage.", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Last Backup: $lastBackup", style = MaterialTheme.typography.bodySmall, color = SleekPrimary)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.markBackupPerformed()
                            Toast.makeText(context, "Local backup created successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Manual Backup Now")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Automatic Local Daily Sync", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        Text("Saves local database snapshots daily without cloud dependency", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                    }
                    Switch(checked = autoBackup, onCheckedChange = { viewModel.toggleAutoBackupEnabled(it) })
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

// ==========================================
// 1️⃣2️⃣ DATA MANAGEMENT SCREEN
// ==========================================
@Composable
fun DataManagementScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val storageSize by viewModel.storageSize.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Data Management", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("LOCAL STORAGE USAGE", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                    Text("Total database size: $storageSize", style = MaterialTheme.typography.bodyMedium, color = SleekPrimary)

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.refreshUsageData()
                            Toast.makeText(context, "Cache cleared successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Clear Temporary Cache")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DESTRUCTIVE ZONE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Text("Resetting data will clear custom categories and settings.", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset Application Preferences", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Preferences?") },
            text = { Text("Are you sure you want to reset all custom app settings to default?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showResetDialog = false
                        Toast.makeText(context, "Preferences reset.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Reset") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ==========================================
// 1️⃣3️⃣ SECURITY SETTINGS SCREEN
// ==========================================
@Composable
fun SecuritySettingsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val appPin by viewModel.appPin.collectAsStateWithLifecycle()
    val lockOnRestart by viewModel.lockOnRestart.collectAsStateWithLifecycle()
    val biometricEnabled by viewModel.biometricEnabled.collectAsStateWithLifecycle()
    val hideAmounts by viewModel.hideSensitiveAmounts.collectAsStateWithLifecycle()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Security Settings", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PASSCODE PIN LOCK", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                    Text(if (!appPin.isNullOrBlank()) "Passcode PIN Status: ACTIVE 🔒" else "Passcode PIN Status: DISABLED 🔓", style = MaterialTheme.typography.bodySmall, color = SleekPrimary)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showPinDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (appPin.isNullOrBlank()) "Set PIN" else "Change PIN")
                        }

                        if (!appPin.isNullOrBlank()) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.setAppPin(null)
                                    Toast.makeText(context, "PIN Passcode removed.", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Remove PIN")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                        Column {
                            Text("Lock Immediately on App Restart", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Requires PIN whenever application reopens", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = lockOnRestart, onCheckedChange = { viewModel.toggleLockOnRestart(it) })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SleekBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Biometric / Fingerprint Unlock", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Use system biometric authentication", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = biometricEnabled, onCheckedChange = { viewModel.toggleBiometricEnabled(it) })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SleekBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Hide Sensitive Balances on Launch", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("Blurs total balance card by default", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                        }
                        Switch(checked = hideAmounts, onCheckedChange = { viewModel.toggleHideSensitiveAmounts(it) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set 4-Digit Security PIN") },
            text = {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 4) pinInput = it },
                    label = { Text("Enter 4 digits") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (pinInput.length == 4) {
                        viewModel.setAppPin(pinInput)
                        showPinDialog = false
                        pinInput = ""
                        Toast.makeText(context, "PIN Passcode set successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please enter exactly 4 digits", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Save PIN") }
            },
            dismissButton = { OutlinedButton(onClick = { showPinDialog = false }) { Text("Cancel") } }
        )
    }
}

// ==========================================
// 1️⃣4️⃣ PRIVACY SETTINGS SCREEN
// ==========================================
@Composable
fun PrivacySettingsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val privacyMode by viewModel.privacyModeEnabled.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Privacy Settings", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("100% LOCAL & OFFLINE GUARANTEE", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("All your financial transactions, account balances, and savings goals remain entirely on your local device. No sensitive finance data is ever transmitted to external servers.", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary, lineHeight = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Privacy Blur Mode", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        Text("Mask financial numbers when sharing screens", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                    }
                    Switch(checked = privacyMode, onCheckedChange = { viewModel.togglePrivacyModeEnabled(it) })
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

// ==========================================
// 1️⃣5️⃣ ABOUT APP SCREEN
// ==========================================
@Composable
fun AboutAppScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("About App", onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Finance Tracker Pro", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
            Text("Version 1.2.0 • Build 2026.08", style = MaterialTheme.typography.bodyMedium, color = SleekTextSecondary)

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("WHAT'S NEW IN THIS RELEASE", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Fully independent settings screens for every sidebar menu item", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                    Text("• 100+ Currency options with Option A/B conversion behavior", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                    Text("• Complete 9-language translation support including RTL for Urdu", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                    Text("• Dedicated Bills & Reminders configuration & category icon manager", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                    Text("• Fluid budget indicator bar with real-time green/red fill", style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

// ==========================================
// 1️⃣6️⃣ HELP & SUPPORT SCREEN
// ==========================================
@Composable
fun HelpSupportScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val faqs = remember {
        listOf(
            "How do I change the default currency?" to "Go to Sidebar -> Currency, search for your currency, and choose Option A (Keep existing) or Option B (Convert existing transactions using real rates).",
            "Are my financial transactions safe?" to "Yes! Your entire financial database is stored 100% locally on your device with local backup & restore capabilities.",
            "How do I manage my monthly budgets?" to "Go to Sidebar -> Budgets or the Home screen budget card to configure monthly limits and warning thresholds.",
            "How do I lock the app with a PIN?" to "Navigate to Sidebar -> Security and tap 'Set PIN' to create a 4-digit security code.",
            "How do I switch the application language?" to "Navigate to Sidebar -> Language and select your language. It instantly updates text across all screens."
        )
    }

    val filtered = remember(searchQuery) {
        faqs.filter { it.first.contains(searchQuery, ignoreCase = true) || it.second.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeaderTitle("Help & Support Center", onBack)

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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            filtered.forEach { (question, answer) ->
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
                            Text(question, fontWeight = FontWeight.Bold, color = SleekTextPrimary, modifier = Modifier.weight(1f))
                            Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = SleekPrimary)
                        }

                        AnimatedVisibility(visible = expanded) {
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = SleekBorder.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(answer, style = MaterialTheme.typography.bodySmall, color = SleekTextSecondary, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

// Keep legacy screen aliases for backwards compatibility
@Composable fun DataAndStorageScreen(viewModel: FinanceViewModel, onBack: () -> Unit) { DataManagementScreen(viewModel, onBack) }
@Composable fun ThemeAndLanguageScreen(viewModel: FinanceViewModel, onBack: () -> Unit) { AppearanceScreen(viewModel, onBack) }
@Composable fun ExportDataScreen(viewModel: FinanceViewModel, onBack: () -> Unit) { DataManagementScreen(viewModel, onBack) }
@Composable fun FaqAndHelpScreen(viewModel: FinanceViewModel, onBack: () -> Unit) { HelpSupportScreen(viewModel, onBack) }
