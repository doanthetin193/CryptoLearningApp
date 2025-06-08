package com.example.cryptolearningapp.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"
    const val API_KEY = "AIzaSyDbrQFONxMSK0hJ7a2gTuF4xC6vaUHnuLc"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: GeminiApi by lazy {
        retrofit.create(GeminiApi::class.java)
    }
} 