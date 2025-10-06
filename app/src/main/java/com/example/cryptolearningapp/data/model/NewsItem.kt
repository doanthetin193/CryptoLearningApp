package com.example.cryptolearningapp.data.model

import com.google.gson.annotations.SerializedName

data class NewsResponse(
    @SerializedName("result")
    val result: List<NewsItem>?
)

data class NewsItem(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("feedDate") val feedDate: Long,
    @SerializedName("source") val source: String,
    @SerializedName("imgUrl") val imgUrl: String,
    @SerializedName("link") val link: String,
    @SerializedName("relatedCoins") val relatedCoins: List<String>?,
    @SerializedName("isFeatured") val isFeatured: Boolean,
    val aiSummary: String? = null,
    val isLoadingSummary: Boolean = false
) 