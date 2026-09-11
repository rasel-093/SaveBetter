package com.example.savebetter.core.domain.usecase.dashboard

import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.ReconciliationStatus
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant

class GetReconciliationSummaryUseCaseTest {

    private lateinit var useCase: GetReconciliationSummaryUseCase

    @Before
    fun setUp() {
        useCase = GetReconciliationSummaryUseCase()
    }

    @Test
    fun `calculates standard reconciliation matching Screen 07 mockup`() {
        // Screen 07 Scenario from mockup:
        // Salary / Income: ৳55,000 (5,500,000 minor)
        // Cash in hand: ৳8,500 (850,000 minor)
        // App-logged expenses: ৳39,045 (3,904,500 minor)
        // Expected:
        // Net Cash Outflow: ৳46,500 (4,650,000 minor)
        // Unrecorded Expense: ৳7,455 (745,500 minor)
        // Status: UNRECORDED_EXPENSE
        val expenses = listOf(
            Expense(
                id = "e1",
                userId = "u1",
                amountMinor = 3904500L,
                categoryId = "cat_1",
                date = Instant.parse("2026-09-10T10:00:00Z"),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )

        val result = useCase(
            year = 2026,
            month = 9,
            salaryMinor = 5500000L,
            handRemainingMinor = 850000L,
            expenses = expenses
        )

        assertEquals(5500000L, result.salaryMinor)
        assertEquals(850000L, result.handRemainingMinor)
        assertEquals(4650000L, result.totalExpenseMinor) // 55,000 - 8,500 = 46,500
        assertEquals(3904500L, result.appLoggedExpenseMinor) // 39,045
        assertEquals(745500L, result.outOfNoteExpenseMinor) // 46,500 - 39,045 = 7,455
        assertEquals(ReconciliationStatus.UNRECORDED_EXPENSE, result.status)
    }

    @Test
    fun `returns BALANCED status when cash in hand and expenses match income perfectly`() {
        // Salary: ৳50,000
        // Cash in hand: ৳10,000
        // Logged expenses: ৳40,000 (20k + 20k)
        val expenses = listOf(
            Expense(
                id = "e1",
                userId = "u1",
                amountMinor = 2000000L,
                categoryId = "cat_1",
                date = Instant.parse("2026-09-10T10:00:00Z"),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            ),
            Expense(
                id = "e2",
                userId = "u1",
                amountMinor = 2000000L,
                categoryId = "cat_2",
                date = Instant.parse("2026-09-12T10:00:00Z"),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )

        val result = useCase(
            year = 2026,
            month = 9,
            salaryMinor = 5000000L,
            handRemainingMinor = 1000000L,
            expenses = expenses
        )

        assertEquals(4000000L, result.totalExpenseMinor)
        assertEquals(4000000L, result.appLoggedExpenseMinor)
        assertEquals(0L, result.outOfNoteExpenseMinor)
        assertEquals(ReconciliationStatus.BALANCED, result.status)
    }

    @Test
    fun `returns DATA_MISMATCH and clamps negative out-of-note to zero`() {
        // Spec requirement: If outOfNoteExpense < 0, show a data mismatch warning
        // instead of displaying a negative out-of-note expense.
        // Starting income: ৳30,000
        // Cash in hand: ৳15,000 -> Net outflow = ৳15,000
        // App logged: ৳20,000 -> Discrepancy = 15,000 - 20,000 = -5,000
        val expenses = listOf(
            Expense(
                id = "e1",
                userId = "u1",
                amountMinor = 2000000L,
                categoryId = "cat_1",
                date = Instant.parse("2026-09-10T10:00:00Z"),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )

        val result = useCase(
            year = 2026,
            month = 9,
            salaryMinor = 3000000L,
            handRemainingMinor = 1500000L,
            expenses = expenses
        )

        assertEquals(1500000L, result.totalExpenseMinor)
        assertEquals(2000000L, result.appLoggedExpenseMinor)
        assertEquals(0L, result.outOfNoteExpenseMinor) // Must be clamped to 0
        assertEquals(ReconciliationStatus.DATA_MISMATCH, result.status)
    }

    @Test
    fun `excludes soft-deleted expenses from logged spending calculation`() {
        val expenses = listOf(
            Expense(
                id = "e1",
                userId = "u1",
                amountMinor = 1000000L,
                categoryId = "cat_1",
                date = Instant.parse("2026-09-10T10:00:00Z"),
                isDeleted = false,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            ),
            Expense(
                id = "e2_deleted",
                userId = "u1",
                amountMinor = 500000L,
                categoryId = "cat_1",
                date = Instant.parse("2026-09-11T10:00:00Z"),
                isDeleted = true, // Tombstone
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )

        val result = useCase(
            year = 2026,
            month = 9,
            salaryMinor = 2000000L,
            handRemainingMinor = 500000L,
            expenses = expenses
        )

        // totalExpense = 20,000 - 5,000 = 15,000
        // appLogged = 10,000 (excluding 5,000 tombstone)
        // outOfNote = 15,000 - 10,000 = 5,000
        assertEquals(1000000L, result.appLoggedExpenseMinor)
        assertEquals(500000L, result.outOfNoteExpenseMinor)
        assertEquals(ReconciliationStatus.UNRECORDED_EXPENSE, result.status)
    }
}
