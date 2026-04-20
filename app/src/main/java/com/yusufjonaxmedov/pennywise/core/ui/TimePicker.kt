package com.yusufjonaxmedov.pennywise.core.ui

import android.app.TimePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberTimePickerLauncher(
    hour: Int,
    minute: Int,
    onSelected: (Int, Int) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    return remember(context, hour, minute, onSelected) {
        {
            TimePickerDialog(
                context,
                { _, selectedHour, selectedMinute -> onSelected(selectedHour, selectedMinute) },
                hour,
                minute,
                true,
            ).show()
        }
    }
}
