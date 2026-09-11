package com.example.savebetter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Temporary Material 3 theme placeholder.
 *
 * This theme is ONLY used at Step 0 to satisfy the compiler.
 * It will be completely replaced by the full SaveBetter design system in Step 2.
 *
 * DO NOT add colors, typography, or components here.
 * The real design system lives in core/designsystem (Step 2).
 */

private val LightColors = lightColorScheme(
    primary   = Color(0xFFC9A227), // gold
    secondary = Color(0xFF3F7856), // moss
    background = Color(0xFFFBF6EA), // paper
    surface    = Color(0xFFFBF6EA),
    onPrimary  = Color(0xFF121A15),
    onBackground = Color(0xFF1E2A22),
    onSurface  = Color(0xFF1E2A22),
)

private val DarkColors = darkColorScheme(
    primary   = Color(0xFFC9A227),
    secondary = Color(0xFF3F7856),
    background = Color(0xFF20281F),
    surface    = Color(0xFF28322A),
    onPrimary  = Color(0xFF121A15),
    onBackground = Color(0xFFF2EDDD),
    onSurface  = Color(0xFFF2EDDD),
)

@Composable
fun SaveBetterTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content     = content
    )
}
