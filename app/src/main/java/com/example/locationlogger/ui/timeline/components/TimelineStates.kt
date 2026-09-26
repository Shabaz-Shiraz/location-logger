package com.example.locationlogger.ui.timeline.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.locationlogger.R
import com.example.locationlogger.ui.components.ShimmerBox
import com.example.locationlogger.ui.theme.LocationLoggerTheme

private const val SkeletonRows = 6

@Composable
fun TimelineSkeleton(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        repeat(SkeletonRows) { index ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(horizontal = TimelineHorizontalPadding)
            ) {
                Box(Modifier.width(timeColumnWidth).padding(top = 24.dp), contentAlignment = Alignment.TopEnd) {
                    ShimmerBox(Modifier.size(width = 40.dp, height = 14.dp))
                }
                Spacer(Modifier.width(RailGap))
                TimelineRail(
                    nodeCenterY = 32.dp,
                    lineAbove = index > 0,
                    lineBelow = index < SkeletonRows - 1
                )
                Spacer(Modifier.width(RailGap))
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 6.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShimmerBox(Modifier.fillMaxWidth(0.9f).height(16.dp))
                        ShimmerBox(Modifier.fillMaxWidth(0.6f).height(16.dp))
                        ShimmerBox(Modifier.fillMaxWidth(0.75f).height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineEmptyState(onBackToHome: () -> Unit, modifier: Modifier = Modifier) {
    CenteredMessage(
        title = stringResource(R.string.timeline_empty_title),
        body = stringResource(R.string.timeline_empty_body),
        modifier = modifier
    ) {
        Button(onClick = onBackToHome) { Text(stringResource(R.string.timeline_back_home)) }
    }
}

@Composable
fun TimelineErrorState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    CenteredMessage(
        title = stringResource(R.string.timeline_error_title),
        body = stringResource(R.string.timeline_error_body),
        modifier = modifier,
        isError = true
    ) {
        OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
    }
}

@Composable
private fun CenteredMessage(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    action: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_notification),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(text = title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        action()
    }
}

@Composable
fun TimelineLoadingFooter(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
    }
}

@Composable
fun TimelineErrorFooter(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.timeline_append_error),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
    }
}

@Composable
fun TimelineEndCap(modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = TimelineHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(timeColumnWidth + RailGap))
        Canvas(
            Modifier
                .width(RailWidth)
                .fillMaxHeight()
        ) {
            val x = size.width / 2
            val y = size.height / 2
            drawLine(lineColor, Offset(x, 0f), Offset(x, y), 2.dp.toPx())
            drawCircle(lineColor, 4.dp.toPx(), Offset(x, y))
        }
        Spacer(Modifier.width(RailGap))
        Text(
            text = stringResource(R.string.timeline_end),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, heightDp = 480)
@Composable
private fun TimelineSkeletonPreview() {
    LocationLoggerTheme { TimelineSkeleton() }
}

@Preview(showBackground = true, heightDp = 480)
@Composable
private fun TimelineEmptyPreview() {
    LocationLoggerTheme { TimelineEmptyState(onBackToHome = {}) }
}

@Preview(showBackground = true, heightDp = 480)
@Composable
private fun TimelineErrorPreview() {
    LocationLoggerTheme(darkTheme = true) { TimelineErrorState(onRetry = {}) }
}

@Preview(showBackground = true)
@Composable
private fun TimelineFootersPreview() {
    LocationLoggerTheme {
        Column {
            TimelineLoadingFooter()
            TimelineErrorFooter(onRetry = {})
            TimelineEndCap()
        }
    }
}
