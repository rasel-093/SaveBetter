package com.example.savebetter.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * SaveBetter design system color palette.
 * Exact tokens derived from expense-tracker-ui-design-savebetter.html
 */
@Immutable
data class SaveBetterColors(
    val ink: Color,
    val inkSoft: Color,
    val cover: Color,
    val paper: Color,
    val paperLine: Color,
    val paperLineStrong: Color,
    val gold: Color,
    val goldSoft: Color,
    val goldTint: Color,
    val brick: Color,
    val brickTint: Color,
    val amber: Color,
    val amberTint: Color,
    val moss: Color,
    val mossTint: Color,
    val slate: Color,
    val textMuted: Color,
    val textOnCover: Color,
    val card: Color,

    // Category colors (cat1 - cat6)
    val cat1: Color = Color(0xFFC9A227),
    val cat2: Color = Color(0xFFA23E32),
    val cat3: Color = Color(0xFF2F6F62),
    val cat4: Color = Color(0xFF4C6785),
    val cat5: Color = Color(0xFF7C8C3E),
    val cat6: Color = Color(0xFF9C8F73),

    val isDark: Boolean = false
)

fun lightSaveBetterColors(): SaveBetterColors = SaveBetterColors(
    ink = Color(0xFF1E2A22),
    inkSoft = Color(0xFF3A4A3E),
    cover = Color(0xFF121A15),
    paper = Color(0xFFFBF6EA),
    paperLine = Color(0xFFE6DCC2),
    paperLineStrong = Color(0xFFD8CBA8),
    gold = Color(0xFFC9A227),
    goldSoft = Color(0xFFEFDFA3),
    goldTint = Color(0xFFF7EFD2),
    brick = Color(0xFFA23E32),
    brickTint = Color(0xFFF1DAD3),
    amber = Color(0xFFB8862F),
    amberTint = Color(0xFFF3E6C6),
    moss = Color(0xFF3F7856),
    mossTint = Color(0xFFDCEADF),
    slate = Color(0xFF4C6785),
    textMuted = Color(0xFF7A7060),
    textOnCover = Color(0xFFD9D2BF),
    card = Color(0xFFFFFFFF),
    isDark = false
)

fun darkSaveBetterColors(): SaveBetterColors = SaveBetterColors(
    ink = Color(0xFFF2EDDD),
    inkSoft = Color(0xFFD5CEBC),
    cover = Color(0xFF121A15),
    paper = Color(0xFF20281F),
    paperLine = Color(0xFF37432E),
    paperLineStrong = Color(0xFF495A3E),
    gold = Color(0xFFC9A227),
    goldSoft = Color(0xFFEFDFA3),
    goldTint = Color(0xFF2F3722),
    brick = Color(0xFFE08677),
    brickTint = Color(0xFF3E2320),
    amber = Color(0xFFCFA043),
    amberTint = Color(0xFF382F1D),
    moss = Color(0xFF539E72),
    mossTint = Color(0xFF1E3827),
    slate = Color(0xFF6B87A8),
    textMuted = Color(0xFF9CA697),
    textOnCover = Color(0xFFD9D2BF),
    card = Color(0xFF28322A),
    isDark = true
)

val LocalSaveBetterColors = staticCompositionLocalOf { lightSaveBetterColors() }
