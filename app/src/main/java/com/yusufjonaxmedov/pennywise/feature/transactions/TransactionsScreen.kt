package com.yusufjonaxmedov.pennywise.feature.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yusufjonaxmedov.pennywise.core.model.SortOption
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import com.yusufjonaxmedov.pennywise.core.ui.EmptyStateCard
import com.yusufjonaxmedov.pennywise.core.ui.SectionHeader
import com.yusufjonaxmedov.pennywise.core.ui.TransactionListItem
import com.yusufjonaxmedov.pennywise.core.ui.readableLabel
import com.yusufjonaxmedov.pennywise.core.ui.rememberDatePickerLauncher
import java.time.LocalDate
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    onManageRecurring: () -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var saveFilterName by rememberSaveable { mutableStateOf("") }
    val currentRange = state.filter.dateRange
    val startPicker = rememberDatePickerLauncher(
        initialDate = currentRange?.asDateRange()?.start ?: LocalDate.now().withDayOfMonth(1),
        onDateSelected = { start ->
            val end = currentRange?.asDateRange()?.endInclusive ?: start
            viewModel.updateDateRange(start.toString(), end.toString())
        },
    )
    val endPicker = rememberDatePickerLauncher(
        initialDate = currentRange?.asDateRange()?.endInclusive ?: LocalDate.now(),
        onDateSelected = { end ->
            val start = currentRange?.asDateRange()?.start ?: end
            viewModel.updateDateRange(start.toString(), end.toString())
        },
    )

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                TransactionsEvent.DeletedWithUndo -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Transaction deleted",
                        actionLabel = "Undo",
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreLastDeleted()
                    }
                }

                is TransactionsEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    val groupedTransactions = state.transactions.groupBy { it.transactionDate }.toSortedMap(compareByDescending { it })

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SectionHeader(
                    title = "Transactions",
                    subtitle = "Search, filter, and reuse the views you lean on most.",
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AssistChip(onClick = onAddExpense, label = { Text("Add expense") })
                    AssistChip(onClick = onAddIncome, label = { Text("Add income") })
                    AssistChip(onClick = onManageRecurring, label = { Text("Recurring") })
                }
            }
            item {
                OutlinedTextField(
                    value = state.filter.searchQuery,
                    onValueChange = viewModel::updateSearch,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search note, payee, or category") },
                    singleLine = true,
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.filter.types.contains(TransactionType.EXPENSE),
                        onClick = { viewModel.toggleType(TransactionType.EXPENSE) },
                        label = { Text("Expenses") },
                    )
                    FilterChip(
                        selected = state.filter.types.contains(TransactionType.INCOME),
                        onClick = { viewModel.toggleType(TransactionType.INCOME) },
                        label = { Text("Income") },
                    )
                    FilterChip(
                        selected = state.filter.sortOption == SortOption.NEWEST_FIRST,
                        onClick = { viewModel.updateSort(SortOption.NEWEST_FIRST) },
                        label = { Text("Newest") },
                    )
                    FilterChip(
                        selected = state.filter.sortOption == SortOption.AMOUNT_HIGH_TO_LOW,
                        onClick = { viewModel.updateSort(SortOption.AMOUNT_HIGH_TO_LOW) },
                        label = { Text("Highest") },
                    )
                }
            }
            if (state.categories.isNotEmpty()) {
                item {
                    SectionHeader(title = "Categories")
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.categories, key = { it.id }) { category ->
                            FilterChip(
                                selected = state.filter.categoryIds.contains(category.id),
                                onClick = { viewModel.toggleCategory(category.id) },
                                label = { Text("${category.emoji} ${category.name}") },
                            )
                        }
                    }
                }
            }
            if (state.accounts.isNotEmpty()) {
                item {
                    SectionHeader(title = "Accounts")
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.accounts, key = { it.id }) { account ->
                            FilterChip(
                                selected = state.filter.accountIds.contains(account.id),
                                onClick = { viewModel.toggleAccount(account.id) },
                                label = { Text(account.name) },
                            )
                        }
                    }
                }
            }
            item {
                SectionHeader(title = "Date range")
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = startPicker,
                        label = { Text("Start: ${currentRange?.start ?: "Any"}") },
                    )
                    AssistChip(
                        onClick = endPicker,
                        label = { Text("End: ${currentRange?.endInclusive ?: "Any"}") },
                    )
                    if (currentRange != null) {
                        AssistChip(
                            onClick = { viewModel.updateDateRange(null, null) },
                            label = { Text("Clear dates") },
                        )
                    }
                }
            }
            if (state.savedFilters.isNotEmpty()) {
                item {
                    SectionHeader(title = "Saved filters")
                }
                items(state.savedFilters, key = { it.id }) { savedFilter ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        AssistChip(
                            onClick = { viewModel.applySavedFilter(savedFilter) },
                            label = { Text(savedFilter.name) },
                        )
                        AssistChip(
                            onClick = { viewModel.deleteSavedFilter(savedFilter.id) },
                            label = { Text("Remove") },
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = saveFilterName,
                    onValueChange = { saveFilterName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Save current filter") },
                    trailingIcon = {
                        AssistChip(
                            onClick = {
                                viewModel.saveCurrentFilter(saveFilterName)
                                saveFilterName = ""
                            },
                            label = { Text("Save") },
                        )
                    },
                )
            }
            item {
                if (state.filter.isEmpty.not()) {
                    AssistChip(
                        onClick = viewModel::clearFilters,
                        label = { Text("Clear all filters") },
                    )
                }
            }
            item {
                SectionHeader(title = "Results", subtitle = "${state.transactions.size} transactions")
            }
            if (state.transactions.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No matching transactions",
                        description = "Try clearing filters or log a new item to build history.",
                    )
                }
            } else {
                groupedTransactions.forEach { (date, transactions) ->
                    item(key = "header-$date") {
                        Text(
                            text = date.readableLabel(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    items(transactions, key = { it.id }) { transaction ->
                        TransactionListItem(
                            transaction = transaction,
                            currencyCode = state.currencyCode,
                            onEdit = { onEditTransaction(transaction.id) },
                            onDuplicate = { viewModel.duplicateTransaction(transaction.id) },
                            onDelete = { viewModel.deleteTransaction(transaction.id) },
                        )
                    }
                }
            }
        }
    }
}
