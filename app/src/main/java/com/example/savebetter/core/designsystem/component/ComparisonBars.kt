package com.example.savebetter.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme

data class ComparisonBarItem(
    val label: String,
    val amountFormatted: String,
    val value: Float,
    val color: Color
)

/**
 * Visual comparative bar pairs (e.g. Budget vs Actual, This Week vs Last Week).
 */
@Composable
fun ComparisonBars(
    primaryItem: ComparisonBarItem,
    secondaryItem: ComparisonBarItem,
    modifier: Modifier = Modifier
) {
    val maxValue = maxOf(primaryItem.value, secondaryItem.value).coerceAtLeast(1f)

    Column(modifier = modifier.fillMaxWidth()) {
        // Primary
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = primaryItem.label,
                    style = SaveBetterTheme.typography.caption,
                    color = SaveBetterTheme.colors.textMuted,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = primaryItem.amountFormatted,
                    style = SaveBetterTheme.typography.amountSmall,
                    color = SaveBetterTheme.colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            ProgressTrack(
                progress = primaryItem.value / maxValue,
                height = 7.dp,
                fillColor = primaryItem.color
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = secondaryItem.label,
                    style = SaveBetterTheme.typography.caption,
                    color = SaveBetterTheme.colors.textMuted,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = secondaryItem.amountFormatted,
                    style = SaveBetterTheme.typography.amountSmall,
                    color = SaveBetterTheme.colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            ProgressTrack(
                progress = secondaryItem.value / maxValue,
                height = 7.dp,
                fillColor = secondaryItem.color
            )
        }
    }
}
