package com.example.savebetter.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme

enum class AlertBannerType {
    Warning,
    Danger,
    Info,
    Success
}

/**
 * Alert banner component matching `.alert` styles.
 *
 * Distinct background tints, border colors, and icons per alert level.
 */
@Composable
fun AlertBanner(
    title: String? = null,
    message: String,
    type: AlertBannerType = AlertBannerType.Warning,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val (bgColor, borderColor, contentColor, defaultIcon) = when (type) {
        AlertBannerType.Warning -> Quadruple(
            SaveBetterTheme.colors.amberTint,
            SaveBetterTheme.colors.amber,
            SaveBetterTheme.colors.ink,
            Icons.Outlined.Warning
        )
        AlertBannerType.Danger -> Quadruple(
            SaveBetterTheme.colors.brickTint,
            SaveBetterTheme.colors.brick,
            SaveBetterTheme.colors.ink,
            Icons.Outlined.Warning
        )
        AlertBannerType.Info -> Quadruple(
            SaveBetterTheme.colors.goldTint,
            SaveBetterTheme.colors.gold,
            SaveBetterTheme.colors.ink,
            Icons.Outlined.Info
        )
        AlertBannerType.Success -> Quadruple(
            SaveBetterTheme.colors.mossTint,
            SaveBetterTheme.colors.moss,
            SaveBetterTheme.colors.ink,
            Icons.Outlined.CheckCircle
        )
    }

    val shape = RoundedCornerShape(10.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .semantics(mergeDescendants = true) {}
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon ?: defaultIcon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 1.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (title != null) {
                Text(
                    text = title,
                    style = SaveBetterTheme.typography.body,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = message,
                style = SaveBetterTheme.typography.caption,
                color = contentColor,
                lineHeight = 16.sp,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
