package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PageSize
import com.example.model.PdfOrientation
import com.example.model.PdfQuality
import com.example.model.WatermarkPosition
import com.example.ui.components.JKCard
import com.example.ui.components.JKPrimaryButton
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
fun PdfSettingsScreen(
    viewModel: AppViewModel,
    onBackClick: () -> Unit,
    onNavigateToProcessing: () -> Unit
) {
    val settings by viewModel.pdfSettings.collectAsState()
    val selectedImages by viewModel.selectedImages.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            JKTopAppBar(
                title = "PDF Settings",
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CharcoalBlack)
                    .border(BorderStroke(0.5.dp, CharcoalBorder))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .navigationBarsPadding()
            ) {
                JKPrimaryButton(
                    text = "CREATE PDF (${selectedImages.size} Page${if (selectedImages.size != 1) "s" else ""})",
                    icon = Icons.Default.PictureAsPdf,
                    onClick = {
                        if (selectedImages.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please add at least one image to create a PDF.")
                            }
                        } else {
                            onNavigateToProcessing()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "create_pdf_button"
                )
            }
        },
        containerColor = CharcoalBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .testTag("pdf_settings_screen"),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // File Name Section
            JKCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FILE NAME",
                        color = CopperLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = settings.fileName,
                        onValueChange = { input ->
                            val sanitized = input.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                            viewModel.updatePdfSettings(fileName = sanitized)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pdf_filename_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CopperLight,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = CharcoalCardElevated,
                            unfocusedContainerColor = CharcoalCardElevated
                        ),
                        singleLine = true,
                        leadingIcon = {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = CopperLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }

            // Page Size Section
            JKCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PAGE SIZE",
                        color = CopperLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PageSize.values().forEach { size ->
                            val isSelected = settings.pageSize == size
                            ChipOption(
                                title = size.displayName,
                                subtitle = size.dimensions,
                                isSelected = isSelected,
                                testTag = "pagesize_${size.name.lowercase()}",
                                onClick = { viewModel.updatePdfSettings(pageSize = size) }
                            )
                        }
                    }
                }
            }

            // Orientation Section
            JKCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ORIENTATION",
                        color = CopperLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PdfOrientation.values().forEach { orientation ->
                            val isSelected = settings.orientation == orientation
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) CopperPrimary else CharcoalCardElevated)
                                    .border(
                                        1.dp,
                                        if (isSelected) CopperLight else CharcoalBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.updatePdfSettings(orientation = orientation) }
                                    .padding(vertical = 12.dp)
                                    .testTag("orientation_${orientation.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = orientation.displayName,
                                    color = if (isSelected) TextWhite else TextGrayLight,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Quality Section
            JKCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "QUALITY",
                        color = CopperLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PdfQuality.values().forEach { q ->
                            val isSelected = settings.quality == q
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) CopperPrimary.copy(alpha = 0.15f) else CharcoalCardElevated)
                                    .border(
                                        1.dp,
                                        if (isSelected) CopperPrimary else CharcoalBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.updatePdfSettings(quality = q) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("quality_${q.name.lowercase()}"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = q.displayName,
                                        color = if (isSelected) CopperLight else TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = q.compressionLabel,
                                        color = TextGrayLight,
                                        fontSize = 11.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(if (isSelected) CopperPrimary else androidx.compose.ui.graphics.Color.Transparent)
                                        .border(1.5.dp, if (isSelected) CopperLight else CharcoalBorder, androidx.compose.foundation.shape.CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            // Watermark Section
            JKCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WATERMARK",
                                color = CopperLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (settings.watermarkEnabled) "Watermark active" else "OFF by default",
                                color = TextGrayLight,
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = settings.watermarkEnabled,
                            onCheckedChange = { viewModel.updatePdfSettings(watermarkEnabled = it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextWhite,
                                checkedTrackColor = CopperPrimary,
                                uncheckedThumbColor = TextGrayMuted,
                                uncheckedTrackColor = CharcoalCardElevated
                            ),
                            modifier = Modifier.testTag("watermark_switch")
                        )
                    }

                    if (settings.watermarkEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Watermark Text
                        Text(
                            text = "Watermark Text",
                            color = TextGrayLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = settings.watermarkText,
                            onValueChange = { viewModel.updatePdfSettings(watermarkText = it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("watermark_text_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CopperLight,
                                unfocusedBorderColor = CharcoalBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = CharcoalCardElevated,
                                unfocusedContainerColor = CharcoalCardElevated
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Position selector (All 7 positions)
                        Text(
                            text = "Position",
                            color = TextGrayLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WatermarkPosition.values().forEach { pos ->
                                val isSelected = settings.watermarkPosition == pos
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) CopperPrimary else CharcoalCardElevated)
                                        .border(1.dp, if (isSelected) CopperLight else CharcoalBorder, RoundedCornerShape(10.dp))
                                        .clickable { viewModel.updatePdfSettings(watermarkPosition = pos) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("watermark_pos_${pos.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pos.displayName,
                                        color = if (isSelected) TextWhite else TextGrayLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Opacity slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Opacity", color = TextGrayLight, fontSize = 12.sp)
                            Text(
                                text = "${(settings.watermarkOpacity * 100).toInt()}%",
                                color = CopperLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = settings.watermarkOpacity,
                            onValueChange = { viewModel.updatePdfSettings(watermarkOpacity = it) },
                            valueRange = 0.1f..0.9f,
                            colors = SliderDefaults.colors(
                                thumbColor = CopperLight,
                                activeTrackColor = CopperPrimary,
                                inactiveTrackColor = CharcoalBorder
                            ),
                            modifier = Modifier.testTag("watermark_opacity_slider")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Size slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Size", color = TextGrayLight, fontSize = 12.sp)
                            Text(
                                text = "${settings.watermarkSize.toInt()} pt",
                                color = CopperLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = settings.watermarkSize,
                            onValueChange = { viewModel.updatePdfSettings(watermarkSize = it) },
                            valueRange = 14f..48f,
                            colors = SliderDefaults.colors(
                                thumbColor = CopperLight,
                                activeTrackColor = CopperPrimary,
                                inactiveTrackColor = CharcoalBorder
                            ),
                            modifier = Modifier.testTag("watermark_size_slider")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChipOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) CopperPrimary else CharcoalCardElevated)
            .border(
                1.dp,
                if (isSelected) CopperLight else CharcoalBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = if (isSelected) TextWhite else TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = subtitle,
                color = if (isSelected) TextWhite.copy(alpha = 0.8f) else TextGrayLight,
                fontSize = 10.sp
            )
        }
    }
}
