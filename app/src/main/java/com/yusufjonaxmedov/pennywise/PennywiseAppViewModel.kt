package com.yusufjonaxmedov.pennywise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufjonaxmedov.pennywise.core.model.AppPreferencesModel
import com.yusufjonaxmedov.pennywise.data.repository.SeedDataInitializer
import com.yusufjonaxmedov.pennywise.domain.repository.PreferencesRepository
import com.yusufjonaxmedov.pennywise.domain.repository.RecurringRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

data class PennywiseAppState(
    val isReady: Boolean = false,
    val preferences: AppPreferencesModel = AppPreferencesModel(),
)

@HiltViewModel
class PennywiseAppViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val recurringRepository: RecurringRepository,
    private val seedDataInitializer: SeedDataInitializer,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PennywiseAppState())
    val uiState: StateFlow<PennywiseAppState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.observePreferences().collectLatest { preferences ->
                _uiState.value = _uiState.value.copy(
                    preferences = preferences,
                    isReady = _uiState.value.isReady,
                )
            }
        }
        viewModelScope.launch {
            runCatching {
                seedDataInitializer.seedIfNeeded()
                recurringRepository.applyDueTemplates()
            }.onFailure { throwable ->
                Timber.e(throwable, "App bootstrap failed.")
            }
            _uiState.value = _uiState.value.copy(isReady = true)
        }
    }

    fun markReady() = Unit
}
