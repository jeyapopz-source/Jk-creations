package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CopperDark
import com.example.ui.theme.CopperGradient
import com.example.ui.theme.CopperLight
import com.example.ui.theme.CopperPrimary
import com.example.ui.theme.CopperSecondary
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JKTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = TextWhite,
                fontWeight = FontWeight.SemiBold,
                fontSize = 19.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("topbar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CopperLight
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CharcoalBlack,
            titleContentColor = TextWhite,
            navigationIconContentColor = CopperLight
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(0.5.dp, CharcoalBorder.copy(alpha = 0.5f))
            )
    )
}

@Composable
fun JKCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderStroke: BorderStroke = BorderStroke(1.dp, CharcoalBorder),
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalCard),
        border = borderStroke,
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(onClick = onClick)
                } else Modifier
            )
    ) {
        content()
    }
}

@Composable
fun JKPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = "primary_button"
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) CopperGradient else Brush.linearGradient(listOf(CharcoalBorder, CharcoalBorder)))
            .then(
                if (enabled) Modifier.clickable(onClick = onClick) else Modifier
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                color = if (enabled) TextWhite else TextGrayMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
fun JKSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = "secondary_button"
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CopperPrimary.copy(alpha = 0.7f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = CopperLight,
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        modifier = modifier
            .height(50.dp)
            .testTag(testTag)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CopperLight,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = CopperLight,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun JKEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(CharcoalCard)
                .border(1.dp, CopperPrimary.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CopperSecondary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = title,
            color = TextWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            color = TextGrayLight,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )
        if (actionButtonText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(20.dp))
            JKSecondaryButton(
                text = actionButtonText,
                onClick = onActionClick
            )
        }
    }
}

@Composable
fun JKWingsBadge(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Box(
        modifier = modifier
            .size(if (compact) 36.dp else 48.dp)
            .clip(RoundedCornerShape(if (compact) 10.dp else 14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(CharcoalCard, CharcoalBlack)
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(listOf(CopperLight, CopperDark)),
                RoundedCornerShape(if (compact) 10.dp else 14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🪶",
                fontSize = if (compact) 16.sp else 20.sp
            )
        }
    }
}
