package com.example.scanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation")
data class Translation(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val isFave: Boolean = false,
    val createAt: Long = System.currentTimeMillis(),
    val inputLange: String,
    val outputLange: String,
    val OriginaleText: String,
    val TradText: String
)
