package com.example.locationlogger.data.location

import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.example.locationlogger.di.IoDispatcher
import com.example.locationlogger.domain.model.GeoPoint
import com.example.locationlogger.domain.repository.AddressResolver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

class GeocoderAddressResolver @Inject constructor(
    private val geocoder: Geocoder,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AddressResolver {

    override suspend fun resolve(point: GeoPoint): String? {
        if (!Geocoder.isPresent()) return null
        return try {
            withTimeoutOrNull(TIMEOUT) { lookup(point) }?.format()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun lookup(point: GeoPoint): Address? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(
                    point.latitude,
                    point.longitude,
                    1,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            continuation.resume(addresses.firstOrNull())
                        }

                        override fun onError(errorMessage: String?) {
                            continuation.resume(null)
                        }
                    }
                )
            }
        } else {
            withContext(ioDispatcher) {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(point.latitude, point.longitude, 1)?.firstOrNull()
            }
        }

    private fun Address.format(): String? {
        getAddressLine(0)?.takeIf { it.isNotBlank() }?.let { return it }
        val street = listOfNotNull(subThoroughfare, thoroughfare).joinToString(" ")
        return listOf(street, locality, adminArea, countryName)
            .filter { !it.isNullOrBlank() }
            .joinToString(", ")
            .ifBlank { null }
    }

    private companion object {
        val TIMEOUT = 10.seconds
    }
}
