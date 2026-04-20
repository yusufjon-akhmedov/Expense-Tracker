package com.yusufjonaxmedov.pennywise.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufjonaxmedov.pennywise.core.model.Account
import com.yusufjonaxmedov.pennywise.core.model.AppPreferencesModel
import com.yusufjonaxmedov.pennywise.core.model.Category
import com.yusufjonaxmedov.pennywise.core.model.DateRangeSerializable
import com.yusufjonaxmedov.pennywise.core.model.SavedFilter
import com.yusufjonaxmedov.pennywise.core.model.SortOption
import com.yusufjonaxmedov.pennywise.core.model.Transaction
import com.yusufjonaxmedov.pennywise.core.model.TransactionDraft
import com.yusufjonaxmedov.pennywise.core.model.TransactionFilter
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import com.yusufjonaxmedov.pennywise.domain.repository.AccountsRepository
import com.yusufjonaxmedov.pennywise.domain.repository.CategoriesRepository
import com.yusufjonaxmedov.pennywise.domain.repository.PreferencesRepository
import com.yusufjonaxmedov.pennywise.domain.repository.TransactionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

sealed interface TransactionsEvent {
    data object DeletedWithUndo : TransactionsEvent
    data class Message(val text: String) : TransactionsEvent
}

data class TransactionsUiState(
    val currencyCode: String = "USD",
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val savedFilters: List<SavedFilter> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val filter: TransactionFilter = TransactionFilter(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    accountsRepository: AccountsRepository,
    categoriesRepository: CategoriesRepository,
    preferencesRepository: PreferencesRepository,
    private val transactionsRepository: TransactionsRepository,
) : ViewModel() {
    private val filter = MutableStateFlow(TransactionFilter())
    private val events = MutableSharedFlow<TransactionsEvent>()
    private var lastDeletedTransaction: TransactionDraft? = null
    private val transactionsFlow = filter.flatMapLatest { activeFilter ->
        transactionsRepository.observeTransactions(activeFilter)
    }

    val eventFlow = events.asSharedFlow()

    private val baseUiState = combine(
        categoriesRepository.observeCategories(),
        accountsRepository.observeAccounts(),
        transactionsRepository.observeSavedFilters(),
        filter,
        transactionsFlow,
    ) { categories: List<Category>,
        accounts: List<Account>,
        savedFilters: List<SavedFilter>,
        activeFilter: TransactionFilter,
        transactions: List<Transaction> ->
        TransactionsUiState(
            categories = categories,
            accounts = accounts,
            savedFilters = savedFilters,
            transactions = transactions,
            filter = activeFilter,
        )
    }

    val uiState: StateFlow<TransactionsUiState> = combine(
        preferencesRepository.observePreferences(),
        baseUiState,
    ) { preferences: AppPreferencesModel, baseState: TransactionsUiState ->
        baseState.copy(currencyCode = preferences.currencyCode)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionsUiState(),
    )

    fun updateSearch(query: String) {
        filter.update { it.copy(searchQuery = query) }
    }

    fun toggleType(type: TransactionType) {
        filter.update { current ->
            val updated = current.types.toMutableSet()
            if (!updated.add(type)) updated.remove(type)
            current.copy(types = updated)
        }
    }

    fun toggleCategory(categoryId: Long) {
        filter.update { current ->
            val updated = current.categoryIds.toMutableSet()
            if (!updated.add(categoryId)) updated.remove(categoryId)
            current.copy(categoryIds = updated)
        }
    }

    fun toggleAccount(accountId: Long) {
        filter.update { current ->
            val updated = current.accountIds.toMutableSet()
            if (!updated.add(accountId)) updated.remove(accountId)
            current.copy(accountIds = updated)
        }
    }

    fun updateSort(sortOption: SortOption) {
        filter.update { it.copy(sortOption = sortOption) }
    }

    fun updateDateRange(start: String?, endInclusive: String?) {
        filter.update {
            it.copy(
                dateRange = if (!start.isNullOrBlank() && !endInclusive.isNullOrBlank()) {
                    DateRangeSerializable(start, endInclusive)
                } else {
                    null
                },
            )
        }
    }

    fun applySavedFilter(savedFilter: SavedFilter) {
        filter.value = savedFilter.filter
    }

    fun clearFilters() {
        filter.value = TransactionFilter()
    }

    fun saveCurrentFilter(name: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                events.emit(TransactionsEvent.Message("Preset name cannot be empty."))
                return@launch
            }
            transactionsRepository.saveFilter(name, filter.value)
            events.emit(TransactionsEvent.Message("Filter saved."))
        }
    }

    fun deleteSavedFilter(filterId: Long) {
        viewModelScope.launch {
            transactionsRepository.deleteSavedFilter(filterId)
        }
    }

    fun duplicateTransaction(transactionId: Long) {
        viewModelScope.launch {
            transactionsRepository.duplicateTransaction(transactionId)
            events.emit(TransactionsEvent.Message("Transaction duplicated."))
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            val transaction = transactionsRepository.getTransaction(transactionId) ?: return@launch
            lastDeletedTransaction = transaction.toDraft()
            transactionsRepository.deleteTransaction(transactionId)
            events.emit(TransactionsEvent.DeletedWithUndo)
        }
    }

    fun restoreLastDeleted() {
        viewModelScope.launch {
            lastDeletedTransaction?.let { draft ->
                transactionsRepository.upsertTransaction(draft)
            }
            lastDeletedTransaction = null
        }
    }
}

private fun Transaction.toDraft(): TransactionDraft = TransactionDraft(
    amountMinor = amountMinor,
    type = type,
    categoryId = category.id,
    accountId = accountId,
    note = note,
    payee = payee,
    tags = tags,
    transactionDate = transactionDate,
    origin = origin,
    recurringTemplateId = recurringTemplateId,
)
