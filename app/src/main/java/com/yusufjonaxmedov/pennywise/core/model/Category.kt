package com.yusufjonaxmedov.pennywise.core.model

data class Category(
    val id: Long,
    val name: String,
    val emoji: String,
    val colorHex: String,
    val type: TransactionType,
    val isDefault: Boolean,
)
