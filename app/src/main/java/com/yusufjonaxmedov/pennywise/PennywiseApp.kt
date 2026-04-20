package com.yusufjonaxmedov.pennywise

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.yusufjonaxmedov.pennywise.core.designsystem.theme.PennywiseTheme
import com.yusufjonaxmedov.pennywise.core.model.ThemeMode
import com.yusufjonaxmedov.pennywise.navigation.PennywiseNavHost

@Composable
fun PennywiseApp(
    state: PennywiseAppState,
    onAppReady: () -> Unit,
) {
    val darkTheme = when (state.preferences.themeMode) {
        ThemeMode.SYSTEM -> null
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    PennywiseTheme(forceDarkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            if (!state.isReady) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                PennywiseNavHost(
                    navController = rememberNavController(),
                    onboardingCompleted = state.preferences.onboardingCompleted,
                    onAppReady = onAppReady,
                )
            }
        }
    }
}
