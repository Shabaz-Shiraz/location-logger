package com.example.locationlogger.ui.timeline

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import com.example.locationlogger.ui.timeline.components.TimelineEmptyState
import com.example.locationlogger.ui.timeline.components.TimelineEndCap
import com.example.locationlogger.ui.timeline.components.TimelineErrorFooter
import com.example.locationlogger.ui.timeline.components.TimelineErrorState
import com.example.locationlogger.ui.timeline.components.TimelineLoadingFooter
import com.example.locationlogger.ui.timeline.components.TimelineSkeleton
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.locationlogger.R
import com.example.locationlogger.ui.format.formatDayLabel
import com.example.locationlogger.ui.mvi.CollectEffects
import com.example.locationlogger.ui.timeline.components.DayHeader
import com.example.locationlogger.ui.timeline.components.TimelineEntryRow

@Composable
fun TimelineScreen(
    onNavigateBack: () -> Unit,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items = viewModel.items.collectAsLazyPagingItems()

    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            TimelineEffect.NavigateBack -> onNavigateBack()
        }
    }

    TimelineContent(state = state, items = items, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineContent(
    state: TimelineState,
    items: LazyPagingItems<TimelineUiItem>,
    onIntent: (TimelineIntent) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.timeline_title)) },
                navigationIcon = {
                    IconButton(onClick = { onIntent(TimelineIntent.BackClicked) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        val loadState = items.loadState
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when {
            items.itemCount == 0 && loadState.refresh is LoadState.Loading ->
                TimelineSkeleton(contentModifier)
            items.itemCount == 0 && loadState.refresh is LoadState.Error ->
                TimelineErrorState(onRetry = items::retry, modifier = contentModifier)
            items.itemCount == 0 && loadState.append.endOfPaginationReached ->
                TimelineEmptyState(onBackToHome = { onIntent(TimelineIntent.BackClicked) }, modifier = contentModifier)
            else -> TimelineList(state, items, onIntent, contentModifier)
        }
    }
}

@Composable
private fun TimelineList(
    state: TimelineState,
    items: LazyPagingItems<TimelineUiItem>,
    onIntent: (TimelineIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val append = items.loadState.append

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 16.dp)) {

        for (index in 0 until items.itemCount) {
            when (val item = items.peek(index)) {
                is TimelineUiItem.DayHeader -> stickyHeader(key = item.key, contentType = "day") {
                    val header = items[index] as? TimelineUiItem.DayHeader ?: item
                    DayHeader(
                        label = formatDayLabel(context, header.date),
                        isFirst = index == 0,
                        modifier = Modifier.animateItem()
                    )
                }
                is TimelineUiItem.Entry -> item(key = item.key, contentType = "entry") {
                    val entry = items[index] as? TimelineUiItem.Entry ?: item
                    if (entry.entry.address == null) {
                        LaunchedEffect(entry.entry.id) { onIntent(TimelineIntent.EntryShown(entry.entry)) }
                    }
                    TimelineEntryRow(
                        entry = entry.entry,
                        // Index 0 is always the header for the newest day.
                        isNewest = index == 1,
                        isLive = state.isTracking,
                        modifier = Modifier.animateItem()
                    )
                }
                null -> Unit
            }
        }

        item(key = "footer", contentType = "footer") {
            when {
                append is LoadState.Loading -> TimelineLoadingFooter()
                append is LoadState.Error -> TimelineErrorFooter(onRetry = items::retry)
                append.endOfPaginationReached -> TimelineEndCap()
            }
        }
    }
}
