package com.example.savebetter.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme

/**
 * Category chip matching `.chip` and `--radius-chip: 20px`.
 */
@Composable
fun CategoryChip(
    label: String,
    dotColor: Color,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val shape = SaveBetterTheme.shapes.chipShape
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    val backgroundColor = if (isSelected) {
        SaveBetterTheme.colors.goldTint
    } else {
        SaveBetterTheme.colors.card
    }

    val borderColor = if (isSelected) {
        SaveBetterTheme.colors.gold
    } else {
        SaveBetterTheme.colors.paperLineStrong
    }

    val textColor = if (isSelected) {
        SaveBetterTheme.colors.ink
    } else {
        SaveBetterTheme.colors.inkSoft
    }

    Row(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .then(clickModifier)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = label,
            style = SaveBetterTheme.typography.caption,
            color = textColor,
            fontSize = 11.5.sp
        )
    }
}
