package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Metallic Copper Accent Palette
val CopperPrimary = Color(0xFFC87D55)
val CopperSecondary = Color(0xFFE09F74)
val CopperDark = Color(0xFF934F2C)
val CopperLight = Color(0xFFF0BD9B)
val CopperContainer = Color(0xFF332018)
val OnCopperContainer = Color(0xFFFFDBCF)

// Dark Charcoal / Black Backgrounds
val CharcoalBlack = Color(0xFF0E0E11)
val CharcoalSurface = Color(0xFF16161B)
val CharcoalCard = Color(0xFF1E1E26)
val CharcoalCardElevated = Color(0xFF262631)
val CharcoalBorder = Color(0xFF2E2E3B)
val CharcoalBorderHighlight = Color(0xFF4A4240)

// Text Colors
val TextWhite = Color(0xFFFFFFFF)
val TextGrayLight = Color(0xFFB0B0BD)
val TextGrayMuted = Color(0xFF767686)

// Semantic colors
val SuccessGreen = Color(0xFF4ADE80)
val ErrorRed = Color(0xFFF87171)

// Gradients
val CopperGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFEAA77F),
        Color(0xFFC87D55),
        Color(0xFFA35730)
    )
)

val CopperSubtleGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x33C87D55),
        Color(0x11C87D55)
    )
)

val DarkSurfaceGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF1E1E26),
        Color(0xFF15151B)
    )
)

val BackgroundRadialGradient = Brush.radialGradient(
    colors = listOf(
        Color(0xFF1E1C22),
        Color(0xFF0E0E11)
    )
)
