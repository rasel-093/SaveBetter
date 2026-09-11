package com.example.savebetter.feature.reconciliation

import app.cash.turbine.test
import com.example.savebetter.core.auth.model.AuthUser
import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.ReconciliationStatus
import com.example.savebetter.core.domain.model.SalaryHandRecord
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.auth.repository.AuthRepository
import com.example.savebetter.core.domain.repository.CategoryRepository
import com.example.savebetter.core.domain.repository.ExpenseRepository
import com.example.savebetter.core.domain.repository.TargetRepository
import com.example.savebetter.core.domain.repository.UserProfileRepository
import com.example.savebetter.core.domain.usecase.dashboard.GetReconciliationSummaryUseCase
import com.example.savebetter.core.domain.usecase.dashboard.SaveSalaryHandRecordUseCase
import com.example.savebetter.core.domain.usecase.expense.AddExpenseUseCase
import com.example.savebetter.core.domain.usecase.i18n.GetLanguageUseCase
import com.example.savebetter.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.YearMonth

class ReconciliationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk()
    private val expenseRepository: ExpenseRepository = mockk(relaxed = true)
    private val targetRepository: TargetRepository = mockk(relaxed = true)
    private val userProfileRepository: UserProfileRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private val getLanguageUseCase: GetLanguageUseCase = mockk()

    private lateinit var getReconciliationSummaryUseCase: GetReconciliationSummaryUseCase
    private lateinit var saveSalaryHandRecordUseCase: SaveSalaryHandRecordUseCase
    private lateinit var addExpenseUseCase: AddExpenseUseCase
    private lateinit var viewModel: ReconciliationViewModel

    private val testUser = AuthUser(
        id = "user_1",
        email = "test@example.com",
        displayName = "Test User"
    )

    private val testProfile = UserProfile(
        id = "user_1",
        name = "Test User",
        email = "test@example.com",
        monthlySalaryMinor = 5500000L // ৳55,000
    )

    private val nowYM = YearMonth.now()

    private val testExpenses = listOf(
        Expense(
            id = "exp_1",
            userId = "user_1",
            amountMinor = 3904500L, // ৳39,045
            categoryId = "cat_1",
            date = Instant.now(),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    )

    private val testSalaryRecord = SalaryHandRecord(
        id = "shr_1",
        userId = "user_1",
        year = nowYM.year,
        month = nowYM.monthValue,
        salaryAmountMinor = 5500000L, // ৳55,000
        handRemainingAmountMinor = 850000L // ৳8,500
    )

    private val testCategories = listOf(
        Category(
            id = "cat_other",
            userId = "user_1",
            nameKey = "category_other",
            customName = "Other",
            icon = "more",
            colorToken = "cat6",
            isDefault = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    )

    @Before
    fun setUp() {
        getReconciliationSummaryUseCase = GetReconciliationSummaryUseCase()
        saveSalaryHandRecordUseCase = SaveSalaryHandRecordUseCase(targetRepository)
        addExpenseUseCase = AddExpenseUseCase(expenseRepository)

        every { authRepository.observeAuthState() } returns flowOf(testUser)
        every { expenseRepository.observeExpenses("user_1") } returns flowOf(testExpenses)
        every { targetRepository.observeSalaryHandRecords("user_1") } returns flowOf(listOf(testSalaryRecord))
        every { userProfileRepository.observeUserProfile("user_1") } returns flowOf(testProfile)
        every { categoryRepository.observeCategories("user_1") } returns flowOf(testCategories)
        every { getLanguageUseCase() } returns flowOf(AppLanguage.ENGLISH)
        coEvery { targetRepository.getSalaryHandRecord(any(), any(), any()) } returns testSalaryRecord

        viewModel = ReconciliationViewModel(
            authRepository = authRepository,
            expenseRepository = expenseRepository,
            targetRepository = targetRepository,
            userProfileRepository = userProfileRepository,
            categoryRepository = categoryRepository,
            getLanguageUseCase = getLanguageUseCase,
            getReconciliationSummaryUseCase = getReconciliationSummaryUseCase,
            saveSalaryHandRecordUseCase = saveSalaryHandRecordUseCase,
            addExpenseUseCase = addExpenseUseCase
        )
        viewModel.initForUser("user_1")
    }

    @Test
    fun `emits calculated reconciliation matching Screen 07 mockup`() = runTest {
        viewModel.uiState.test {
            // Await non-empty state
            var state = awaitItem()
            while (state.summary.salaryMinor == 0L) {
                state = awaitItem()
            }

            assertEquals(5500000L, state.summary.salaryMinor)
            assertEquals(850000L, state.summary.handRemainingMinor)
            assertEquals(4650000L, state.summary.totalExpenseMinor) // ৳46,500
            assertEquals(3904500L, state.summary.appLoggedExpenseMinor) // ৳39,045
            assertEquals(745500L, state.summary.outOfNoteExpenseMinor) // ৳7,455
            assertEquals(ReconciliationStatus.UNRECORDED_EXPENSE, state.summary.status)
        }
    }

    @Test
    fun `recalculates reconciliation dynamically when user modifies cash in hand`() = runTest {
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.summary.salaryMinor == 0L) {
                state = awaitItem()
            }

            // User changes cash in hand to ৳15,955 (1,595,500 minor)
            // totalExpense = 55,000 - 15,955 = 39,045
            // outOfNote = 39,045 - 39,045 = 0 -> BALANCED
            viewModel.onHandRemainingChanged("15955")

            state = awaitItem()
            while (state.summary.handRemainingMinor != 1595500L) {
                state = awaitItem()
            }

            assertEquals(1595500L, state.summary.handRemainingMinor)
            assertEquals(3904500L, state.summary.totalExpenseMinor)
            assertEquals(0L, state.summary.outOfNoteExpenseMinor)
            assertEquals(ReconciliationStatus.BALANCED, state.summary.status)
        }
    }

    @Test
    fun `navigating previous and next month updates selected year month`() = runTest {
        val currentYM = YearMonth.now()
        val prevYM = currentYM.minusMonths(1)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.summary.salaryMinor == 0L) {
                state = awaitItem()
            }
            assertEquals(currentYM, state.selectedYearMonth)

            viewModel.navigatePreviousMonth()
            state = awaitItem()
            while (state.selectedYearMonth != prevYM) {
                state = awaitItem()
            }
            assertEquals(prevYM, state.selectedYearMonth)

            viewModel.navigateNextMonth()
            state = awaitItem()
            while (state.selectedYearMonth != currentYM) {
                state = awaitItem()
            }
            assertEquals(currentYM, state.selectedYearMonth)
        }
    }

    @Test
    fun `saveRecord calls TargetRepository with updated values`() = runTest {
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.summary.salaryMinor == 0L) {
                state = awaitItem()
            }

            viewModel.onSalaryChanged("60000")
            viewModel.onHandRemainingChanged("10000")

            state = awaitItem()
            while (state.summary.salaryMinor != 6000000L) {
                state = awaitItem()
            }

            viewModel.saveRecord()

            coVerify { targetRepository.saveSalaryHandRecord(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `quickLogDiscrepancy adds unrecorded expense to ExpenseRepository`() = runTest {
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.summary.salaryMinor == 0L) {
                state = awaitItem()
            }

            assertEquals(745500L, state.summary.outOfNoteExpenseMinor)

            viewModel.quickLogDiscrepancy()

            coVerify { expenseRepository.addExpense(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
