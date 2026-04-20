package com.yusufjonaxmedov.pennywise.feature.transactions

import app.cash.turbine.test
import com.yusufjonaxmedov.pennywise.core.model.Account
import com.yusufjonaxmedov.pennywise.core.model.AccountDraft
import com.yusufjonaxmedov.pennywise.core.model.AccountType
import com.yusufjonaxmedov.pennywise.core.model.AppPreferencesModel
import com.yusufjonaxmedov.pennywise.core.model.Category
import com.yusufjonaxmedov.pennywise.core.model.CategoryDraft
import com.yusufjonaxmedov.pennywise.core.model.DashboardSummary
import com.yusufjonaxmedov.pennywise.core.model.SavedFilter
import com.yusufjonaxmedov.pennywise.core.model.ThemeMode
import com.yusufjonaxmedov.pennywise.core.model.Transaction
import com.yusufjonaxmedov.pennywise.core.model.TransactionDraft
import com.yusufjonaxmedov.pennywise.core.model.TransactionFilter
import com.yusufjonaxmedov.pennywise.core.model.TransactionOrigin
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import com.yusufjonaxmedov.pennywise.core.model.WeekStart
import com.yusufjonaxmedov.pennywise.core.testing.MainDispatcherRule
import com.yusufjonaxmedov.pennywise.domain.repository.AccountsRepository
import com.yusufjonaxmedov.pennywise.domain.repository.CategoriesRepository
import com.yusufjonaxmedov.pennywise.domain.repository.PreferencesRepository
import com.yusufjonaxmedov.pennywise.domain.repository.TransactionsRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TransactionsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `search updates state and filters emitted list`() = runTest {
        val repository = FakeTransactionsRepository()
        val viewModel = TransactionsViewModel(
            accountsRepository = FakeAccountsRepository(),
            categoriesRepository = FakeCategoriesRepository(),
            preferencesRepository = FakePreferencesRepository(),
            transactionsRepository = repository,
        )

        viewModel.uiState.test {
            awaitItem()
            viewModel.updateSearch("coffee")
            var updated = awaitItem()
            while (updated.filter.searchQuery != "coffee") {
                updated = awaitItem()
            }
            assertEquals("coffee", updated.filter.searchQuery)
            assertEquals(1, updated.transactions.size)
        }
    }

    @Test
    fun `delete and restore returns transaction to list`() = runTest {
        val repository = FakeTransactionsRepository()
        val viewModel = TransactionsViewModel(
            accountsRepository = FakeAccountsRepository(),
            categoriesRepository = FakeCategoriesRepository(),
            preferencesRepository = FakePreferencesRepository(),
            transactionsRepository = repository,
        )

        viewModel.deleteTransaction(1)
        assertEquals(1, repository.transactions.value.size)

        viewModel.restoreLastDeleted()
        assertEquals(2, repository.transactions.value.size)
    }
}

private class FakeTransactionsRepository : TransactionsRepository {
    val transactions = MutableStateFlow(
        listOf(
            sampleTransaction(id = 1, note = "Morning coffee"),
            sampleTransaction(id = 2, note = "Salary"),
        ),
    )
    private val savedFilters = MutableStateFlow(emptyList<SavedFilter>())

    override fun observeDashboard(monthKey: String): Flow<DashboardSummary> = MutableStateFlow(
        DashboardSummary(
            monthKey = monthKey,
            incomeMinor = 20_000L,
            expenseMinor = 500L,
            totalBudgetMinor = 10_000L,
            topCategories = emptyList(),
            recentTransactions = transactions.value,
            dueRecurringCount = 0,
        ),
    )

    override fun observeTransactions(filter: TransactionFilter): Flow<List<Transaction>> = transactions.map { items ->
        items.filter { transaction ->
            val queryMatch = filter.searchQuery.isBlank() || transaction.note.contains(filter.searchQuery, ignoreCase = true)
            val typeMatch = filter.types.isEmpty() || filter.types.contains(transaction.type)
            queryMatch && typeMatch
        }
    }

    override fun observeSavedFilters(): Flow<List<SavedFilter>> = savedFilters

    override suspend fun getTransaction(transactionId: Long): Transaction? =
        transactions.value.firstOrNull { it.id == transactionId }

    override suspend fun upsertTransaction(draft: TransactionDraft): Long {
        val id = draft.id ?: (transactions.value.maxOfOrNull { it.id } ?: 0L) + 1
        transactions.update { current ->
            current.filterNot { it.id == id } + sampleTransaction(id = id, note = draft.note, type = draft.type)
        }
        return id
    }

    override suspend fun duplicateTransaction(transactionId: Long): Long = transactionId + 100

    override suspend fun deleteTransaction(transactionId: Long) {
        transactions.update { current -> current.filterNot { it.id == transactionId } }
    }

    override suspend fun saveFilter(name: String, filter: TransactionFilter, pinned: Boolean): Long = 1L

    override suspend fun deleteSavedFilter(savedFilterId: Long) = Unit

    override suspend fun exportTransactions(filter: TransactionFilter): List<Transaction> = transactions.value
}

private class FakeAccountsRepository : AccountsRepository {
    private val accounts = MutableStateFlow(
        listOf(Account(1, "Cash", AccountType.CASH, 0, 0, false)),
    )

    override fun observeAccounts(): Flow<List<Account>> = accounts
    override suspend fun getAccount(accountId: Long): Account? = accounts.value.first()
    override suspend fun upsertAccount(draft: AccountDraft): Long = draft.id ?: 1L
    override suspend fun deleteAccount(accountId: Long) = Unit
}

private class FakeCategoriesRepository : CategoriesRepository {
    private val categories = MutableStateFlow(
        listOf(
            Category(1, "Coffee", "☕", "#155EEF", TransactionType.EXPENSE, false),
            Category(2, "Salary", "💼", "#117A65", TransactionType.INCOME, false),
        ),
    )

    override fun observeCategories(type: TransactionType?): Flow<List<Category>> = categories.map { items ->
        if (type == null) items else items.filter { it.type == type }
    }

    override suspend fun getCategory(categoryId: Long): Category? =
        categories.value.firstOrNull { it.id == categoryId }

    override suspend fun upsertCategory(draft: CategoryDraft): Long = draft.id ?: 1L

    override suspend fun deleteCategory(categoryId: Long, replacementCategoryId: Long?) = Unit
}

private class FakePreferencesRepository : PreferencesRepository {
    private val state = MutableStateFlow(AppPreferencesModel())

    override fun observePreferences(): Flow<AppPreferencesModel> = state
    override suspend fun setOnboardingCompleted(completed: Boolean) = Unit
    override suspend fun updateCurrency(currencyCode: String) = Unit
    override suspend fun updateBudgetsEnabled(enabled: Boolean) = Unit
    override suspend fun updateThemeMode(themeMode: ThemeMode) = Unit
    override suspend fun updateWeekStart(weekStart: WeekStart) = Unit
    override suspend fun updateHapticsEnabled(enabled: Boolean) = Unit
    override suspend fun updateReminder(enabled: Boolean, hour: Int, minute: Int) = Unit
}

private fun sampleTransaction(
    id: Long,
    note: String,
    type: TransactionType = TransactionType.EXPENSE,
): Transaction {
    val category = if (type == TransactionType.EXPENSE) {
        Category(1, "Coffee", "☕", "#155EEF", type, false)
    } else {
        Category(2, "Salary", "💼", "#117A65", type, false)
    }
    return Transaction(
        id = id,
        amountMinor = if (type == TransactionType.EXPENSE) 500L else 20_000L,
        type = type,
        category = category,
        accountId = 1L,
        accountName = "Cash",
        accountType = AccountType.CASH,
        note = note,
        payee = "",
        tags = emptyList(),
        transactionDate = LocalDate.of(2026, 4, 20),
        origin = TransactionOrigin.MANUAL,
        recurringTemplateId = null,
    )
}
