package com.example.savebetter.ui.theme

import androidx.compose.runtime.Composable
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme as CoreSaveBetterTheme

/**
 * Backward-compatible bridge delegating to the unified design system in
 * [com.example.savebetter.core.designsystem.theme.SaveBetterTheme].
 */
@Composable
fun SaveBetterTheme(
    darkTheme: Boolean = false,
    language: com.example.savebetter.core.i18n.AppLanguage = com.example.savebetter.core.i18n.AppLanguage.ENGLISH,
    content: @Composable () -> Unit
) {
    CoreSaveBetterTheme(
        darkTheme = darkTheme,
        language = language,
        content = content
    )
}
