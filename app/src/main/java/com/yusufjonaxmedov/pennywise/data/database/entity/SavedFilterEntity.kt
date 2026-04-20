package com.yusufjonaxmedov.pennywise.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yusufjonaxmedov.pennywise.core.model.SortOption
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "saved_filters",
    indices = [
        Index(value = ["pinned"]),
    ],
)
data class SavedFilterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val searchQuery: String,
    val categoryIdsJson: String,
    val accountIdsJson: String,
    val typesJson: String,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val sortOption: SortOption,
    val pinned: Boolean = true,
    val createdAt: Instant,
)
