package com.example.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

/**
 * Full Screen 4-Digit PIN Lock Screen.
 * Prevents app access when locked until the correct 4-digit PIN is entered.
 */
@Composable
fun KeypadButton(
    item: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
                if (item == "C" || item == "DEL") Color.Transparent
                else Color(0xFF232530)
            )
            .border(
                width = if (item == "C" || item == "DEL") 0.dp else 1.dp,
                color = Color(0xFF383A48),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (item) {
            "DEL" -> Icon(
                imageVector = Icons.Default.Backspace,
                contentDescription = "Delete",
                tint = Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
            "C" -> Text(
                text = "CLEAR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray
            )
            else -> Text(
                text = item,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * First-Run Optional Passcode Setup Dialog.
 * Appears once on app first launch if passcode hasn't been set or explicitly skipped.
 */
@Composable
fun FirstRunPinSetupDialog(
    onSetPin: (String) -> Unit,
    onMaybeLater: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Enter, 2: Confirm
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    val currentInput = if (step == 1) firstPin else confirmPin

    Dialog(
        onDismissRequest = onMaybeLater,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1B1D26),
            border = BorderStroke(1.dp, Color(0xFF2E303A))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF7ED)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = if (step == 1) "Protect Your App" else "Confirm Passcode",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = if (errorText.isNotEmpty()) errorText
                    else if (step == 1) "Set a 4-digit PIN to secure your financial records."
                    else "Re-enter your 4-digit PIN to confirm.",
                    fontSize = 13.sp,
                    color = if (errorText.isNotEmpty()) Color(0xFFEF4444) else Color.LightGray,
                    textAlign = TextAlign.Center
                )

                // 4 PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isFilled = index < currentInput.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) Color(0xFF10B981) else Color.Transparent)
                                .border(
                                    width = 2.dp,
                                    color = if (isFilled) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Mini Keypad
                PinKeypad(
                    onDigitClick = { digit ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (step == 1) {
                            if (firstPin.length < 4) {
                                firstPin += digit
                                errorText = ""
                                if (firstPin.length == 4) {
                                    step = 2
                                }
                            }
                        } else {
                            if (confirmPin.length < 4) {
                                confirmPin += digit
                                errorText = ""
                                if (confirmPin.length == 4) {
                                    if (confirmPin == firstPin) {
                                        onSetPin(firstPin)
                                    } else {
                                        errorText = "Passcodes do not match. Try again."
                                        confirmPin = ""
                                        step = 1
                                        firstPin = ""
                                    }
                                }
                            }
                        }
                    },
                    onBackspaceClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (step == 1 && firstPin.isNotEmpty()) {
                            firstPin = firstPin.dropLast(1)
                        } else if (step == 2 && confirmPin.isNotEmpty()) {
                            confirmPin = confirmPin.dropLast(1)
                        }
                    },
                    onClearClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (step == 1) firstPin = "" else confirmPin = ""
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onMaybeLater) {
                        Text("Maybe later", color = Color.LightGray, fontWeight = FontWeight.SemiBold)
                    }

                    if (step == 2) {
                        TextButton(onClick = {
                            step = 1
                            firstPin = ""
                            confirmPin = ""
                            errorText = ""
                        }) {
                            Text("Reset", color = Color(0xFFF97316), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Change or Setup/Remove Passcode Security Dialog.
 */
@Composable
fun ChangePinDialog(
    currentAppPin: String?,
    onDismiss: () -> Unit,
    onSavePin: (String?) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Steps:
    // If currentAppPin exists: 1: Current PIN, 2: New PIN, 3: Confirm New PIN
    // If currentAppPin == null: 2: New PIN, 3: Confirm New PIN
    var step by remember { mutableIntStateOf(if (currentAppPin != null) 1 else 2) }

    var enteredCurrent by remember { mutableStateOf("") }
    var enteredNew by remember { mutableStateOf("") }
    var enteredConfirm by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }

    val activeInput = when (step) {
        1 -> enteredCurrent
        2 -> enteredNew
        else -> enteredConfirm
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1B1D26),
            border = BorderStroke(1.dp, Color(0xFF2E303A))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentAppPin != null) "Change Passcode" else "Setup Passcode",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Text(
                    text = if (errorText.isNotEmpty()) errorText
                    else when (step) {
                        1 -> "Enter your current 4-digit passcode."
                        2 -> "Enter your new 4-digit passcode."
                        else -> "Re-enter your new 4-digit passcode to confirm."
                    },
                    fontSize = 13.sp,
                    color = if (errorText.isNotEmpty()) Color(0xFFEF4444) else Color.LightGray,
                    textAlign = TextAlign.Center
                )

                // 4 PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isFilled = index < activeInput.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) Color(0xFF10B981) else Color.Transparent)
                                .border(
                                    width = 2.dp,
                                    color = if (isFilled) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Keypad
                PinKeypad(
                    onDigitClick = { digit ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        when (step) {
                            1 -> {
                                if (enteredCurrent.length < 4) {
                                    enteredCurrent += digit
                                    errorText = ""
                                    if (enteredCurrent.length == 4) {
                                        if (enteredCurrent == currentAppPin) {
                                            step = 2
                                        } else {
                                            errorText = "Incorrect current passcode."
                                            enteredCurrent = ""
                                        }
                                    }
                                }
                            }
                            2 -> {
                                if (enteredNew.length < 4) {
                                    enteredNew += digit
                                    errorText = ""
                                    if (enteredNew.length == 4) {
                                        step = 3
                                    }
                                }
                            }
                            3 -> {
                                if (enteredConfirm.length < 4) {
                                    enteredConfirm += digit
                                    errorText = ""
                                    if (enteredConfirm.length == 4) {
                                        if (enteredConfirm == enteredNew) {
                                            onSavePin(enteredNew)
                                            Toast.makeText(context, "Passcode updated successfully!", Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                        } else {
                                            errorText = "Passcodes do not match. Try again."
                                            enteredConfirm = ""
                                            step = 2
                                            enteredNew = ""
                                        }
                                    }
                                }
                            }
                        }
                    },
                    onBackspaceClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        when (step) {
                            1 -> if (enteredCurrent.isNotEmpty()) enteredCurrent = enteredCurrent.dropLast(1)
                            2 -> if (enteredNew.isNotEmpty()) enteredNew = enteredNew.dropLast(1)
                            3 -> if (enteredConfirm.isNotEmpty()) enteredConfirm = enteredConfirm.dropLast(1)
                        }
                    },
                    onClearClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        when (step) {
                            1 -> enteredCurrent = ""
                            2 -> enteredNew = ""
                            3 -> enteredConfirm = ""
                        }
                    }
                )

                // Remove Passcode option if currentAppPin exists
                if (currentAppPin != null && step == 1) {
                    TextButton(
                        onClick = {
                            if (enteredCurrent == currentAppPin) {
                                onSavePin(null)
                                Toast.makeText(context, "Passcode removed.", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            } else {
                                errorText = "Type current passcode first to remove."
                            }
                        }
                    ) {
                        Text("Turn Off Passcode Lock", color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
fun PinLockScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    var enteredPin by remember { mutableStateOf("") }
    var isErrorState by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val vibrateOnError = {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(150)
            }
        } catch (_: Exception) {}
    }

    LaunchedEffect(enteredPin) {
        if (enteredPin.length == 4) {
            val success = viewModel.unlockAppWithPin(enteredPin)
            if (success) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                enteredPin = ""
                isErrorState = false
                errorMessage = ""
            } else {
                vibrateOnError()
                isErrorState = true
                errorMessage = "Incorrect Passcode. Please try again."
                enteredPin = ""
            }
        } else if (enteredPin.isNotEmpty()) {
            isErrorState = false
            errorMessage = ""
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1014),
                        Color(0xFF1B1D26),
                        Color(0xFF0F1014)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 400.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Brand Logo & Lock Indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(SleekPrimary.copy(alpha = 0.15f))
                        .border(2.dp, SleekPrimary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "App Locked",
                        tint = SleekPrimary,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Text(
                    text = "Welcome Back",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = if (isErrorState) errorMessage else "Enter 4-digit PIN code to unlock",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isErrorState) Color(0xFFEF4444) else Color.LightGray,
                    textAlign = TextAlign.Center
                )

                // 4 PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    repeat(4) { index ->
                        val isFilled = index < enteredPin.length
                        val dotScale by animateFloatAsState(
                            targetValue = if (isFilled) 1.25f else 1.0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "dotScale"
                        )

                        Box(
                            modifier = Modifier
                                .scale(dotScale)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isErrorState -> Color(0xFFEF4444)
                                        isFilled -> SleekPrimary
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = 2.dp,
                                    color = when {
                                        isErrorState -> Color(0xFFEF4444)
                                        isFilled -> SleekPrimary
                                        else -> Color.Gray.copy(alpha = 0.5f)
                                    },
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Numeric Keypad
            PinKeypad(
                onDigitClick = { digit ->
                    if (enteredPin.length < 4) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        enteredPin += digit
                    }
                },
                onBackspaceClick = {
                    if (enteredPin.isNotEmpty()) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        enteredPin = enteredPin.dropLast(1)
                    }
                },
                onClearClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    enteredPin = ""
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * Standard 0-9 Tactile Numeric Keypad.
 */
@Composable
fun PinKeypad(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keypadGrid = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "DEL")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keypadGrid.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { item ->
                    KeypadButton(
                        item = item,
                        onClick = {
                            when (item) {
                                "C" -> onClearClick()
                                "DEL" -> onBackspaceClick()
                                else -> onDigitClick(item)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    item: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
                if (item == "C" || item == "DEL") Color.Transparent
                else Color(0xFF232530)
            )
            .border(
                width = if (item == "C" || item == "DEL") 0.dp else 1.dp,
                color = Color(0xFF383A48),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (item) {
            "DEL" -> Icon(
                imageVector = Icons.Default.Backspace,
                contentDescription = "Delete",
                tint = Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
            "C" -> Text(
                text = "CLEAR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray
            )
            else -> Text(
                text = item,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * First-Run Optional Passcode Setup Dialog.
 * Appears once on app first launch if passcode hasn't been set or explicitly skipped.
 */
@Composable
fun FirstRunPinSetupDialog(
    onSetPin: (String) -> Unit,
    onMaybeLater: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Enter, 2: Confirm
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    val currentInput = if (step == 1) firstPin else confirmPin

    Dialog(
        onDismissRequest = onMaybeLater,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1B1D26),
            border = BorderStroke(1.dp, Color(0xFF2E303A))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF7ED)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = if (step == 1) "Protect Your App" else "Confirm Passcode",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = if (errorText.isNotEmpty()) errorText
                    else if (step == 1) "Set a 4-digit PIN to secure your financial records."
                    else "Re-enter your 4-digit PIN to confirm.",
                    fontSize = 13.sp,
                    color = if (errorText.isNotEmpty()) Color(0xFFEF4444) else Color.LightGray,
                    textAlign = TextAlign.Center
                )

                // 4 PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isFilled = index < currentInput.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) Color(0xFF10B981) else Color.Transparent)
                                .border(
                                    width = 2.dp,
                                    color = if (isFilled) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Mini Keypad
                PinKeypad(
                    onDigitClick = { digit ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (step == 1) {
                            if (firstPin.length < 4) {
                                firstPin += digit
                                errorText = ""
                                if (firstPin.length == 4) {
                                    step = 2
                                }
                            }
                        } else {
                            if (confirmPin.length < 4) {
                                confirmPin += digit
                                errorText = ""
                                if (confirmPin.length == 4) {
                                    if (confirmPin == firstPin) {
                                        onSetPin(firstPin)
                                    } else {
                                        errorText = "Passcodes do not match. Try again."
                                        confirmPin = ""
                                        step = 1
                                        firstPin = ""
                                    }
                                }
                            }
                        }
                    },
                    onBackspaceClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (step == 1 && firstPin.isNotEmpty()) {
                            firstPin = firstPin.dropLast(1)
                        } else if (step == 2 && confirmPin.isNotEmpty()) {
                            confirmPin = confirmPin.dropLast(1)
                        }
                    },
                    onClearClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (step == 1) firstPin = "" else confirmPin = ""
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onMaybeLater) {
                        Text("Maybe later", color = Color.LightGray, fontWeight = FontWeight.SemiBold)
                    }

                    if (step == 2) {
                        TextButton(onClick = {
                            step = 1
                            firstPin = ""
                            confirmPin = ""
                            errorText = ""
                        }) {
                            Text("Reset", color = Color(0xFFF97316), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Change or Setup/Remove Passcode Security Dialog.
 */
@Composable
fun ChangePinDialog(
    currentAppPin: String?,
    onDismiss: () -> Unit,
    onSavePin: (String?) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Steps:
    // If currentAppPin exists: 1: Current PIN, 2: New PIN, 3: Confirm New PIN
    // If currentAppPin == null: 2: New PIN, 3: Confirm New PIN
    var step by remember { mutableIntStateOf(if (currentAppPin != null) 1 else 2) }

    var enteredCurrent by remember { mutableStateOf("") }
    var enteredNew by remember { mutableStateOf("") }
    var enteredConfirm by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }

    val activeInput = when (step) {
        1 -> enteredCurrent
        2 -> enteredNew
        else -> enteredConfirm
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1B1D26),
            border = BorderStroke(1.dp, Color(0xFF2E303A))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentAppPin != null) "Change Passcode" else "Setup Passcode",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Text(
                    text = if (errorText.isNotEmpty()) errorText
                    else when (step) {
                        1 -> "Enter your current 4-digit passcode."
                        2 -> "Enter your new 4-digit passcode."
                        else -> "Re-enter your new 4-digit passcode to confirm."
                    },
                    fontSize = 13.sp,
                    color = if (errorText.isNotEmpty()) Color(0xFFEF4444) else Color.LightGray,
                    textAlign = TextAlign.Center
                )

                // 4 PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isFilled = index < activeInput.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) Color(0xFF10B981) else Color.Transparent)
                                .border(
                                    width = 2.dp,
                                    color = if (isFilled) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Keypad
                PinKeypad(
                    onDigitClick = { digit ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        when (step) {
                            1 -> {
                                if (enteredCurrent.length < 4) {
                                    enteredCurrent += digit
                                    errorText = ""
                                    if (enteredCurrent.length == 4) {
                                        if (enteredCurrent == currentAppPin) {
                                            step = 2
                                        } else {
                                            errorText = "Incorrect current passcode."
                                            enteredCurrent = ""
                                        }
                                    }
                                }
                            }
                            2 -> {
                                if (enteredNew.length < 4) {
                                    enteredNew += digit
                                    errorText = ""
                                    if (enteredNew.length == 4) {
                                        step = 3
                                    }
                                }
                            }
                            3 -> {
                                if (enteredConfirm.length < 4) {
                                    enteredConfirm += digit
                                    errorText = ""
                                    if (enteredConfirm.length == 4) {
                                        if (enteredConfirm == enteredNew) {
                                            onSavePin(enteredNew)
                                            Toast.makeText(context, "Passcode updated successfully!", Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                        } else {
                                            errorText = "Passcodes do not match. Try again."
                                            enteredConfirm = ""
                                            step = 2
                                            enteredNew = ""
                                        }
                                    }
                                }
                            }
                        }
                    },
                    onBackspaceClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        when (step) {
                            1 -> if (enteredCurrent.isNotEmpty()) enteredCurrent = enteredCurrent.dropLast(1)
                            2 -> if (enteredNew.isNotEmpty()) enteredNew = enteredNew.dropLast(1)
                            3 -> if (enteredConfirm.isNotEmpty()) enteredConfirm = enteredConfirm.dropLast(1)
                        }
                    },
                    onClearClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        when (step) {
                            1 -> enteredCurrent = ""
                            2 -> enteredNew = ""
                            3 -> enteredConfirm = ""
                        }
                    }
                )

                // Remove Passcode option if currentAppPin exists
                if (currentAppPin != null && step == 1) {
                    TextButton(
                        onClick = {
                            if (enteredCurrent == currentAppPin) {
                                onSavePin(null)
                                Toast.makeText(context, "Passcode removed.", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            } else {
                                errorText = "Type current passcode first to remove."
                            }
                        }
                    ) {
                        Text("Turn Off Passcode Lock", color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}