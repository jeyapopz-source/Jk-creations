package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CropRect
import com.example.model.FilterType
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

@Composable
fun ImageEditorScreen(
    viewModel: AppViewModel,
    onBackClick: () -> Unit,
    onNavigateToPdfSettings: () -> Unit
) {
    val activeImage by viewModel.activeEditorImage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var isCropMode by remember { mutableStateOf(false) }

    // Normalized crop box bounds (0f..1f)
    var cropLeft by remember { mutableFloatStateOf(activeImage?.cropRect?.left ?: 0.05f) }
    var cropTop by remember { mutableFloatStateOf(activeImage?.cropRect?.top ?: 0.05f) }
    var cropRight by remember { mutableFloatStateOf(activeImage?.cropRect?.right ?: 0.95f) }
    var cropBottom by remember { mutableFloatStateOf(activeImage?.cropRect?.bottom ?: 0.95f) }

    val activeFilter = activeImage?.filterType ?: FilterType.ORIGINAL

    // Color filter representation for Compose preview
    val composeColorFilter = remember(activeFilter) {
        when (activeFilter) {
            FilterType.ORIGINAL -> null
            FilterType.GRAYSCALE -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
            FilterType.BLACK_AND_WHITE -> {
                val cm = ColorMatrix(
                    floatArrayOf(
                        2.5f, 2.5f, 2.5f, 0f, -250f,
                        2.5f, 2.5f, 2.5f, 0f, -250f,
                        2.5f, 2.5f, 2.5f, 0f, -250f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                ColorFilter.colorMatrix(cm)
            }
            FilterType.DOCUMENT -> {
                val cm = ColorMatrix(
                    floatArrayOf(
                        1.4f, 0f, 0f, 0f, 15f,
                        0f, 1.4f, 0f, 0f, 15f,
                        0f, 0f, 1.4f, 0f, 15f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                ColorFilter.colorMatrix(cm)
            }
            FilterType.AUTO -> {
                val cm = ColorMatrix(
                    floatArrayOf(
                        1.25f, 0f, 0f, 0f, 10f,
                        0f, 1.25f, 0f, 0f, 10f,
                        0f, 0f, 1.25f, 0f, 10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                ColorFilter.colorMatrix(cm)
            }
        }
    }

    Scaffold(
        topBar = {
            JKTopAppBar(
                title = if (isCropMode) "Crop Document" else "Edit Document",
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch { snackbarHostState.showSnackbar("Adjustments preserved for PDF") }
                            onBackClick()
                        },
                        modifier = Modifier.testTag("editor_save_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = CopperLight
                        )
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
                .padding(16.dp)
                .navigationBarsPadding()
                .testTag("image_editor_screen"),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Main Document Preview Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CharcoalCard)
                    .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val rotation = activeImage?.rotationDegrees ?: 0

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .rotate(rotation.toFloat()),
                    contentAlignment = Alignment.Center
                ) {
                    if (activeImage?.uri != null) {
                        AsyncImage(
                            model = Uri.parse(activeImage!!.uri),
                            contentDescription = "Document Page",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            colorFilter = composeColorFilter
                        )
                    } else {
                        // Document Canvas template with active filters
                        Column(
                            modifier = Modifier
                                .fillMaxSize(0.9f)
                                .background(if (activeFilter == FilterType.BLACK_AND_WHITE) Color.White else Color(0xFFF9F9F9))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "JK CREATIONS DOCUMENT",
                                color = if (activeFilter == FilterType.BLACK_AND_WHITE) Color.Black else Color.DarkGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                val lineColor = if (activeFilter == FilterType.BLACK_AND_WHITE) Color.Black else Color.Gray
                                Box(modifier = Modifier.fillMaxWidth(0.95f).height(6.dp).background(lineColor))
                                Box(modifier = Modifier.fillMaxWidth(0.85f).height(6.dp).background(lineColor))
                                Box(modifier = Modifier.fillMaxWidth(0.90f).height(6.dp).background(lineColor))
                                Box(modifier = Modifier.fillMaxWidth(0.70f).height(6.dp).background(lineColor))
                            }
                            Text(
                                text = "Page ${activeImage?.pageNumber ?: 1} • ${activeFilter.displayName}",
                                color = Color.DarkGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Interactive Crop Overlay Rectangle
                    if (isCropMode) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val leftPx = cropLeft * w
                            val topPx = cropTop * h
                            val rightPx = cropRight * w
                            val bottomPx = cropBottom * h

                            // Dimmed outer area
                            drawRect(
                                color = Color.Black.copy(alpha = 0.55f),
                                topLeft = Offset.Zero,
                                size = Size(w, topPx)
                            )
                            drawRect(
                                color = Color.Black.copy(alpha = 0.55f),
                                topLeft = Offset(0f, bottomPx),
                                size = Size(w, h - bottomPx)
                            )
                            drawRect(
                                color = Color.Black.copy(alpha = 0.55f),
                                topLeft = Offset(0f, topPx),
                                size = Size(leftPx, bottomPx - topPx)
                            )
                            drawRect(
                                color = Color.Black.copy(alpha = 0.55f),
                                topLeft = Offset(rightPx, topPx),
                                size = Size(w - rightPx, bottomPx - topPx)
                            )

                            // Copper Crop Boundary Line
                            drawRect(
                                color = Color(0xFFE09F74),
                                topLeft = Offset(leftPx, topPx),
                                size = Size(rightPx - leftPx, bottomPx - topPx),
                                style = Stroke(width = 3.dp.toPx())
                            )

                            // Corner Handles
                            val handleSize = 14.dp.toPx()
                            drawCircle(Color(0xFFE09F74), handleSize / 2, Offset(leftPx, topPx))
                            drawCircle(Color(0xFFE09F74), handleSize / 2, Offset(rightPx, topPx))
                            drawCircle(Color(0xFFE09F74), handleSize / 2, Offset(leftPx, bottomPx))
                            drawCircle(Color(0xFFE09F74), handleSize / 2, Offset(rightPx, bottomPx))
                        }
                    }
                }

                // Page Tag in Canvas
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CharcoalBlack.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Page ${activeImage?.pageNumber ?: 1} • ${activeImage?.rotationDegrees ?: 0}°",
                        color = CopperLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isCropMode) {
                // Crop Adjustment Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CharcoalCard)
                        .border(1.dp, CopperPrimary.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "DRAG HANDLES OR USE SLIDERS TO CROP",
                        color = CopperLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Horizontal Margins", color = TextGrayLight, fontSize = 12.sp)
                        Text(
                            text = "${((cropRight - cropLeft) * 100).toInt()}% width",
                            color = CopperLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = cropLeft,
                        onValueChange = {
                            cropLeft = it.coerceIn(0f, 0.4f)
                            cropRight = (1f - cropLeft).coerceIn(0.6f, 1f)
                        },
                        valueRange = 0f..0.4f,
                        colors = SliderDefaults.colors(
                            thumbColor = CopperLight,
                            activeTrackColor = CopperPrimary,
                            inactiveTrackColor = CharcoalBorder
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Vertical Margins", color = TextGrayLight, fontSize = 12.sp)
                        Text(
                            text = "${((cropBottom - cropTop) * 100).toInt()}% height",
                            color = CopperLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = cropTop,
                        onValueChange = {
                            cropTop = it.coerceIn(0f, 0.4f)
                            cropBottom = (1f - cropTop).coerceIn(0.6f, 1f)
                        },
                        valueRange = 0f..0.4f,
                        colors = SliderDefaults.colors(
                            thumbColor = CopperLight,
                            activeTrackColor = CopperPrimary,
                            inactiveTrackColor = CharcoalBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        JKSecondaryButton(
                            text = "Cancel",
                            icon = Icons.Default.Close,
                            onClick = { isCropMode = false },
                            modifier = Modifier.weight(1f),
                            testTag = "crop_cancel_button"
                        )

                        JKPrimaryButton(
                            text = "Confirm Crop",
                            icon = Icons.Default.Check,
                            onClick = {
                                viewModel.updateEditorCrop(
                                    CropRect(cropLeft, cropTop, cropRight, cropBottom)
                                )
                                isCropMode = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Crop applied to document")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "crop_confirm_button"
                        )
                    }
                }
            } else {
                // Editing Tool Controls Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CharcoalCard)
                        .border(1.dp, CharcoalBorder, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    // Row of Tools: Crop & Rotate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        EditorActionButton(
                            icon = Icons.Default.Crop,
                            label = "Crop",
                            isActive = activeImage?.cropRect != null,
                            testTag = "editor_tool_crop",
                            onClick = { isCropMode = true }
                        )

                        EditorActionButton(
                            icon = Icons.Default.RotateRight,
                            label = "Rotate 90°",
                            isActive = false,
                            testTag = "editor_tool_rotate",
                            onClick = {
                                viewModel.rotateEditorImage()
                                scope.launch { snackbarHostState.showSnackbar("Rotated 90°") }
                            }
                        )

                        EditorActionButton(
                            icon = Icons.Default.AutoFixHigh,
                            label = "Auto",
                            isActive = activeFilter == FilterType.AUTO,
                            testTag = "editor_tool_autofix",
                            onClick = {
                                viewModel.updateEditorFilter(FilterType.AUTO)
                                scope.launch { snackbarHostState.showSnackbar("Auto enhanced applied") }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "DOCUMENT ENHANCEMENT",
                        color = TextGrayMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Real Filter Chips List: Original, Auto, Document, Grayscale, Black & White
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterType.values().forEach { filter ->
                            val isSelected = activeFilter == filter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) CopperPrimary else CharcoalCardElevated)
                                    .border(
                                        1.dp,
                                        if (isSelected) CopperLight else CharcoalBorder,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        viewModel.updateEditorFilter(filter)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Applied ${filter.displayName} filter")
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("filter_chip_${filter.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = filter.displayName,
                                    color = if (isSelected) TextWhite else TextGrayLight,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation forward to PDF Settings
            JKPrimaryButton(
                text = "Continue to PDF Settings",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = onNavigateToPdfSettings,
                modifier = Modifier.fillMaxWidth(),
                testTag = "editor_proceed_pdf_settings"
            )
        }
    }
}

@Composable
private fun EditorActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (isActive) CopperPrimary else CharcoalCardElevated)
                .border(
                    1.dp,
                    if (isActive) CopperLight else CharcoalBorder,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) TextWhite else CopperLight,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = if (isActive) CopperLight else TextGrayLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
