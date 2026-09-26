package com.example.locationlogger.ui.format

import android.content.Context
import android.text.format.DateFormat
import com.example.locationlogger.domain.model.GeoPoint
import java.time.Instant
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/** Short time in the device's locale, honouring its 12/24-hour setting. */
fun formatTime(context: Context, instant: Instant): String =
    DateFormat.getTimeFormat(context).format(Date.from(instant))

/** e.g. "12.97160° N, 77.59460° E". Always a dot decimal so the columns line up in any locale. */
fun GeoPoint.formatCoordinates(): String {
    val lat = String.format(Locale.US, "%.5f° %s", abs(latitude), if (latitude >= 0) "N" else "S")
    val lon = String.format(Locale.US, "%.5f° %s", abs(longitude), if (longitude >= 0) "E" else "W")
    return "$lat, $lon"
}
