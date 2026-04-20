package com.yusufjonaxmedov.pennywise.core.common

import com.yusufjonaxmedov.pennywise.core.model.DateRange
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

fun LocalDate.toMonthKey(): String = YearMonth.from(this).format(monthFormatter)

fun String.toYearMonth(): YearMonth = YearMonth.parse(this, monthFormatter)

fun YearMonth.asDateRange(): DateRange = DateRange(
    start = atDay(1),
    endInclusive = atEndOfMonth(),
)
