package com.example.cryptolearningapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cryptolearningapp.data.dao.UserDao
import com.example.cryptolearningapp.data.dao.UserProgressDao
import com.example.cryptolearningapp.data.model.User
import com.example.cryptolearningapp.data.model.UserProgress
import com.example.cryptolearningapp.data.converter.Converters

@Database(
    entities = [User::class, UserProgress::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CryptoDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userProgressDao(): UserProgressDao
} 