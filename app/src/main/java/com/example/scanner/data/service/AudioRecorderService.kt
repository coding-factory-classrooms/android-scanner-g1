package com.example.scanner.data.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AudioRecorderService {
    fun startRecording(context: Context): Result<Unit>
    
    fun stopRecording(): Result<String>
    
    fun isRecording(): Boolean
    
    val transcribedText: StateFlow<String>
}

class AudioRecorderServiceImpl : AudioRecorderService {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var isCurrentlyRecording = false
    private val _transcribedText = MutableStateFlow("")
    override val transcribedText: StateFlow<String> = _transcribedText.asStateFlow()
    
    // Écouteur pour les résultats de reconnaissance vocale
    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        
        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Erreur audio"
                SpeechRecognizer.ERROR_CLIENT -> "Erreur client"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissions insuffisantes"
                SpeechRecognizer.ERROR_NETWORK -> "Erreur réseau"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout réseau"
                SpeechRecognizer.ERROR_NO_MATCH -> "Aucune correspondance"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconnaissance occupée"
                SpeechRecognizer.ERROR_SERVER -> "Erreur serveur"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Timeout parole"
                else -> "Erreur inconnue"
            }
            _transcribedText.value = "Erreur: $errorMessage"
        }
        
        // Résultats finaux de la reconnaissance
        override fun onResults(results: Bundle?) {
            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { matches ->
                if (matches.isNotEmpty()) {
                    _transcribedText.value = matches.joinToString(" ")
                }
            }
        }
        
        // Résultats partiels en temps réel (pendant la parole)
        override fun onPartialResults(partialResults: Bundle?) {
            partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { matches ->
                if (matches.isNotEmpty()) {
                    _transcribedText.value = matches.joinToString(" ")
                }
            }
        }
        
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
    
    override fun startRecording(context: Context): Result<Unit> = runCatching {
        // Vérifie si une reconnaissance est déjà en cours
        if (isCurrentlyRecording) throw IllegalStateException("Une reconnaissance est déjà en cours")
        
        // Vérifie si la reconnaissance vocale est disponible sur l'appareil
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            throw IllegalStateException("La reconnaissance vocale n'est pas disponible")
        }
        
        // Crée et configure le SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(recognitionListener)
        }
        
        // Configure l'intent de reconnaissance en français par défaut
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR") // Français par défaut
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }.also { intent ->
            speechRecognizer?.startListening(intent)
        }
        
        isCurrentlyRecording = true
        _transcribedText.value = ""
    }
    
    override fun stopRecording(): Result<String> = runCatching {
        // Vérifie qu'une reconnaissance est en cours
        if (!isCurrentlyRecording) throw IllegalStateException("Aucune reconnaissance en cours")
        
        // Arrête et libère les ressources
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        isCurrentlyRecording = false
        
        // Récupère le texte final transcrit
        val finalText = _transcribedText.value
        _transcribedText.value = ""
        finalText
    }
    
    override fun isRecording() = isCurrentlyRecording
}
