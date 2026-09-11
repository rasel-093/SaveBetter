package com.example.savebetter.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.savebetter.core.domain.model.DebtDirection
import com.example.savebetter.core.domain.model.SyncState
import java.time.Instant

/**
 * Local Room entity for debt and credit entries.
 */
@Entity(
    tableName = "debt_credits",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "direction"]),
        Index(value = ["userId", "isSettled"])
    ]
)
data class DebtCreditEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val direction: DebtDirection,
    val personName: String,
    val amountMinor: Long,
    val note: String?,
    val date: Instant,
    val dueDate: Instant?,
    val isSettled: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncState,
    val isDeleted: Boolean = false,
    val deletedAt: Instant? = null
)
