package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.JKPrimaryButton
import com.example.ui.components.JKSecondaryButton
import com.example.ui.theme.BackgroundRadialGradient
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CopperDark
import com.example.ui.theme.CopperLight
import com.example.ui.theme.CopperPrimary
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.AppViewModel

@Composable
fun PdfProcessingScreen(
    viewModel: AppViewModel,
    onProcessingFinished: () -> Unit,
    onBackToSettings: () -> Unit
) {
    val progress by viewModel.processingProgress.collectAsState()
    val statusText by viewModel.processingStatusText.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val errorMsg by viewModel.pdfGenerationError.collectAsState()

    var started by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!started) {
            started = true
            viewModel.startRealPdfGeneration(
                onSuccess = {
                    onProcessingFinished()
                },
                onError = { /* Handled in UI through pdfGenerationError state */ }
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundRadialGradient)
            .padding(32.dp)
            .testTag("pdf_processing_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (errorMsg != null) {
                // Error state
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(CharcoalCard)
                        .border(2.dp, Color(0xFFF87171), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Unable to create PDF",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = errorMsg ?: "Please try again.",
                    color = TextGrayLight,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                JKPrimaryButton(
                    text = "Return to Settings",
                    onClick = onBackToSettings,
                    modifier = Modifier.fillMaxWidth(0.85f),
                    testTag = "processing_error_back_button"
                )
            } else {
                // Real Processing graphic
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(CharcoalCard)
                        .border(
                            2.dp,
                            Brush.sweepGradient(
                                listOf(CopperLight, CopperDark, CharcoalBorder, CopperLight)
                            ),
                            CircleShape
                        )
                        .rotate(pulseAngle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = CopperLight,
                        modifier = Modifier
                            .size(48.dp)
                            .rotate(-pulseAngle)
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Main Title
                Text(
                    text = "Creating PDF...",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Real status text from generator service
                Text(
                    text = statusText,
                    color = CopperLight,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Linear Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CharcoalCard)
                        .border(1.dp, CharcoalBorder, RoundedCornerShape(6.dp))
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = CopperPrimary,
                        trackColor = CharcoalCard,
                        strokeCap = StrokeCap.Round
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = TextGrayLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Compiling high-resolution vectorized PDF with JK Creations engine.",
                    color = TextGrayMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                if (!isProcessing && progress >= 1.0f) {
                    Spacer(modifier = Modifier.height(24.dp))
                    JKPrimaryButton(
                        text = "View PDF Document",
                        onClick = onProcessingFinished,
                        modifier = Modifier.fillMaxWidth(0.85f),
                        testTag = "processing_done_button"
                    )
                }
            }
        }
    }
}
