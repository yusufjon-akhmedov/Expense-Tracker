package com.yusufjonaxmedov.pennywise.core.model

data class SavedFilter(
    val id: Long,
    val name: String,
    val filter: TransactionFilter,
    val pinned: Boolean,
)
