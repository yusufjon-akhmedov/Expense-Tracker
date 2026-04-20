package com.yusufjonaxmedov.pennywise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.viewModels
import androidx.core.view.WindowCompat.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint(ComponentActivity::class)
class MainActivity : Hilt_MainActivity() {
    private val viewModel: PennywiseAppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { !viewModel.uiState.value.isReady }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            PennywiseApp(
                state = state,
                onAppReady = viewModel::markReady,
            )
        }
    }
}
