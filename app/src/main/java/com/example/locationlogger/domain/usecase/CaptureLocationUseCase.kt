package com.example.locationlogger.domain.usecase

import com.example.locationlogger.domain.model.LocationEntry
import com.example.locationlogger.domain.model.LocationFix
import com.example.locationlogger.domain.repository.AddressResolver
import com.example.locationlogger.domain.repository.LocationLogRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CaptureLocationUseCase @Inject constructor(
    private val addressResolver: AddressResolver,
    private val locationLogRepository: LocationLogRepository
) {
    // The fix is always saved; a failed lookup just leaves the address empty so the
    // timeline can retry it later instead of losing the entry.
    suspend operator fun invoke(fix: LocationFix): LocationEntry {
        val latest = locationLogRepository.observeLatest().first()
        if (latest != null && latest.timestamp == fix.timestamp) return latest

        val address = addressResolver.resolve(fix.point)
        return locationLogRepository.add(fix, address)
    }
}
