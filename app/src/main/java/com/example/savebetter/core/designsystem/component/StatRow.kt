package com.example.savebetter.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme

data class StatItem(
    val label: String,
    val value: String,
    val valueColor: Color? = null
)

/**
 * Metric summary row displaying financial statistics.
 *
 * All monetary amounts strictly use IBM Plex Mono.
 */
@Composable
fun StatRow(
    items: List<StatItem>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = item.label,
                    style = SaveBetterTheme.typography.caption,
                    color = SaveBetterTheme.colors.textMuted
                )
                Text(
                    text = item.value,
                    style = SaveBetterTheme.typography.amountMedium,
                    color = item.valueColor ?: SaveBetterTheme.colors.ink
                )
            }
        }
    }
}
