package com.example.scanner.data.service

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import java.io.File

interface AudioRecorderService {
    fun startRecording(context: Context, fileName: String): Result<File>
    fun stopRecording(): Result<String>
    fun isRecording(): Boolean
    fun getCurrentAmplitude(): Int
}

class AudioRecorderServiceImpl : AudioRecorderService {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isCurrentlyRecording = false

    override fun startRecording(context: Context, fileName: String): Result<File> = runCatching {
        if (isCurrentlyRecording) throw IllegalStateException("Already recording")
        val file = File(context.cacheDir, "$fileName.3gp")
        outputFile = file
        mediaRecorder = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        isCurrentlyRecording = true
        file
    }

    override fun stopRecording(): Result<String> = runCatching {
        mediaRecorder?.apply { stop(); release() }
        mediaRecorder = null
        isCurrentlyRecording = false
        val file = outputFile ?: throw IllegalStateException("No file recorded")
        val base64 = Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
        file.delete()
        outputFile = null
        base64
    }

    override fun isRecording() = isCurrentlyRecording

    override fun getCurrentAmplitude() = try {
        if (isCurrentlyRecording && mediaRecorder != null) mediaRecorder!!.maxAmplitude else 0
    } catch (_: Exception) { 0 }
}
