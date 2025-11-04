package com.example.scanner.data.repository

import android.content.Context
import com.example.scanner.data.service.AudioRecorderService
import java.io.File

interface AudioRepository {
    suspend fun startRecording(context: Context, fileName: String): Result<File>
    suspend fun stopRecording(): Result<String>
    fun isRecording(): Boolean
    fun getCurrentAmplitude(): Int
}

class AudioRepositoryImpl(private val audioRecorderService: AudioRecorderService) : AudioRepository {
    override suspend fun startRecording(context: Context, fileName: String) =
        runCatching { audioRecorderService.startRecording(context, fileName).getOrThrow() }
    override suspend fun stopRecording() = runCatching { audioRecorderService.stopRecording().getOrThrow() }
    override fun isRecording() = audioRecorderService.isRecording()
    override fun getCurrentAmplitude() = audioRecorderService.getCurrentAmplitude()
}
