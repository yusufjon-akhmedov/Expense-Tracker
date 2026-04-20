package com.yusufjonaxmedov.pennywise.data.database.converter

import androidx.room.TypeConverter
import com.yusufjonaxmedov.pennywise.core.model.AccountType
import com.yusufjonaxmedov.pennywise.core.model.RecurringFrequency
import com.yusufjonaxmedov.pennywise.core.model.SortOption
import com.yusufjonaxmedov.pennywise.core.model.ThemeMode
import com.yusufjonaxmedov.pennywise.core.model.TransactionOrigin
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import com.yusufjonaxmedov.pennywise.core.model.WeekStart
import java.time.Instant
import java.time.LocalDate

class RoomConverters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? = value?.let(TransactionType::valueOf)

    @TypeConverter
    fun fromAccountType(value: AccountType?): String? = value?.name

    @TypeConverter
    fun toAccountType(value: String?): AccountType? = value?.let(AccountType::valueOf)

    @TypeConverter
    fun fromRecurringFrequency(value: RecurringFrequency?): String? = value?.name

    @TypeConverter
    fun toRecurringFrequency(value: String?): RecurringFrequency? = value?.let(RecurringFrequency::valueOf)

    @TypeConverter
    fun fromSortOption(value: SortOption?): String? = value?.name

    @TypeConverter
    fun toSortOption(value: String?): SortOption? = value?.let(SortOption::valueOf)

    @TypeConverter
    fun fromThemeMode(value: ThemeMode?): String? = value?.name

    @TypeConverter
    fun toThemeMode(value: String?): ThemeMode? = value?.let(ThemeMode::valueOf)

    @TypeConverter
    fun fromWeekStart(value: WeekStart?): String? = value?.name

    @TypeConverter
    fun toWeekStart(value: String?): WeekStart? = value?.let(WeekStart::valueOf)

    @TypeConverter
    fun fromTransactionOrigin(value: TransactionOrigin?): String? = value?.name

    @TypeConverter
    fun toTransactionOrigin(value: String?): TransactionOrigin? = value?.let(TransactionOrigin::valueOf)
}
