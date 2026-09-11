package com.example.savebetter.core.designsystem.theme

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.savebetter.core.i18n.AppLanguage
import java.util.Locale

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }

/**
 * Accessor object for the current SaveBetter theme tokens and active language.
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

    val language: AppLanguage
        @Composable
        @ReadOnlyComposable
        get() = LocalAppLanguage.current
}

/**
 * SaveBetter design system root theme provider.
 *
 * Provides [LocalSaveBetterColors], [LocalSaveBetterTypography], [LocalSaveBetterShapes],
 * and [LocalAppLanguage] to the composition. Dynamically re-configures [LocalConfiguration]
 * so all [androidx.compose.ui.res.stringResource] calls instantly reflect the active language.
 */
@Composable
fun SaveBetterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    language: AppLanguage = LocalAppLanguage.current,
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

    val currentConfig = LocalConfiguration.current
    val localizedConfig = Configuration(currentConfig).apply {
        setLocale(Locale.forLanguageTag(language.code))
    }

    CompositionLocalProvider(
        LocalConfiguration provides localizedConfig,
        LocalSaveBetterColors provides colors,
        LocalSaveBetterTypography provides typography,
        LocalSaveBetterShapes provides shapes,
        LocalAppLanguage provides language
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content
        )
    }
}
