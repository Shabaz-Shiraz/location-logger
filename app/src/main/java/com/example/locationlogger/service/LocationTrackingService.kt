package com.example.locationlogger.service

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleService

class LocationTrackingService : LifecycleService() {

    companion object {
        const val ACTION_START = "com.example.locationlogger.action.START_TRACKING"

        fun startIntent(context: Context): Intent =
            Intent(context, LocationTrackingService::class.java).setAction(ACTION_START)

        fun stopIntent(context: Context): Intent =
            Intent(context, LocationTrackingService::class.java)
    }
}
