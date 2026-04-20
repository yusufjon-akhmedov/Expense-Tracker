package com.yusufjonaxmedov.pennywise.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufjonaxmedov.pennywise.core.model.DateRange
import com.yusufjonaxmedov.pennywise.core.model.ReportSnapshot
import com.yusufjonaxmedov.pennywise.domain.repository.PreferencesRepository
import com.yusufjonaxmedov.pennywise.domain.repository.ReportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

enum class ReportRangePreset {
    MONTH,
    QUARTER,
    YEAR,
}

data class ReportsUiState(
    val currencyCode: String = "USD",
    val preset: ReportRangePreset = ReportRangePreset.QUARTER,
    val report: ReportSnapshot? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportsViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository,
    reportsRepository: ReportsRepository,
) : ViewModel() {
    private val preset = MutableStateFlow(ReportRangePreset.QUARTER)

    val uiState: StateFlow<ReportsUiState> = combine(
        preferencesRepository.observePreferences(),
        preset,
        preset.flatMapLatest { selectedPreset ->
            reportsRepository.observeReport(selectedPreset.toDateRange())
        },
    ) { preferences, selectedPreset, report ->
        ReportsUiState(
            currencyCode = preferences.currencyCode,
            preset = selectedPreset,
            report = report,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReportsUiState(),
    )

    fun updatePreset(value: ReportRangePreset) {
        preset.value = value
    }
}

private fun ReportRangePreset.toDateRange(): DateRange {
    val today = LocalDate.now()
    return when (this) {
        ReportRangePreset.MONTH -> DateRange(today.withDayOfMonth(1), today)
        ReportRangePreset.QUARTER -> DateRange(today.minusMonths(2).withDayOfMonth(1), today)
        ReportRangePreset.YEAR -> DateRange(today.minusMonths(11).withDayOfMonth(1), today)
    }
}
