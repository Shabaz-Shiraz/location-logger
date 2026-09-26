package com.example.locationlogger.ui.timeline.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.locationlogger.ui.components.PulsingDot

internal val RailWidth = 28.dp
private val LineWidth = 2.dp
private val NodeSize = 12.dp

@Composable
fun TimelineRail(
    nodeCenterY: Dp,
    modifier: Modifier = Modifier,
    lineAbove: Boolean = true,
    lineBelow: Boolean = true,
    showNode: Boolean = true,
    live: Boolean = false
) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val nodeColor = MaterialTheme.colorScheme.primary
    val holeColor = MaterialTheme.colorScheme.background
    val liveColor = MaterialTheme.colorScheme.tertiary

    Box(modifier.width(RailWidth).fillMaxHeight()) {
        Canvas(Modifier.fillMaxSize()) {
            val x = size.width / 2
            val nodeY = nodeCenterY.toPx()
            val stroke = LineWidth.toPx()
            if (lineAbove) drawLine(lineColor, Offset(x, 0f), Offset(x, nodeY), stroke)
            if (lineBelow) drawLine(lineColor, Offset(x, nodeY), Offset(x, size.height), stroke)
            if (showNode && !live) {
                val radius = NodeSize.toPx() / 2
                drawCircle(holeColor, radius, Offset(x, nodeY))
                drawCircle(nodeColor, radius - stroke / 2, Offset(x, nodeY), style = Stroke(stroke * 1.5f))
            }
        }
        if (showNode && live) {
            PulsingDot(
                color = liveColor,
                dotSize = NodeSize,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .centerOn(nodeCenterY)
            )
        }
    }
}

// Takes no space itself and draws centred on (0, y), so a halo wider than the rail isn't clipped.
private fun Modifier.centerOn(y: Dp): Modifier = layout { measurable, _ ->
    val placeable = measurable.measure(Constraints())
    layout(0, 0) {
        placeable.place(-placeable.width / 2, y.roundToPx() - placeable.height / 2)
    }
}
