package com.example.cryptolearningapp.di

import com.example.cryptolearningapp.data.api.GeminiApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGeminiApi(okHttpClient: OkHttpClient, gsonConverterFactory: GsonConverterFactory): GeminiApi {
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1beta/")
            .client(okHttpClient) 
            .addConverterFactory(gsonConverterFactory) // Sử dụng lại GsonConverterFactory từ NetworkModule
            .build()
            .create(GeminiApi::class.java)
    }
} 