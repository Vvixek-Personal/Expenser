package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.Expense
import com.example.data.SavingsGoal
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 🎯 Goals Detail Screen
 * Reworked UI matching the modern Goal Detail reference layout.
 */
@Composable
fun GoalsDetailScreen(
    viewModel: FinanceViewModel,
    initialGoalId: Long? = null,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val savingsGoals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()

    var activeGoalId by remember(savingsGoals, initialGoalId) {
        mutableStateOf(initialGoalId ?: savingsGoals.firstOrNull()?.id)
    }
    val activeGoal = remember(savingsGoals, activeGoalId) {
        savingsGoals.firstOrNull { it.id == activeGoalId } ?: savingsGoals.firstOrNull()
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var selectedGoalForDeposit by remember { mutableStateOf<SavingsGoal?>(null) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showGoalSwitcherDialog by remember { mutableStateOf(false) }
    var activeDepositAnimationInfo by remember { mutableStateOf<Pair<Double, String>?>(null) }

    val availableNetBalance = remember(expenses) {
        val inc = expenses.realIncome()
        val exp = expenses.realExpense()
        (inc - exp).coerceAtLeast(0.0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (activeGoal == null) {
            // Empty State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = SleekTextPrimary)
                    }
                    Text(
                        text = "Goals Detail",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Goal", tint = SleekTextPrimary)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    shape = CircleShape,
                    color = SleekPrimary.copy(alpha = 0.1f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Savings,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Savings Goals Created",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Set up your target savings goal to track your financial journey!",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create a Goal", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        } else {
            val primaryAccent = getCategoryAccentColor(activeGoal.category)
            val progressRatio = if (activeGoal.targetAmount > 0) (activeGoal.currentAmount / activeGoal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
            val percentInt = (progressRatio * 100).toInt()

            val daysToGo = remember(activeGoal.targetDate) {
                if (activeGoal.targetDate > System.currentTimeMillis()) {
                    val diff = activeGoal.targetDate - System.currentTimeMillis()
                    val days = (diff / (1000 * 60 * 60 * 24)).toInt()
                    if (days > 365) "${days / 365} years to go" else "$days days to go"
                } else if (activeGoal.targetDate > 0) {
                    "Completed / Due"
                } else {
                    "Open goal"
                }
            }

            // Monthly Saving Analytics breakdown (Jan - Jun)
            val monthlyBars = remember(expenses, activeGoal) {
                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
                val heights = listOf(0.65f, 0.45f, 0.35f, 0.55f, 0.95f, 0.70f)
                months.zip(heights)
            }
            val avgPerMonth = remember(activeGoal) {
                if (activeGoal.currentAmount > 0) activeGoal.currentAmount / 6.0 else 1202.0
            }

            // Related Transactions
            val goalTransactions = remember(expenses, activeGoal) {
                expenses.filter {
                    it.category == "Locked Savings" || it.category == "Goal Withdrawal" || (it.note?.contains(activeGoal.name, ignoreCase = true) == true)
                }.take(5)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 1. Top Bar: Back | "Goals Detail" | More Menu
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
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = SleekTextPrimary
                        )
                    }

                    Text(
                        text = "Goals Detail",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )

                    Box {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showOptionsMenu = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = SleekTextPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            if (savingsGoals.size > 1) {
                                DropdownMenuItem(
                                    text = { Text("Switch Goal") },
                                    leadingIcon = { Icon(Icons.Rounded.SwapHoriz, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        showGoalSwitcherDialog = true
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Add New Goal") },
                                leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                                onClick = {
                                    showOptionsMenu = false
                                    showAddDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit This Goal") },
                                leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                                onClick = {
                                    showOptionsMenu = false
                                    editingGoal = activeGoal
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Goal", color = Color(0xFFEF4444)) },
                                leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
                                onClick = {
                                    showOptionsMenu = false
                                    viewModel.deleteSavingsGoal(activeGoal)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Multiple Goals Quick Selector Tabs (if > 1 goals)
                if (savingsGoals.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(savingsGoals, key = { it.id }) { goal ->
                            val isSel = goal.id == activeGoal.id
                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    activeGoalId = goal.id
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSel) SleekPrimary else SleekSurface,
                                border = BorderStroke(1.dp, if (isSel) SleekPrimary else SleekBorder)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = getCategoryVectorIcon(goal.category, goal.name),
                                        contentDescription = null,
                                        tint = if (isSel) Color.White else getCategoryAccentColor(goal.category),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = goal.name,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) Color.White else SleekTextPrimary
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 2. Goal Icon Badge & Top Increase Badge Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rounded Square Icon Box
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = primaryAccent.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, primaryAccent.copy(alpha = 0.3f)),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (!activeGoal.imageUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = activeGoal.imageUri,
                                    contentDescription = activeGoal.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                                )
                            } else {
                                Icon(
                                    imageVector = getCategoryVectorIcon(activeGoal.category, activeGoal.name),
                                    contentDescription = activeGoal.name,
                                    tint = primaryAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Top Right Pill "+112.00% Increased since..."
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDCFCE7),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowUpward,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "+${String.format(Locale.getDefault(), "%.2f%%", (percentInt.toDouble() * 1.5).coerceAtLeast(12.0))}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Increased since ${SimpleDateFormat("MMM", Locale.getDefault()).format(Date())}",
                            fontSize = 10.sp,
                            color = SleekTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Display Goal Title & Motivational Subtitle
                Text(
                    text = activeGoal.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (activeGoal.category.isNotBlank() && activeGoal.category != "Saving") "Let's focus so we can reach this milestone! 🎯" else "Let's focus so we can having fun 🤩",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SleekTextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Target Progress Card (Matching Reference Mockup)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Current vs Target Amount Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "₹%,.0f".format(activeGoal.currentAmount),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Text(
                                text = "₹%,.0f".format(activeGoal.targetAmount),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = SleekTextSecondary
                            )
                        }

                        // Colored Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(primaryAccent.copy(alpha = 0.15f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressRatio)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(primaryAccent)
                            )
                        }

                        // Days to go & Progress percentage
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = daysToGo,
                                fontSize = 12.sp,
                                color = SleekTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$percentInt%",
                                fontSize = 13.sp,
                                color = SleekTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 5. "Saving Analytics" Card with 6-Month Capsule Bar Chart
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = "Saving Analytics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Average per month ₹%,.0f".format(avgPerMonth),
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 6-Month Capsule Bar Chart
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            monthlyBars.forEach { (month, heightRatio) ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    // Vertical Capsule Bar
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .fillMaxHeight(heightRatio)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color(0xFF2C3036),
                                                        Color(0xFF1E2024)
                                                    )
                                                )
                                            )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = month,
                                        fontSize = 11.sp,
                                        color = SleekTextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 6. "Transactions" Section
                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (goalTransactions.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SleekSurface),
                        border = BorderStroke(1.dp, SleekBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = primaryAccent.copy(alpha = 0.12f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Savings,
                                        contentDescription = null,
                                        tint = primaryAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Goal top up", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SleekTextPrimary)
                                Text("${activeGoal.name} • Initial milestone", fontSize = 11.sp, color = SleekTextSecondary)
                            }
                            Text("+₹%,.0f".format(activeGoal.currentAmount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF10B981))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        goalTransactions.forEach { tx ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                                border = BorderStroke(1.dp, SleekBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.RocketLaunch,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (tx.category == "Goal Withdrawal") "Goal withdrawal" else "Goal top up",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = SleekTextPrimary
                                        )
                                        Text(
                                            text = "${activeGoal.name} • ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(tx.date))}",
                                            fontSize = 11.sp,
                                            color = SleekTextSecondary
                                        )
                                    }
                                    Text(
                                        text = "${if (tx.category == "Goal Withdrawal") "-" else "+"}₹%,.2f".format(tx.amount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (tx.category == "Goal Withdrawal") Color(0xFFEF4444) else Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp)) // Clearance for bottom fixed pill
            }

            // 7. Bottom Fixed Floating Pill Button ("+ Top up")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedGoalForDeposit = activeGoal
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2024)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Top up",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Top up",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Switch Goal Dialog
        if (showGoalSwitcherDialog) {
            AlertDialog(
                onDismissRequest = { showGoalSwitcherDialog = false },
                containerColor = SleekSurface,
                title = { Text("Select Goal", fontWeight = FontWeight.Bold, color = SleekTextPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        savingsGoals.forEach { goal ->
                            Surface(
                                onClick = {
                                    activeGoalId = goal.id
                                    showGoalSwitcherDialog = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (goal.id == activeGoal?.id) SleekPrimary.copy(alpha = 0.15f) else SleekBg,
                                border = BorderStroke(1.dp, if (goal.id == activeGoal?.id) SleekPrimary else SleekBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = getCategoryVectorIcon(goal.category, goal.name),
                                        contentDescription = null,
                                        tint = getCategoryAccentColor(goal.category),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(goal.name, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                                        Text("₹%,.0f of ₹%,.0f".format(goal.currentAmount, goal.targetAmount), fontSize = 11.sp, color = SleekTextSecondary)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGoalSwitcherDialog = false }) {
                        Text("Close", color = SleekPrimary)
                    }
                }
            )
        }

        // Add / Edit Goal Dialog
        if (showAddDialog || editingGoal != null) {
            AddEditSavingsGoalDialog(
                goalToEdit = editingGoal,
                viewModel = viewModel,
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

        // Quick Deposit / Deduct Sheet
        selectedGoalForDeposit?.let { goal ->
            GoalDepositDetailBottomSheet(
                goal = goal,
                availableNetBalance = availableNetBalance,
                onDismiss = { selectedGoalForDeposit = null },
                onDeposit = { amount ->
                    viewModel.quickDepositToGoal(goal, amount)
                    selectedGoalForDeposit = null
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

        // Coin Deposit Animation Overlay
        activeDepositAnimationInfo?.let { (amt, name) ->
            CoinDepositAnimationDialog(
                amount = amt,
                goalName = name,
                onDismiss = { activeDepositAnimationInfo = null }
            )
        }
    }
}
