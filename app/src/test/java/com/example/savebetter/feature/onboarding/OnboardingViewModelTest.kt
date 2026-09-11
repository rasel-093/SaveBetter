package com.example.savebetter.feature.onboarding

import app.cash.turbine.test
import com.example.savebetter.R
import com.example.savebetter.core.domain.usecase.profile.CompleteOnboardingUseCase
import com.example.savebetter.core.domain.usecase.profile.OnboardingParams
import com.example.savebetter.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val completeOnboardingUseCase: CompleteOnboardingUseCase = mockk()
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        viewModel = OnboardingViewModel(completeOnboardingUseCase)
    }

    @Test
    fun `entering salary auto calculates recommended target defaults`() = runTest {
        viewModel.onSalaryChange("50000")

        val state = viewModel.uiState.value
        assertEquals("50000", state.salaryInput)
        assertEquals("35000", state.monthlyTargetInput) // 70%
        assertEquals("10000", state.savingGoalInput)   // 20%
        assertEquals("8750", state.weeklyTargetInput)   // monthlyTarget / 4
    }

    @Test
    fun `entering Bengali numerals converts cleanly to digits and calculates defaults`() = runTest {
        viewModel.onSalaryChange("৫০,০০০")

        val state = viewModel.uiState.value
        assertEquals("50000", state.salaryInput)
        assertEquals("35000", state.monthlyTargetInput)
    }

    @Test
    fun `manual edits to targets are preserved when salary changes`() = runTest {
        viewModel.onMonthlyTargetChange("40000")
        viewModel.onSalaryChange("60000")

        val state = viewModel.uiState.value
        assertEquals("60000", state.salaryInput)
        assertEquals("40000", state.monthlyTargetInput)
    }

    @Test
    fun `submit with empty inputs triggers validation errors without invoking use case`() = runTest {
        viewModel.submit(userId = "user_123")

        val state = viewModel.uiState.value
        assertEquals(R.string.onboarding_error_invalid_salary, state.salaryErrorResId)
        assertEquals(R.string.onboarding_error_invalid_monthly_target, state.monthlyTargetErrorResId)
        assertEquals(R.string.onboarding_error_invalid_weekly_target, state.weeklyTargetErrorResId)
        assertFalse(state.isSuccess)
        assertFalse(state.isLoading)

        coVerify(exactly = 0) { completeOnboardingUseCase(any()) }
    }

    @Test
    fun `submit with valid inputs calls usecase and emits isSuccess true`() = runTest {
        val paramsSlot = slot<OnboardingParams>()
        coEvery { completeOnboardingUseCase(capture(paramsSlot)) } returns Result.success(Unit)

        viewModel.onSalaryChange("50000")
        viewModel.submit(
            userId = "user_123",
            name = "Test User",
            email = "user@example.com",
            preferredLanguage = "bn"
        )

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isSuccess)
            assertFalse(state.isLoading)
            assertNull(state.generalError)
        }

        coVerify(exactly = 1) { completeOnboardingUseCase(any()) }
        assertEquals("user_123", paramsSlot.captured.userId)
        assertEquals(5000000L, paramsSlot.captured.monthlySalaryMinor)
        assertEquals(3500000L, paramsSlot.captured.monthlyTargetMinor)
        assertEquals(1000000L, paramsSlot.captured.savingGoalMinor)
        assertEquals(875000L, paramsSlot.captured.weeklyTargetMinor)
        assertEquals("bn", paramsSlot.captured.preferredLanguage)
    }

    @Test
    fun `submit failure surfaces error in ui state`() = runTest {
        coEvery { completeOnboardingUseCase(any()) } returns Result.failure(Exception("Room database locked"))

        viewModel.onSalaryChange("50000")
        viewModel.submit(userId = "user_123")

        val state = viewModel.uiState.value
        assertFalse(state.isSuccess)
        assertFalse(state.isLoading)
        assertEquals("Room database locked", state.generalError)
    }
}
