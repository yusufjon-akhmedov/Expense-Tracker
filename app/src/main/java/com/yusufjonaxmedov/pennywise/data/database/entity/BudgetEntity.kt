package com.yusufjonaxmedov.pennywise.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["monthKey", "categoryId"], unique = true),
        Index(value = ["categoryId"]),
    ],
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monthKey: String,
    val categoryId: Long,
    val limitMinor: Long,
    val rolloverEnabled: Boolean = false,
    val warnThresholdPercent: Int = 80,
)
