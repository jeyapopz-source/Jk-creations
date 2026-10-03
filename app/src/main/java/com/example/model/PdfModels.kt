package com.example.model

data class PointF2D(
    val x: Float = 0f,
    val y: Float = 0f
)

data class DocumentCorners(
    val topLeft: PointF2D = PointF2D(0.05f, 0.05f),
    val topRight: PointF2D = PointF2D(0.95f, 0.05f),
    val bottomRight: PointF2D = PointF2D(0.95f, 0.95f),
    val bottomLeft: PointF2D = PointF2D(0.05f, 0.95f)
)

data class CropRect(
    val left: Float = 0f,
    val top: Float = 0f,
    val right: Float = 1f,
    val bottom: Float = 1f
)

data class ImageItem(
    val id: String,
    val uri: String? = null,
    val name: String = "Page",
    val pageNumber: Int = 1,
    val rotationDegrees: Int = 0,
    val filterType: FilterType = FilterType.ORIGINAL,
    val cropRect: CropRect? = null,
    val corners: DocumentCorners? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class PdfSettingsData(
    val pageSize: PageSize = PageSize.A4,
    val orientation: PdfOrientation = PdfOrientation.AUTO,
    val quality: PdfQuality = PdfQuality.BALANCED,
    val watermarkEnabled: Boolean = false,
    val watermarkText: String = "JK Creations",
    val watermarkPosition: WatermarkPosition = WatermarkPosition.CENTER,
    val watermarkOpacity: Float = 0.35f,
    val watermarkSize: Float = 24f,
    val fileName: String = "My_Document.pdf"
)

data class TextToPdfSettings(
    val title: String = "",
    val content: String = "",
    val fontSizeSp: Float = 14f,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val alignment: TextAlignOption = TextAlignOption.LEFT,
    val textColorHex: String = "#1E1E24",
    val paragraphSpacingDp: Float = 12f,
    val lineSpacingMultiplier: Float = 1.3f,
    val pageSize: PageSize = PageSize.A4,
    val orientation: PdfOrientation = PdfOrientation.PORTRAIT,
    val marginOption: MarginOption = MarginOption.NORMAL,
    val showHeader: Boolean = false,
    val headerText: String = "JK Creations",
    val showFooter: Boolean = false,
    val footerText: String = "Document Suite",
    val showPageNumber: Boolean = true,
    val fileName: String = ""
)

data class PdfDocumentItem(
    val id: String,
    val fileName: String,
    val filePath: String,
    val pageCount: Int,
    val fileSizeBytes: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = createdAt,
    val isMissing: Boolean = false
)

data class AppSettingsData(
    val language: String = "English",
    val theme: String = "Dark Charcoal",
    val defaultPageSize: PageSize = PageSize.A4,
    val defaultQuality: PdfQuality = PdfQuality.BALANCED,
    val defaultOrientation: PdfOrientation = PdfOrientation.AUTO,
    val defaultFileName: String = "My_Document.pdf",
    val defaultWatermarkText: String = "JK Creations",
    val savePreferences: String = "Internal App Storage"
)
