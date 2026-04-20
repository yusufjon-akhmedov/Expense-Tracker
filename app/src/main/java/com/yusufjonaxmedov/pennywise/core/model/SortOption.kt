package com.yusufjonaxmedov.pennywise.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class SortOption {
    NEWEST_FIRST,
    OLDEST_FIRST,
    AMOUNT_HIGH_TO_LOW,
    AMOUNT_LOW_TO_HIGH,
}
