package com.example.locationlogger.ui.timeline.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.locationlogger.R
import com.example.locationlogger.domain.model.GeoPoint
import com.example.locationlogger.domain.model.LocationEntry
import com.example.locationlogger.ui.components.AddressUnavailablePill
import com.example.locationlogger.ui.format.formatCoordinates
import com.example.locationlogger.ui.format.formatTimeParts
import com.example.locationlogger.ui.theme.CoordinatesTextStyle
import com.example.locationlogger.ui.theme.LocationLoggerTheme
import java.time.Instant
import kotlin.math.roundToInt

internal val timeColumnWidth: Dp
    @Composable get() = with(LocalDensity.current) { 56.sp.toDp() }
internal val TimelineHorizontalPadding = 16.dp
internal val RailGap = 8.dp

private val CardTopGap = 6.dp
private val CardPadding = 14.dp

private val nodeCenterY: Dp
    @Composable get() = CardTopGap + CardPadding +
        with(LocalDensity.current) { (MaterialTheme.typography.titleMedium.lineHeight / 2).toDp() }

@Composable
fun TimelineEntryRow(
    entry: LocationEntry,
    modifier: Modifier = Modifier,
    isNewest: Boolean = false,
    isOldest: Boolean = false,
    isLive: Boolean = false
) {
    val nodeY = nodeCenterY
    val timeStyle = MaterialTheme.typography.titleSmall
    val timeTopPadding = nodeY - with(LocalDensity.current) { (timeStyle.lineHeight / 2).toDp() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = TimelineHorizontalPadding)
    ) {
        val (clock, marker) = formatTimeParts(LocalContext.current, entry.timestamp)
        val timeColor = if (isNewest) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .width(timeColumnWidth)
                .padding(top = timeTopPadding)
        ) {
            Text(text = clock, style = timeStyle, color = timeColor, maxLines = 1, textAlign = TextAlign.End)
            if (marker != null) {
                Text(
                    text = marker,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
        Spacer(Modifier.width(RailGap))
        TimelineRail(
            nodeCenterY = nodeY,
            lineAbove = !isNewest,
            lineBelow = !isOldest,
            live = isNewest && isLive
        )
        Spacer(Modifier.width(RailGap))
        EntryCard(
            entry = entry,
            modifier = Modifier
                .weight(1f)
                .padding(top = CardTopGap, bottom = 6.dp)
        )
    }
}

@Composable
private fun EntryCard(entry: LocationEntry, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(CardPadding),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (entry.address != null) {
                Text(
                    text = entry.address,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                AddressUnavailablePill()
            }
            Text(
                text = stringResource(
                    R.string.timeline_coordinates_accuracy,
                    entry.point.formatCoordinates(),
                    entry.accuracyMeters.roundToInt()
                ),
                style = CoordinatesTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private val previewEntry = LocationEntry(
    id = 1,
    timestamp = Instant.parse("2026-09-26T04:30:00Z"),
    point = GeoPoint(9.98160, 76.28000),
    accuracyMeters = 12.4f,
    address = "Marine Drive, Ernakulam, Kochi, Kerala 682031, India"
)

@Preview(name = "Light", showBackground = true)
@Composable
private fun TimelineEntryRowPreview() {
    LocationLoggerTheme(darkTheme = false) {
        Column {
            TimelineEntryRow(previewEntry, isNewest = true, isLive = true)
            TimelineEntryRow(previewEntry.copy(id = 2, address = null))
            TimelineEntryRow(previewEntry.copy(id = 3), isOldest = true)
        }
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0E1513)
@Composable
private fun TimelineEntryRowDarkPreview() {
    LocationLoggerTheme(darkTheme = true) {
        Column {
            TimelineEntryRow(previewEntry, isNewest = true, isLive = true)
            TimelineEntryRow(previewEntry.copy(id = 2, address = null), isOldest = true)
        }
    }
}
