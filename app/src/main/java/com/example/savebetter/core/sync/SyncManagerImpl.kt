package com.example.savebetter.core.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.savebetter.core.auth.repository.AuthRepository
import com.example.savebetter.core.designsystem.component.SyncStatus
import com.example.savebetter.core.domain.repository.SyncRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncRepository: SyncRepository,
    private val authRepository: AuthRepository
) : SyncManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isOnline = MutableStateFlow(checkInitialConnectivity())
    private val _isManualSyncing = MutableStateFlow(false)
    private val _isWorkerRunning = MutableStateFlow(false)
    private val _hasSyncError = MutableStateFlow(false)

    init {
        registerNetworkCallback()
        observeWorkManager()
    }

    override val syncStatus: StateFlow<SyncStatus> = combine(
        _isOnline,
        _isManualSyncing,
        _isWorkerRunning,
        _hasSyncError
    ) { online, manualSyncing, workerRunning, hasError ->
        when {
            !online -> SyncStatus.Offline
            manualSyncing || workerRunning -> SyncStatus.Syncing
            hasError -> SyncStatus.Error
            else -> SyncStatus.Synced
        }
    }.stateIn(scope, SharingStarted.Eagerly, if (_isOnline.value) SyncStatus.Synced else SyncStatus.Offline)

    override fun schedulePeriodicSync() {
        runCatching {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SyncWorker.WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }
    }

    override fun requestImmediateSync() {
        runCatching {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val immediateRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                SyncWorker.WORK_NAME_IMMEDIATE,
                ExistingWorkPolicy.REPLACE,
                immediateRequest
            )
        }
    }

    override fun cancelAllSync() {
        runCatching {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(SyncWorker.WORK_NAME_PERIODIC)
            workManager.cancelUniqueWork(SyncWorker.WORK_NAME_IMMEDIATE)
        }
    }

    override suspend fun syncNow(): Result<Unit> {
        val currentUser = authRepository.observeAuthState().first()
            ?: return Result.failure(IllegalStateException("No user authenticated"))

        _isManualSyncing.value = true
        return try {
            val result = syncRepository.syncAll(currentUser.id)
            _hasSyncError.value = result.isFailure
            result
        } catch (e: Exception) {
            _hasSyncError.value = true
            Result.failure(e)
        } finally {
            _isManualSyncing.value = false
        }
    }

    private fun checkInitialConnectivity(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return true
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun registerNetworkCallback() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(
            networkRequest,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val wasOffline = !_isOnline.value
                    _isOnline.value = true
                    if (wasOffline) {
                        // Trigger immediate sync upon reconnection
                        requestImmediateSync()
                    }
                }

                override fun onLost(network: Network) {
                    _isOnline.value = false
                }
            }
        )
    }

    private fun observeWorkManager() {
        runCatching {
            val workManager = WorkManager.getInstance(context)
            scope.launch {
                workManager.getWorkInfosForUniqueWorkFlow(SyncWorker.WORK_NAME_IMMEDIATE)
                    .collect { workInfos ->
                        updateWorkerState(workInfos)
                    }
            }
            scope.launch {
                workManager.getWorkInfosForUniqueWorkFlow(SyncWorker.WORK_NAME_PERIODIC)
                    .collect { workInfos ->
                        updateWorkerState(workInfos)
                    }
            }
        }
    }

    private fun updateWorkerState(workInfos: List<WorkInfo>) {
        val isRunning = workInfos.any { it.state == WorkInfo.State.RUNNING }
        val hasFailed = workInfos.any { it.state == WorkInfo.State.FAILED }
        val hasSucceeded = workInfos.any { it.state == WorkInfo.State.SUCCEEDED }

        _isWorkerRunning.value = isRunning
        if (hasFailed) {
            _hasSyncError.value = true
        } else if (hasSucceeded) {
            _hasSyncError.value = false
        }
    }
}
