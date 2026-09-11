package com.example.savebetter.core.domain.usecase.i18n

import app.cash.turbine.test
import com.example.savebetter.core.data.local.LanguagePreferences
import com.example.savebetter.core.i18n.AppLanguage
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [GetLanguageUseCase].
 */
class GetLanguageUseCaseTest {

    private val languagePreferences: LanguagePreferences = mockk()
    private val useCase = GetLanguageUseCase(languagePreferences)

    @Test
    fun `invoke delegates directly to languagePreferences`() = runTest {
        every { languagePreferences.language } returns flowOf(AppLanguage.BANGLA)

        useCase().test {
            assertEquals(AppLanguage.BANGLA, awaitItem())
            awaitComplete()
        }
    }
}
