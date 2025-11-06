package com.example.scanner.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.scanner.ui.components.atoms.ScreenTitle
import com.example.scanner.ui.components.molecules.LanguageSelector
import com.example.scanner.ui.components.organisms.RecordingControls
import com.example.scanner.ui.components.organisms.RecordingStatus
import com.example.scanner.ui.components.organisms.RecordingStatusState
import com.example.scanner.ui.viewmodel.AudioRecorderViewModel
import com.example.scanner.ui.viewmodel.ScreenState
import org.koin.androidx.compose.koinViewModel

@Composable
fun AudioRecorderScreen(
    viewModel: AudioRecorderViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val duration by viewModel.durationState.collectAsState()
    val context = LocalContext.current
    val hasPermission = ContextCompat.checkSelfPermission(
        context, 
        Manifest.permission.RECORD_AUDIO
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { viewModel.onPermissionResult(it) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ScreenTitle(text = "Reconnaissance Vocale", modifier = Modifier.padding(bottom = 16.dp))
        
        LanguageSelector(
            selectedLanguage = uiState.targetLanguage,
            onLanguageSelected = viewModel::selectTargetLanguage,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        uiState.errorMessage?.let { error ->
            RecordingStatus(
                status = RecordingStatusState.Error(error),
                onReset = { viewModel.clearError(); viewModel.resetState() }
            )
            Spacer(Modifier.height(16.dp))
        }

        when (uiState.screenState) {
            ScreenState.IDLE -> {
                RecordingStatus(status = RecordingStatusState.Idle, onReset = {})
                Spacer(Modifier.height(32.dp))
                RecordingControls(
                    isRecording = false,
                    duration = duration,
                    transcribedText = "",
                    onRecordClick = {
                        if (hasPermission) viewModel.startRecording()
                        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onStopClick = {},
                    onDebugClick = viewModel::onDebugClick
                )
            }
            ScreenState.RECORDING -> {
                RecordingControls(
                    isRecording = true,
                    duration = uiState.recordingDuration,
                    transcribedText = uiState.transcribedText,
                    onRecordClick = {},
                    onStopClick = viewModel::stopRecording,
                    onDebugClick = {}
                )
            }
            ScreenState.TRANSCRIBED -> {
                uiState.finalTranscribedText?.let { text ->
                    RecordingStatus(
                        status = RecordingStatusState.Recorded(text),
                        onReset = viewModel::resetState
                    )
                }
            }
        }
    }
}
