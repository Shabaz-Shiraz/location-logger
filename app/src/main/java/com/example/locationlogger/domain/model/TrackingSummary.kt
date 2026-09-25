package com.example.locationlogger.domain.model

import java.time.Instant

data class TrackingSummary(
    val status: TrackingStatus,
    val entryCount: Int,
    val latestEntry: LocationEntry?,
    val nextCaptureAt: Instant?
)
