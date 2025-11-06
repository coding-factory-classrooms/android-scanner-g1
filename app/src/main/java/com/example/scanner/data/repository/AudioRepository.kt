package com.example.scanner.data.repository

import android.content.Context
import com.example.scanner.data.service.AudioRecorderService
import kotlinx.coroutines.flow.StateFlow

interface AudioRepository {
    suspend fun startRecording(context: Context): Result<Unit>
    suspend fun stopRecording(): Result<String>
    fun isRecording(): Boolean
    val transcribedText: StateFlow<String>
}

class AudioRepositoryImpl(private val audioRecorderService: AudioRecorderService) : AudioRepository {
    override suspend fun startRecording(context: Context) =
        runCatching { audioRecorderService.startRecording(context).getOrThrow() }
    
    override suspend fun stopRecording() = 
        runCatching { audioRecorderService.stopRecording().getOrThrow() }
    
    override fun isRecording() = audioRecorderService.isRecording()
    
    override val transcribedText: StateFlow<String> = audioRecorderService.transcribedText
}
