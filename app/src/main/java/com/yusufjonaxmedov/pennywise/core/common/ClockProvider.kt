package com.yusufjonaxmedov.pennywise.core.common

import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

interface ClockProvider {
    fun currentDate(): LocalDate
    fun currentInstant(): Instant
}

@Singleton
class SystemClockProvider @Inject constructor() : ClockProvider {
    override fun currentDate(): LocalDate = LocalDate.now()

    override fun currentInstant(): Instant = Instant.now()
}
