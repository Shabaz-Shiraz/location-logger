package com.example.locationlogger.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.locationlogger.data.local.LocationDatabase.Companion.LOCATION_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationEntryDao {

    @Query("SELECT * FROM $LOCATION_TABLE ORDER BY timestamp DESC, id DESC")
    fun pagingSource(): PagingSource<Int, LocationEntryEntity>

    @Insert
    suspend fun insert(entry: LocationEntryEntity): Long

    @Query("UPDATE $LOCATION_TABLE SET address = :address WHERE id = :id")
    suspend fun updateAddress(id: Long, address: String)

    @Query("SELECT COUNT(*) FROM $LOCATION_TABLE")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM $LOCATION_TABLE ORDER BY timestamp DESC, id DESC LIMIT 1")
    fun observeLatest(): Flow<LocationEntryEntity?>
}
