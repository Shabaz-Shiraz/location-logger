package com.example.locationlogger.domain.model

import java.time.Instant

data class LocationFix(
    val point: GeoPoint,
    val accuracyMeters: Float,
    val timestamp: Instant
)
