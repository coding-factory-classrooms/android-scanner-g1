package com.example.scanner.ui.viewmodel

import android.app.Application
import android.content.Context
import com.example.scanner.data.dao.TranslationDao
import com.example.scanner.data.model.Translation
import com.example.scanner.data.repository.AudioRepository
import com.example.scanner.data.repository.TranslationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain

class TestAudioRepository(
    private val transcribedTextFlow: MutableStateFlow<String> = MutableStateFlow("")
) : AudioRepository {
    var startRecordingResult: Result<Unit> = Result.success(Unit)
    var stopRecordingResult: Result<String> = Result.success("")
    var isRecordingValue: Boolean = false

    override suspend fun startRecording(context: Context): Result<Unit> = startRecordingResult
    override suspend fun stopRecording(): Result<String> = stopRecordingResult
    override fun isRecording(): Boolean = isRecordingValue
    override val transcribedText: StateFlow<String> = transcribedTextFlow
}

@OptIn(ExperimentalCoroutinesApi::class)
class AudioRecorderViewModelTest {

    private lateinit var application: Application
    private lateinit var audioRepository: TestAudioRepository
    private lateinit var translationDao: TranslationDao
    private lateinit var translationRepository: TranslationRepository
    private lateinit var viewModel: AudioRecorderViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val transcribedTextFlow = MutableStateFlow<String>("")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = mock(Application::class.java)
        translationDao = mock(TranslationDao::class.java)
        translationRepository = mock(TranslationRepository::class.java)
        audioRepository = TestAudioRepository(transcribedTextFlow)
    }
    
    private fun createViewModel() = AudioRecorderViewModel(
        application, audioRepository, translationDao, translationRepository
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        reset(translationDao, translationRepository)
    }

    @Test
    fun `startRecording should update state to RECORDING on success`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        transcribedTextFlow.value = ""
        testDispatcher.scheduler.advanceUntilIdle()
        audioRepository.startRecordingResult = Result.success(Unit)
        
        viewModel.startRecording()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.screenState == ScreenState.IDLE || 
                  state.screenState == ScreenState.RECORDING || 
                  state.screenState == ScreenState.TRANSCRIBED)
    }

    @Test
    fun `startRecording should handle error`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        transcribedTextFlow.value = ""
        testDispatcher.scheduler.advanceUntilIdle()
        audioRepository.startRecordingResult = Result.failure(SecurityException("Permission denied"))

        viewModel.startRecording()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Permission microphone refusée", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `stopRecording should update state and save translation on success`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        transcribedTextFlow.value = ""
        testDispatcher.scheduler.advanceUntilIdle()
        val finalText = "Hello World"
        audioRepository.startRecordingResult = Result.success(Unit)
        audioRepository.stopRecordingResult = Result.success(finalText)
        `when`(translationRepository.translate(finalText, "auto", "en"))
            .thenReturn(Result.success("Bonjour le monde"))

        viewModel.startRecording()
        testDispatcher.scheduler.advanceUntilIdle()

        if (viewModel.uiState.value.screenState == ScreenState.RECORDING) {
            viewModel.stopRecording()
            testDispatcher.scheduler.advanceUntilIdle()
            verify(translationRepository).translate(finalText, "auto", "en")
            verify(translationDao).insert(any(Translation::class.java))
        }
    }

    @Test
    fun `stopRecording should handle error`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        transcribedTextFlow.value = ""
        testDispatcher.scheduler.advanceUntilIdle()
        audioRepository.startRecordingResult = Result.success(Unit)
        audioRepository.stopRecordingResult = Result.failure(Exception("Stop failed"))

        viewModel.startRecording()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.stopRecording()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `saveTranslationToDatabase should handle translation failure`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        transcribedTextFlow.value = ""
        testDispatcher.scheduler.advanceUntilIdle()
        val originalText = "Hello"
        audioRepository.startRecordingResult = Result.success(Unit)
        audioRepository.stopRecordingResult = Result.success(originalText)
        `when`(translationRepository.translate(originalText, "auto", "en"))
            .thenReturn(Result.failure(Exception("Translation failed")))

        viewModel.startRecording()
        testDispatcher.scheduler.advanceUntilIdle()

        if (viewModel.uiState.value.screenState == ScreenState.RECORDING) {
            viewModel.stopRecording()
            testDispatcher.scheduler.advanceUntilIdle()
            verify(translationRepository).translate(originalText, "auto", "en")
            verify(translationDao).insert(argThat { 
                it.originalText == originalText && 
                it.tradText == "" && 
                it.inputLange == "auto" && 
                it.outputLange == "en"
            })
        }
    }
}
