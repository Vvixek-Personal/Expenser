package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Account
import com.example.data.SavingsGoal
import com.example.ui.theme.*

@Composable
fun SavingsGoalsSection(
    savingsGoals: List<SavingsGoal>,
    accounts: List<Account> = emptyList(),
    selectedLanguage: String = "English",
    onAddGoalClick: () -> Unit,
    onEditGoalClick: (SavingsGoal) -> Unit,
    onDeleteGoalClick: (SavingsGoal) -> Unit,
    onDepositGoalClick: (SavingsGoal) -> Unit,
    onQuickDeposit: (SavingsGoal, Double) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SleekPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Savings,
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = LanguageManager.tr("Savings Goals", selectedLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Save for PS5, Gadgets, Trips & Reserves",
                        style = MaterialTheme.typography.labelSmall,
                        color = SleekTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Surface(
                onClick = onAddGoalClick,
                shape = RoundedCornerShape(20.dp),
                color = SleekPrimary,
                modifier = Modifier.testTag("add_savings_goal_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = LanguageManager.tr("Create Goal", selectedLanguage),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (savingsGoals.isEmpty()) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddGoalClick() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎮 📱 ✈️",
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Savings Goals Yet",
                        style = MaterialTheme.typography.titleSmall,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Set a target prize (like PS5 or Emergency Fund) and start saving daily, weekly, or monthly!",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                savingsGoals.forEach { goal ->
                    SavingsGoalCard(
                        goal = goal,
                        selectedLanguage = selectedLanguage,
                        onEdit = { onEditGoalClick(goal) },
                        onDelete = { onDeleteGoalClick(goal) },
                        onDeposit = { onDepositGoalClick(goal) },
                        onQuickDeposit = { amount -> onQuickDeposit(goal, amount) }
                    )
                }
            }
        }
    }
}

@Composable
fun SavingsGoalCard(
    goal: SavingsGoal,
    selectedLanguage: String = "English",
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDeposit: () -> Unit,
    onQuickDeposit: (Double) -> Unit
) {
    val progressRatio = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "goalProgress"
    )

    val percentInt = (progressRatio * 100).toInt()
    val isCompleted = goal.currentAmount >= goal.targetAmount
    val remainingAmount = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)

    var showMenu by remember { mutableStateOf(false) }

    // Calculated entry gap display
    val calculatedGap = if (goal.isAutoGap) {
        if (goal.contributionAmount > 0) goal.contributionAmount
        else (goal.targetAmount / 10.0).coerceAtLeast(10.0)
    } else {
        goal.contributionAmount
    }

    val frequencyLabel = when (goal.frequency.uppercase()) {
        "DAILY" -> "Daily Gap"
        "WEEKLY" -> "Weekly Gap"
        "MONTHLY" -> "Monthly Gap"
        else -> "Manual Gap"
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, if (isCompleted) Color(0xFF10B981) else SleekBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("savings_goal_card_${goal.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Emoji Tag, Title & Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isCompleted) Color(0xFF10B981).copy(alpha = 0.2f)
                                else SleekPrimaryContainer.copy(alpha = 0.3f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = goal.iconTag.ifBlank { "🎮" },
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = goal.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = SleekTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            if (isCompleted) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF10B981))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "REACHED 🎉",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Target: ₹%,.0f".format(goal.targetAmount),
                                style = MaterialTheme.typography.bodySmall,
                                color = SleekTextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.12f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = "Locked",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Locked: ₹%,.0f".format(goal.currentAmount),
                                        color = Color(0xFF2563EB),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Options",
                            tint = SleekTextSecondary
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Goal") },
                            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Goal", color = Color(0xFFEF4444)) },
                            leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Line & Amounts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Saved so far",
                        style = MaterialTheme.typography.labelSmall,
                        color = SleekTextSecondary,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "₹%,.0f".format(goal.currentAmount),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isCompleted) Color(0xFF10B981) else SleekPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) Color(0xFF10B981).copy(alpha = 0.2f)
                            else SleekPrimary.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$percentInt%",
                        color = if (isCompleted) Color(0xFF10B981) else SleekPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Animated Progress Indicator Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                color = if (isCompleted) Color(0xFF10B981) else SleekPrimary,
                trackColor = SleekPrimary.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Frequency Gap & Remaining info row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SleekBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = SleekTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$frequencyLabel: ",
                        fontSize = 11.sp,
                        color = SleekTextSecondary
                    )
                    Text(
                        text = if (calculatedGap > 0) "₹%,.0f".format(calculatedGap) else "Flexible",
                        fontSize = 11.sp,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (remainingAmount > 0) "₹%,.0f left".format(remainingAmount) else "Completed!",
                    fontSize = 11.sp,
                    color = if (remainingAmount > 0) SleekTextSecondary else Color(0xFF10B981),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row: Quick Deposit Chips & Deposit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick add chips
                    val chip1 = if (calculatedGap > 0) calculatedGap else 100.0
                    val chip2 = chip1 * 2

                    QuickDepositChip(
                        label = "+₹%,.0f".format(chip1),
                        onClick = { onQuickDeposit(chip1) }
                    )
                    QuickDepositChip(
                        label = "+₹%,.0f".format(chip2),
                        onClick = { onQuickDeposit(chip2) }
                    )
                }

                Button(
                    onClick = onDeposit,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimaryContainer),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalanceWallet,
                        contentDescription = null,
                        tint = SleekOnPrimaryContainer,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = LanguageManager.tr("Deposit Money", selectedLanguage),
                        color = SleekOnPrimaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun QuickDepositChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SleekPrimary.copy(alpha = 0.1f))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = SleekPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AddEditSavingsGoalDialog(
    goalToEdit: SavingsGoal? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, targetAmount: Double, initialAmount: Double, frequency: String, contributionAmount: Double, isAutoGap: Boolean, iconTag: String) -> Unit
) {
    var name by remember { mutableStateOf(goalToEdit?.name ?: "") }
    var targetAmountStr by remember { mutableStateOf(goalToEdit?.targetAmount?.takeIf { it > 0 }?.let { "%.0f".format(it) } ?: "") }
    var currentAmountStr by remember { mutableStateOf(goalToEdit?.currentAmount?.takeIf { it > 0 }?.let { "%.0f".format(it) } ?: "") }
    var frequency by remember { mutableStateOf(goalToEdit?.frequency ?: "WEEKLY") }
    var isAutoGap by remember { mutableStateOf(goalToEdit?.isAutoGap ?: true) }
    var manualContributionStr by remember { mutableStateOf(goalToEdit?.contributionAmount?.takeIf { it > 0 }?.let { "%.0f".format(it) } ?: "") }
    var iconTag by remember { mutableStateOf(goalToEdit?.iconTag ?: "🎮") }

    val presetTags = listOf("PS5 🎮", "Phone 📱", "Trip ✈️", "Car 🚗", "Emergency 🛡️", "Laptop 💻", "House 🏠")
    val presetIcons = listOf("🎮", "📱", "✈️", "🚗", "🛡️", "💻", "🏠", "👟", "⌚", "🎸")

    val targetDouble = targetAmountStr.toDoubleOrNull() ?: 0.0
    val autoCalculatedGap = remember(targetDouble, frequency) {
        if (targetDouble <= 0) 0.0
        else when (frequency.uppercase()) {
            "DAILY" -> targetDouble / 30.0
            "WEEKLY" -> targetDouble / 10.0
            "MONTHLY" -> targetDouble / 6.0
            else -> targetDouble / 10.0
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (goalToEdit == null) "Create Savings Goal" else "Edit Savings Goal",
                    style = MaterialTheme.typography.titleMedium,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Quick preset pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetTags.take(4).forEach { tag ->
                        val textOnly = tag.split(" ").first()
                        val emoji = tag.split(" ").last()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SleekPrimary.copy(alpha = 0.1f))
                                .clickable {
                                    if (name.isBlank() || name == "PlayStation 5" || name == "New Phone") {
                                        name = textOnly
                                    }
                                    iconTag = emoji
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 10.sp,
                                color = SleekPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Icon Tag Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Icon:",
                        fontSize = 12.sp,
                        color = SleekTextSecondary
                    )
                    presetIcons.take(6).forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (iconTag == emoji) SleekPrimaryContainer else SleekBg)
                                .clickable { iconTag = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Goal Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name (e.g. PS5, iPhone, Japan Trip)", color = SleekTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Target Amount & Initial Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = targetAmountStr,
                        onValueChange = { targetAmountStr = it },
                        label = { Text("Target Prize (₹)", color = SleekTextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("target_amount_input")
                    )

                    OutlinedTextField(
                        value = currentAmountStr,
                        onValueChange = { currentAmountStr = it },
                        label = { Text("Initial Saved (₹)", color = SleekTextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Saving Mode: Automatic vs Manual Buttons
                Text(
                    text = "Saving Mode",
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf(true to "Automatic ⚡", false to "Manual 🖐️")
                    modes.forEach { (modeVal, label) ->
                        val isSel = isAutoGap == modeVal
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) SleekPrimary else SleekBg)
                                .clickable { isAutoGap = modeVal }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                color = if (isSel) Color.White else SleekTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (isAutoGap) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Automatic Deduction Interval",
                        style = MaterialTheme.typography.labelSmall,
                        color = SleekTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val frequencies = listOf("DAILY" to "Daily", "WEEKLY" to "Weekly", "MONTHLY" to "Monthly")
                        frequencies.forEach { (key, label) ->
                            val isSel = frequency.equals(key, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) SleekPrimaryContainer.copy(alpha = 0.8f) else SleekBg)
                                    .clickable { frequency = key }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    color = if (isSel) SleekPrimary else SleekTextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SleekPrimary.copy(alpha = 0.08f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = if (targetDouble > 0) "💡 Money will be deducted automatically ~₹%,.0f / %s".format(autoCalculatedGap, frequency.lowercase())
                            else "Enter target prize to view automatic deduction estimation",
                            fontSize = 11.sp,
                            color = SleekPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SleekPrimary.copy(alpha = 0.08f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "💡 Manual saving: You choose when to add money to this goal.",
                            fontSize = 11.sp,
                            color = SleekPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = SleekTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val target = targetAmountStr.toDoubleOrNull() ?: 0.0
                            val initial = currentAmountStr.toDoubleOrNull() ?: 0.0
                            val manualVal = manualContributionStr.toDoubleOrNull() ?: 0.0
                            val finalGap = if (isAutoGap) autoCalculatedGap else manualVal
                            if (name.isNotBlank() && target > 0) {
                                onSave(name, target, initial, frequency, finalGap, isAutoGap, iconTag)
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                    ) {
                        Text("Save Goal", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DepositToGoalDialog(
    goal: SavingsGoal,
    accounts: List<Account> = emptyList(),
    onDismiss: () -> Unit,
    onConfirmDeposit: (amount: Double, accountId: Int?) -> Unit
) {
    var depositStr by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf<Int?>(accounts.firstOrNull()?.id) }

    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = goal.iconTag.ifBlank { "🎮" }, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Deposit to ${goal.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = SleekTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Saved: ₹%,.0f of ₹%,.0f".format(goal.currentAmount, goal.targetAmount),
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick add buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presetAmounts = listOf(100.0, 500.0, 1000.0, remaining)
                    presetAmounts.distinct().filter { it > 0 }.take(4).forEach { amt ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SleekPrimary.copy(alpha = 0.12f))
                                .clickable { depositStr = "%.0f".format(amt) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (amt == remaining) "Full Target" else "+₹%,.0f".format(amt),
                                fontSize = 10.sp,
                                color = SleekPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = depositStr,
                    onValueChange = { depositStr = it },
                    label = { Text("Deposit Amount (₹)", color = SleekTextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_amount_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2563EB).copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Lock",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Deposited money will be locked in this goal & deducted from spendable balance.",
                        fontSize = 11.sp,
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Medium
                    )
                }

                if (accounts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Source Account:",
                        fontSize = 11.sp,
                        color = SleekTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        accounts.take(3).forEach { acc ->
                            val isSelected = selectedAccountId == acc.id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) SleekPrimary else SleekBg)
                                    .clickable { selectedAccountId = acc.id }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = acc.name,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White else SleekTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = SleekTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = depositStr.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onConfirmDeposit(amt, selectedAccountId)
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                    ) {
                        Text("Confirm Deposit", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
