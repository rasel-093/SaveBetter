package com.example.savebetter.core.di

import com.example.savebetter.core.sync.SyncManager
import com.example.savebetter.core.sync.SyncManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindSyncManager(
        impl: SyncManagerImpl
    ): SyncManager
}
