package com.example.locationlogger.domain.usecase

import com.example.locationlogger.domain.repository.TrackingRepository
import javax.inject.Inject

class StartTrackingUseCase @Inject constructor(
    private val trackingRepository: TrackingRepository
) {
    suspend operator fun invoke() = trackingRepository.start()
}
