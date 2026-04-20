package com.yusufjonaxmedov.pennywise.core.model

data class CategorySpend(
    val category: Category,
    val amountMinor: Long,
)

data class MonthlyTrendPoint(
    val monthKey: String,
    val incomeMinor: Long,
    val expenseMinor: Long,
)

data class ReportSnapshot(
    val range: DateRange,
    val totalIncomeMinor: Long,
    val totalExpenseMinor: Long,
    val categorySpend: List<CategorySpend>,
    val monthlyTrend: List<MonthlyTrendPoint>,
    val budgetSummaries: List<Budget>,
)
