package com.yusufjonaxmedov.pennywise.data.repository

import com.yusufjonaxmedov.pennywise.core.common.ClockProvider
import com.yusufjonaxmedov.pennywise.core.common.asDateRange
import com.yusufjonaxmedov.pennywise.core.common.toYearMonth
import com.yusufjonaxmedov.pennywise.core.model.DashboardSummary
import com.yusufjonaxmedov.pennywise.core.model.SavedFilter
import com.yusufjonaxmedov.pennywise.core.model.Transaction
import com.yusufjonaxmedov.pennywise.core.model.TransactionDraft
import com.yusufjonaxmedov.pennywise.core.model.TransactionFilter
import com.yusufjonaxmedov.pennywise.data.database.TransactionQueryFactory
import com.yusufjonaxmedov.pennywise.data.database.dao.BudgetDao
import com.yusufjonaxmedov.pennywise.data.database.dao.RecurringTemplateDao
import com.yusufjonaxmedov.pennywise.data.database.dao.SavedFilterDao
import com.yusufjonaxmedov.pennywise.data.database.dao.TransactionDao
import com.yusufjonaxmedov.pennywise.data.database.entity.SavedFilterEntity
import com.yusufjonaxmedov.pennywise.data.database.entity.TransactionEntity
import com.yusufjonaxmedov.pennywise.domain.repository.TransactionsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionsRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val clockProvider: ClockProvider,
    private val json: Json,
    private val recurringTemplateDao: RecurringTemplateDao,
    private val savedFilterDao: SavedFilterDao,
    private val transactionDao: TransactionDao,
) : TransactionsRepository {
    override fun observeDashboard(monthKey: String): Flow<DashboardSummary> {
        val range = monthKey.toYearMonth().asDateRange()
        return combine(
            transactionDao.observeMonthSnapshot(range.start.toString(), range.endInclusive.toString()),
            transactionDao.observeTopCategorySpend(range.start.toString(), range.endInclusive.toString(), 4),
            transactionDao.observeFiltered(TransactionQueryFactory.create(TransactionFilter(), limit = 5)),
            budgetDao.observeBudgetsForMonth(monthKey),
            recurringTemplateDao.observeDueTemplateCount(clockProvider.currentDate()),
        ) { snapshot, topCategories, recentTransactions, budgets, dueCount ->
            val totalBudget = budgets.find { it.categoryId == 0L }?.limitMinor
                ?: budgets.filter { it.categoryId != 0L }.sumOf { it.limitMinor }
            DashboardSummary(
                monthKey = monthKey,
                incomeMinor = snapshot.incomeMinor,
                expenseMinor = snapshot.expenseMinor,
                totalBudgetMinor = totalBudget,
                topCategories = topCategories.map { it.toModel() },
                recentTransactions = recentTransactions.map { it.toModel(json) },
                dueRecurringCount = dueCount,
            )
        }
    }

    override fun observeTransactions(filter: TransactionFilter): Flow<List<Transaction>> =
        transactionDao.observeFiltered(TransactionQueryFactory.create(filter))
            .map { rows -> rows.map { it.toModel(json) } }

    override fun observeSavedFilters(): Flow<List<SavedFilter>> =
        savedFilterDao.observeAll().map { items -> items.map { it.toModel(json) } }

    override suspend fun getTransaction(transactionId: Long): Transaction? =
        transactionDao.listFiltered(
            TransactionQueryFactory.create(
                filter = TransactionFilter(),
                transactionId = transactionId,
                limit = 1,
            ),
        ).firstOrNull()?.toModel(json)

    override suspend fun upsertTransaction(draft: TransactionDraft): Long {
        val existing = if (draft.id != null) transactionDao.getById(draft.id) else null
        val entity = TransactionEntity(
            id = draft.id ?: 0,
            amountMinor = draft.amountMinor,
            type = draft.type,
            categoryId = draft.categoryId,
            accountId = draft.accountId,
            note = draft.note.trim(),
            payee = draft.payee.trim(),
            tagsJson = json.encodeStringList(draft.tags),
            transactionDate = draft.transactionDate,
            origin = draft.origin,
            recurringTemplateId = draft.recurringTemplateId,
            createdAt = existing?.createdAt ?: clockProvider.currentInstant(),
            updatedAt = clockProvider.currentInstant(),
        )
        return if (draft.id == null) {
            transactionDao.insert(entity)
        } else {
            transactionDao.update(entity)
            draft.id
        }
    }

    override suspend fun duplicateTransaction(transactionId: Long): Long {
        val existing = transactionDao.getById(transactionId) ?: error("Transaction not found.")
        val duplicated = existing.copy(
            id = 0,
            createdAt = clockProvider.currentInstant(),
            updatedAt = clockProvider.currentInstant(),
            transactionDate = clockProvider.currentDate(),
            origin = com.yusufjonaxmedov.pennywise.core.model.TransactionOrigin.MANUAL,
            recurringTemplateId = null,
        )
        return transactionDao.insert(duplicated)
    }

    override suspend fun deleteTransaction(transactionId: Long) {
        transactionDao.deleteById(transactionId)
    }

    override suspend fun saveFilter(name: String, filter: TransactionFilter, pinned: Boolean): Long {
        val dateRange = filter.dateRange?.asDateRange()
        return savedFilterDao.insert(
            SavedFilterEntity(
                name = name.trim(),
                searchQuery = filter.searchQuery.trim(),
                categoryIdsJson = json.encodeLongSet(filter.categoryIds),
                accountIdsJson = json.encodeLongSet(filter.accountIds),
                typesJson = json.encodeTypeSet(filter.types),
                startDate = dateRange?.start,
                endDate = dateRange?.endInclusive,
                sortOption = filter.sortOption,
                pinned = pinned,
                createdAt = clockProvider.currentInstant(),
            ),
        )
    }

    override suspend fun deleteSavedFilter(savedFilterId: Long) {
        savedFilterDao.deleteById(savedFilterId)
    }

    override suspend fun exportTransactions(filter: TransactionFilter): List<Transaction> =
        transactionDao.listFiltered(TransactionQueryFactory.create(filter)).map { it.toModel(json) }
}
