package com.yusufjonaxmedov.pennywise.feature.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufjonaxmedov.pennywise.core.common.MoneyParser
import com.yusufjonaxmedov.pennywise.core.model.Account
import com.yusufjonaxmedov.pennywise.core.model.AccountDraft
import com.yusufjonaxmedov.pennywise.core.model.AccountType
import com.yusufjonaxmedov.pennywise.domain.repository.AccountsRepository
import com.yusufjonaxmedov.pennywise.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountsUiState(
    val currencyCode: String = "USD",
    val accounts: List<Account> = emptyList(),
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository,
    preferencesRepository: PreferencesRepository,
) : ViewModel() {
    private val events = MutableSharedFlow<String>()
    val eventFlow = events.asSharedFlow()

    val uiState: StateFlow<AccountsUiState> = combine(
        preferencesRepository.observePreferences(),
        accountsRepository.observeAccounts(),
    ) { preferences, accounts ->
        AccountsUiState(
            currencyCode = preferences.currencyCode,
            accounts = accounts,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccountsUiState(),
    )

    fun saveAccount(accountId: Long?, name: String, type: AccountType, initialBalanceInput: String, archived: Boolean) {
        viewModelScope.launch {
            val initialBalanceMinor = MoneyParser.parseMinorAmount(initialBalanceInput) ?: 0L
            accountsRepository.upsertAccount(
                AccountDraft(
                    id = accountId,
                    name = name,
                    type = type,
                    initialBalanceMinor = initialBalanceMinor,
                    archived = archived,
                ),
            )
        }
    }

    fun deleteAccount(accountId: Long) {
        viewModelScope.launch {
            runCatching { accountsRepository.deleteAccount(accountId) }
                .onFailure { events.emit(it.message ?: "Unable to delete account.") }
        }
    }
}
