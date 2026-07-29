package com.example.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

/**
 * Floating Dock Bar modeled after the macOS/iOS taskbar style,
 * dynamically regulated by the active application Theme Palette.
 * Expanded horizontally across the bottom screen with balanced squircle tiles.
 */
@Composable
fun FloatingDockBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    onAddClick: () -> Unit,
    selectedLanguage: String = "English",
    modifier: Modifier = Modifier
) {
    // Theme palette derived colors
    val containerBg = SleekSurface
    val containerBorder = SleekBorder
    val primaryAccent = SleekPrimary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Expanded Horizontally Theme-Regulated Capsule Container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = primaryAccent.copy(alpha = 0.25f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            containerBg,
                            containerBg.copy(alpha = 0.95f)
                        )
                    )
                )
                .border(
                    border = BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryAccent.copy(alpha = 0.3f),
                                containerBorder
                            )
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tile 1: Dashboard
            DockTile(
                icon = Icons.Default.Dashboard,
                title = LanguageManager.tr("Dashboard", selectedLanguage),
                isSelected = currentScreen == Screen.Dashboard,
                testTag = "nav_item_dashboard",
                modifier = Modifier.weight(1f),
                onClick = { onScreenSelected(Screen.Dashboard) }
            )

            // Tile 2: Expenses
            DockTile(
                icon = Icons.Default.ReceiptLong,
                title = LanguageManager.tr("Expenses", selectedLanguage),
                isSelected = currentScreen == Screen.Expenses,
                testTag = "nav_item_expenses",
                modifier = Modifier.weight(1f),
                onClick = { onScreenSelected(Screen.Expenses) }
            )

            // Tile 3: Add (Center Action Tile)
            DockTile(
                icon = Icons.Default.Add,
                title = LanguageManager.tr("Add", selectedLanguage),
                isSelected = false,
                isActionTile = true,
                testTag = "nav_item_add",
                modifier = Modifier.weight(1f),
                onClick = onAddClick
            )

            // Tile 4: Analytics
            DockTile(
                icon = Icons.Default.PieChart,
                title = LanguageManager.tr("Analytics", selectedLanguage),
                isSelected = currentScreen == Screen.Analytics,
                testTag = "nav_item_analytics",
                modifier = Modifier.weight(1f),
                onClick = { onScreenSelected(Screen.Analytics) }
            )

            // Tile 5: Calendar
            DockTile(
                icon = Icons.Default.CalendarMonth,
                title = LanguageManager.tr("Calendar", selectedLanguage),
                isSelected = currentScreen == Screen.Calendar,
                testTag = "nav_item_calendar",
                modifier = Modifier.weight(1f),
                onClick = { onScreenSelected(Screen.Calendar) }
            )
        }
    }
}

@Composable
private fun DockTile(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActionTile: Boolean = false
) {
    val primaryAccent = SleekPrimary
    val primaryContainer = SleekPrimaryContainer
    val textSecondary = SleekTextSecondary
    val surfaceColor = SleekSurface
    val borderColor = SleekBorder

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dock_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .height(44.dp)
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = when {
                        isActionTile -> Brush.linearGradient(
                            listOf(primaryAccent, primaryAccent.copy(alpha = 0.85f))
                        )
                        isSelected -> Brush.linearGradient(
                            listOf(primaryAccent, primaryAccent.copy(alpha = 0.8f))
                        )
                        else -> Brush.linearGradient(
                            listOf(
                                primaryContainer.copy(alpha = 0.35f),
                                surfaceColor.copy(alpha = 0.5f)
                            )
                        )
                    }
                )
                .border(
                    border = BorderStroke(
                        width = if (isSelected || isActionTile) 1.5.dp else 1.dp,
                        color = if (isSelected || isActionTile) {
                            primaryAccent.copy(alpha = 0.9f)
                        } else {
                            borderColor.copy(alpha = 0.6f)
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected || isActionTile) {
                    Color.White
                } else {
                    textSecondary
                },
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // macOS Dock Style Active Indicator Dot
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(width = 12.dp, height = 3.dp)
                    .clip(CircleShape)
                    .background(primaryAccent)
            )
        } else {
            Spacer(modifier = Modifier.height(3.dp))
        }
    }
}
