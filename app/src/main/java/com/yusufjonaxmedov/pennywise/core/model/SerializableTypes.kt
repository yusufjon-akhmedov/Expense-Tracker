package com.yusufjonaxmedov.pennywise.core.model

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class DateRangeSerializable(
    val start: String,
    val endInclusive: String,
) {
    fun asDateRange(): DateRange = DateRange(
        start = LocalDate.parse(start),
        endInclusive = LocalDate.parse(endInclusive),
    )

    companion object {
        fun from(dateRange: DateRange): DateRangeSerializable = DateRangeSerializable(
            start = dateRange.start.toString(),
            endInclusive = dateRange.endInclusive.toString(),
        )
    }
}
