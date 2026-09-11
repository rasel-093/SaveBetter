package com.example.savebetter.core.domain.model

/**
 * Synchronization state for local entities and domain models.
 *
 * Tracks whether a locally created or modified record has successfully
 * synchronised with the remote data source.
 */
enum class SyncState {
    SYNCED,
    PENDING,
    SYNCING,
    ERROR
}
