package com.example.savebetter.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebetter.R
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme

data class DonutSlice(
    val label: String,
    val value: Float,
    val color: Color
)

/**
 * Donut chart component matching `.donut` and `.donut-legend`.
 *
 * Visualizes category distribution with percentage legend.
 */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    chartSize: Dp = 108.dp,
    strokeWidth: Dp = 18.dp,
    showLegend: Boolean = true
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)
    val cdChart = stringResource(R.string.cd_donut_chart)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = cdChart },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(chartSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(chartSize)) {
                var startAngle = -90f
                slices.forEach { slice ->
                    val sweepAngle = (slice.value / total) * 360f
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }
        }

        if (showLegend) {
            Spacer(modifier = Modifier.width(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                slices.forEach { slice ->
                    val percentage = (slice.value / total * 100).toInt()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(slice.color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = slice.label,
                            style = SaveBetterTheme.typography.caption,
                            color = SaveBetterTheme.colors.inkSoft,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$percentage%",
                            style = SaveBetterTheme.typography.amountSmall,
                            color = SaveBetterTheme.colors.textMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
