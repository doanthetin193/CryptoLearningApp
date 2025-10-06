package com.example.cryptolearningapp.ui.screen.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cryptolearningapp.data.model.ChartTimeframe
import com.example.cryptolearningapp.data.model.PricePrediction
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    viewModel: ChartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentTimeframe by viewModel.currentTimeframe.collectAsState()
    val currentPrice by viewModel.currentPrice.collectAsState()
    val prediction by viewModel.prediction.collectAsState()
    val isPredicting by viewModel.isPredicting.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Bitcoin Charts",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Price Header
            PriceHeader(currentPrice = currentPrice)
            
            // Timeframe Selector
            TimeframeSelector(
                selectedTimeframe = currentTimeframe,
                onTimeframeSelected = { viewModel.selectTimeframe(it) }
            )
            
            // Chart Content
            when (uiState) {
                is ChartUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is ChartUiState.Success -> {
                    BitcoinChart(chartData = (uiState as ChartUiState.Success).chartData)
                }
                
                is ChartUiState.Error -> {
                    Column {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = (uiState as ChartUiState.Error).message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.selectTimeframe(currentTimeframe) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text("Thử lại")
                                    }
                                    
                                    OutlinedButton(
                                        onClick = { viewModel.generatePrediction() }
                                    ) {
                                        Text("Phân tích offline")
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Hiển thị placeholder chart
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "📈",
                                        style = MaterialTheme.typography.headlineLarge
                                    )
                                    Text(
                                        text = "Biểu đồ không khả dụng",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Bạn vẫn có thể sử dụng phân tích AI",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // AI Prediction Section
            AIPredictionSection(
                prediction = prediction,
                isPredicting = isPredicting,
                onGeneratePrediction = { viewModel.generatePrediction() },
                onClearPrediction = { viewModel.clearPrediction() }
            )
        }
    }
}

@Composable
private fun PriceHeader(currentPrice: Pair<Double, Double>?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bitcoin (BTC)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            currentPrice?.let { (price, change) ->
                Text(
                    text = "$${String.format("%,.0f", price)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                val changeColor = if (change >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                Text(
                    text = "${if (change >= 0) "+" else ""}${String.format("%.2f", change)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = changeColor,
                    fontWeight = FontWeight.Medium
                )
            } ?: run {
                Text(
                    text = "Loading price...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun TimeframeSelector(
    selectedTimeframe: ChartTimeframe,
    onTimeframeSelected: (ChartTimeframe) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ChartTimeframe.values().forEach { timeframe ->
            FilterChip(
                onClick = { onTimeframeSelected(timeframe) },
                label = { Text(timeframe.label) },
                selected = selectedTimeframe == timeframe
            )
        }
    }
}

@Composable
private fun BitcoinChart(chartData: List<com.example.cryptolearningapp.data.model.ChartDataPoint>) {
    if (chartData.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            SimpleLineChart(
                data = chartData,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun SimpleLineChart(
    data: List<com.example.cryptolearningapp.data.model.ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    val minPrice = data.minOf { it.price }.toFloat()
    val maxPrice = data.maxOf { it.price }.toFloat()
    val priceRange = maxPrice - minPrice
    
    Canvas(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
    ) {
        val width = size.width
        val height = size.height
        
        // Draw price line
        val path = Path()
        
        data.forEachIndexed { index, point ->
            val x = (index.toFloat() / (data.size - 1)) * width
            val normalizedPrice = ((point.price.toFloat() - minPrice) / priceRange)
            val y = height - (normalizedPrice * height)
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        // Draw the line
        drawPath(
            path = path,
            color = androidx.compose.ui.graphics.Color(0xFF2196F3),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
        )
        
        // Draw price points
        data.forEachIndexed { index, point ->
            val x = (index.toFloat() / (data.size - 1)) * width
            val normalizedPrice = ((point.price.toFloat() - minPrice) / priceRange)
            val y = height - (normalizedPrice * height)
            
            drawCircle(
                color = androidx.compose.ui.graphics.Color(0xFF2196F3),
                radius = 2.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun AIPredictionSection(
    prediction: PricePrediction?,
    isPredicting: Boolean,
    onGeneratePrediction: () -> Unit,
    onClearPrediction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Price Prediction",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (prediction != null) {
                    TextButton(onClick = onClearPrediction) {
                        Text("Clear")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isPredicting) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI đang phân tích...")
                }
            } else if (prediction != null) {
                PredictionContent(prediction = prediction)
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nhấn để AI phân tích biểu đồ và dự đoán giá dựa trên kiến thức crypto",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(onClick = onGeneratePrediction) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Phân tích & Dự đoán")
                    }
                }
            }
        }
    }
}

@Composable
private fun PredictionContent(prediction: PricePrediction) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Predicted Price
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dự đoán giá (${prediction.timeframe}):",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$${String.format("%,.0f", prediction.predictedPrice)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Confidence Level
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Độ tin cậy:",
                style = MaterialTheme.typography.bodyMedium
            )
            val confidenceColor = when (prediction.confidence) {
                "high" -> Color(0xFF4CAF50)
                "medium" -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }
            Text(
                text = prediction.confidence.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = confidenceColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(confidenceColor.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        
        // AI Reasoning
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Phân tích AI:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = prediction.reasoning,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Educational Connection
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "📚 Liên kết bài học:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = prediction.educationalConnection,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}