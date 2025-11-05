package com.example.scanner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.dao.TranslationDao
import com.example.scanner.data.model.Translation
import com.example.scanner.data.repository.AudioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val isRecording: Boolean = false,
    val transcribedText: String = "", // Texte transcrit en temps réel
    val finalTranscribedText: String? = null, // Texte final après arrêt
    val recordingDuration: Long = 0L,
    val selectedLanguage: String = "fr-FR",
    val errorMessage: String? = null,
    var isDebug: Boolean = false
)

class AudioRecorderViewModel(
    application: Application,
    private val audioRepository: AudioRepository,
    private val translationDao: TranslationDao
) : AndroidViewModel(application) {

    val durationState = MutableStateFlow(0L)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<UiEffect>(extraBufferCapacity = 1)
    private var durationJob: Job? = null
    private var transcriptionJob: Job? = null

    init {
        // Observer le texte transcrit en temps réel
        viewModelScope.launch {
            audioRepository.transcribedText.collectLatest { text ->
                _uiState.update { it.copy(transcribedText = text) }
            }
        }
    }

    fun selectLanguage(languageCode: String) {
        _uiState.update { it.copy(selectedLanguage = languageCode) }
    }

    fun activeOnDebug() {
        _uiState.update { it.copy(isDebug = true) }
        stopRecording()
    }

    fun startRecording() {
        if (_uiState.value.isDebug) {
            stopRecording()
            return
        }
        if (audioRepository.isRecording()) {
            _uiState.update { it.copy(errorMessage = "L'enregistrement est déjà en cours") }
            return
        }
        _uiState.update { 
            it.copy(
                errorMessage = null, 
                transcribedText = "",
                finalTranscribedText = null
            ) 
        }
        viewModelScope.launch {
            audioRepository.startRecording(getApplication(), _uiState.value.selectedLanguage).fold(
                onSuccess = {
                    _uiState.update { it.copy(isRecording = true) }
                    startDurationTracking()
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
        // Mode debug : texte de test
        if (_uiState.value.isDebug) {
            val debugText = "Bonjour"
            saveTranslationToDatabase(debugText)
            _uiState.update {
                it.copy(
                    isRecording = false,
                    transcribedText = "",
                    finalTranscribedText = debugText,
                    recordingDuration = 0L
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
                onSuccess = { finalText ->
                    durationJob?.cancel()
                    transcriptionJob?.cancel()
                    
                    // Sauvegarder dans la base de données
                    saveTranslationToDatabase(finalText)
                    
                    _uiState.update {
                        it.copy(
                            isRecording = false,
                            transcribedText = "",
                            finalTranscribedText = finalText,
                            recordingDuration = 0L
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

    private fun saveTranslationToDatabase(text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            val translation = Translation(
                inputLange = _uiState.value.selectedLanguage,
                outputLange = "", // Sera défini lors de la traduction
                OriginaleText = text,
                TradText = "" // Sera défini lors de la traduction
            )
            translationDao.insert(translation)
        }
    }

    private fun startDurationTracking() {
        durationJob?.cancel()
        durationJob = viewModelScope.launch {
            durationState.value = 0L
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

    fun resetState() {
        if (audioRepository.isRecording()) stopRecording()
        _uiState.value = UiState()
        emitEffect(UiEffect.StateReset)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onPermissionResult(isGranted: Boolean) {
        val currentState = _uiState.value
        if (isGranted && !currentState.isRecording && currentState.finalTranscribedText == null) {
            _uiState.update { it.copy(errorMessage = null) }
            startRecording()
        } else if (!isGranted) {
            _uiState.update { 
                it.copy(errorMessage = "Permission microphone refusée. L'enregistrement audio nécessite cette permission.") 
            }
        }
    }

    private fun emitEffect(effect: UiEffect) = viewModelScope.launch { _uiEffect.emit(effect) }

    sealed class UiEffect {
        object RecordingStarted : UiEffect()
        object RecordingStopped : UiEffect()
        object StateReset : UiEffect()
    }
}
