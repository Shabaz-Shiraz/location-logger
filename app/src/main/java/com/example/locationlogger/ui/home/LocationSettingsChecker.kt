package com.example.locationlogger.ui.home

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.location.LocationManagerCompat
import com.example.locationlogger.domain.CAPTURE_INTERVAL
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

@Composable
fun rememberLocationSettingsChecker(onResult: (enabled: Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)


    val resolutionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { currentOnResult(context.isLocationEnabled()) }

    return remember(context, resolutionLauncher) {
        {
            val request = LocationSettingsRequest.Builder()
                .addLocationRequest(
                    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, CAPTURE_INTERVAL.inWholeMilliseconds)
                        .build()
                )
                .build()

            LocationServices.getSettingsClient(context)
                .checkLocationSettings(request)
                .addOnSuccessListener { currentOnResult(true) }
                .addOnFailureListener { error ->
                    when {
                        error is ResolvableApiException -> resolutionLauncher.launch(
                            IntentSenderRequest.Builder(error.resolution).build()
                        )
                        context.isLocationEnabled() -> currentOnResult(true)
                        else -> {
                            context.startActivity(
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                            currentOnResult(false)
                        }
                    }
                }
        }
    }
}

private fun Context.isLocationEnabled(): Boolean =
    LocationManagerCompat.isLocationEnabled(getSystemService(LocationManager::class.java))
