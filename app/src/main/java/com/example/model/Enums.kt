package com.example.model

enum class PageSize(val displayName: String, val dimensions: String, val widthPt: Int, val heightPt: Int) {
    A4("A4", "210 × 297 mm", 595, 842),
    A5("A5", "148 × 210 mm", 420, 595),
    LETTER("Letter", "8.5 × 11 in", 612, 792),
    LEGAL("Legal", "8.5 × 14 in", 612, 1008),
    ORIGINAL("Original", "Match Image", 0, 0)
}

enum class PdfOrientation(val displayName: String) {
    AUTO("Auto"),
    PORTRAIT("Portrait"),
    LANDSCAPE("Landscape")
}

enum class PdfQuality(val displayName: String, val compressionLabel: String, val jpegQuality: Int, val maxDimension: Int) {
    SMALL_SIZE("Small Size", "Smaller file, high compression", 55, 1200),
    BALANCED("Balanced", "Recommended for most documents", 80, 1800),
    HIGH_QUALITY("High Quality", "Crisp text and high detail", 95, 2500)
}

enum class FilterType(val displayName: String) {
    ORIGINAL("Original"),
    AUTO("Auto"),
    DOCUMENT("Document"),
    GRAYSCALE("Grayscale"),
    BLACK_AND_WHITE("Black & White")
}

enum class WatermarkPosition(val displayName: String) {
    TOP_LEFT("Top Left"),
    TOP_CENTER("Top Center"),
    TOP_RIGHT("Top Right"),
    CENTER("Center"),
    BOTTOM_LEFT("Bottom Left"),
    BOTTOM_CENTER("Bottom Center"),
    BOTTOM_RIGHT("Bottom Right")
}

enum class ScanMode {
    SINGLE,
    BATCH
}

enum class PdfSortOption(val displayName: String) {
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First"),
    NAME_ASC("Name A–Z"),
    NAME_DESC("Name Z–A"),
    LARGEST_FIRST("Largest First"),
    SMALLEST_FIRST("Smallest First")
}

enum class TextAlignOption(val displayName: String) {
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right"),
    JUSTIFY("Justify")
}

enum class MarginOption(val displayName: String, val marginPt: Float) {
    NARROW("Narrow", 20f),
    NORMAL("Normal", 36f),
    WIDE("Wide", 54f)
}
