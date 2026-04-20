package com.yusufjonaxmedov.pennywise.feature.onboarding

import com.yusufjonaxmedov.pennywise.core.model.AppPreferencesModel
import com.yusufjonaxmedov.pennywise.core.model.ThemeMode
import com.yusufjonaxmedov.pennywise.core.model.WeekStart
import com.yusufjonaxmedov.pennywise.core.testing.MainDispatcherRule
import com.yusufjonaxmedov.pennywise.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `complete onboarding persists selected settings`() {
        val repository = FakePreferencesRepository()
        val viewModel = OnboardingViewModel(repository)

        viewModel.selectCurrency("EUR")
        viewModel.setBudgetsEnabled(false)
        viewModel.completeOnboarding {}

        assertEquals("EUR", repository.state.value.currencyCode)
        assertEquals(false, repository.state.value.budgetsEnabled)
        assertEquals(true, repository.state.value.onboardingCompleted)
    }
}

private class FakePreferencesRepository : PreferencesRepository {
    val state = MutableStateFlow(AppPreferencesModel())

    override fun observePreferences(): Flow<AppPreferencesModel> = state

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        state.update { it.copy(onboardingCompleted = completed) }
    }

    override suspend fun updateCurrency(currencyCode: String) {
        state.update { it.copy(currencyCode = currencyCode) }
    }

    override suspend fun updateBudgetsEnabled(enabled: Boolean) {
        state.update { it.copy(budgetsEnabled = enabled) }
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        state.update { it.copy(themeMode = themeMode) }
    }

    override suspend fun updateWeekStart(weekStart: WeekStart) {
        state.update { it.copy(firstDayOfWeek = weekStart) }
    }

    override suspend fun updateHapticsEnabled(enabled: Boolean) {
        state.update { it.copy(hapticsEnabled = enabled) }
    }

    override suspend fun updateReminder(enabled: Boolean, hour: Int, minute: Int) {
        state.update { it.copy(reminderEnabled = enabled, reminderHour = hour, reminderMinute = minute) }
    }
}
