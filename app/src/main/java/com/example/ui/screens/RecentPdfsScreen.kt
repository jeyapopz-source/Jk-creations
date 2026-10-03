package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PdfDocumentItem
import com.example.model.PdfSortOption
import com.example.service.FileSharingUtils
import com.example.ui.components.JKEmptyState
import com.example.ui.components.JKPrimaryButton
import com.example.ui.components.JKSecondaryButton
import com.example.ui.components.JKTopAppBar
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CopperDark
import com.example.ui.theme.CopperLight
import com.example.ui.theme.CopperPrimary
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentPdfsScreen(
    viewModel: AppViewModel,
    onBackClick: () -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val recentPdfs by viewModel.recentPdfs.collectAsState()
    val filteredPdfs by viewModel.filteredRecentPdfs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeSort by viewModel.sortOption.collectAsState()
    val selectedPdfForDetails by viewModel.selectedPdfForDetails.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var sortMenuExpanded by remember { mutableStateOf(false) }

    // Dialog States
    var pdfToRename by remember { mutableStateOf<PdfDocumentItem?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var renameError by remember { mutableStateOf<String?>(null) }

    var pdfToDelete by remember { mutableStateOf<PdfDocumentItem?>(null) }
    var pdfToExport by remember { mutableStateOf<PdfDocumentItem?>(null) }

    // SAF Document Export Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { destUri: Uri? ->
        if (destUri != null && pdfToExport != null) {
            val result = viewModel.exportPdf(pdfToExport!!.filePath, destUri)
            if (result.isSuccess) {
                scope.launch { snackbarHostState.showSnackbar("PDF exported successfully") }
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Export failed"
                scope.launch { snackbarHostState.showSnackbar("Export error: $err") }
            }
        }
        pdfToExport = null
    }

    LaunchedEffect(Unit) {
        viewModel.loadRecentPdfs()
    }

    // Rename Dialog
    if (pdfToRename != null) {
        AlertDialog(
            onDismissRequest = {
                pdfToRename = null
                renameError = null
            },
            title = {
                Text("Rename PDF", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column {
                    Text("Enter a new filename for this document:", color = TextGrayLight, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = {
                            renameInput = it
                            renameError = null
                        },
                        singleLine = true,
                        isError = renameError != null,
                        supportingText = {
                            if (renameError != null) {
                                Text(text = renameError!!, color = Color(0xFFF87171), fontSize = 11.sp)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CopperLight,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            errorBorderColor = Color(0xFFF87171)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("rename_pdf_text_field")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val pdf = pdfToRename
                        if (pdf != null) {
                            val res = viewModel.renameRecentPdf(pdf.id, renameInput)
                            if (res.isSuccess) {
                                scope.launch { snackbarHostState.showSnackbar("Renamed successfully") }
                                pdfToRename = null
                                renameError = null
                            } else {
                                renameError = res.exceptionOrNull()?.localizedMessage ?: "Rename failed"
                            }
                        }
                    },
                    modifier = Modifier.testTag("rename_pdf_confirm_button")
                ) {
                    Text("Rename", color = CopperLight, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pdfToRename = null
                    renameError = null
                }) {
                    Text(if (renameError != null) "Cancel" else "Cancel", color = TextGrayLight)
                }
            },
            containerColor = CharcoalCardElevated,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Delete Confirmation Dialog
    if (pdfToDelete != null) {
        AlertDialog(
            onDismissRequest = { pdfToDelete = null },
            title = {
                Text("Delete this PDF?", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text(
                    "Are you sure you want to delete ${pdfToDelete?.fileName}? This action removes the local document permanently.",
                    color = TextGrayLight,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pdfToDelete?.let {
                            viewModel.deleteRecentPdf(it.id)
                            scope.launch { snackbarHostState.showSnackbar("PDF deleted") }
                        }
                        pdfToDelete = null
                    },
                    modifier = Modifier.testTag("delete_pdf_confirm_button")
                ) {
                    Text("Delete", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pdfToDelete = null }) {
                    Text("Cancel", color = CopperLight)
                }
            },
            containerColor = CharcoalCardElevated,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // PDF Details Dialog
    if (selectedPdfForDetails != null) {
        val pdf = selectedPdfForDetails!!
        PdfDetailsDialog(
            pdf = pdf,
            onDismiss = { viewModel.selectPdfForDetails(null) },
            onOpen = {
                viewModel.selectPdfForDetails(null)
                handleOpenPdf(context, pdf, scope, snackbarHostState)
            },
            onShare = {
                viewModel.selectPdfForDetails(null)
                FileSharingUtils.sharePdf(context, pdf.filePath)
            },
            onRename = {
                viewModel.selectPdfForDetails(null)
                renameInput = pdf.fileName.removeSuffix(".pdf")
                renameError = null
                pdfToRename = pdf
            },
            onDelete = {
                viewModel.selectPdfForDetails(null)
                pdfToDelete = pdf
            }
        )
    }

    Scaffold(
        topBar = {
            JKTopAppBar(
                title = "Recent PDFs",
                onBackClick = onBackClick,
                actions = {
                    // Sort Menu Button
                    Box {
                        IconButton(
                            onClick = { sortMenuExpanded = true },
                            modifier = Modifier.testTag("recent_pdfs_sort_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort PDFs",
                                tint = CopperLight
                            )
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false },
                            modifier = Modifier.background(CharcoalCard)
                        ) {
                            PdfSortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.displayName,
                                            color = if (activeSort == option) CopperLight else TextWhite,
                                            fontWeight = if (activeSort == option) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSortOption(option)
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CharcoalBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("recent_pdfs_screen")
        ) {
            // Search Field
            if (recentPdfs.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text("Search PDFs", color = TextGrayMuted, fontSize = 14.sp)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = CopperLight,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Search",
                                        tint = TextGrayLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CopperLight,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = CharcoalCard,
                            unfocusedContainerColor = CharcoalCard
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recent_pdfs_search_field")
                    )
                }

                // Active Sort indicator pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sorted by: ${activeSort.displayName}",
                        color = TextGrayMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${filteredPdfs.size} PDF${if (filteredPdfs.size != 1) "s" else ""}",
                        color = CopperLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Body Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (recentPdfs.isEmpty()) {
                    // Empty state per prompt: "No PDFs created yet"
                    JKEmptyState(
                        icon = Icons.Default.Description,
                        title = "No PDFs created yet",
                        subtitle = "Create your first PDF from photos or scanned documents.",
                        actionButtonText = "Create PDF",
                        onActionClick = onNavigateToCreate,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (filteredPdfs.isEmpty()) {
                    // Empty Search result state
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                            .align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextGrayMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No PDFs found",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No document matches \"$searchQuery\"",
                            color = TextGrayLight,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        JKSecondaryButton(
                            text = "Clear Search",
                            onClick = { viewModel.setSearchQuery("") },
                            testTag = "clear_search_empty_button"
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredPdfs, key = { it.id }) { pdf ->
                            RecentPdfItemCard(
                                pdf = pdf,
                                onOpen = {
                                    handleOpenPdf(context, pdf, scope, snackbarHostState)
                                },
                                onShare = {
                                    if (pdf.isMissing) {
                                        scope.launch { snackbarHostState.showSnackbar("File no longer available") }
                                    } else {
                                        FileSharingUtils.sharePdf(context, pdf.filePath)
                                    }
                                },
                                onRename = {
                                    renameInput = pdf.fileName.removeSuffix(".pdf")
                                    renameError = null
                                    pdfToRename = pdf
                                },
                                onDelete = {
                                    pdfToDelete = pdf
                                },
                                onDetails = {
                                    viewModel.selectPdfForDetails(pdf)
                                },
                                onExport = {
                                    pdfToExport = pdf
                                    exportLauncher.launch(pdf.fileName)
                                },
                                onRemoveStale = {
                                    viewModel.removeStaleRecentPdf(pdf.id)
                                    scope.launch { snackbarHostState.showSnackbar("Removed stale entry from history") }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun handleOpenPdf(
    context: android.content.Context,
    pdf: PdfDocumentItem,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val result = FileSharingUtils.openPdf(context, pdf.filePath)
    when (result) {
        FileSharingUtils.OpenResult.SUCCESS -> {}
        FileSharingUtils.OpenResult.FILE_MISSING -> {
            scope.launch { snackbarHostState.showSnackbar("File no longer available on storage.") }
        }
        FileSharingUtils.OpenResult.NO_VIEWER_APP -> {
            scope.launch { snackbarHostState.showSnackbar("No compatible PDF viewer is installed on this device.") }
        }
        FileSharingUtils.OpenResult.URI_ERROR -> {
            scope.launch { snackbarHostState.showSnackbar("Unable to generate secure file URI.") }
        }
    }
}

@Composable
private fun RecentPdfItemCard(
    pdf: PdfDocumentItem,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDetails: () -> Unit,
    onExport: () -> Unit,
    onRemoveStale: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val formattedDate = remember(pdf.createdAt) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(Date(pdf.createdAt))
    }

    val fileSizeFormatted = remember(pdf.fileSizeBytes) {
        val kb = (pdf.fileSizeBytes / 1024)
        if (kb > 1024) String.format("%.1f MB", kb / 1024.0) else "$kb KB"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalCardElevated),
        border = BorderStroke(
            1.dp,
            if (pdf.isMissing) Color(0xFFF87171).copy(alpha = 0.5f) else CharcoalBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .testTag("recent_pdf_item_${pdf.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // PDF Icon Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (pdf.isMissing) Color(0x33F87171) else CopperPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (pdf.isMissing) Icons.Default.Warning else Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = if (pdf.isMissing) Color(0xFFF87171) else CopperLight,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = pdf.fileName,
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (pdf.isMissing) {
                        Text(
                            text = "File no longer available",
                            color = Color(0xFFF87171),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "${pdf.pageCount} Page${if (pdf.pageCount > 1) "s" else ""} • $fileSizeFormatted",
                            color = CopperLight,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = formattedDate,
                        color = TextGrayLight,
                        fontSize = 10.sp
                    )
                }
            }

            // Options 3-dot Menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("recent_pdf_options_${pdf.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextGrayLight
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(CharcoalCard)
                ) {
                    if (pdf.isMissing) {
                        DropdownMenuItem(
                            text = { Text("Remove from History", color = Color(0xFFF87171)) },
                            leadingIcon = { Icon(Icons.Default.DeleteForever, null, tint = Color(0xFFF87171)) },
                            onClick = { menuExpanded = false; onRemoveStale() }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Open", color = TextWhite) },
                            leadingIcon = { Icon(Icons.Default.OpenInNew, null, tint = CopperLight) },
                            onClick = { menuExpanded = false; onOpen() }
                        )
                        DropdownMenuItem(
                            text = { Text("Share", color = TextWhite) },
                            leadingIcon = { Icon(Icons.Default.Share, null, tint = CopperLight) },
                            onClick = { menuExpanded = false; onShare() }
                        )
                        DropdownMenuItem(
                            text = { Text("Save / Export", color = TextWhite) },
                            leadingIcon = { Icon(Icons.Default.Download, null, tint = CopperLight) },
                            onClick = { menuExpanded = false; onExport() }
                        )
                        DropdownMenuItem(
                            text = { Text("Rename", color = TextWhite) },
                            leadingIcon = { Icon(Icons.Default.Edit, null, tint = CopperLight) },
                            onClick = { menuExpanded = false; onRename() }
                        )
                        DropdownMenuItem(
                            text = { Text("Details", color = TextWhite) },
                            leadingIcon = { Icon(Icons.Default.Info, null, tint = CopperLight) },
                            onClick = { menuExpanded = false; onDetails() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFF87171)) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFF87171)) },
                            onClick = { menuExpanded = false; onDelete() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfDetailsDialog(
    pdf: PdfDocumentItem,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val createdStr = remember(pdf.createdAt) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault())
        sdf.format(Date(pdf.createdAt))
    }
    val modifiedStr = remember(pdf.modifiedAt) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault())
        sdf.format(Date(pdf.modifiedAt))
    }
    val fileSizeFormatted = remember(pdf.fileSizeBytes) {
        val kb = (pdf.fileSizeBytes / 1024)
        if (kb > 1024) String.format("%.2f MB (%d bytes)", kb / 1024.0, pdf.fileSizeBytes) else "$kb KB (${pdf.fileSizeBytes} bytes)"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PDF Details", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextGrayLight)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailInfoItem("Filename", pdf.fileName)
                DetailInfoItem("Pages", "${pdf.pageCount} Pages")
                DetailInfoItem("File Size", fileSizeFormatted)
                DetailInfoItem("Created Date", createdStr)
                DetailInfoItem("Modified Date", modifiedStr)
                DetailInfoItem("Storage Path", pdf.filePath)
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                TextButton(onClick = onOpen) {
                    Text("Open", color = CopperLight, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onShare) {
                    Text("Share", color = CopperLight, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onRename) {
                    Text("Rename", color = CopperLight, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDelete) {
                    Text("Delete", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = CharcoalCardElevated,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
private fun DetailInfoItem(label: String, value: String) {
    Column {
        Text(text = label, color = CopperLight, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = TextWhite, fontSize = 13.sp, lineHeight = 18.sp)
    }
}
