package com.example.savebetter.feature.onboarding

import androidx.annotation.StringRes

/**
 * UI state for the Onboarding screen (Screen 02).
 */
data class OnboardingUiState(
    val salaryInput: String = "",
    val monthlyTargetInput: String = "",
    val savingGoalInput: String = "",
    val weeklyTargetInput: String = "",
    @get:StringRes val salaryErrorResId: Int? = null,
    @get:StringRes val monthlyTargetErrorResId: Int? = null,
    @get:StringRes val savingGoalErrorResId: Int? = null,
    @get:StringRes val weeklyTargetErrorResId: Int? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)
