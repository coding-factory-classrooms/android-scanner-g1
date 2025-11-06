package com.example.scanner.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.scanner.data.dao.TranslationDao
import com.example.scanner.data.model.Translation

@Database(
    entities = [Translation::class],
    version = 2,
    exportSchema = false
)
abstract class AppDb : RoomDatabase() {
    abstract fun translationDao(): TranslationDao
    
    companion object {
        private const val DATABASE_NAME = "app.db"
        
        fun create(context: Context): AppDb {
            return Room.databaseBuilder(
                context,
                AppDb::class.java,
                DATABASE_NAME
            ).build()
        }
        
        fun getDatabasePath(context: Context): String {
            return context.getDatabasePath(DATABASE_NAME).absolutePath
        }
    }
}
