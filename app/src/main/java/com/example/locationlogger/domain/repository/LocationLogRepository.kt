package com.example.locationlogger.domain.repository

import androidx.paging.PagingData
import com.example.locationlogger.domain.model.LocationEntry
import com.example.locationlogger.domain.model.LocationFix
import kotlinx.coroutines.flow.Flow

interface LocationLogRepository {

    fun pagedEntries(): Flow<PagingData<LocationEntry>>

    fun observeCount(): Flow<Int>

    fun observeLatest(): Flow<LocationEntry?>

    suspend fun add(fix: LocationFix, address: String?): LocationEntry

    suspend fun updateAddress(id: Long, address: String)

    /**
     * Returns true only the first time it is called for [id] in this process, so a missing
     * address is retried at most once per app session.
     */
    fun claimAddressRetry(id: Long): Boolean
}
