package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.compose.ui.draw.blur

@Composable
fun ActivityRing(
    progress: Float,
    color: Color,
    dimColor: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 12.dp
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().padding(strokeWidth / 2)) {
            val diameter = size.minDimension - strokeWidth.toPx()
            val left = (size.width - diameter) / 2f
            val top = (size.height - diameter) / 2f
            val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)
            val topLeft = androidx.compose.ui.geometry.Offset(left, top)

            // Background track
            drawArc(
                color = dimColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Foreground track (with glow if needed, here just basic arc for simplicity on Canvas)
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = (animatedProgress.value * 360f).coerceIn(0f, 360f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
