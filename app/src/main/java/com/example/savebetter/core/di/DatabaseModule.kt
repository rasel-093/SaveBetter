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

import com.example.savebetter.core.data.local.dao.CategoryDao
import com.example.savebetter.core.data.local.dao.DebtCreditDao
import com.example.savebetter.core.data.local.dao.ExpenseDao
import com.example.savebetter.core.data.local.dao.MonthlyTargetDao
import com.example.savebetter.core.data.local.dao.SalaryHandRecordDao
import com.example.savebetter.core.data.local.dao.UserDao
import com.example.savebetter.core.data.local.dao.WeeklyTargetDao

/**
 * Hilt module that provides the Room database instance and all DAOs.
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
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    @Provides
    fun provideUserDao(db: SaveBetterDatabase): UserDao = db.userDao()

    @Provides
    fun provideExpenseDao(db: SaveBetterDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideCategoryDao(db: SaveBetterDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideWeeklyTargetDao(db: SaveBetterDatabase): WeeklyTargetDao = db.weeklyTargetDao()

    @Provides
    fun provideMonthlyTargetDao(db: SaveBetterDatabase): MonthlyTargetDao = db.monthlyTargetDao()

    @Provides
    fun provideSalaryHandRecordDao(db: SaveBetterDatabase): SalaryHandRecordDao = db.salaryHandRecordDao()

    @Provides
    fun provideDebtCreditDao(db: SaveBetterDatabase): DebtCreditDao = db.debtCreditDao()
}
