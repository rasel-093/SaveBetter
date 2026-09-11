package com.example.savebetter.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.savebetter.core.domain.model.SyncState
import java.time.Instant

/**
 * Local Room entity for expense transactions.
 */
@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["date"]),
        Index(value = ["categoryId"]),
        Index(value = ["userId", "date"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amountMinor: Long,
    val categoryId: String,
    val note: String?,
    val date: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncState,
    val isDeleted: Boolean = false,
    val deletedAt: Instant? = null
)
