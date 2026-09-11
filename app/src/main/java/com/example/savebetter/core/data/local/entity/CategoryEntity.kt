package com.example.savebetter.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.savebetter.core.domain.model.SyncState
import java.time.Instant

/**
 * Local Room entity for categories.
 */
@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "isDefault"])
    ]
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val nameKey: String?,
    val customName: String?,
    val icon: String,
    val colorToken: String,
    val isDefault: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncState,
    val isDeleted: Boolean = false
)
