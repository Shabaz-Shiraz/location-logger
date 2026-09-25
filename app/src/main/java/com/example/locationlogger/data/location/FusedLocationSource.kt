package com.example.locationlogger.data.location

import android.annotation.SuppressLint
import android.os.Looper
import com.example.locationlogger.domain.model.GeoPoint
import com.example.locationlogger.domain.model.LocationFix
import com.example.locationlogger.domain.repository.LocationSource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration

class FusedLocationSource @Inject constructor(
    private val client: FusedLocationProviderClient
) : LocationSource {

    // Permission is checked by the service before it starts collecting, a revoked permission closes the flow with a SecurityException
    @SuppressLint("MissingPermission")
    override fun updates(interval: Duration): Flow<LocationFix> = callbackFlow {
        val intervalMillis = interval.inWholeMilliseconds

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis)
            .setMinUpdateIntervalMillis(intervalMillis)
            .setMaxUpdateDelayMillis(0)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                trySend(
                    LocationFix(
                        point = GeoPoint(location.latitude, location.longitude),
                        accuracyMeters = location.accuracy,
                        timestamp = Instant.ofEpochMilli(location.time)
                    )
                )
            }
        }

        try {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            close(e)
        }

        awaitClose { client.removeLocationUpdates(callback) }
    }
}
