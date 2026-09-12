package com.example.savebetter.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.savebetter.core.auth.repository.AuthRepository
import com.example.savebetter.core.domain.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Background worker responsible for synchronizing local database changes with remote backend.
 *
 * Runs via WorkManager with network constraints. Operates exclusively through
 * [SyncRepository] without any direct knowledge of UI or Firebase.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val authRepository: AuthRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME_PERIODIC = "SaveBetterPeriodicSyncWork"
        const val WORK_NAME_IMMEDIATE = "SaveBetterImmediateSyncWork"
        const val KEY_USER_ID = "sync_user_id"
    }

    override suspend fun doWork(): Result {
        val userId = inputData.getString(KEY_USER_ID)
            ?: authRepository.observeAuthState().first()?.id
            ?: return Result.success() // No user logged in, nothing to sync

        return try {
            val syncResult = syncRepository.syncAll(userId)
            if (syncResult.isSuccess) {
                Result.success()
            } else {
                if (runAttemptCount < 3) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
