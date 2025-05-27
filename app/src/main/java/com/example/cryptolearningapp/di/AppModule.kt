package com.example.cryptolearningapp.di

import android.content.Context
import androidx.room.Room
import com.example.cryptolearningapp.data.database.CryptoDatabase
import com.example.cryptolearningapp.data.dao.UserDao
import com.example.cryptolearningapp.data.dao.UserProgressDao
import com.example.cryptolearningapp.data.repository.CryptoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCryptoDatabase(
        @ApplicationContext context: Context
    ): CryptoDatabase {
        return Room.databaseBuilder(
            context,
            CryptoDatabase::class.java,
            "crypto_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: CryptoDatabase) = database.userDao()

    @Provides
    @Singleton
    fun provideUserProgressDao(database: CryptoDatabase) = database.userProgressDao()

    @Provides
    @Singleton
    fun provideCryptoRepository(
        userDao: UserDao,
        userProgressDao: UserProgressDao
    ): CryptoRepository {
        return CryptoRepository(userDao, userProgressDao)
    }
} 