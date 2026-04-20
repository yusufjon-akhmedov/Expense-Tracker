package com.yusufjonaxmedov.pennywise.feature.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import com.yusufjonaxmedov.pennywise.core.ui.rememberDatePickerLauncher
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditorScreen(
    onDone: () -> Unit,
    viewModel: TransactionEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val openDatePicker = rememberDatePickerLauncher(
        initialDate = state.transactionDate,
        onDateSelected = viewModel::updateDate,
    )

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                TransactionEditorEvent.Saved -> onDone()
                is TransactionEditorEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
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
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Transaction details",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Capture the amount, category, account, and context cleanly.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.selectedType == TransactionType.EXPENSE,
                        onClick = { viewModel.updateType(TransactionType.EXPENSE) },
                        label = { Text("Expense") },
                    )
                    FilterChip(
                        selected = state.selectedType == TransactionType.INCOME,
                        onClick = { viewModel.updateType(TransactionType.INCOME) },
                        label = { Text("Income") },
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = state.amountInput,
                    onValueChange = viewModel::updateAmount,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount (${state.currencyCode})") },
                    supportingText = state.errors.amount?.let { { Text(it) } },
                    isError = state.errors.amount != null,
                    singleLine = true,
                )
            }
            item {
                DropdownField(
                    label = "Category",
                    selectedLabel = state.categories.firstOrNull { it.id == state.categoryId }?.let { "${it.emoji} ${it.name}" } ?: "Select",
                    options = state.categories.map { it.id to "${it.emoji} ${it.name}" },
                    error = state.errors.category,
                    onSelected = viewModel::updateCategory,
                )
            }
            item {
                DropdownField(
                    label = "Account",
                    selectedLabel = state.accounts.firstOrNull { it.id == state.accountId }?.name ?: "Select",
                    options = state.accounts.map { it.id to it.name },
                    error = state.errors.account,
                    onSelected = viewModel::updateAccount,
                )
            }
            item {
                OutlinedTextField(
                    value = state.payee,
                    onValueChange = viewModel::updatePayee,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Merchant or payee") },
                )
            }
            item {
                OutlinedTextField(
                    value = state.note,
                    onValueChange = viewModel::updateNote,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Note") },
                    supportingText = state.errors.note?.let { { Text(it) } },
                    isError = state.errors.note != null,
                )
            }
            item {
                OutlinedTextField(
                    value = state.tagsInput,
                    onValueChange = viewModel::updateTags,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tags (comma separated)") },
                )
            }
            item {
                Button(
                    onClick = openDatePicker,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Date: ${state.transactionDate}")
                }
            }
            item {
                Button(
                    onClick = viewModel::save,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.saving,
                ) {
                    Text("Save transaction")
                }
            }
        }
    }
}

@Composable
private fun DropdownField(
    label: String,
    selectedLabel: String,
    options: List<Pair<Long, String>>,
    error: String?,
    onSelected: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = { expanded = true },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            supportingText = error?.let { { Text(it) } },
            isError = error != null,
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { (id, title) ->
                DropdownMenuItem(
                    text = { Text(title) },
                    onClick = {
                        expanded = false
                        onSelected(id)
                    },
                )
            }
        }
    }
}
