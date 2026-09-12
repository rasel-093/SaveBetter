package com.example.savebetter.feature.reconciliation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebetter.R
import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.core.domain.model.ReconciliationStatus
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.auth.repository.AuthRepository
import com.example.savebetter.core.domain.repository.CategoryRepository
import com.example.savebetter.core.domain.repository.ExpenseRepository
import com.example.savebetter.core.domain.repository.TargetRepository
import com.example.savebetter.core.domain.repository.UserProfileRepository
import com.example.savebetter.core.domain.usecase.dashboard.GetReconciliationSummaryUseCase
import com.example.savebetter.core.domain.usecase.dashboard.SaveSalaryHandRecordUseCase
import com.example.savebetter.core.domain.usecase.expense.AddExpenseParams
import com.example.savebetter.core.domain.usecase.expense.AddExpenseUseCase
import com.example.savebetter.core.domain.usecase.i18n.GetLanguageUseCase
import com.example.savebetter.core.i18n.CurrencyFormatter
import com.example.savebetter.core.i18n.NumeralConverter
import com.example.savebetter.core.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReconciliationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val expenseRepository: ExpenseRepository,
    private val targetRepository: TargetRepository,
    private val userProfileRepository: UserProfileRepository,
    private val categoryRepository: CategoryRepository,
    private val getLanguageUseCase: GetLanguageUseCase,
    private val getReconciliationSummaryUseCase: GetReconciliationSummaryUseCase,
    private val saveSalaryHandRecordUseCase: SaveSalaryHandRecordUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val syncManager: SyncManager? = null
) : ViewModel() {

    private val activeUserId = MutableStateFlow<String?>(null)
    private val selectedYearMonth = MutableStateFlow(YearMonth.now())
    private val salaryInput = MutableStateFlow<String?>(null)
    private val handRemainingInput = MutableStateFlow<String?>(null)
    private val isSaving = MutableStateFlow(false)
    private val showQuickLogDialog = MutableStateFlow(false)
    private val userMessageResId = MutableStateFlow<Int?>(null)

    init {
        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                activeUserId.value = user?.id
            }
        }
    }

    private data class InputState(
        val salaryInput: String?,
        val handRemainingInput: String?,
        val isSaving: Boolean,
        val showQuickLogDialog: Boolean,
        val userMessageResId: Int?
    )

    private val inputStateFlow = combine(
        salaryInput,
        handRemainingInput,
        isSaving,
        showQuickLogDialog,
        userMessageResId
    ) { sal, hand, saving, quickLog, msg ->
        InputState(sal, hand, saving, quickLog, msg)
    }

    val uiState: StateFlow<ReconciliationUiState> = combine(
        activeUserId,
        selectedYearMonth,
        getLanguageUseCase(),
        inputStateFlow
    ) { userId, ym, lang, input ->
        DataQuery(
            userId = userId,
            yearMonth = ym,
            language = lang,
            salaryInput = input.salaryInput,
            handRemainingInput = input.handRemainingInput,
            isSaving = input.isSaving,
            showQuickLogDialog = input.showQuickLogDialog,
            userMessageResId = input.userMessageResId
        )
    }.flatMapLatest { query ->
        val userId = query.userId
        if (userId == null) {
            flowOf(ReconciliationUiState())
        } else {
            combine(
                expenseRepository.observeExpenses(userId),
                targetRepository.observeSalaryHandRecords(userId),
                userProfileRepository.observeUserProfile(userId)
            ) { expenses, salaryRecords, profile ->
                val ym = query.yearMonth
                val lang = query.language

                // 1. Filter active month non-deleted expenses
                val activeMonthExpenses = expenses.filter { exp ->
                    !exp.isDeleted &&
                            exp.date.atZone(ZoneId.systemDefault()).toLocalDate().let { date ->
                                date.year == ym.year && date.monthValue == ym.monthValue
                            }
                }

                // 2. Find existing record for this month
                val existingRecord = salaryRecords.find { it.year == ym.year && it.month == ym.monthValue }

                // 3. Resolve effective salary
                val defaultSalaryMinor = existingRecord?.salaryAmountMinor
                    ?: profile?.monthlySalaryMinor
                    ?: 0L

                val effectiveSalaryMinor = if (query.salaryInput != null) {
                    parseInputToMinor(query.salaryInput)
                } else {
                    defaultSalaryMinor
                }

                // 4. Resolve effective hand remaining
                val defaultHandMinor = existingRecord?.handRemainingAmountMinor ?: 0L
                val effectiveHandMinor = if (query.handRemainingInput != null) {
                    parseInputToMinor(query.handRemainingInput)
                } else {
                    defaultHandMinor
                }

                // 5. Calculate reconciliation summary
                val summary = getReconciliationSummaryUseCase(
                    year = ym.year,
                    month = ym.monthValue,
                    salaryMinor = effectiveSalaryMinor,
                    handRemainingMinor = effectiveHandMinor,
                    expenses = activeMonthExpenses
                )

                // 6. Format input field text for display
                val salaryFieldText = query.salaryInput ?: run {
                    if (defaultSalaryMinor > 0L) {
                        formatUnitsWithoutCurrency(defaultSalaryMinor / 100L, lang)
                    } else {
                        ""
                    }
                }

                val handFieldText = query.handRemainingInput ?: run {
                    if (defaultHandMinor > 0L) {
                        formatUnitsWithoutCurrency(defaultHandMinor / 100L, lang)
                    } else {
                        ""
                    }
                }

                val monthDisplay = formatMonthDisplay(ym, lang)
                val isCurrent = ym == YearMonth.now()
                val syncStatus = existingRecord?.syncStatus ?: SyncState.SYNCED

                ReconciliationUiState(
                    selectedYearMonth = ym,
                    monthDisplay = monthDisplay,
                    salaryInput = salaryFieldText,
                    handRemainingInput = handFieldText,
                    summary = summary,
                    formattedSalary = CurrencyFormatter.formatMinor(summary.salaryMinor, lang),
                    formattedHandRemaining = CurrencyFormatter.formatMinor(summary.handRemainingMinor, lang),
                    formattedNetOutflow = CurrencyFormatter.formatMinor(summary.totalExpenseMinor, lang),
                    formattedAppLogged = CurrencyFormatter.formatMinor(summary.appLoggedExpenseMinor, lang),
                    formattedUnrecorded = CurrencyFormatter.formatMinor(summary.outOfNoteExpenseMinor, lang),
                    isSaving = query.isSaving,
                    showQuickLogDialog = query.showQuickLogDialog,
                    syncStatus = syncStatus,
                    isCurrentMonth = isCurrent,
                    userMessageResId = query.userMessageResId
                )
            }
        }.combine(syncManager?.syncStatus ?: flowOf(null)) { state, liveStatus ->
            state.copy(liveSyncStatus = liveStatus)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReconciliationUiState()
    )

    fun initForUser(userId: String) {
        activeUserId.value = userId
    }

    fun onSalaryChanged(newSalary: String) {
        salaryInput.value = newSalary
    }

    fun onHandRemainingChanged(newRemaining: String) {
        handRemainingInput.value = newRemaining
    }

    fun navigatePreviousMonth() {
        salaryInput.value = null
        handRemainingInput.value = null
        selectedYearMonth.update { it.minusMonths(1) }
    }

    fun navigateNextMonth() {
        salaryInput.value = null
        handRemainingInput.value = null
        selectedYearMonth.update { it.plusMonths(1) }
    }

    fun saveRecord() {
        val userId = activeUserId.value ?: return
        val state = uiState.value
        val ym = state.selectedYearMonth

        viewModelScope.launch {
            isSaving.value = true
            val result = saveSalaryHandRecordUseCase(
                userId = userId,
                year = ym.year,
                month = ym.monthValue,
                salaryAmountMinor = state.summary.salaryMinor,
                handRemainingAmountMinor = state.summary.handRemainingMinor
            )
            isSaving.value = false
            if (result.isSuccess) {
                userMessageResId.value = R.string.reconciliation_record_saved
            }
        }
    }

    fun showQuickLogConfirmation() {
        showQuickLogDialog.value = true
    }

    fun dismissQuickLogConfirmation() {
        showQuickLogDialog.value = false
    }

    fun quickLogDiscrepancy() {
        val userId = activeUserId.value ?: return
        val state = uiState.value
        val unrecordedMinor = state.summary.outOfNoteExpenseMinor
        if (unrecordedMinor <= 0L) return

        viewModelScope.launch {
            isSaving.value = true
            try {
                // 1. Resolve category
                val categories = categoryRepository.observeCategories(userId).first()
                val categoryId = categories.find { it.nameKey == "category_other" }?.id
                    ?: categories.firstOrNull()?.id
                    ?: "cat_other"

                // 2. Add expense
                val lang = getLanguageUseCase().first()
                val note = if (lang == AppLanguage.BANGLA) "মাস শেষের অমিল হিসাব" else "Month-end unrecorded expense"
                addExpenseUseCase(
                    AddExpenseParams(
                        userId = userId,
                        amountMinor = unrecordedMinor,
                        categoryId = categoryId,
                        date = Instant.now(),
                        note = note
                    )
                )

                // 3. Save salary hand record
                val ym = state.selectedYearMonth
                saveSalaryHandRecordUseCase(
                    userId = userId,
                    year = ym.year,
                    month = ym.monthValue,
                    salaryAmountMinor = state.summary.salaryMinor,
                    handRemainingAmountMinor = state.summary.handRemainingMinor
                )

                userMessageResId.value = R.string.reconciliation_record_saved
            } finally {
                isSaving.value = false
                showQuickLogDialog.value = false
            }
        }
    }

    fun clearUserMessage() {
        userMessageResId.value = null
    }

    private fun parseInputToMinor(input: String): Long {
        val englishDigits = NumeralConverter.toEnglishDigits(input).filter { it.isDigit() }
        val major = englishDigits.toLongOrNull() ?: 0L
        return major * 100L
    }

    private fun formatUnitsWithoutCurrency(amount: Long, language: AppLanguage): String {
        val formatted = String.format(Locale.US, "%,d", amount)
        return if (language == AppLanguage.BANGLA) {
            NumeralConverter.toBanglaDigits(formatted)
        } else {
            formatted
        }
    }

    private fun formatMonthDisplay(ym: YearMonth, language: AppLanguage): String {
        return if (language == AppLanguage.BANGLA) {
            val bnMonth = getBanglaMonthName(ym.monthValue)
            val bnYear = NumeralConverter.toBanglaDigits(ym.year.toString())
            "$bnMonth $bnYear"
        } else {
            ym.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
        }
    }

    private fun getBanglaMonthName(month: Int): String {
        return when (month) {
            1 -> "জানুয়ারি"
            2 -> "ফেব্রুয়ারি"
            3 -> "মার্চ"
            4 -> "এপ্রিল"
            5 -> "মে"
            6 -> "জুন"
            7 -> "জুলাই"
            8 -> "আগস্ট"
            9 -> "সেপ্টেম্বর"
            10 -> "অক্টোবর"
            11 -> "নভেম্বর"
            12 -> "ডিসেম্বর"
            else -> "মাস"
        }
    }

    private data class DataQuery(
        val userId: String?,
        val yearMonth: YearMonth,
        val language: AppLanguage,
        val salaryInput: String?,
        val handRemainingInput: String?,
        val isSaving: Boolean,
        val showQuickLogDialog: Boolean,
        val userMessageResId: Int?
    )
}
