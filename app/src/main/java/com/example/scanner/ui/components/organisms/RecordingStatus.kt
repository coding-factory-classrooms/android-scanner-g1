package com.example.scanner.ui.components.organisms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.scanner.ui.components.atoms.ErrorText
import com.example.scanner.ui.components.atoms.StatusText
import com.example.scanner.ui.components.molecules.TranscribedTextCard

@Composable
fun RecordingStatus(
    status: RecordingStatusState,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (status) {
        is RecordingStatusState.Idle -> StatusText("Prêt à transcrire", modifier)
        is RecordingStatusState.Recorded -> Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TranscribedTextCard(status.text)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onReset) { Text("Nouvelle transcription") }
        }
        is RecordingStatusState.Error -> Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ErrorText(status.message, Modifier.padding(bottom = 16.dp))
            Button(onClick = onReset) { Text("Réessayer") }
        }
    }
}

sealed class RecordingStatusState {
    object Idle : RecordingStatusState()
    data class Recorded(val text: String) : RecordingStatusState()
    data class Error(val message: String) : RecordingStatusState()
}
