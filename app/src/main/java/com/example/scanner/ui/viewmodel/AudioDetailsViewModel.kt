package com.example.scanner.ui.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.dao.TranslationDao
import com.example.scanner.data.model.Translation
import kotlinx.coroutines.launch

data class AudioDetailsUiState(
    val translation: Translation? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AudioDetailsViewModel(
    private val translationDao: TranslationDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioDetailsUiState())
    val uiState: StateFlow<AudioDetailsUiState> = _uiState

    fun loadTranslation(id: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val translation = translationDao.getById(id)
                if (translation != null) {
                    _uiState.value = _uiState.value.copy(
                        translation = translation,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Translation with id $id not found"
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
}