package com.example.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import com.example.model.FilterType
import com.example.model.ImageItem
import com.example.model.PageSize
import com.example.model.PdfDocumentItem
import com.example.model.PdfOrientation
import com.example.model.PdfQuality
import com.example.model.PdfSettingsData
import com.example.model.WatermarkPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

class PdfGeneratorService(private val context: Context) : PdfService {

    override suspend fun createPdf(
        images: List<ImageItem>,
        settings: PdfSettingsData,
        onProgress: (Int, String) -> Unit
    ): Result<PdfDocumentItem> = withContext(Dispatchers.IO) {
        try {
            if (images.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("Please add at least one image to create a PDF."))
            }

            onProgress(5, "Preparing images...")

            // Sanitize file name
            var cleanFileName = settings.fileName.trim()
                .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            if (cleanFileName.isBlank()) {
                cleanFileName = "My_Document.pdf"
            }
            if (!cleanFileName.endsWith(".pdf", ignoreCase = true)) {
                cleanFileName += ".pdf"
            }

            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            val outputFile = File(storageDir, cleanFileName)

            val pdfDocument = PdfDocument()

            val totalPages = images.size
            for (index in images.indices) {
                val pageItem = images[index]
                val pageNumber = index + 1

                val progressPercent = 10 + ((pageNumber.toFloat() / totalPages) * 75).toInt()
                onProgress(progressPercent, "Processing page $pageNumber of $totalPages...")

                // 1. Load bitmap safely
                var bitmap: Bitmap? = null
                if (pageItem.uri != null) {
                    val uri = Uri.parse(pageItem.uri)
                    bitmap = ImageProcessingUtils.loadSampledBitmapFromUri(
                        context = context,
                        uri = uri,
                        maxDimension = settings.quality.maxDimension
                    )
                }

                // Fallback to generated high-resolution document bitmap if file Uri unavailable
                if (bitmap == null) {
                    bitmap = ImageProcessingUtils.createDocumentBitmap(pageNumber = pageNumber)
                }

                // Apply Perspective Transform if corners are defined
                if (pageItem.corners != null) {
                    val warped = ImageProcessingUtils.applyPerspectiveTransform(bitmap, pageItem.corners)
                    if (warped != bitmap) {
                        bitmap.recycle()
                        bitmap = warped
                    }
                }

                // 2. Apply Rotation
                if (pageItem.rotationDegrees != 0) {
                    val rotated = ImageProcessingUtils.rotateBitmap(bitmap, pageItem.rotationDegrees)
                    if (rotated != bitmap) {
                        bitmap.recycle()
                        bitmap = rotated
                    }
                }

                // 3. Apply Crop
                if (pageItem.cropRect != null) {
                    val cropped = ImageProcessingUtils.cropBitmap(bitmap, pageItem.cropRect)
                    if (cropped != bitmap) {
                        bitmap.recycle()
                        bitmap = cropped
                    }
                }

                // 4. Apply Enhancement
                if (pageItem.filterType != FilterType.ORIGINAL) {
                    val filtered = ImageProcessingUtils.applyEnhancement(bitmap, pageItem.filterType)
                    if (filtered != bitmap) {
                        bitmap.recycle()
                        bitmap = filtered
                    }
                }

                // 5. Determine PDF Page Dimensions
                val (pageWidth, pageHeight) = calculatePageDimensions(
                    bitmap = bitmap,
                    pageSize = settings.pageSize,
                    orientation = settings.orientation
                )

                // 6. Create PDF Page
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // Draw background white
                canvas.drawColor(Color.WHITE)

                // 7. Scale and Center Bitmap onto PDF Page maintaining aspect ratio
                val margin = if (settings.pageSize == PageSize.ORIGINAL) 0f else 20f
                val availWidth = (pageWidth - (margin * 2)).coerceAtLeast(10f)
                val availHeight = (pageHeight - (margin * 2)).coerceAtLeast(10f)

                val bmpWidth = bitmap.width.toFloat()
                val bmpHeight = bitmap.height.toFloat()

                val scale = min(availWidth / bmpWidth, availHeight / bmpHeight)
                val destWidth = bmpWidth * scale
                val destHeight = bmpHeight * scale

                val destLeft = margin + ((availWidth - destWidth) / 2f)
                val destTop = margin + ((availHeight - destHeight) / 2f)

                val destRect = RectF(destLeft, destTop, destLeft + destWidth, destTop + destHeight)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                canvas.drawBitmap(bitmap, null, destRect, paint)

                // 8. Apply Watermark if enabled
                if (settings.watermarkEnabled && settings.watermarkText.isNotBlank()) {
                    drawWatermark(
                        canvas = canvas,
                        pageWidth = pageWidth,
                        pageHeight = pageHeight,
                        text = settings.watermarkText,
                        position = settings.watermarkPosition,
                        opacity = settings.watermarkOpacity,
                        textSize = settings.watermarkSize
                    )
                }

                pdfDocument.finishPage(page)
                bitmap.recycle()
            }

            onProgress(90, "Creating PDF...")
            onProgress(95, "Saving PDF...")

            val outputStream = FileOutputStream(outputFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            onProgress(100, "PDF Ready!")

            val resultItem = PdfDocumentItem(
                id = UUID.randomUUID().toString(),
                fileName = cleanFileName,
                filePath = outputFile.absolutePath,
                pageCount = totalPages,
                fileSizeBytes = outputFile.length(),
                createdAt = System.currentTimeMillis()
            )

            // Save to recent documents store
            LocalPdfStorageManager.saveRecentPdf(context, resultItem)

            Result.success(resultItem)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun calculatePageDimensions(
        bitmap: Bitmap,
        pageSize: PageSize,
        orientation: PdfOrientation
    ): Pair<Int, Int> {
        if (pageSize == PageSize.ORIGINAL) {
            return Pair(bitmap.width, bitmap.height)
        }

        var w = pageSize.widthPt
        var h = pageSize.heightPt

        val isLandscape = when (orientation) {
            PdfOrientation.AUTO -> bitmap.width > bitmap.height
            PdfOrientation.PORTRAIT -> false
            PdfOrientation.LANDSCAPE -> true
        }

        return if (isLandscape) {
            Pair(max(w, h), min(w, h))
        } else {
            Pair(min(w, h), max(w, h))
        }
    }

    private fun drawWatermark(
        canvas: android.graphics.Canvas,
        pageWidth: Int,
        pageHeight: Int,
        text: String,
        position: WatermarkPosition,
        opacity: Float,
        textSize: Float
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            alpha = (opacity.coerceIn(0.05f, 0.95f) * 255).toInt()
            this.textSize = textSize * 1.5f
            isFakeBoldText = true
        }

        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val textWidth = textBounds.width().toFloat()
        val textHeight = textBounds.height().toFloat()

        when (position) {
            WatermarkPosition.TOP_LEFT -> {
                canvas.drawText(text, 30f, 40f + textHeight, paint)
            }
            WatermarkPosition.TOP_CENTER -> {
                val x = (pageWidth - textWidth) / 2f
                canvas.drawText(text, x, 40f + textHeight, paint)
            }
            WatermarkPosition.TOP_RIGHT -> {
                val x = pageWidth - textWidth - 30f
                canvas.drawText(text, x, 40f + textHeight, paint)
            }
            WatermarkPosition.CENTER -> {
                canvas.save()
                val centerX = pageWidth / 2f
                val centerY = pageHeight / 2f
                canvas.rotate(-45f, centerX, centerY)
                val x = centerX - (textWidth / 2f)
                val y = centerY + (textHeight / 2f)
                canvas.drawText(text, x, y, paint)
                canvas.restore()
            }
            WatermarkPosition.BOTTOM_LEFT -> {
                canvas.drawText(text, 30f, pageHeight - 30f, paint)
            }
            WatermarkPosition.BOTTOM_CENTER -> {
                val x = (pageWidth - textWidth) / 2f
                canvas.drawText(text, x, pageHeight - 30f, paint)
            }
            WatermarkPosition.BOTTOM_RIGHT -> {
                val x = pageWidth - textWidth - 30f
                canvas.drawText(text, x, pageHeight - 30f, paint)
            }
        }
    }

    suspend fun createTextPdf(
        settings: com.example.model.TextToPdfSettings
    ): Result<PdfDocumentItem> = withContext(Dispatchers.IO) {
        try {
            if (settings.content.isBlank() && settings.title.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Document is empty. Please enter text."))
            }

            var cleanFileName = settings.fileName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
            if (cleanFileName.isBlank()) {
                val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                cleanFileName = "Text_Document_$timeStamp.pdf"
            }
            if (!cleanFileName.endsWith(".pdf", ignoreCase = true)) {
                cleanFileName += ".pdf"
            }

            val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
            if (!storageDir.exists()) storageDir.mkdirs()
            val outputFile = File(storageDir, cleanFileName)

            val (pageWidth, pageHeight) = when (settings.orientation) {
                PdfOrientation.PORTRAIT, PdfOrientation.AUTO -> Pair(settings.pageSize.widthPt.coerceAtLeast(400), settings.pageSize.heightPt.coerceAtLeast(500))
                PdfOrientation.LANDSCAPE -> Pair(settings.pageSize.heightPt.coerceAtLeast(500), settings.pageSize.widthPt.coerceAtLeast(400))
            }

            val margin = settings.marginOption.marginPt
            val contentWidth = (pageWidth - (margin * 2)).toInt().coerceAtLeast(100)

            val headerHeight = if (settings.showHeader) 35f else 0f
            val footerHeight = if (settings.showFooter || settings.showPageNumber) 30f else 0f
            val contentHeight = pageHeight - (margin * 2) - headerHeight - footerHeight

            var style = android.graphics.Typeface.NORMAL
            if (settings.isBold && settings.isItalic) style = android.graphics.Typeface.BOLD_ITALIC
            else if (settings.isBold) style = android.graphics.Typeface.BOLD
            else if (settings.isItalic) style = android.graphics.Typeface.ITALIC

            val bodyPaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = settings.fontSizeSp * 1.33f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, style)
                try {
                    color = Color.parseColor(settings.textColorHex)
                } catch (e: Exception) {
                    color = Color.rgb(30, 30, 36)
                }
            }

            val alignment = when (settings.alignment) {
                com.example.model.TextAlignOption.LEFT -> android.text.Layout.Alignment.ALIGN_NORMAL
                com.example.model.TextAlignOption.CENTER -> android.text.Layout.Alignment.ALIGN_CENTER
                com.example.model.TextAlignOption.RIGHT -> android.text.Layout.Alignment.ALIGN_OPPOSITE
                com.example.model.TextAlignOption.JUSTIFY -> android.text.Layout.Alignment.ALIGN_NORMAL
            }

            var titleLayout: android.text.StaticLayout? = null
            var titleHeight = 0f
            if (settings.title.isNotBlank()) {
                val titlePaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = (settings.fontSizeSp * 1.7f).coerceAtLeast(18f)
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                    try {
                        color = Color.parseColor(settings.textColorHex)
                    } catch (e: Exception) {
                        color = Color.rgb(20, 20, 25)
                    }
                }
                titleLayout = android.text.StaticLayout.Builder.obtain(settings.title, 0, settings.title.length, titlePaint, contentWidth)
                    .setAlignment(alignment)
                    .setLineSpacing(0f, 1.2f)
                    .setIncludePad(true)
                    .build()
                titleHeight = titleLayout.height.toFloat() + 20f
            }

            val fullContent = settings.content
            val bodyLayout = android.text.StaticLayout.Builder.obtain(fullContent, 0, fullContent.length, bodyPaint, contentWidth)
                .setAlignment(alignment)
                .setLineSpacing(0f, settings.lineSpacingMultiplier)
                .setIncludePad(true)
                .build()

            val totalLines = bodyLayout.lineCount
            val pageLineRanges = mutableListOf<Pair<Int, Int>>()

            var currentLine = 0
            var isFirstPage = true
            while (currentLine < totalLines) {
                val availableH = if (isFirstPage) contentHeight - titleHeight else contentHeight
                val startLine = currentLine
                val startTop = bodyLayout.getLineTop(startLine)

                while (currentLine < totalLines) {
                    val currentBottom = bodyLayout.getLineBottom(currentLine)
                    if (currentBottom - startTop > availableH && currentLine > startLine) {
                        break
                    }
                    currentLine++
                }
                pageLineRanges.add(Pair(startLine, currentLine))
                isFirstPage = false
            }

            if (pageLineRanges.isEmpty()) {
                pageLineRanges.add(Pair(0, 0))
            }

            val totalPdfPages = pageLineRanges.size
            val pdfDocument = PdfDocument()

            for (pIndex in 0 until totalPdfPages) {
                val pageNumber = pIndex + 1
                val (startLine, endLine) = pageLineRanges[pIndex]

                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawColor(Color.WHITE)

                if (settings.showHeader) {
                    val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.rgb(120, 120, 130)
                        textSize = 10f
                    }
                    canvas.drawText(settings.headerText, margin, margin + 15f, headerPaint)
                    val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.rgb(220, 220, 230)
                        strokeWidth = 1f
                    }
                    canvas.drawLine(margin, margin + 25f, pageWidth - margin, margin + 25f, rulePaint)
                }

                var yOffset = margin + headerHeight
                if (pIndex == 0 && titleLayout != null) {
                    canvas.save()
                    canvas.translate(margin, yOffset)
                    titleLayout.draw(canvas)
                    canvas.restore()
                    yOffset += titleHeight
                }

                if (endLine > startLine) {
                    val startY = bodyLayout.getLineTop(startLine).toFloat()
                    val endY = bodyLayout.getLineBottom(endLine - 1).toFloat()

                    canvas.save()
                    canvas.clipRect(margin, yOffset, margin + contentWidth, yOffset + (endY - startY) + 5f)
                    canvas.translate(margin, yOffset - startY)
                    bodyLayout.draw(canvas)
                    canvas.restore()
                }

                if (settings.showFooter || settings.showPageNumber) {
                    val footerRuleY = pageHeight - margin - footerHeight + 10f
                    val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.rgb(220, 220, 230)
                        strokeWidth = 1f
                    }
                    canvas.drawLine(margin, footerRuleY, pageWidth - margin, footerRuleY, rulePaint)

                    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.rgb(120, 120, 130)
                        textSize = 10f
                    }
                    if (settings.showFooter && settings.footerText.isNotBlank()) {
                        canvas.drawText(settings.footerText, margin, footerRuleY + 15f, footerPaint)
                    }
                    if (settings.showPageNumber) {
                        val pageNumText = "Page $pageNumber of $totalPdfPages"
                        val pageNumWidth = footerPaint.measureText(pageNumText)
                        canvas.drawText(pageNumText, pageWidth - margin - pageNumWidth, footerRuleY + 15f, footerPaint)
                    }
                }

                pdfDocument.finishPage(page)
            }

            val fos = FileOutputStream(outputFile)
            pdfDocument.writeTo(fos)
            fos.flush()
            fos.close()
            pdfDocument.close()

            val resultItem = PdfDocumentItem(
                id = UUID.randomUUID().toString(),
                fileName = cleanFileName,
                filePath = outputFile.absolutePath,
                pageCount = totalPdfPages,
                fileSizeBytes = outputFile.length(),
                createdAt = System.currentTimeMillis()
            )
            LocalPdfStorageManager.saveRecentPdf(context, resultItem)
            Result.success(resultItem)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
