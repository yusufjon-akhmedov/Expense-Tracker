package com.yusufjonaxmedov.pennywise.core.model

data class AppPreferencesModel(
    val onboardingCompleted: Boolean = false,
    val currencyCode: String = "USD",
    val budgetsEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val firstDayOfWeek: WeekStart = WeekStart.MONDAY,
    val hapticsEnabled: Boolean = true,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
)
