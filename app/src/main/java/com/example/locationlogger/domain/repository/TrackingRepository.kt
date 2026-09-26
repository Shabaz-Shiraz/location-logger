package com.example.locationlogger.domain.repository

import com.example.locationlogger.domain.model.TrackingStatus
import kotlinx.coroutines.flow.Flow

interface TrackingRepository {

    fun observeStatus(): Flow<TrackingStatus>

    suspend fun start()

    suspend fun stop()

    suspend fun resumeIfInterrupted()
}
