package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.Expense
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseDetailDialog(
    expense: Expense,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    var currentImagePath by remember { mutableStateOf(expense.imagePath) }
    var activeBitmapForEdit by remember { mutableStateOf<Bitmap?>(null) }
    var showCropper by remember { mutableStateOf(false) }

    // Launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                }
                activeBitmapForEdit = bitmap
                showCropper = true
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            activeBitmapForEdit = bitmap
            showCropper = true
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(SleekBg),
            color = SleekBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SleekTextPrimary)
                    }
                    Text(
                        text = "Receipt Detail",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Detail card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(SleekPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(expense.category, viewModel.categoryIcons.value),
                                    contentDescription = expense.category,
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = expense.category,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SleekTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(expense.date)),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SleekTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "-₹${String.format(Locale.getDefault(), "%,.2f", expense.amount)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )

                        if (!expense.note.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = expense.note,
                                style = MaterialTheme.typography.bodyLarge,
                                color = SleekTextPrimary,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Image receipt attachment section
                Text(
                    text = "Receipt Attachment",
                    style = MaterialTheme.typography.titleSmall,
                    color = SleekPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (!currentImagePath.isNullOrBlank() && File(currentImagePath!!).exists()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, SleekBorder, RoundedCornerShape(20.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = File(currentImagePath!!),
                            contentDescription = "Receipt Image",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Floating action overlay to clear or re-edit
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    val currentFile = File(currentImagePath!!)
                                    if (currentFile.exists()) currentFile.delete()
                                    val updated = expense.copy(imagePath = null)
                                    viewModel.updateExpense(updated)
                                    currentImagePath = null
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Attachment", tint = Color.White)
                            }
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SleekSurface),
                        border = BorderStroke(1.dp, SleekBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ReceiptLong,
                                contentDescription = null,
                                tint = SleekTextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No Receipt Image Attached",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SleekTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { cameraLauncher.launch() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Camera", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimaryContainer),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Photo, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gallery", color = SleekPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Edit Button
                Button(
                    onClick = {
                        onDismiss()
                        onEditClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Transaction Details", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showCropper && activeBitmapForEdit != null) {
        ImageEditDialog(
            initialBitmap = activeBitmapForEdit!!,
            onDismiss = { showCropper = false },
            onSave = { savedPath ->
                showCropper = false
                currentImagePath = savedPath
                val updated = expense.copy(imagePath = savedPath)
                viewModel.updateExpense(updated)
                viewModel.refreshUsageData() // Live refresh sizes
            }
        )
    }
}

@Composable
fun ImageEditDialog(
    initialBitmap: Bitmap,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val context = LocalContext.current
    var currentBitmap by remember { mutableStateOf(initialBitmap) }
    var cropFactor by remember { mutableStateOf(0.0f) } // edge crop inset factor (0f is no crop, 0.4f is high center zoom)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF111215)),
            color = Color(0xFF111215)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                    }
                    Text(
                        text = "Receipt Photo Editor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(
                        onClick = {
                            // Apply cropping if slider is active, then save
                            var finalBitmap = currentBitmap
                            if (cropFactor > 0.05f) {
                                val xSize = (finalBitmap.width * (1f - cropFactor * 2)).toInt().coerceIn(10, finalBitmap.width)
                                val ySize = (finalBitmap.height * (1f - cropFactor * 2)).toInt().coerceIn(10, finalBitmap.height)
                                val startX = ((finalBitmap.width - xSize) / 2).coerceIn(0, finalBitmap.width - 1)
                                val startY = ((finalBitmap.height - ySize) / 2).coerceIn(0, finalBitmap.height - 1)
                                try {
                                    finalBitmap = Bitmap.createBitmap(finalBitmap, startX, startY, xSize, ySize)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            // Write to file
                            try {
                                val file = File(context.filesDir, "receipt_${System.currentTimeMillis()}.jpg")
                                FileOutputStream(file).use { out ->
                                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                                }
                                onSave(file.absolutePath)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error saving receipt: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Apply", tint = Color(0xFF10B981))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Canvas/Image Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(20.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = currentBitmap.asImageBitmap(),
                        contentDescription = "Editing Canvas",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay crop bounding visual helper if cropFactor is active
                    if (cropFactor > 0.05f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(1f - cropFactor * 2)
                                .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Editor Controls Panel
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1D21)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFF2C2F36)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Adjustment Tools",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Rotate control
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.RotateRight, contentDescription = null, tint = Color.White)
                                Text("Rotation angle", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    val matrix = Matrix().apply { postRotate(90f) }
                                    currentBitmap = Bitmap.createBitmap(
                                        currentBitmap, 0, 0, currentBitmap.width, currentBitmap.height, matrix, true
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2F36)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Rotate 90°", color = Color.White, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFF2C2F36))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Crop control slider
                        Text(
                            text = "Crop Bounds Center Inset",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = cropFactor,
                            onValueChange = { cropFactor = it },
                            valueRange = 0f..0.45f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor = Color(0xFF2C2F36)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Full Frame", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("Tight Center Crop", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
