package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun DigitalSignatureScreen(
    viewModel: AppViewModel,
    onBackClick: () -> Unit
) {
    val lines = remember { mutableStateListOf<List<Offset>>() }
    val currentLine = remember { mutableStateListOf<Offset>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            JKTopAppBar(
                title = "Digital Signature",
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CharcoalBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .navigationBarsPadding()
                .testTag("digital_signature_screen"),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Sign Your Document",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Use your finger or stylus to draw your official signature.",
                    color = TextGrayLight,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Clean Signature Canvas Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(CharcoalCardElevated)
                        .border(1.5.dp, CopperPrimary.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentLine.clear()
                                    currentLine.add(offset)
                                },
                                onDrag = { change, _ ->
                                    currentLine.add(change.position)
                                },
                                onDragEnd = {
                                    lines.add(currentLine.toList())
                                    currentLine.clear()
                                }
                            )
                        }
                        .testTag("signature_canvas_area")
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Guideline for signature
                        val baselineY = size.height * 0.75f
                        drawLine(
                            color = CharcoalBorder,
                            start = Offset(40f, baselineY),
                            end = Offset(size.width - 40f, baselineY),
                            strokeWidth = 2f
                        )

                        // Render finished strokes
                        lines.forEach { strokePoints ->
                            if (strokePoints.size > 1) {
                                val path = Path().apply {
                                    moveTo(strokePoints.first().x, strokePoints.first().y)
                                    for (i in 1 until strokePoints.size) {
                                        lineTo(strokePoints[i].x, strokePoints[i].y)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = CopperLight,
                                    style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }

                        // Render current stroke
                        if (currentLine.size > 1) {
                            val path = Path().apply {
                                moveTo(currentLine.first().x, currentLine.first().y)
                                for (i in 1 until currentLine.size) {
                                    lineTo(currentLine[i].x, currentLine[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = CopperLight,
                                style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    // Empty prompt if no strokes
                    if (lines.isEmpty() && currentLine.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Draw,
                                contentDescription = null,
                                tint = CopperLight.copy(alpha = 0.4f),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sign here",
                                color = TextGrayMuted,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // "X" indicator on signature line
                    Text(
                        text = "✕",
                        color = CopperDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 20.dp, bottom = 66.dp)
                    )
                }
            }

            // Action Buttons: Clear, Save Signature, Cancel
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Save Signature Button
                JKPrimaryButton(
                    text = "Save Signature",
                    icon = Icons.Default.Save,
                    onClick = {
                        if (lines.isNotEmpty()) {
                            viewModel.saveSignature()
                            scope.launch {
                                snackbarHostState.showSnackbar("Signature saved successfully!")
                            }
                            onBackClick()
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please draw a signature first")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "save_signature_button"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Clear Button
                    JKSecondaryButton(
                        text = "Clear",
                        icon = Icons.Default.Clear,
                        onClick = {
                            lines.clear()
                            currentLine.clear()
                            viewModel.clearSignature()
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "clear_signature_button"
                    )

                    // Cancel Button
                    JKSecondaryButton(
                        text = "Cancel",
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f),
                        testTag = "cancel_signature_button"
                    )
                }
            }
        }
    }
}
