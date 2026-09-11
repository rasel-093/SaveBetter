package com.example.savebetter.core.domain.model

import java.time.Instant

/**
 * Backend-agnostic domain model for a user's financial profile.
 */
data class UserProfile(
    val id: String,
    val name: String?,
    val email: String?,
    val monthlySalaryMinor: Long = 0L,
    val preferredLanguage: String = "en",
    val onboardingCompleted: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val syncStatus: SyncState = SyncState.SYNCED
)
