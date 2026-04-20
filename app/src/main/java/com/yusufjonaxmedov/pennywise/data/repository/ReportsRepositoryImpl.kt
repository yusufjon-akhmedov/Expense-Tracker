package com.yusufjonaxmedov.pennywise.data.repository

import com.yusufjonaxmedov.pennywise.core.common.asDateRange
import com.yusufjonaxmedov.pennywise.core.common.toMonthKey
import com.yusufjonaxmedov.pennywise.core.common.toYearMonth
import com.yusufjonaxmedov.pennywise.core.model.ReportSnapshot
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import com.yusufjonaxmedov.pennywise.core.model.DateRange
import com.yusufjonaxmedov.pennywise.data.database.dao.BudgetDao
import com.yusufjonaxmedov.pennywise.data.database.dao.CategoryDao
import com.yusufjonaxmedov.pennywise.data.database.dao.TransactionDao
import com.yusufjonaxmedov.pennywise.domain.repository.ReportsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportsRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
) : ReportsRepository {
    override fun observeReport(range: DateRange): Flow<ReportSnapshot> {
        val monthKey = range.endInclusive.toMonthKey()
        return combine(
            transactionDao.observeMonthSnapshot(range.start.toString(), range.endInclusive.toString()),
            transactionDao.observeExpenseSpendByCategory(range.start.toString(), range.endInclusive.toString()),
            transactionDao.observeMonthlyTrend(range.start.toString(), range.endInclusive.toString()),
            budgetDao.observeBudgetsForMonth(monthKey),
            categoryDao.observeByType(TransactionType.EXPENSE),
        ) { snapshot, categorySpend, trend, budgets, categories ->
            val categoriesById = categories.associateBy { it.id }
            val spendMap = categorySpend.associate { it.categoryId to it.spentMinor }
            val totalExpenseSpend = spendMap.values.sum()

            ReportSnapshot(
                range = range,
                totalIncomeMinor = snapshot.incomeMinor,
                totalExpenseMinor = snapshot.expenseMinor,
                categorySpend = categorySpend.map { it.toModel() },
                monthlyTrend = trend.map { it.toModel() },
                budgetSummaries = budgets.map { budget ->
                    buildBudget(
                        id = budget.id,
                        monthKey = budget.monthKey,
                        category = categoriesById[budget.categoryId]?.toModel(),
                        limitMinor = budget.limitMinor,
                        spentMinor = if (budget.categoryId == 0L) totalExpenseSpend else spendMap[budget.categoryId] ?: 0L,
                        rolloverEnabled = budget.rolloverEnabled,
                        warnThresholdPercent = budget.warnThresholdPercent,
                    )
                },
            )
        }
    }
}
