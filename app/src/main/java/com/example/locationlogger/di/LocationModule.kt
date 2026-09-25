package com.example.locationlogger.di

import android.content.Context
import android.location.Geocoder
import com.example.locationlogger.data.location.FusedLocationSource
import com.example.locationlogger.data.location.GeocoderAddressResolver
import com.example.locationlogger.domain.repository.AddressResolver
import com.example.locationlogger.domain.repository.LocationSource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    abstract fun bindLocationSource(source: FusedLocationSource): LocationSource

    @Binds
    abstract fun bindAddressResolver(resolver: GeocoderAddressResolver): AddressResolver

    companion object {

        @Provides
        @Singleton
        fun provideFusedLocationClient(@ApplicationContext context: Context): FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        @Provides
        @Singleton
        fun provideGeocoder(@ApplicationContext context: Context): Geocoder = Geocoder(context)
    }
}
