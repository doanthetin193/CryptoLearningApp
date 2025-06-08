package com.example.cryptolearningapp.data.api

import com.example.cryptolearningapp.data.model.GeminiRequest
import com.example.cryptolearningapp.data.model.GeminiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApi {
    @POST("models/gemini-2.0-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
} 