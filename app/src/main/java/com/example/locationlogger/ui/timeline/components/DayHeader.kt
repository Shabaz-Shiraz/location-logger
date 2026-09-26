package com.example.locationlogger.ui.timeline.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.locationlogger.ui.theme.LocationLoggerTheme

private val HeaderHeight = 44.dp

@Composable
fun DayHeader(
    label: String,
    modifier: Modifier = Modifier,
    isFirst: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(HeaderHeight)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = TimelineHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(timeColumnWidth + RailGap))
        TimelineRail(
            nodeCenterY = HeaderHeight / 2,
            lineAbove = !isFirst,
            lineBelow = !isFirst,
            showNode = false
        )
        Spacer(Modifier.width(RailGap))
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DayHeaderPreview() {
    LocationLoggerTheme {
        DayHeader(label = "Yesterday")
    }
}
