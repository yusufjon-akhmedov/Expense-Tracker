package com.yusufjonaxmedov.pennywise.data.database

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.yusufjonaxmedov.pennywise.core.model.SortOption
import com.yusufjonaxmedov.pennywise.core.model.TransactionFilter

object TransactionQueryFactory {
    fun create(
        filter: TransactionFilter,
        limit: Int? = null,
        transactionId: Long? = null,
    ): SupportSQLiteQuery {
        val sql = StringBuilder(
            """
            SELECT
                t.id AS id,
                t.amountMinor AS amountMinor,
                t.type AS type,
                t.note AS note,
                t.payee AS payee,
                t.tagsJson AS tagsJson,
                t.transactionDate AS transactionDate,
                t.origin AS origin,
                t.recurringTemplateId AS recurringTemplateId,
                t.createdAt AS createdAt,
                t.updatedAt AS updatedAt,
                c.id AS categoryId,
                c.name AS categoryName,
                c.emoji AS categoryEmoji,
                c.colorHex AS categoryColorHex,
                a.id AS accountId,
                a.name AS accountName,
                a.type AS accountType
            FROM transactions t
            INNER JOIN categories c ON c.id = t.categoryId
            INNER JOIN accounts a ON a.id = t.accountId
            WHERE 1 = 1
            """.trimIndent(),
        )

        val args = mutableListOf<Any>()

        transactionId?.let {
            sql.append(" AND t.id = ?")
            args += it
        }

        if (filter.searchQuery.isNotBlank()) {
            val value = "%${filter.searchQuery.trim().lowercase()}%"
            sql.append(" AND (LOWER(t.note) LIKE ? OR LOWER(t.payee) LIKE ? OR LOWER(c.name) LIKE ?)")
            repeat(3) { args += value }
        }

        if (filter.categoryIds.isNotEmpty()) {
            sql.append(" AND t.categoryId IN (${placeholders(filter.categoryIds.size)})")
            args.addAll(filter.categoryIds.toList())
        }

        if (filter.accountIds.isNotEmpty()) {
            sql.append(" AND t.accountId IN (${placeholders(filter.accountIds.size)})")
            args.addAll(filter.accountIds.toList())
        }

        if (filter.types.isNotEmpty()) {
            sql.append(" AND t.type IN (${placeholders(filter.types.size)})")
            args.addAll(filter.types.map { it.name })
        }

        filter.dateRange?.let { range ->
            sql.append(" AND t.transactionDate BETWEEN ? AND ?")
            args += range.start
            args += range.endInclusive
        }

        sql.append(
            when (filter.sortOption) {
                SortOption.NEWEST_FIRST -> " ORDER BY t.transactionDate DESC, t.id DESC"
                SortOption.OLDEST_FIRST -> " ORDER BY t.transactionDate ASC, t.id ASC"
                SortOption.AMOUNT_HIGH_TO_LOW -> " ORDER BY t.amountMinor DESC, t.transactionDate DESC"
                SortOption.AMOUNT_LOW_TO_HIGH -> " ORDER BY t.amountMinor ASC, t.transactionDate DESC"
            },
        )

        limit?.let {
            sql.append(" LIMIT ?")
            args += it
        }

        return SimpleSQLiteQuery(sql.toString(), args.toTypedArray())
    }

    private fun placeholders(size: Int): String = List(size) { "?" }.joinToString(", ")
}
