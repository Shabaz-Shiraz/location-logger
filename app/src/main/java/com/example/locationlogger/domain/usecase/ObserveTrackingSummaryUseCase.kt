package com.example.locationlogger.domain.usecase

import com.example.locationlogger.domain.CAPTURE_INTERVAL
import com.example.locationlogger.domain.model.TrackingStatus
import com.example.locationlogger.domain.model.TrackingSummary
import com.example.locationlogger.domain.repository.LocationLogRepository
import com.example.locationlogger.domain.repository.TrackingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import kotlin.time.toJavaDuration

class ObserveTrackingSummaryUseCase @Inject constructor(
    private val trackingRepository: TrackingRepository,
    private val locationLogRepository: LocationLogRepository
) {
    operator fun invoke(): Flow<TrackingSummary> = combine(
        trackingRepository.observeStatus(),
        locationLogRepository.observeCount(),
        locationLogRepository.observeLatest()
    ) { status, count, latest ->
        TrackingSummary(
            status = status,
            entryCount = count,
            latestEntry = latest,
            nextCaptureAt = latest
                ?.takeIf { status == TrackingStatus.Active }
                ?.timestamp
                ?.plus(CAPTURE_INTERVAL.toJavaDuration())
        )
    }
}
