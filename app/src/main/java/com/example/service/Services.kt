package com.example.service

import android.graphics.Bitmap
import com.example.model.FilterType
import com.example.model.ImageItem
import com.example.model.PdfDocumentItem
import com.example.model.PdfSettingsData
import kotlinx.coroutines.flow.Flow

/**
 * Interface prepared for future PDF creation, compression, and watermarking engine.
 */
interface PdfService {
    suspend fun createPdf(
        images: List<ImageItem>,
        settings: PdfSettingsData,
        onProgress: (Int, String) -> Unit
    ): Result<PdfDocumentItem>
}

/**
 * Interface prepared for camera scanner and document boundary detection.
 */
interface ScannerService {
    suspend fun detectDocumentCorners(bitmap: Bitmap): List<Pair<Float, Float>>
    suspend fun autoEnhanceScan(bitmap: Bitmap): Bitmap
}

/**
 * Interface prepared for image enhancements, rotation, filters, and cropping.
 */
interface ImageProcessingService {
    suspend fun applyFilter(bitmap: Bitmap, filter: FilterType): Bitmap
    suspend fun rotateImage(bitmap: Bitmap, degrees: Int): Bitmap
}

/**
 * Interface prepared for local PDF storage, history, and metadata.
 */
interface PdfStorageService {
    fun getRecentPdfs(): Flow<List<PdfDocumentItem>>
    suspend fun deletePdf(id: String): Boolean
    suspend fun renamePdf(id: String, newName: String): Boolean
}

/**
 * Interface prepared for digital signature saving and embedding.
 */
interface SignatureService {
    suspend fun saveSignature(signatureBitmap: Bitmap): Result<String>
    suspend fun getSavedSignature(): Bitmap?
}
