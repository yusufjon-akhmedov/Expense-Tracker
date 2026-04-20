package com.yusufjonaxmedov.pennywise.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class RecurringFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
}
