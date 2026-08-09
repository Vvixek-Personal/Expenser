package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = when (themeModeState) {
      "dark" -> true
      "light" -> false
      "device" -> isSystemInDarkTheme()
      else -> isDarkModeActive
  },
  dynamicColor: Boolean = isFollowDeviceColorsState && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val colorScheme = when {
    dynamicColor && darkTheme -> dynamicDarkColorScheme(context)
    dynamicColor && !darkTheme -> dynamicLightColorScheme(context)
    darkTheme -> darkColorScheme(
      primary = SleekPrimary,
      secondary = SleekPrimaryContainer,
      tertiary = SavingGold,
      background = SleekBg,
      surface = SleekSurface,
      onPrimary = SleekBg,
      onSecondary = SleekTextPrimary,
      onBackground = SleekTextPrimary,
      onSurface = SleekTextPrimary,
      outline = SleekBorder
    )
    else -> lightColorScheme(
      primary = SleekPrimary,
      secondary = SleekPrimaryContainer,
      tertiary = SavingGold,
      background = SleekBg,
      surface = SleekSurface,
      onPrimary = SleekSurface,
      onSecondary = SleekOnPrimaryContainer,
      onBackground = SleekTextPrimary,
      onSurface = SleekTextPrimary,
      outline = SleekBorder
    )
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
