package com.yusufjonaxmedov.pennywise.feature.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
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
import com.yusufjonaxmedov.pennywise.core.ui.IncomeExpenseTrendChart
import com.yusufjonaxmedov.pennywise.core.ui.MetricCard
import com.yusufjonaxmedov.pennywise.core.ui.SectionHeader

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val report = state.report

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionHeader(
                title = "Reports",
                subtitle = "Useful trends, not decorative charts.",
            )
        }
        item {
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportRangePreset.entries.forEach { preset ->
                    FilterChip(
                        selected = state.preset == preset,
                        onClick = { viewModel.updatePreset(preset) },
                        label = { Text(preset.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
        }
        if (report == null) {
            item {
                EmptyStateCard(
                    title = "Building your report",
                    description = "We’re aggregating local transactions and budgets for the selected range.",
                )
            }
        } else {
            item {
                MetricCard(
                    title = "Income",
                    value = MoneyFormatter.format(report.totalIncomeMinor, state.currencyCode),
                    supportingText = "Captured in the selected range",
                )
            }
            item {
                MetricCard(
                    title = "Expense",
                    value = MoneyFormatter.format(report.totalExpenseMinor, state.currencyCode),
                    supportingText = "Spend across all tracked accounts",
                )
            }
            item {
                if (report.categorySpend.isEmpty()) {
                    EmptyStateCard(
                        title = "No spending distribution yet",
                        description = "Once expenses are logged, category mix and trend charts will appear here.",
                    )
                } else {
                    CategorySpendChart(
                        items = report.categorySpend,
                        currencyCode = state.currencyCode,
                    )
                }
            }
            item {
                if (report.monthlyTrend.isNotEmpty()) {
                    IncomeExpenseTrendChart(items = report.monthlyTrend)
                }
            }
            item {
                SectionHeader(title = "Budget adherence")
            }
            if (report.budgetSummaries.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No active budgets in this report window",
                        description = "Create budgets to compare targets against actual spend.",
                    )
                }
            } else {
                items(report.budgetSummaries, key = { it.id }) { budget ->
                    Text(
                        text = "${budget.category?.name ?: "Monthly total"}: ${MoneyFormatter.format(budget.spentMinor, state.currencyCode)} / ${MoneyFormatter.format(budget.limitMinor, state.currencyCode)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
