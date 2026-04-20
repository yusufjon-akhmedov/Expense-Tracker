package com.yusufjonaxmedov.pennywise.feature.recurring

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
import com.yusufjonaxmedov.pennywise.core.model.RecurringFrequency
import com.yusufjonaxmedov.pennywise.core.model.RecurringTemplate
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import com.yusufjonaxmedov.pennywise.core.ui.EmptyStateCard
import com.yusufjonaxmedov.pennywise.core.ui.SectionHeader
import com.yusufjonaxmedov.pennywise.core.ui.rememberDatePickerLauncher
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate

@Composable
fun RecurringTemplatesScreen(
    onBack: () -> Unit,
    viewModel: RecurringTemplatesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var editing by remember { mutableStateOf<RecurringTemplate?>(null) }

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
                    title = "Recurring templates",
                    subtitle = "${state.dueCount} due right now",
                    actionLabel = "Back",
                    onActionClick = onBack,
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = viewModel::applyDue, label = { Text("Apply due items") })
                    AssistChip(
                        onClick = {
                            val category = state.categories.firstOrNull() ?: return@AssistChip
                            val account = state.accounts.firstOrNull() ?: return@AssistChip
                            editing = RecurringTemplate(
                                id = 0,
                                name = "",
                                amountMinor = 0,
                                type = TransactionType.EXPENSE,
                                category = category,
                                accountId = account.id,
                                accountName = account.name,
                                accountType = account.type,
                                note = "",
                                payee = "",
                                tags = emptyList(),
                                frequency = RecurringFrequency.MONTHLY,
                                intervalValue = 1,
                                dayOfMonth = null,
                                dayOfWeekIso = null,
                                nextOccurrenceDate = LocalDate.now(),
                                active = true,
                            )
                        },
                        label = { Text("Add template") },
                    )
                }
            }
            if (state.templates.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No recurring templates yet",
                        description = "Create repeating income or expense patterns, then apply due items whenever you want a clean catch-up flow.",
                    )
                }
            } else {
                items(state.templates, key = { it.id }) { template ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(template.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${template.frequency.name.lowercase().replaceFirstChar { it.uppercase() }} • next ${template.nextOccurrenceDate}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = { editing = template }, label = { Text("Edit") })
                            AssistChip(onClick = { viewModel.deleteTemplate(template.id) }, label = { Text("Delete") })
                        }
                    }
                }
            }
        }
    }

    editing?.let { template ->
        RecurringEditorDialog(
            template = template.takeIf { it.id != 0L },
            categories = state.categories,
            accounts = state.accounts,
            onDismiss = { editing = null },
            onSave = { name, amount, type, categoryId, accountId, note, payee, frequency, nextDate ->
                viewModel.saveTemplate(template.takeIf { it.id != 0L }?.id, name, amount, type, categoryId, accountId, note, payee, frequency, nextDate)
                editing = null
            },
        )
    }
}

@Composable
private fun RecurringEditorDialog(
    template: RecurringTemplate?,
    categories: List<com.yusufjonaxmedov.pennywise.core.model.Category>,
    accounts: List<com.yusufjonaxmedov.pennywise.core.model.Account>,
    onDismiss: () -> Unit,
    onSave: (String, String, TransactionType, Long, Long, String, String, RecurringFrequency, LocalDate) -> Unit,
) {
    var name by remember(template) { mutableStateOf(template?.name.orEmpty()) }
    var amount by remember(template) { mutableStateOf(((template?.amountMinor ?: 0L) / 100.0).toString()) }
    var type by remember(template) { mutableStateOf(template?.type ?: TransactionType.EXPENSE) }
    var categoryId by remember(template) { mutableStateOf(template?.category?.id ?: categories.firstOrNull { it.type == type }?.id ?: 0L) }
    var accountId by remember(template) { mutableStateOf(template?.accountId ?: accounts.firstOrNull()?.id ?: 0L) }
    var note by remember(template) { mutableStateOf(template?.note.orEmpty()) }
    var payee by remember(template) { mutableStateOf(template?.payee.orEmpty()) }
    var frequency by remember(template) { mutableStateOf(template?.frequency ?: RecurringFrequency.MONTHLY) }
    var nextDate by remember(template) { mutableStateOf(template?.nextOccurrenceDate ?: LocalDate.now()) }
    val openDatePicker = rememberDatePickerLauncher(nextDate) { nextDate = it }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = { onSave(name, amount, type, categoryId, accountId, note, payee, frequency, nextDate) }) { Text("Save") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(if (template == null) "Recurring template" else "Edit recurring template") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionType.entries.forEach { option ->
                        FilterChip(selected = type == option, onClick = { type = option }, label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) })
                    }
                }
                categories.filter { it.type == type }.forEach { category ->
                    FilterChip(selected = categoryId == category.id, onClick = { categoryId = category.id }, label = { Text("${category.emoji} ${category.name}") })
                }
                accounts.forEach { account ->
                    FilterChip(selected = accountId == account.id, onClick = { accountId = account.id }, label = { Text(account.name) })
                }
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") })
                OutlinedTextField(value = payee, onValueChange = { payee = it }, label = { Text("Payee") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecurringFrequency.entries.forEach { option ->
                        FilterChip(selected = frequency == option, onClick = { frequency = option }, label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) })
                    }
                }
                AssistChip(onClick = openDatePicker, label = { Text("Next date: $nextDate") })
            }
        },
    )
}
