package com.example.locationlogger.ui.home

import com.example.locationlogger.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() :
    MviViewModel<HomeState, HomeIntent, HomeEffect>(HomeState) {

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.OpenTimeline -> sendEffect(HomeEffect.NavigateToTimeline)
        }
    }
}
