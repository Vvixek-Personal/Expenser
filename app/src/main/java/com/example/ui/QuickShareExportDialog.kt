package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Expense
import com.example.ui.theme.*
import java.util.Calendar

/**
 * 📤 Quick Share & Export Dialog
 * 2-step flow:
 * Step 1: Select Time Period & Transaction Type
 * Step 2: Select Format: CSV, PDF, or Image
 */
@Composable
fun QuickShareExportDialog(
    allExpenses: List<Expense>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var currentStep by remember { mutableIntStateOf(1) }
    var selectedPeriod by remember { mutableStateOf("This Month") }
    var selectedType by remember { mutableStateOf("All") }

    val periodOptions = listOf("This Month", "Last 30 Days", "Last Month", "This Year", "All Time")
    val typeOptions = listOf("All", "Expense", "Income")

    // Filter transactions based on selection
    val filteredExpenses = remember(allExpenses, selectedPeriod, selectedType) {
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        var list = allExpenses.filter { expense ->
            val cal = Calendar.getInstance().apply { timeInMillis = expense.date }
            when (selectedPeriod) {
                "This Month" -> cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
                "Last 30 Days" -> (System.currentTimeMillis() - expense.date) <= 30L * 24 * 60 * 60 * 1000
                "Last Month" -> {
                    val lastMonthCal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
                    cal.get(Calendar.MONTH) == lastMonthCal.get(Calendar.MONTH) && cal.get(Calendar.YEAR) == lastMonthCal.get(Calendar.YEAR)
                }
                "This Year" -> cal.get(Calendar.YEAR) == currentYear
                else -> true
            }
        }

        if (selectedType == "Expense") {
            list = list.filter { it.type != "INCOME" }
        } else if (selectedType == "Income") {
            list = list.filter { it.type == "INCOME" }
        }
        list
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SleekPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = null,
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (currentStep == 1) "Export Transactions" else "Select Format",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Text(
                                text = if (currentStep == 1) "Step 1 of 2: Filters" else "Step 2 of 2: Export As",
                                style = MaterialTheme.typography.labelSmall,
                                color = SleekTextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = SleekTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                AnimatedContent(
                    targetState = currentStep,
                    label = "ExportStepAnim"
                ) { step ->
                    if (step == 1) {
                        // Step 1: Filters (Time Period & Type)
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Period Selector
                            Text(
                                text = "Time Period",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                periodOptions.forEach { period ->
                                    val isSelected = selectedPeriod == period
                                    Surface(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedPeriod = period
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) SleekPrimary.copy(alpha = 0.12f) else SleekBg,
                                        border = BorderStroke(1.dp, if (isSelected) SleekPrimary else SleekBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = period,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) SleekPrimary else SleekTextPrimary
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Rounded.CheckCircle,
                                                    contentDescription = null,
                                                    tint = SleekPrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Type Selector
                            Text(
                                text = "Transaction Type",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                typeOptions.forEach { type ->
                                    val isSelected = selectedType == type
                                    Surface(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedType = type
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) SleekPrimary else SleekBg,
                                        border = BorderStroke(1.dp, if (isSelected) SleekPrimary else SleekBorder),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = type,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else SleekTextPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            // Summary Info
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SleekBg,
                                border = BorderStroke(1.dp, SleekBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Matching transactions:",
                                        fontSize = 12.sp,
                                        color = SleekTextSecondary
                                    )
                                    Text(
                                        text = "${filteredExpenses.size} items",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekTextPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Next Button
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    currentStep = 2
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text("Proceed to Format", color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        // Step 2: Choose Format (CSV, PDF, Image)
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                text = "Choose export format for $selectedPeriod ($selectedType, ${filteredExpenses.size} txns):",
                                fontSize = 12.sp,
                                color = SleekTextSecondary
                            )

                            // Option 1: PDF Document
                            FormatOptionCard(
                                title = "PDF Statement",
                                subtitle = "Formal printable statement with charts & summaries",
                                icon = Icons.Rounded.PictureAsPdf,
                                iconColor = Color(0xFFEF4444),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    DataExporter.sharePdfReport(
                                        context = context,
                                        expenses = filteredExpenses,
                                        dateRangeStr = selectedPeriod,
                                        typeFilterStr = selectedType,
                                        categoryFilterStr = "All"
                                    )
                                    onDismiss()
                                }
                            )

                            // Option 2: CSV Spreadsheet
                            FormatOptionCard(
                                title = "CSV Spreadsheet",
                                subtitle = "Universal spreadsheet format (Excel, Sheets)",
                                icon = Icons.Rounded.TableChart,
                                iconColor = Color(0xFF10B981),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    DataExporter.exportToCSV(
                                        context = context,
                                        expenses = filteredExpenses,
                                        dateRangeStr = selectedPeriod,
                                        typeFilterStr = selectedType,
                                        categoryFilterStr = "All"
                                    )
                                    onDismiss()
                                }
                            )

                            // Option 3: Image Report
                            FormatOptionCard(
                                title = "Image Summary",
                                subtitle = "High-resolution graphic image report to share",
                                icon = Icons.Rounded.Image,
                                iconColor = Color(0xFF3B82F6),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    DataExporter.shareImageReport(
                                        context = context,
                                        expenses = filteredExpenses,
                                        dateRangeStr = selectedPeriod,
                                        typeFilterStr = selectedType,
                                        categoryFilterStr = "All"
                                    )
                                    onDismiss()
                                }
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Back to step 1 button
                            OutlinedButton(
                                onClick = { currentStep = 1 },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, SleekBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Text("Back to Filter", color = SleekTextPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = SleekBg,
        border = BorderStroke(1.dp, SleekBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SleekTextPrimary)
                Text(subtitle, fontSize = 11.sp, color = SleekTextSecondary)
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = SleekTextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}
