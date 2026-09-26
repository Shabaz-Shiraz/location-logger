package com.example.locationlogger.ui.home

import com.example.locationlogger.domain.model.LocationEntry
import com.example.locationlogger.domain.model.TrackingStatus
import java.time.Instant

data class LocationPermissions(
    val precise: Boolean = false,
    val approximate: Boolean = false,
    val background: Boolean = false,
    val notifications: Boolean = false
) {
    val approximateOnly: Boolean get() = approximate && !precise
}

data class HomeState(
    val trackingStatus: TrackingStatus = TrackingStatus.Paused,
    val permissions: LocationPermissions = LocationPermissions(),
    // Set once the user has denied location in a way the system won't ask about again.
    val locationPermissionBlocked: Boolean = false,
    val isLocationEnabled: Boolean = true,
    val entryCount: Int = 0,
    val latestEntry: LocationEntry? = null,
    val nextCaptureAt: Instant? = null
) {
    val isTracking: Boolean get() = trackingStatus == TrackingStatus.Active
    // With approximate location only, entries could be kilometres off
    val canStart: Boolean get() = !permissions.approximateOnly
}

sealed interface HomeIntent {
    data object StartClicked : HomeIntent
    data object PauseClicked : HomeIntent
    data class PermissionsChanged(val permissions: LocationPermissions) : HomeIntent
    data class PermissionsRequestResult(
        val permissions: LocationPermissions,
        val blocked: Boolean
    ) : HomeIntent
    data class LocationSettingsResult(val enabled: Boolean) : HomeIntent
    data object AllowPreciseClicked : HomeIntent
    data object TurnOnLocationClicked : HomeIntent
    data object AllowBackgroundClicked : HomeIntent
    data object NotificationSettingsClicked : HomeIntent
    data object OpenAppSettingsClicked : HomeIntent
    data object OpenTimeline : HomeIntent
}

sealed interface HomeEffect {
    data object RequestLocationPermissions : HomeEffect
    data object RequestBackgroundPermission : HomeEffect
    data object ResolveLocationSettings : HomeEffect
    data object OpenAppSettings : HomeEffect
    data object OpenNotificationSettings : HomeEffect
    data object ShowLocationRequired : HomeEffect
    data object NavigateToTimeline : HomeEffect
}
