package com.example.scanner.data.repository

import com.example.scanner.data.dao.TranslationDao
import com.example.scanner.data.model.Translation

interface TranslationRepository {
    suspend fun findOne(id: Long, useFakeData: Boolean = false): Result<Translation>
    // find all 
}

class TranslationRepositoryImpl(private val dao: TranslationDao): TranslationRepository {

    override suspend fun findOne(id: Long, useFakeData: Boolean): Result<Translation> {
        if (useFakeData) {
            return Result.success(
                Translation(
                    id = id,
                    isFave = false,
                    createAt = System.currentTimeMillis(),
                    inputLange = "fr",
                    outputLange = "en",
                    OriginaleText = "Bonjour, comment allez-vous?",
                    TradText = "Hello, how are you?",
                    pathAudioFile = "/fake/audio/path/translation_$id.mp3"
                )
            )
        }
        val translation = dao.getById(id)
        return if (translation != null) {
            Result.success(translation)
        } else {
            Result.failure(Exception("Translation with id $id not found"))
        }
    }

    // override pour getAll()
}



