package com.example.locationlogger.domain.repository

import com.example.locationlogger.domain.model.GeoPoint

interface AddressResolver {

    suspend fun resolve(point: GeoPoint): String?
}
