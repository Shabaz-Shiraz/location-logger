package com.example.locationlogger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LocationEntryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class LocationDatabase : RoomDatabase() {

    abstract fun locationEntryDao(): LocationEntryDao

    companion object {
        const val DB_FILE_NAME = "location_logger.db"

        const val LOCATION_TABLE = "location_entries"
    }
}
