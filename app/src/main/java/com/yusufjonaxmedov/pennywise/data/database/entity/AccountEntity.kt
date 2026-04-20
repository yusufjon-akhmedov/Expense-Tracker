package com.yusufjonaxmedov.pennywise.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yusufjonaxmedov.pennywise.core.model.AccountType
import java.time.Instant

@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["name"], unique = true),
    ],
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val initialBalanceMinor: Long,
    val archived: Boolean = false,
    val createdAt: Instant,
)
