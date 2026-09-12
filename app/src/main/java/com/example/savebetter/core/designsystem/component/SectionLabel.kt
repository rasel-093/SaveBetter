package com.example.savebetter.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme

/**
 * Uppercase section tracking header matching `.section-label`.
 */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text.uppercase(),
            style = SaveBetterTheme.typography.sectionLabel,
            color = SaveBetterTheme.colors.textMuted,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = SaveBetterTheme.typography.caption,
                color = SaveBetterTheme.colors.moss,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clickable(onClick = onActionClick)
                    .padding(start = 8.dp)
            )
        }
    }
}
