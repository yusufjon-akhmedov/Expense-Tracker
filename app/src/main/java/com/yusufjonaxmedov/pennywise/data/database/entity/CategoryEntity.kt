package com.yusufjonaxmedov.pennywise.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import java.time.Instant

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["name", "type"], unique = true),
    ],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String,
    val colorHex: String,
    val type: TransactionType,
    val isDefault: Boolean = false,
    val createdAt: Instant,
)
