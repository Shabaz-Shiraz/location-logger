package com.example.locationlogger.data.local

import com.example.locationlogger.domain.model.GeoPoint
import com.example.locationlogger.domain.model.LocationEntry
import com.example.locationlogger.domain.model.LocationFix
import java.time.Instant

fun LocationEntryEntity.toDomain(): LocationEntry = LocationEntry(
    id = id,
    timestamp = Instant.ofEpochMilli(timestamp),
    point = GeoPoint(latitude, longitude),
    accuracyMeters = accuracyMeters,
    address = address
)

fun LocationFix.toEntity(address: String?): LocationEntryEntity = LocationEntryEntity(
    timestamp = timestamp.toEpochMilli(),
    latitude = point.latitude,
    longitude = point.longitude,
    accuracyMeters = accuracyMeters,
    address = address
)
