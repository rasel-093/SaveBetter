package com.example.savebetter.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Accessor object for the current SaveBetter theme tokens.
 */
object SaveBetterTheme {
    val colors: SaveBetterColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSaveBetterColors.current

    val typography: SaveBetterTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalSaveBetterTypography.current

    val shapes: SaveBetterShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalSaveBetterShapes.current
}

/**
 * SaveBetter design system root theme provider.
 *
 * Provides [LocalSaveBetterColors], [LocalSaveBetterTypography], and [LocalSaveBetterShapes]
 * to the composition, while bridging to Material 3's [MaterialTheme].
 */
@Composable
fun SaveBetterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colors: SaveBetterColors = if (darkTheme) darkSaveBetterColors() else lightSaveBetterColors(),
    typography: SaveBetterTypography = SaveBetterTypography(),
    shapes: SaveBetterShapes = SaveBetterShapes(),
    content: @Composable () -> Unit
) {
    val materialColorScheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.gold,
            secondary = colors.moss,
            background = colors.paper,
            surface = colors.card,
            onPrimary = colors.cover,
            onSecondary = colors.paper,
            onBackground = colors.ink,
            onSurface = colors.ink,
            error = colors.brick
        )
    } else {
        lightColorScheme(
            primary = colors.gold,
            secondary = colors.moss,
            background = colors.paper,
            surface = colors.card,
            onPrimary = colors.cover,
            onSecondary = colors.paper,
            onBackground = colors.ink,
            onSurface = colors.ink,
            error = colors.brick
        )
    }

    CompositionLocalProvider(
        LocalSaveBetterColors provides colors,
        LocalSaveBetterTypography provides typography,
        LocalSaveBetterShapes provides shapes
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content
        )
    }
}
