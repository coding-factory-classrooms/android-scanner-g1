package com.example.scanner.data.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.scanner.data.dao.TranslationDao
import com.example.scanner.data.model.Translation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface DatabaseFixtureService {
    suspend fun seedDatabaseIfNeeded()
    suspend fun clearDatabase()
    suspend fun resetAndReseed()
}

class DatabaseFixtureServiceImpl(
    private val translationDao: TranslationDao,
    private val context: Context
) : DatabaseFixtureService {

    private val prefs: SharedPreferences = 
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    
    private val FIXTURES_INSERTED_KEY = "fixtures_inserted"
    private val RESET_DB_KEY = "reset_database"
    private val TAG = "DatabaseFixtureService"

    override suspend fun seedDatabaseIfNeeded() {
        // Vérifier si on doit supprimer la DB
        val shouldReset = prefs.getBoolean(RESET_DB_KEY, false)
        if (shouldReset) {
            Log.d(TAG, "Reset flag detected, clearing database...")
            clearDatabase()
            prefs.edit().putBoolean(RESET_DB_KEY, false).apply()
        }
        
        // Réinitialiser à chaque lancement
        Log.d(TAG, "Resetting database and reseeding fixtures at each launch...")
        resetAndReseed()
    }

    private suspend fun insertFixtures() {
        val fixtures = listOf(
            Translation(
                id = 0,
                isFave = true,
                createAt = System.currentTimeMillis() - 86400000, // Il y a 1 jour
                inputLange = "fr-FR",
                outputLange = "en-US",
                originalText = "Bonjour, comment allez-vous ?",
                tradText = "Hello, how are you?"
            ),
            Translation(
                id = 0,
                isFave = false,
                createAt = System.currentTimeMillis() - 172800000, // Il y a 2 jours
                inputLange = "en-US",
                outputLange = "fr-FR",
                originalText = "Good morning, have a nice day!",
                tradText = "Bonjour, passez une bonne journée !"
            ),
            Translation(
                id = 0,
                isFave = true,
                createAt = System.currentTimeMillis() - 259200000, // Il y a 3 jours
                inputLange = "fr-FR",
                outputLange = "es-ES",
                originalText = "Merci beaucoup pour votre aide",
                tradText = "Muchas gracias por tu ayuda"
            ),
            Translation(
                id = 0,
                isFave = false,
                createAt = System.currentTimeMillis() - 345600000, // Il y a 4 jours
                inputLange = "de-DE",
                outputLange = "fr-FR",
                originalText = "Guten Tag, wie geht es Ihnen?",
                tradText = "Bonjour, comment allez-vous ?"
            ),
            Translation(
                id = 0,
                isFave = false,
                createAt = System.currentTimeMillis() - 432000000, // Il y a 5 jours
                inputLange = "it-IT",
                outputLange = "fr-FR",
                originalText = "Ciao, come stai?",
                tradText = "Salut, comment vas-tu ?"
            )
        )

        fixtures.forEachIndexed { index, translation ->
            val insertedId = translationDao.insert(translation)
            Log.d(TAG, "Fixture ${index + 1}/${fixtures.size} inserted with id: $insertedId")
        }
        
        Log.d(TAG, "Total fixtures inserted: ${fixtures.size}")
    }
    
    override suspend fun clearDatabase() {
        val countBefore = translationDao.getAll().first().size
        translationDao.deleteAll()
        prefs.edit().putBoolean(FIXTURES_INSERTED_KEY, false).apply()
        Log.d(TAG, "Database cleared: $countBefore translations deleted")
    }
    
    override suspend fun resetAndReseed() {
        Log.d(TAG, "Resetting database and reseeding fixtures...")
        clearDatabase()
        insertFixtures()
        prefs.edit().putBoolean(FIXTURES_INSERTED_KEY, true).apply()
        Log.d(TAG, "OK - Database reset and fixtures reinserted successfully")
    }
}

