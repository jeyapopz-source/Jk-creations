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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.FileSharingUtils
import com.example.ui.components.JKCard
import com.example.ui.components.JKPrimaryButton
import com.example.ui.components.JKSecondaryButton
import com.example.ui.theme.BackgroundRadialGradient
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CopperDark
import com.example.ui.theme.CopperGradient
import com.example.ui.theme.CopperLight
import com.example.ui.theme.CopperPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun PdfSuccessScreen(
    viewModel: AppViewModel,
    onCreateAnother: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val lastPdf by viewModel.lastGeneratedPdf.collectAsState()
    val pdfSettings by viewModel.pdfSettings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val fileName = lastPdf?.fileName ?: pdfSettings.fileName.ifBlank { "My_Document.pdf" }
    val pageCount = lastPdf?.pageCount ?: 1
    val fileSizeFormatted = if (lastPdf != null) {
        val kb = (lastPdf!!.fileSizeBytes / 1024)
        if (kb > 1024) String.format("%.1f MB", kb / 1024.0) else "$kb KB"
    } else {
        "245 KB"
    }

    // Modern Android Storage Access Framework (SAF) document export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { destUri: Uri? ->
        if (destUri != null && lastPdf != null) {
            val result = viewModel.exportPdf(lastPdf!!.filePath, destUri)
            if (result.isSuccess) {
                scope.launch {
                    snackbarHostState.showSnackbar("PDF exported successfully")
                }
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Failed to export PDF"
                scope.launch {
                    snackbarHostState.showSnackbar("Export failed: $err")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CharcoalBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundRadialGradient)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .testTag("pdf_success_screen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Success Badge with Checkmark
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(CharcoalCardElevated)
                        .border(
                            2.dp,
                            Brush.linearGradient(listOf(SuccessGreen, CopperLight)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = SuccessGreen,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "✓ PDF Created Successfully",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your document has been compiled and saved to local storage.",
                    color = TextGrayLight,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // PDF Metadata Card
                JKCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CopperPrimary.copy(alpha = 0.2f))
                                    .border(1.dp, CopperPrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = CopperLight,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = fileName,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Ready to view, share or export",
                                    color = CopperLight,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(CharcoalBorder)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Metadata: Filename, Number of pages, File size
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            MetaStatItem(label = "PAGES", value = "$pageCount Page${if (pageCount > 1) "s" else ""}")
                            MetaStatItem(label = "SIZE", value = fileSizeFormatted)
                            MetaStatItem(label = "FORMAT", value = "PDF Standard")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Open PDF Button with real Intent and FileProvider
                JKPrimaryButton(
                    text = "Open PDF",
                    icon = Icons.Default.OpenInNew,
                    onClick = {
                        val path = lastPdf?.filePath
                        if (path != null) {
                            val result = FileSharingUtils.openPdf(context, path)
                            when (result) {
                                FileSharingUtils.OpenResult.SUCCESS -> {}
                                FileSharingUtils.OpenResult.FILE_MISSING -> {
                                    scope.launch { snackbarHostState.showSnackbar("File no longer available on storage.") }
                                }
                                FileSharingUtils.OpenResult.NO_VIEWER_APP -> {
                                    scope.launch { snackbarHostState.showSnackbar("No compatible PDF viewer is installed on this device.") }
                                }
                                FileSharingUtils.OpenResult.URI_ERROR -> {
                                    scope.launch { snackbarHostState.showSnackbar("Unable to access PDF file.") }
                                }
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("PDF file path not found.")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "success_open_pdf_button"
                )

                // Share and Save / Export
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Real Share sheet
                    JKSecondaryButton(
                        text = "Share",
                        icon = Icons.Default.Share,
                        onClick = {
                            val path = lastPdf?.filePath
                            if (path != null) {
                                val success = FileSharingUtils.sharePdf(context, path)
                                if (!success) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Unable to initiate sharing.")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "success_share_button"
                    )

                    // Real Save / Export using Storage Access Framework
                    JKSecondaryButton(
                        text = "Save / Export",
                        icon = Icons.Default.Download,
                        onClick = {
                            if (lastPdf != null) {
                                exportLauncher.launch(fileName)
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("No PDF file to export.")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "success_save_export_button"
                    )
                }

                // Create Another Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            viewModel.resetForNewPdf()
                            onCreateAnother()
                        }
                        .padding(vertical = 12.dp)
                        .testTag("success_create_another_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = CopperLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Create Another",
                            color = CopperLight,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = TextGrayMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = TextWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
