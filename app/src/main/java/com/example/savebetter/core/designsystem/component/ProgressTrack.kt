package com.example.savebetter.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme

/**
 * Budget progress track with smooth animation and state-aware colors.
 *
 * Defaults to:
 * - Moss green (< 70% used)
 * - Gold/Amber (70% - 90% used)
 * - Brick red (> 90% used)
 */
@Composable
fun ProgressTrack(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    trackColor: Color = SaveBetterTheme.colors.paperLine,
    fillColor: Color? = null
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        label = "ProgressTrackAnimation"
    )

    val resolvedFillColor = fillColor ?: when {
        clampedProgress >= 1.00f -> SaveBetterTheme.colors.brick
        clampedProgress >= 0.80f -> SaveBetterTheme.colors.gold
        else -> SaveBetterTheme.colors.moss
    }

    val shape = RoundedCornerShape(height / 2)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(shape)
                .background(resolvedFillColor)
        )
    }
}
