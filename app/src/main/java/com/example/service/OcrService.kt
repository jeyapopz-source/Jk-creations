package com.example.service

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.model.FilterType
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

enum class OcrLanguage(val displayName: String, val code: String, val isBundled: Boolean) {
    ENGLISH("English (Latin)", "en", true),
    TAMIL("Tamil (Indic)", "ta", false)
}

data class OcrResult(
    val extractedText: String,
    val lineCount: Int,
    val wordCount: Int,
    val language: OcrLanguage = OcrLanguage.ENGLISH
)

class OcrService(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Extracts text locally from a bitmap or image Uri.
     * Preprocesses contrast/edges when helpful without destroying the original.
     */
    suspend fun extractTextFromImage(
        bitmap: Bitmap?,
        uri: Uri? = null,
        language: OcrLanguage = OcrLanguage.ENGLISH
    ): Result<OcrResult> = withContext(Dispatchers.Default) {
        try {
            if (language == OcrLanguage.TAMIL && !language.isBundled) {
                return@withContext Result.failure(
                    IllegalArgumentException("Tamil offline recognition is not bundled in standard ML Kit Latin on-device engine. Please use English for on-device recognition.")
                )
            }

            // Load or use bitmap
            var sourceBitmap: Bitmap? = bitmap
            if (sourceBitmap == null && uri != null) {
                sourceBitmap = ImageProcessingUtils.loadSampledBitmapFromUri(context, uri, maxDimension = 2000)
            }

            if (sourceBitmap == null) {
                return@withContext Result.failure(
                    IllegalArgumentException("Unable to load document image for text recognition.")
                )
            }

            // Preprocess image for OCR (Document enhancement: boosts contrast & sharpens text)
            val preprocessed = ImageProcessingUtils.applyEnhancement(sourceBitmap, FilterType.DOCUMENT)

            val inputImage = InputImage.fromBitmap(preprocessed, 0)
            val visionText = recognizer.process(inputImage).await()

            if (preprocessed != sourceBitmap) {
                preprocessed.recycle()
            }

            val rawText = visionText.text.trim()
            if (rawText.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("No readable text was detected. Try a clearer document image.")
                )
            }

            // Cleanly format paragraphs from text blocks
            val formattedBuilder = StringBuilder()
            var totalLines = 0
            for (block in visionText.textBlocks) {
                val blockText = block.text.trim()
                if (blockText.isNotEmpty()) {
                    formattedBuilder.append(blockText)
                    formattedBuilder.append("\n\n")
                    totalLines += block.lines.size
                }
            }

            val finalText = formattedBuilder.toString().trim()
            val wordCount = finalText.split(Regex("\\s+")).filter { it.isNotBlank() }.size

            Result.success(
                OcrResult(
                    extractedText = finalText,
                    lineCount = totalLines,
                    wordCount = wordCount,
                    language = language
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            val message = e.localizedMessage ?: "OCR processing failed"
            Result.failure(Exception("OCR Error: $message"))
        }
    }
}
