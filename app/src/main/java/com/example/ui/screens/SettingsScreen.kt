package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.JKCard
import com.example.ui.components.JKTopAppBar
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CopperDark
import com.example.ui.theme.CopperLight
import com.example.ui.theme.CopperPrimary
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    onBackClick: () -> Unit,
    onNavigateToSignature: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val appSettings by viewModel.appSettings.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            JKTopAppBar(
                title = "Settings",
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
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding()
                .testTag("settings_screen"),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // General Section
            SettingsSectionHeader(title = "GENERAL")
            JKCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Language,
                        title = "Language",
                        value = appSettings.language,
                        onClick = { scope.launch { snackbarHostState.showSnackbar("English (US)") } }
                    )
                    SettingsDivider()
                    SettingsRowItem(
                        icon = Icons.Default.ColorLens,
                        title = "Theme",
                        value = appSettings.theme,
                        onClick = { scope.launch { snackbarHostState.showSnackbar("Dark Charcoal & Metallic Copper") } }
                    )
                    SettingsDivider()
                    SettingsRowItem(
                        icon = Icons.Default.Description,
                        title = "Default Page Size",
                        value = appSettings.defaultPageSize.displayName,
                        onClick = { scope.launch { snackbarHostState.showSnackbar("Default Page Size: A4") } }
                    )
                    SettingsDivider()
                    SettingsRowItem(
                        icon = Icons.Default.PictureAsPdf,
                        title = "Default Quality",
                        value = appSettings.defaultQuality.displayName,
                        onClick = { scope.launch { snackbarHostState.showSnackbar("Default Quality: Balanced") } }
                    )
                    SettingsDivider()
                    SettingsRowItem(
                        icon = Icons.Default.Settings,
                        title = "Default Orientation",
                        value = appSettings.defaultOrientation.displayName,
                        onClick = { scope.launch { snackbarHostState.showSnackbar("Default Orientation: Auto") } }
                    )
                }
            }

            // PDF Section
            SettingsSectionHeader(title = "PDF")
            JKCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Description,
                        title = "Default Filename",
                        value = appSettings.defaultFileName,
                        onClick = { scope.launch { snackbarHostState.showSnackbar("Default: My_Document.pdf") } }
                    )
                    SettingsDivider()
                    SettingsRowItem(
                        icon = Icons.Default.WaterDrop,
                        title = "Watermark Settings",
                        value = "JK Creations",
                        onClick = { scope.launch { snackbarHostState.showSnackbar("Custom watermark configurable per PDF") } }
                    )
                }
            }

            // Tools Section
            SettingsSectionHeader(title = "TOOLS")
            JKCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Draw,
                        title = "Digital Signature",
                        value = "Configure Canvas",
                        testTag = "settings_digital_signature_item",
                        onClick = onNavigateToSignature
                    )
                }
            }

            // Storage Section
            SettingsSectionHeader(title = "STORAGE")
            JKCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Folder,
                        title = "Save / Export preferences",
                        value = appSettings.savePreferences,
                        onClick = { scope.launch { snackbarHostState.showSnackbar("Internal Storage / Documents") } }
                    )
                }
            }

            // About Section
            SettingsSectionHeader(title = "ABOUT")
            JKCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Info,
                        title = "About JK Creations",
                        value = "v1.0.0",
                        testTag = "settings_about_item",
                        onClick = onNavigateToAbout
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = CopperLight,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    value: String? = null,
    testTag: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                color = TextWhite,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value != null) {
                Text(
                    text = value,
                    color = TextGrayLight,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextGrayMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(CharcoalBorder.copy(alpha = 0.6f))
    )
}
