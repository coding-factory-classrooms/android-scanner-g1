package com.example.scanner.ui.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.model.Translation
import com.example.scanner.data.repository.TranslationRepository
import kotlinx.coroutines.launch



// appler mon rep 
// chercher le path pour recuperer le sons
// 3pg 
// player 3pg 

data class AudioDetailsUiState(
    val translation: Translation? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AudioDetailsViewModel(
    private val translationRepository: TranslationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioDetailsUiState())
    val uiState: StateFlow<AudioDetailsUiState> = _uiState


    fun loadTranslation(id: Long, useFakeData: Boolean = true) {
        viewModelScope.launch {
            try {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = translationRepository.findOne(id, useFakeData)
                result.onSuccess { data ->
                    _uiState.value = _uiState.value.copy(
                        translation = data,
                        isLoading = false,
                        error = null
                    )
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    // Methode pour envoyer la bonne fle au player
    fun getAudioPath(): String {
        return _uiState.value.translation?.pathAudioFile ?: ""
    }

}