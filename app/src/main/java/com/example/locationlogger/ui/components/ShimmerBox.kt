package com.example.locationlogger.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape

/** A placeholder block with a highlight sweeping across it while content loads. */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraSmall
) {
    val base = MaterialTheme.colorScheme.surfaceContainerHighest
    val highlight = MaterialTheme.colorScheme.surfaceContainerLow
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerProgress"
    )

    Box(
        modifier
            .clip(shape)
            .drawBehind {
                val band = size.width
                val x = -band + (size.width + band) * progress
                drawRect(
                    Brush.linearGradient(
                        colors = listOf(base, highlight, base),
                        start = Offset(x, 0f),
                        end = Offset(x + band, size.height)
                    )
                )
            }
    )
}
