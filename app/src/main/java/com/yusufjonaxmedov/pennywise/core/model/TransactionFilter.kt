package com.yusufjonaxmedov.pennywise.core.model

import kotlinx.serialization.Serializable

@Serializable
data class TransactionFilter(
    val searchQuery: String = "",
    val categoryIds: Set<Long> = emptySet(),
    val accountIds: Set<Long> = emptySet(),
    val types: Set<TransactionType> = emptySet(),
    val dateRange: DateRangeSerializable? = null,
    val sortOption: SortOption = SortOption.NEWEST_FIRST,
) {
    val isEmpty: Boolean
        get() = searchQuery.isBlank() &&
            categoryIds.isEmpty() &&
            accountIds.isEmpty() &&
            types.isEmpty() &&
            dateRange == null &&
            sortOption == SortOption.NEWEST_FIRST
}
