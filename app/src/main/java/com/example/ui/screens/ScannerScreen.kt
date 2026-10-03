package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.model.DocumentCorners
import com.example.model.FilterType
import com.example.model.PointF2D
import com.example.model.ScanMode
import com.example.service.ImageProcessingUtils
import com.example.ui.components.JKPrimaryButton
import com.example.ui.components.JKSecondaryButton
import com.example.ui.components.JKTopAppBar
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CopperDark
import com.example.ui.theme.CopperGradient
import com.example.ui.theme.CopperLight
import com.example.ui.theme.CopperPrimary
import com.example.ui.theme.CopperSecondary
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.hypot

enum class FlashMode {
    AUTO, ON, OFF
}

enum class ScannerViewStep {
    CAMERA,
    DOCUMENT_CROP_AND_ENHANCE
}

data class ScanHistorySnapshot(
    val corners: DocumentCorners,
    val rotation: Int,
    val filter: FilterType,
    val isPerspectiveApplied: Boolean
)

@Composable
fun ScannerScreen(
    viewModel: AppViewModel,
    onBackClick: () -> Unit,
    onNavigateToSelectedImages: () -> Unit
) {
    val selectedImages by viewModel.selectedImages.collectAsState()
    val scanMode by viewModel.scanMode.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var viewStep by remember { mutableStateOf(ScannerViewStep.CAMERA) }
    var flashMode by remember { mutableStateOf(FlashMode.AUTO) }

    // Active capture tracking
    var capturedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var currentWorkingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessingStep by remember { mutableStateOf(false) }

    // Document processing states for active scanned page
    var corners by remember { mutableStateOf(DocumentCorners()) }
    var rotationDegrees by remember { mutableIntStateOf(0) }
    var activeFilter by remember { mutableStateOf(FilterType.ORIGINAL) }
    var isPerspectiveApplied by remember { mutableStateOf(false) }

    // Undo / Redo stacks
    val undoStack = remember { mutableStateListOf<ScanHistorySnapshot>() }
    val redoStack = remember { mutableStateListOf<ScanHistorySnapshot>() }

    fun pushHistorySnapshot() {
        undoStack.add(
            ScanHistorySnapshot(
                corners = corners,
                rotation = rotationDegrees,
                filter = activeFilter,
                isPerspectiveApplied = isPerspectiveApplied
            )
        )
        redoStack.clear()
    }

    fun applyUndo() {
        if (undoStack.isNotEmpty()) {
            val currentState = ScanHistorySnapshot(
                corners = corners,
                rotation = rotationDegrees,
                filter = activeFilter,
                isPerspectiveApplied = isPerspectiveApplied
            )
            redoStack.add(currentState)
            val previous = undoStack.removeAt(undoStack.lastIndex)
            corners = previous.corners
            rotationDegrees = previous.rotation
            activeFilter = previous.filter
            isPerspectiveApplied = previous.isPerspectiveApplied
        }
    }

    fun applyRedo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(
                ScanHistorySnapshot(
                    corners = corners,
                    rotation = rotationDegrees,
                    filter = activeFilter,
                    isPerspectiveApplied = isPerspectiveApplied
                )
            )
            corners = next.corners
            rotationDegrees = next.rotation
            activeFilter = next.filter
            isPerspectiveApplied = next.isPerspectiveApplied
        }
    }

    // Camera permission
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    fun onDocumentCaptured(uri: Uri?) {
        capturedPhotoUri = uri
        isProcessingStep = true
        viewStep = ScannerViewStep.DOCUMENT_CROP_AND_ENHANCE
        undoStack.clear()
        redoStack.clear()
        rotationDegrees = 0
        activeFilter = FilterType.ORIGINAL
        isPerspectiveApplied = false

        scope.launch(Dispatchers.Default) {
            val bmp = if (uri != null) {
                ImageProcessingUtils.loadSampledBitmapFromUri(context, uri, maxDimension = 1800)
            } else {
                ImageProcessingUtils.createDocumentBitmap(selectedImages.size + 1)
            }

            val detected = if (bmp != null) {
                ImageProcessingUtils.detectDocumentCorners(bmp)
            } else {
                DocumentCorners()
            }

            withContext(Dispatchers.Main) {
                currentWorkingBitmap = bmp
                corners = detected
                isProcessingStep = false
                pushHistorySnapshot()
            }
        }
    }

    // Camera capture launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && capturedPhotoUri != null) {
            onDocumentCaptured(capturedPhotoUri)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission is required to scan documents.")
            }
        }
    }

    fun launchCamera() {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        try {
            val photoFile = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, photoFile)
            capturedPhotoUri = uri
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback for emulator / non-camera hardware: create high-res sample document
            onDocumentCaptured(null)
        }
    }

    BackHandler {
        if (viewStep == ScannerViewStep.DOCUMENT_CROP_AND_ENHANCE) {
            viewStep = ScannerViewStep.CAMERA
        } else {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            JKTopAppBar(
                title = if (viewStep == ScannerViewStep.CAMERA) "Document Scanner" else "Adjust Document",
                onBackClick = {
                    if (viewStep == ScannerViewStep.DOCUMENT_CROP_AND_ENHANCE) {
                        viewStep = ScannerViewStep.CAMERA
                    } else {
                        onBackClick()
                    }
                },
                actions = {
                    if (viewStep == ScannerViewStep.DOCUMENT_CROP_AND_ENHANCE) {
                        // Undo & Redo Actions
                        IconButton(
                            onClick = { applyUndo() },
                            enabled = undoStack.isNotEmpty(),
                            modifier = Modifier.testTag("scanner_undo_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo",
                                tint = if (undoStack.isNotEmpty()) CopperLight else TextGrayMuted
                            )
                        }

                        IconButton(
                            onClick = { applyRedo() },
                            enabled = redoStack.isNotEmpty(),
                            modifier = Modifier.testTag("scanner_redo_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo",
                                tint = if (redoStack.isNotEmpty()) CopperLight else TextGrayMuted
                            )
                        }

                        // Reset Action
                        IconButton(
                            onClick = {
                                pushHistorySnapshot()
                                corners = DocumentCorners()
                                rotationDegrees = 0
                                activeFilter = FilterType.ORIGINAL
                                isPerspectiveApplied = false
                                scope.launch { snackbarHostState.showSnackbar("Adjustments reset") }
                            },
                            modifier = Modifier.testTag("scanner_reset_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset Crop",
                                tint = CopperLight
                            )
                        }
                    } else if (selectedImages.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(CopperPrimary.copy(alpha = 0.2f))
                                .border(1.dp, CopperPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .clickable(onClick = onNavigateToSelectedImages)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("scanner_view_selected_badge")
                        ) {
                            Text(
                                text = "${selectedImages.size} Page${if (selectedImages.size > 1) "s" else ""}",
                                color = CopperLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Proceed",
                                tint = CopperLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CharcoalBlack
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .testTag("scanner_screen")
        ) {
            if (viewStep == ScannerViewStep.CAMERA) {
                // ==================== 1. CAMERA SCANNER VIEW ====================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mode Selector: Single Scan / Batch Scan
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(CharcoalCard)
                            .border(1.dp, CharcoalBorder, RoundedCornerShape(24.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ScanModePill(
                            title = "Single Scan",
                            isSelected = scanMode == ScanMode.SINGLE,
                            testTag = "scan_mode_single",
                            onClick = { viewModel.setScanMode(ScanMode.SINGLE) }
                        )
                        ScanModePill(
                            title = "Batch Scan",
                            isSelected = scanMode == ScanMode.BATCH,
                            testTag = "scan_mode_batch",
                            onClick = { viewModel.setScanMode(ScanMode.BATCH) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Camera Viewfinder Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(CharcoalCard)
                            .border(1.5.dp, CharcoalBorder, RoundedCornerShape(20.dp))
                            .testTag("camera_preview_area"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!hasCameraPermission) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = CopperLight,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Camera Permission Required",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Please grant camera permission to scan documents directly.",
                                    color = TextGrayLight,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                JKSecondaryButton(
                                    text = "Grant Permission",
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                                )
                            }
                        } else {
                            // Viewfinder alignment frame
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.85f)
                                    .border(
                                        BorderStroke(
                                            2.dp,
                                            Brush.linearGradient(listOf(CopperLight, CopperDark))
                                        ),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CropFree,
                                        contentDescription = null,
                                        tint = CopperLight.copy(alpha = 0.7f),
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Position document inside frame",
                                        color = TextWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (scanMode == ScanMode.BATCH) "Batch Mode: Continuous capture" else "Single Mode: Scan & Review",
                                        color = CopperLight,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Flash & Camera Mode Utility bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Flash toggle
                                IconButton(
                                    onClick = {
                                        flashMode = when (flashMode) {
                                            FlashMode.AUTO -> FlashMode.ON
                                            FlashMode.ON -> FlashMode.OFF
                                            FlashMode.OFF -> FlashMode.AUTO
                                        }
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Flash mode: ${flashMode.name}")
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(CharcoalBlack.copy(alpha = 0.75f))
                                        .border(1.dp, CharcoalBorder, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = when (flashMode) {
                                            FlashMode.AUTO -> Icons.Default.FlashAuto
                                            FlashMode.ON -> Icons.Default.FlashOn
                                            FlashMode.OFF -> Icons.Default.FlashOff
                                        },
                                        contentDescription = "Flash ${flashMode.name}",
                                        tint = CopperLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                if (selectedImages.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(CharcoalBlack.copy(alpha = 0.8f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${selectedImages.size} in Session",
                                            color = CopperLight,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Camera Action Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sample Scan fallback / Test page
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onDocumentCaptured(null) }
                                .padding(8.dp)
                                .testTag("scanner_sample_page_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(CharcoalCard)
                                    .border(1.dp, CharcoalBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Sample Document",
                                    tint = CopperLight,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Sample", color = TextGrayLight, fontSize = 11.sp)
                        }

                        // Large Copper Shutter Capture Button
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .border(3.dp, CopperLight, CircleShape)
                                .padding(6.dp)
                                .clip(CircleShape)
                                .background(CopperGradient)
                                .clickable { launchCamera() }
                                .testTag("scanner_capture_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Capture Document",
                                tint = TextWhite,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Finished Review button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    if (selectedImages.isNotEmpty()) {
                                        onNavigateToSelectedImages()
                                    } else {
                                        scope.launch { snackbarHostState.showSnackbar("Scan at least one page first.") }
                                    }
                                }
                                .padding(8.dp)
                                .testTag("scanner_review_pages_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(CharcoalCard)
                                    .border(1.dp, CharcoalBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Finish Scanning",
                                    tint = if (selectedImages.isNotEmpty()) CopperLight else TextGrayMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Organizer", color = TextGrayLight, fontSize = 11.sp)
                        }
                    }

                    if (selectedImages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        JKPrimaryButton(
                            text = "Finish & Organize ${selectedImages.size} Page${if (selectedImages.size > 1) "s" else ""}",
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            onClick = onNavigateToSelectedImages,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "scanner_continue_button"
                        )
                    }
                }
            } else {
                // ==================== 2. SMART DOCUMENT CROP & ENHANCE VIEW ====================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isProcessingStep) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = CopperLight)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Detecting document edges...",
                                    color = TextWhite,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        // Interactive Document Editor Canvas with 4 Draggable Handles
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(CharcoalCard)
                                .border(1.dp, CharcoalBorder, RoundedCornerShape(18.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .rotate(rotationDegrees.toFloat())
                            ) {
                                val canvasWidth = constraints.maxWidth.toFloat()
                                val canvasHeight = constraints.maxHeight.toFloat()

                                // Render document preview bitmap
                                val composeFilter = remember(activeFilter) {
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

                                if (capturedPhotoUri != null) {
                                    AsyncImage(
                                        model = capturedPhotoUri,
                                        contentDescription = "Scanned Page",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit,
                                        colorFilter = composeFilter
                                    )
                                } else {
                                    // Placeholder document layout
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFFFBFBFB))
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "JK CREATIONS DOCUMENT SCAN",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Box(modifier = Modifier.fillMaxWidth(0.9f).height(6.dp).background(Color.Gray))
                                            Box(modifier = Modifier.fillMaxWidth(0.7f).height(6.dp).background(Color.Gray))
                                            Box(modifier = Modifier.fillMaxWidth(0.85f).height(6.dp).background(Color.Gray))
                                            Box(modifier = Modifier.fillMaxWidth(0.6f).height(6.dp).background(Color.Gray))
                                        }
                                        Text(
                                            text = "Edge Detection Active",
                                            color = Color.DarkGray,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                // Interactive Four Corner Overlay & Polygon Highlight
                                val tlOffset = Offset(corners.topLeft.x * canvasWidth, corners.topLeft.y * canvasHeight)
                                val trOffset = Offset(corners.topRight.x * canvasWidth, corners.topRight.y * canvasHeight)
                                val brOffset = Offset(corners.bottomRight.x * canvasWidth, corners.bottomRight.y * canvasHeight)
                                val blOffset = Offset(corners.bottomLeft.x * canvasWidth, corners.bottomLeft.y * canvasHeight)

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    // Draw highlighted quadrilateral
                                    val polyPath = Path().apply {
                                        moveTo(tlOffset.x, tlOffset.y)
                                        lineTo(trOffset.x, trOffset.y)
                                        lineTo(brOffset.x, brOffset.y)
                                        lineTo(blOffset.x, blOffset.y)
                                        close()
                                    }
                                    drawPath(
                                        path = polyPath,
                                        color = Color(0x33E09F74) // Subtle copper highlight
                                    )
                                    drawPath(
                                        path = polyPath,
                                        color = Color(0xFFE09F74),
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )

                                    // Draw corner handles
                                    val handleRadius = 12.dp.toPx()
                                    listOf(tlOffset, trOffset, brOffset, blOffset).forEach { pt ->
                                        drawCircle(Color(0xFFE09F74), handleRadius, pt)
                                        drawCircle(Color.White, handleRadius * 0.5f, pt)
                                    }
                                }

                                // Draggable Handle Touch Areas (48dp touch targets)
                                CornerHandleTouchTarget(
                                    normalizedPos = corners.topLeft,
                                    canvasWidth = canvasWidth,
                                    canvasHeight = canvasHeight,
                                    onDrag = { dx, dy ->
                                        corners = corners.copy(
                                            topLeft = PointF2D(
                                                (corners.topLeft.x + (dx / canvasWidth)).coerceIn(0.01f, corners.topRight.x - 0.1f),
                                                (corners.topLeft.y + (dy / canvasHeight)).coerceIn(0.01f, corners.bottomLeft.y - 0.1f)
                                            )
                                        )
                                    },
                                    onDragEnd = { pushHistorySnapshot() }
                                )

                                CornerHandleTouchTarget(
                                    normalizedPos = corners.topRight,
                                    canvasWidth = canvasWidth,
                                    canvasHeight = canvasHeight,
                                    onDrag = { dx, dy ->
                                        corners = corners.copy(
                                            topRight = PointF2D(
                                                (corners.topRight.x + (dx / canvasWidth)).coerceIn(corners.topLeft.x + 0.1f, 0.99f),
                                                (corners.topRight.y + (dy / canvasHeight)).coerceIn(0.01f, corners.bottomRight.y - 0.1f)
                                            )
                                        )
                                    },
                                    onDragEnd = { pushHistorySnapshot() }
                                )

                                CornerHandleTouchTarget(
                                    normalizedPos = corners.bottomRight,
                                    canvasWidth = canvasWidth,
                                    canvasHeight = canvasHeight,
                                    onDrag = { dx, dy ->
                                        corners = corners.copy(
                                            bottomRight = PointF2D(
                                                (corners.bottomRight.x + (dx / canvasWidth)).coerceIn(corners.bottomLeft.x + 0.1f, 0.99f),
                                                (corners.bottomRight.y + (dy / canvasHeight)).coerceIn(corners.topRight.y + 0.1f, 0.99f)
                                            )
                                        )
                                    },
                                    onDragEnd = { pushHistorySnapshot() }
                                )

                                CornerHandleTouchTarget(
                                    normalizedPos = corners.bottomLeft,
                                    canvasWidth = canvasWidth,
                                    canvasHeight = canvasHeight,
                                    onDrag = { dx, dy ->
                                        corners = corners.copy(
                                            bottomLeft = PointF2D(
                                                (corners.bottomLeft.x + (dx / canvasWidth)).coerceIn(0.01f, corners.bottomRight.x - 0.1f),
                                                (corners.bottomLeft.y + (dy / canvasHeight)).coerceIn(corners.topLeft.y + 0.1f, 0.99f)
                                            )
                                        )
                                    },
                                    onDragEnd = { pushHistorySnapshot() }
                                )
                            }

                            // Status Tag
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CharcoalBlack.copy(alpha = 0.85f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isPerspectiveApplied) "Perspective Corrected" else "Drag 4 Corners to Adjust Crop",
                                    color = CopperLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Editing Toolbar Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CharcoalCard)
                            .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        // Quick Action Buttons: Auto Crop / Perspective, Rotate 90, Reset
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ScannerToolItem(
                                icon = Icons.Default.Transform,
                                label = if (isPerspectiveApplied) "Warped" else "Perspective",
                                isActive = isPerspectiveApplied,
                                testTag = "tool_perspective_correct",
                                onClick = {
                                    pushHistorySnapshot()
                                    isPerspectiveApplied = !isPerspectiveApplied
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (isPerspectiveApplied) "Four-point perspective transform applied" else "Original perspective restored"
                                        )
                                    }
                                }
                            )

                            ScannerToolItem(
                                icon = Icons.Default.RotateRight,
                                label = "Rotate 90°",
                                isActive = false,
                                testTag = "tool_rotate",
                                onClick = {
                                    pushHistorySnapshot()
                                    rotationDegrees = (rotationDegrees + 90) % 360
                                }
                            )

                            ScannerToolItem(
                                icon = Icons.Default.AutoFixHigh,
                                label = "Auto Enhance",
                                isActive = activeFilter == FilterType.AUTO,
                                testTag = "tool_auto_enhance",
                                onClick = {
                                    pushHistorySnapshot()
                                    activeFilter = FilterType.AUTO
                                    scope.launch { snackbarHostState.showSnackbar("Auto enhancement applied") }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Enhancement Filters Row: Original, Auto, Document, Grayscale, Black & White
                        Text(
                            text = "DOCUMENT ENHANCEMENT",
                            color = TextGrayMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

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
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) CopperPrimary else CharcoalCardElevated)
                                        .border(
                                            1.dp,
                                            if (isSelected) CopperLight else CharcoalBorder,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            pushHistorySnapshot()
                                            activeFilter = filter
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("scanner_filter_${filter.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = filter.displayName,
                                        color = if (isSelected) TextWhite else TextGrayLight,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom Confirm / Add to Batch Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        JKSecondaryButton(
                            text = "Retake",
                            icon = Icons.Default.Refresh,
                            onClick = {
                                viewStep = ScannerViewStep.CAMERA
                                launchCamera()
                            },
                            modifier = Modifier.weight(0.38f),
                            testTag = "scanner_editor_retake_button"
                        )

                        JKPrimaryButton(
                            text = if (scanMode == ScanMode.BATCH) "Add to Batch (Page ${selectedImages.size + 1})" else "Keep & Continue",
                            icon = Icons.Default.Check,
                            onClick = {
                                scope.launch(Dispatchers.Default) {
                                    // Apply perspective transform & filter to working bitmap if needed
                                    var finalBitmap = currentWorkingBitmap
                                    if (finalBitmap != null) {
                                        if (isPerspectiveApplied) {
                                            finalBitmap = ImageProcessingUtils.applyPerspectiveTransform(finalBitmap, corners)
                                        }
                                        if (rotationDegrees != 0) {
                                            finalBitmap = ImageProcessingUtils.rotateBitmap(finalBitmap, rotationDegrees)
                                        }
                                        if (activeFilter != FilterType.ORIGINAL) {
                                            finalBitmap = ImageProcessingUtils.applyEnhancement(finalBitmap, activeFilter)
                                        }
                                    }

                                    val savedPath = if (finalBitmap != null) {
                                        ImageProcessingUtils.saveBitmapToTempFile(context, finalBitmap, prefix = "scanned_doc_")
                                    } else {
                                        capturedPhotoUri?.toString()
                                    }

                                    withContext(Dispatchers.Main) {
                                        viewModel.addCapturedImage(savedPath)
                                        if (scanMode == ScanMode.BATCH) {
                                            viewStep = ScannerViewStep.CAMERA
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Page ${selectedImages.size + 1} added to batch!")
                                            }
                                        } else {
                                            onNavigateToSelectedImages()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(0.62f),
                            testTag = "scanner_editor_accept_button"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CornerHandleTouchTarget(
    normalizedPos: PointF2D,
    canvasWidth: Float,
    canvasHeight: Float,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val xDp = with(density) { (normalizedPos.x * canvasWidth - 24.dp.toPx()).toDp() }
    val yDp = with(density) { (normalizedPos.y * canvasHeight - 24.dp.toPx()).toDp() }

    Box(
        modifier = Modifier
            .offset(x = xDp, y = yDp)
            .size(48.dp) // Accessibility touch target
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = onDragEnd,
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                )
            }
    )
}

@Composable
private fun ScannerToolItem(
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
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (isActive) CopperPrimary else CharcoalCardElevated)
                .border(1.dp, if (isActive) CopperLight else CharcoalBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) TextWhite else CopperLight,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isActive) CopperLight else TextGrayLight,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ScanModePill(
    title: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) CopperPrimary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) TextWhite else TextGrayLight,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}
