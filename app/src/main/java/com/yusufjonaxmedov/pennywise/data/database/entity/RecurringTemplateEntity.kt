package com.yusufjonaxmedov.pennywise.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yusufjonaxmedov.pennywise.core.model.RecurringFrequency
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "recurring_templates",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
            deferred = true,
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT,
            deferred = true,
        ),
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["accountId"]),
        Index(value = ["nextOccurrenceDate"]),
    ],
)
data class RecurringTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amountMinor: Long,
    val type: TransactionType,
    val categoryId: Long,
    val accountId: Long,
    val note: String,
    val payee: String,
    val tagsJson: String,
    val frequency: RecurringFrequency,
    val intervalValue: Int = 1,
    val dayOfMonth: Int? = null,
    val dayOfWeekIso: Int? = null,
    val nextOccurrenceDate: LocalDate,
    val active: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant,
)
