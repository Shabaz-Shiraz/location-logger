package com.example.locationlogger.ui.timeline

import com.example.locationlogger.domain.model.LocationEntry

data class TimelineState(
    val isTracking: Boolean = false
)

sealed interface TimelineIntent {
    data object BackClicked : TimelineIntent
    data class EntryShown(val entry: LocationEntry) : TimelineIntent
}

sealed interface TimelineEffect {
    data object NavigateBack : TimelineEffect
}
