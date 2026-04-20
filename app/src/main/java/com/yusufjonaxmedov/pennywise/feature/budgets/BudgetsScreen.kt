package com.yusufjonaxmedov.pennywise.feature.budgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yusufjonaxmedov.pennywise.core.common.MoneyFormatter
import com.yusufjonaxmedov.pennywise.core.ui.BudgetProgressBar
import com.yusufjonaxmedov.pennywise.core.ui.EmptyStateCard
import com.yusufjonaxmedov.pennywise.core.ui.SectionHeader
import com.yusufjonaxmedov.pennywise.core.ui.monthLabel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BudgetsScreen(
    viewModel: BudgetsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var dialogState by remember { mutableStateOf<BudgetDialogState?>(null) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest(snackbarHostState::showSnackbar)
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SectionHeader(
                    title = "Budgets",
                    subtitle = state.monthKey.monthLabel(),
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = viewModel::previousMonth, label = { Text("Previous") })
                    AssistChip(onClick = viewModel::nextMonth, label = { Text("Next") })
                    AssistChip(
                        onClick = { dialogState = BudgetDialogState() },
                        label = { Text("New budget") },
                    )
                }
            }
            if (!state.budgetsEnabled) {
                item {
                    EmptyStateCard(
                        title = "Budgets are disabled",
                        description = "Turn them on in Settings if you want spending targets and progress indicators.",
                    )
                }
            }
            if (state.budgets.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No budgets for this month",
                        description = "Create a total budget or set category-level limits for tighter visibility.",
                    )
                }
            } else {
                items(state.budgets, key = { it.id }) { budget ->
                    Card(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = budget.category?.let { "${it.emoji} ${it.name}" } ?: "Monthly total",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                AssistChip(
                                    onClick = { viewModel.deleteBudget(budget.id) },
                                    label = { Text("Delete") },
                                )
                            }
                            Text(
                                text = "${MoneyFormatter.format(budget.spentMinor, state.currencyCode)} of ${MoneyFormatter.format(budget.limitMinor, state.currencyCode)}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            BudgetProgressBar(progress = budget.progress, status = budget.status)
                            Text(
                                text = "Remaining: ${MoneyFormatter.format(budget.remainingMinor, state.currencyCode)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    dialogState?.let { dialog ->
        BudgetEditorDialog(
            categories = state.categories,
            onDismiss = { dialogState = null },
            onSave = { categoryId, amount, rollover ->
                viewModel.saveBudget(
                    budgetId = dialog.budgetId,
                    categoryId = categoryId,
                    amountInput = amount,
                    rolloverEnabled = rollover,
                )
                dialogState = null
            },
        )
    }
}

private data class BudgetDialogState(val budgetId: Long? = null)

@Composable
private fun BudgetEditorDialog(
    categories: List<com.yusufjonaxmedov.pennywise.core.model.Category>,
    onDismiss: () -> Unit,
    onSave: (Long, String, Boolean) -> Unit,
) {
    var selectedCategoryId by remember { mutableStateOf(0L) }
    var amount by remember { mutableStateOf("") }
    var rolloverEnabled by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onSave(selectedCategoryId, amount, rolloverEnabled) }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Category scope", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedCategoryId == 0L,
                        onClick = { selectedCategoryId = 0L },
                        label = { Text("Monthly total") },
                    )
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategoryId == category.id,
                            onClick = { selectedCategoryId = category.id },
                            label = { Text("${category.emoji} ${category.name}") },
                        )
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Budget amount") },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = rolloverEnabled, onCheckedChange = { rolloverEnabled = it })
                    Text("Roll over unused amount")
                }
            }
        },
    )
}
