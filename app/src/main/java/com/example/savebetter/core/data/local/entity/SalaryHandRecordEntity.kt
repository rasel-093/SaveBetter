package com.example.savebetter.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.savebetter.core.domain.model.SyncState
import java.time.Instant

/**
 * Local Room entity for monthly salary and cash in hand tracking.
 */
@Entity(
    tableName = "salary_hand_records",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "year", "month"], unique = true)
    ]
)
data class SalaryHandRecordEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val month: Int,
    val year: Int,
    val salaryAmountMinor: Long,
    val handRemainingAmountMinor: Long,
    val updatedAt: Instant,
    val syncStatus: SyncState
)
