package com.lamontlabs.quantravision.ui.screens.intelligence

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.ui.components.*
import com.lamontlabs.quantravision.ui.theme.*
import com.lamontlabs.quantravision.entitlements.Feature
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.NeonText
import com.lamontlabs.quantravision.ui.StaticBrandBackground

@Composable
fun RegimeNavigatorScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onNavigateToPaywall: () -> Unit = {}
) {
    FeatureGate(
        feature = Feature.REGIME_NAVIGATOR,
        onUpgradeClick = onNavigateToPaywall
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Regime Navigator") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            StaticBrandBackground {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(AppSpacing.base)
                ) {
                    NeonText(
                        text = "Market Regime Analysis",
                        style = AppTypography.headlineMedium
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    
                    Text(
                        text = "AI-powered market regime detection helps you adapt your strategy to current market conditions.",
                        style = AppTypography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    
                    MetallicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(AppSpacing.md)) {
                            Text(
                                text = "Current Regime",
                                style = AppTypography.titleMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.sm))
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = AppColors.Success,
                                    modifier = Modifier.size(32.dp)
                                )
                                Column {
                                    Text(
                                        text = "Trending Bullish",
                                        style = AppTypography.titleLarge,
                                        color = AppColors.Success
                                    )
                                    Text(
                                        text = "Confidence: 87%",
                                        style = AppTypography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    
                    SectionHeader(title = "Regime History")
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    
                    listOf(
                        RegimeData("Trending Bullish", "87%", "2 days", AppColors.Success),
                        RegimeData("Range-Bound", "72%", "5 days", AppColors.Warning),
                        RegimeData("Volatile", "91%", "3 days", AppColors.Error)
                    ).forEach { regime ->
                        RegimeHistoryCard(regime)
                        Spacer(modifier = Modifier.height(AppSpacing.sm))
                    }
                }
            }
        }
    }
}

data class RegimeData(val name: String, val confidence: String, val duration: String, val color: Color)

@Composable
private fun RegimeHistoryCard(regime: RegimeData) {
    MetallicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = regime.name,
                    style = AppTypography.titleSmall,
                    color = regime.color
                )
                Text(
                    text = "Duration: ${regime.duration}",
                    style = AppTypography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            Text(
                text = regime.confidence,
                style = AppTypography.titleSmall,
                color = Color.White
            )
        }
    }
}
