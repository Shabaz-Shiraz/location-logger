package com.example.locationlogger.ui.timeline

data object TimelineState

sealed interface TimelineIntent {
    data object BackClicked : TimelineIntent
}

sealed interface TimelineEffect {
    data object NavigateBack : TimelineEffect
}
