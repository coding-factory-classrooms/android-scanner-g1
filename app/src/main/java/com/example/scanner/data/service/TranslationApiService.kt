package com.example.scanner.data.service

import com.example.scanner.data.model.TranslationRequest
import com.example.scanner.data.model.TranslationResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface TranslationApiService {
    @POST("translate")
    suspend fun translate(@Body request: TranslationRequest): TranslationResponse
}

