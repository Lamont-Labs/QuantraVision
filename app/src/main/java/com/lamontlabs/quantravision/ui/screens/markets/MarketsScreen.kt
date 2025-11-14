package com.lamontlabs.quantravision.ui.screens.markets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lamontlabs.quantravision.entitlements.Feature
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.NeonText
import com.lamontlabs.quantravision.ui.StaticBrandBackground
import com.lamontlabs.quantravision.ui.components.ErrorState
import com.lamontlabs.quantravision.ui.components.FeatureGate
import com.lamontlabs.quantravision.ui.theme.AppColors
import com.lamontlabs.quantravision.ui.theme.AppElevation
import com.lamontlabs.quantravision.ui.theme.AppSpacing
import com.lamontlabs.quantravision.ui.theme.AppTypography
import com.lamontlabs.quantravision.ui.viewmodels.MarketsViewModel

/**
 * MarketsScreen - Real-time market data display
 */
@Composable
fun MarketsScreen(
    modifier: Modifier = Modifier,
    onNavigateToPaywall: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember { MarketsViewModel(context) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    FeatureGate(
        feature = Feature.MARKET_DATA,
        onUpgradeClick = onNavigateToPaywall
    ) {
        StaticBrandBackground {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(AppSpacing.base)
            ) {
                NeonText(
                    text = "Markets",
                    style = AppTypography.headlineLarge
                )
                
                Spacer(modifier = Modifier.height(AppSpacing.md))
                
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.errorMessage != null) {
                    ErrorState(
                        message = uiState.errorMessage!!,
                        onRetry = { viewModel.refreshMarkets() }
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        items(uiState.markets) { market ->
                            MarketCard(
                                market = market,
                                isSelected = market == uiState.selectedMarket,
                                onClick = { viewModel.selectMarket(market) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketCard(
    market: MarketsViewModel.MarketData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    MetallicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = if (isSelected) AppElevation.high else AppElevation.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = market.symbol,
                    style = AppTypography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "$${String.format("%.2f", market.price)}",
                    style = AppTypography.bodyLarge,
                    color = Color.White
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                val changeColor = if (market.change >= 0) AppColors.Success else AppColors.Error
                Text(
                    text = "${if (market.change >= 0) "+" else ""}${String.format("%.2f", market.change)}",
                    style = AppTypography.bodyMedium,
                    color = changeColor
                )
                Text(
                    text = "${if (market.changePercent >= 0) "+" else ""}${String.format("%.2f", market.changePercent)}%",
                    style = AppTypography.labelMedium,
                    color = changeColor
                )
            }
        }
    }
}
