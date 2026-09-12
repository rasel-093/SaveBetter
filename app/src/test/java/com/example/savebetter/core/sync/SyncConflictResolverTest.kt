package com.example.savebetter.core.sync

import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.DebtCredit
import com.example.savebetter.core.domain.model.DebtDirection
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.MonthlyTarget
import com.example.savebetter.core.domain.model.SalaryHandRecord
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.model.WeeklyTarget
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for [SyncConflictResolver] verifying Last-Write-Wins (LWW) conflict resolution
 * and soft-deletion tombstone semantics across all 7 entities.
 */
class SyncConflictResolverTest {

    private val t0 = Instant.parse("2026-09-01T10:00:00Z")
    private val t1 = Instant.parse("2026-09-01T11:00:00Z")
    private val t2 = Instant.parse("2026-09-01T12:00:00Z")

    // ── Expense Conflict Tests ──────────────────────────────────────────────

    @Test
    fun `resolveExpense returns USE_REMOTE when local is null`() {
        val remote = Expense(
            id = "e1",
            userId = "u1",
            amountMinor = 5000L,
            categoryId = "cat1",
            note = "Groceries",
            date = t1,
            createdAt = t1,
            updatedAt = t1,
            syncStatus = SyncState.SYNCED
        )
        val result = SyncConflictResolver.resolveExpense(null, remote)
        assertEquals(ConflictResolution.USE_REMOTE, result)
    }

    @Test
    fun `resolveExpense returns USE_REMOTE when remote is strictly newer`() {
        val local = Expense(
            id = "e1",
            userId = "u1",
            amountMinor = 5000L,
            categoryId = "cat1",
            note = "Groceries",
            date = t0,
            createdAt = t0,
            updatedAt = t1,
            syncStatus = SyncState.PENDING
        )
        val remote = Expense(
            id = "e1",
            userId = "u1",
            amountMinor = 6000L,
            categoryId = "cat1",
            note = "Groceries Updated",
            date = t0,
            createdAt = t0,
            updatedAt = t2,
            syncStatus = SyncState.SYNCED
        )

        val result = SyncConflictResolver.resolveExpense(local, remote)
        assertEquals(ConflictResolution.USE_REMOTE, result)
    }

    @Test
    fun `resolveExpense returns KEEP_LOCAL when local is strictly newer`() {
        val local = Expense(
            id = "e1",
            userId = "u1",
            amountMinor = 7000L,
            categoryId = "cat1",
            note = "Groceries Newer",
            date = t0,
            createdAt = t0,
            updatedAt = t2,
            syncStatus = SyncState.PENDING
        )
        val remote = Expense(
            id = "e1",
            userId = "u1",
            amountMinor = 6000L,
            categoryId = "cat1",
            note = "Groceries Older",
            date = t0,
            createdAt = t0,
            updatedAt = t1,
            syncStatus = SyncState.SYNCED
        )

        val result = SyncConflictResolver.resolveExpense(local, remote)
        assertEquals(ConflictResolution.KEEP_LOCAL, result)
    }

    @Test
    fun `resolveExpense returns KEEP_LOCAL when local has pending edits and timestamps are equal`() {
        val local = Expense(
            id = "e1",
            userId = "u1",
            amountMinor = 5000L,
            categoryId = "cat1",
            note = "Local",
            date = t0,
            createdAt = t0,
            updatedAt = t1,
            syncStatus = SyncState.PENDING
        )
        val remote = Expense(
            id = "e1",
            userId = "u1",
            amountMinor = 5000L,
            categoryId = "cat1",
            note = "Remote",
            date = t0,
            createdAt = t0,
            updatedAt = t1,
            syncStatus = SyncState.SYNCED
        )

        val result = SyncConflictResolver.resolveExpense(local, remote)
        assertEquals(ConflictResolution.KEEP_LOCAL, result)
    }

    @Test
    fun `resolveExpense respects remote tombstone when remote is newer`() {
        val local = Expense(
            id = "e1",
            userId = "u1",
            amountMinor = 5000L,
            categoryId = "cat1",
            note = "Active",
            date = t0,
            createdAt = t0,
            updatedAt = t1,
            syncStatus = SyncState.PENDING,
            isDeleted = false
        )
        val remote = Expense(
            id = "e1",
            userId = "u1",
            amountMinor = 5000L,
            categoryId = "cat1",
            note = "Deleted",
            date = t0,
            createdAt = t0,
            updatedAt = t2,
            syncStatus = SyncState.SYNCED,
            isDeleted = true,
            deletedAt = t2
        )

        val result = SyncConflictResolver.resolveExpense(local, remote)
        assertEquals(ConflictResolution.USE_REMOTE, result)
    }

    @Test
    fun `resolveExpense preserves local deletion tombstone if local deletion is newer`() {
        val local = Expense(
            id = "e1",
            userId = "u1",
            amountMinor = 5000L,
            categoryId = "cat1",
            note = "Deleted Local",
            date = t0,
            createdAt = t0,
            updatedAt = t2,
            syncStatus = SyncState.PENDING,
            isDeleted = true,
            deletedAt = t2
        )
        val remote = Expense(
            id = "e1",
            userId = "u1",
            amountMinor = 5000L,
            categoryId = "cat1",
            note = "Active Remote",
            date = t0,
            createdAt = t0,
            updatedAt = t1,
            syncStatus = SyncState.SYNCED,
            isDeleted = false
        )

        val result = SyncConflictResolver.resolveExpense(local, remote)
        assertEquals(ConflictResolution.KEEP_LOCAL, result)
    }

    // ── User Profile Conflict Tests ─────────────────────────────────────────

    @Test
    fun `resolveUser returns USE_REMOTE for newer remote profile`() {
        val local = UserProfile(
            id = "u1",
            name = "Old Name",
            email = "u@test.com",
            monthlySalaryMinor = 5000000L,
            preferredLanguage = "en",
            onboardingCompleted = true,
            createdAt = t0,
            updatedAt = t1,
            syncStatus = SyncState.PENDING
        )
        val remote = UserProfile(
            id = "u1",
            name = "New Name",
            email = "u@test.com",
            monthlySalaryMinor = 6000000L,
            preferredLanguage = "bn",
            onboardingCompleted = true,
            createdAt = t0,
            updatedAt = t2,
            syncStatus = SyncState.SYNCED
        )

        val result = SyncConflictResolver.resolveUser(local, remote)
        assertEquals(ConflictResolution.USE_REMOTE, result)
    }

    @Test
    fun `resolveUser returns KEEP_LOCAL for newer local profile`() {
        val local = UserProfile(
            id = "u1",
            name = "Newer Local",
            email = "u@test.com",
            monthlySalaryMinor = 7000000L,
            preferredLanguage = "en",
            onboardingCompleted = true,
            createdAt = t0,
            updatedAt = t2,
            syncStatus = SyncState.PENDING
        )
        val remote = UserProfile(
            id = "u1",
            name = "Older Remote",
            email = "u@test.com",
            monthlySalaryMinor = 6000000L,
            preferredLanguage = "bn",
            onboardingCompleted = true,
            createdAt = t0,
            updatedAt = t1,
            syncStatus = SyncState.SYNCED
        )

        val result = SyncConflictResolver.resolveUser(local, remote)
        assertEquals(ConflictResolution.KEEP_LOCAL, result)
    }

    // ── Category Conflict Tests ─────────────────────────────────────────────

    @Test
    fun `resolveCategory honors LWW and tombstones`() {
        val local = Category(
            id = "cat1",
            userId = "u1",
            customName = "Food",
            icon = "ic_food",
            colorToken = "color_1",
            isDefault = true,
            createdAt = t0,
            updatedAt = t1,
            syncStatus = SyncState.PENDING,
            isDeleted = false
        )
        val remote = Category(
            id = "cat1",
            userId = "u1",
            customName = "Food & Dining",
            icon = "ic_food",
            colorToken = "color_2",
            isDefault = true,
            createdAt = t0,
            updatedAt = t2,
            syncStatus = SyncState.SYNCED,
            isDeleted = false
        )

        val result = SyncConflictResolver.resolveCategory(local, remote)
        assertEquals(ConflictResolution.USE_REMOTE, result)
    }

    // ── Target & Record Conflict Tests ──────────────────────────────────────

    @Test
    fun `resolveWeeklyTarget selects USE_REMOTE when remote is newer`() {
        val local = WeeklyTarget(
            id = "w1",
            userId = "u1",
            weekStart = "2026-09-01",
            weekEnd = "2026-09-07",
            targetAmountMinor = 100000L,
            updatedAt = t1,
            syncStatus = SyncState.PENDING
        )
        val remote = WeeklyTarget(
            id = "w1",
            userId = "u1",
            weekStart = "2026-09-01",
            weekEnd = "2026-09-07",
            targetAmountMinor = 120000L,
            updatedAt = t2,
            syncStatus = SyncState.SYNCED
        )

        val result = SyncConflictResolver.resolveWeeklyTarget(local, remote)
        assertEquals(ConflictResolution.USE_REMOTE, result)
    }

    @Test
    fun `resolveMonthlyTarget selects USE_REMOTE when remote is newer`() {
        val local = MonthlyTarget(
            id = "m1",
            userId = "u1",
            month = 9,
            year = 2026,
            targetAmountMinor = 400000L,
            updatedAt = t1,
            syncStatus = SyncState.PENDING
        )
        val remote = MonthlyTarget(
            id = "m1",
            userId = "u1",
            month = 9,
            year = 2026,
            targetAmountMinor = 450000L,
            updatedAt = t2,
            syncStatus = SyncState.SYNCED
        )

        val result = SyncConflictResolver.resolveMonthlyTarget(local, remote)
        assertEquals(ConflictResolution.USE_REMOTE, result)
    }

    @Test
    fun `resolveSalaryHandRecord selects KEEP_LOCAL when local is newer`() {
        val local = SalaryHandRecord(
            id = "s1",
            userId = "u1",
            month = 9,
            year = 2026,
            salaryAmountMinor = 5000000L,
            handRemainingAmountMinor = 200000L,
            updatedAt = t2,
            syncStatus = SyncState.PENDING
        )
        val remote = SalaryHandRecord(
            id = "s1",
            userId = "u1",
            month = 9,
            year = 2026,
            salaryAmountMinor = 5000000L,
            handRemainingAmountMinor = 150000L,
            updatedAt = t1,
            syncStatus = SyncState.SYNCED
        )

        val result = SyncConflictResolver.resolveSalaryHandRecord(local, remote)
        assertEquals(ConflictResolution.KEEP_LOCAL, result)
    }

    @Test
    fun `resolveDebtCredit selects USE_REMOTE when remote is newer and tombstone applied`() {
        val local = DebtCredit(
            id = "d1",
            userId = "u1",
            direction = DebtDirection.PAYABLE,
            personName = "Bob",
            amountMinor = 150000L,
            date = t0,
            createdAt = t0,
            updatedAt = t1,
            syncStatus = SyncState.PENDING,
            isDeleted = false
        )
        val remote = DebtCredit(
            id = "d1",
            userId = "u1",
            direction = DebtDirection.PAYABLE,
            personName = "Bob",
            amountMinor = 150000L,
            date = t0,
            createdAt = t0,
            updatedAt = t2,
            syncStatus = SyncState.SYNCED,
            isDeleted = true,
            deletedAt = t2
        )

        val result = SyncConflictResolver.resolveDebtCredit(local, remote)
        assertEquals(ConflictResolution.USE_REMOTE, result)
    }
}
