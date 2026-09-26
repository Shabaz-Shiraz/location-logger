package com.example.locationlogger.ui.timeline

import com.example.locationlogger.domain.model.LocationEntry
import java.time.LocalDate

sealed interface TimelineUiItem {
    val key: String

    data class DayHeader(val date: LocalDate) : TimelineUiItem {
        override val key: String get() = "day-$date"
    }

    data class Entry(val entry: LocationEntry, val date: LocalDate) : TimelineUiItem {
        override val key: String get() = "entry-${entry.id}"
    }
}
