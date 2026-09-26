package com.example.locationlogger.ui.home

import androidx.lifecycle.viewModelScope
import com.example.locationlogger.domain.usecase.ObserveLocationEnabledUseCase
import com.example.locationlogger.domain.usecase.ObserveTrackingSummaryUseCase
import com.example.locationlogger.domain.usecase.PauseTrackingUseCase
import com.example.locationlogger.domain.usecase.StartTrackingUseCase
import com.example.locationlogger.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeTrackingSummary: ObserveTrackingSummaryUseCase,
    observeLocationEnabled: ObserveLocationEnabledUseCase,
    private val startTracking: StartTrackingUseCase,
    private val pauseTracking: PauseTrackingUseCase
) : MviViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    private var startAfterPermissions = false
    private var startPending = false

    init {
        viewModelScope.launch {
            observeTrackingSummary().collect { summary ->
                setState {
                    copy(
                        trackingStatus = summary.status,
                        entryCount = summary.entryCount,
                        latestEntry = summary.latestEntry,
                        nextCaptureAt = summary.nextCaptureAt
                    )
                }
            }
        }
        viewModelScope.launch {
            observeLocationEnabled().collect { enabled -> setState { copy(isLocationEnabled = enabled) } }
        }
    }

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.StartClicked -> onStartClicked()
            HomeIntent.PauseClicked -> viewModelScope.launch { pauseTracking() }
            is HomeIntent.PermissionsChanged -> setState {
                copy(
                    permissions = intent.permissions,
                    locationPermissionBlocked = locationPermissionBlocked && !intent.permissions.precise
                )
            }
            is HomeIntent.PermissionsRequestResult -> {
                setState { copy(permissions = intent.permissions, locationPermissionBlocked = intent.blocked) }
                if (startAfterPermissions && intent.permissions.precise) checkLocationSettingsThenStart()
                startAfterPermissions = false
            }
            is HomeIntent.LocationSettingsResult -> onLocationSettingsResult(intent.enabled)
            HomeIntent.AllowPreciseClicked -> sendEffect(
                if (state.value.locationPermissionBlocked) HomeEffect.OpenAppSettings
                else HomeEffect.RequestLocationPermissions
            )
            HomeIntent.TurnOnLocationClicked -> sendEffect(HomeEffect.ResolveLocationSettings)
            HomeIntent.AllowBackgroundClicked -> sendEffect(HomeEffect.RequestBackgroundPermission)
            HomeIntent.NotificationSettingsClicked -> sendEffect(HomeEffect.OpenNotificationSettings)
            HomeIntent.OpenAppSettingsClicked -> sendEffect(HomeEffect.OpenAppSettings)
            HomeIntent.OpenTimeline -> sendEffect(HomeEffect.NavigateToTimeline)
        }
    }

    private fun onStartClicked() {
        val current = state.value
        when {
            current.permissions.precise -> checkLocationSettingsThenStart()
            current.locationPermissionBlocked -> sendEffect(HomeEffect.OpenAppSettings)
            else -> {
                startAfterPermissions = true
                sendEffect(HomeEffect.RequestLocationPermissions)
            }
        }
    }

    private fun checkLocationSettingsThenStart() {
        startPending = true
        sendEffect(HomeEffect.ResolveLocationSettings)
    }

    private fun onLocationSettingsResult(enabled: Boolean) {
        if (!startPending) return
        startPending = false
        if (enabled) {
            viewModelScope.launch { startTracking() }
        } else {
            sendEffect(HomeEffect.ShowLocationRequired)
        }
    }
}
