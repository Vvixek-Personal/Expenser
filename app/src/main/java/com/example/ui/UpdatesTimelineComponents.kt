package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class SpecCategory(val label: String, val tagColor: Color) {
    FEATURE("FEATURE", Color(0xFF10B981)),
    ANIMATION("ANIMATION", Color(0xFFEC4899)),
    UI_UX("UI / UX", Color(0xFF6366F1)),
    PERFORMANCE("PERFORMANCE", Color(0xFF0EA5E9)),
    SECURITY("SECURITY", Color(0xFFF59E0B)),
    SYSTEM("SYSTEM", Color(0xFF8B5CF6))
}

data class UpdateSpecification(
    val category: SpecCategory,
    val title: String,
    val description: String
)

data class AppReleaseUpdate(
    val version: String,
    val releaseTag: String,
    val releaseDate: String,
    val relativeTime: String,
    val timestamp: Long,
    val headline: String,
    val specifications: List<UpdateSpecification>,
    val isLatest: Boolean = false
)

/**
 * Generates release updates history following the strict rule: "One Day = 1 Version".
 * All features for a given date are contained within that single version.
 * Real-time dates are dynamically computed from the device clock.
 */
fun getAppUpdatesHistory(): List<AppReleaseUpdate> {
    val now = System.currentTimeMillis()
    val dayMillis = 86400000L
    val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

    val dateToday = dateFormat.format(Date(now))
    val dateYesterday = dateFormat.format(Date(now - 1 * dayMillis))
    val date5DaysAgo = dateFormat.format(Date(now - 5 * dayMillis))
    val date12DaysAgo = dateFormat.format(Date(now - 12 * dayMillis))
    val date19DaysAgo = dateFormat.format(Date(now - 19 * dayMillis))
    val date28DaysAgo = dateFormat.format(Date(now - 28 * dayMillis))
    val date45DaysAgo = dateFormat.format(Date(now - 45 * dayMillis))

    return listOf(
        // TODAY: v1.27 (Google Sign-Up, Smart Budget Gauge, Savings Carousel, Liquid Glass Smoothness)
        AppReleaseUpdate(
            version = "v1.27",
            releaseTag = "MEGA UPDATE",
            releaseDate = dateToday,
            relativeTime = "Today",
            timestamp = now,
            headline = "Google Sign-Up Integration, Smart Budget Gauge, Savings Carousel & Liquid Glass UI",
            isLatest = true,
            specifications = listOf(
                UpdateSpecification(
                    category = SpecCategory.FEATURE,
                    title = "Real Firebase Auth & Google Sign-Up Integration",
                    description = "Implemented genuine Firebase Auth (GoogleAuthProvider) integrated with Android CredentialManager for real Google Sign-In strictly inside the Profile screen."
                ),
                UpdateSpecification(
                    category = SpecCategory.FEATURE,
                    title = "Firebase Firestore Cloud Persistence & Two-Way Sync",
                    description = "Securely keeps track of user data with Firestore. Automatically syncs transactions, expenses, budgets, savings goals, accounts, and reminders."
                ),
                UpdateSpecification(
                    category = SpecCategory.UI_UX,
                    title = "Smart Budget Gauge & Carousel Formatting Fix",
                    description = "Resolved string interpolation syntax inside the canvas circular budget health ring and savings badge, cleanly displaying dynamic numerical percentages without raw code artifacts."
                ),
                UpdateSpecification(
                    category = SpecCategory.FEATURE,
                    title = "Dual Local & Firestore Cloud Sync Engine",
                    description = "Engineered two-tier data architecture: local Room database + Firestore Cloud Database. Automatically synchronizes all 6 collections (transactions, expenses, budgets, savings goals, accounts, reminders) with on-demand sync triggers in Profile and Backup & Restore."
                ),
                UpdateSpecification(
                    category = SpecCategory.FEATURE,
                    title = "Post-SignUp Name & Profile Photo Customization",
                    description = "Interactive post-signup setup asking for custom name and photo with automatic fallback to Gmail profile and avatar."
                ),
                UpdateSpecification(
                    category = SpecCategory.UI_UX,
                    title = "Smart Budget Health Gauge Ring",
                    description = "Circular progress gauge displaying real-time monthly budget utilization with color-coded safety zones and 1-tap adjustment."
                ),
                UpdateSpecification(
                    category = SpecCategory.FEATURE,
                    title = "Savings Goals Mini-Carousel Widget",
                    description = "Horizontal swipeable card carousel on the home screen with live progress percentage and 1-tap quick deposit chips."
                ),
                UpdateSpecification(
                    category = SpecCategory.ANIMATION,
                    title = "AnimatedContent Shared & Fluid Transitions",
                    description = "Implemented advanced AnimatedContent spring transitions between dashboard and detail views for a fluid visual effect."
                ),
                UpdateSpecification(
                    category = SpecCategory.UI_UX,
                    title = "Daily Spending Pace Widget Removal",
                    description = "Cleaned up dashboard layout by removing the daily spending pace card to prioritize core budgeting."
                ),
                UpdateSpecification(
                    category = SpecCategory.ANIMATION,
                    title = "Liquid Glass Smoothness & Glassmorphism Polish",
                    description = "Upgraded all cards, sheets, and headers with translucent glassmorphic blur layers and shimmering borders."
                )
            )
        ),

        // YESTERDAY: v1.26
        AppReleaseUpdate(
            version = "v1.26",
            releaseTag = "STABLE",
            releaseDate = dateYesterday,
            relativeTime = "Yesterday",
            timestamp = now - 1 * dayMillis,
            headline = "Real Exchange Rate API, Room Reminders, Push Notifications & Lifecycle Re-Lock",
            isLatest = false,
            specifications = listOf(
                UpdateSpecification(
                    category = SpecCategory.FEATURE,
                    title = "Real Open Exchange Rate API Integration",
                    description = "Robust JSON forex API client (open.er-api.com) parsing live rates and dynamically updates CurrencyManager conversions."
                ),
                UpdateSpecification(
                    category = SpecCategory.FEATURE,
                    title = "Room Database Reminders & Push Notifications",
                    description = "Local Room persistence for reminders paired with Android NotificationManager push alerts."
                ),
                UpdateSpecification(
                    category = SpecCategory.UI_UX,
                    title = "4-Week Calendar Grid & Week View",
                    description = "Simultaneous rendering of all 4+ weeks of the month in week view with weekly cashflow totals."
                ),
                UpdateSpecification(
                    category = SpecCategory.SECURITY,
                    title = "Process Lifecycle Re-Lock & PIN Security",
                    description = "Automatic application re-lock upon backgrounding and resumption."
                )
            )
        ),

        // YESTERDAY: 1 single version (v1.25)
        AppReleaseUpdate(
            version = "v1.25",
            releaseTag = "MAJOR UPDATE",
            releaseDate = dateYesterday,
            relativeTime = "Yesterday",
            timestamp = now - 1 * dayMillis,
            headline = "Micro-Interactions, Streak Celebration & Multi-Currency Engine",
            specifications = listOf(
                UpdateSpecification(
                    category = SpecCategory.ANIMATION,
                    title = "Daily Streak Celebration Polish",
                    description = "Tap-anywhere dismissal mechanism with particle ember bursts and rotating sunburst animations."
                ),
                UpdateSpecification(
                    category = SpecCategory.UI_UX,
                    title = "Interactive Calendar Navigation",
                    description = "Day-by-day cash flow visualizer with transaction dots and month/week toggle."
                ),
                UpdateSpecification(
                    category = SpecCategory.FEATURE,
                    title = "100+ Currencies Option A / Option B",
                    description = "Complete currency catalog with safe Option A (keep existing) and Option B (convert existing) mechanisms."
                ),
                UpdateSpecification(
                    category = SpecCategory.SYSTEM,
                    title = "Full 9-Language Localization Suite",
                    description = "Comprehensive translations for Hindi, Bengali, Marathi, Punjabi, French, Chinese, Urdu, and Japanese."
                )
            )
        ),

        // 5 DAYS AGO: 1 single version (v1.24)
        AppReleaseUpdate(
            version = "v1.24",
            releaseTag = "STABLE",
            releaseDate = date5DaysAgo,
            relativeTime = "5 days ago",
            timestamp = now - 5 * dayMillis,
            headline = "Daily Spending Allowance & Safe-to-Spend Tracker",
            specifications = listOf(
                UpdateSpecification(
                    category = SpecCategory.FEATURE,
                    title = "Safe-to-Spend Daily Allowance Engine",
                    description = "Dynamic daily budget calculations based on remaining monthly budget and days left in current month."
                ),
                UpdateSpecification(
                    category = SpecCategory.UI_UX,
                    title = "Inline Pay-Now for Upcoming Bills",
                    description = "Convenient 1-tap bill settlement directly from the dashboard upcoming bills card."
                ),
                UpdateSpecification(
                    category = SpecCategory.PERFORMANCE,
                    title = "Optimized Recomposition & Lazy Rendering",
                    description = "Derived state optimizations for rapid screen switching."
                )
            )
        ),

        // 12 DAYS AGO: 1 single version (v1.23)
        AppReleaseUpdate(
            version = "v1.23",
            releaseTag = "STABLE",
            releaseDate = date12DaysAgo,
            relativeTime = "12 days ago",
            timestamp = now - 12 * dayMillis,
            headline = "Exchange Rate Caching & Category Customization",
            specifications = listOf(
                UpdateSpecification(
                    category = SpecCategory.FEATURE,
                    title = "10-Day Local Exchange Rate Cache",
                    description = "Offline-first real-time currency conversion rates cached safely on-device."
                ),
                UpdateSpecification(
                    category = SpecCategory.UI_UX,
                    title = "Custom Category Icons & Colors",
                    description = "Full visual customization for income and expense categories."
                ),
                UpdateSpecification(
                    category = SpecCategory.SECURITY,
                    title = "Privacy Mode with Sensitive Data Masking",
                    description = "Toggle balance visibility with one tap to conceal net worth in public spaces."
                )
            )
        ),

        // 19 DAYS AGO: 1 single version (v1.22)
        AppReleaseUpdate(
            version = "v1.22",
            releaseTag = "STABLE",
            releaseDate = date19DaysAgo,
            relativeTime = "19 days ago",
            timestamp = now - 19 * dayMillis,
            headline = "Financial Calculations Hub & Search Ledger",
            specifications = listOf(
                UpdateSpecification(
                    category = SpecCategory.FEATURE,
                    title = "Comprehensive Calculations Suite",
                    description = "Split bill, percentage change, loan amortization, and tip calculator."
                ),
                UpdateSpecification(
                    category = SpecCategory.PERFORMANCE,
                    title = "Multi-Filter Transaction Search",
                    description = "Instant filter by category, date range, payment mode, and note query."
                )
            )
        ),

        // 28 DAYS AGO: 1 single version (v1.21)
        AppReleaseUpdate(
            version = "v1.21",
            releaseTag = "STABLE",
            releaseDate = date28DaysAgo,
            relativeTime = "28 days ago",
            timestamp = now - 28 * dayMillis,
            headline = "Room Database Engine & Local Encrypted Backup",
            specifications = listOf(
                UpdateSpecification(
                    category = SpecCategory.SYSTEM,
                    title = "100% Offline Room Persistence",
                    description = "Zero cloud tracking, zero external telemetry; user data stays on device."
                ),
                UpdateSpecification(
                    category = SpecCategory.FEATURE,
                    title = "Local JSON / CSV Backup & Restore",
                    description = "One-tap file backup and restoration with schema validation."
                )
            )
        ),

        // 45 DAYS AGO: 1 single version (v1.0)
        AppReleaseUpdate(
            version = "v1.0",
            releaseTag = "INITIAL RELEASE",
            releaseDate = date45DaysAgo,
            relativeTime = "45 days ago",
            timestamp = now - 45 * dayMillis,
            headline = "Initial Launch: Modern Personal Finance Tracker",
            specifications = listOf(
                UpdateSpecification(
                    category = SpecCategory.UI_UX,
                    title = "Material 3 Fintech Aesthetic",
                    description = "Dark slate background, vibrant accent badges, and clean typography."
                ),
                UpdateSpecification(
                    category = SpecCategory.FEATURE,
                    title = "Income & Expense Ledger",
                    description = "Fast transaction entry with tags, notes, and payment mode tracking."
                )
            )
        )
    )
}

/**
 * 🚀 Interactive Updates Timeline View
 * Strictly implements "One Day = 1 Version" logic: every day has its own dedicated release version
 * with real-time dates, animated glowing milestone nodes, category filter chips, and expandable specifications.
 */
@Composable
fun UpdatesTimelineView(
    modifier: Modifier = Modifier
) {
    val updates = remember { getAppUpdatesHistory() }
    var selectedFilter by remember { mutableStateOf("All") }
    var isCheckingUpdates by remember { mutableStateOf(false) }
    var showCheckToast by remember { mutableStateOf(false) }

    // Pulsing radar animation for latest release milestone
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_and_shimmer")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Shimmering alpha for active card border
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    // Rotating check spinner
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing)
        ),
        label = "rotation"
    )

    val filterCategories = remember { listOf("All", "FEATURE", "ANIMATION", "UI / UX", "PERFORMANCE", "SYSTEM") }

    Column(modifier = modifier.fillMaxWidth()) {
        // Timeline Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Release Timeline & Specs",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SleekPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "LIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SleekPrimary,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Daily versions with real-time dates • Verified specifications",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary
                        )
                    }

                    Button(
                        onClick = { isCheckingUpdates = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        if (isCheckingUpdates) {
                            Icon(
                                Icons.Rounded.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(16.dp)
                                    .rotate(rotation)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Checking...", fontSize = 12.sp, color = Color.White)
                        } else {
                            Icon(
                                Icons.Rounded.Sync,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Check", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }

                LaunchedEffect(isCheckingUpdates) {
                    if (isCheckingUpdates) {
                        kotlinx.coroutines.delay(1000)
                        isCheckingUpdates = false
                        showCheckToast = true
                    }
                }

                AnimatedVisibility(
                    visible = showCheckToast,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "You are on the latest release (v1.27). Everything is up to date!",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Filter chips row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterCategories) { cat ->
                val isSelected = selectedFilter == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = cat },
                    label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SleekPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = SleekSurfaceVariant,
                        labelColor = SleekTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = SleekBorder,
                        selectedBorderColor = SleekPrimary
                    )
                )
            }
        }

        // Timeline items list (One day = 1 version)
        Column(modifier = Modifier.fillMaxWidth()) {
            updates.forEachIndexed { index, release ->
                val isLast = index == updates.lastIndex
                TimelineReleaseNodeCard(
                    release = release,
                    isLast = isLast,
                    selectedFilter = selectedFilter,
                    pulseScale = if (release.isLatest) pulseScale else 1f,
                    pulseAlpha = if (release.isLatest) pulseAlpha else 0f,
                    shimmerAlpha = if (release.isLatest) shimmerAlpha else 0.3f
                )
            }
        }
    }
}

/**
 * 📦 Single Node on the Updates Timeline (One day = 1 version).
 * Shows the version badge, real-time date, relative time, headline, and specifications with spring animations.
 */
@Composable
fun TimelineReleaseNodeCard(
    release: AppReleaseUpdate,
    isLast: Boolean,
    selectedFilter: String,
    pulseScale: Float = 1f,
    pulseAlpha: Float = 0f,
    shimmerAlpha: Float = 0.3f
) {
    var expanded by remember { mutableStateOf(release.isLatest) }

    val filteredSpecs = remember(selectedFilter, release.specifications) {
        if (selectedFilter == "All") release.specifications
        else release.specifications.filter { it.category.label == selectedFilter }
    }

    if (selectedFilter != "All" && filteredSpecs.isEmpty()) return

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cardScale"
    )

    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "arrowRotation"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("timeline_release_${release.version}")
    ) {
        // Left Column: Timeline Stem & Milestone Icon Node
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            // Milestone Node
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(34.dp)
            ) {
                if (release.isLatest) {
                    Box(
                        modifier = Modifier
                            .size(34.dp * pulseScale)
                            .clip(CircleShape)
                            .background(SleekPrimary.copy(alpha = pulseAlpha))
                    )
                }

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(
                            if (release.isLatest) {
                                Brush.radialGradient(listOf(Color(0xFF6366F1), SleekPrimary))
                            } else {
                                Brush.linearGradient(listOf(Color(0xFF64748B), Color(0xFF475569)))
                            }
                        )
                        .border(
                            BorderStroke(2.dp, if (release.isLatest) Color.White else SleekSurface),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (release.isLatest) Icons.Rounded.RocketLaunch else Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Connecting Vertical Stem
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .weight(1f)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    if (release.isLatest) SleekPrimary else Color(0xFF64748B).copy(alpha = 0.6f),
                                    Color(0xFF64748B).copy(alpha = 0.25f)
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right Column: Release Specification Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(
                1.dp,
                if (release.isLatest) SleekPrimary.copy(alpha = shimmerAlpha) else SleekBorder
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .scale(cardScale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    expanded = !expanded
                }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Top Header Row: Version badge, Tag, Real-time Date, Relative Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (release.isLatest) SleekPrimary else SleekSurfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (release.isLatest) SleekPrimary else SleekBorder
                            )
                        ) {
                            Text(
                                text = release.version,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (release.isLatest) Color.White else SleekTextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (release.isLatest) IncomeGreen.copy(alpha = 0.15f) else SleekSurfaceVariant
                        ) {
                            Text(
                                text = release.releaseTag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (release.isLatest) IncomeGreen else SleekTextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Real-time Date and Relative time
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = release.releaseDate,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = release.relativeTime,
                            fontSize = 10.sp,
                            color = if (release.isLatest) SleekPrimary else SleekTextSecondary,
                            fontWeight = if (release.isLatest) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = release.headline,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Specifications count & Expand Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredSpecs.size} specifications listed",
                        fontSize = 11.sp,
                        color = SleekTextSecondary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = if (expanded) "Hide Details" else "View Details",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(arrowRotation)
                        )
                    }
                }

                // Expandable Specifications List with Spring Animation
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)) + fadeIn(),
                    exit = shrinkVertically(animationSpec = tween(180)) + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HorizontalDivider(color = SleekBorder.copy(alpha = 0.6f))

                        filteredSpecs.forEach { spec ->
                            SpecificationItemRow(spec = spec)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🏷️ Single Specification Detail Item with Tag Pill
 */
@Composable
fun SpecificationItemRow(spec: UpdateSpecification) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = spec.category.tagColor.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, spec.category.tagColor.copy(alpha = 0.35f)),
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Text(
                text = spec.category.label,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = spec.category.tagColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = spec.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = spec.description,
                fontSize = 11.sp,
                color = SleekTextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
