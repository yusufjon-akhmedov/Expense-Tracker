package com.yusufjonaxmedov.pennywise.core.ui

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

fun LocalDate.readableLabel(): String = format(dateFormatter)

fun String.monthLabel(): String = YearMonth.parse(this).format(monthFormatter)
