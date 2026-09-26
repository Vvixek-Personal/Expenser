package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ui.theme.*

/**
 * Authentic 4-color Google 'G' Logo rendered cleanly via Canvas.
 */
@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val minDim = minOf(width, height)
        val strokeWidth = minDim * 0.19f
        val radius = (minDim - strokeWidth) / 2f
        val center = Offset(width / 2f, height / 2f)

        // Red top arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 190f,
            sweepAngle = 120f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Yellow bottom-left arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 120f,
            sweepAngle = 60f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Green bottom arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 40f,
            sweepAngle = 75f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Blue right arc
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -45f,
            sweepAngle = 80f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Blue horizontal crossbar
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(center.x - radius * 0.1f, center.y),
            end = Offset(center.x + radius + strokeWidth * 0.25f, center.y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Exclusive Google Sign-Up button for the Profile screen.
 * Exactly one button exists in the app as requested.
 */
@Composable
fun GoogleSignUpButton(
    onClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("google_signup_button")
            .clickable(enabled = !isLoading) { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color(0xFF4285F4),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Connecting with Google…",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            } else {
                GoogleLogoIcon(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sign up with Google",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            }
        }
    }
}

/**
 * Status card shown when Google Account is linked in Profile.
 * Displays Firestore cloud sync status and Sync Now button.
 */
@Composable
fun GoogleAccountConnectedCard(
    email: String?,
    isSyncing: Boolean = false,
    lastSyncTime: String? = null,
    onSyncNow: () -> Unit = {},
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, Color(0xFF4285F4).copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("google_connected_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4285F4).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    GoogleLogoIcon(modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Connected with Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SleekTextPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = email ?: "user@gmail.com",
                        fontSize = 12.sp,
                        color = SleekTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                TextButton(
                    onClick = onSignOut,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Sign out",
                        tint = SleekTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sign out",
                        fontSize = 12.sp,
                        color = SleekTextSecondary
                    )
                }
            }

            Divider(color = SleekBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

            // Firestore Cloud Sync Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4285F4),
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        Text(
                            text = "Firestore Cloud Sync Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = if (lastSyncTime != null) "Last: $lastSyncTime" else "Syncing all data to cloud…",
                            fontSize = 10.sp,
                            color = SleekTextSecondary
                        )
                    }
                }

                Button(
                    onClick = onSyncNow,
                    enabled = !isSyncing,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Syncing…", fontSize = 11.sp, color = Color.White)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync Now", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Google Credential Configuration Dialog.
 * Shown when Google Play Services / Firebase requires custom Web Client ID setup.
 */
@Composable
fun GoogleSetupGuidanceDialog(
    initialClientId: String?,
    onDismiss: () -> Unit,
    onSaveAndRetry: (String) -> Unit
) {
    var clientIdText by remember { mutableStateOf(initialClientId ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GoogleLogoIcon(modifier = Modifier.size(36.dp))

                Text(
                    text = "Google Sign-In Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Firebase Auth and CredentialManager require a Google Cloud OAuth Web Client ID or a google-services.json file to authenticate with Google servers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = clientIdText,
                    onValueChange = { clientIdText = it },
                    label = { Text("OAuth Web Client ID") },
                    placeholder = { Text("e.g. 123456-abc.apps.googleusercontent.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSaveAndRetry(clientIdText.trim()) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                    ) {
                        Text("Save & Retry", color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Post-Signup Dialog:
 * "After signup just ask for name and profile pic. if not give use gmail profile"
 */
@Composable
fun GooglePostSignUpProfileDialog(
    gmailEmail: String,
    gmailDefaultName: String,
    gmailDefaultPhotoUrl: String?,
    onDismiss: () -> Unit,
    onUseGmailProfile: () -> Unit,
    onSaveCustomProfile: (customName: String?, customPhotoUri: String?) -> Unit
) {
    var enteredName by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri.toString()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoogleLogoIcon(modifier = Modifier.size(20.dp))
                    Text(
                        text = "Google Sign-Up Successful!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                }

                Text(
                    text = "Connected as $gmailEmail",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "You can customize your name and photo below, or tap 'Use Gmail Profile' to keep your Google account details.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )

                // Profile Picture Preview with Camera Picker Button
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier
                        .size(92.dp)
                        .clickable { imagePickerLauncher.launch("image/*") }
                ) {
                    val previewPhoto = selectedPhotoUri ?: gmailDefaultPhotoUrl
                    if (!previewPhoto.isNullOrBlank()) {
                        AsyncImage(
                            model = previewPhoto,
                            contentDescription = "Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF4285F4).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = gmailDefaultName.firstOrNull()?.uppercase() ?: "G",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4285F4)
                            )
                        }
                    }

                    // Camera Badge
                    Surface(
                        shape = CircleShape,
                        color = SleekPrimary,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Pick photo",
                            tint = Color.White,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }

                // If user selected custom photo, show option to clear and revert
                if (selectedPhotoUri != null) {
                    TextButton(
                        onClick = { selectedPhotoUri = null },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Revert to Gmail photo", fontSize = 11.sp, color = SleekPrimary)
                    }
                }

                // Name Input
                OutlinedTextField(
                    value = enteredName,
                    onValueChange = { enteredName = it },
                    label = { Text("Name (Optional)") },
                    placeholder = { Text(gmailDefaultName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = {
                        Text(
                            text = if (enteredName.isBlank()) "Will default to '$gmailDefaultName'" else "Custom name entered",
                            fontSize = 11.sp,
                            color = SleekTextSecondary
                        )
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Primary: Save (will use custom if provided, otherwise Gmail profile)
                    Button(
                        onClick = {
                            val nameToUse = enteredName.trim().ifBlank { null }
                            onSaveCustomProfile(nameToUse, selectedPhotoUri)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                    ) {
                        Text(
                            text = if (enteredName.isNotBlank() || selectedPhotoUri != null) "Save Custom Profile" else "Save & Continue",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Secondary: Explicit "Use Gmail Profile"
                    OutlinedButton(
                        onClick = onUseGmailProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SleekBorder)
                    ) {
                        Text(
                            text = "Use Gmail Profile",
                            fontWeight = FontWeight.SemiBold,
                            color = SleekTextPrimary
                        )
                    }
                }
            }
        }
    }
}
