package com.example.savebetter.core.data.remote.firebase.dto

import androidx.annotation.Keep

/**
 * Firebase Firestore DTO for weekly targets.
 */
@Keep
data class WeeklyTargetFirestoreDto(
    val id: String = "",
    val userId: String = "",
    val weekStart: String = "",
    val weekEnd: String = "",
    val targetAmountMinor: Long = 0L,
    val updatedAtEpochMilli: Long = 0L
)
