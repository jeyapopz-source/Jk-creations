package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PdfDocumentItem
import com.example.service.FileSharingUtils
import com.example.ui.components.JKCard
import com.example.ui.components.JKEmptyState
import com.example.ui.components.JKWingsBadge
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CopperDark
import com.example.ui.theme.CopperGradient
import com.example.ui.theme.CopperLight
import com.example.ui.theme.CopperPrimary
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onNavigateToScanner: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToRecentPdfs: () -> Unit,
    onNavigateToTextToPdf: () -> Unit = {}
) {
    val recentPdfs by viewModel.recentPdfs.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadRecentPdfs()
    }

    // Real System Photo Picker for multiple image selection
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val stringUris = uris.map { it.toString() }
            viewModel.addMultipleImages(stringUris)
            onNavigateToGallery()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBlack)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("home_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Logo and Brand Name
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                JKWingsBadge(compact = false)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "JK Creations",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Document Suite",
                        color = CopperLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right: Settings Icon
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CharcoalCard)
                    .border(1.dp, CharcoalBorder, CircleShape)
                    .testTag("home_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = CopperLight,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Main Title & Subtitle
        Text(
            text = "Image to PDF Converter with Wings",
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Scan, enhance and create professional PDFs from your images.",
            color = TextGrayLight,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Main Action Cards (3 Key Actions)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Action Card 1: Scan Document
            HomeActionCard(
                title = "Scan Document",
                description = "Smart camera scanner",
                icon = Icons.Default.CameraAlt,
                isPrimary = true,
                modifier = Modifier.weight(1f),
                testTag = "home_take_photo_card",
                onClick = onNavigateToScanner
            )

            // Action Card 2: Import Images
            HomeActionCard(
                title = "Import Images",
                description = "Choose from gallery",
                icon = Icons.Default.PhotoLibrary,
                isPrimary = false,
                modifier = Modifier.weight(1f),
                testTag = "home_from_gallery_card",
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Card 3: Text to PDF
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CharcoalCardElevated),
            border = BorderStroke(1.dp, CopperPrimary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable(onClick = onNavigateToTextToPdf)
                .testTag("home_text_to_pdf_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(CopperGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Text to PDF",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Write & format documents with custom layouts",
                            color = TextGrayLight,
                            fontSize = 12.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = CopperLight,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Recent PDFs Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 18.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(CopperPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recent PDFs",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            if (recentPdfs.isNotEmpty()) {
                Text(
                    text = "View All (${recentPdfs.size})",
                    color = CopperLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(onClick = onNavigateToRecentPdfs)
                        .padding(4.dp)
                        .testTag("home_recent_pdfs_view_all")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (recentPdfs.isEmpty()) {
            // Attractive Empty State per STEP 3 specifications
            JKCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToRecentPdfs)
                    .testTag("recent_pdfs_empty_card")
            ) {
                JKEmptyState(
                    icon = Icons.Default.Description,
                    title = "No PDFs created yet",
                    subtitle = "Create your first PDF from photos or scanned documents.",
                    actionButtonText = "Create PDF",
                    onActionClick = onNavigateToScanner
                )
            }
        } else {
            // Real Recent PDF Cards: Show latest 3-5 files
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                recentPdfs.take(5).forEach { pdf ->
                    RecentPdfPreviewCard(
                        pdf = pdf,
                        onOpen = {
                            val result = FileSharingUtils.openPdf(context, pdf.filePath)
                            when (result) {
                                FileSharingUtils.OpenResult.SUCCESS -> {}
                                FileSharingUtils.OpenResult.FILE_MISSING -> {
                                    Toast.makeText(context, "File no longer available", Toast.LENGTH_SHORT).show()
                                    viewModel.loadRecentPdfs()
                                }
                                FileSharingUtils.OpenResult.NO_VIEWER_APP -> {
                                    Toast.makeText(context, "No compatible PDF viewer installed on this device.", Toast.LENGTH_LONG).show()
                                }
                                FileSharingUtils.OpenResult.URI_ERROR -> {
                                    Toast.makeText(context, "Unable to access file.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onShare = {
                            FileSharingUtils.sharePdf(context, pdf.filePath)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RecentPdfPreviewCard(
    pdf: PdfDocumentItem,
    onOpen: () -> Unit,
    onShare: () -> Unit
) {
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
        border = BorderStroke(1.dp, CharcoalBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .testTag("recent_pdf_card_${pdf.id}")
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
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CopperPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = CopperLight,
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
                    Text(
                        text = "${pdf.pageCount} Page${if (pdf.pageCount > 1) "s" else ""} • $fileSizeFormatted",
                        color = CopperLight,
                        fontSize = 12.sp
                    )
                    Text(
                        text = formattedDate,
                        color = TextGrayLight,
                        fontSize = 10.sp
                    )
                }
            }

            Row {
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = CopperLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onOpen) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Open",
                        tint = TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPrimary: Boolean,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) CharcoalCardElevated else CharcoalCard
        ),
        border = BorderStroke(
            1.dp,
            if (isPrimary) CopperPrimary.copy(alpha = 0.6f) else CharcoalBorder
        ),
        modifier = modifier
            .height(165.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPrimary) CopperGradient else Brush.linearGradient(listOf(CharcoalBorder, CharcoalBorder))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = TextGrayLight,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
