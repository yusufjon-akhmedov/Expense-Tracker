package com.yusufjonaxmedov.pennywise.core.ui

import android.app.DatePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate

@Composable
fun rememberDatePickerLauncher(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    return remember(context, initialDate, onDateSelected) {
        {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
                },
                initialDate.year,
                initialDate.monthValue - 1,
                initialDate.dayOfMonth,
            ).show()
        }
    }
}
