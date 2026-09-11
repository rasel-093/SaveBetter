package com.example.savebetter.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.savebetter.core.domain.model.SyncState
import java.time.Instant

/**
 * Local Room entity for monthly budget targets and savings goals.
 */
@Entity(
    tableName = "monthly_targets",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "year", "month"], unique = true)
    ]
)
data class MonthlyTargetEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val month: Int,
    val year: Int,
    val targetAmountMinor: Long,
    val savingGoalMinor: Long,
    val updatedAt: Instant,
    val syncStatus: SyncState
)
