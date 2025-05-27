package com.example.cryptolearningapp.di

import android.content.Context
import com.example.cryptolearningapp.data.repository.UserProfileRepository
import com.example.cryptolearningapp.data.repository.UserProgressRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserProfileRepository(
        @ApplicationContext context: Context
    ): UserProfileRepository {
        return UserProfileRepository(context)
    }

    @Provides
    @Singleton
    fun provideUserProgressRepository(
        @ApplicationContext context: Context
    ): UserProgressRepository {
        return UserProgressRepository(context)
    }
} 