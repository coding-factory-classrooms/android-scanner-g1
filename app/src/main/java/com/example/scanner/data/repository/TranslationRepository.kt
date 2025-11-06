package com.example.scanner.data.repository

import android.util.Log
import com.example.scanner.data.dao.TranslationDao
import com.example.scanner.data.model.Translation
import com.example.scanner.data.model.TranslationRequest
import com.example.scanner.data.service.TranslationApiService
import kotlinx.coroutines.flow.first

interface TranslationRepository {
    suspend fun translate(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Result<String>
    
    suspend fun findOne(id: Long): Result<Translation>
    
    suspend fun getRecentTranslations(limit: Int): Result<List<Translation>>

    suspend fun deleteById(id: Long): Result<Unit>
}

class TranslationRepositoryImpl(
    private val apiService: TranslationApiService,
    private val dao: TranslationDao
) : TranslationRepository {
    
    companion object {
        private const val TAG = "TranslationRepository"
    }
    
    override suspend fun translate(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Result<String> = runCatching {
        Log.d(TAG, "=== Début appel API de traduction ===")
        Log.d(TAG, "Texte à traduire: $text")
        Log.d(TAG, "Source: $sourceLang")
        Log.d(TAG, "Target: $targetLang")
        
        val request = TranslationRequest(
            text = text,
            source = sourceLang,
            target = targetLang,
            format = "text"
        )
        
        Log.d(TAG, "Requête API: $request")
        
        val response = apiService.translate(request)
        
        Log.d(TAG, "Réponse API reçue:")
        Log.d(TAG, "  - Texte traduit: ${response.translatedText}")
        Log.d(TAG, "  - Langue détectée: ${response.detectedLanguage?.language}")
        Log.d(TAG, "  - Confiance: ${response.detectedLanguage?.confidence}")
        Log.d(TAG, "=== Fin appel API de traduction ===")
        
        response.translatedText
    }.onFailure { error ->
        Log.e(TAG, "=== Erreur lors de l'appel API ===", error)
        Log.e(TAG, "Message d'erreur: ${error.message}")
        Log.e(TAG, "Type d'erreur: ${error.javaClass.simpleName}")
        Log.e(TAG, "=== Fin erreur ===")
    }
    
    override suspend fun findOne(id: Long): Result<Translation> = runCatching {
        val translation = dao.getById(id)
        if (translation != null) {
            Result.success(translation)
        } else {
            Result.failure(Exception("Translation with id $id not found"))
        }
    }.getOrElse { error ->
        Result.failure(error)
    }
    
    override suspend fun getRecentTranslations(limit: Int): Result<List<Translation>> {
        return try {
            val translations = dao.getAll().first()
            Result.success(translations.take(limit))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteById(id: Long): Result<Unit> = runCatching {
        dao.deleteById(id)
    }
}

