package com.example.ui

import android.net.Uri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.Account
import com.example.data.SavingsGoal
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// 🎯 FULL SAVING GOALS SCREEN (MATCHING REFERENCE IMAGE)
// ==========================================
@Composable
fun SavingGoalsFullScreen(
    viewModel: FinanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val savingsGoals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var selectedGoalForDeposit by remember { mutableStateOf<SavingsGoal?>(null) }

    // Active Coin Deposit Animation state
    var activeDepositAnimationInfo by remember { mutableStateOf<Pair<Double, String>?>(null) }

    BackHandler { onBack() }

    // Calculate Total Savings
    val totalSaving = remember(savingsGoals) { savingsGoals.sumOf { g: SavingsGoal -> g.currentAmount } }

    // Category Tabs list
    val categories: List<String> = remember(savingsGoals) {
        val base = listOf("All", "Saving", "Investment", "Expenditure")
        val custom = savingsGoals.map { g: SavingsGoal -> g.category }.distinct().filter { c: String -> c !in base }
        base + custom
    }

    // Filtered goals grid
    val filteredGoals: List<SavingsGoal> = remember(savingsGoals, selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") {
            savingsGoals
        } else {
            savingsGoals.filter { g: SavingsGoal -> g.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Header Bar: Back | Title ("Saving Goals") | More Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = SleekTextPrimary
                    )
                }

                Text(
                    text = "Saving Goals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = SleekTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Centered Total Saving Display
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Saving",
                    fontSize = 14.sp,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹%,.2f".format(totalSaving),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = SleekTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 3. Section Title "My Saving" & "+ Add New" Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Saving",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )

                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showAddDialog = true
                    },
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, SleekPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add New",
                        color = SleekPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Category Filter Pills (All, Saving, Investment, Expenditure)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories, key = { cat: String -> cat }) { cat: String ->
                    val isSelected = selectedCategoryFilter.equals(cat, ignoreCase = true)
                    val bgColor = if (isSelected) SleekPrimary else SleekSurface
                    val textColor = if (isSelected) Color.White else SleekTextSecondary
                    val borderColor = if (isSelected) SleekPrimary else SleekBorder

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedCategoryFilter = cat
                            }
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. 2-Column Grid of Goal Cards
            if (filteredGoals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SleekPrimary.copy(alpha = 0.1f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Savings,
                                    contentDescription = null,
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (savingsGoals.isEmpty()) "No Saving Goals Created" else "No Goals in $selectedCategoryFilter",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap '+ Add New' to set up your target saving goal!",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredGoals, key = { goal: SavingsGoal -> goal.id }) { goal: SavingsGoal ->
                        SavingGoalCardItem(
                            goal = goal,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedGoalForDeposit = goal
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Add / Edit Goal Dialog
        if (showAddDialog || editingGoal != null) {
            AddEditSavingsGoalDialog(
                goalToEdit = editingGoal,
                onDismiss = {
                    showAddDialog = false
                    editingGoal = null
                },
                onSave = { title, target, category, imageUri, targetDate ->
                    if (editingGoal != null) {
                        viewModel.updateSavingsGoal(
                            editingGoal!!.copy(
                                name = title,
                                targetAmount = target,
                                category = category,
                                imageUri = imageUri,
                                targetDate = targetDate
                            )
                        )
                    } else {
                        viewModel.addSavingsGoal(
                            name = title,
                            targetAmount = target,
                            initialAmount = 0.0,
                            targetDate = targetDate,
                            category = category,
                            imageUri = imageUri
                        )
                    }
                    showAddDialog = false
                    editingGoal = null
                }
            )
        }

        // Goal Detail & Quick Deposit Sheet
        selectedGoalForDeposit?.let { goal ->
            GoalDepositDetailBottomSheet(
                goal = goal,
                onDismiss = { selectedGoalForDeposit = null },
                onDeposit = { amount ->
                    viewModel.quickDepositToGoal(goal, amount)
                    selectedGoalForDeposit = null
                    // Trigger coin rolling animation!
                    activeDepositAnimationInfo = Pair(amount, goal.name)
                },
                onDeduct = { amount ->
                    viewModel.quickDeductFromGoal(goal, amount)
                    selectedGoalForDeposit = null
                },
                onEdit = {
                    editingGoal = goal
                    selectedGoalForDeposit = null
                },
                onDelete = {
                    viewModel.deleteSavingsGoal(goal)
                    selectedGoalForDeposit = null
                }
            )
        }

        // Coin Deposit Animation Overlay (Video matched)
        activeDepositAnimationInfo?.let { (amt, name) ->
            CoinDepositAnimationDialog(
                amount = amt,
                goalName = name,
                onDismiss = { activeDepositAnimationInfo = null }
            )
        }
    }
}

// ==========================================
// 🎨 GOAL CARD ITEM (MATCHING REFERENCE IMAGE GRID)
// ==========================================
@Composable
fun SavingGoalCardItem(
    goal: SavingsGoal,
    onClick: () -> Unit
) {
    val progressRatio = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio.coerceIn(0f, 1f),
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "goalProgress"
    )

    val formattedDate = remember(goal.targetDate) {
        if (goal.targetDate > 0) {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(goal.targetDate))
        } else {
            "Open Goal"
        }
    }

    val primaryAccent = getCategoryAccentColor(goal.category)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("savings_goal_item_${goal.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Circular Avatar Badge with surrounding Progress Ring Arc
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Track Ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = primaryAccent.copy(alpha = 0.15f),
                        style = Stroke(width = 4.dp.toPx())
                    )
                    drawArc(
                        color = primaryAccent,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner Avatar Image or Category Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(primaryAccent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!goal.imageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = goal.imageUri,
                            contentDescription = goal.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = getCategoryVectorIcon(goal.category, goal.name),
                            contentDescription = goal.category,
                            tint = primaryAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Goal Title
            Text(
                text = goal.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Target Date Subtitle
            Text(
                text = formattedDate,
                fontSize = 11.sp,
                color = SleekTextSecondary
            )

            // Goal Saved vs Target Amount Row
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "₹%,.0f".format(goal.currentAmount),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryAccent
                )
                Text(
                    text = " of ₹%,.0f".format(goal.targetAmount),
                    fontSize = 11.sp,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

// ==========================================
// 🪙 COIN DEPOSIT ANIMATION DIALOG (VIDEO MATCHED)
// ==========================================
@Composable
fun CoinDepositAnimationDialog(
    amount: Double,
    goalName: String,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var animPhase by remember { mutableIntStateOf(0) } // 0: Rolling, 1: Arcing, 2: Dropped into Wallet Slot

    val rollX by animateFloatAsState(
        targetValue = if (animPhase >= 1) 0f else -130f,
        animationSpec = tween(550, easing = FastOutSlowInEasing),
        label = "rollX"
    )

    val rollRotation by animateFloatAsState(
        targetValue = if (animPhase >= 1) 720f else 0f,
        animationSpec = tween(550, easing = FastOutSlowInEasing),
        label = "rollRotation"
    )

    val coinY by animateFloatAsState(
        targetValue = when (animPhase) {
            0 -> 0f
            1 -> -80f
            else -> 18f
        },
        animationSpec = tween(if (animPhase == 1) 380 else 420, easing = FastOutSlowInEasing),
        label = "coinY"
    )

    val coinScale by animateFloatAsState(
        targetValue = when (animPhase) {
            0 -> 1f
            1 -> 1.15f
            else -> 0.40f
        },
        animationSpec = tween(420),
        label = "coinScale"
    )

    val cardScale by animateFloatAsState(
        targetValue = if (animPhase == 2) 1.08f else 1f,
        animationSpec = tween(300),
        label = "cardScale"
    )

    val cardGlowAlpha by animateFloatAsState(
        targetValue = if (animPhase == 2) 0.6f else 0f,
        animationSpec = tween(400),
        label = "cardGlowAlpha"
    )

    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        delay(120)
        animPhase = 1
        delay(420)
        animPhase = 2
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        delay(1800)
        onDismiss()
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Depositing to $goalName",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextSecondary
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Animation Canvas Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Horizontal rolling track line
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(2.dp)
                                .align(Alignment.Center)
                        ) {
                            drawLine(
                                color = SleekBorder,
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                                strokeWidth = 2.dp.toPx()
                            )
                        }

                        // Wallet / Card Slot Container
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(y = 20.dp)
                                .scale(cardScale)
                                .width(180.dp)
                                .height(105.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF7C3AED), Color(0xFF4C1D95))
                                    )
                                )
                                .border(
                                    width = 2.dp,
                                    color = Color(0xFFA78BFA).copy(alpha = cardGlowAlpha + 0.4f),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            // Slot Opening Line
                            Box(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .width(80.dp)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF2E1065))
                            )

                            // Subtle Card Icon / Brand Tag
                            Text(
                                text = "FS",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White.copy(alpha = 0.35f),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        // Animated Gold Coin with INR Symbol (₹)
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(x = rollX.dp, y = coinY.dp)
                                .rotate(rollRotation)
                                .scale(coinScale)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFFFDE047), Color(0xFFEAB308), Color(0xFFCA8A04))
                                    )
                                )
                                .border(2.dp, Color(0xFFFEF08A), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "₹",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF713F12)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = animPhase == 2,
                        enter = fadeIn() + expandVertically()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "+₹%,.2f Saved!".format(amount),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Deducted from usable balance & locked in goal",
                                fontSize = 11.sp,
                                color = SleekTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// ➕ ADD / EDIT GOAL DIALOG
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditSavingsGoalDialog(
    goalToEdit: SavingsGoal? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, target: Double, category: String, imageUri: String?, targetDate: Long) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var name by remember { mutableStateOf(goalToEdit?.name ?: "") }
    var targetStr by remember { mutableStateOf(goalToEdit?.targetAmount?.takeIf { it > 0 }?.toString() ?: "") }
    var category by remember { mutableStateOf(goalToEdit?.category ?: "Saving") }
    var imageUriStr by remember { mutableStateOf<String?>(goalToEdit?.imageUri) }

    var selectedPresetMonths by remember { mutableIntStateOf(6) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Custom Image Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUriStr = uri.toString()
        }
    }

    val availableCategories = listOf(
        "Saving", "Investment", "Expenditure", "Travel", "Tech",
        "Shopping", "Vehicle", "Education", "Emergency"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (goalToEdit == null) "Add New Saving Goal" else "Edit Saving Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )

                // Goal Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Goal Name (e.g. Holiday, Laptop)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_name_input")
                )

                // Target Amount Input in INR (₹)
                OutlinedTextField(
                    value = targetStr,
                    onValueChange = {
                        targetStr = it
                        errorMessage = null
                    },
                    label = { Text("Total Target Money (INR ₹)") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = SleekPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_target_input")
                )

                // Category Selection
                Text(
                    text = "Select Category",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextSecondary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    availableCategories.forEach { cat ->
                        val isSel = category.equals(cat, ignoreCase = true)
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                category = cat
                            },
                            label = { Text(cat, fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Target Timeframe Presets
                Text(
                    text = "Target Timeframe",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextSecondary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(3 to "3 Mon", 6 to "6 Mon", 12 to "1 Year", 24 to "2 Years").forEach { (months, label) ->
                        val isSel = selectedPresetMonths == months
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) SleekPrimary else SleekPrimaryContainer.copy(alpha = 0.2f))
                                .clickable { selectedPresetMonths = months }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else SleekTextSecondary
                            )
                        }
                    }
                }

                // Goal Image Option
                Text(
                    text = "Goal Image (Optional)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextSecondary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(SleekPrimary.copy(alpha = 0.12f))
                            .border(1.dp, SleekBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!imageUriStr.isNullOrBlank()) {
                            AsyncImage(
                                model = imageUriStr,
                                contentDescription = "Custom Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = getCategoryVectorIcon(category, name),
                                contentDescription = "Default Category Icon",
                                tint = SleekPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Choose Image", fontSize = 11.sp)
                            }

                            if (!imageUriStr.isNullOrBlank()) {
                                TextButton(
                                    onClick = { imageUriStr = null }
                                ) {
                                    Text("Remove", fontSize = 11.sp, color = Color(0xFFEF4444))
                                }
                            }
                        }
                        Text(
                            text = if (imageUriStr == null) "A category icon avatar will be created automatically" else "Custom image attached",
                            fontSize = 10.sp,
                            color = SleekTextSecondary
                        )
                    }
                }

                errorMessage?.let { err ->
                    Text(err, color = Color(0xFFEF4444), fontSize = 12.sp)
                }

                // Dialog Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = SleekTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val targetVal = targetStr.toDoubleOrNull()
                            if (name.isBlank()) {
                                errorMessage = "Please enter a goal name"
                                return@Button
                            }
                            if (targetVal == null || targetVal <= 0) {
                                errorMessage = "Please enter a valid target amount"
                                return@Button
                            }

                            val cal = Calendar.getInstance()
                            cal.add(Calendar.MONTH, selectedPresetMonths)
                            val calcDate = cal.timeInMillis

                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSave(name.trim(), targetVal, category, imageUriStr, calcDate)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                    ) {
                        Text(if (goalToEdit == null) "Save Goal" else "Update", color = Color.White)
                    }
                }
            }
        }
    }
}

// ==========================================
// 🔍 GOAL DEPOSIT & DETAIL BOTTOM SHEET
// ==========================================
@Composable
fun GoalDepositDetailBottomSheet(
    goal: SavingsGoal,
    onDismiss: () -> Unit,
    onDeposit: (Double) -> Unit,
    onDeduct: (Double) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var depositInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
    val primaryAccent = getCategoryAccentColor(goal.category)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(primaryAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!goal.imageUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = goal.imageUri,
                                    contentDescription = goal.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = getCategoryVectorIcon(goal.category, goal.name),
                                    contentDescription = goal.category,
                                    tint = primaryAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = goal.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Text(
                                text = "Category: ${goal.category}",
                                fontSize = 12.sp,
                                color = SleekTextSecondary
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = SleekPrimary)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                        }
                    }
                }

                // Progress Banner
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = primaryAccent.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Saved so far", fontSize = 11.sp, color = SleekTextSecondary)
                            Text(
                                "₹%,.2f".format(goal.currentAmount),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = primaryAccent
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Target Amount", fontSize = 11.sp, color = SleekTextSecondary)
                            Text(
                                "₹%,.2f".format(goal.targetAmount),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                        }
                    }
                }

                // Deposit Money Input Section
                Text(
                    text = "Deposit Money to Goal",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )

                OutlinedTextField(
                    value = depositInput,
                    onValueChange = {
                        depositInput = it
                        errorMessage = null
                    },
                    label = { Text("Enter Deposit Amount") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = SleekPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Preset Deposit Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(100.0, 500.0, 1000.0, 2000.0).forEach { preset ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SleekPrimaryContainer.copy(alpha = 0.2f))
                                .border(1.dp, SleekBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    depositInput = preset.toInt().toString()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+₹%,.0f".format(preset),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekPrimary
                            )
                        }
                    }
                }

                errorMessage?.let {
                    Text(it, color = Color(0xFFEF4444), fontSize = 12.sp)
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (goal.currentAmount > 0) {
                        OutlinedButton(
                            onClick = {
                                val amt = depositInput.toDoubleOrNull() ?: 0.0
                                if (amt <= 0) {
                                    errorMessage = "Enter valid deduction amount"
                                    return@OutlinedButton
                                }
                                onDeduct(amt)
                            },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Withdraw", color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            val amt = depositInput.toDoubleOrNull() ?: 0.0
                            if (amt <= 0) {
                                errorMessage = "Enter valid deposit amount"
                                return@Button
                            }
                            onDeposit(amt)
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+ Deposit", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 🎨 CATEGORY VECTOR & ACCENT HELPERS
// ==========================================
fun getCategoryVectorIcon(category: String, name: String): ImageVector {
    val combined = "$category $name".lowercase()
    return when {
        combined.contains("holiday") || combined.contains("travel") || combined.contains("trip") || combined.contains("beach") -> Icons.Rounded.BeachAccess
        combined.contains("laptop") || combined.contains("tech") || combined.contains("phone") || combined.contains("gadget") -> Icons.Rounded.Laptop
        combined.contains("education") || combined.contains("study") || combined.contains("school") || combined.contains("college") -> Icons.Rounded.School
        combined.contains("shopping") || combined.contains("cloth") || combined.contains("fashion") -> Icons.Rounded.ShoppingBag
        combined.contains("car") || combined.contains("vehicle") || combined.contains("bike") -> Icons.Rounded.DirectionsCar
        combined.contains("investment") || combined.contains("stock") || combined.contains("mutual") -> Icons.Rounded.TrendingUp
        combined.contains("emergency") || combined.contains("health") || combined.contains("medical") -> Icons.Rounded.Shield
        else -> Icons.Rounded.Savings
    }
}

fun getCategoryAccentColor(category: String): Color {
    return when (category.lowercase()) {
        "investment" -> Color(0xFF10B981)
        "expenditure", "shopping" -> Color(0xFFF59E0B)
        "travel", "holiday" -> Color(0xFF0EA5E9)
        "tech" -> Color(0xFF6366F1)
        "education" -> Color(0xFF8B5CF6)
        "vehicle" -> Color(0xFFEC4899)
        "emergency" -> Color(0xFFEF4444)
        else -> Color(0xFF0D9488) // Sleek teal
    }
}

// ==========================================
// 📌 LEGACY EMBEDDED SECTION (MAINTAINED)
// ==========================================
@Composable
fun SavingsGoalsSection(
    savingsGoals: List<SavingsGoal>,
    accounts: List<Account> = emptyList(),
    selectedLanguage: String = "English",
    onAddGoalClick: () -> Unit,
    onEditGoalClick: (SavingsGoal) -> Unit,
    onDeleteGoalClick: (SavingsGoal) -> Unit,
    onDepositGoalClick: (SavingsGoal) -> Unit,
    onDeductGoalClick: ((SavingsGoal) -> Unit)? = null,
    onQuickDeposit: (SavingsGoal, Double) -> Unit,
    onQuickDeduct: ((SavingsGoal, Double) -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Saving Goals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary
            )
            OutlinedButton(
                onClick = onAddGoalClick,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SleekPrimary)
            ) {
                Text("+ Add New", fontSize = 12.sp, color = SleekPrimary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (savingsGoals.isEmpty()) {
            Text("No saving goals yet. Tap '+ Add New' to get started!", fontSize = 12.sp, color = SleekTextSecondary)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                savingsGoals.forEach { goal ->
                    SavingGoalCardItem(
                        goal = goal,
                        onClick = { onDepositGoalClick(goal) }
                    )
                }
            }
        }
    }
}
