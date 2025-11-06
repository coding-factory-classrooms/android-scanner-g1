package com.example.scanner.ui.viewmodel

import com.example.scanner.data.model.Translation
import com.example.scanner.data.repository.TranslationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalCoroutinesApi::class)
class ListTranslationViewModelTest {

    private lateinit var translationRepository: TranslationRepository
    private lateinit var viewModel: ListTranslationViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        translationRepository = mock(TranslationRepository::class.java)
        viewModel = ListTranslationViewModel(translationRepository)
    }

    @Test
    fun `loadTranslations should update state with translations on success`() = runTest(testDispatcher) {
        val mockTranslations = listOf(
            Translation(id = 1L, originalText = "Hello", tradText = "Bonjour", inputLange = "en", outputLange = "fr")
        )
        `when`(translationRepository.getRecentTranslations(10))
            .thenReturn(Result.success(mockTranslations))

        viewModel.loadTranslations()
        advanceUntilIdle()

        assertEquals(mockTranslations, viewModel.uiState.value.translations)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `loadTranslations should handle error`() = runTest(testDispatcher) {
        `when`(translationRepository.getRecentTranslations(10))
            .thenReturn(Result.failure(Exception("Network error")))

        viewModel.loadTranslations()
        advanceUntilIdle()

        assertEquals("Erreur de chargement", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onSearchQueryChanged should filter translations`() = runTest(testDispatcher) {
        val mockTranslations = listOf(
            Translation(id = 1L, originalText = "Hello", tradText = "Bonjour", inputLange = "en", outputLange = "fr"),
            Translation(id = 2L, originalText = "World", tradText = "Monde", inputLange = "en", outputLange = "fr")
        )
        `when`(translationRepository.getRecentTranslations(10))
            .thenReturn(Result.success(mockTranslations))

        viewModel.loadTranslations()
        advanceUntilIdle()
        viewModel.onSearchQueryChanged("Hello")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.filteredTranslations.size)
        assertEquals("Hello", viewModel.uiState.value.filteredTranslations[0].originalText)
    }

    @Test
    fun `deleteTranslation should handle error`() = runTest(testDispatcher) {
        `when`(translationRepository.deleteById(1L))
            .thenReturn(Result.failure(Exception("Delete failed")))

        viewModel.deleteTranslation(1L)
        advanceUntilIdle()

        assertEquals("Suppression impossible", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `toggleFavorite should handle error`() = runTest(testDispatcher) {
        `when`(translationRepository.updateFavorite(1L, true))
            .thenReturn(Result.failure(Exception("Update failed")))

        viewModel.toggleFavorite(1L, false)
        advanceUntilIdle()

        assertEquals("Impossible de mettre à jour le favori", viewModel.uiState.value.errorMessage)
    }
}
