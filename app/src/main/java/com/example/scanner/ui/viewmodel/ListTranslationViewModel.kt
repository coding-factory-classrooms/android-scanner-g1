package com.example.scanner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.model.Translation
import com.example.scanner.data.repository.TranslationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class ListTranslationViewModel (
    private val translationRepository: TranslationRepository
): ViewModel(){
    data class UiState(
        val translations: List<Translation> = emptyList(),
        val filteredTranslations: List<Translation> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val searchQuery: String = "" // j'ai ajoute liste de recherche
    )
    //Seul le ViewModel modifie
    private val _uiState = MutableStateFlow(UiState())
    //La View observe seulement
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadTranslations() {
        viewModelScope.launch {
            // 1. Loading = true
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 2. Appeler le repository
            val result = translationRepository.getRecentTranslations(10)

            // 3. Traiter result
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                translations = result.getOrNull() ?: emptyList(),
                isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                errorMessage = "Erreur de chargement",
                isLoading = false
                )
            }
        }
    }


    fun onSearchQueryChanged(query: String) {
        _uiState.update { currentState ->
            val filtered = if (query.isEmpty()) {
                currentState.translations  // Toutes les traductions
            } else {
                currentState.translations.filter {
                    translation ->
                    //logique de filtra// // Retourne true si la query est dans l'un OU l'autre
                    translation.originalText.contains(query, ignoreCase = true) ||
                    translation.tradText.contains(query, ignoreCase = true)
                }
            }

            currentState.copy(
                searchQuery = query,
                filteredTranslations = filtered
            )
        }
    }

    fun deleteTranslation(id: Long) {
        viewModelScope.launch {
            val result = translationRepository.deleteById(id)
            if (result.isSuccess) {
                loadTranslations()
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Suppression impossible")
            }
        }
    }

    fun toggleFavorite(id: Long, currentIsFave: Boolean) {
        viewModelScope.launch {
            val result = translationRepository.updateFavorite(id, !currentIsFave)
            if (result.isSuccess) {
                loadTranslations()
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Impossible de mettre à jour le favori")
            }
        }
    }
}
