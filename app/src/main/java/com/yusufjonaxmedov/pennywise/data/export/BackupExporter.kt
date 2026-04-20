package com.yusufjonaxmedov.pennywise.data.export

import android.content.ContentResolver
import android.net.Uri
import com.yusufjonaxmedov.pennywise.core.model.Transaction
import com.yusufjonaxmedov.pennywise.domain.repository.TransactionsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.OutputStreamWriter
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class BackupExporter @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val json: Json,
    private val transactionsRepository: TransactionsRepository,
) {
    suspend fun exportCsv(uri: Uri) {
        val transactions = transactionsRepository.exportTransactions()
        writeToUri(uri) { writer ->
            writer.appendLine("id,date,type,amountMinor,currency,category,account,payee,note,tags")
            transactions.forEach { transaction ->
                writer.appendLine(
                    listOf(
                        transaction.id,
                        transaction.transactionDate,
                        transaction.type,
                        transaction.amountMinor,
                        "",
                        escapeCsv(transaction.category.name),
                        escapeCsv(transaction.accountName),
                        escapeCsv(transaction.payee),
                        escapeCsv(transaction.note),
                        escapeCsv(transaction.tags.joinToString("|")),
                    ).joinToString(","),
                )
            }
        }
    }

    suspend fun exportJson(uri: Uri) {
        val transactions = transactionsRepository.exportTransactions()
        val payload = BackupPayload(
            exportedAt = Instant.now().toString(),
            transactions = transactions.map {
                BackupTransaction(
                    id = it.id,
                    date = it.transactionDate.toString(),
                    type = it.type.name,
                    amountMinor = it.amountMinor,
                    category = it.category.name,
                    account = it.accountName,
                    payee = it.payee,
                    note = it.note,
                    tags = it.tags,
                )
            },
        )
        writeToUri(uri) { writer ->
            writer.write(json.encodeToString(payload))
        }
    }

    private fun writeToUri(uri: Uri, block: (OutputStreamWriter) -> Unit) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use(block)
        } ?: error("Unable to open export destination.")
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n")
        return if (!needsQuotes) value else "\"${value.replace("\"", "\"\"")}\""
    }
}

@Serializable
private data class BackupPayload(
    val exportedAt: String,
    val transactions: List<BackupTransaction>,
)

@Serializable
private data class BackupTransaction(
    val id: Long,
    val date: String,
    val type: String,
    val amountMinor: Long,
    val category: String,
    val account: String,
    val payee: String,
    val note: String,
    val tags: List<String>,
)
