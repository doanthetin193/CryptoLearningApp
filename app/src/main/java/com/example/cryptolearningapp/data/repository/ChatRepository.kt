package com.example.cryptolearningapp.data.repository

import com.example.cryptolearningapp.BuildConfig
import com.example.cryptolearningapp.data.api.GeminiApi
import com.example.cryptolearningapp.data.model.Content
import com.example.cryptolearningapp.data.model.GeminiRequest
import com.example.cryptolearningapp.data.model.GeminiResponse
import java.io.IOException
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val api: GeminiApi
) {
    suspend fun sendMessage(message: String): Result<String> {
        return try {
            val contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(
                        com.example.cryptolearningapp.data.model.Part(
                            text = "Trả lời bằng tiếng Việt, giải thích dễ hiểu cho người mới bắt đầu: $message"
                        )
                    )
                )
            )

            val request = GeminiRequest(contents = contents)
            // API key được load từ BuildConfig (local.properties)
            val apiKey = BuildConfig.GEMINI_API_KEY
            
            if (apiKey.isEmpty()) {
                return Result.failure(RuntimeException("Gemini API key không được cấu hình. Vui lòng thêm GEMINI_API_KEY vào local.properties"))
            }
            
            val response = api.generateContent(apiKey, request)

            if (response.isSuccessful) {
                response.body()?.let {
                    val answer = it.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (answer != null) {
                        Result.success(answer)
                    } else {
                        Result.failure(RuntimeException("Không nhận được câu trả lời từ Gemini."))
                    }
                } ?: Result.failure(RuntimeException("Phản hồi từ Gemini API rỗng."))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = "Lỗi HTTP ${response.code()}: ${errorBody ?: "Lỗi không rõ"}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: IOException) {
            Result.failure(IOException("Lỗi mạng: ${e.message}", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 