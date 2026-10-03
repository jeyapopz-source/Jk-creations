package com.example

import com.example.model.DocumentCorners
import com.example.model.MarginOption
import com.example.model.PageSize
import com.example.model.PdfDocumentItem
import com.example.model.PdfOrientation
import com.example.model.PdfSortOption
import com.example.model.PointF2D
import com.example.model.TextAlignOption
import com.example.model.TextToPdfSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testDocumentCornersDefaults() {
        val corners = DocumentCorners()
        assertNotNull(corners.topLeft)
        assertNotNull(corners.topRight)
        assertNotNull(corners.bottomRight)
        assertNotNull(corners.bottomLeft)

        assertTrue(corners.topLeft.x < corners.topRight.x)
        assertTrue(corners.topLeft.y < corners.bottomLeft.y)
        assertTrue(corners.bottomLeft.x < corners.bottomRight.x)
        assertTrue(corners.topRight.y < corners.bottomRight.y)
    }

    @Test
    fun testTextToPdfSettings_defaults() {
        val settings = TextToPdfSettings(
            title = "My Report",
            content = "This is a test paragraph.",
            fontSizeSp = 16f,
            isBold = true,
            alignment = TextAlignOption.CENTER,
            pageSize = PageSize.A4,
            orientation = PdfOrientation.PORTRAIT,
            marginOption = MarginOption.NORMAL
        )

        assertEquals("My Report", settings.title)
        assertEquals("This is a test paragraph.", settings.content)
        assertEquals(16f, settings.fontSizeSp)
        assertTrue(settings.isBold)
        assertEquals(TextAlignOption.CENTER, settings.alignment)
        assertEquals(PageSize.A4, settings.pageSize)
        assertEquals(PdfOrientation.PORTRAIT, settings.orientation)
        assertEquals(MarginOption.NORMAL, settings.marginOption)
    }

    @Test
    fun testPdfSearch_caseInsensitive() {
        val list = listOf(
            PdfDocumentItem("1", "Invoice_2026.pdf", "/path/1", 2, 1000L),
            PdfDocumentItem("2", "School_Report.pdf", "/path/2", 4, 2000L),
            PdfDocumentItem("3", "Documents.pdf", "/path/3", 1, 500L)
        )

        val query = "school"
        val filtered = list.filter { it.fileName.lowercase().contains(query.lowercase()) }
        assertEquals(1, filtered.size)
        assertEquals("School_Report.pdf", filtered.first().fileName)
    }

    @Test
    fun testPdfSort_options() {
        val item1 = PdfDocumentItem("1", "Alpha.pdf", "/path/1", 2, 500L, createdAt = 1000L)
        val item2 = PdfDocumentItem("2", "Beta.pdf", "/path/2", 4, 2000L, createdAt = 2000L)
        val item3 = PdfDocumentItem("3", "Gamma.pdf", "/path/3", 1, 1000L, createdAt = 3000L)
        val list = listOf(item2, item1, item3)

        // Newest first
        val newest = list.sortedByDescending { it.createdAt }
        assertEquals("Gamma.pdf", newest.first().fileName)

        // Oldest first
        val oldest = list.sortedBy { it.createdAt }
        assertEquals("Alpha.pdf", oldest.first().fileName)

        // Name A-Z
        val nameAsc = list.sortedBy { it.fileName.lowercase() }
        assertEquals("Alpha.pdf", nameAsc.first().fileName)
        assertEquals("Gamma.pdf", nameAsc.last().fileName)

        // Largest first
        val largest = list.sortedByDescending { it.fileSizeBytes }
        assertEquals("Beta.pdf", largest.first().fileName)

        // Smallest first
        val smallest = list.sortedBy { it.fileSizeBytes }
        assertEquals("Alpha.pdf", smallest.first().fileName)
    }

    @Test
    fun testFilenameSanitization() {
        val input = "My/Illegal:Doc*Name?.pdf"
        val clean = input.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        assertEquals("My_Illegal_Doc_Name_.pdf", clean)
        assertTrue(clean.endsWith(".pdf"))
    }
}
