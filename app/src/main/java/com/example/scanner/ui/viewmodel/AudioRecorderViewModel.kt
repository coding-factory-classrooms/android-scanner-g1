package com.example.scanner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.repository.AudioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UiState(
    val isRecording: Boolean = false,
    val recordedBase64: String? = null,
    val amplitude: Int = 0,
    val recordingDuration: Long = 0L,
    val selectedLanguage: String = "fr-FR",
    val errorMessage: String? = null,
    var isDebug: Boolean = false
)

class AudioRecorderViewModel(
    application: Application,
    private val audioRepository: AudioRepository
) : AndroidViewModel(application) {

    val durationState = MutableStateFlow(0L)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<UiEffect>(extraBufferCapacity = 1)
    private var recordingFile: File? = null
    private var durationJob: Job? = null
    private var amplitudeJob: Job? = null

    fun selectLanguage(languageCode: String) {
        _uiState.update { it.copy(selectedLanguage = languageCode) }
    }

    fun activeOnDebug(){
        // Fonction qui appel change la valeur de isDebug + lance stop recording pour avoir un fake donnée
        _uiState.value.isDebug = true
        stopRecording()
    }

    fun startRecording() {
        // vérifie if Debug == True
        if (_uiState.value.isDebug) {
            stopRecording()
            return
        }
        if (audioRepository.isRecording()) {
            _uiState.update { it.copy(errorMessage = "L'enregistrement est déjà en cours") }
            return
        }
        _uiState.update { it.copy(errorMessage = null, recordedBase64 = null) }
        val fileName = "recording_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}"
        viewModelScope.launch {
            audioRepository.startRecording(getApplication(), fileName).fold(
                onSuccess = { file ->
                    recordingFile = file
                    _uiState.update { it.copy(isRecording = true) }
                    startDurationTracking()
                    startAmplitudeTracking()
                    emitEffect(UiEffect.RecordingStarted)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            errorMessage = when (error) {
                                is IllegalStateException -> "Impossible de démarrer l'enregistrement. Vérifiez que le microphone est disponible."
                                is SecurityException -> "Permission microphone refusée. Veuillez autoriser l'accès au microphone."
                                else -> error.message ?: "Erreur lors du démarrage de l'enregistrement"
                            },
                            isRecording = false
                        )
                    }
                }
            )
        }
    }

    fun stopRecording() {
        // vérifie if Debug == True et retourne directement un base64 de test
        if (_uiState.value.isDebug) {
            _uiState.update {
                it.copy(
                    isRecording = false,
                    recordedBase64 = "Qm9uam91cg==",
                    recordingDuration = 0L,
                    amplitude = 0
                )
            }
            emitEffect(UiEffect.RecordingStopped)
            return
        }
        if (!audioRepository.isRecording()) {
            _uiState.update { it.copy(errorMessage = "Aucun enregistrement en cours") }
            return
        }
        viewModelScope.launch {
            audioRepository.stopRecording().fold(
                onSuccess = { base64 ->
                    durationJob?.cancel()
                    amplitudeJob?.cancel()
                    recordingFile?.delete()
                    recordingFile = null
                    _uiState.update {
                        it.copy(
                            isRecording = false,
                            recordedBase64 = base64,
                            recordingDuration = 0L,
                            amplitude = 0
                        )
                    }
                    emitEffect(UiEffect.RecordingStopped)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            errorMessage = "Erreur lors de l'arrêt de l'enregistrement: ${error.message}",
                            isRecording = false
                        )
                    }
                }
            )
        }
    }

    private fun startDurationTracking() {
        durationJob?.cancel()
        durationJob = viewModelScope.launch {
            while (audioRepository.isRecording() && durationState.value < 60000) {
                delay(1000)
                durationState.value += 1000
                _uiState.update { it.copy(recordingDuration = durationState.value) }
            }
            if (durationState.value >= 60000) {
                stopRecording()
            }
        }
    }

    private fun startAmplitudeTracking() {
        amplitudeJob?.cancel()
        amplitudeJob = viewModelScope.launch {
            while (audioRepository.isRecording()) {
                delay(50)
                _uiState.update { it.copy(amplitude = audioRepository.getCurrentAmplitude()) }
            }
        }
    }

    fun resetState() {
        if (audioRepository.isRecording()) stopRecording()
        recordingFile?.delete()
        recordingFile = null
        _uiState.value = UiState()
        emitEffect(UiEffect.StateReset)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onPermissionResult(isGranted: Boolean) {
        val currentState = _uiState.value
        if (isGranted && !currentState.isRecording && currentState.recordedBase64 == null) {
            _uiState.update { it.copy(errorMessage = null) }
            startRecording()
        } else if (!isGranted) {
            _uiState.update { it.copy(errorMessage = "Permission microphone refusée. L'enregistrement audio nécessite cette permission.") }
        }
    }

    private fun emitEffect(effect: UiEffect) = viewModelScope.launch { _uiEffect.emit(effect) }

    sealed class UiEffect {
        object RecordingStarted : UiEffect()
        object RecordingStopped : UiEffect()
        object StateReset : UiEffect()
    }
}
