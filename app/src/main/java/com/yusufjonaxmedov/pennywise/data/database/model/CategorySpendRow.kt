package com.yusufjonaxmedov.pennywise.data.database.model

data class CategorySpendRow(
    val categoryId: Long,
    val categoryName: String,
    val categoryEmoji: String,
    val categoryColorHex: String,
    val spentMinor: Long,
)
