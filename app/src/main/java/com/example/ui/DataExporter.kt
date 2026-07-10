package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.Expense
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object DataExporter {

    fun sharePdfReport(context: Context, expenses: List<Expense>, dateRangeStr: String, categoryFilter: String) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        val paint = Paint()
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subTitlePaint = Paint().apply {
            color = Color.GRAY
            textSize = 12f
        }
        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
        }
        
        canvas.drawText("OFFLINE LEDGER - EXPENSE REPORT", 40f, 60f, titlePaint)
        canvas.drawText("Period: $dateRangeStr   |   Category: $categoryFilter", 40f, 85f, subTitlePaint)
        canvas.drawText("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", 40f, 105f, subTitlePaint)
        
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, 120f, 555f, 120f, paint)
        
        val totalAmount = expenses.sumOf { it.amount }
        paint.color = Color.parseColor("#F3F4F6")
        canvas.drawRoundRect(40f, 140f, 555f, 190f, 10f, 10f, paint)
        
        val summaryPaint = Paint().apply {
            color = Color.parseColor("#1E3A8A")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("Total Expenses: ₹${String.format(Locale.getDefault(), "%,.2f", totalAmount)}", 60f, 170f, summaryPaint)
        
        var y = 230f
        canvas.drawText("DATE", 45f, y, headerPaint)
        canvas.drawText("CATEGORY", 140f, y, headerPaint)
        canvas.drawText("DESCRIPTION", 260f, y, headerPaint)
        canvas.drawText("AMOUNT", 480f, y, headerPaint)
        
        paint.color = Color.DKGRAY
        canvas.drawLine(40f, y + 8, 555f, y + 8, paint)
        y += 28f
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        expenses.take(20).forEach { item ->
            if (y > 800f) return@forEach
            val dateStr = sdf.format(Date(item.date))
            canvas.drawText(dateStr, 45f, y, bodyPaint)
            canvas.drawText(item.category, 140f, y, bodyPaint)
            canvas.drawText((item.note ?: "No description").take(28), 260f, y, bodyPaint)
            canvas.drawText("₹${String.format(Locale.getDefault(), "%,.2f", item.amount)}", 480f, y, bodyPaint)
            
            paint.color = Color.parseColor("#E5E7EB")
            canvas.drawLine(40f, y + 6, 555f, y + 6, paint)
            y += 24f
        }
        
        if (expenses.size > 20) {
            canvas.drawText("... and ${expenses.size - 20} more items", 45f, y + 10, subTitlePaint)
        }
        
        pdfDocument.finishPage(page)
        
        val cachePath = File(context.cacheDir, "reports")
        cachePath.mkdirs()
        val pdfFile = File(cachePath, "expense_report.pdf")
        
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Offline Ledger PDF Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share PDF Report"))
    }

    fun shareImageReport(context: Context, expenses: List<Expense>, dateRangeStr: String, categoryFilter: String) {
        val width = 600
        val height = 800
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val bgPaint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, height.toFloat(), Color.parseColor("#111827"), Color.parseColor("#1F2937"), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        
        val paint = Paint()
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subTitlePaint = Paint().apply {
            color = Color.parseColor("#9CA3AF")
            textSize = 14f
        }
        val valuePaint = Paint().apply {
            color = Color.parseColor("#10B981")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        
        canvas.drawText("OFFLINE LEDGER", 50f, 70f, titlePaint)
        canvas.drawText("Expense Statement Summary", 50f, 100f, subTitlePaint)
        
        paint.color = Color.parseColor("#374151")
        canvas.drawRoundRect(50f, 130f, 550f, 190f, 12f, 12f, paint)
        
        val filterPaint = Paint().apply {
            color = Color.parseColor("#E5E7EB")
            textSize = 12f
        }
        canvas.drawText("Range: $dateRangeStr", 70f, 155f, filterPaint)
        canvas.drawText("Category: $categoryFilter", 70f, 175f, filterPaint)
        
        paint.color = Color.parseColor("#1F2937")
        paint.strokeWidth = 2f
        paint.style = Paint.Style.FILL_AND_STROKE
        canvas.drawRoundRect(50f, 220f, 550f, 320f, 16f, 16f, paint)
        
        canvas.drawText("Total Spend Sum:", 75f, 255f, subTitlePaint)
        val totalAmount = expenses.sumOf { it.amount }
        canvas.drawText("₹${String.format(Locale.getDefault(), "%,.2f", totalAmount)}", 75f, 295f, valuePaint)
        
        canvas.drawText("Recent Filtered Items:", 50f, 365f, titlePaint.apply { textSize = 16f })
        
        val itemPaint = Paint().apply {
            color = Color.WHITE
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val itemSubPaint = Paint().apply {
            color = Color.parseColor("#9CA3AF")
            textSize = 11f
        }
        val itemAmountPaint = Paint().apply {
            color = Color.parseColor("#EF4444")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        
        var y = 400f
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        expenses.take(8).forEach { item ->
            paint.color = Color.parseColor("#1F2937")
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(50f, y, 550f, y + 42f, 8f, 8f, paint)
            
            canvas.drawText((item.note ?: "Expense").take(22), 65f, y + 25f, itemPaint)
            canvas.drawText("${item.category} • ${sdf.format(Date(item.date))}", 65f, y + 40f, itemSubPaint)
            canvas.drawText("-₹${String.format(Locale.getDefault(), "%,.2f", item.amount)}", 430f, y + 26f, itemAmountPaint)
            
            y += 48f
        }
        
        if (expenses.size > 8) {
            canvas.drawText("... and ${expenses.size - 8} more items logged", 50f, y + 15f, subTitlePaint)
        }
        
        canvas.drawText("Created with Love for Vivek & AI Offline Ledger", 50f, 770f, subTitlePaint.apply { textSize = 11f })
        
        val cachePath = File(context.cacheDir, "reports")
        cachePath.mkdirs()
        val imageFile = File(cachePath, "expense_report.jpg")
        
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Offline Ledger Image Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Image Report"))
    }
}
