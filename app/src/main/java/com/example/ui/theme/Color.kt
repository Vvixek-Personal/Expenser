package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Sleek Interface Theme Colors (Backed by dynamic Compose states)
var activeSleekPrimary by mutableStateOf(Color(0xFF0061A4))
var activeSleekPrimaryContainer by mutableStateOf(Color(0xFFD3E4FF))
var activeSleekOnPrimaryContainer by mutableStateOf(Color(0xFF001D36))

val SleekPrimary: Color get() = activeSleekPrimary
val SleekPrimaryContainer: Color get() = activeSleekPrimaryContainer
val SleekOnPrimaryContainer: Color get() = activeSleekOnPrimaryContainer

// Predefined Themes Palette
fun getPresetThemeColors(index: Int, customHue: Float): Triple<Color, Color, Color> {
    return when (index) {
        0 -> Triple(Color(0xFF0061A4), Color(0xFFD3E4FF), Color(0xFF001D36)) // Classic Blue
        1 -> Triple(Color(0xFF5A6370), Color(0xFFE2E8F0), Color(0xFF1E293B)) // Slate Grey
        2 -> Triple(Color(0xFF1A365D), Color(0xFFDBEAFE), Color(0xFF1E3A8A)) // Navy Blue
        3 -> Triple(Color(0xFF2B4C7E), Color(0xFFDCE6F1), Color(0xFF1A3050)) // Steel Blue
        4 -> Triple(Color(0xFF4E6B50), Color(0xFFE2EFE3), Color(0xFF263A28)) // Sage Green
        5 -> Triple(Color(0xFF0D9488), Color(0xFFCCFBF1), Color(0xFF115E59)) // Teal
        6 -> Triple(Color(0xFF16A34A), Color(0xFFDCFCE7), Color(0xFF15803D)) // Forest Green
        7 -> Triple(Color(0xFF65A30D), Color(0xFFECFCCB), Color(0xFF3F6212)) // Olive Green
        8 -> Triple(Color(0xFFD97706), Color(0xFFFEF3C7), Color(0xFF92400E)) // Gold/Amber
        9 -> Triple(Color(0xFFEA580C), Color(0xFFFFEDD5), Color(0xFF9A3412)) // Bronze/Orange
        10 -> Triple(Color(0xFF854D0E), Color(0xFFFEF3C7), Color(0xFF78350F)) // Brown/Cocoa
        11 -> Triple(Color(0xFFE11D48), Color(0xFFFFE4E6), Color(0xFF9F1239)) // Rose
        12 -> Triple(Color(0xFF9D174D), Color(0xFFFCE7F3), Color(0xFF701A75)) // Plum
        13 -> Triple(Color(0xFF7C3AED), Color(0xFFEDE9FE), Color(0xFF5B21B6)) // Purple
        14 -> Triple(Color(0xFF6366F1), Color(0xFFE0E7FF), Color(0xFF3730A3)) // Lavender
        15 -> { // Dynamic custom HSL/HSV color
            val p = Color.hsv(customHue, 0.75f, 0.65f)
            val pc = Color.hsv(customHue, 0.25f, 0.92f)
            val opc = Color.hsv(customHue, 0.9f, 0.25f)
            Triple(p, pc, opc)
        }
        else -> Triple(Color(0xFF0061A4), Color(0xFFD3E4FF), Color(0xFF001D36))
    }
}

var isDarkModeActive by mutableStateOf(false)

fun updateThemeColors(index: Int, customHue: Float) {
    val (p, pc, opc) = getPresetThemeColors(index, customHue)
    activeSleekPrimary = p
    activeSleekPrimaryContainer = pc
    activeSleekOnPrimaryContainer = opc
}

fun mixPrimaryWithColor(primary: Color, base: Color, fraction: Float): Color {
    return Color(
        red = (primary.red * fraction + base.red * (1f - fraction)).coerceIn(0f, 1f),
        green = (primary.green * fraction + base.green * (1f - fraction)).coerceIn(0f, 1f),
        blue = (primary.blue * fraction + base.blue * (1f - fraction)).coerceIn(0f, 1f),
        alpha = 1.0f
    )
}

val SleekBg: Color get() = if (isDarkModeActive) {
    mixPrimaryWithColor(activeSleekPrimary, Color(0xFF111215), 0.04f)
} else {
    mixPrimaryWithColor(activeSleekPrimary, Color(0xFFFAFAFC), 0.06f)
}

val SleekSurface: Color get() = if (isDarkModeActive) {
    mixPrimaryWithColor(activeSleekPrimary, Color(0xFF1C1D21), 0.05f)
} else {
    Color(0xFFFFFFFF)
}

val SleekBorder: Color get() = if (isDarkModeActive) {
    Color(0xFF2C2F36)
} else {
    Color(0xFFE1E3E8)
}

val SleekTextPrimary: Color get() = if (isDarkModeActive) {
    Color(0xFFF1F1F5)
} else {
    Color(0xFF1A1C1E)
}

val SleekTextSecondary: Color get() = if (isDarkModeActive) {
    Color(0xFF9CA3AF)
} else {
    Color(0xFF44474E)
}

val SleekNeutralLight = Color(0xFFE2E2E6)

// Semantic Alerts (Sleek Theme Palette)
val ExpenseRed = Color(0xFFBA1A1A)
val ExpenseRedBg = Color(0xFFFDE2E4)
val IncomeGreen = Color(0xFF146C2E)
val IncomeGreenBg = Color(0xFFD1F2EB)
val SavingGold = Color(0xFF0061A4)
val WarningOrange = Color(0xFFF59E0B)
val InfoBlue = Color(0xFF0061A4)

// Backward-compatible aliases for "Sleek Interface" look
val SlateDarkBg = SleekBg
val SlateDarkCard = SleekSurface
val SlateDarkBorder = SleekBorder
val SlateTextPrimary = SleekTextPrimary
val SlateTextSecondary = SleekTextSecondary
val EmeraldPrimary = SleekPrimary
val EmeraldSecondary = SleekPrimaryContainer
val AmberTertiary = SleekPrimaryContainer
