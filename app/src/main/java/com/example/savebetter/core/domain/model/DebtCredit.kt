package com.example.savebetter.core.domain.model

import java.time.Instant

/**
 * Direction of debt/credit entry.
 */
enum class DebtDirection {
    RECEIVABLE, // Lent to someone / to receive (PAWNA)
    PAYABLE     // Borrowed from someone / to pay (DENA)
}

/**
 * Backend-agnostic domain model for Debt/Credit management.
 */
data class DebtCredit(
    val id: String,
    val userId: String,
    val direction: DebtDirection,
    val personName: String,
    val amountMinor: Long,
    val note: String? = null,
    val date: Instant,
    val dueDate: Instant? = null,
    val isSettled: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val syncStatus: SyncState = SyncState.SYNCED,
    val isDeleted: Boolean = false,
    val deletedAt: Instant? = null
)
