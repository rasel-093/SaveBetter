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
    content: @Composable () -> Unit
) {
    CoreSaveBetterTheme(
        darkTheme = darkTheme,
        content = content
    )
}
