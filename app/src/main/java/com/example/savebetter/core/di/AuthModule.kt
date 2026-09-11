package com.example.savebetter.core.di

import com.example.savebetter.core.auth.repository.AuthRepository
import com.example.savebetter.core.data.remote.auth.AuthRemoteDataSource
import com.example.savebetter.core.data.remote.firebase.FirebaseAuthRemoteDataSource
import com.example.savebetter.core.data.repository.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding authentication interfaces to concrete implementations.
 *
 * When swapping Firebase for Django, only this module's binding for
 * [AuthRemoteDataSource] needs to be replaced.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRemoteDataSource(
        impl: FirebaseAuthRemoteDataSource
    ): AuthRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}
