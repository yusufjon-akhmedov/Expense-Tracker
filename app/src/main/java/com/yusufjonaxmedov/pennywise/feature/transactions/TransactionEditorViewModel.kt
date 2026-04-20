package com.yusufjonaxmedov.pennywise.feature.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yusufjonaxmedov.pennywise.core.common.MoneyParser
import com.yusufjonaxmedov.pennywise.core.model.Account
import com.yusufjonaxmedov.pennywise.core.model.Category
import com.yusufjonaxmedov.pennywise.core.model.TransactionDraft
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import com.yusufjonaxmedov.pennywise.domain.repository.AccountsRepository
import com.yusufjonaxmedov.pennywise.domain.repository.CategoriesRepository
import com.yusufjonaxmedov.pennywise.domain.repository.PreferencesRepository
import com.yusufjonaxmedov.pennywise.domain.repository.TransactionsRepository
import com.yusufjonaxmedov.pennywise.navigation.TransactionEditorRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionEditorErrors(
    val amount: String? = null,
    val category: String? = null,
    val account: String? = null,
    val note: String? = null,
)

data class TransactionEditorUiState(
    val currencyCode: String = "USD",
    val amountInput: String = "",
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val note: String = "",
    val payee: String = "",
    val tagsInput: String = "",
    val transactionDate: LocalDate = LocalDate.now(),
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val errors: TransactionEditorErrors = TransactionEditorErrors(),
    val saving: Boolean = false,
)

sealed interface TransactionEditorEvent {
    data object Saved : TransactionEditorEvent
    data class Message(val text: String) : TransactionEditorEvent
}

@HiltViewModel
class TransactionEditorViewModel @Inject constructor(
    accountsRepository: AccountsRepository,
    categoriesRepository: CategoriesRepository,
    preferencesRepository: PreferencesRepository,
    private val transactionsRepository: TransactionsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<TransactionEditorRoute>()
    private val mutableState = MutableStateFlow(
        TransactionEditorUiState(selectedType = route.transactionType),
    )
    private val events = MutableSharedFlow<TransactionEditorEvent>()

    val eventFlow = events.asSharedFlow()

    val uiState: StateFlow<TransactionEditorUiState> = combine(
        mutableState,
        preferencesRepository.observePreferences(),
        categoriesRepository.observeCategories(),
        accountsRepository.observeAccounts(),
    ) { current, preferences, categories, accounts ->
        current.copy(
            currencyCode = preferences.currencyCode,
            categories = categories.filter { it.type == current.selectedType },
            accounts = accounts.filterNot { it.archived },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = mutableState.value,
    )

    init {
        route.transactionId?.let { transactionId ->
            viewModelScope.launch {
                val transaction = transactionsRepository.getTransaction(transactionId) ?: return@launch
                mutableState.update {
                    it.copy(
                        amountInput = (transaction.amountMinor / 100.0).toString(),
                        selectedType = transaction.type,
                        categoryId = transaction.category.id,
                        accountId = transaction.accountId,
                        note = transaction.note,
                        payee = transaction.payee,
                        tagsInput = transaction.tags.joinToString(", "),
                        transactionDate = transaction.transactionDate,
                    )
                }
            }
        }
    }

    fun updateAmount(value: String) {
        mutableState.update { it.copy(amountInput = value, errors = it.errors.copy(amount = null)) }
    }

    fun updateType(type: TransactionType) {
        mutableState.update { it.copy(selectedType = type, categoryId = null) }
    }

    fun updateCategory(categoryId: Long) {
        mutableState.update { it.copy(categoryId = categoryId, errors = it.errors.copy(category = null)) }
    }

    fun updateAccount(accountId: Long) {
        mutableState.update { it.copy(accountId = accountId, errors = it.errors.copy(account = null)) }
    }

    fun updateNote(value: String) {
        mutableState.update { it.copy(note = value.take(120), errors = it.errors.copy(note = null)) }
    }

    fun updatePayee(value: String) {
        mutableState.update { it.copy(payee = value.take(60)) }
    }

    fun updateTags(value: String) {
        mutableState.update { it.copy(tagsInput = value) }
    }

    fun updateDate(value: LocalDate) {
        mutableState.update { it.copy(transactionDate = value) }
    }

    fun save() {
        viewModelScope.launch {
            val state = uiState.value
            val amountMinor = MoneyParser.parseMinorAmount(state.amountInput)
            val errors = TransactionEditorErrors(
                amount = if (amountMinor == null || amountMinor <= 0) "Enter a valid amount." else null,
                category = if (state.categoryId == null) "Choose a category." else null,
                account = if (state.accountId == null) "Choose an account." else null,
                note = if (state.note.length > 120) "Keep notes under 120 characters." else null,
            )
            if (listOf(errors.amount, errors.category, errors.account, errors.note).any { it != null }) {
                mutableState.update { it.copy(errors = errors) }
                return@launch
            }

            mutableState.update { it.copy(saving = true) }
            transactionsRepository.upsertTransaction(
                TransactionDraft(
                    id = route.transactionId,
                    amountMinor = amountMinor ?: return@launch,
                    type = state.selectedType,
                    categoryId = state.categoryId ?: return@launch,
                    accountId = state.accountId ?: return@launch,
                    note = state.note,
                    payee = state.payee,
                    tags = state.tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    transactionDate = state.transactionDate,
                ),
            )
            mutableState.update { it.copy(saving = false) }
            events.emit(TransactionEditorEvent.Saved)
        }
    }
}
