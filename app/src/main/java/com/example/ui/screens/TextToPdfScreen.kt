package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarginOption
import com.example.model.PageSize
import com.example.model.PdfOrientation
import com.example.model.TextAlignOption
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

enum class TextEditorTab {
    EDIT,
    PAGE_SETTINGS,
    PREVIEW
}

@Composable
fun TextToPdfScreen(
    viewModel: AppViewModel,
    onBackClick: () -> Unit,
    onPdfCreatedSuccess: () -> Unit
) {
    val settings by viewModel.textToPdfSettings.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(TextEditorTab.EDIT) }

    Scaffold(
        topBar = {
            JKTopAppBar(
                title = "Text to PDF",
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
                if (isProcessing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = CopperLight, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Creating PDF...", color = TextWhite, fontSize = 14.sp)
                    }
                } else {
                    JKPrimaryButton(
                        text = "CREATE PDF",
                        icon = Icons.Default.PictureAsPdf,
                        onClick = {
                            if (settings.content.isBlank() && settings.title.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please enter some text to create a PDF.")
                                }
                            } else {
                                viewModel.generateTextPdf(
                                    onSuccess = { onPdfCreatedSuccess() },
                                    onError = { err ->
                                        scope.launch { snackbarHostState.showSnackbar(err) }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "text_to_pdf_create_button"
                    )
                }
            }
        },
        containerColor = CharcoalBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("text_to_pdf_screen")
        ) {
            // Tab Selector: Edit | Page Settings | Preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CharcoalCard)
                    .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                EditorTabPill(
                    title = "Text Editor",
                    icon = Icons.Default.Edit,
                    isSelected = activeTab == TextEditorTab.EDIT,
                    onClick = { activeTab = TextEditorTab.EDIT },
                    testTag = "tab_text_editor"
                )
                EditorTabPill(
                    title = "Page Setup",
                    icon = Icons.Default.Tune,
                    isSelected = activeTab == TextEditorTab.PAGE_SETTINGS,
                    onClick = { activeTab = TextEditorTab.PAGE_SETTINGS },
                    testTag = "tab_page_setup"
                )
                EditorTabPill(
                    title = "Preview",
                    icon = Icons.Default.Visibility,
                    isSelected = activeTab == TextEditorTab.PREVIEW,
                    onClick = { activeTab = TextEditorTab.PREVIEW },
                    testTag = "tab_preview"
                )
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (activeTab) {
                    TextEditorTab.EDIT -> {
                        TextEditorContent(
                            settings = settings,
                            onUpdate = { t, c, fs, b, it, al, col, ps, ls ->
                                viewModel.updateTextToPdfSettings(
                                    title = t,
                                    content = c,
                                    fontSizeSp = fs,
                                    isBold = b,
                                    isItalic = it,
                                    alignment = al,
                                    textColorHex = col,
                                    paragraphSpacingDp = ps,
                                    lineSpacingMultiplier = ls
                                )
                            }
                        )
                    }
                    TextEditorTab.PAGE_SETTINGS -> {
                        PageSettingsContent(
                            settings = settings,
                            onUpdate = { size, ori, mar, sHead, head, sFoot, foot, sNum, name ->
                                viewModel.updateTextToPdfSettings(
                                    pageSize = size,
                                    orientation = ori,
                                    marginOption = mar,
                                    showHeader = sHead,
                                    headerText = head,
                                    showFooter = sFoot,
                                    footerText = foot,
                                    showPageNumber = sNum,
                                    fileName = name
                                )
                            }
                        )
                    }
                    TextEditorTab.PREVIEW -> {
                        TextPdfPreviewContent(settings = settings)
                    }
                }
            }
        }
    }
}

@Composable
private fun TextEditorContent(
    settings: com.example.model.TextToPdfSettings,
    onUpdate: (
        title: String?,
        content: String?,
        fontSizeSp: Float?,
        isBold: Boolean?,
        isItalic: Boolean?,
        alignment: TextAlignOption?,
        textColorHex: String?,
        paragraphSpacingDp: Float?,
        lineSpacingMultiplier: Float?
    ) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Formatting Toolbar Card
        JKCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Row 1: Bold, Italic, Alignments, Font Size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Style Toggles
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ToolbarToggleButton(
                            icon = Icons.Default.FormatBold,
                            isActive = settings.isBold,
                            onClick = { onUpdate(null, null, null, !settings.isBold, null, null, null, null, null) },
                            testTag = "tool_bold"
                        )
                        ToolbarToggleButton(
                            icon = Icons.Default.FormatItalic,
                            isActive = settings.isItalic,
                            onClick = { onUpdate(null, null, null, null, !settings.isItalic, null, null, null, null) },
                            testTag = "tool_italic"
                        )
                    }

                    // Alignment Toggles
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ToolbarToggleButton(
                            icon = Icons.Default.FormatAlignLeft,
                            isActive = settings.alignment == TextAlignOption.LEFT,
                            onClick = { onUpdate(null, null, null, null, null, TextAlignOption.LEFT, null, null, null) },
                            testTag = "tool_align_left"
                        )
                        ToolbarToggleButton(
                            icon = Icons.Default.FormatAlignCenter,
                            isActive = settings.alignment == TextAlignOption.CENTER,
                            onClick = { onUpdate(null, null, null, null, null, TextAlignOption.CENTER, null, null, null) },
                            testTag = "tool_align_center"
                        )
                        ToolbarToggleButton(
                            icon = Icons.Default.FormatAlignRight,
                            isActive = settings.alignment == TextAlignOption.RIGHT,
                            onClick = { onUpdate(null, null, null, null, null, TextAlignOption.RIGHT, null, null, null) },
                            testTag = "tool_align_right"
                        )
                        ToolbarToggleButton(
                            icon = Icons.Default.FormatAlignJustify,
                            isActive = settings.alignment == TextAlignOption.JUSTIFY,
                            onClick = { onUpdate(null, null, null, null, null, TextAlignOption.JUSTIFY, null, null, null) },
                            testTag = "tool_align_justify"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Font Size & Color Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Font Size Buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "Size:", color = TextGrayLight, fontSize = 12.sp)
                        listOf(12f, 14f, 16f, 18f, 22f).forEach { size ->
                            val isSelected = settings.fontSizeSp == size
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) CopperPrimary else CharcoalCardElevated)
                                    .border(1.dp, if (isSelected) CopperLight else CharcoalBorder, RoundedCornerShape(6.dp))
                                    .clickable { onUpdate(null, null, size, null, null, null, null, null, null) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${size.toInt()}",
                                    color = if (isSelected) TextWhite else TextGrayLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Text Color Swatches
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "#1E1E24" to Color(0xFF1E1E24),
                            "#C87D55" to Color(0xFFC87D55),
                            "#1E3A8A" to Color(0xFF1E3A8A),
                            "#4B5563" to Color(0xFF4B5563)
                        ).forEach { (hex, clr) ->
                            val isSelected = settings.textColorHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(clr)
                                    .border(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) CopperLight else CharcoalBorder,
                                        CircleShape
                                    )
                                    .clickable { onUpdate(null, null, null, null, null, null, hex, null, null) }
                            )
                        }
                    }
                }
            }
        }

        // Title Input
        OutlinedTextField(
            value = settings.title,
            onValueChange = { onUpdate(it, null, null, null, null, null, null, null, null) },
            placeholder = { Text("Document Title (Optional)", color = TextGrayMuted, fontSize = 15.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("text_pdf_title_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CopperLight,
                unfocusedBorderColor = CharcoalBorder,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedContainerColor = CharcoalCard,
                unfocusedContainerColor = CharcoalCard
            )
        )

        // Large Multiline Content Editor
        OutlinedTextField(
            value = settings.content,
            onValueChange = { onUpdate(null, it, null, null, null, null, null, null, null) },
            placeholder = { Text("Enter or paste your text here...\n\nText will be automatically formatted and paginated into a real PDF document.", color = TextGrayMuted, fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .testTag("text_pdf_content_input"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CopperLight,
                unfocusedBorderColor = CharcoalBorder,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedContainerColor = CharcoalCard,
                unfocusedContainerColor = CharcoalCard
            )
        )
    }
}

@Composable
private fun PageSettingsContent(
    settings: com.example.model.TextToPdfSettings,
    onUpdate: (
        pageSize: PageSize?,
        orientation: PdfOrientation?,
        marginOption: MarginOption?,
        showHeader: Boolean?,
        headerText: String?,
        showFooter: Boolean?,
        footerText: String?,
        showPageNumber: Boolean?,
        fileName: String?
    ) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // PDF File Name
        JKCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PDF FILENAME",
                    color = CopperLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = settings.fileName,
                    onValueChange = { onUpdate(null, null, null, null, null, null, null, null, it) },
                    placeholder = { Text("Text_Document_...pdf", color = TextGrayMuted, fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("text_pdf_filename_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CopperLight,
                        unfocusedBorderColor = CharcoalBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = CharcoalCardElevated,
                        unfocusedContainerColor = CharcoalCardElevated
                    )
                )
            }
        }

        // Paper Size: A4, A5, Letter, Legal
        JKCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PAPER SIZE",
                    color = CopperLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(PageSize.A4, PageSize.A5, PageSize.LETTER, PageSize.LEGAL).forEach { sz ->
                        val isSelected = settings.pageSize == sz
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CopperPrimary else CharcoalCardElevated)
                                .border(1.dp, if (isSelected) CopperLight else CharcoalBorder, RoundedCornerShape(10.dp))
                                .clickable { onUpdate(sz, null, null, null, null, null, null, null, null) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sz.displayName,
                                color = if (isSelected) TextWhite else TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Orientation: Portrait / Landscape
        JKCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ORIENTATION",
                    color = CopperLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(PdfOrientation.PORTRAIT, PdfOrientation.LANDSCAPE).forEach { ori ->
                        val isSelected = settings.orientation == ori
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CopperPrimary else CharcoalCardElevated)
                                .border(1.dp, if (isSelected) CopperLight else CharcoalBorder, RoundedCornerShape(10.dp))
                                .clickable { onUpdate(null, ori, null, null, null, null, null, null, null) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ori.displayName,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Margins: Normal, Narrow, Wide
        JKCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "MARGINS",
                    color = CopperLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MarginOption.values().forEach { mar ->
                        val isSelected = settings.marginOption == mar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CopperPrimary else CharcoalCardElevated)
                                .border(1.dp, if (isSelected) CopperLight else CharcoalBorder, RoundedCornerShape(10.dp))
                                .clickable { onUpdate(null, null, mar, null, null, null, null, null, null) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mar.displayName,
                                color = TextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Header, Footer, and Page Number Options
        JKCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "HEADER & FOOTER",
                    color = CopperLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Header toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Include Header", color = TextWhite, fontSize = 13.sp)
                    Switch(
                        checked = settings.showHeader,
                        onCheckedChange = { onUpdate(null, null, null, it, null, null, null, null, null) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextWhite,
                            checkedTrackColor = CopperPrimary,
                            uncheckedThumbColor = TextGrayMuted,
                            uncheckedTrackColor = CharcoalCardElevated
                        )
                    )
                }
                if (settings.showHeader) {
                    OutlinedTextField(
                        value = settings.headerText,
                        onValueChange = { onUpdate(null, null, null, null, it, null, null, null, null) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CopperLight,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Include Footer", color = TextWhite, fontSize = 13.sp)
                    Switch(
                        checked = settings.showFooter,
                        onCheckedChange = { onUpdate(null, null, null, null, null, it, null, null, null) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextWhite,
                            checkedTrackColor = CopperPrimary,
                            uncheckedThumbColor = TextGrayMuted,
                            uncheckedTrackColor = CharcoalCardElevated
                        )
                    )
                }
                if (settings.showFooter) {
                    OutlinedTextField(
                        value = settings.footerText,
                        onValueChange = { onUpdate(null, null, null, null, null, null, it, null, null) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CopperLight,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Page Number toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Show Page Numbers", color = TextWhite, fontSize = 13.sp)
                    Switch(
                        checked = settings.showPageNumber,
                        onCheckedChange = { onUpdate(null, null, null, null, null, null, null, it, null) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextWhite,
                            checkedTrackColor = CopperPrimary,
                            uncheckedThumbColor = TextGrayMuted,
                            uncheckedTrackColor = CharcoalCardElevated
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TextPdfPreviewContent(settings: com.example.model.TextToPdfSettings) {
    val scrollState = rememberScrollState()

    // Estimate pages based on word/character count
    val charsPerPage = 1200
    val totalEstimatedPages = ((settings.content.length / charsPerPage) + 1).coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "PDF PREVIEW ($totalEstimatedPages Page${if (totalEstimatedPages > 1) "s" else ""})",
            color = CopperLight,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        for (p in 1..totalEstimatedPages) {
            val startIdx = ((p - 1) * charsPerPage).coerceAtMost(settings.content.length)
            val endIdx = (p * charsPerPage).coerceAtMost(settings.content.length)
            val pageSlice = if (settings.content.isNotBlank()) settings.content.substring(startIdx, endIdx) else "No content entered yet."

            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header
                    if (settings.showHeader && settings.headerText.isNotBlank()) {
                        Column {
                            Text(
                                text = settings.headerText,
                                color = Color.Gray,
                                fontSize = 9.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                        }
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Body
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp)
                    ) {
                        if (p == 1 && settings.title.isNotBlank()) {
                            Text(
                                text = settings.title,
                                color = Color.Black,
                                fontSize = (settings.fontSizeSp * 1.3f).sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = when (settings.alignment) {
                                    TextAlignOption.CENTER -> TextAlign.Center
                                    TextAlignOption.RIGHT -> TextAlign.Right
                                    else -> TextAlign.Left
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Text(
                            text = pageSlice,
                            color = Color.Black,
                            fontSize = (settings.fontSizeSp * 0.85f).sp,
                            fontWeight = if (settings.isBold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (settings.isItalic) FontStyle.Italic else FontStyle.Normal,
                            textAlign = when (settings.alignment) {
                                TextAlignOption.CENTER -> TextAlign.Center
                                TextAlignOption.RIGHT -> TextAlign.Right
                                TextAlignOption.JUSTIFY -> TextAlign.Justify
                                else -> TextAlign.Left
                            },
                            lineHeight = (settings.fontSizeSp * 1.2f).sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Footer
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (settings.showFooter) settings.footerText else "",
                                color = Color.Gray,
                                fontSize = 9.sp
                            )
                            Text(
                                text = if (settings.showPageNumber) "Page $p of $totalEstimatedPages" else "",
                                color = Color.Gray,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolbarToggleButton(
    icon: ImageVector,
    isActive: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) CopperPrimary else CharcoalCardElevated)
            .border(1.dp, if (isActive) CopperLight else CharcoalBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) TextWhite else TextGrayLight,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun EditorTabPill(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) CopperPrimary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) TextWhite else TextGrayLight,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = if (isSelected) TextWhite else TextGrayLight,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }
}
