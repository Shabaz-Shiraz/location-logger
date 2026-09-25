package com.example.locationlogger.domain.usecase

import com.example.locationlogger.domain.model.LocationEntry
import com.example.locationlogger.domain.repository.AddressResolver
import com.example.locationlogger.domain.repository.LocationLogRepository
import javax.inject.Inject

class ResolveMissingAddressUseCase @Inject constructor(
    private val addressResolver: AddressResolver,
    private val locationLogRepository: LocationLogRepository
) {
    // Room invalidates the paging source on update, so a successful lookup shows up in the list without any extra wiring.
    suspend operator fun invoke(entry: LocationEntry) {
        if (entry.address != null) return
        if (!locationLogRepository.claimAddressRetry(entry.id)) return
        val address = addressResolver.resolve(entry.point) ?: return
        locationLogRepository.updateAddress(entry.id, address)
    }
}
