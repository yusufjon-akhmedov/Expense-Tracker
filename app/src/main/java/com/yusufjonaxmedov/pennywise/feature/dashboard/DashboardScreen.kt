package com.yusufjonaxmedov.pennywise.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yusufjonaxmedov.pennywise.core.common.MoneyFormatter
import com.yusufjonaxmedov.pennywise.core.ui.CategorySpendChart
import com.yusufjonaxmedov.pennywise.core.ui.EmptyStateCard
import com.yusufjonaxmedov.pennywise.core.ui.MetricCard
import com.yusufjonaxmedov.pennywise.core.ui.SectionHeader
import com.yusufjonaxmedov.pennywise.core.ui.TransactionListItem
import com.yusufjonaxmedov.pennywise.core.ui.monthLabel

@Composable
fun DashboardScreen(
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onOpenBudgets: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenRecurring: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val summary = state.summary
    val currencyCode = state.preferences.currencyCode

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = summary?.monthKey?.monthLabel() ?: "This month",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "A quick read on cash flow, budgets, and your latest activity.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(onClick = onAddExpense, label = { Text("Add expense") })
                AssistChip(onClick = onAddIncome, label = { Text("Add income") })
                AssistChip(onClick = onOpenReports, label = { Text("Reports") })
                AssistChip(onClick = onOpenBudgets, label = { Text("Budgets") })
            }
        }
        if (summary != null) {
            item {
                MetricCard(
                    title = "Income",
                    value = MoneyFormatter.format(summary.incomeMinor, currencyCode),
                    supportingText = "Recorded for ${summary.monthKey.monthLabel()}",
                )
            }
            item {
                MetricCard(
                    title = "Expenses",
                    value = MoneyFormatter.format(summary.expenseMinor, currencyCode),
                    supportingText = "Tracked spending this month",
                )
            }
            item {
                MetricCard(
                    title = "Remaining budget",
                    value = MoneyFormatter.format(summary.remainingBudgetMinor, currencyCode),
                    supportingText = if (summary.totalBudgetMinor > 0) {
                        "Based on your active monthly budgets"
                    } else {
                        "Set a budget to keep this number grounded"
                    },
                )
            }
            if (summary.dueRecurringCount > 0) {
                item {
                    EmptyStateCard(
                        title = "${summary.dueRecurringCount} recurring items are ready",
                        description = "Apply them to keep this month’s view accurate without retyping the same transaction.",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AssistChip(
                        onClick = onOpenRecurring,
                        label = { Text("Review recurring") },
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
            item {
                SectionHeader(
                    title = "Top spending categories",
                    subtitle = "Where most of the month has gone so far",
                )
            }
            item {
                if (summary.topCategories.isEmpty()) {
                    EmptyStateCard(
                        title = "No expense trends yet",
                        description = "Add a few transactions and the dashboard will surface your heaviest categories here.",
                    )
                } else {
                    CategorySpendChart(
                        items = summary.topCategories,
                        currencyCode = currencyCode,
                    )
                }
            }
            item {
                SectionHeader(
                    title = "Recent transactions",
                    subtitle = "Latest activity across your accounts",
                    actionLabel = "See all",
                    onActionClick = onOpenTransactions,
                )
            }
            if (summary.recentTransactions.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "Nothing logged yet",
                        description = "Start with one expense or income entry and the home screen will begin building useful context.",
                    )
                }
            } else {
                items(summary.recentTransactions, key = { it.id }) { transaction ->
                    TransactionListItem(
                        transaction = transaction,
                        currencyCode = currencyCode,
                        onEdit = onOpenTransactions,
                        onDuplicate = onOpenTransactions,
                        onDelete = {},
                    )
                }
            }
        } else {
            item {
                EmptyStateCard(
                    title = "Preparing your dashboard",
                    description = "We’re loading local summaries and recent activity.",
                )
            }
        }
    }
}
