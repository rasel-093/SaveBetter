package com.example.savebetter.core.data.remote.firebase.dto

import androidx.annotation.Keep

/**
 * Firebase Firestore DTO for monthly targets.
 */
@Keep
data class MonthlyTargetFirestoreDto(
    val id: String = "",
    val userId: String = "",
    val month: Int = 1,
    val year: Int = 2026,
    val targetAmountMinor: Long = 0L,
    val savingGoalMinor: Long = 0L,
    val updatedAtEpochMilli: Long = 0L
)
