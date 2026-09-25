package com.example.locationlogger.di

import android.content.Context
import androidx.room.Room
import com.example.locationlogger.data.local.LocationDatabase
import com.example.locationlogger.data.local.LocationEntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LocationDatabase =
        Room.databaseBuilder(context, LocationDatabase::class.java, LocationDatabase.DB_FILE_NAME).build()

    @Provides
    fun provideLocationEntryDao(database: LocationDatabase): LocationEntryDao =
        database.locationEntryDao()
}
