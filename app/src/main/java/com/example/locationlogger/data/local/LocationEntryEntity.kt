package com.example.locationlogger.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.locationlogger.data.local.LocationDatabase.Companion.LOCATION_TABLE

@Entity(
    tableName = LOCATION_TABLE,
    indices = [Index("timestamp")]
)
data class LocationEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    // Epoch millis (UTC); formatting to local time happens in the UI.
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "accuracy_meters") val accuracyMeters: Float,
    val address: String?
)
