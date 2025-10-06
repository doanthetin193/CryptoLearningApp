package com.example.cryptolearningapp.ui.screen.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptolearningapp.data.model.ChartDataPoint
import com.example.cryptolearningapp.data.model.ChartTimeframe
import com.example.cryptolearningapp.data.model.PricePrediction
import com.example.cryptolearningapp.data.repository.ChartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val repository: ChartRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ChartUiState>(ChartUiState.Loading)
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()
    
    private val _currentTimeframe = MutableStateFlow(ChartTimeframe.SEVEN_DAYS)
    val currentTimeframe: StateFlow<ChartTimeframe> = _currentTimeframe.asStateFlow()
    
    private val _currentPrice = MutableStateFlow<Pair<Double, Double>?>(null)
    val currentPrice: StateFlow<Pair<Double, Double>?> = _currentPrice.asStateFlow()
    
    private val _prediction = MutableStateFlow<PricePrediction?>(null)
    val prediction: StateFlow<PricePrediction?> = _prediction.asStateFlow()
    
    private val _isPredicting = MutableStateFlow(false)
    val isPredicting: StateFlow<Boolean> = _isPredicting.asStateFlow()
    
    init {
        loadChartData()
        loadCurrentPrice()
    }
    
    fun selectTimeframe(timeframe: ChartTimeframe) {
        _currentTimeframe.value = timeframe
        loadChartData()
    }
    
    private fun loadChartData() {
        viewModelScope.launch {
            _uiState.value = ChartUiState.Loading
            
            repository.getChartData(_currentTimeframe.value)
                .onSuccess { data ->
                    _uiState.value = ChartUiState.Success(data)
                }
                .onFailure { error ->
                    _uiState.value = ChartUiState.Error(error.message ?: "Có lỗi xảy ra")
                }
        }
    }
    
    private fun loadCurrentPrice() {
        viewModelScope.launch {
            repository.getCurrentPrice()
                .onSuccess { priceData ->
                    _currentPrice.value = priceData
                }
                .onFailure {
                    // Silent fail for current price
                }
        }
    }
    
    fun generatePrediction() {
        viewModelScope.launch {
            _isPredicting.value = true
            _prediction.value = null
            
            val currentState = _uiState.value
            val chartData = if (currentState is ChartUiState.Success) {
                currentState.chartData
            } else {
                emptyList() // Cho phép offline prediction
            }
            
            repository.generatePricePrediction(chartData, _currentTimeframe.value)
                .onSuccess { prediction ->
                    _prediction.value = prediction
                    _isPredicting.value = false
                }
                .onFailure { error ->
                    _isPredicting.value = false
                    // Could show error state here
                }
        }
    }
    
    fun clearPrediction() {
        _prediction.value = null
    }
}

sealed class ChartUiState {
    data object Loading : ChartUiState()
    data class Success(val chartData: List<ChartDataPoint>) : ChartUiState()
    data class Error(val message: String) : ChartUiState()
}