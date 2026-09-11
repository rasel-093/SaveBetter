package com.example.savebetter.core.domain.model

import androidx.annotation.StringRes

enum class WeeklyAdviceType {
    CATEGORY_DOMINANT,
    FREQUENT_SMALL,
    PROJECTED_OVERSHOOT,
    ON_TRACK
}

data class WeeklyAdvice(
    val id: String,
    val type: WeeklyAdviceType,
    @get:StringRes val titleResId: Int,
    val titleArgs: List<String> = emptyList(),
    @get:StringRes val messageResId: Int,
    val messageArgs: List<String> = emptyList(),
    val isWarning: Boolean = false
)
