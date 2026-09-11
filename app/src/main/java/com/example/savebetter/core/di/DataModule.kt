package com.example.savebetter.core.di

import com.example.savebetter.core.data.remote.CategoryRemoteDataSource
import com.example.savebetter.core.data.remote.DebtCreditRemoteDataSource
import com.example.savebetter.core.data.remote.ExpenseRemoteDataSource
import com.example.savebetter.core.data.remote.TargetRemoteDataSource
import com.example.savebetter.core.data.remote.UserProfileRemoteDataSource
import com.example.savebetter.core.data.remote.firebase.FirebaseCategoryRemoteDataSource
import com.example.savebetter.core.data.remote.firebase.FirebaseDebtCreditRemoteDataSource
import com.example.savebetter.core.data.remote.firebase.FirebaseExpenseRemoteDataSource
import com.example.savebetter.core.data.remote.firebase.FirebaseTargetRemoteDataSource
import com.example.savebetter.core.data.remote.firebase.FirebaseUserProfileRemoteDataSource
import com.example.savebetter.core.data.repository.CategoryRepositoryImpl
import com.example.savebetter.core.data.repository.DebtCreditRepositoryImpl
import com.example.savebetter.core.data.repository.ExpenseRepositoryImpl
import com.example.savebetter.core.data.repository.TargetRepositoryImpl
import com.example.savebetter.core.data.repository.UserProfileRepositoryImpl
import com.example.savebetter.core.domain.repository.CategoryRepository
import com.example.savebetter.core.domain.repository.DebtCreditRepository
import com.example.savebetter.core.domain.repository.ExpenseRepository
import com.example.savebetter.core.domain.repository.TargetRepository
import com.example.savebetter.core.domain.repository.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding repository and remote data source interfaces to their implementations.
 *
 * To swap from Firebase to Django REST API in the future:
 * Only change the RemoteDataSource bindings here to Django implementations.
 * UI, ViewModels, Use Cases, and Repositories remain completely unchanged.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    // ── Repository Bindings ──────────────────────────────────────────────────
    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        impl: UserProfileRepositoryImpl
    ): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        impl: ExpenseRepositoryImpl
    ): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTargetRepository(
        impl: TargetRepositoryImpl
    ): TargetRepository

    @Binds
    @Singleton
    abstract fun bindDebtCreditRepository(
        impl: DebtCreditRepositoryImpl
    ): DebtCreditRepository

    // ── Remote Data Source Bindings ──────────────────────────────────────────
    @Binds
    @Singleton
    abstract fun bindUserProfileRemoteDataSource(
        impl: FirebaseUserProfileRemoteDataSource
    ): UserProfileRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindExpenseRemoteDataSource(
        impl: FirebaseExpenseRemoteDataSource
    ): ExpenseRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindCategoryRemoteDataSource(
        impl: FirebaseCategoryRemoteDataSource
    ): CategoryRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindTargetRemoteDataSource(
        impl: FirebaseTargetRemoteDataSource
    ): TargetRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindDebtCreditRemoteDataSource(
        impl: FirebaseDebtCreditRemoteDataSource
    ): DebtCreditRemoteDataSource
}
