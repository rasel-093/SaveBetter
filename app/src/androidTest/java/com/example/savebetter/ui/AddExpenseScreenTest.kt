package com.example.savebetter.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.savebetter.R
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.usecase.expense.AddExpenseUseCase
import com.example.savebetter.core.domain.usecase.expense.CreateCustomCategoryUseCase
import com.example.savebetter.core.domain.usecase.expense.DeleteExpenseUseCase
import com.example.savebetter.core.domain.usecase.expense.UpdateExpenseUseCase
import com.example.savebetter.feature.addexpense.AddExpenseViewModel
import com.example.savebetter.feature.addexpense.ui.AddExpenseScreen
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddExpenseScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var expenseRepository: FakeExpenseRepository
    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var viewModel: AddExpenseViewModel
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        val testCategories = listOf(
            Category(
                id = "cat_1",
                userId = "user_123",
                nameKey = null,
                customName = "Household",
                icon = "home",
                colorToken = "cat2",
                isDefault = true
            )
        )
        expenseRepository = FakeExpenseRepository()
        categoryRepository = FakeCategoryRepository(testCategories)

        viewModel = AddExpenseViewModel(
            addExpenseUseCase = AddExpenseUseCase(expenseRepository),
            updateExpenseUseCase = UpdateExpenseUseCase(expenseRepository),
            deleteExpenseUseCase = DeleteExpenseUseCase(expenseRepository),
            createCustomCategoryUseCase = CreateCustomCategoryUseCase(categoryRepository),
            expenseRepository = expenseRepository,
            categoryRepository = categoryRepository
        )
    }

    @Test
    fun addExpenseScreen_rendersAllEssentialSections() {
        composeTestRule.setContent {
            SaveBetterTheme {
                AddExpenseScreen(
                    userId = "user_123",
                    onDismiss = {},
                    viewModel = viewModel
                )
            }
        }

        val screenTitle = context.getString(R.string.add_expense_title)
        val categoryLabel = context.getString(R.string.expense_category_label)
        val saveButton = context.getString(R.string.action_save_expense)

        composeTestRule.onNodeWithText(screenTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(categoryLabel, ignoreCase = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Household").assertIsDisplayed()
        composeTestRule.onNodeWithText(saveButton).assertIsDisplayed()
    }

    @Test
    fun addExpenseScreen_tappingNumericKeypad_updatesAmount() {
        composeTestRule.setContent {
            SaveBetterTheme {
                AddExpenseScreen(
                    userId = "user_123",
                    onDismiss = {},
                    viewModel = viewModel
                )
            }
        }

        // Tap 5, 0, 0
        composeTestRule.onNodeWithText("5").performClick()
        composeTestRule.onNodeWithText("0").performClick()
        composeTestRule.onNodeWithText("0").performClick()

        assertEquals("500", viewModel.uiState.value.amountString)
    }

    @Test
    fun addExpenseScreen_selectingCategoryChip_updatesSelectedCategory() {
        composeTestRule.setContent {
            SaveBetterTheme {
                AddExpenseScreen(
                    userId = "user_123",
                    onDismiss = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Household").performClick()

        assertEquals("cat_1", viewModel.uiState.value.selectedCategoryId)
    }
}
