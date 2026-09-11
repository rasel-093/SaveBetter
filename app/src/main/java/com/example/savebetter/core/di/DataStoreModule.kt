package com.example.savebetter.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.savebetter.core.data.local.userPreferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides the DataStore<Preferences> singleton.
 *
 * DataStore stores lightweight user preferences:
 *   - language selection
 *   - theme mode
 *   - notification toggles
 *   - vibration preference
 *
 * User-specific financial data is stored in Room, not DataStore.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.userPreferencesDataStore
}
