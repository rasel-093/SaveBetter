package com.example.savebetter.core.domain.usecase.auth

import android.content.Context
import com.example.savebetter.core.auth.repository.AuthRepository
import com.example.savebetter.core.data.local.SaveBetterDatabase
import com.example.savebetter.core.data.local.SettingsPreferences
import com.example.savebetter.core.domain.repository.UserProfileRepository
import com.example.savebetter.core.notification.ReconciliationReminderScheduler

import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case coordinating permanent account deletion.
 *
 * Sequence of operations:
 * 1. If password provided, re-authenticates to refresh sensitive auth credentials.
 * 2. Deletes remote user data from remote service (Firestore collections under /users/{userId}).
 * 3. Deletes Firebase Authentication account.
 * 4. Clears all local Room database tables for this user and calls clearAllTables().
 * 5. Clears user DataStore preferences.
 * 6. Cancels scheduled WorkManager background jobs.
 *
 * Firebase-specific classes are never exposed outside the Firebase data layer.
 */
@Singleton
class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val settingsPreferences: SettingsPreferences,
    private val database: SaveBetterDatabase,
    @ApplicationContext private val context: Context
) {

    suspend operator fun invoke(
        userId: String,
        password: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // 1. If password provided, re-authenticate first to ensure token freshness
            if (!password.isNullOrBlank()) {
                authRepository.reauthenticate(password).getOrThrow()
            }

            // 2. Delete remote user data (Firestore / remote API)
            userProfileRepository.deleteUserData(userId).getOrThrow()

            // 3. Delete remote authentication account (Firebase Auth)
            authRepository.deleteAccount().getOrThrow()

            // 4. Clear local Room database tables
            database.userDao().deleteUser(userId)
            database.expenseDao().deleteExpensesByUserId(userId)
            database.categoryDao().deleteCategoriesByUserId(userId)
            database.weeklyTargetDao().deleteWeeklyTargetsByUserId(userId)
            database.monthlyTargetDao().deleteMonthlyTargetsByUserId(userId)
            database.salaryHandRecordDao().deleteSalaryHandRecordsByUserId(userId)
            database.debtCreditDao().deleteDebtCreditsByUserId(userId)
            database.clearAllTables()

            // 5. Clear user-specific DataStore preferences
            settingsPreferences.clearAllPreferences()

            // 6. Cancel scheduled WorkManager jobs
            ReconciliationReminderScheduler.cancelAllUserWork(context)
            Unit
        }
    }
}


