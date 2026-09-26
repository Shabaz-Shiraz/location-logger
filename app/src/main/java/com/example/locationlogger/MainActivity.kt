package com.example.locationlogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.locationlogger.domain.usecase.ResumeInterruptedTrackingUseCase
import com.example.locationlogger.ui.navigation.AppNavHost
import com.example.locationlogger.ui.theme.LocationLoggerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var resumeInterruptedTracking: ResumeInterruptedTrackingUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocationLoggerTheme {
                AppNavHost()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch { resumeInterruptedTracking() }
    }
}
