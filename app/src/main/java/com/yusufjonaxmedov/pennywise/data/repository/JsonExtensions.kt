package com.yusufjonaxmedov.pennywise.data.repository

import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal fun Json.encodeLongSet(value: Set<Long>): String = encodeToString(value.toList().sorted())

internal fun Json.decodeLongSet(value: String): Set<Long> =
    runCatching { decodeFromString<List<Long>>(value).toSet() }.getOrDefault(emptySet())

internal fun Json.encodeStringList(value: List<String>): String = encodeToString(value)

internal fun Json.decodeStringList(value: String): List<String> =
    runCatching { decodeFromString<List<String>>(value) }.getOrDefault(emptyList())

internal fun Json.encodeTypeSet(value: Set<TransactionType>): String = encodeToString(value.toList())

internal fun Json.decodeTypeSet(value: String): Set<TransactionType> =
    runCatching { decodeFromString<List<TransactionType>>(value).toSet() }.getOrDefault(emptySet())
