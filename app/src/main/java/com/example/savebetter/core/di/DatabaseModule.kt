package com.example.savebetter.core.di

import android.content.Context
import androidx.room.Room
import com.example.savebetter.core.data.local.SaveBetterDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides the Room database instance and all DAOs.
 *
 * The database is a singleton — one instance for the entire application lifetime.
 *
 * DAOs will be provided here as they are created in Step 4.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSaveBetterDatabase(
        @ApplicationContext context: Context
    ): SaveBetterDatabase =
        Room.databaseBuilder(
            context,
            SaveBetterDatabase::class.java,
            "savebetter.db"
        )
        // Step 4 will add migrations here; for now use destructive migration
        // during development only.
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    // DAOs will be provided here in Step 4:
    // @Provides fun provideExpenseDao(db: SaveBetterDatabase): ExpenseDao = db.expenseDao()
}
