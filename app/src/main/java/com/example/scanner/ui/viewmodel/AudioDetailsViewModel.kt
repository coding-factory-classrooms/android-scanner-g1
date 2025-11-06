package com.example.scanner.ui.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.repository.TranslationRepository
import com.example.scanner.data.model.Translation
import kotlinx.coroutines.launch

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

    fun loadTranslation(id: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val result = translationRepository.findOne(id)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        translation = result.getOrNull(),
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Translation with id $id not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun toggleFavorite() {
        val current = _uiState.value.translation ?: return
        viewModelScope.launch {
            try {
                translationRepository.updateFavorite(current.id, !current.isFave)
                loadTranslation(current.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Unknown error")
            }
        }
    }

    fun deleteTranslation(onDeleted: () -> Unit) {
        val current = _uiState.value.translation ?: return
        viewModelScope.launch {
            try {
                translationRepository.deleteById(current.id)
                onDeleted()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Unknown error")
            }
        }
    }
}