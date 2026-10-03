package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppSettingsData
import com.example.model.CropRect
import com.example.model.FilterType
import com.example.model.ImageItem
import com.example.model.PageSize
import com.example.model.PdfDocumentItem
import com.example.model.PdfOrientation
import com.example.model.PdfQuality
import com.example.model.PdfSettingsData
import com.example.model.PdfSortOption
import com.example.model.ScanMode
import com.example.model.WatermarkPosition
import com.example.service.LocalPdfStorageManager
import com.example.service.PdfGeneratorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val pdfGenerator = PdfGeneratorService(application)

    // Image workflow state
    private val _selectedImages = MutableStateFlow<List<ImageItem>>(emptyList())
    val selectedImages: StateFlow<List<ImageItem>> = _selectedImages.asStateFlow()

    private val _activeEditorImage = MutableStateFlow<ImageItem?>(null)
    val activeEditorImage: StateFlow<ImageItem?> = _activeEditorImage.asStateFlow()

    private val _scanMode = MutableStateFlow(ScanMode.SINGLE)
    val scanMode: StateFlow<ScanMode> = _scanMode.asStateFlow()

    // PDF Configuration
    private val _pdfSettings = MutableStateFlow(PdfSettingsData())
    val pdfSettings: StateFlow<PdfSettingsData> = _pdfSettings.asStateFlow()

    // App Preferences
    private val _appSettings = MutableStateFlow(AppSettingsData())
    val appSettings: StateFlow<AppSettingsData> = _appSettings.asStateFlow()

    // Processing State
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _processingProgress = MutableStateFlow(0f)
    val processingProgress: StateFlow<Float> = _processingProgress.asStateFlow()

    private val _processingStatusText = MutableStateFlow("Preparing images...")
    val processingStatusText: StateFlow<String> = _processingStatusText.asStateFlow()

    // Generated PDF Result
    private val _lastGeneratedPdf = MutableStateFlow<PdfDocumentItem?>(null)
    val lastGeneratedPdf: StateFlow<PdfDocumentItem?> = _lastGeneratedPdf.asStateFlow()

    private val _pdfGenerationError = MutableStateFlow<String?>(null)
    val pdfGenerationError: StateFlow<String?> = _pdfGenerationError.asStateFlow()

    // Text to PDF Configuration
    private val _textToPdfSettings = MutableStateFlow(com.example.model.TextToPdfSettings())
    val textToPdfSettings: StateFlow<com.example.model.TextToPdfSettings> = _textToPdfSettings.asStateFlow()

    // Recent PDFs (Real documents from storage)
    private val _recentPdfs = MutableStateFlow<List<PdfDocumentItem>>(emptyList())
    val recentPdfs: StateFlow<List<PdfDocumentItem>> = _recentPdfs.asStateFlow()

    // Search and Sort State for Recent PDFs
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(PdfSortOption.NEWEST_FIRST)
    val sortOption: StateFlow<PdfSortOption> = _sortOption.asStateFlow()

    // Details Dialog Selection
    private val _selectedPdfForDetails = MutableStateFlow<PdfDocumentItem?>(null)
    val selectedPdfForDetails: StateFlow<PdfDocumentItem?> = _selectedPdfForDetails.asStateFlow()

    // Filtered and Sorted Recent PDFs Flow
    val filteredRecentPdfs: StateFlow<List<PdfDocumentItem>> = combine(
        _recentPdfs,
        _searchQuery,
        _sortOption
    ) { pdfs, query, sort ->
        val filtered = if (query.isBlank()) {
            pdfs
        } else {
            val q = query.trim().lowercase()
            pdfs.filter { it.fileName.lowercase().contains(q) }
        }

        when (sort) {
            PdfSortOption.NEWEST_FIRST -> filtered.sortedByDescending { it.createdAt }
            PdfSortOption.OLDEST_FIRST -> filtered.sortedBy { it.createdAt }
            PdfSortOption.NAME_ASC -> filtered.sortedBy { it.fileName.lowercase() }
            PdfSortOption.NAME_DESC -> filtered.sortedByDescending { it.fileName.lowercase() }
            PdfSortOption.LARGEST_FIRST -> filtered.sortedByDescending { it.fileSizeBytes }
            PdfSortOption.SMALLEST_FIRST -> filtered.sortedBy { it.fileSizeBytes }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Signature state
    private val _hasSignature = MutableStateFlow(false)
    val hasSignature: StateFlow<Boolean> = _hasSignature.asStateFlow()

    init {
        loadRecentPdfs()
    }

    fun loadRecentPdfs() {
        val list = LocalPdfStorageManager.getRecentPdfs(getApplication(), includeMissing = true)
        _recentPdfs.value = list
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: PdfSortOption) {
        _sortOption.value = option
    }

    fun selectPdfForDetails(pdf: PdfDocumentItem?) {
        _selectedPdfForDetails.value = pdf
    }

    fun setScanMode(mode: ScanMode) {
        _scanMode.value = mode
    }

    fun addCapturedImage(imageUri: String? = null) {
        val currentList = _selectedImages.value.toMutableList()
        val nextNumber = currentList.size + 1
        val item = ImageItem(
            id = UUID.randomUUID().toString(),
            uri = imageUri,
            name = "Page $nextNumber",
            pageNumber = nextNumber
        )
        currentList.add(item)
        _selectedImages.value = currentList
        if (_activeEditorImage.value == null) {
            _activeEditorImage.value = item
        }
    }

    fun addMultipleImages(uris: List<String>) {
        if (uris.isEmpty()) return
        val currentList = _selectedImages.value.toMutableList()
        var currentCount = currentList.size
        for (uri in uris) {
            currentCount++
            currentList.add(
                ImageItem(
                    id = UUID.randomUUID().toString(),
                    uri = uri,
                    name = "Page $currentCount",
                    pageNumber = currentCount
                )
            )
        }
        _selectedImages.value = currentList
        if (_activeEditorImage.value == null) {
            _activeEditorImage.value = currentList.firstOrNull()
        }
    }

    fun removeImage(id: String) {
        val filtered = _selectedImages.value.filter { it.id != id }
            .mapIndexed { index, item -> item.copy(pageNumber = index + 1, name = "Page ${index + 1}") }
        _selectedImages.value = filtered
        if (_activeEditorImage.value?.id == id) {
            _activeEditorImage.value = filtered.firstOrNull()
        }
    }

    fun rotateImage(id: String) {
        _selectedImages.value = _selectedImages.value.map { item ->
            if (item.id == id) {
                val newRot = (item.rotationDegrees + 90) % 360
                val updated = item.copy(rotationDegrees = newRot)
                if (_activeEditorImage.value?.id == id) {
                    _activeEditorImage.value = updated
                }
                updated
            } else {
                item
            }
        }
    }

    fun moveImage(fromIndex: Int, toIndex: Int) {
        val current = _selectedImages.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices && fromIndex != toIndex) {
            val moved = current.removeAt(fromIndex)
            current.add(toIndex, moved)
            val renumbered = current.mapIndexed { idx, itm ->
                itm.copy(pageNumber = idx + 1, name = "Page ${idx + 1}")
            }
            _selectedImages.value = renumbered
        }
    }

    fun setActiveEditorImage(item: ImageItem) {
        _activeEditorImage.value = item
    }

    fun updateEditorFilter(filter: FilterType) {
        val current = _activeEditorImage.value ?: return
        val updated = current.copy(filterType = filter)
        _activeEditorImage.value = updated
        _selectedImages.value = _selectedImages.value.map {
            if (it.id == updated.id) updated else it
        }
    }

    fun updateEditorCrop(crop: CropRect) {
        val current = _activeEditorImage.value ?: return
        val updated = current.copy(cropRect = crop)
        _activeEditorImage.value = updated
        _selectedImages.value = _selectedImages.value.map {
            if (it.id == updated.id) updated else it
        }
    }

    fun rotateEditorImage() {
        val current = _activeEditorImage.value ?: return
        rotateImage(current.id)
    }

    fun updatePdfSettings(
        pageSize: PageSize? = null,
        orientation: PdfOrientation? = null,
        quality: PdfQuality? = null,
        watermarkEnabled: Boolean? = null,
        watermarkText: String? = null,
        watermarkPosition: WatermarkPosition? = null,
        watermarkOpacity: Float? = null,
        watermarkSize: Float? = null,
        fileName: String? = null
    ) {
        val current = _pdfSettings.value
        _pdfSettings.value = current.copy(
            pageSize = pageSize ?: current.pageSize,
            orientation = orientation ?: current.orientation,
            quality = quality ?: current.quality,
            watermarkEnabled = watermarkEnabled ?: current.watermarkEnabled,
            watermarkText = watermarkText ?: current.watermarkText,
            watermarkPosition = watermarkPosition ?: current.watermarkPosition,
            watermarkOpacity = watermarkOpacity ?: current.watermarkOpacity,
            watermarkSize = watermarkSize ?: current.watermarkSize,
            fileName = fileName ?: current.fileName
        )
    }

    fun startRealPdfGeneration(onSuccess: (PdfDocumentItem) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isProcessing.value = true
            _processingProgress.value = 0.05f
            _processingStatusText.value = "Preparing images..."
            _pdfGenerationError.value = null

            val result = pdfGenerator.createPdf(
                images = _selectedImages.value,
                settings = _pdfSettings.value,
                onProgress = { percent, status ->
                    _processingProgress.value = (percent / 100f).coerceIn(0.05f, 1f)
                    _processingStatusText.value = status
                }
            )

            _isProcessing.value = false

            result.onSuccess { pdfItem ->
                _lastGeneratedPdf.value = pdfItem
                loadRecentPdfs()
                onSuccess(pdfItem)
            }.onFailure { ex ->
                val errorMsg = ex.localizedMessage ?: "Unable to create PDF. Please try again."
                _pdfGenerationError.value = errorMsg
                onError(errorMsg)
            }
        }
    }

    fun deleteRecentPdf(id: String) {
        LocalPdfStorageManager.deletePdf(getApplication(), id)
        loadRecentPdfs()
        if (_selectedPdfForDetails.value?.id == id) {
            _selectedPdfForDetails.value = null
        }
    }

    fun removeStaleRecentPdf(id: String) {
        LocalPdfStorageManager.removeStalePdf(getApplication(), id)
        loadRecentPdfs()
        if (_selectedPdfForDetails.value?.id == id) {
            _selectedPdfForDetails.value = null
        }
    }

    fun renameRecentPdf(id: String, newName: String): Result<PdfDocumentItem> {
        val result = LocalPdfStorageManager.renamePdf(getApplication(), id, newName)
        if (result.isSuccess) {
            loadRecentPdfs()
            val updated = result.getOrNull()
            if (_selectedPdfForDetails.value?.id == id) {
                _selectedPdfForDetails.value = updated
            }
        }
        return result
    }

    fun exportPdf(sourceFilePath: String, destUri: Uri): Result<Unit> {
        return LocalPdfStorageManager.exportPdf(getApplication(), sourceFilePath, destUri)
    }

    fun updateTextToPdfSettings(
        title: String? = null,
        content: String? = null,
        fontSizeSp: Float? = null,
        isBold: Boolean? = null,
        isItalic: Boolean? = null,
        alignment: com.example.model.TextAlignOption? = null,
        textColorHex: String? = null,
        paragraphSpacingDp: Float? = null,
        lineSpacingMultiplier: Float? = null,
        pageSize: PageSize? = null,
        orientation: PdfOrientation? = null,
        marginOption: com.example.model.MarginOption? = null,
        showHeader: Boolean? = null,
        headerText: String? = null,
        showFooter: Boolean? = null,
        footerText: String? = null,
        showPageNumber: Boolean? = null,
        fileName: String? = null
    ) {
        val cur = _textToPdfSettings.value
        _textToPdfSettings.value = cur.copy(
            title = title ?: cur.title,
            content = content ?: cur.content,
            fontSizeSp = fontSizeSp ?: cur.fontSizeSp,
            isBold = isBold ?: cur.isBold,
            isItalic = isItalic ?: cur.isItalic,
            alignment = alignment ?: cur.alignment,
            textColorHex = textColorHex ?: cur.textColorHex,
            paragraphSpacingDp = paragraphSpacingDp ?: cur.paragraphSpacingDp,
            lineSpacingMultiplier = lineSpacingMultiplier ?: cur.lineSpacingMultiplier,
            pageSize = pageSize ?: cur.pageSize,
            orientation = orientation ?: cur.orientation,
            marginOption = marginOption ?: cur.marginOption,
            showHeader = showHeader ?: cur.showHeader,
            headerText = headerText ?: cur.headerText,
            showFooter = showFooter ?: cur.showFooter,
            footerText = footerText ?: cur.footerText,
            showPageNumber = showPageNumber ?: cur.showPageNumber,
            fileName = fileName ?: cur.fileName
        )
    }

    fun generateTextPdf(
        onSuccess: (PdfDocumentItem) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isProcessing.value = true
            _processingProgress.value = 0.3f
            _processingStatusText.value = "Creating Text PDF..."
            _pdfGenerationError.value = null

            val result = pdfGenerator.createTextPdf(_textToPdfSettings.value)
            _isProcessing.value = false

            result.onSuccess { pdfItem ->
                _lastGeneratedPdf.value = pdfItem
                loadRecentPdfs()
                onSuccess(pdfItem)
            }.onFailure { ex ->
                val errorMsg = ex.localizedMessage ?: "Unable to create PDF. Please try again."
                _pdfGenerationError.value = errorMsg
                onError(errorMsg)
            }
        }
    }

    fun saveSignature() {
        _hasSignature.value = true
    }

    fun clearSignature() {
        _hasSignature.value = false
    }

    fun resetForNewPdf() {
        _selectedImages.value = emptyList()
        _activeEditorImage.value = null
        _pdfSettings.value = PdfSettingsData()
        _processingProgress.value = 0f
        _lastGeneratedPdf.value = null
        _pdfGenerationError.value = null
    }

    fun updateAppSettings(
        language: String? = null,
        theme: String? = null,
        defaultPageSize: PageSize? = null,
        defaultQuality: PdfQuality? = null,
        defaultOrientation: PdfOrientation? = null,
        defaultFileName: String? = null,
        savePreferences: String? = null
    ) {
        val cur = _appSettings.value
        _appSettings.value = cur.copy(
            language = language ?: cur.language,
            theme = theme ?: cur.theme,
            defaultPageSize = defaultPageSize ?: cur.defaultPageSize,
            defaultQuality = defaultQuality ?: cur.defaultQuality,
            defaultOrientation = defaultOrientation ?: cur.defaultOrientation,
            defaultFileName = defaultFileName ?: cur.defaultFileName,
            savePreferences = savePreferences ?: cur.savePreferences
        )
    }
}
