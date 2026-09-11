package com.example.savebetter.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.savebetter.R

val TiroBanglaFontFamily = FontFamily(
    Font(R.font.tiro_bangla_regular, weight = FontWeight.Normal),
    Font(R.font.tiro_bangla_regular, weight = FontWeight.Bold)
)

val HindSiliguriFontFamily = FontFamily(
    Font(R.font.hind_siliguri_regular, weight = FontWeight.Normal),
    Font(R.font.hind_siliguri_semibold, weight = FontWeight.Medium),
    Font(R.font.hind_siliguri_semibold, weight = FontWeight.SemiBold),
    Font(R.font.hind_siliguri_semibold, weight = FontWeight.Bold)
)

val IBMPlexMonoFontFamily = FontFamily(
    Font(R.font.ibm_plex_mono_regular, weight = FontWeight.Normal),
    Font(R.font.ibm_plex_mono_semibold, weight = FontWeight.Medium),
    Font(R.font.ibm_plex_mono_semibold, weight = FontWeight.SemiBold),
    Font(R.font.ibm_plex_mono_semibold, weight = FontWeight.Bold)
)

/**
 * SaveBetter design system typography.
 *
 * Rules:
 * - Headings use Tiro Bangla
 * - Body/UI text uses Hind Siliguri
 * - Monetary amounts ALWAYS use IBM Plex Mono
 */
@Immutable
data class SaveBetterTypography(
    val screenTitle: TextStyle = TextStyle(
        fontFamily = TiroBanglaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    val screenSubtitle: TextStyle = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    val sectionLabel: TextStyle = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.12.em
    ),
    val body: TextStyle = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    val caption: TextStyle = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp
    ),
    val amountLarge: TextStyle = TextStyle(
        fontFamily = IBMPlexMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp
    ),
    val amountMedium: TextStyle = TextStyle(
        fontFamily = IBMPlexMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    val amountSmall: TextStyle = TextStyle(
        fontFamily = IBMPlexMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
)

val LocalSaveBetterTypography = staticCompositionLocalOf { SaveBetterTypography() }
