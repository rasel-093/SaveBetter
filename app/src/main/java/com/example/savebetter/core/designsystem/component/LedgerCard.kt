package com.example.savebetter.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme

/**
 * Paper-styled card container matching `.card` and `.card--flush`.
 *
 * Uses Column layout so children stack vertically, not on top of each other.
 * Automatically adapts background and border colors between light and dark themes.
 */
@Composable
fun LedgerCard(
    modifier: Modifier = Modifier,
    isFlush: Boolean = false,
    contentPadding: Dp = if (isFlush) 0.dp else 16.dp,
    containerColor: Color = SaveBetterTheme.colors.card,
    borderColor: Color = SaveBetterTheme.colors.paperLine,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = SaveBetterTheme.shapes.cardShape
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .border(1.dp, borderColor, shape)
            .then(clickModifier)
            .padding(contentPadding),
        content = content
    )
}
