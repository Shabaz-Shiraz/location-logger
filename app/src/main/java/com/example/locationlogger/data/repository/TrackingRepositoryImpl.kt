package com.example.locationlogger.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.locationlogger.data.local.TrackingPreferences
import com.example.locationlogger.domain.model.TrackingStatus
import com.example.locationlogger.domain.repository.TrackingRepository
import com.example.locationlogger.service.LocationTrackingService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: TrackingPreferences
) : TrackingRepository {

    override fun observeStatus(): Flow<TrackingStatus> =
        preferences.isTracking.map { if (it) TrackingStatus.Active else TrackingStatus.Paused }


    override suspend fun start() {
        preferences.setTracking(true)
        ContextCompat.startForegroundService(context, LocationTrackingService.startIntent(context))
    }

    override suspend fun stop() {
        preferences.setTracking(false)
        context.stopService(LocationTrackingService.stopIntent(context))
    }

    override suspend fun resumeIfInterrupted() {
        if (!preferences.isTracking.first() || LocationTrackingService.isRunning) return

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) start() else preferences.setTracking(false)
    }
}
