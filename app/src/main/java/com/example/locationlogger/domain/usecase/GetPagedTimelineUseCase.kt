package com.example.locationlogger.domain.usecase

import androidx.paging.PagingData
import com.example.locationlogger.domain.model.LocationEntry
import com.example.locationlogger.domain.repository.LocationLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPagedTimelineUseCase @Inject constructor(
    private val locationLogRepository: LocationLogRepository
) {
    operator fun invoke(): Flow<PagingData<LocationEntry>> = locationLogRepository.pagedEntries()
}
