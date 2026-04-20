package com.yusufjonaxmedov.pennywise.core.model

data class DashboardSummary(
    val monthKey: String,
    val incomeMinor: Long,
    val expenseMinor: Long,
    val totalBudgetMinor: Long,
    val topCategories: List<CategorySpend>,
    val recentTransactions: List<Transaction>,
    val dueRecurringCount: Int,
) {
    val remainingBudgetMinor: Long = totalBudgetMinor - expenseMinor
}
