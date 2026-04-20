package com.yusufjonaxmedov.pennywise.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufjonaxmedov.pennywise.core.common.toMonthKey
import com.yusufjonaxmedov.pennywise.core.model.AppPreferencesModel
import com.yusufjonaxmedov.pennywise.core.model.DashboardSummary
import com.yusufjonaxmedov.pennywise.domain.repository.PreferencesRepository
import com.yusufjonaxmedov.pennywise.domain.repository.TransactionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val preferences: AppPreferencesModel = AppPreferencesModel(),
    val summary: DashboardSummary? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository,
    transactionsRepository: TransactionsRepository,
) : ViewModel() {
    private val monthKey = LocalDate.now().toMonthKey()

    val uiState: StateFlow<DashboardUiState> = combine(
        preferencesRepository.observePreferences(),
        transactionsRepository.observeDashboard(monthKey),
    ) { preferences, summary ->
        DashboardUiState(
            preferences = preferences,
            summary = summary,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(),
    )
}
