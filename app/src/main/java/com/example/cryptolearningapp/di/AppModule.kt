package com.example.cryptolearningapp.di

import android.content.Context
import com.example.cryptolearningapp.data.api.GeminiApi
import com.example.cryptolearningapp.data.local.PreferenceManager
import com.example.cryptolearningapp.data.repository.ChatRepository
import com.example.cryptolearningapp.data.repository.CryptoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferenceManager(
        @ApplicationContext context: Context
    ): PreferenceManager {
        return PreferenceManager(context)
    }

    @Provides
    @Singleton
    fun provideCryptoRepository(
        preferenceManager: PreferenceManager
    ): CryptoRepository {
        return CryptoRepository(preferenceManager)
    }

    @Provides
    @Singleton
    fun provideGeminiApi(): GeminiApi {
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1beta/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        api: GeminiApi
    ): ChatRepository {
        return ChatRepository(api)
    }
} 