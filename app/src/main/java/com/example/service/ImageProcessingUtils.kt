package com.example.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.net.Uri
import com.example.model.CropRect
import com.example.model.DocumentCorners
import com.example.model.FilterType
import com.example.model.PointF2D
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

object ImageProcessingUtils {

    /**
     * Loads a bitmap from Uri with memory-safe downsampling.
     */
    fun loadSampledBitmapFromUri(
        context: Context,
        uri: Uri,
        maxDimension: Int = 1800
    ): Bitmap? {
        return try {
            // First decode bounds
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return null

            val origWidth = options.outWidth
            val origHeight = options.outHeight
            if (origWidth <= 0 || origHeight <= 0) return null

            // Calculate inSampleSize
            var inSampleSize = 1
            val largestDim = max(origWidth, origHeight)
            if (largestDim > maxDimension) {
                while ((largestDim / (inSampleSize * 2)) >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            // Decode actual bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates a placeholder test document bitmap if no camera/gallery image exists.
     */
    fun createDocumentBitmap(
        pageNumber: Int,
        width: Int = 1200,
        height: Int = 1600
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.rgb(30, 30, 38)
        paint.textSize = 48f
        paint.isFakeBoldText = true
        canvas.drawText("JK CREATIONS DOCUMENT", 100f, 150f, paint)

        paint.textSize = 28f
        paint.color = Color.rgb(120, 120, 130)
        canvas.drawText("Page $pageNumber • High-Resolution Scan", 100f, 210f, paint)

        // Draw document rule lines
        paint.color = Color.rgb(200, 200, 215)
        paint.strokeWidth = 3f
        canvas.drawLine(100f, 240f, width - 100f, 240f, paint)

        paint.color = Color.rgb(70, 70, 80)
        var y = 320f
        val lineSpacing = 60f
        for (i in 1..18) {
            val lineWidth = if (i % 5 == 0) width - 350f else width - 100f
            canvas.drawRect(100f, y, lineWidth, y + 16f, paint)
            y += lineSpacing
        }

        // Draw footer badge
        paint.color = Color.rgb(180, 100, 60)
        paint.textSize = 24f
        canvas.drawText("Verified with Wings • JK Inc.", 100f, height - 100f, paint)

        return bitmap
    }

    /**
     * Detects document boundaries locally.
     * Uses fast luminance edge analysis to locate the paper rectangle boundaries,
     * safely falling back to standard inset corners so the user is never blocked.
     */
    fun detectDocumentCorners(bitmap: Bitmap): DocumentCorners {
        return try {
            val maxSampleDim = 250
            val largestDim = max(bitmap.width, bitmap.height)
            val scale = maxSampleDim.toFloat() / largestDim.coerceAtLeast(1)
            val sampleW = (bitmap.width * scale).toInt().coerceAtLeast(20)
            val sampleH = (bitmap.height * scale).toInt().coerceAtLeast(20)

            val small = Bitmap.createScaledBitmap(bitmap, sampleW, sampleH, false)
            val pixels = IntArray(sampleW * sampleH)
            small.getPixels(pixels, 0, sampleW, 0, 0, sampleW, sampleH)
            small.recycle()

            // Calculate luminance matrix
            val lum = FloatArray(sampleW * sampleH)
            for (i in pixels.indices) {
                val c = pixels[i]
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF
                lum[i] = (0.299f * r + 0.587f * g + 0.114f * b)
            }

            // Detect horizontal & vertical bounds by gradient change
            var topBoundary = 0.06f
            var bottomBoundary = 0.94f
            var leftBoundary = 0.06f
            var rightBoundary = 0.94f

            // Scan from top towards center
            for (y in (sampleH * 0.04f).toInt() until (sampleH * 0.35f).toInt()) {
                var rowGrad = 0f
                for (x in (sampleW * 0.2f).toInt() until (sampleW * 0.8f).toInt()) {
                    val diff = Math.abs(lum[y * sampleW + x] - lum[(y + 1) * sampleW + x])
                    if (diff > rowGrad) rowGrad = diff
                }
                if (rowGrad > 35f) {
                    topBoundary = (y.toFloat() / sampleH).coerceIn(0.04f, 0.25f)
                    break
                }
            }

            // Scan from bottom towards center
            for (y in (sampleH * 0.96f).toInt() downTo (sampleH * 0.65f).toInt()) {
                var rowGrad = 0f
                for (x in (sampleW * 0.2f).toInt() until (sampleW * 0.8f).toInt()) {
                    val diff = Math.abs(lum[y * sampleW + x] - lum[(y - 1) * sampleW + x])
                    if (diff > rowGrad) rowGrad = diff
                }
                if (rowGrad > 35f) {
                    bottomBoundary = (y.toFloat() / sampleH).coerceIn(0.75f, 0.96f)
                    break
                }
            }

            // Scan from left towards center
            for (x in (sampleW * 0.04f).toInt() until (sampleW * 0.35f).toInt()) {
                var colGrad = 0f
                for (y in (sampleH * 0.2f).toInt() until (sampleH * 0.8f).toInt()) {
                    val diff = Math.abs(lum[y * sampleW + x] - lum[y * sampleW + (x + 1)])
                    if (diff > colGrad) colGrad = diff
                }
                if (colGrad > 35f) {
                    leftBoundary = (x.toFloat() / sampleW).coerceIn(0.04f, 0.25f)
                    break
                }
            }

            // Scan from right towards center
            for (x in (sampleW * 0.96f).toInt() downTo (sampleW * 0.65f).toInt()) {
                var colGrad = 0f
                for (y in (sampleH * 0.2f).toInt() until (sampleH * 0.8f).toInt()) {
                    val diff = Math.abs(lum[y * sampleW + x] - lum[y * sampleW + (x - 1)])
                    if (diff > colGrad) colGrad = diff
                }
                if (colGrad > 35f) {
                    rightBoundary = (x.toFloat() / sampleW).coerceIn(0.75f, 0.96f)
                    break
                }
            }

            DocumentCorners(
                topLeft = PointF2D(leftBoundary, topBoundary),
                topRight = PointF2D(rightBoundary, topBoundary),
                bottomRight = PointF2D(rightBoundary, bottomBoundary),
                bottomLeft = PointF2D(leftBoundary, bottomBoundary)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            DocumentCorners(
                topLeft = PointF2D(0.06f, 0.06f),
                topRight = PointF2D(0.94f, 0.06f),
                bottomRight = PointF2D(0.94f, 0.94f),
                bottomLeft = PointF2D(0.06f, 0.94f)
            )
        }
    }

    /**
     * Applies perspective correction / four-point transform locally.
     * Uses Android's Matrix.setPolyToPoly to compute perspective transformation.
     */
    fun applyPerspectiveTransform(source: Bitmap, corners: DocumentCorners): Bitmap {
        return try {
            val pTL = PointF(corners.topLeft.x * source.width, corners.topLeft.y * source.height)
            val pTR = PointF(corners.topRight.x * source.width, corners.topRight.y * source.height)
            val pBR = PointF(corners.bottomRight.x * source.width, corners.bottomRight.y * source.height)
            val pBL = PointF(corners.bottomLeft.x * source.width, corners.bottomLeft.y * source.height)

            val topDist = hypot(pTR.x - pTL.x, pTR.y - pTL.y)
            val botDist = hypot(pBR.x - pBL.x, pBR.y - pBL.y)
            val leftDist = hypot(pBL.x - pTL.x, pBL.y - pTL.y)
            val rightDist = hypot(pBR.x - pTR.x, pBR.y - pTR.y)

            val targetWidth = max(topDist, botDist).toInt().coerceIn(150, 3000)
            val targetHeight = max(leftDist, rightDist).toInt().coerceIn(150, 4000)

            val srcPoints = floatArrayOf(
                pTL.x, pTL.y,
                pTR.x, pTR.y,
                pBR.x, pBR.y,
                pBL.x, pBL.y
            )
            val dstPoints = floatArrayOf(
                0f, 0f,
                targetWidth.toFloat(), 0f,
                targetWidth.toFloat(), targetHeight.toFloat(),
                0f, targetHeight.toFloat()
            )

            val matrix = Matrix()
            val polySuccess = matrix.setPolyToPoly(srcPoints, 0, dstPoints, 0, 4)
            if (!polySuccess) {
                return source
            }

            val result = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(source, matrix, paint)
            result
        } catch (e: Exception) {
            e.printStackTrace()
            source
        }
    }

    /**
     * Applies rotation to the bitmap.
     */
    fun rotateBitmap(source: Bitmap, degrees: Int): Bitmap {
        if (degrees % 360 == 0) return source
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    /**
     * Applies crop to the bitmap using normalized coordinates.
     */
    fun cropBitmap(source: Bitmap, crop: CropRect): Bitmap {
        val left = (crop.left * source.width).toInt().coerceIn(0, source.width - 1)
        val top = (crop.top * source.height).toInt().coerceIn(0, source.height - 1)
        val right = (crop.right * source.width).toInt().coerceIn(left + 1, source.width)
        val bottom = (crop.bottom * source.height).toInt().coerceIn(top + 1, source.height)

        val cropWidth = right - left
        val cropHeight = bottom - top

        return if (cropWidth > 0 && cropHeight > 0) {
            Bitmap.createBitmap(source, left, top, cropWidth, cropHeight)
        } else {
            source
        }
    }

    /**
     * Applies real enhancement filters.
     */
    fun applyEnhancement(source: Bitmap, filter: FilterType): Bitmap {
        return when (filter) {
            FilterType.ORIGINAL -> source
            FilterType.GRAYSCALE -> applyGrayscale(source)
            FilterType.BLACK_AND_WHITE -> applyBlackAndWhite(source)
            FilterType.DOCUMENT -> applyDocumentEnhance(source)
            FilterType.AUTO -> applyAutoEnhance(source)
        }
    }

    private fun applyGrayscale(source: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return output
    }

    private fun applyDocumentEnhance(source: Bitmap): Bitmap {
        // High contrast, slightly increased brightness, desaturated to crisp text
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val contrast = 1.35f
        val brightness = 15f
        val cm = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )
        val satMatrix = ColorMatrix().apply { setSaturation(0.2f) }
        cm.postConcat(satMatrix)

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return output
    }

    private fun applyBlackAndWhite(source: Bitmap): Bitmap {
        val gray = applyGrayscale(source)
        val width = gray.width
        val height = gray.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        gray.getPixels(pixels, 0, width, 0, 0, width, height)

        var sumLuminance = 0L
        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            sumLuminance += r
        }
        val avg = (sumLuminance / pixels.size).toInt().coerceIn(100, 160)

        for (i in pixels.indices) {
            val r = (pixels[i] shr 16) and 0xFF
            val bwColor = if (r > avg) Color.WHITE else Color.BLACK
            pixels[i] = bwColor
        }

        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return output
    }

    private fun applyAutoEnhance(source: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cm = ColorMatrix(
            floatArrayOf(
                1.25f, 0f, 0f, 0f, 10f,
                0f, 1.25f, 0f, 0f, 10f,
                0f, 0f, 1.25f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return output
    }

    /**
     * Saves a temporary bitmap to app cache and returns the absolute path.
     */
    fun saveBitmapToTempFile(context: Context, bitmap: Bitmap, prefix: String = "scan_"): String? {
        return try {
            val cacheDir = context.cacheDir
            val file = File(cacheDir, "${prefix}${System.currentTimeMillis()}.jpg")
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.flush()
            out.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
