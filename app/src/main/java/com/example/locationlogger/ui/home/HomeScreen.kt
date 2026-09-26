package com.example.locationlogger.ui.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.locationlogger.R
import com.example.locationlogger.domain.CAPTURE_INTERVAL
import com.example.locationlogger.domain.model.GeoPoint
import com.example.locationlogger.domain.model.LocationEntry
import com.example.locationlogger.domain.model.TrackingStatus
import com.example.locationlogger.ui.components.PermissionCard
import com.example.locationlogger.ui.format.formatCoordinates
import com.example.locationlogger.ui.format.formatTime
import com.example.locationlogger.ui.mvi.CollectEffects
import com.example.locationlogger.ui.theme.CoordinatesTextStyle
import com.example.locationlogger.ui.theme.LocationLoggerTheme
import kotlinx.coroutines.launch
import java.time.Instant

@Composable
fun HomeScreen(
    onNavigateToTimeline: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val locationRequiredMessage = stringResource(R.string.home_location_required)

    // Permissions can change in system settings while we're away, so re-read on every return.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onIntent(HomeIntent.PermissionsChanged(context.readLocationPermissions()))
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        val permissions = context.readLocationPermissions()
        // No rationale after a denial means the system won't show the dialog again.
        val blocked = !permissions.precise && activity?.shouldShowRequestPermissionRationale(
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == false
        viewModel.onIntent(HomeIntent.PermissionsRequestResult(permissions, blocked))
    }
    // On 11+ this opens the app's location page in Settings.
    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        viewModel.onIntent(HomeIntent.PermissionsChanged(context.readLocationPermissions()))
    }
    val checkLocationSettings = rememberLocationSettingsChecker { enabled ->
        viewModel.onIntent(HomeIntent.LocationSettingsResult(enabled))
    }

    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            HomeEffect.RequestLocationPermissions ->
                locationPermissionLauncher.launch(locationPermissionsToRequest())
            HomeEffect.RequestBackgroundPermission -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            HomeEffect.ResolveLocationSettings -> checkLocationSettings()
            HomeEffect.OpenAppSettings -> context.openAppSettings()
            HomeEffect.OpenNotificationSettings -> context.openNotificationSettings()
            HomeEffect.ShowLocationRequired -> scope.launch {
                snackbarHostState.showSnackbar(locationRequiredMessage)
            }
            HomeEffect.NavigateToTimeline -> onNavigateToTimeline()
        }
    }

    HomeContent(state = state, snackbarHostState = snackbarHostState, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeState,
    snackbarHostState: SnackbarHostState,
    onIntent: (HomeIntent) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusCard(state)
            PermissionCards(state, onIntent)

            if (state.isTracking) {
                FilledTonalButton(
                    onClick = { onIntent(HomeIntent.PauseClicked) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.home_pause)) }
            } else {
                Button(
                    onClick = { onIntent(HomeIntent.StartClicked) },
                    enabled = state.canStart,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.home_start)) }
            }
            OutlinedButton(
                onClick = { onIntent(HomeIntent.OpenTimeline) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.home_view_timeline)) }
        }
    }
}

@Composable
private fun StatusCard(state: HomeState) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (state.isTracking) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(
                    if (state.isTracking) R.string.home_status_active else R.string.home_status_paused
                ),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = pluralStringResource(
                    R.plurals.home_capture_interval,
                    CAPTURE_INTERVAL.inWholeMinutes.toInt(),
                    CAPTURE_INTERVAL.inWholeMinutes.toInt()
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = pluralStringResource(R.plurals.home_entry_count, state.entryCount, state.entryCount),
                style = MaterialTheme.typography.bodyMedium
            )

            val latest = state.latestEntry
            if (latest != null) {
                Text(
                    text = stringResource(R.string.home_last_capture, formatTime(context, latest.timestamp)),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = latest.address ?: stringResource(R.string.address_unavailable),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(text = latest.point.formatCoordinates(), style = CoordinatesTextStyle)
            }

            val next = state.nextCaptureAt
            if (state.isTracking && state.isLocationEnabled && next != null) {
                Text(
                    text = stringResource(R.string.home_next_capture, formatTime(context, next)),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PermissionCards(state: HomeState, onIntent: (HomeIntent) -> Unit) {
    val permissions = state.permissions

    if (!state.isLocationEnabled) {
        PermissionCard(
            title = stringResource(R.string.card_location_off_title),
            body = stringResource(R.string.card_location_off_body),
            actionLabel = stringResource(R.string.action_turn_on),
            onAction = { onIntent(HomeIntent.TurnOnLocationClicked) },
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    }

    when {
        permissions.approximateOnly -> PermissionCard(
            title = stringResource(R.string.card_precise_title),
            body = stringResource(R.string.card_precise_body),
            actionLabel = stringResource(
                if (state.locationPermissionBlocked) R.string.action_open_settings else R.string.card_precise_action
            ),
            onAction = { onIntent(HomeIntent.AllowPreciseClicked) }
        )
        !permissions.precise && state.locationPermissionBlocked -> PermissionCard(
            title = stringResource(R.string.card_location_denied_title),
            body = stringResource(R.string.card_location_denied_body),
            actionLabel = stringResource(R.string.action_open_settings),
            onAction = { onIntent(HomeIntent.OpenAppSettingsClicked) }
        )
    }

    if (permissions.precise && !permissions.background) {
        PermissionCard(
            title = stringResource(R.string.card_background_title),
            body = stringResource(R.string.card_background_body),
            actionLabel = stringResource(R.string.card_background_action),
            onAction = { onIntent(HomeIntent.AllowBackgroundClicked) },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    if (permissions.precise && !permissions.notifications) {
        PermissionCard(
            title = stringResource(R.string.card_notifications_title),
            body = stringResource(R.string.card_notifications_body),
            actionLabel = stringResource(R.string.action_turn_on),
            onAction = { onIntent(HomeIntent.NotificationSettingsClicked) },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
private fun HomeContentTrackingPreview() {
    LocationLoggerTheme {
        HomeContent(
            state = HomeState(
                trackingStatus = TrackingStatus.Active,
                permissions = LocationPermissions(precise = true, approximate = true, notifications = true),
                entryCount = 42,
                latestEntry = LocationEntry(
                    id = 1,
                    timestamp = Instant.parse("2026-09-26T09:30:00Z"),
                    point = GeoPoint(12.9716, 77.5946),
                    accuracyMeters = 8f,
                    address = "MG Road, Bengaluru, Karnataka 560001, India"
                ),
                nextCaptureAt = Instant.parse("2026-09-26T09:40:00Z")
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onIntent = {}
        )
    }
}

@Preview
@Composable
private fun HomeContentApproximatePreview() {
    LocationLoggerTheme {
        HomeContent(
            state = HomeState(
                permissions = LocationPermissions(approximate = true),
                isLocationEnabled = false
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onIntent = {}
        )
    }
}
