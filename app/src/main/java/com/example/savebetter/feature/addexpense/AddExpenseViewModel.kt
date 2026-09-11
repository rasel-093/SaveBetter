package com.example.savebetter.feature.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebetter.R
import com.example.savebetter.core.domain.repository.CategoryRepository
import com.example.savebetter.core.domain.repository.ExpenseRepository
import com.example.savebetter.core.domain.usecase.expense.AddExpenseParams
import com.example.savebetter.core.domain.usecase.expense.AddExpenseUseCase
import com.example.savebetter.core.domain.usecase.expense.CreateCustomCategoryUseCase
import com.example.savebetter.core.domain.usecase.expense.DeleteExpenseUseCase
import com.example.savebetter.core.domain.usecase.expense.UpdateExpenseParams
import com.example.savebetter.core.domain.usecase.expense.UpdateExpenseUseCase
import com.example.savebetter.core.i18n.NumeralConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val createCustomCategoryUseCase: CreateCustomCategoryUseCase,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    private var activeUserId: String? = null

    fun initForUser(userId: String, editExpenseId: String? = null) {
        activeUserId = userId
        viewModelScope.launch {
            categoryRepository.observeCategories(userId).collect { categories ->
                _uiState.update { current ->
                    current.copy(
                        categories = categories.filter { !it.isDeleted },
                        selectedCategoryId = current.selectedCategoryId ?: categories.firstOrNull()?.id
                    )
                }
            }
        }

        if (editExpenseId != null) {
            loadForEdit(editExpenseId)
        }
    }

    private fun loadForEdit(expenseId: String) {
        viewModelScope.launch {
            val expense = expenseRepository.getExpenseById(expenseId) ?: return@launch
            val amountMajor = expense.amountMinor / 100.0
            val amountStr = if (amountMajor % 1.0 == 0.0) {
                amountMajor.toLong().toString()
            } else {
                amountMajor.toString()
            }
            val expenseLocalDate = expense.date.atZone(ZoneId.systemDefault()).toLocalDate()

            _uiState.update {
                it.copy(
                    isEditing = true,
                    expenseId = expenseId,
                    amountString = amountStr,
                    selectedCategoryId = expense.categoryId,
                    selectedDate = expenseLocalDate,
                    note = expense.note ?: ""
                )
            }
        }
    }

    fun onDigitClick(digit: Char) {
        _uiState.update { current ->
            val currentStr = current.amountString
            if (currentStr.length >= 10) return@update current // cap maximum length

            val newStr = if (currentStr == "0") {
                digit.toString()
            } else {
                // If there's a decimal, limit to 2 decimal places
                val dotIndex = currentStr.indexOf('.')
                if (dotIndex != -1 && currentStr.length - dotIndex > 2) {
                    currentStr
                } else {
                    currentStr + digit
                }
            }
            current.copy(amountString = newStr, amountErrorResId = null)
        }
    }

    fun onDecimalClick() {
        _uiState.update { current ->
            if (!current.amountString.contains('.')) {
                current.copy(amountString = current.amountString + ".", amountErrorResId = null)
            } else current
        }
    }

    fun onBackspaceClick() {
        _uiState.update { current ->
            val currentStr = current.amountString
            val newStr = if (currentStr.length <= 1) {
                "0"
            } else {
                currentStr.dropLast(1)
            }
            current.copy(amountString = newStr, amountErrorResId = null)
        }
    }

    fun onClearClick() {
        _uiState.update { it.copy(amountString = "0", amountErrorResId = null) }
    }

    fun onCategorySelected(categoryId: String) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, categoryErrorResId = null) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun openNewCategoryDialog() {
        _uiState.update {
            it.copy(
                isNewCategoryDialogOpen = true,
                newCategoryName = "",
                newCategoryColorToken = "cat1",
                newCategoryErrorResId = null
            )
        }
    }

    fun closeNewCategoryDialog() {
        _uiState.update { it.copy(isNewCategoryDialogOpen = false) }
    }

    fun onNewCategoryNameChange(name: String) {
        _uiState.update { it.copy(newCategoryName = name, newCategoryErrorResId = null) }
    }

    fun onNewCategoryColorChange(colorToken: String) {
        _uiState.update { it.copy(newCategoryColorToken = colorToken) }
    }

    fun createCustomCategory() {
        val userId = activeUserId ?: return
        val state = _uiState.value
        val name = state.newCategoryName.trim()

        if (name.isBlank()) {
            _uiState.update { it.copy(newCategoryErrorResId = R.string.error_category_name_empty) }
            return
        }

        viewModelScope.launch {
            val result = createCustomCategoryUseCase(
                userId = userId,
                customName = name,
                colorToken = state.newCategoryColorToken
            )
            result.onSuccess { created ->
                _uiState.update {
                    it.copy(
                        isNewCategoryDialogOpen = false,
                        selectedCategoryId = created.id
                    )
                }
            }
        }
    }

    fun save(userId: String) {
        val state = _uiState.value
        val cleanAmountStr = NumeralConverter.toEnglishDigits(state.amountString)
        val amountDouble = cleanAmountStr.toDoubleOrNull() ?: 0.0

        var hasError = false
        var amountError: Int? = null
        var categoryError: Int? = null

        if (amountDouble <= 0.0) {
            amountError = R.string.error_enter_amount
            hasError = true
        }

        if (state.selectedCategoryId == null) {
            categoryError = R.string.error_select_category
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    amountErrorResId = amountError,
                    categoryErrorResId = categoryError
                )
            }
            return
        }

        val amountMinor = (amountDouble * 100L).toLong()
        val dateInstant = state.selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            if (state.isEditing && state.expenseId != null) {
                updateExpenseUseCase(
                    UpdateExpenseParams(
                        id = state.expenseId,
                        userId = userId,
                        amountMinor = amountMinor,
                        categoryId = state.selectedCategoryId!!,
                        date = dateInstant,
                        note = state.note
                    )
                ).onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                }.onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } else {
                addExpenseUseCase(
                    AddExpenseParams(
                        userId = userId,
                        amountMinor = amountMinor,
                        categoryId = state.selectedCategoryId!!,
                        date = dateInstant,
                        note = state.note
                    )
                ).onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                }.onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun delete() {
        val expenseId = _uiState.value.expenseId ?: return
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            deleteExpenseUseCase(expenseId).onSuccess {
                _uiState.update { it.copy(isLoading = false, isDeleted = true) }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
