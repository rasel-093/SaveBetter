package com.example.savebetter.core.sync

import com.example.savebetter.core.designsystem.component.SyncStatus
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for managing background synchronization schedules, triggers,
 * and observing real-time sync status for UI indicators.
 */
interface SyncManager {

    /**
     * Observable sync status (Synced, Syncing, Offline, Error) for UI display.
     */
    val syncStatus: StateFlow<SyncStatus>

    /**
     * Enqueues a periodic background sync job (15-minute intervals with connected network constraint).
     */
    fun schedulePeriodicSync()

    /**
     * Enqueues an immediate background sync job with connected network constraint.
     */
    fun requestImmediateSync()

    /**
     * Cancels all scheduled sync jobs (used on logout and account deletion).
     */
    fun cancelAllSync()

    /**
     * Manually triggers an immediate synchronization synchronously and returns the result.
     */
    suspend fun syncNow(): Result<Unit>
}
