package com.example.cryptolearningapp.data.api

import com.example.cryptolearningapp.data.model.ChartResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CoinGeckoApi {
    @GET("coins/{id}/market_chart")
    suspend fun getCoinChart(
        @Path("id") coinId: String = "bitcoin",
        @Query("vs_currency") currency: String = "usd",
        @Query("days") days: Int
    ): ChartResponse
    
    @GET("simple/price")
    suspend fun getCurrentPrice(
        @Query("ids") ids: String = "bitcoin",
        @Query("vs_currencies") currencies: String = "usd",
        @Query("include_24hr_change") includeChange: Boolean = true
    ): Map<String, Map<String, Double>>
}