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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme

data class StatItem(
    val label: String,
    val value: String,
    val valueColor: Color? = null
)

/**
 * Single label/value pair in a ledger card row (budget lines, reconciliation breakdown, etc.).
 */
@Composable
fun LedgerLabelValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = SaveBetterTheme.colors.ink,
    valueStyle: TextStyle = SaveBetterTheme.typography.amountMedium,
    valueFontWeight: FontWeight? = FontWeight.Bold
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = SaveBetterTheme.typography.caption,
            color = SaveBetterTheme.colors.textMuted,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = valueStyle,
            color = valueColor,
            fontWeight = valueFontWeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = item.label,
                    style = SaveBetterTheme.typography.caption,
                    color = SaveBetterTheme.colors.textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = item.value,
                    style = SaveBetterTheme.typography.amountSmall,
                    color = item.valueColor ?: SaveBetterTheme.colors.ink,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
