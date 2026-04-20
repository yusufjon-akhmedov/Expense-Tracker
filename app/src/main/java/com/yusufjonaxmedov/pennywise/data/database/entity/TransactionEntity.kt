package com.yusufjonaxmedov.pennywise.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yusufjonaxmedov.pennywise.core.model.TransactionOrigin
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "transactions",
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
        ForeignKey(
            entity = RecurringTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["recurringTemplateId"],
            onDelete = ForeignKey.SET_NULL,
            deferred = true,
        ),
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["accountId"]),
        Index(value = ["type"]),
        Index(value = ["transactionDate"]),
        Index(value = ["recurringTemplateId"]),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMinor: Long,
    val type: TransactionType,
    val categoryId: Long,
    val accountId: Long,
    val note: String,
    val payee: String,
    val tagsJson: String,
    val transactionDate: LocalDate,
    val origin: TransactionOrigin = TransactionOrigin.MANUAL,
    val recurringTemplateId: Long? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)
