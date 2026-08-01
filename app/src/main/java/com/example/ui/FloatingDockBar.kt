package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.isDarkModeActive
import com.example.ui.theme.mixPrimaryWithColor

/**
 * Image-3 Inspired Floating Dock Bar:
 * Translucent glass container floating at bottom of screen.
 * Active item expands into a white capsule containing a white circle icon and text label ("Home", etc.).
 * Inactive items are sleek circular outline buttons with icons.
 */
@Composable
fun FloatingDockBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    selectedLanguage: String = "English",
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    // Theme-adaptive translucent glass tint
    val baseTint = if (isDarkModeActive) Color(0xFF161922) else Color(0xFF1E293B)
    val dockContainerBg = mixPrimaryWithColor(SleekPrimary, baseTint, 0.45f).copy(alpha = 0.88f)
    val dockBorderColor = mixPrimaryWithColor(SleekPrimary, Color.White, 0.5f).copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Translucent Floating Capsule Container (3rd Image Style)
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(40.dp),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(40.dp))
                .background(dockContainerBg)
                .border(
                    border = BorderStroke(1.5.dp, dockBorderColor),
                    shape = RoundedCornerShape(40.dp)
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Home (Dashboard)
            ImageInspiredDockTile(
                icon = Icons.Default.Dashboard,
                title = LanguageManager.tr("Home", selectedLanguage),
                isSelected = currentScreen == Screen.Dashboard,
                testTag = "nav_item_dashboard",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onScreenSelected(Screen.Dashboard)
                }
            )

            // Tab 2: Finance (Expenses)
            ImageInspiredDockTile(
                icon = Icons.Default.ReceiptLong,
                title = LanguageManager.tr("Transactions", selectedLanguage),
                isSelected = currentScreen == Screen.Expenses,
                testTag = "nav_item_expenses",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onScreenSelected(Screen.Expenses)
                }
            )

            // Tab 3: Analytics
            ImageInspiredDockTile(
                icon = Icons.Default.PieChart,
                title = LanguageManager.tr("Analytics", selectedLanguage),
                isSelected = currentScreen == Screen.Analytics,
                testTag = "nav_item_analytics",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onScreenSelected(Screen.Analytics)
                }
            )

            // Tab 4: Calendar
            ImageInspiredDockTile(
                icon = Icons.Default.CalendarMonth,
                title = LanguageManager.tr("Calendar", selectedLanguage),
                isSelected = currentScreen == Screen.Calendar,
                testTag = "nav_item_calendar",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onScreenSelected(Screen.Calendar)
                }
            )
        }
    }
}

@Composable
private fun ImageInspiredDockTile(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    isActionTile: Boolean = false
) {
    val activeBg = Color.White
    val inactiveCircleBorder = Color(0xFFC8DCCE).copy(alpha = 0.45f)
    val inactiveIconColor = Color.White

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .testTag(testTag)
            .height(48.dp)
            .clip(CircleShape)
            .background(if (isSelected) activeBg else Color.Transparent)
            .border(
                border = BorderStroke(
                    width = if (isSelected) 0.dp else 1.dp,
                    color = if (isSelected) Color.Transparent else inactiveCircleBorder
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = if (isSelected) 14.dp else 12.dp,
                vertical = 6.dp
            )
    ) {
        if (isSelected) {
            // White circle icon container with dark icon inside (Matching Image 3 active pill)
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = Color(0xFF1E293B),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            // Inactive item: Circular outline button with white icon inside
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isActionTile) SleekPrimary else inactiveIconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

