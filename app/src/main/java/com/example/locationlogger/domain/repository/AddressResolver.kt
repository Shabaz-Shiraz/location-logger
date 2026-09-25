package com.example.locationlogger.domain.repository

import com.example.locationlogger.domain.model.GeoPoint

interface AddressResolver {

    /** Street address for [point], or null if it can't be resolved right now. Never throws. */
    suspend fun resolve(point: GeoPoint): String?
}
