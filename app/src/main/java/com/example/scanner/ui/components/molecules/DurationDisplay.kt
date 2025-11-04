package com.example.scanner.ui.components.molecules

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import java.util.Locale

@Composable
fun DurationDisplay(duration: Long, modifier: Modifier = Modifier) {
    val seconds = (duration / 1000) % 60
    val minutes = (duration / (1000 * 60)) % 60
    val hours = duration / (1000 * 60 * 60)

    Text(
        text = String.format(Locale.getDefault(), if (hours > 0) "%02d:%02d:%02d" else "%02d:%02d", hours, minutes, seconds),
        style = MaterialTheme.typography.displayMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}
