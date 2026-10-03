package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.model.PdfDocumentItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream

object LocalPdfStorageManager {
    private const val PREFS_NAME = "jk_recent_pdfs_prefs"
    private const val KEY_RECENT_PDFS = "recent_pdfs_json"

    fun saveRecentPdf(context: Context, item: PdfDocumentItem) {
        val currentList = getRecentPdfs(context, includeMissing = true).toMutableList()
        currentList.removeAll { it.filePath == item.filePath || it.id == item.id }
        currentList.add(0, item) // newest first

        saveList(context, currentList)
    }

    fun getRecentPdfs(context: Context, includeMissing: Boolean = true): List<PdfDocumentItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_RECENT_PDFS, null) ?: return emptyList()
        val list = mutableListOf<PdfDocumentItem>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val filePath = obj.getString("filePath")
                val file = File(filePath)
                val fileExists = file.exists()

                if (fileExists || includeMissing) {
                    val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    val modifiedAt = obj.optLong("modifiedAt", createdAt)
                    val fileSize = if (fileExists && file.length() > 0) file.length() else obj.optLong("fileSizeBytes", 0L)

                    list.add(
                        PdfDocumentItem(
                            id = obj.getString("id"),
                            fileName = obj.getString("fileName"),
                            filePath = filePath,
                            pageCount = obj.optInt("pageCount", 1),
                            fileSizeBytes = fileSize,
                            createdAt = createdAt,
                            modifiedAt = modifiedAt,
                            isMissing = !fileExists
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun isFileNameTaken(context: Context, newName: String, excludeId: String? = null): Boolean {
        var clean = newName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
        if (!clean.endsWith(".pdf", ignoreCase = true)) clean += ".pdf"

        val currentList = getRecentPdfs(context, includeMissing = true)
        for (item in currentList) {
            if (item.id != excludeId && item.fileName.equals(clean, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    fun renamePdf(context: Context, id: String, newName: String): Result<PdfDocumentItem> {
        var clean = newName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
        if (clean.isBlank() || clean.equals(".pdf", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Filename cannot be empty."))
        }
        if (!clean.endsWith(".pdf", ignoreCase = true)) {
            clean += ".pdf"
        }

        if (isFileNameTaken(context, clean, excludeId = id)) {
            return Result.failure(IllegalArgumentException("A file with this name already exists."))
        }

        val currentList = getRecentPdfs(context, includeMissing = true).toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index == -1) {
            return Result.failure(IllegalArgumentException("Document not found in history."))
        }

        val oldItem = currentList[index]
        val oldFile = File(oldItem.filePath)
        if (!oldFile.exists()) {
            return Result.failure(IllegalStateException("Physical file no longer available."))
        }

        val newFile = File(oldFile.parentFile, clean)
        if (newFile.exists() && newFile.absolutePath != oldFile.absolutePath) {
            return Result.failure(IllegalArgumentException("A file with this name already exists."))
        }

        val success = oldFile.renameTo(newFile)
        if (success) {
            val updated = oldItem.copy(
                fileName = clean,
                filePath = newFile.absolutePath,
                modifiedAt = System.currentTimeMillis(),
                isMissing = false
            )
            currentList[index] = updated
            saveList(context, currentList)
            return Result.success(updated)
        } else {
            return Result.failure(IllegalStateException("Failed to rename file on storage."))
        }
    }

    fun deletePdf(context: Context, id: String): Boolean {
        val currentList = getRecentPdfs(context, includeMissing = true).toMutableList()
        val item = currentList.find { it.id == id } ?: return false
        val file = File(item.filePath)
        if (file.exists()) {
            file.delete()
        }
        currentList.removeAll { it.id == id }
        saveList(context, currentList)
        return true
    }

    fun removeStalePdf(context: Context, id: String) {
        val currentList = getRecentPdfs(context, includeMissing = true).toMutableList()
        currentList.removeAll { it.id == id }
        saveList(context, currentList)
    }

    fun exportPdf(context: Context, sourceFilePath: String, destUri: Uri): Result<Unit> {
        return try {
            val sourceFile = File(sourceFilePath)
            if (!sourceFile.exists()) {
                return Result.failure(IllegalStateException("Source PDF file does not exist."))
            }

            val inputStream: InputStream = FileInputStream(sourceFile)
            val outputStream: OutputStream = context.contentResolver.openOutputStream(destUri)
                ?: return Result.failure(IllegalStateException("Cannot open destination storage."))

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun saveList(context: Context, list: List<PdfDocumentItem>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        list.take(50).forEach { pdf ->
            val obj = JSONObject().apply {
                put("id", pdf.id)
                put("fileName", pdf.fileName)
                put("filePath", pdf.filePath)
                put("pageCount", pdf.pageCount)
                put("fileSizeBytes", pdf.fileSizeBytes)
                put("createdAt", pdf.createdAt)
                put("modifiedAt", pdf.modifiedAt)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_RECENT_PDFS, jsonArray.toString()).apply()
    }
}

object FileSharingUtils {

    fun getContentUriForFile(context: Context, file: File): Uri? {
        return try {
            val authority = "${context.packageName}.fileprovider"
            FileProvider.getUriForFile(context, authority, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun openPdf(context: Context, filePath: String): OpenResult {
        val file = File(filePath)
        if (!file.exists()) {
            return OpenResult.FILE_MISSING
        }

        val contentUri = getContentUriForFile(context, file) ?: return OpenResult.URI_ERROR

        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
            OpenResult.SUCCESS
        } catch (e: Exception) {
            e.printStackTrace()
            OpenResult.NO_VIEWER_APP
        }
    }

    fun sharePdf(context: Context, filePath: String): Boolean {
        val file = File(filePath)
        if (!file.exists()) return false

        val contentUri = getContentUriForFile(context, file) ?: return false

        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            val chooser = Intent.createChooser(intent, "Share PDF Document").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    enum class OpenResult {
        SUCCESS,
        FILE_MISSING,
        NO_VIEWER_APP,
        URI_ERROR
    }
}
