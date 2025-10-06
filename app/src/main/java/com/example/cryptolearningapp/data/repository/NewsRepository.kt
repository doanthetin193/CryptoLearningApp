package com.example.cryptolearningapp.data.repository

import com.example.cryptolearningapp.BuildConfig
import com.example.cryptolearningapp.data.api.CryptoNewsApi
import com.example.cryptolearningapp.data.api.GeminiApi
import com.example.cryptolearningapp.data.model.Content
import com.example.cryptolearningapp.data.model.GeminiRequest
import com.example.cryptolearningapp.data.model.NewsItem
import com.example.cryptolearningapp.data.model.Part
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val api: CryptoNewsApi,
    private val geminiApi: GeminiApi
) {
    suspend fun getNews(): Result<List<NewsItem>> {
        return try {
            val apiKey = BuildConfig.NEWS_API_KEY
            
            if (apiKey.isEmpty()) {
                return Result.failure(Exception("News API key không được cấu hình. Vui lòng thêm NEWS_API_KEY vào local.properties"))
            }
            
            val response = api.getNews(apiKey)
            if (response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("Không có dữ liệu tin tức"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi kết nối: ${e.message}"))
        }
    }
    
    suspend fun generateAISummary(title: String): Result<String> {
        return try {
            val contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(
                        Part(text = "Tóm tắt tin tức crypto này trong 2-3 câu ngắn gọn, dễ hiểu cho người mới bắt đầu, trả lời bằng tiếng Việt: $title")
                    )
                )
            )

            val request = GeminiRequest(contents = contents)
            val apiKey = BuildConfig.GEMINI_API_KEY
            
            if (apiKey.isEmpty()) {
                return Result.failure(Exception("Gemini API key không được cấu hình"))
            }
            
            val response = geminiApi.generateContent(apiKey, request)

            if (response.isSuccessful) {
                response.body()?.let { geminiResponse ->
                    val summary = geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (summary != null) {
                        Result.success(summary)
                    } else {
                        Result.failure(Exception("Không nhận được tóm tắt từ AI"))
                    }
                } ?: Result.failure(Exception("Phản hồi AI rỗng"))
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Lỗi API: ${response.code()} - ${errorBody ?: "Lỗi không rõ"}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tạo tóm tắt AI: ${e.message}"))
        }
    }
} 