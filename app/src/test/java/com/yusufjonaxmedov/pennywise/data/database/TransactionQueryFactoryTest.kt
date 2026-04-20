package com.yusufjonaxmedov.pennywise.data.database

import androidx.sqlite.db.SimpleSQLiteQuery
import com.yusufjonaxmedov.pennywise.core.model.DateRangeSerializable
import com.yusufjonaxmedov.pennywise.core.model.SortOption
import com.yusufjonaxmedov.pennywise.core.model.TransactionFilter
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionQueryFactoryTest {
    @Test
    fun `create adds compound clauses for search date category account and type filters`() {
        val query = TransactionQueryFactory.create(
            filter = TransactionFilter(
                searchQuery = "coffee",
                categoryIds = setOf(1L, 2L),
                accountIds = setOf(9L),
                types = setOf(TransactionType.EXPENSE),
                dateRange = DateRangeSerializable("2026-04-01", "2026-04-20"),
                sortOption = SortOption.AMOUNT_HIGH_TO_LOW,
            ),
            limit = 20,
        ) as SimpleSQLiteQuery

        assertTrue(query.sql.contains("LOWER(t.note) LIKE ?"))
        assertTrue(query.sql.contains("t.categoryId IN (?, ?)"))
        assertTrue(query.sql.contains("t.accountId IN (?)"))
        assertTrue(query.sql.contains("t.type IN (?)"))
        assertTrue(query.sql.contains("t.transactionDate BETWEEN ? AND ?"))
        assertTrue(query.sql.contains("ORDER BY t.amountMinor DESC"))
        assertTrue(query.sql.contains("LIMIT ?"))
    }
}
