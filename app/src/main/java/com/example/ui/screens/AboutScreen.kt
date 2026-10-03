package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.JKCard
import com.example.ui.components.JKTopAppBar
import com.example.ui.components.JKWingsBadge
import com.example.ui.theme.BackgroundRadialGradient
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CopperDark
import com.example.ui.theme.CopperLight
import com.example.ui.theme.CopperPrimary
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextWhite

@Composable
fun AboutScreen(
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            JKTopAppBar(
                title = "About JK Creations",
                onBackClick = onBackClick
            )
        },
        containerColor = CharcoalBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundRadialGradient)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .navigationBarsPadding()
                .testTag("about_screen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Emblem
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(CharcoalCard)
                        .border(
                            1.5.dp,
                            Brush.linearGradient(listOf(CopperLight, CopperDark)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🪶",
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Brand Name
                Text(
                    text = "JK Creations",
                    color = CopperLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Main App Title
                Text(
                    text = "Image to PDF Converter with Wings",
                    color = TextWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Version Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CharcoalCard)
                        .border(1.dp, CharcoalBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Version 1.0.0",
                        color = TextGrayLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Description Card
                JKCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "A simple and professional image-to-PDF application designed for fast document scanning, image enhancement and PDF creation.",
                            color = TextWhite,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Feature Highlights
                JKCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        FeatureRow(
                            icon = Icons.Default.CameraAlt,
                            title = "High-Quality Document Scanning",
                            subtitle = "Clean single and batch capture workflows"
                        )
                        FeatureRow(
                            icon = Icons.Default.AutoAwesome,
                            title = "Smart Image Enhancements",
                            subtitle = "Document filters, cropping, and rotation"
                        )
                        FeatureRow(
                            icon = Icons.Default.PictureAsPdf,
                            title = "Custom PDF Formats",
                            subtitle = "A4, Letter, Legal with custom watermark"
                        )
                        FeatureRow(
                            icon = Icons.Default.Draw,
                            title = "Digital Signatures",
                            subtitle = "Fast canvas signing for official documents"
                        )
                        FeatureRow(
                            icon = Icons.Default.Security,
                            title = "Offline & Private",
                            subtitle = "Local-first processing without cloud tracking"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Footer Information
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "© 2026 JK Inc. All Rights Reserved.",
                    color = TextGrayLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Powered by JK Inc. | Made in India",
                    color = CopperLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CopperPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CopperLight,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = TextWhite,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                text = subtitle,
                color = TextGrayMuted,
                fontSize = 11.sp
            )
        }
    }
}
