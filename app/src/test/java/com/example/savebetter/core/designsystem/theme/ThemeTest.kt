package com.example.savebetter.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeTest {

    @Test
    fun `light colors match HTML specification exactly`() {
        val light = lightSaveBetterColors()

        assertFalse(light.isDark)
        assertEquals(Color(0xFF1E2A22), light.ink)
        assertEquals(Color(0xFF3A4A3E), light.inkSoft)
        assertEquals(Color(0xFF121A15), light.cover)
        assertEquals(Color(0xFFFBF6EA), light.paper)
        assertEquals(Color(0xFFE6DCC2), light.paperLine)
        assertEquals(Color(0xFFD8CBA8), light.paperLineStrong)
        assertEquals(Color(0xFFC9A227), light.gold)
        assertEquals(Color(0xFFEFDFA3), light.goldSoft)
        assertEquals(Color(0xFFF7EFD2), light.goldTint)
        assertEquals(Color(0xFFA23E32), light.brick)
        assertEquals(Color(0xFFF1DAD3), light.brickTint)
        assertEquals(Color(0xFFB8862F), light.amber)
        assertEquals(Color(0xFF3F7856), light.moss)
        assertEquals(Color(0xFFDCEADF), light.mossTint)
        assertEquals(Color(0xFF4C6785), light.slate)
        assertEquals(Color(0xFF7A7060), light.textMuted)
    }

    @Test
    fun `dark colors match HTML specification dark surface tokens`() {
        val dark = darkSaveBetterColors()

        assertTrue(dark.isDark)
        assertEquals(Color(0xFF20281F), dark.paper)
        assertEquals(Color(0xFF28322A), dark.card)
        assertEquals(Color(0xFF37432E), dark.paperLine)
        assertEquals(Color(0xFFF2EDDD), dark.ink)
        assertEquals(Color(0xFF9CA697), dark.textMuted)
        assertEquals(Color(0xFFE08677), dark.brick)
    }

    @Test
    fun `category colors match cat1 through cat6`() {
        val colors = lightSaveBetterColors()

        assertEquals(Color(0xFFC9A227), colors.cat1)
        assertEquals(Color(0xFFA23E32), colors.cat2)
        assertEquals(Color(0xFF2F6F62), colors.cat3)
        assertEquals(Color(0xFF4C6785), colors.cat4)
        assertEquals(Color(0xFF7C8C3E), colors.cat5)
        assertEquals(Color(0xFF9C8F73), colors.cat6)
    }

    @Test
    fun `shape tokens match radius specifications`() {
        val shapes = SaveBetterShapes()

        assertEquals(10.dp, shapes.radiusCard)
        assertEquals(20.dp, shapes.radiusChip)
    }

    @Test
    fun `typography defines all required text styles`() {
        val typography = SaveBetterTypography()

        assertNotNull(typography.screenTitle)
        assertNotNull(typography.screenSubtitle)
        assertNotNull(typography.sectionLabel)
        assertNotNull(typography.body)
        assertNotNull(typography.caption)
        assertNotNull(typography.amountLarge)
        assertNotNull(typography.amountMedium)
        assertNotNull(typography.amountSmall)
    }
}
