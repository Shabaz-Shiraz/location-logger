package com.example.locationlogger.domain.usecase

import com.example.locationlogger.domain.model.TrackingStatus
import com.example.locationlogger.domain.repository.TrackingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTrackingStatusUseCase @Inject constructor(
    private val trackingRepository: TrackingRepository
) {
    operator fun invoke(): Flow<TrackingStatus> = trackingRepository.observeStatus()
}
