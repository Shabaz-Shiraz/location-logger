package com.example.locationlogger.di

import com.example.locationlogger.data.repository.LocationLogRepositoryImpl
import com.example.locationlogger.data.repository.TrackingRepositoryImpl
import com.example.locationlogger.domain.repository.LocationLogRepository
import com.example.locationlogger.domain.repository.TrackingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindLocationLogRepository(impl: LocationLogRepositoryImpl): LocationLogRepository

    @Binds
    abstract fun bindTrackingRepository(impl: TrackingRepositoryImpl): TrackingRepository
}
