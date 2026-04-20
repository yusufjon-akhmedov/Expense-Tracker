package com.yusufjonaxmedov.pennywise.feature.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufjonaxmedov.pennywise.core.common.MoneyParser
import com.yusufjonaxmedov.pennywise.core.model.Account
import com.yusufjonaxmedov.pennywise.core.model.Category
import com.yusufjonaxmedov.pennywise.core.model.RecurringFrequency
import com.yusufjonaxmedov.pennywise.core.model.RecurringTemplate
import com.yusufjonaxmedov.pennywise.core.model.RecurringTemplateDraft
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import com.yusufjonaxmedov.pennywise.domain.repository.AccountsRepository
import com.yusufjonaxmedov.pennywise.domain.repository.CategoriesRepository
import com.yusufjonaxmedov.pennywise.domain.repository.PreferencesRepository
import com.yusufjonaxmedov.pennywise.domain.repository.RecurringRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecurringTemplatesUiState(
    val currencyCode: String = "USD",
    val dueCount: Int = 0,
    val templates: List<RecurringTemplate> = emptyList(),
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
)

@HiltViewModel
class RecurringTemplatesViewModel @Inject constructor(
    private val recurringRepository: RecurringRepository,
    categoriesRepository: CategoriesRepository,
    accountsRepository: AccountsRepository,
    preferencesRepository: PreferencesRepository,
) : ViewModel() {
    private val events = MutableSharedFlow<String>()
    val eventFlow = events.asSharedFlow()

    val uiState: StateFlow<RecurringTemplatesUiState> = combine(
        preferencesRepository.observePreferences(),
        recurringRepository.observeDueCount(),
        recurringRepository.observeTemplates(),
        categoriesRepository.observeCategories(),
        accountsRepository.observeAccounts(),
    ) { preferences, dueCount, templates, categories, accounts ->
        RecurringTemplatesUiState(
            currencyCode = preferences.currencyCode,
            dueCount = dueCount,
            templates = templates,
            categories = categories,
            accounts = accounts.filterNot { it.archived },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecurringTemplatesUiState(),
    )

    fun applyDue() {
        viewModelScope.launch {
            val count = recurringRepository.applyDueTemplates()
            events.emit(if (count == 0) "No due recurring items right now." else "$count recurring transactions created.")
        }
    }

    fun saveTemplate(
        templateId: Long?,
        name: String,
        amountInput: String,
        type: TransactionType,
        categoryId: Long,
        accountId: Long,
        note: String,
        payee: String,
        frequency: RecurringFrequency,
        nextDate: LocalDate,
    ) {
        viewModelScope.launch {
            val amountMinor = MoneyParser.parseMinorAmount(amountInput)
            if (amountMinor == null || amountMinor <= 0) {
                events.emit("Enter a valid recurring amount.")
                return@launch
            }
            recurringRepository.upsertTemplate(
                RecurringTemplateDraft(
                    id = templateId,
                    name = name,
                    amountMinor = amountMinor,
                    type = type,
                    categoryId = categoryId,
                    accountId = accountId,
                    note = note,
                    payee = payee,
                    tags = emptyList(),
                    frequency = frequency,
                    intervalValue = 1,
                    nextOccurrenceDate = nextDate,
                ),
            )
        }
    }

    fun deleteTemplate(templateId: Long) {
        viewModelScope.launch {
            recurringRepository.deleteTemplate(templateId)
        }
    }
}
