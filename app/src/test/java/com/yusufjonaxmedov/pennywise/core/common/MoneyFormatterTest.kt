package com.yusufjonaxmedov.pennywise.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyFormatterTest {
    @Test
    fun `parse minor amount accepts decimals safely`() {
        assertEquals(12_345L, MoneyParser.parseMinorAmount("123.45"))
        assertEquals(12_340L, MoneyParser.parseMinorAmount("123.4"))
    }

    @Test
    fun `parse minor amount rejects blank input`() {
        assertNull(MoneyParser.parseMinorAmount(" "))
    }

    @Test
    fun `minor to major keeps two-decimal precision`() {
        assertEquals("12.34", MoneyFormatter.minorToMajor(1_234L).toPlainString())
    }
}
