package com.example.locationlogger.domain.repository

import com.example.locationlogger.domain.model.LocationFix
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface LocationSource {

    fun updates(interval: Duration): Flow<LocationFix>
    fun observeLocationEnabled(): Flow<Boolean>
}
