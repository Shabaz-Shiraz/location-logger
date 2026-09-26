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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val RingCount = 3

@Composable
fun RadarPulse(
    active: Boolean,
    activeColor: Color,
    idleColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp
) {
    if (active) {
        val transition = rememberInfiniteTransition(label = "radar")
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
            label = "radarProgress"
        )
        Canvas(modifier.size(size)) {
            val maxRadius = this.size.minDimension / 2
            val dotRadius = maxRadius * 0.2f
            repeat(RingCount) { ring ->
                // Staggered so there's always a ring on its way out.
                val p = (progress + ring.toFloat() / RingCount) % 1f
                drawCircle(
                    color = activeColor.copy(alpha = 0.55f * (1f - p)),
                    radius = dotRadius + (maxRadius - dotRadius) * p,
                    style = Stroke(2.dp.toPx())
                )
            }
            drawDot(activeColor, dotRadius)
        }
    } else {
        Canvas(modifier.size(size)) {
            val maxRadius = this.size.minDimension / 2
            drawCircle(idleColor.copy(alpha = 0.35f), maxRadius * 0.55f, style = Stroke(2.dp.toPx()))
            drawDot(idleColor, maxRadius * 0.2f)
        }
    }
}

private fun DrawScope.drawDot(color: Color, radius: Float) {
    drawCircle(color.copy(alpha = 0.2f), radius * 1.8f)
    drawCircle(color, radius)
}
