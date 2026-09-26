package com.example.locationlogger.ui.timeline

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.locationlogger.domain.model.TrackingStatus
import com.example.locationlogger.domain.usecase.GetPagedTimelineUseCase
import com.example.locationlogger.domain.usecase.ObserveTrackingStatusUseCase
import com.example.locationlogger.domain.usecase.ResolveMissingAddressUseCase
import com.example.locationlogger.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    getPagedTimeline: GetPagedTimelineUseCase,
    observeTrackingStatus: ObserveTrackingStatusUseCase,
    private val resolveMissingAddress: ResolveMissingAddressUseCase
) : MviViewModel<TimelineState, TimelineIntent, TimelineEffect>(TimelineState()) {

    val items: Flow<PagingData<TimelineUiItem>> = getPagedTimeline()
        .map { page ->
            val zone = ZoneId.systemDefault()
            page
                .map { TimelineUiItem.Entry(it, it.timestamp.atZone(zone).toLocalDate()) }
                .insertSeparators<TimelineUiItem.Entry, TimelineUiItem> { before, after ->
                    // Newest first, so a header goes above the first entry of each (older) day.
                    when {
                        after == null -> null
                        before == null || before.date != after.date -> TimelineUiItem.DayHeader(after.date)
                        else -> null
                    }
                }
        }
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            observeTrackingStatus().collect { status ->
                setState { copy(isTracking = status == TrackingStatus.Active) }
            }
        }
    }

    override fun onIntent(intent: TimelineIntent) {
        when (intent) {
            TimelineIntent.BackClicked -> sendEffect(TimelineEffect.NavigateBack)
            is TimelineIntent.EntryShown -> viewModelScope.launch { resolveMissingAddress(intent.entry) }
        }
    }
}
