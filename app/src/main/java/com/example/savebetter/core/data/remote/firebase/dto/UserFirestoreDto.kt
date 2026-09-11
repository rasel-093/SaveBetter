package com.example.savebetter.core.data.remote.firebase.dto

import androidx.annotation.Keep

/**
 * Firebase Firestore DTO for user profile.
 *
 * Isolated to the Firebase implementation layer.
 */
@Keep
data class UserFirestoreDto(
    val id: String = "",
    val name: String? = null,
    val email: String? = null,
    val monthlySalaryMinor: Long = 0L,
    val preferredLanguage: String = "en",
    val onboardingCompleted: Boolean = false,
    val createdAtEpochMilli: Long = 0L,
    val updatedAtEpochMilli: Long = 0L
)
