package com.example.cryptolearningapp.data.model

import com.google.gson.annotations.SerializedName

data class ChartResponse(
    @SerializedName("prices") val prices: List<List<Double>>
)

data class ChartDataPoint(
    val timestamp: Long,
    val price: Double
)

data class PricePrediction(
    val predictedPrice: Double,
    val confidence: String, // "high", "medium", "low"
    val reasoning: String,
    val timeframe: String, // "24h", "7d", "30d"
    val technicalFactors: List<String>,
    val educationalConnection: String
)

enum class ChartTimeframe(val days: Int, val label: String) {
    ONE_DAY(1, "1D"),
    SEVEN_DAYS(7, "7D"), 
    THIRTY_DAYS(30, "30D"),
    NINETY_DAYS(90, "90D")
}