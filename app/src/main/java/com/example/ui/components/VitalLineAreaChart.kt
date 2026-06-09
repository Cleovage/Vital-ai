package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextSecondary

@Composable
fun VitalLineAreaChart(
    yValues: List<Float>,
    metricName: String,
    lineColor: Color,
    areaColor: Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        if (yValues.isEmpty()) return@Canvas

        val maxVal = yValues.maxOrNull() ?: 0f
        val minVal = yValues.minOrNull() ?: 0f
        val range = (maxVal - minVal).coerceAtLeast(1f)
        
        // Add 15% padding top and bottom so the line doesn't hit the absolute edges
        val yMax = maxVal + range * 0.15f
        val yMin = (minVal - range * 0.15f).coerceAtLeast(0f)
        val yRange = (yMax - yMin).coerceAtLeast(1f)

        val width = size.width
        val height = size.height

        // Define padding for axes/labels
        val paddingLeft = 50.dp.toPx()
        val paddingRight = 16.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val paddingBottom = 28.dp.toPx()

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // 1. Draw 3 Horizontal Gridlines & Y-axis labels
        val gridLinesCount = 3
        for (i in 0 until gridLinesCount) {
            val ratio = i.toFloat() / (gridLinesCount - 1)
            val gridY = paddingTop + chartHeight * ratio
            val gridVal = yMax - ratio * yRange

            // Gridline
            drawLine(
                color = TextSecondary.copy(alpha = 0.15f),
                start = Offset(paddingLeft, gridY),
                end = Offset(paddingLeft + chartWidth, gridY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            )

            // Y-axis label
            val textLayoutResult = textMeasurer.measure(
                text = formatValue(gridVal, metricName),
                style = TextStyle(color = TextSecondary, fontSize = 10.sp)
            )
            drawText(
                textLayoutResult = textLayoutResult,
                color = TextSecondary,
                topLeft = Offset(
                    x = paddingLeft - textLayoutResult.size.width - 8.dp.toPx(),
                    y = gridY - textLayoutResult.size.height / 2f
                )
            )
        }

        // 2. Draw X-axis day labels
        val days = listOf("6d", "5d", "4d", "3d", "2d", "1d", "Today")
        days.forEachIndexed { i, day ->
            val x = paddingLeft + i * (chartWidth / 6f)
            val textLayoutResult = textMeasurer.measure(
                text = day,
                style = TextStyle(color = TextSecondary, fontSize = 10.sp)
            )
            drawText(
                textLayoutResult = textLayoutResult,
                color = TextSecondary,
                topLeft = Offset(
                    x = x - textLayoutResult.size.width / 2f,
                    y = paddingTop + chartHeight + 8.dp.toPx()
                )
            )
        }

        // 3. Compute Bezier Path & Gradient Area Path
        val path = Path()
        val areaPath = Path()
        val bottomY = paddingTop + chartHeight

        yValues.forEachIndexed { i, yValue ->
            val x = paddingLeft + i * (chartWidth / 6f)
            val y = paddingTop + chartHeight * (1f - (yValue - yMin) / yRange)

            if (i == 0) {
                path.moveTo(x, y)
                areaPath.moveTo(x, bottomY)
                areaPath.lineTo(x, y)
            } else {
                val prevX = paddingLeft + (i - 1) * (chartWidth / 6f)
                val prevY = paddingTop + chartHeight * (1f - (yValues[i - 1] - yMin) / yRange)
                
                // Cubic Bezier control points for smooth curves
                val cp1X = prevX + (x - prevX) / 2f
                val cp1Y = prevY
                val cp2X = prevX + (x - prevX) / 2f
                val cp2Y = y

                path.cubicTo(cp1X, cp1Y, cp2X, cp2Y, x, y)
                areaPath.cubicTo(cp1X, cp1Y, cp2X, cp2Y, x, y)
            }

            if (i == yValues.size - 1) {
                areaPath.lineTo(x, bottomY)
                areaPath.close()
            }
        }

        // 4. Draw Gradient Area
        if (yValues.size > 1) {
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        areaColor.copy(alpha = 0.35f),
                        areaColor.copy(alpha = 0.0f)
                    ),
                    startY = paddingTop,
                    endY = paddingTop + chartHeight
                )
            )
        }

        // 5. Draw Bezier Line
        if (yValues.size > 1) {
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        // 6. Draw Dots/Indicators for each point
        yValues.forEachIndexed { i, yValue ->
            val x = paddingLeft + i * (chartWidth / 6f)
            val y = paddingTop + chartHeight * (1f - (yValue - yMin) / yRange)

            if (i == yValues.size - 1) {
                // Today's highlighted dot
                drawCircle(
                    color = lineColor.copy(alpha = 0.3f),
                    radius = 8.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            } else {
                // Regular dot
                drawCircle(
                    color = lineColor.copy(alpha = 0.8f),
                    radius = 3.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}

private fun formatValue(value: Float, metricName: String): String {
    val key = metricName.lowercase()
    return when (key) {
        "distance", "hydration" -> String.format("%.1f", value)
        "spo2" -> "${value.toInt()}%"
        else -> value.toInt().toString()
    }
}
