package com.yusufjonaxmedov.pennywise.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.yusufjonaxmedov.pennywise.core.model.AppPreferencesModel
import com.yusufjonaxmedov.pennywise.core.model.ThemeMode
import com.yusufjonaxmedov.pennywise.core.model.WeekStart
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val store = PreferenceDataStoreFactory.create(
        produceFile = { File(context.filesDir, DATASTORE_FILE_NAME) },
    )

    val preferences: Flow<AppPreferencesModel> = store.data.map { prefs ->
        AppPreferencesModel(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            currencyCode = prefs[Keys.CURRENCY_CODE] ?: "USD",
            budgetsEnabled = prefs[Keys.BUDGETS_ENABLED] ?: true,
            themeMode = prefs[Keys.THEME_MODE]?.let(ThemeMode::valueOf) ?: ThemeMode.SYSTEM,
            firstDayOfWeek = prefs[Keys.WEEK_START]?.let(WeekStart::valueOf) ?: WeekStart.MONDAY,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            reminderEnabled = prefs[Keys.REMINDER_ENABLED] ?: false,
            reminderHour = prefs[Keys.REMINDER_HOUR] ?: 20,
            reminderMinute = prefs[Keys.REMINDER_MINUTE] ?: 0,
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        store.edit { prefs -> prefs[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun updateCurrency(currencyCode: String) {
        store.edit { prefs -> prefs[Keys.CURRENCY_CODE] = currencyCode }
    }

    suspend fun updateBudgetsEnabled(enabled: Boolean) {
        store.edit { prefs -> prefs[Keys.BUDGETS_ENABLED] = enabled }
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        store.edit { prefs -> prefs[Keys.THEME_MODE] = themeMode.name }
    }

    suspend fun updateWeekStart(weekStart: WeekStart) {
        store.edit { prefs -> prefs[Keys.WEEK_START] = weekStart.name }
    }

    suspend fun updateHapticsEnabled(enabled: Boolean) {
        store.edit { prefs -> prefs[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun updateReminder(enabled: Boolean, hour: Int, minute: Int) {
        store.edit { prefs ->
            prefs[Keys.REMINDER_ENABLED] = enabled
            prefs[Keys.REMINDER_HOUR] = hour
            prefs[Keys.REMINDER_MINUTE] = minute
        }
    }

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val BUDGETS_ENABLED = booleanPreferencesKey("budgets_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val WEEK_START = stringPreferencesKey("week_start")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    }

    private companion object {
        const val DATASTORE_FILE_NAME = "user_preferences.preferences_pb"
    }
}
