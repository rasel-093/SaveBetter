package com.example.savebetter.core.sync

import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.DebtCredit
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.MonthlyTarget
import com.example.savebetter.core.domain.model.SalaryHandRecord
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.model.WeeklyTarget

/**
 * Resolution decision for sync conflict evaluation.
 */
enum class ConflictResolution {
    USE_REMOTE,
    KEEP_LOCAL
}

/**
 * Conflict resolution engine for multi-device data synchronization.
 *
 * Conflict Resolution Strategy:
 * 1. Server-Authoritative Last-Write-Wins (LWW) with Timestamp Comparison.
 *    - Each record tracks `updatedAt: Instant`.
 *    - If local record is untouched ([SyncState.SYNCED]), any remote update is applied.
 *    - If local record has concurrent pending edits ([SyncState.PENDING]):
 *      - If remote is strictly newer (`remote.updatedAt > local.updatedAt`), remote wins.
 *      - If local is newer or equal (`local.updatedAt >= remote.updatedAt`), local wins
 *        (kept as PENDING so it can be pushed to remote).
 * 2. Tombstone / Soft-Deletion Synchronization:
 *    - Deletions are represented by `isDeleted = true` and `deletedAt: Instant?`.
 *    - If remote record is deleted and its deletion timestamp is newer or equal to local update,
 *      deletion wins (local is marked deleted).
 *    - If local record was deleted and is newer than remote, local tombstone wins.
 */
object SyncConflictResolver {

    fun resolveUser(local: UserProfile?, remote: UserProfile): ConflictResolution {
        if (local == null) return ConflictResolution.USE_REMOTE
        if (local.syncStatus == SyncState.SYNCED) return ConflictResolution.USE_REMOTE
        return if (remote.updatedAt.isAfter(local.updatedAt)) {
            ConflictResolution.USE_REMOTE
        } else {
            ConflictResolution.KEEP_LOCAL
        }
    }

    fun resolveExpense(local: Expense?, remote: Expense): ConflictResolution {
        if (local == null) return ConflictResolution.USE_REMOTE
        if (local.syncStatus == SyncState.SYNCED) return ConflictResolution.USE_REMOTE

        // If remote is deleted, check deletion timestamp vs local update
        if (remote.isDeleted) {
            val remoteDeletedTime = remote.deletedAt ?: remote.updatedAt
            return if (!local.updatedAt.isAfter(remoteDeletedTime)) {
                ConflictResolution.USE_REMOTE
            } else {
                ConflictResolution.KEEP_LOCAL
            }
        }

        // If local is deleted, check local deletion timestamp vs remote update
        if (local.isDeleted) {
            val localDeletedTime = local.deletedAt ?: local.updatedAt
            return if (remote.updatedAt.isAfter(localDeletedTime)) {
                ConflictResolution.USE_REMOTE
            } else {
                ConflictResolution.KEEP_LOCAL
            }
        }

        return if (remote.updatedAt.isAfter(local.updatedAt)) {
            ConflictResolution.USE_REMOTE
        } else {
            ConflictResolution.KEEP_LOCAL
        }
    }

    fun resolveCategory(local: Category?, remote: Category): ConflictResolution {
        if (local == null) return ConflictResolution.USE_REMOTE
        if (local.syncStatus == SyncState.SYNCED) return ConflictResolution.USE_REMOTE

        if (remote.isDeleted && !local.updatedAt.isAfter(remote.updatedAt)) {
            return ConflictResolution.USE_REMOTE
        }

        return if (remote.updatedAt.isAfter(local.updatedAt)) {
            ConflictResolution.USE_REMOTE
        } else {
            ConflictResolution.KEEP_LOCAL
        }
    }

    fun resolveWeeklyTarget(local: WeeklyTarget?, remote: WeeklyTarget): ConflictResolution {
        if (local == null) return ConflictResolution.USE_REMOTE
        if (local.syncStatus == SyncState.SYNCED) return ConflictResolution.USE_REMOTE
        return if (remote.updatedAt.isAfter(local.updatedAt)) {
            ConflictResolution.USE_REMOTE
        } else {
            ConflictResolution.KEEP_LOCAL
        }
    }

    fun resolveMonthlyTarget(local: MonthlyTarget?, remote: MonthlyTarget): ConflictResolution {
        if (local == null) return ConflictResolution.USE_REMOTE
        if (local.syncStatus == SyncState.SYNCED) return ConflictResolution.USE_REMOTE
        return if (remote.updatedAt.isAfter(local.updatedAt)) {
            ConflictResolution.USE_REMOTE
        } else {
            ConflictResolution.KEEP_LOCAL
        }
    }

    fun resolveSalaryHandRecord(local: SalaryHandRecord?, remote: SalaryHandRecord): ConflictResolution {
        if (local == null) return ConflictResolution.USE_REMOTE
        if (local.syncStatus == SyncState.SYNCED) return ConflictResolution.USE_REMOTE
        return if (remote.updatedAt.isAfter(local.updatedAt)) {
            ConflictResolution.USE_REMOTE
        } else {
            ConflictResolution.KEEP_LOCAL
        }
    }

    fun resolveDebtCredit(local: DebtCredit?, remote: DebtCredit): ConflictResolution {
        if (local == null) return ConflictResolution.USE_REMOTE
        if (local.syncStatus == SyncState.SYNCED) return ConflictResolution.USE_REMOTE

        if (remote.isDeleted) {
            val remoteDeletedTime = remote.deletedAt ?: remote.updatedAt
            return if (!local.updatedAt.isAfter(remoteDeletedTime)) {
                ConflictResolution.USE_REMOTE
            } else {
                ConflictResolution.KEEP_LOCAL
            }
        }

        if (local.isDeleted) {
            val localDeletedTime = local.deletedAt ?: local.updatedAt
            return if (remote.updatedAt.isAfter(localDeletedTime)) {
                ConflictResolution.USE_REMOTE
            } else {
                ConflictResolution.KEEP_LOCAL
            }
        }

        return if (remote.updatedAt.isAfter(local.updatedAt)) {
            ConflictResolution.USE_REMOTE
        } else {
            ConflictResolution.KEEP_LOCAL
        }
    }
}
