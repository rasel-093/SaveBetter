package com.example.savebetter.core.domain.model

import java.time.Instant

/**
 * Backend-agnostic domain model for an expense transaction.
 *
 * Stored amounts use minor units (poisha / cents, Long) to prevent
 * floating-point precision issues.
 */
data class Expense(
    val id: String,
    val userId: String,
    val amountMinor: Long,
    val categoryId: String,
    val note: String? = null,
    val date: Instant,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val syncStatus: SyncState = SyncState.SYNCED,
    val isDeleted: Boolean = false,
    val deletedAt: Instant? = null
)
