package com.yusufjonaxmedov.pennywise.domain.repository

import com.yusufjonaxmedov.pennywise.core.model.AppPreferencesModel
import com.yusufjonaxmedov.pennywise.core.model.ThemeMode
import com.yusufjonaxmedov.pennywise.core.model.WeekStart
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun observePreferences(): Flow<AppPreferencesModel>
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun updateCurrency(currencyCode: String)
    suspend fun updateBudgetsEnabled(enabled: Boolean)
    suspend fun updateThemeMode(themeMode: ThemeMode)
    suspend fun updateWeekStart(weekStart: WeekStart)
    suspend fun updateHapticsEnabled(enabled: Boolean)
    suspend fun updateReminder(enabled: Boolean, hour: Int, minute: Int)
}
