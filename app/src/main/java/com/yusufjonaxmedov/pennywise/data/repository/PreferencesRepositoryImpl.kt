package com.yusufjonaxmedov.pennywise.data.repository

import com.yusufjonaxmedov.pennywise.core.model.AppPreferencesModel
import com.yusufjonaxmedov.pennywise.core.model.ThemeMode
import com.yusufjonaxmedov.pennywise.core.model.WeekStart
import com.yusufjonaxmedov.pennywise.data.datastore.PreferencesDataStore
import com.yusufjonaxmedov.pennywise.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: PreferencesDataStore,
) : PreferencesRepository {
    override fun observePreferences(): Flow<AppPreferencesModel> = dataStore.preferences

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.setOnboardingCompleted(completed)
    }

    override suspend fun updateCurrency(currencyCode: String) {
        dataStore.updateCurrency(currencyCode)
    }

    override suspend fun updateBudgetsEnabled(enabled: Boolean) {
        dataStore.updateBudgetsEnabled(enabled)
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.updateThemeMode(themeMode)
    }

    override suspend fun updateWeekStart(weekStart: WeekStart) {
        dataStore.updateWeekStart(weekStart)
    }

    override suspend fun updateHapticsEnabled(enabled: Boolean) {
        dataStore.updateHapticsEnabled(enabled)
    }

    override suspend fun updateReminder(enabled: Boolean, hour: Int, minute: Int) {
        dataStore.updateReminder(enabled, hour, minute)
    }
}
