package com.yusufjonaxmedov.pennywise.feature.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufjonaxmedov.pennywise.core.common.MoneyParser
import com.yusufjonaxmedov.pennywise.core.common.toMonthKey
import com.yusufjonaxmedov.pennywise.core.common.toYearMonth
import com.yusufjonaxmedov.pennywise.core.model.Budget
import com.yusufjonaxmedov.pennywise.core.model.BudgetDraft
import com.yusufjonaxmedov.pennywise.core.model.Category
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import com.yusufjonaxmedov.pennywise.domain.repository.BudgetsRepository
import com.yusufjonaxmedov.pennywise.domain.repository.CategoriesRepository
import com.yusufjonaxmedov.pennywise.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BudgetsUiState(
    val currencyCode: String = "USD",
    val budgetsEnabled: Boolean = true,
    val monthKey: String = LocalDate.now().toMonthKey(),
    val budgets: List<Budget> = emptyList(),
    val categories: List<Category> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetsRepository: BudgetsRepository,
    categoriesRepository: CategoriesRepository,
    preferencesRepository: PreferencesRepository,
) : ViewModel() {
    private val monthKey = MutableStateFlow(LocalDate.now().toMonthKey())
    private val events = MutableSharedFlow<String>()

    val eventFlow = events.asSharedFlow()

    val uiState: StateFlow<BudgetsUiState> = combine(
        preferencesRepository.observePreferences(),
        categoriesRepository.observeCategories(TransactionType.EXPENSE),
        monthKey,
        monthKey.flatMapLatest(budgetsRepository::observeBudgets),
    ) { preferences, categories, selectedMonth, budgets ->
        BudgetsUiState(
            currencyCode = preferences.currencyCode,
            budgetsEnabled = preferences.budgetsEnabled,
            monthKey = selectedMonth,
            budgets = budgets,
            categories = categories,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BudgetsUiState(),
    )

    fun previousMonth() {
        monthKey.update { it.toYearMonth().minusMonths(1).toString() }
    }

    fun nextMonth() {
        monthKey.update { it.toYearMonth().plusMonths(1).toString() }
    }

    fun saveBudget(
        budgetId: Long?,
        categoryId: Long,
        amountInput: String,
        rolloverEnabled: Boolean,
    ) {
        viewModelScope.launch {
            val amountMinor = MoneyParser.parseMinorAmount(amountInput)
            if (amountMinor == null || amountMinor <= 0) {
                events.emit("Enter a valid budget amount.")
                return@launch
            }
            budgetsRepository.upsertBudget(
                BudgetDraft(
                    id = budgetId,
                    monthKey = monthKey.value,
                    categoryId = categoryId,
                    limitMinor = amountMinor,
                    rolloverEnabled = rolloverEnabled,
                ),
            )
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            budgetsRepository.deleteBudget(budgetId)
            events.emit("Budget removed.")
        }
    }
}
