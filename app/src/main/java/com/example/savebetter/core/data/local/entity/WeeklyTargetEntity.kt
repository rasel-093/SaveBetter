package com.example.savebetter.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.savebetter.core.domain.model.SyncState
import java.time.Instant

/**
 * Local Room entity for weekly budget targets.
 */
@Entity(
    tableName = "weekly_targets",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "weekStart"], unique = true)
    ]
)
data class WeeklyTargetEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val weekStart: String,
    val weekEnd: String,
    val targetAmountMinor: Long,
    val updatedAt: Instant,
    val syncStatus: SyncState
)
