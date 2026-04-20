package com.yusufjonaxmedov.pennywise.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType {
    EXPENSE,
    INCOME,
}
