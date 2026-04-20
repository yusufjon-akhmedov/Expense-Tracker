package com.yusufjonaxmedov.pennywise.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufjonaxmedov.pennywise.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val selectedCurrencyCode: String = "USD",
    val budgetsEnabled: Boolean = true,
    val saving: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun selectCurrency(currencyCode: String) {
        _uiState.value = _uiState.value.copy(selectedCurrencyCode = currencyCode)
    }

    fun setBudgetsEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(budgetsEnabled = enabled)
    }

    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saving = true)
            preferencesRepository.updateCurrency(_uiState.value.selectedCurrencyCode)
            preferencesRepository.updateBudgetsEnabled(_uiState.value.budgetsEnabled)
            preferencesRepository.setOnboardingCompleted(true)
            onComplete()
            _uiState.value = _uiState.value.copy(saving = false)
        }
    }
}
