package com.example.cryptolearningapp.data.repository

import com.example.cryptolearningapp.data.api.CryptoNewsApi
import com.example.cryptolearningapp.data.model.NewsItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val api: CryptoNewsApi
) {
    suspend fun getNews(): Result<List<NewsItem>> {
        return try {
            val response = api.getNews()
            if (response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("Không có dữ liệu tin tức"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi kết nối: ${e.message}"))
        }
    }
} 