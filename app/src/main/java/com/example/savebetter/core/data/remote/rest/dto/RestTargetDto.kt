package com.example.savebetter.core.data.remote.rest.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * REST API DTOs for budget targets and salary records.
 */

@Serializable
data class RestWeeklyTargetDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("week_start") val weekStart: String,
    @SerialName("week_end") val weekEnd: String,
    @SerialName("target_amount_minor") val targetAmountMinor: Long,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class RestMonthlyTargetDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("month") val month: Int,
    @SerialName("year") val year: Int,
    @SerialName("target_amount_minor") val targetAmountMinor: Long,
    @SerialName("saving_goal_minor") val savingGoalMinor: Long = 0L,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class RestSalaryHandRecordDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("month") val month: Int,
    @SerialName("year") val year: Int,
    @SerialName("salary_amount_minor") val salaryAmountMinor: Long,
    @SerialName("hand_remaining_amount_minor") val handRemainingAmountMinor: Long,
    @SerialName("updated_at") val updatedAt: String
)
