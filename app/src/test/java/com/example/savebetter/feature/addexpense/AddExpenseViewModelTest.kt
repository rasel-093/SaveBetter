package com.example.savebetter.feature.addexpense

import app.cash.turbine.test
import com.example.savebetter.R
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.repository.CategoryRepository
import com.example.savebetter.core.domain.repository.ExpenseRepository
import com.example.savebetter.core.domain.usecase.expense.AddExpenseParams
import com.example.savebetter.core.domain.usecase.expense.AddExpenseUseCase
import com.example.savebetter.core.domain.usecase.expense.CreateCustomCategoryUseCase
import com.example.savebetter.core.domain.usecase.expense.DeleteExpenseUseCase
import com.example.savebetter.core.domain.usecase.expense.UpdateExpenseParams
import com.example.savebetter.core.domain.usecase.expense.UpdateExpenseUseCase
import com.example.savebetter.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class AddExpenseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val addExpenseUseCase: AddExpenseUseCase = mockk(relaxed = true)
    private val updateExpenseUseCase: UpdateExpenseUseCase = mockk(relaxed = true)
    private val deleteExpenseUseCase: DeleteExpenseUseCase = mockk(relaxed = true)
    private val createCustomCategoryUseCase: CreateCustomCategoryUseCase = mockk(relaxed = true)
    private val expenseRepository: ExpenseRepository = mockk(relaxed = true)
    private val categoryRepository: CategoryRepository = mockk(relaxed = true)

    private lateinit var viewModel: AddExpenseViewModel

    private val sampleCategories = listOf(
        Category(
            id = "cat_1",
            userId = "user_1",
            nameKey = "category_household",
            customName = null,
            icon = "home",
            colorToken = "cat1",
            isDefault = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        ),
        Category(
            id = "cat_2",
            userId = "user_1",
            nameKey = "category_health",
            customName = null,
            icon = "health",
            colorToken = "cat2",
            isDefault = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    )

    @Before
    fun setUp() {
        coEvery { categoryRepository.observeCategories("user_1") } returns flowOf(sampleCategories)

        viewModel = AddExpenseViewModel(
            addExpenseUseCase = addExpenseUseCase,
            updateExpenseUseCase = updateExpenseUseCase,
            deleteExpenseUseCase = deleteExpenseUseCase,
            createCustomCategoryUseCase = createCustomCategoryUseCase,
            expenseRepository = expenseRepository,
            categoryRepository = categoryRepository
        )
    }

    @Test
    fun `keypad digits append and backspace deletes correctly`() {
        assertEquals("0", viewModel.uiState.value.amountString)

        viewModel.onDigitClick('5')
        assertEquals("5", viewModel.uiState.value.amountString)

        viewModel.onDigitClick('0')
        assertEquals("50", viewModel.uiState.value.amountString)

        viewModel.onDecimalClick()
        assertEquals("50.", viewModel.uiState.value.amountString)

        viewModel.onDigitClick('7')
        viewModel.onDigitClick('5')
        assertEquals("50.75", viewModel.uiState.value.amountString)

        // Additional decimal places beyond 2 are ignored
        viewModel.onDigitClick('9')
        assertEquals("50.75", viewModel.uiState.value.amountString)

        // Duplicate decimal is ignored
        viewModel.onDecimalClick()
        assertEquals("50.75", viewModel.uiState.value.amountString)

        // Backspace
        viewModel.onBackspaceClick()
        assertEquals("50.7", viewModel.uiState.value.amountString)

        viewModel.onClearClick()
        assertEquals("0", viewModel.uiState.value.amountString)
    }

    @Test
    fun `initForUser loads categories and auto-selects first category`() = runTest {
        viewModel.initForUser("user_1")

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.categories.size)
            assertEquals("cat_1", state.selectedCategoryId)
            assertFalse(state.isEditing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save with 0 amount surfaces amount error`() {
        viewModel.initForUser("user_1")
        viewModel.onClearClick()

        viewModel.save("user_1")

        val state = viewModel.uiState.value
        assertEquals(R.string.error_enter_amount, state.amountErrorResId)
        coVerify(exactly = 0) { addExpenseUseCase(any()) }
    }

    @Test
    fun `save with valid amount and category invokes AddExpenseUseCase with minor units`() = runTest {
        val paramsSlot = slot<AddExpenseParams>()
        coEvery { addExpenseUseCase(capture(paramsSlot)) } returns Result.success(
            Expense(
                id = "exp_new",
                userId = "user_1",
                amountMinor = 15050L,
                categoryId = "cat_1",
                date = Instant.now(),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )

        viewModel.initForUser("user_1")
        viewModel.onDigitClick('1')
        viewModel.onDigitClick('5')
        viewModel.onDigitClick('0')
        viewModel.onDecimalClick()
        viewModel.onDigitClick('5')
        viewModel.onDigitClick('0') // 150.50 -> 15050 minor units
        viewModel.onNoteChange("Lunch with colleagues")

        viewModel.save("user_1")

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isSaved)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { addExpenseUseCase(any()) }
        val captured = paramsSlot.captured
        assertEquals("user_1", captured.userId)
        assertEquals(15050L, captured.amountMinor)
        assertEquals("cat_1", captured.categoryId)
        assertEquals("Lunch with colleagues", captured.note)
    }

    @Test
    fun `initForUser with editExpenseId loads existing expense into edit state`() = runTest {
        val existingExpense = Expense(
            id = "exp_999",
            userId = "user_1",
            amountMinor = 250000L, // ৳2500.00
            categoryId = "cat_2",
            note = "Doctor visit",
            date = Instant.parse("2026-03-10T10:00:00Z"),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        coEvery { expenseRepository.getExpenseById("exp_999") } returns existingExpense

        viewModel.initForUser("user_1", editExpenseId = "exp_999")

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isEditing)
            assertEquals("exp_999", state.expenseId)
            assertEquals("2500", state.amountString)
            assertEquals("cat_2", state.selectedCategoryId)
            assertEquals("Doctor visit", state.note)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save in edit mode invokes UpdateExpenseUseCase`() = runTest {
        val existingExpense = Expense(
            id = "exp_999",
            userId = "user_1",
            amountMinor = 250000L,
            categoryId = "cat_2",
            date = Instant.parse("2026-03-10T10:00:00Z"),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        coEvery { expenseRepository.getExpenseById("exp_999") } returns existingExpense

        val updateSlot = slot<UpdateExpenseParams>()
        coEvery { updateExpenseUseCase(capture(updateSlot)) } returns Result.success(Unit)

        viewModel.initForUser("user_1", editExpenseId = "exp_999")
        viewModel.onNoteChange("Updated note")
        viewModel.save("user_1")

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isSaved)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { updateExpenseUseCase(any()) }
        assertEquals("exp_999", updateSlot.captured.id)
        assertEquals("Updated note", updateSlot.captured.note)
        assertEquals(250000L, updateSlot.captured.amountMinor)
    }

    @Test
    fun `delete invokes DeleteExpenseUseCase and sets isDeleted true`() = runTest {
        val existingExpense = Expense(
            id = "exp_delete",
            userId = "user_1",
            amountMinor = 10000L,
            categoryId = "cat_1",
            date = Instant.now(),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        coEvery { expenseRepository.getExpenseById("exp_delete") } returns existingExpense
        coEvery { deleteExpenseUseCase("exp_delete") } returns Result.success(Unit)

        viewModel.initForUser("user_1", editExpenseId = "exp_delete")
        viewModel.delete()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isDeleted)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { deleteExpenseUseCase("exp_delete") }
    }

    @Test
    fun `createCustomCategory validates empty name and calls usecase`() = runTest {
        viewModel.initForUser("user_1")
        viewModel.openNewCategoryDialog()

        // Blank name triggers error
        viewModel.createCustomCategory()
        assertEquals(R.string.error_category_name_empty, viewModel.uiState.value.newCategoryErrorResId)
        coVerify(exactly = 0) { createCustomCategoryUseCase(any(), any(), any(), any()) }

        // Valid name invokes usecase and auto-selects new category
        val created = Category(
            id = "cat_custom_1",
            userId = "user_1",
            nameKey = null,
            customName = "Gadgets",
            icon = "category",
            colorToken = "cat3",
            isDefault = false,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        coEvery { createCustomCategoryUseCase("user_1", "Gadgets", "cat3", any()) } returns Result.success(created)

        viewModel.onNewCategoryNameChange("Gadgets")
        viewModel.onNewCategoryColorChange("cat3")
        viewModel.createCustomCategory()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isNewCategoryDialogOpen)
            assertEquals("cat_custom_1", state.selectedCategoryId)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
