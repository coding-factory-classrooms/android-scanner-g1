package com.example.scanner.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.dao.TranslationDao
import com.example.scanner.data.model.Translation
import com.example.scanner.data.repository.AudioRepository
import com.example.scanner.data.repository.TranslationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScreenState {
    IDLE,
    RECORDING,
    TRANSCRIBED
}

data class UiState(
    val screenState: ScreenState = ScreenState.IDLE,
    val transcribedText: String = "",
    val finalTranscribedText: String? = null,
    val recordingDuration: Long = 0L,
    val targetLanguage: String = "en",
    val errorMessage: String? = null
)

class AudioRecorderViewModel(
    application: Application,
    private val audioRepository: AudioRepository,
    private val translationDao: TranslationDao,
    private val translationRepository: TranslationRepository
) : AndroidViewModel(application) {

    val durationState = MutableStateFlow(0L)
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    private var durationJob: Job? = null

    init {
        viewModelScope.launch {
            audioRepository.transcribedText.collectLatest { text ->
                _uiState.update { it.copy(transcribedText = text) }
            }
        }
    }

    fun selectTargetLanguage(languageCode: String) {
        _uiState.update { it.copy(targetLanguage = languageCode) }
    }

    fun onDebugClick() {
        val debugText = "Bonjour"
        saveTranslationToDatabase(debugText)
        _uiState.update {
            it.copy(
                screenState = ScreenState.TRANSCRIBED,
                transcribedText = "",
                finalTranscribedText = debugText,
                recordingDuration = 0L
            )
        }
    }

    fun startRecording() {
        if (_uiState.value.screenState == ScreenState.RECORDING) {
            _uiState.update { it.copy(errorMessage = "L'enregistrement est déjà en cours") }
            return
        }

        _uiState.update { 
            it.copy(
                screenState = ScreenState.IDLE,
                errorMessage = null,
                transcribedText = "",
                finalTranscribedText = null
            ) 
        }

        viewModelScope.launch {
            audioRepository.startRecording(getApplication()).fold(
                onSuccess = {
                    _uiState.update { it.copy(screenState = ScreenState.RECORDING) }
                    startDurationTracking()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            screenState = ScreenState.IDLE,
                            errorMessage = when (error) {
                                is IllegalStateException -> "Impossible de démarrer l'enregistrement"
                                is SecurityException -> "Permission microphone refusée"
                                else -> error.message ?: "Erreur lors du démarrage"
                            }
                        )
                    }
                }
            )
        }
    }

    fun stopRecording() {
        if (_uiState.value.screenState != ScreenState.RECORDING) return

        viewModelScope.launch {
            audioRepository.stopRecording().fold(
                onSuccess = { finalText ->
                    durationJob?.cancel()
                    saveTranslationToDatabase(finalText)
                    _uiState.update {
                        it.copy(
                            screenState = ScreenState.TRANSCRIBED,
                            transcribedText = "",
                            finalTranscribedText = finalText,
                            recordingDuration = 0L
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            screenState = ScreenState.IDLE,
                            errorMessage = "Erreur: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    private fun saveTranslationToDatabase(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            val sourceLang = "auto"
            val targetLang = _uiState.value.targetLanguage

            Log.d("AudioRecorderViewModel", "Appel API: source=$sourceLang, target=$targetLang")

            translationRepository.translate(text, sourceLang, targetLang).fold(
                onSuccess = { translatedText ->
                    translationDao.insert(
                        Translation(
                            inputLange = sourceLang,
                            outputLange = targetLang,
                            originalText = text,
                            tradText = translatedText
                        )
                    )
                    Log.d("AudioRecorderViewModel", "✓ Traduction sauvegardée")
                },
                onFailure = { error ->
                    translationDao.insert(
                        Translation(
                            inputLange = sourceLang,
                            outputLange = targetLang,
                            originalText = text,
                            tradText = ""
                        )
                    )
                    _uiState.update { 
                        it.copy(errorMessage = "Erreur de traduction: ${error.message}") 
                    }
                    Log.e("AudioRecorderViewModel", "✗ Erreur traduction", error)
                }
            )
        }
    }

    private fun startDurationTracking() {
        durationJob?.cancel()
        durationJob = viewModelScope.launch {
            durationState.value = 0L
            while (_uiState.value.screenState == ScreenState.RECORDING && durationState.value < 60000) {
                delay(1000)
                durationState.value += 1000
                _uiState.update { it.copy(recordingDuration = durationState.value) }
            }
            if (durationState.value >= 60000) stopRecording()
        }
    }

    fun resetState() {
        if (_uiState.value.screenState == ScreenState.RECORDING) stopRecording()
        _uiState.value = UiState()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted && _uiState.value.screenState == ScreenState.IDLE) {
            _uiState.update { it.copy(errorMessage = null) }
            startRecording()
        } else if (!isGranted) {
            _uiState.update { 
                it.copy(errorMessage = "Permission microphone refusée") 
            }
        }
    }
}
