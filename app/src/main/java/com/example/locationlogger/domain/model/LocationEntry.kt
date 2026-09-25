package com.example.locationlogger.domain.model

import java.time.Instant

data class LocationEntry(
    val id: Long,
    val timestamp: Instant,
    val point: GeoPoint,
    val accuracyMeters: Float,
    // Null when reverse geocoding failed at capture time.
    val address: String?
)
