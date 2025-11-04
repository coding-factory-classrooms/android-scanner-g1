package com.example.scanner.ui.components.molecules

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun AmplitudeVisualizer(
    amplitude: Int,
    modifier: Modifier = Modifier,
    barCount: Int = 20,
    height: androidx.compose.ui.unit.Dp = 200.dp
) {
    val density = LocalDensity.current
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val normalizedAmplitude = (amplitude / 32767f).coerceIn(0f, 1f)
            val barWidth = size.width / barCount
            val maxHeight = size.height * 0.8f
            val spacing = with(density) { 2.dp.toPx() }

            for (i in 0 until barCount) {
                val barHeight = maxHeight * normalizedAmplitude * (0.5f + (i % 3) * 0.25f)
                val x = i * barWidth + barWidth / 2
                val y = size.height / 2 - barHeight / 2

                drawRect(
                    color = primaryColor,
                    topLeft = Offset(x - barWidth / 2 + spacing, y),
                    size = Size(barWidth - spacing * 2, barHeight)
                )
            }
        }
    }
}
