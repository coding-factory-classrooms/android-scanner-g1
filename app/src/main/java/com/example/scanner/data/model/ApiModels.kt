package com.example.scanner.data.model

import com.google.gson.annotations.SerializedName

// Requête API
data class TranslationRequest(
    @SerializedName("q")
    val text: String,
    @SerializedName("source")
    val source: String = "auto",
    @SerializedName("target")
    val target: String,
    @SerializedName("format")
    val format: String = "text"
)

// Réponse API
data class DetectedLanguage(
    @SerializedName("confidence")
    val confidence: Double,
    @SerializedName("language")
    val language: String
)

data class TranslationResponse(
    @SerializedName("detectedLanguage")
    val detectedLanguage: DetectedLanguage?,
    @SerializedName("translatedText")
    val translatedText: String
)

