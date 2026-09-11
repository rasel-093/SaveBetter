package com.example.savebetter.core.domain.model

import androidx.annotation.StringRes

enum class MonthlySuggestionType {
    OUTLIER_EXCLUDED,
    REDUCTION_SUGGESTION,
    SAVINGS_FEASIBLE
}

data class MonthlySuggestion(
    val id: String,
    val type: MonthlySuggestionType,
    @get:StringRes val titleResId: Int,
    val titleArgs: List<String> = emptyList(),
    @get:StringRes val messageResId: Int,
    val messageArgs: List<String> = emptyList(),
    val isWarning: Boolean = false
)
