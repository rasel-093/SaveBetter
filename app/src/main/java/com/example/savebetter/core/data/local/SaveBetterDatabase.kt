package com.example.savebetter.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * SaveBetter local Room database.
 *
 * This is the central Room database for the application. It is the
 * *local source of truth* — the UI observes Flows backed by this database.
 * Remote data (Firebase now, Django later) is synchronised *into* this database
 * by the SyncWorker.
 *
 * Entity list is intentionally empty at Step 0.
 * Entities will be added in Step 4 (Room Local Database).
 *
 * Increment [version] whenever schema changes and provide a [Migration]
 * or use [fallbackToDestructiveMigration] in development only.
 *
 * ┌──────────────────────────────────────────────────────────────┐
 * │  Architecture note                                           │
 * │                                                              │
 * │  The UI must NEVER interact with the database directly.      │
 * │  Access pattern:                                             │
 * │    Compose UI → ViewModel → UseCase → Repository → DAO      │
 * └──────────────────────────────────────────────────────────────┘
 */
@Database(
    entities = [DbMetaEntity::class],  // Placeholder — replaced by full entity set in Step 4
    version  = 1,
    exportSchema = true
)
@TypeConverters(SaveBetterTypeConverters::class)
abstract class SaveBetterDatabase : RoomDatabase() {
    // DAOs will be added here in Step 4 as entities are created.
    // Example: abstract fun expenseDao(): ExpenseDao
}
