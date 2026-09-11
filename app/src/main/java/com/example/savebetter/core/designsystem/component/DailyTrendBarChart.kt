package com.example.savebetter.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme

data class DailyBarData(
    val label: String,
    val amount: Float,
    val isHighlighted: Boolean = false
)

/**
 * Weekly / daily expense trend bar chart matching `.bars` and `.bars-axis`.
 */
@Composable
fun DailyTrendBarChart(
    bars: List<DailyBarData>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 80.dp,
    highlightColor: Color = SaveBetterTheme.colors.gold,
    defaultBarColor: Color = SaveBetterTheme.colors.paperLineStrong,
    onBarClick: ((DailyBarData) -> Unit)? = null
) {
    val maxAmount = bars.maxOfOrNull { it.amount }?.takeIf { it > 0f } ?: 1f

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            bars.forEach { bar ->
                val fraction = (bar.amount / maxAmount).coerceIn(0.05f, 1f)
                val animatedHeightFraction by animateFloatAsState(
                    targetValue = fraction,
                    label = "BarHeightAnimation"
                )

                val barColor = if (bar.isHighlighted) highlightColor else defaultBarColor
                val clickModifier = if (onBarClick != null) Modifier.clickable { onBarClick(bar) } else Modifier

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(animatedHeightFraction)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(barColor)
                            .then(clickModifier)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Axis
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            bars.forEach { bar ->
                Text(
                    text = bar.label,
                    style = SaveBetterTheme.typography.caption,
                    color = if (bar.isHighlighted) SaveBetterTheme.colors.ink else SaveBetterTheme.colors.textMuted,
                    fontSize = 10.5.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
