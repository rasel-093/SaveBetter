package com.example.savebetter.feature.addexpense

import androidx.annotation.StringRes
import com.example.savebetter.core.domain.model.Category
import java.time.LocalDate

/**
 * UI state for Add/Edit Expense screen (Screen 04).
 */
data class AddExpenseUiState(
    val amountString: String = "0",
    val selectedCategoryId: String? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val note: String = "",
    val categories: List<Category> = emptyList(),
    val isEditing: Boolean = false,
    val expenseId: String? = null,
    @get:StringRes val amountErrorResId: Int? = null,
    @get:StringRes val categoryErrorResId: Int? = null,
    val isNewCategoryDialogOpen: Boolean = false,
    val newCategoryName: String = "",
    val newCategoryColorToken: String = "cat1",
    @get:StringRes val newCategoryErrorResId: Int? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false
)
