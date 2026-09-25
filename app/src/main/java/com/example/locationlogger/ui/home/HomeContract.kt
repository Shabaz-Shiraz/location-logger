package com.example.locationlogger.ui.home

data object HomeState

sealed interface HomeIntent {
    data object OpenTimeline : HomeIntent
}

sealed interface HomeEffect {
    data object NavigateToTimeline : HomeEffect
}
