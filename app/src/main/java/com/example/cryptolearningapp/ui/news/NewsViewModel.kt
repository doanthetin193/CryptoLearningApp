package com.example.cryptolearningapp.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptolearningapp.data.model.NewsItem
import com.example.cryptolearningapp.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    init {
        loadNews()
    }

    fun loadNews() {
        viewModelScope.launch {
            try {
                _uiState.value = NewsUiState.Loading
                repository.getNews()
                    .onSuccess { news ->
                        if (news.isEmpty()) {
                            _uiState.value = NewsUiState.Error("Không có tin tức nào")
                        } else {
                            _uiState.value = NewsUiState.Success(news)
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = NewsUiState.Error(
                            error.message ?: "Có lỗi xảy ra khi tải tin tức"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = NewsUiState.Error(
                    e.message ?: "Có lỗi xảy ra khi tải tin tức"
                )
            }
        }
    }
    
    fun generateAISummary(newsItem: NewsItem) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is NewsUiState.Success) {
                // Set loading state for this specific item
                val updatedNews = currentState.news.map { item ->
                    if (item.id == newsItem.id) {
                        item.copy(isLoadingSummary = true)
                    } else item
                }
                _uiState.value = NewsUiState.Success(updatedNews)
                
                // Generate AI summary
                repository.generateAISummary(newsItem.title)
                    .onSuccess { summary ->
                        val finalNews = currentState.news.map { item ->
                            if (item.id == newsItem.id) {
                                item.copy(aiSummary = summary, isLoadingSummary = false)
                            } else item
                        }
                        _uiState.value = NewsUiState.Success(finalNews)
                    }
                    .onFailure { error ->
                        val finalNews = currentState.news.map { item ->
                            if (item.id == newsItem.id) {
                                item.copy(isLoadingSummary = false)
                            } else item
                        }
                        _uiState.value = NewsUiState.Success(finalNews)
                        // Could show error snackbar here
                    }
            }
        }
    }
}

sealed class NewsUiState {
    data object Loading : NewsUiState()
    data class Success(val news: List<NewsItem>) : NewsUiState()
    data class Error(val message: String) : NewsUiState()
} 