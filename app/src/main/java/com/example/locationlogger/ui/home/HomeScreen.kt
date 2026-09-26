package com.example.locationlogger.ui.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.locationlogger.ui.components.AddressUnavailablePill
import com.example.locationlogger.ui.components.PermissionCard
import com.example.locationlogger.ui.components.RadarPulse
import com.example.locationlogger.ui.format.formatCoordinates
import com.example.locationlogger.ui.format.formatTime
import com.example.locationlogger.ui.mvi.CollectEffects
import com.example.locationlogger.ui.theme.CoordinatesTextStyle
import com.example.locationlogger.ui.theme.LocationLoggerTheme
import kotlinx.coroutines.launch
import java.time.Instant
import kotlin.math.roundToInt

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
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeroStatusCard(state)
            StatsRow(state)
            PermissionCards(state, onIntent)
            state.latestEntry?.let { entry ->
                LatestLocationCard(entry, onClick = { onIntent(HomeIntent.OpenTimeline) })
            }
            TrackingButtons(state, onIntent)
        }
    }
}

@Composable
private fun HeroStatusCard(state: HomeState) {
    val colors = MaterialTheme.colorScheme
    val top by animateColorAsState(
        if (state.isTracking) colors.primaryContainer else colors.surfaceContainerHigh,
        label = "heroTop"
    )
    val bottom by animateColorAsState(
        if (state.isTracking) colors.surfaceContainerLow else colors.surfaceContainerHigh,
        label = "heroBottom"
    )

    Surface(shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(top, bottom)))
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadarPulse(
                active = state.isTracking,
                activeColor = colors.tertiary,
                idleColor = colors.outline
            )
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AnimatedContent(
                    targetState = state.isTracking,
                    transitionSpec = {
                        (fadeIn() + slideInVertically { it / 2 }) togetherWith
                            (fadeOut() + slideOutVertically { -it / 2 })
                    },
                    label = "heroStatus"
                ) { tracking ->
                    Text(
                        text = stringResource(if (tracking) R.string.home_status_active else R.string.home_status_paused),
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.onSurface
                    )
                }
                Text(
                    text = pluralStringResource(
                        R.plurals.home_capture_interval,
                        CAPTURE_INTERVAL.inWholeMinutes.toInt(),
                        CAPTURE_INTERVAL.inWholeMinutes.toInt()
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatsRow(state: HomeState) {
    val context = LocalContext.current
    val none = stringResource(R.string.stat_none)
    val next = state.nextCaptureAt
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatTile(
            label = stringResource(R.string.stat_entries),
            value = state.entryCount.toString(),
            modifier = Modifier.weight(1f)
        )
        StatTile(
            label = stringResource(R.string.stat_last_capture),
            value = state.latestEntry?.let { formatTime(context, it.timestamp) } ?: none,
            modifier = Modifier.weight(1f)
        )
        StatTile(
            label = stringResource(R.string.stat_next_capture),
            value = when {
                !state.isTracking || next == null -> none
                !state.isLocationEnabled -> stringResource(R.string.stat_location_off)
                // The estimate goes stale if the schedule was interrupted, e.g. by a reboot.
                next.isBefore(Instant.now()) -> stringResource(R.string.stat_soon)
                else -> formatTime(context, next)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LatestLocationCard(entry: LocationEntry, onClick: () -> Unit) {
    val context = LocalContext.current
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.home_latest_title, formatTime(context, entry.timestamp)),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            if (entry.address != null) {
                Text(text = entry.address, style = MaterialTheme.typography.titleMedium)
            } else {
                AddressUnavailablePill()
            }
            Text(
                text = stringResource(
                    R.string.timeline_coordinates_accuracy,
                    entry.point.formatCoordinates(),
                    entry.accuracyMeters.roundToInt()
                ),
                style = CoordinatesTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TrackingButtons(state: HomeState, onIntent: (HomeIntent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.isTracking) {
            FilledTonalButton(
                onClick = { onIntent(HomeIntent.PauseClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                ButtonContent(R.drawable.ic_pause, stringResource(R.string.home_pause))
            }
        } else {
            Button(
                onClick = { onIntent(HomeIntent.StartClicked) },
                enabled = state.canStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                ButtonContent(R.drawable.ic_play, stringResource(R.string.home_start))
            }
        }
        OutlinedButton(
            onClick = { onIntent(HomeIntent.OpenTimeline) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) { Text(stringResource(R.string.home_view_timeline)) }
    }
}

@Composable
private fun ButtonContent(icon: Int, label: String) {
    Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(20.dp))
    Spacer(Modifier.width(8.dp))
    Text(label, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun PermissionCards(state: HomeState, onIntent: (HomeIntent) -> Unit) {
    val permissions = state.permissions
    val colors = MaterialTheme.colorScheme

    if (!state.isLocationEnabled) {
        PermissionCard(
            title = stringResource(R.string.card_location_off_title),
            body = stringResource(R.string.card_location_off_body),
            actionLabel = stringResource(R.string.action_turn_on),
            onAction = { onIntent(HomeIntent.TurnOnLocationClicked) },
            icon = R.drawable.ic_notification,
            containerColor = colors.errorContainer,
            contentColor = colors.onErrorContainer,
            accentColor = colors.error
        )
    }

    when {
        permissions.approximateOnly -> PermissionCard(
            title = stringResource(R.string.card_precise_title),
            body = stringResource(R.string.card_precise_body),
            actionLabel = stringResource(
                if (state.locationPermissionBlocked) R.string.action_open_settings else R.string.card_precise_action
            ),
            onAction = { onIntent(HomeIntent.AllowPreciseClicked) },
            icon = R.drawable.ic_notification,
            containerColor = colors.tertiaryContainer,
            contentColor = colors.onTertiaryContainer,
            accentColor = colors.tertiary
        )
        !permissions.precise && state.locationPermissionBlocked -> PermissionCard(
            title = stringResource(R.string.card_location_denied_title),
            body = stringResource(R.string.card_location_denied_body),
            actionLabel = stringResource(R.string.action_open_settings),
            onAction = { onIntent(HomeIntent.OpenAppSettingsClicked) },
            icon = R.drawable.ic_notification,
            containerColor = colors.errorContainer,
            contentColor = colors.onErrorContainer,
            accentColor = colors.error
        )
    }

    if (permissions.precise && !permissions.background) {
        PermissionCard(
            title = stringResource(R.string.card_background_title),
            body = stringResource(R.string.card_background_body),
            actionLabel = stringResource(R.string.card_background_action),
            onAction = { onIntent(HomeIntent.AllowBackgroundClicked) },
            icon = R.drawable.ic_clock
        )
    }

    if (permissions.precise && !permissions.notifications) {
        PermissionCard(
            title = stringResource(R.string.card_notifications_title),
            body = stringResource(R.string.card_notifications_body),
            actionLabel = stringResource(R.string.action_turn_on),
            onAction = { onIntent(HomeIntent.NotificationSettingsClicked) },
            icon = R.drawable.ic_bell
        )
    }
}

private val previewEntry = LocationEntry(
    id = 1,
    timestamp = Instant.parse("2026-09-26T04:30:00Z"),
    point = GeoPoint(9.98160, 76.28000),
    accuracyMeters = 12f,
    address = "Marine Drive, Ernakulam, Kochi, Kerala 682031, India"
)

@Preview(name = "Tracking")
@Composable
private fun HomeContentTrackingPreview() {
    LocationLoggerTheme {
        HomeContent(
            state = HomeState(
                trackingStatus = TrackingStatus.Active,
                permissions = LocationPermissions(precise = true, approximate = true, notifications = true),
                entryCount = 42,
                latestEntry = previewEntry,
                nextCaptureAt = Instant.parse("2026-09-26T04:40:00Z")
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onIntent = {}
        )
    }
}

@Preview(name = "Paused, dark")
@Composable
private fun HomeContentPausedDarkPreview() {
    LocationLoggerTheme(darkTheme = true) {
        HomeContent(
            state = HomeState(
                permissions = LocationPermissions(approximate = true),
                isLocationEnabled = false,
                entryCount = 3,
                latestEntry = previewEntry.copy(address = null)
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onIntent = {}
        )
    }
}
