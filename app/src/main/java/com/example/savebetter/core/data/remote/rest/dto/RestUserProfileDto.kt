package com.example.savebetter.core.data.remote.rest.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * REST API DTO for user profile and financial settings.
 */
@Serializable
data class RestUserProfileDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("monthly_salary_minor") val monthlySalaryMinor: Long = 0L,
    @SerialName("preferred_language") val preferredLanguage: String = "en",
    @SerialName("onboarding_completed") val onboardingCompleted: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)
