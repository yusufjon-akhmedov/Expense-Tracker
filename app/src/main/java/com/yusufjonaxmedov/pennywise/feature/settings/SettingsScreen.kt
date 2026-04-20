package com.yusufjonaxmedov.pennywise.feature.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yusufjonaxmedov.pennywise.core.model.ThemeMode
import com.yusufjonaxmedov.pennywise.core.model.WeekStart
import com.yusufjonaxmedov.pennywise.core.ui.SectionHeader
import com.yusufjonaxmedov.pennywise.core.ui.rememberTimePickerLauncher
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(
    onManageCategories: () -> Unit,
    onManageAccounts: () -> Unit,
    onManageRecurring: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val csvLauncher = rememberLauncherForActivityResult(CreateDocument("text/csv")) { uri ->
        uri?.let(viewModel::exportCsv)
    }
    val jsonLauncher = rememberLauncherForActivityResult(CreateDocument("application/json")) { uri ->
        uri?.let(viewModel::exportJson)
    }
    val permissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if (granted) {
            viewModel.updateReminder(true, state.reminderHour, state.reminderMinute)
        }
    }
    val openTimePicker = rememberTimePickerLauncher(
        hour = state.reminderHour,
        minute = state.reminderMinute,
    ) { hour, minute ->
        viewModel.updateReminder(state.reminderEnabled, hour, minute)
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is SettingsEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SectionHeader(
                    title = "Settings",
                    subtitle = "Everything here stays local on device unless you export it yourself.",
                )
            }
            item {
                Text("Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { themeMode ->
                        FilterChip(
                            selected = state.themeMode == themeMode,
                            onClick = { viewModel.updateTheme(themeMode) },
                            label = { Text(themeMode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }
            }
            item {
                Text("Currency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("USD", "EUR", "GBP", "UZS").forEach { code ->
                        FilterChip(
                            selected = state.currencyCode == code,
                            onClick = { viewModel.updateCurrency(code) },
                            label = { Text(code) },
                        )
                    }
                }
            }
            item {
                Text("First day of week", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WeekStart.entries.forEach { weekStart ->
                        FilterChip(
                            selected = state.firstDayOfWeek == weekStart,
                            onClick = { viewModel.updateWeekStart(weekStart) },
                            label = { Text(weekStart.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Budgets enabled")
                    Switch(checked = state.budgetsEnabled, onCheckedChange = viewModel::updateBudgets)
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Haptics")
                    Switch(checked = state.hapticsEnabled, onCheckedChange = viewModel::updateHaptics)
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Expense reminder")
                    Switch(
                        checked = state.reminderEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.updateReminder(enabled, state.reminderHour, state.reminderMinute)
                            }
                        },
                    )
                }
            }
            item {
                AssistChip(
                    onClick = openTimePicker,
                    label = { Text("Reminder time: %02d:%02d".format(state.reminderHour, state.reminderMinute)) },
                )
            }
            item {
                SectionHeader(title = "Management")
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = onManageCategories, label = { Text("Categories") })
                    AssistChip(onClick = onManageAccounts, label = { Text("Accounts") })
                    AssistChip(onClick = onManageRecurring, label = { Text("Recurring") })
                }
            }
            item {
                SectionHeader(title = "Export")
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { csvLauncher.launch("pennywise-transactions.csv") }, label = { Text("Export CSV") })
                    AssistChip(onClick = { jsonLauncher.launch("pennywise-backup.json") }, label = { Text("Export JSON") })
                }
            }
            item {
                Text(
                    text = "App version 1.0.0\nData stays local to this device unless you explicitly export it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
