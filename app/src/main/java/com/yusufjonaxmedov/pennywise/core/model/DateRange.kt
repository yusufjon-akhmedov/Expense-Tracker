package com.yusufjonaxmedov.pennywise.core.model

import java.time.LocalDate

data class DateRange(
    val start: LocalDate,
    val endInclusive: LocalDate,
) {
    init {
        require(!endInclusive.isBefore(start)) {
            "End date must be on or after start date."
        }
    }

    fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(endInclusive)
}
