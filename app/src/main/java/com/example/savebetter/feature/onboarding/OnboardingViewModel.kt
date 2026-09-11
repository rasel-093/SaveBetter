package com.example.savebetter.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebetter.R
import com.example.savebetter.core.domain.usecase.profile.CompleteOnboardingUseCase
import com.example.savebetter.core.domain.usecase.profile.OnboardingParams
import com.example.savebetter.core.i18n.NumeralConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var userManuallyEditedTargets = false

    fun onSalaryChange(input: String) {
        val clean = cleanInput(input)
        _uiState.update { current ->
            val updated = current.copy(
                salaryInput = clean,
                salaryErrorResId = null,
                generalError = null
            )
            // Auto-calculate smart defaults if the user hasn't typed their own targets yet
            if (!userManuallyEditedTargets && clean.isNotEmpty()) {
                val salary = clean.toLongOrNull()
                if (salary != null && salary > 0) {
                    val monthlyTarget = (salary * 0.70).toLong()
                    val savingGoal = (salary * 0.20).toLong()
                    val weeklyTarget = monthlyTarget / 4
                    updated.copy(
                        monthlyTargetInput = monthlyTarget.toString(),
                        savingGoalInput = savingGoal.toString(),
                        weeklyTargetInput = weeklyTarget.toString(),
                        monthlyTargetErrorResId = null,
                        savingGoalErrorResId = null,
                        weeklyTargetErrorResId = null
                    )
                } else updated
            } else updated
        }
    }

    fun onMonthlyTargetChange(input: String) {
        userManuallyEditedTargets = true
        val clean = cleanInput(input)
        _uiState.update {
            it.copy(
                monthlyTargetInput = clean,
                monthlyTargetErrorResId = null,
                generalError = null
            )
        }
    }

    fun onSavingGoalChange(input: String) {
        userManuallyEditedTargets = true
        val clean = cleanInput(input)
        _uiState.update {
            it.copy(
                savingGoalInput = clean,
                savingGoalErrorResId = null,
                generalError = null
            )
        }
    }

    fun onWeeklyTargetChange(input: String) {
        userManuallyEditedTargets = true
        val clean = cleanInput(input)
        _uiState.update {
            it.copy(
                weeklyTargetInput = clean,
                weeklyTargetErrorResId = null,
                generalError = null
            )
        }
    }

    fun submit(
        userId: String,
        name: String? = null,
        email: String? = null,
        preferredLanguage: String = "en"
    ) {
        val state = _uiState.value
        val salaryMajor = parseMajorAmount(state.salaryInput)
        val monthlyTargetMajor = parseMajorAmount(state.monthlyTargetInput)
        val savingGoalMajor = parseMajorAmount(state.savingGoalInput)
        val weeklyTargetMajor = parseMajorAmount(state.weeklyTargetInput)

        var hasError = false
        var salaryError: Int? = null
        var monthlyTargetError: Int? = null
        var savingGoalError: Int? = null
        var weeklyTargetError: Int? = null

        if (salaryMajor == null || salaryMajor <= 0) {
            salaryError = R.string.onboarding_error_invalid_salary
            hasError = true
        }
        if (monthlyTargetMajor == null || monthlyTargetMajor <= 0) {
            monthlyTargetError = R.string.onboarding_error_invalid_monthly_target
            hasError = true
        }
        if (savingGoalMajor == null || savingGoalMajor < 0) {
            savingGoalError = R.string.onboarding_error_invalid_saving_goal
            hasError = true
        }
        if (weeklyTargetMajor == null || weeklyTargetMajor <= 0) {
            weeklyTargetError = R.string.onboarding_error_invalid_weekly_target
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    salaryErrorResId = salaryError,
                    monthlyTargetErrorResId = monthlyTargetError,
                    savingGoalErrorResId = savingGoalError,
                    weeklyTargetErrorResId = weeklyTargetError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, generalError = null) }

        viewModelScope.launch {
            val params = OnboardingParams(
                userId = userId,
                name = name,
                email = email,
                monthlySalaryMinor = salaryMajor!! * 100L,
                monthlyTargetMinor = monthlyTargetMajor!! * 100L,
                savingGoalMinor = savingGoalMajor!! * 100L,
                weeklyTargetMinor = weeklyTargetMajor!! * 100L,
                preferredLanguage = preferredLanguage
            )

            val result = completeOnboardingUseCase(params)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = error.localizedMessage ?: "Failed to save onboarding targets"
                        )
                    }
                }
            )
        }
    }

    private fun cleanInput(input: String): String {
        val ascii = NumeralConverter.toEnglishDigits(input)
        return ascii.filter { it.isDigit() }
    }

    private fun parseMajorAmount(input: String): Long? {
        val clean = cleanInput(input)
        return clean.toLongOrNull()
    }
}
