package com.example.savebetter.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shape tokens matching expense-tracker-ui-design-savebetter.html:
 * --radius-card: 10px;
 * --radius-chip: 20px;
 */
@Immutable
data class SaveBetterShapes(
    val radiusCard: Dp = 10.dp,
    val radiusChip: Dp = 20.dp,
    val radiusButton: Dp = 12.dp,
    val radiusPill: Dp = 20.dp,
    val cardShape: RoundedCornerShape = RoundedCornerShape(10.dp),
    val chipShape: RoundedCornerShape = RoundedCornerShape(20.dp),
    val buttonShape: RoundedCornerShape = RoundedCornerShape(12.dp),
    val pillShape: RoundedCornerShape = RoundedCornerShape(20.dp)
)

val LocalSaveBetterShapes = staticCompositionLocalOf { SaveBetterShapes() }
