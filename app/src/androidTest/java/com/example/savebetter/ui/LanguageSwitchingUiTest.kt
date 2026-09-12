package com.example.savebetter.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme
import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.core.i18n.CurrencyFormatter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LanguageSwitchingUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun languageSwitching_updatesCurrencyAndNumeralFormattingDynamically() {
        val amountMinor = 125050L // 1250.50

        composeTestRule.setContent {
            var currentLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }

            SaveBetterTheme(language = currentLanguage) {
                Column {
                    Text(
                        text = CurrencyFormatter.formatMinor(amountMinor, currentLanguage)
                    )

                    Button(onClick = { currentLanguage = AppLanguage.BANGLA }) {
                        Text("Switch to Bangla")
                    }

                    Button(onClick = { currentLanguage = AppLanguage.ENGLISH }) {
                        Text("Switch to English")
                    }
                }
            }
        }

        // Initially in English: 1,250.50
        val englishFormatted = CurrencyFormatter.formatMinor(amountMinor, AppLanguage.ENGLISH)
        composeTestRule.onNodeWithText(englishFormatted).assertIsDisplayed()

        // Switch to Bangla
        composeTestRule.onNodeWithText("Switch to Bangla").performClick()

        // Verify Bangla formatted text is displayed: ১,২৫০.৫০
        val banglaFormatted = CurrencyFormatter.formatMinor(amountMinor, AppLanguage.BANGLA)
        composeTestRule.onNodeWithText(banglaFormatted).assertIsDisplayed()

        // Switch back to English
        composeTestRule.onNodeWithText("Switch to English").performClick()
        composeTestRule.onNodeWithText(englishFormatted).assertIsDisplayed()
    }
}
