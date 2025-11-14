package com.lamontlabs.quantravision.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MarketsViewModel(private val context: Context) : ViewModel() {
    
    data class MarketData(
        val symbol: String,
        val price: Double,
        val change: Double,
        val changePercent: Double,
        val volume: Long
    )
    
    data class UiState(
        val markets: List<MarketData> = emptyList(),
        val selectedMarket: MarketData? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val hasMarketAccess: Boolean = false
    )
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    init {
        checkMarketAccess()
        loadMarkets()
    }
    
    private fun checkMarketAccess() {
        val hasAccess = EntitlementManager.hasFeatureAccess(SubscriptionTier.STANDARD)
        _uiState.update { it.copy(hasMarketAccess = hasAccess) }
    }
    
    private fun loadMarkets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                delay(500)
                
                val mockMarkets = listOf(
                    MarketData("SPY", 450.25, 2.75, 0.61, 75000000),
                    MarketData("QQQ", 375.80, -1.20, -0.32, 42000000),
                    MarketData("AAPL", 182.50, 3.15, 1.76, 55000000),
                    MarketData("MSFT", 378.20, -2.40, -0.63, 28000000),
                    MarketData("TSLA", 245.60, 5.80, 2.42, 98000000),
                    MarketData("NVDA", 495.30, 8.50, 1.75, 62000000),
                    MarketData("GOOGL", 142.75, -0.85, -0.59, 31000000),
                    MarketData("AMZN", 168.90, 1.90, 1.14, 45000000)
                )
                
                _uiState.update {
                    it.copy(
                        markets = mockMarkets,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load market data: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun selectMarket(market: MarketData) {
        _uiState.update { it.copy(selectedMarket = market) }
    }
    
    fun refreshMarkets() {
        loadMarkets()
    }
}
