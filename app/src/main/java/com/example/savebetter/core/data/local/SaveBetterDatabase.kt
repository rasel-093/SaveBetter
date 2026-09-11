package com.example.savebetter.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import com.example.savebetter.core.data.local.dao.CategoryDao
import com.example.savebetter.core.data.local.dao.DebtCreditDao
import com.example.savebetter.core.data.local.dao.ExpenseDao
import com.example.savebetter.core.data.local.dao.MonthlyTargetDao
import com.example.savebetter.core.data.local.dao.SalaryHandRecordDao
import com.example.savebetter.core.data.local.dao.UserDao
import com.example.savebetter.core.data.local.dao.WeeklyTargetDao
import com.example.savebetter.core.data.local.entity.CategoryEntity
import com.example.savebetter.core.data.local.entity.DebtCreditEntity
import com.example.savebetter.core.data.local.entity.ExpenseEntity
import com.example.savebetter.core.data.local.entity.MonthlyTargetEntity
import com.example.savebetter.core.data.local.entity.SalaryHandRecordEntity
import com.example.savebetter.core.data.local.entity.UserEntity
import com.example.savebetter.core.data.local.entity.WeeklyTargetEntity

/**
 * SaveBetter local Room database.
 *
 * This is the central Room database for the application. It is the
 * *local source of truth* — the UI observes Flows backed by this database.
 * Remote data (Firebase now, Django later) is synchronised *into* this database
 * by the SyncWorker.
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
    entities = [
        UserEntity::class,
        ExpenseEntity::class,
        CategoryEntity::class,
        WeeklyTargetEntity::class,
        MonthlyTargetEntity::class,
        SalaryHandRecordEntity::class,
        DebtCreditEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(SaveBetterTypeConverters::class)
abstract class SaveBetterDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun weeklyTargetDao(): WeeklyTargetDao
    abstract fun monthlyTargetDao(): MonthlyTargetDao
    abstract fun salaryHandRecordDao(): SalaryHandRecordDao
    abstract fun debtCreditDao(): DebtCreditDao
}
