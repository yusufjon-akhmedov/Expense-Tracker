package com.yusufjonaxmedov.pennywise.core.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object MoneyFormatter {
    fun format(
        minorAmount: Long,
        currencyCode: String,
        locale: Locale = Locale.getDefault(),
    ): String {
        val formatter = NumberFormat.getCurrencyInstance(locale)
        formatter.currency = Currency.getInstance(currencyCode)
        return formatter.format(minorToMajor(minorAmount))
    }

    fun minorToMajor(minorAmount: Long): BigDecimal =
        BigDecimal.valueOf(minorAmount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
}

object MoneyParser {
    fun parseMinorAmount(rawValue: String): Long? {
        val sanitized = rawValue
            .trim()
            .replace(",", ".")
            .replace(Regex("[^0-9.-]"), "")

        if (sanitized.isBlank()) return null

        return runCatching {
            BigDecimal(sanitized)
                .setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .longValueExact()
        }.getOrNull()
    }
}
