package com.example.savebetter.core.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for synchronizing local Room database with remote data source.
 *
 * Implements two-way synchronization:
 * - Push: uploads pending local changes to remote backend
 * - Pull: downloads remote changes and resolves conflicts using LWW
 */
interface SyncRepository {

    /**
     * Performs a full synchronization (push pending, then pull remote) for the given user.
     */
    suspend fun syncAll(userId: String): Result<Unit>

    /**
     * Pushes all pending local changes for the 7 entities to the remote backend.
     */
    suspend fun pushPending(userId: String): Result<Unit>

    /**
     * Pulls remote changes for all 7 entities and merges them using Last-Write-Wins conflict resolution.
     */
    suspend fun pullRemote(userId: String): Result<Unit>

    /**
     * Observes the total number of pending (unsynced) items across all entities for a user.
     */
    fun observeTotalPendingCount(userId: String): Flow<Int>
}
