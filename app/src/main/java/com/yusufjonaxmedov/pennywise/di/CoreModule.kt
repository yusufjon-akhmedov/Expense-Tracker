package com.yusufjonaxmedov.pennywise.di

import com.yusufjonaxmedov.pennywise.core.common.ClockProvider
import com.yusufjonaxmedov.pennywise.core.common.SystemClockProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    @Provides
    @Singleton
    fun provideClockProvider(): ClockProvider = SystemClockProvider()

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
}
