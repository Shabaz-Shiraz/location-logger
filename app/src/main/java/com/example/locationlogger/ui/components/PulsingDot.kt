package com.example.locationlogger.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PulsingDot(
    color: Color,
    modifier: Modifier = Modifier,
    dotSize: Dp = 12.dp,
    haloScale: Float = 2.4f
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
        label = "pulseProgress"
    )

    Canvas(modifier.size(dotSize * haloScale)) {
        val dotRadius = dotSize.toPx() / 2
        drawCircle(
            color = color.copy(alpha = 0.45f * (1f - progress)),
            radius = dotRadius + (size.minDimension / 2 - dotRadius) * progress
        )
        drawCircle(color = color, radius = dotRadius)
    }
}
