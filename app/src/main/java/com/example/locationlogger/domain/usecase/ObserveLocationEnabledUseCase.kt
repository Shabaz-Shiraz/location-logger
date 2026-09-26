package com.example.locationlogger.domain.usecase

import com.example.locationlogger.domain.repository.LocationSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLocationEnabledUseCase @Inject constructor(
    private val locationSource: LocationSource
) {
    operator fun invoke(): Flow<Boolean> = locationSource.observeLocationEnabled()
}
