package com.example.savebetter.feature.settings

import app.cash.turbine.test
import com.example.savebetter.core.domain.usecase.i18n.GetLanguageUseCase
import com.example.savebetter.core.domain.usecase.i18n.SetLanguageUseCase
import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [LanguageViewModel].
 */
class LanguageViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getLanguageUseCase: GetLanguageUseCase = mockk()
    private val setLanguageUseCase: SetLanguageUseCase = mockk(relaxed = true)

    @Test
    fun `currentLanguage reflects language emitted by GetLanguageUseCase`() = runTest {
        every { getLanguageUseCase() } returns flowOf(AppLanguage.BANGLA)

        val viewModel = LanguageViewModel(getLanguageUseCase, setLanguageUseCase)

        viewModel.currentLanguage.test {
            assertEquals(AppLanguage.BANGLA, awaitItem())
        }
    }

    @Test
    fun `onLanguageSelected triggers SetLanguageUseCase`() = runTest {
        every { getLanguageUseCase() } returns flowOf(AppLanguage.ENGLISH)
        coEvery { setLanguageUseCase(any()) } returns Unit

        val viewModel = LanguageViewModel(getLanguageUseCase, setLanguageUseCase)

        viewModel.onLanguageSelected(AppLanguage.BANGLA)

        coVerify { setLanguageUseCase(AppLanguage.BANGLA) }
    }
}
