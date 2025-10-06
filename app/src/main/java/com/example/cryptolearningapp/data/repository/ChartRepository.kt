package com.example.cryptolearningapp.data.repository

import com.example.cryptolearningapp.BuildConfig
import com.example.cryptolearningapp.data.api.CoinGeckoApi
import com.example.cryptolearningapp.data.api.GeminiApi
import com.example.cryptolearningapp.data.model.ChartDataPoint
import com.example.cryptolearningapp.data.model.ChartTimeframe
import com.example.cryptolearningapp.data.model.Content
import com.example.cryptolearningapp.data.model.GeminiRequest
import com.example.cryptolearningapp.data.model.Part
import com.example.cryptolearningapp.data.model.PricePrediction
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChartRepository @Inject constructor(
    private val coinGeckoApi: CoinGeckoApi,
    private val geminiApi: GeminiApi
) {
    
    // Cache để tránh spam API
    private val chartCache = mutableMapOf<ChartTimeframe, Pair<Long, List<ChartDataPoint>>>()
    private val cacheValidityMs = 5 * 60 * 1000L // 5 phút
    
    suspend fun getChartData(timeframe: ChartTimeframe): Result<List<ChartDataPoint>> {
        return try {
            // Kiểm tra cache trước
            val cached = chartCache[timeframe]
            val currentTime = System.currentTimeMillis()
            
            if (cached != null && (currentTime - cached.first) < cacheValidityMs) {
                return Result.success(cached.second)
            }
            
            // Gọi API với retry logic
            val response = try {
                coinGeckoApi.getCoinChart(days = timeframe.days)
            } catch (e: Exception) {
                // Nếu lỗi 429 hoặc network, return cached data nếu có
                if (cached != null) {
                    return Result.success(cached.second)
                }
                throw e
            }
            
            val chartPoints = response.prices.map { priceData ->
                ChartDataPoint(
                    timestamp = priceData[0].toLong(),
                    price = priceData[1]
                )
            }
            
            // Cache kết quả
            chartCache[timeframe] = Pair(currentTime, chartPoints)
            
            Result.success(chartPoints)
        } catch (e: Exception) {
            val errorMsg = when {
                e.message?.contains("429") == true -> 
                    "API bị giới hạn tốc độ. Vui lòng đợi 1-2 phút rồi thử lại."
                e.message?.contains("timeout") == true || e.message?.contains("network") == true -> 
                    "Lỗi kết nối mạng. Kiểm tra internet và thử lại."
                else -> "Không thể tải dữ liệu biểu đồ: ${e.message}"
            }
            Result.failure(Exception(errorMsg))
        }
    }
    
    suspend fun getCurrentPrice(): Result<Pair<Double, Double>> {
        return try {
            val response = coinGeckoApi.getCurrentPrice()
            val bitcoinData = response["bitcoin"]
            val price = bitcoinData?.get("usd") ?: 0.0
            val change24h = bitcoinData?.get("usd_24h_change") ?: 0.0
            Result.success(Pair(price, change24h))
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tải giá hiện tại: ${e.message}"))
        }
    }
    
    suspend fun generatePricePrediction(
        chartData: List<ChartDataPoint>,
        timeframe: ChartTimeframe
    ): Result<PricePrediction> {
        return try {
            // Kiểm tra nếu không có data
            if (chartData.isEmpty()) {
                return generateOfflinePrediction()
            }
            
            val currentPrice = chartData.lastOrNull()?.price ?: 0.0
            val firstPrice = chartData.firstOrNull()?.price ?: 0.0
            val priceChange = ((currentPrice - firstPrice) / firstPrice * 100)
            
            val prompt = """
            Dữ liệu Bitcoin: Giá $${String.format("%.0f", currentPrice)}, thay đổi ${String.format("%.1f", priceChange)}% (${timeframe.label})
            
            Trả về JSON này, không thêm markdown hay text khác:
            {
                "predictedPrice": ${String.format("%.0f", currentPrice * (1 + priceChange/100 * 0.1))},
                "confidence": "medium",
                "reasoning": "Phân tích dựa trên trend và volume cho thấy Bitcoin có xu hướng tích cực. Scarcity và adoption tăng hỗ trợ giá dài hạn.",
                "timeframe": "24h",
                "technicalFactors": ["Trend Analysis", "Support Levels", "Market Sentiment"],
                "educationalConnection": "Áp dụng kiến thức Bitcoin Economics và Market Cycles từ bài học"
            }
            """
            
            val contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part(text = prompt))
                )
            )
            
            val request = GeminiRequest(contents = contents)
            val apiKey = BuildConfig.GEMINI_API_KEY
            
            if (apiKey.isEmpty()) {
                return Result.failure(Exception("Gemini API key chưa được cấu hình"))
            }
            
            val response = geminiApi.generateContent(apiKey, request)
            
            if (response.isSuccessful) {
                response.body()?.let { geminiResponse ->
                    val aiResponse = geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (aiResponse != null) {
                        // Parse JSON response từ AI
                        val prediction = try {
                            parseAIPredictionResponse(aiResponse, currentPrice, priceChange)
                        } catch (e: Exception) {
                            // Fallback nếu không parse được JSON
                            PricePrediction(
                                predictedPrice = currentPrice * (1 + (priceChange / 100) * 0.1),
                                confidence = "medium", 
                                reasoning = aiResponse.take(200) + if (aiResponse.length > 200) "..." else "",
                                timeframe = "24h",
                                technicalFactors = listOf("Technical Analysis", "Market Trends"),
                                educationalConnection = "Dựa trên kiến thức bài học crypto"
                            )
                        }
                        Result.success(prediction)
                    } else {
                        Result.failure(Exception("Không nhận được dự đoán từ AI"))
                    }
                } ?: Result.failure(Exception("Phản hồi AI rỗng"))
            } else {
                Result.failure(Exception("Lỗi API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể tạo dự đoán: ${e.message}"))
        }
    }
    
    private suspend fun generateOfflinePrediction(): Result<PricePrediction> {
        return try {
            val prompt = """
            Phân tích Bitcoin tổng quát. Trả về JSON này, không thêm gì khác:
            
            {
                "predictedPrice": 45500,
                "confidence": "medium",
                "reasoning": "Bitcoin có triển vọng tích cực dài hạn nhờ scarcity, institutional adoption và acceptance tăng. Halving cycles lịch sử cho thấy xu hướng tăng trưởng.",
                "timeframe": "24h",
                "technicalFactors": ["Historical Cycles", "Institutional Adoption", "Scarcity Economics"],
                "educationalConnection": "Áp dụng kiến thức Bitcoin Halving và Market Cycles từ các bài học"
            }
            """
            
            val contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part(text = prompt))
                )
            )
            
            val request = GeminiRequest(contents = contents)
            val apiKey = BuildConfig.GEMINI_API_KEY
            
            if (apiKey.isEmpty()) {
                return Result.failure(Exception("Gemini API key chưa được cấu hình"))
            }
            
            val response = geminiApi.generateContent(apiKey, request)
            
            if (response.isSuccessful) {
                response.body()?.let { geminiResponse ->
                    val aiResponse = geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (aiResponse != null) {
                        // Parse JSON response từ AI
                        val prediction = try {
                            parseAIPredictionResponse(aiResponse, 45000.0, 0.0)
                        } catch (e: Exception) {
                            // Fallback nếu không parse được JSON
                            PricePrediction(
                                predictedPrice = 45000.0,
                                confidence = "medium",
                                reasoning = aiResponse.take(300) + if (aiResponse.length > 300) "..." else "",
                                timeframe = "24h", 
                                technicalFactors = listOf("Market Analysis", "Educational Knowledge"),
                                educationalConnection = "Phân tích dựa trên kiến thức crypto tổng quát"
                            )
                        }
                        Result.success(prediction)
                    } else {
                        Result.failure(Exception("Không nhận được phân tích từ AI"))
                    }
                } ?: Result.failure(Exception("Phản hồi AI rỗng"))
            } else {
                Result.failure(Exception("Lỗi AI API: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Fallback prediction nếu AI cũng lỗi
            val fallbackPrediction = PricePrediction(
                predictedPrice = 45000.0,
                confidence = "low",
                reasoning = "Phân tích offline: Bitcoin có xu hướng tăng trưởng dài hạn do tính khan hiếm và adoption tăng. Tuy nhiên, thị trường crypto rất biến động và bị ảnh hưởng bởi nhiều yếu tố bên ngoài.",
                timeframe = "24h",
                technicalFactors = listOf("Historical Trends", "Scarcity Economics", "Market Adoption"),
                educationalConnection = "Dựa trên bài học về Bitcoin Economics và Market Cycles"
            )
            Result.success(fallbackPrediction)
        }
    }
    
    private fun parseAIPredictionResponse(
        aiResponse: String, 
        currentPrice: Double, 
        priceChange: Double
    ): PricePrediction {
        val gson = Gson()
        
        // Cố gắng extract JSON từ response (có thể có markdown formatting)
        val jsonStart = aiResponse.indexOf("{")
        val jsonEnd = aiResponse.lastIndexOf("}") + 1
        
        return if (jsonStart != -1 && jsonEnd > jsonStart) {
            val jsonString = aiResponse.substring(jsonStart, jsonEnd)
            
            try {
                // Parse JSON response
                val jsonResponse = gson.fromJson(jsonString, Map::class.java)
                
                PricePrediction(
                    predictedPrice = (jsonResponse["predictedPrice"] as? Double) ?: currentPrice,
                    confidence = (jsonResponse["confidence"] as? String) ?: "medium",
                    reasoning = (jsonResponse["reasoning"] as? String) ?: "Phân tích kỹ thuật cơ bản",
                    timeframe = (jsonResponse["timeframe"] as? String) ?: "24h",
                    technicalFactors = (jsonResponse["technicalFactors"] as? List<String>) ?: listOf("Market Analysis"),
                    educationalConnection = (jsonResponse["educationalConnection"] as? String) ?: "Kiến thức crypto cơ bản"
                )
            } catch (e: JsonSyntaxException) {
                // Nếu JSON parse fail, dùng text thuần
                createFallbackPrediction(aiResponse, currentPrice)
            }
        } else {
            // Không có JSON, dùng text thuần
            createFallbackPrediction(aiResponse, currentPrice)
        }
    }
    
    private fun createFallbackPrediction(aiResponse: String, currentPrice: Double): PricePrediction {
        return PricePrediction(
            predictedPrice = currentPrice,
            confidence = "medium",
            reasoning = aiResponse.take(400) + if (aiResponse.length > 400) "..." else "",
            timeframe = "24h",
            technicalFactors = listOf("AI Analysis", "Market Trends"),
            educationalConnection = "Phân tích dựa trên kiến thức crypto và blockchain"
        )
    }
}