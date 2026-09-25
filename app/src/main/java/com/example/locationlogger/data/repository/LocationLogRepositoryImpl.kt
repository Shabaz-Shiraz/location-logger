package com.example.locationlogger.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.locationlogger.data.local.LocationEntryDao
import com.example.locationlogger.data.local.toDomain
import com.example.locationlogger.data.local.toEntity
import com.example.locationlogger.domain.model.LocationEntry
import com.example.locationlogger.domain.model.LocationFix
import com.example.locationlogger.domain.repository.LocationLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationLogRepositoryImpl @Inject constructor(
    private val dao: LocationEntryDao
) : LocationLogRepository {

    // Lives as long as the process, once per app session
    private val addressRetryClaims = ConcurrentHashMap.newKeySet<Long>()

    override fun pagedEntries(): Flow<PagingData<LocationEntry>> =
        Pager(
            // initialLoadSize defaults to 3 * pageSize; keep every request at 15.
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { dao.pagingSource() }
        ).flow.map { page -> page.map { it.toDomain() } }

    override fun observeCount(): Flow<Int> = dao.observeCount()

    override fun observeLatest(): Flow<LocationEntry?> = dao.observeLatest().map { it?.toDomain() }

    override suspend fun add(fix: LocationFix, address: String?): LocationEntry {
        val entity = fix.toEntity(address)
        val id = dao.insert(entity)
        return entity.copy(id = id).toDomain()
    }

    override suspend fun updateAddress(id: Long, address: String) {
        dao.updateAddress(id, address)
    }

    override fun claimAddressRetry(id: Long): Boolean = addressRetryClaims.add(id)

    private companion object {
        const val PAGE_SIZE = 15
    }
}
