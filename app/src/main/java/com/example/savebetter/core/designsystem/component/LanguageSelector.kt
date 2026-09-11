package com.example.savebetter.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme
import com.example.savebetter.core.i18n.AppLanguage

/**
 * Segmented language toggle matching the paper aesthetic.
 *
 * Allows instant switching between English and বাংলা.
 */
@Composable
fun LanguageSelector(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val outerShape = RoundedCornerShape(12.dp)
    val innerShape = RoundedCornerShape(9.dp)

    Row(
        modifier = modifier
            .clip(outerShape)
            .background(SaveBetterTheme.colors.card)
            .border(1.dp, SaveBetterTheme.colors.paperLineStrong, outerShape)
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppLanguage.entries.forEach { lang ->
            val isSelected = lang == selectedLanguage
            val bg = if (isSelected) SaveBetterTheme.colors.goldTint else SaveBetterTheme.colors.card
            val borderModifier = if (isSelected) {
                Modifier.border(1.dp, SaveBetterTheme.colors.gold, innerShape)
            } else {
                Modifier
            }

            Box(
                modifier = Modifier
                    .clip(innerShape)
                    .background(bg)
                    .then(borderModifier)
                    .clickable { onLanguageSelected(lang) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = lang.nativeName,
                    style = SaveBetterTheme.typography.caption,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) SaveBetterTheme.colors.ink else SaveBetterTheme.colors.textMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}
