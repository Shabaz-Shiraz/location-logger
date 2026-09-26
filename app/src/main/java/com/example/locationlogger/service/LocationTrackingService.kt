package com.example.locationlogger.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.PowerManager
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.locationlogger.domain.CAPTURE_INTERVAL
import com.example.locationlogger.domain.model.LocationFix
import com.example.locationlogger.domain.model.TrackingStatus
import com.example.locationlogger.domain.repository.LocationSource
import com.example.locationlogger.domain.usecase.CaptureLocationUseCase
import com.example.locationlogger.domain.usecase.ObserveTrackingStatusUseCase
import com.example.locationlogger.domain.usecase.PauseTrackingUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : LifecycleService() {

    @Inject lateinit var locationSource: LocationSource
    @Inject lateinit var captureLocation: CaptureLocationUseCase
    @Inject lateinit var observeTrackingStatus: ObserveTrackingStatusUseCase
    @Inject lateinit var pauseTracking: PauseTrackingUseCase
    @Inject lateinit var notificationFactory: TrackingNotificationFactory

    private val wakeLock: PowerManager.WakeLock by lazy {
        getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
            .apply { setReferenceCounted(false) }
    }

    private var trackingJob: Job? = null
    private var notificationState = TrackingNotificationState()

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        notificationFactory.createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_PAUSE -> lifecycleScope.launch { pauseTracking() }
            // A null intent means the system restarted us after killing the process (START_STICKY).
            else -> lifecycleScope.launch { resumeAfterRestart() }
        }
        return START_STICKY
    }

    // Swiping the app away from recents shouldn't stop logging; only Pause does.
    override fun onTaskRemoved(rootIntent: Intent?) = Unit

    override fun onDestroy() {
        isRunning = false
        if (wakeLock.isHeld) wakeLock.release()
        super.onDestroy()
    }

    private fun startTracking() {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) || !startInForeground()) {
            stopSelf()
            return
        }
        if (trackingJob?.isActive == true) return

        trackingJob = lifecycleScope.launch {
            launch {
                locationSource.observeLocationEnabled().collect { enabled ->
                    notificationState = notificationState.copy(locationEnabled = enabled)
                    notificationFactory.post(notificationState)
                }
            }
            locationSource.updates(CAPTURE_INTERVAL)
                .catch { e -> if (e is SecurityException) pauseTracking() else throw e }
                .collect(::capture)
        }
    }

    private suspend fun resumeAfterRestart() {
        val stillWanted = observeTrackingStatus().first() == TrackingStatus.Active
        // Without background access a restarted service can't receive location while the app
        // isn't visible. The flag stays on, so tracking resumes the next time the app is opened.
        val canRunInBackground = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        if (stillWanted && canRunInBackground) startTracking() else stopSelf()
    }

    private suspend fun capture(fix: LocationFix) {
        // The fix wakes the CPU for the callback only; geocoding and the insert need longer.
        wakeLock.acquire(WAKE_LOCK_TIMEOUT_MS)
        try {
            val entry = captureLocation(fix)
            notificationState = notificationState.copy(lastEntry = entry)
            notificationFactory.post(notificationState)
        } finally {
            if (wakeLock.isHeld) wakeLock.release()
        }
    }

    private fun startInForeground(): Boolean = try {
        ServiceCompat.startForeground(
            this,
            TrackingNotificationFactory.NOTIFICATION_ID,
            notificationFactory.build(notificationState),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            } else {
                0
            }
        )
        true
    } catch (_: IllegalStateException) {
        // ForegroundServiceStartNotAllowedException: a sticky restart while the app is in the background (12+).
        false
    } catch (_: SecurityException) {
        // Location permission missing when promoting a location-type service (14+).
        false
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val ACTION_START = "com.example.locationlogger.action.START_TRACKING"
        private const val ACTION_PAUSE = "com.example.locationlogger.action.PAUSE_TRACKING"
        private const val WAKE_LOCK_TAG = "LocationLogger:capture"
        private const val WAKE_LOCK_TIMEOUT_MS = 30_000L

        @Volatile
        var isRunning = false
            private set

        fun startIntent(context: Context): Intent =
            Intent(context, LocationTrackingService::class.java).setAction(ACTION_START)

        fun pauseIntent(context: Context): Intent =
            Intent(context, LocationTrackingService::class.java).setAction(ACTION_PAUSE)

        fun stopIntent(context: Context): Intent =
            Intent(context, LocationTrackingService::class.java)
    }
}
