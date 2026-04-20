package com.yusufjonaxmedov.pennywise.feature.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufjonaxmedov.pennywise.core.model.AppPreferencesModel
import com.yusufjonaxmedov.pennywise.core.model.ThemeMode
import com.yusufjonaxmedov.pennywise.core.model.WeekStart
import com.yusufjonaxmedov.pennywise.data.export.BackupExporter
import com.yusufjonaxmedov.pennywise.domain.repository.PreferencesRepository
import com.yusufjonaxmedov.pennywise.work.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SettingsEvent {
    data class Message(val text: String) : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupExporter: BackupExporter,
    private val preferencesRepository: PreferencesRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {
    private val events = MutableSharedFlow<SettingsEvent>()
    val eventFlow = events.asSharedFlow()

    val uiState: StateFlow<AppPreferencesModel> = preferencesRepository.observePreferences().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppPreferencesModel(),
    )

    fun updateTheme(themeMode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.updateThemeMode(themeMode) }
    }

    fun updateCurrency(currencyCode: String) {
        viewModelScope.launch { preferencesRepository.updateCurrency(currencyCode) }
    }

    fun updateWeekStart(weekStart: WeekStart) {
        viewModelScope.launch { preferencesRepository.updateWeekStart(weekStart) }
    }

    fun updateBudgets(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updateBudgetsEnabled(enabled) }
    }

    fun updateHaptics(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updateHapticsEnabled(enabled) }
    }

    fun updateReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesRepository.updateReminder(enabled, hour, minute)
            if (enabled) {
                reminderScheduler.schedule(hour, minute)
                events.emit(SettingsEvent.Message("Daily reminder scheduled."))
            } else {
                reminderScheduler.cancel()
                events.emit(SettingsEvent.Message("Reminder disabled."))
            }
        }
    }

    fun exportCsv(uri: Uri) {
        viewModelScope.launch {
            runCatching { backupExporter.exportCsv(uri) }
                .onSuccess { events.emit(SettingsEvent.Message("CSV export complete.")) }
                .onFailure { events.emit(SettingsEvent.Message("CSV export failed.")) }
        }
    }

    fun exportJson(uri: Uri) {
        viewModelScope.launch {
            runCatching { backupExporter.exportJson(uri) }
                .onSuccess { events.emit(SettingsEvent.Message("JSON backup saved.")) }
                .onFailure { events.emit(SettingsEvent.Message("JSON backup failed.")) }
        }
    }
}
