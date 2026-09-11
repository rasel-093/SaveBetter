package com.example.savebetter.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.savebetter.core.domain.model.SyncState
import java.time.Instant

/**
 * Local Room entity for the authenticated user profile.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String?,
    val email: String?,
    val monthlySalaryMinor: Long,
    val preferredLanguage: String,
    val onboardingCompleted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncState
)
