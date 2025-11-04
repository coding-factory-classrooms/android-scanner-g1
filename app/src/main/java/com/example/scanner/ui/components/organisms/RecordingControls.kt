package com.example.scanner.ui.components.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.scanner.ui.components.atoms.RecordIconButton
import com.example.scanner.ui.components.atoms.StopIconButton
import com.example.scanner.ui.components.molecules.AmplitudeVisualizer
import com.example.scanner.ui.components.molecules.DurationDisplay

@Composable
fun RecordingControls(
    isRecording: Boolean,
    duration: Long,
    amplitude: Int,
    onRecordClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isRecording) {
            AmplitudeVisualizer(amplitude = amplitude)
            Spacer(modifier = Modifier.height(24.dp))
            DurationDisplay(duration = duration)
            Spacer(modifier = Modifier.height(32.dp))
            StopIconButton(
                onClick = onStopClick,
                modifier = Modifier.size(100.dp)
            )
        } else {
            RecordIconButton(
                onClick = onRecordClick,
                modifier = Modifier.size(100.dp)
            )
        }
    }
}
