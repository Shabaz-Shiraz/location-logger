package com.example.locationlogger.ui.format

import android.content.Context
import android.text.format.DateFormat
import com.example.locationlogger.R
import com.example.locationlogger.domain.model.GeoPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.abs

fun formatTime(context: Context, instant: Instant): String =
    DateFormat.getTimeFormat(context).format(Date.from(instant))

fun formatTimeParts(context: Context, instant: Instant): Pair<String, String?> {
    val locale = context.resources.configuration.locales[0]
    val time = instant.atZone(ZoneId.systemDefault())
    if (DateFormat.is24HourFormat(context)) {
        val pattern = DateFormat.getBestDateTimePattern(locale, "Hmm")
        return DateTimeFormatter.ofPattern(pattern, locale).format(time) to null
    }
    val clock = DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "hmm")
        .replace("a", "").trim(), locale).format(time)
    val marker = DateTimeFormatter.ofPattern("a", locale).format(time)
    return clock to marker
}

/** "Today", "Yesterday", otherwise e.g. "Fri, 25 Sep" (with the year once it's not the current one). */
fun formatDayLabel(context: Context, date: LocalDate, today: LocalDate = LocalDate.now()): String =
    when (date) {
        today -> context.getString(R.string.day_today)
        today.minusDays(1) -> context.getString(R.string.day_yesterday)
        else -> {
            val locale = context.resources.configuration.locales[0]
            val skeleton = if (date.year == today.year) "EEEdMMM" else "EEEdMMMyyyy"
            DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, skeleton), locale).format(date)
        }
    }

fun GeoPoint.formatCoordinates(): String {
    val lat = String.format(Locale.US, "%.5f° %s", abs(latitude), if (latitude >= 0) "N" else "S")
    val lon = String.format(Locale.US, "%.5f° %s", abs(longitude), if (longitude >= 0) "E" else "W")
    return "$lat, $lon"
}
