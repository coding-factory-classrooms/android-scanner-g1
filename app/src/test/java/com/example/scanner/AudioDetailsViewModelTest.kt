// package com.example.scanner.ui.viewmodel

// import com.example.scanner.data.model.Translation
// import com.example.scanner.data.repository.TranslationRepository
// import kotlinx.coroutines.ExperimentalCoroutinesApi
// import kotlinx.coroutines.test.StandardTestDispatcher
// import kotlinx.coroutines.test.advanceUntilIdle
// import kotlinx.coroutines.test.runTest
// import kotlinx.coroutines.test.setMain
// import org.junit.Before
// import org.junit.Test
// import org.junit.Assert.*
// import org.mockito.Mockito.*
// import kotlinx.coroutines.Dispatchers

// @OptIn(ExperimentalCoroutinesApi::class)
// class AudioDetailsViewModelTest {

//     private lateinit var translationRepository: TranslationRepository
//     private lateinit var viewModel: AudioDetailsViewModel
//     private val testDispatcher = StandardTestDispatcher()

//     @Before
//     fun setup() {
//         Dispatchers.setMain(testDispatcher)
//         translationRepository = mock(TranslationRepository::class.java)
//         viewModel = AudioDetailsViewModel(translationRepository)
//     }

//     @Test
//     fun `loadTranslation should update state with translation on success`() = runTest(testDispatcher) {
//         val mockTranslation = Translation(
//             id = 1L, originalText = "Hello", tradText = "Bonjour",
//             inputLange = "en", outputLange = "fr", isFave = false
//         )
//         `when`(translationRepository.findOne(1L)).thenReturn(Result.success(mockTranslation))

//         viewModel.loadTranslation(1L)
//         advanceUntilIdle()

//         assertEquals(mockTranslation, viewModel.uiState.value.translation)
//         assertNull(viewModel.uiState.value.error)
//     }

//     @Test
//     fun `loadTranslation should handle error`() = runTest(testDispatcher) {
//         `when`(translationRepository.findOne(1L))
//             .thenReturn(Result.failure(Exception("Translation not found")))

//         viewModel.loadTranslation(1L)
//         advanceUntilIdle()

//         assertNotNull(viewModel.uiState.value.error)
//         assertNull(viewModel.uiState.value.translation)
//     }

//     @Test
//     fun `toggleFavorite should update translation`() = runTest(testDispatcher) {
//         val mockTranslation = Translation(
//             id = 1L, originalText = "Hello", tradText = "Bonjour",
//             inputLange = "en", outputLange = "fr", isFave = false
//         )
//         `when`(translationRepository.findOne(1L)).thenReturn(Result.success(mockTranslation))
//         `when`(translationRepository.updateFavorite(1L, true)).thenReturn(Result.success(Unit))

//         viewModel.loadTranslation(1L)
//         advanceUntilIdle()
//         viewModel.toggleFavorite()
//         advanceUntilIdle()

//         verify(translationRepository).updateFavorite(1L, true)
//     }

//     @Test
//     fun `deleteTranslation should call repository and callback`() = runTest(testDispatcher) {
//         val mockTranslation = Translation(
//             id = 1L, originalText = "Hello", tradText = "Bonjour",
//             inputLange = "en", outputLange = "fr"
//         )
//         `when`(translationRepository.findOne(1L)).thenReturn(Result.success(mockTranslation))
//         `when`(translationRepository.deleteById(1L)).thenReturn(Result.success(Unit))

//         viewModel.loadTranslation(1L)
//         advanceUntilIdle()

//         var callbackCalled = false
//         viewModel.deleteTranslation { callbackCalled = true }
//         advanceUntilIdle()

//         verify(translationRepository).deleteById(1L)
//         assertTrue(callbackCalled)
//     }
// }
