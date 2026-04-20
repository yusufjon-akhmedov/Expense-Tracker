package com.yusufjonaxmedov.pennywise.data.repository

import com.yusufjonaxmedov.pennywise.core.common.asDateRange
import com.yusufjonaxmedov.pennywise.core.common.toYearMonth
import com.yusufjonaxmedov.pennywise.core.model.Budget
import com.yusufjonaxmedov.pennywise.core.model.BudgetDraft
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import com.yusufjonaxmedov.pennywise.data.database.dao.BudgetDao
import com.yusufjonaxmedov.pennywise.data.database.dao.CategoryDao
import com.yusufjonaxmedov.pennywise.data.database.dao.TransactionDao
import com.yusufjonaxmedov.pennywise.data.database.entity.BudgetEntity
import com.yusufjonaxmedov.pennywise.domain.repository.BudgetsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetsRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
) : BudgetsRepository {
    override fun observeBudgets(monthKey: String): Flow<List<Budget>> {
        val range = monthKey.toYearMonth().asDateRange()
        return combine(
            budgetDao.observeBudgetsForMonth(monthKey),
            categoryDao.observeByType(TransactionType.EXPENSE),
            transactionDao.observeExpenseSpendByCategory(range.start.toString(), range.endInclusive.toString()),
        ) { budgets, categories, spends ->
            val categoryMap = categories.associateBy { it.id }
            val spendMap = spends.associate { it.categoryId to it.spentMinor }
            val totalExpenseSpend = spendMap.values.sum()
            budgets.map { budget ->
                val category = categoryMap[budget.categoryId]?.toModel()
                val spent = if (budget.categoryId == 0L) totalExpenseSpend else spendMap[budget.categoryId] ?: 0L
                buildBudget(
                    id = budget.id,
                    monthKey = budget.monthKey,
                    category = category,
                    limitMinor = budget.limitMinor,
                    spentMinor = spent,
                    rolloverEnabled = budget.rolloverEnabled,
                    warnThresholdPercent = budget.warnThresholdPercent,
                )
            }
        }
    }

    override suspend fun getBudget(budgetId: Long): Budget? {
        val budget = budgetDao.getById(budgetId) ?: return null
        return observeBudgets(budget.monthKey).first().firstOrNull { it.id == budgetId }
    }

    override suspend fun upsertBudget(draft: BudgetDraft): Long {
        require(draft.categoryId >= 0) { "Budget category scope must be valid." }
        val entity = BudgetEntity(
            id = draft.id ?: 0,
            monthKey = draft.monthKey,
            categoryId = draft.categoryId,
            limitMinor = draft.limitMinor,
            rolloverEnabled = draft.rolloverEnabled,
            warnThresholdPercent = draft.warnThresholdPercent,
        )
        return if (draft.id == null) {
            budgetDao.insert(entity)
        } else {
            budgetDao.update(entity)
            draft.id
        }
    }

    override suspend fun deleteBudget(budgetId: Long) {
        budgetDao.deleteById(budgetId)
    }
}
