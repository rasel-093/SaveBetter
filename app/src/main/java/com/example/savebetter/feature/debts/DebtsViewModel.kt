package com.example.savebetter.feature.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebetter.core.auth.repository.AuthRepository
import com.example.savebetter.core.domain.model.DebtCredit
import com.example.savebetter.core.domain.model.DebtCreditSummary
import com.example.savebetter.core.domain.model.DebtDirection
import com.example.savebetter.core.domain.usecase.debt.AddDebtCreditUseCase
import com.example.savebetter.core.domain.usecase.debt.DeleteDebtCreditUseCase
import com.example.savebetter.core.domain.usecase.debt.GetDebtsAndCreditsSummaryUseCase
import com.example.savebetter.core.domain.usecase.debt.SettleDebtCreditUseCase
import com.example.savebetter.core.domain.usecase.debt.UpdateDebtCreditUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DebtsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getDebtsAndCreditsSummaryUseCase: GetDebtsAndCreditsSummaryUseCase,
    private val addDebtCreditUseCase: AddDebtCreditUseCase,
    private val updateDebtCreditUseCase: UpdateDebtCreditUseCase,
    private val settleDebtCreditUseCase: SettleDebtCreditUseCase,
    private val deleteDebtCreditUseCase: DeleteDebtCreditUseCase
) : ViewModel() {

    private val activeUserId = MutableStateFlow<String?>(null)
    private val selectedTab = MutableStateFlow(DebtTab.ACTIVE)
    data class DialogState(
        val isOpen: Boolean = false,
        val editing: DebtCredit? = null,
        val error: String? = null,
        val info: String? = null
    )

    private val dialogState = MutableStateFlow(DialogState())

    init {
        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                if (user != null) {
                    activeUserId.value = user.id
                }
            }
        }
    }

    fun initUser(userId: String) {
        activeUserId.value = userId
    }

    private val summaryFlow: kotlinx.coroutines.flow.Flow<DebtCreditSummary> = activeUserId.flatMapLatest { userId ->
        if (userId == null) {
            flowOf(DebtCreditSummary())
        } else {
            getDebtsAndCreditsSummaryUseCase(userId)
        }
    }

    val uiState: StateFlow<DebtsUiState> = combine(
        activeUserId,
        selectedTab,
        summaryFlow,
        dialogState
    ) { userId, tab, summary, dialog ->
        DebtsUiState(
            isLoading = userId == null,
            summary = summary,
            selectedTab = tab,
            isAddEditSheetOpen = dialog.isOpen,
            editingItem = dialog.editing,
            errorMessage = dialog.error,
            infoMessage = dialog.info
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DebtsUiState(isLoading = true)
    )

    fun selectTab(tab: DebtTab) {
        selectedTab.value = tab
    }

    fun openAddDialog() {
        dialogState.value = DialogState(isOpen = true, editing = null)
    }

    fun openEditDialog(item: DebtCredit) {
        dialogState.value = DialogState(isOpen = true, editing = item)
    }

    fun dismissDialog() {
        dialogState.value = DialogState(isOpen = false, editing = null)
    }

    fun clearMessages() {
        dialogState.update { it.copy(error = null, info = null) }
    }

    fun saveDebtCredit(
        direction: DebtDirection,
        personName: String,
        amountMinor: Long,
        note: String?,
        dueDate: Instant?
    ) {
        val uid = activeUserId.value ?: return
        val currentEdit = dialogState.value.editing

        viewModelScope.launch {
            if (currentEdit != null) {
                val updated = currentEdit.copy(
                    direction = direction,
                    personName = personName,
                    amountMinor = amountMinor,
                    note = note,
                    dueDate = dueDate
                )
                val result = updateDebtCreditUseCase(updated)
                result.fold(
                    onSuccess = {
                        dismissDialog()
                    },
                    onFailure = { error ->
                        dialogState.update { it.copy(error = error.message) }
                    }
                )
            } else {
                val result = addDebtCreditUseCase(
                    userId = uid,
                    direction = direction,
                    personName = personName,
                    amountMinor = amountMinor,
                    note = note,
                    dueDate = dueDate
                )
                result.fold(
                    onSuccess = {
                        dismissDialog()
                    },
                    onFailure = { error ->
                        dialogState.update { it.copy(error = error.message) }
                    }
                )
            }
        }
    }

    fun toggleSettle(item: DebtCredit) {
        viewModelScope.launch {
            settleDebtCreditUseCase(item.id, !item.isSettled)
        }
    }

    fun deleteItem(item: DebtCredit) {
        viewModelScope.launch {
            deleteDebtCreditUseCase(item.id)
            if (dialogState.value.editing?.id == item.id) {
                dismissDialog()
            }
        }
    }
}
