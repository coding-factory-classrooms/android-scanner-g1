package com.example.scanner.data.repository

import com.example.scanner.data.dao.TranslationDao
import com.example.scanner.data.model.Translation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface TranslationRepository{
    suspend fun getRecentTranslations(limit: Int): Result<List<Translation>>
}

class TranslationRepositoryImpl(
    private val translationDao: TranslationDao
) : TranslationRepository {

    //ici je crois qu'il faut injecter la methode getAll() de mon dao afin de récup les donne et
    // remplacet ma donne en brut
    //private val mockTranslations = listOf(Translation(id = 1,isFave = false, createAt = System.currentTimeMillis(), inputLange = "en-US", outputLange = "fr-FR", OriginaleText = "Bonjour", TradText = "Hello", pathAudioFile = ""))
    override suspend fun getRecentTranslations(limit: Int): Result<List<Translation>> {
        return try {
            val translation = translationDao.getAll().first()
            Result.success(translation.take(limit))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}