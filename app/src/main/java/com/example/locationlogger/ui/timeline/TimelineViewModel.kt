package com.example.locationlogger.ui.timeline

import com.example.locationlogger.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor() :
    MviViewModel<TimelineState, TimelineIntent, TimelineEffect>(TimelineState) {

    override fun onIntent(intent: TimelineIntent) {
        when (intent) {
            TimelineIntent.BackClicked -> sendEffect(TimelineEffect.NavigateBack)
        }
    }
}
