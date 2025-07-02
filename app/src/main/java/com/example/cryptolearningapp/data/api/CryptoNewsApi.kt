package com.example.cryptolearningapp.data.api

import com.example.cryptolearningapp.data.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface CryptoNewsApi {
    @GET("news")
    suspend fun getNews(
        @Header("X-API-KEY") apiKey: String,
        @Header("accept") accept: String = "application/json"
    ): NewsResponse
} 