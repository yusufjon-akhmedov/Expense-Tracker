package com.yusufjonaxmedov.pennywise.feature.accounts

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yusufjonaxmedov.pennywise.core.common.MoneyFormatter
import com.yusufjonaxmedov.pennywise.core.model.Account
import com.yusufjonaxmedov.pennywise.core.model.AccountType
import com.yusufjonaxmedov.pennywise.core.ui.EmptyStateCard
import com.yusufjonaxmedov.pennywise.core.ui.SectionHeader
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AccountsScreen(
    onBack: () -> Unit,
    viewModel: AccountsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var editing by remember { mutableStateOf<Account?>(null) }

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
                    title = "Accounts",
                    subtitle = "Balances are derived from your opening amount plus every recorded transaction.",
                    actionLabel = "Back",
                    onActionClick = onBack,
                )
            }
            item {
                AssistChip(onClick = { editing = Account(id = 0, name = "", type = AccountType.CASH, initialBalanceMinor = 0, currentBalanceMinor = 0, archived = false) }, label = { Text("Add account") })
            }
            if (state.accounts.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No accounts yet",
                        description = "Split balances by cash, card, bank, or savings to make reports more useful.",
                    )
                }
            } else {
                items(state.accounts, key = { it.id }) { account ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(account.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${account.type.name.lowercase().replaceFirstChar { it.uppercase() }} • ${MoneyFormatter.format(account.currentBalanceMinor, state.currencyCode)}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = { editing = account }, label = { Text("Edit") })
                            AssistChip(onClick = { viewModel.deleteAccount(account.id) }, label = { Text("Delete") })
                        }
                    }
                }
            }
        }
    }

    editing?.let { account ->
        AccountEditorDialog(
            account = account.takeIf { it.id != 0L },
            onDismiss = { editing = null },
            onSave = { name, type, balance, archived ->
                viewModel.saveAccount(account.takeIf { it.id != 0L }?.id, name, type, balance, archived)
                editing = null
            },
        )
    }
}

@Composable
private fun AccountEditorDialog(
    account: Account?,
    onDismiss: () -> Unit,
    onSave: (String, AccountType, String, Boolean) -> Unit,
) {
    var name by remember(account) { mutableStateOf(account?.name.orEmpty()) }
    var balance by remember(account) { mutableStateOf(((account?.initialBalanceMinor ?: 0L) / 100.0).toString()) }
    var archived by remember(account) { mutableStateOf(account?.archived ?: false) }
    var type by remember(account) { mutableStateOf(account?.type ?: AccountType.CASH) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = { onSave(name, type, balance, archived) }) { Text("Save") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(if (account == null) "New account" else "Edit account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountType.entries.forEach { option ->
                        FilterChip(
                            selected = type == option,
                            onClick = { type = option },
                            label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }
                OutlinedTextField(value = balance, onValueChange = { balance = it }, label = { Text("Opening balance") })
                Row {
                    Checkbox(checked = archived, onCheckedChange = { archived = it })
                    Text("Archive account")
                }
            }
        },
    )
}
