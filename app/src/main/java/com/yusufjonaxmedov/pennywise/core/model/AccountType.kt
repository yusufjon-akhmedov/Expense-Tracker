package com.yusufjonaxmedov.pennywise.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class AccountType {
    CASH,
    CARD,
    BANK,
    SAVINGS,
}
