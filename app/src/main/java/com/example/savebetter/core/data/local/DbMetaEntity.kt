package com.example.savebetter.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Minimal placeholder entity required to satisfy the Room @Database annotation
 * at Step 0. This entity holds no meaningful data and will be REPLACED by the
 * full entity set (UserEntity, ExpenseEntity, etc.) in Step 4.
 *
 * Do NOT use this entity in any business logic.
 */
@Entity(tableName = "db_meta")
internal data class DbMetaEntity(
    @PrimaryKey val key: String = "schema_version",
    val value: String = "0"
)
