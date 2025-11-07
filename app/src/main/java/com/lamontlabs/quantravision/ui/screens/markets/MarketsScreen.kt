package com.lamontlabs.quantravision.ui.screens.markets

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.ui.*

/**
 * Markets Screen - Intelligence & Pattern Library
 * Dual-layer metallic cards with holographic visual effects
 * 
 * Features:
 * - QuantumGridBackground with ParticleStarfield overlay
 * - GlassMorphicCard outer layers with MetallicCard enhancements
 * - HolographicChart previews showing pattern data trends
 * - RegimeTimelineBand visualizations
 * - PredictiveHeatmap confidence indicators
 * - NeonText section headers with glow effects
 * - PRO badges with enhanced neon styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketsScreen(
    context: Context,
    onNavigateToTemplates: () -> Unit,
    onNavigateToIntelligence: () -> Unit,
    onNavigateToPredictions: () -> Unit,
    onNavigateToBacktesting: () -> Unit,
    onNavigateToSimilarity: () -> Unit,
    onNavigateToMultiChart: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Static brand background - no animations
        StaticBrandBackground(modifier = Modifier.fillMaxSize())
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        NeonText(
                            text = "Markets & Intelligence",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            glowIntensity = 0.6f
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                // ============================================
                // PATTERN LIBRARY SECTION
                // ============================================
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    NeonText(
                        text = "Pattern Library",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        glowColor = NeonCyan,
                        glowIntensity = 0.8f,
                        enablePulse = true
                    )
                }
                
                // Template Manager - Standard Feature (no PRO badge)
                item {
                    DualLayerFeatureCard(
                        icon = Icons.Default.Category,
                        title = "Template Manager",
                        description = "Browse and configure all 109 chart patterns",
                        badge = "109 Patterns",
                        isPro = false,
                        confidence = 0.95f,
                        enableShimmer = false,
                        chartData = generatePatternDistributionData(),
                        onClick = onNavigateToTemplates
                    )
                }
                
                // ============================================
                // INTELLIGENCE STACK SECTION
                // ============================================
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    NeonText(
                        text = "Intelligence Stack",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        glowColor = NeonGold,
                        glowIntensity = 0.9f,
                        enablePulse = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    RegimeTimelineBand(
                        regimeData = listOf(
                            RegimePeriod("Bull Trend", Color(0xFF00FF88), 0.35f),
                            RegimePeriod("Consolidation", Color(0xFFFFB347), 0.3f),
                            RegimePeriod("Bear Trend", Color(0xFFFF4444), 0.2f),
                            RegimePeriod("High Vol", NeonCyan, 0.15f)
                        )
                    )
                }
                
                // Intelligence Hub - PRO Feature with Enhanced Styling
                item {
                    DualLayerFeatureCard(
                        icon = Icons.Default.Psychology,
                        title = "Intelligence Hub",
                        description = "Regime Navigator • Pattern-to-Plan • Behavioral Guardrails",
                        badge = "💎 PRO",
                        isPro = true,
                        confidence = 0.92f,
                        enableShimmer = true,
                        chartData = generateIntelligenceData(),
                        onClick = onNavigateToIntelligence
                    )
                }
                
                // Predictions - PRO Feature
                item {
                    DualLayerFeatureCard(
                        icon = Icons.Default.TrendingUp,
                        title = "Predictions",
                        description = "AI-powered pattern outcome forecasting",
                        badge = "💎 PRO",
                        isPro = true,
                        confidence = 0.88f,
                        enableShimmer = true,
                        chartData = generatePredictionData(),
                        onClick = onNavigateToPredictions
                    )
                }
                
                // Backtesting - PRO Feature
                item {
                    DualLayerFeatureCard(
                        icon = Icons.Default.Timeline,
                        title = "Backtesting",
                        description = "Test patterns against historical data",
                        badge = "💎 PRO",
                        isPro = true,
                        confidence = 0.85f,
                        enableShimmer = false,
                        chartData = generateBacktestData(),
                        onClick = onNavigateToBacktesting
                    )
                }
                
                // Similarity Search - PRO Feature
                item {
                    DualLayerFeatureCard(
                        icon = Icons.Default.Search,
                        title = "Similarity Search",
                        description = "Find similar historical chart patterns",
                        badge = "💎 PRO",
                        isPro = true,
                        confidence = 0.82f,
                        enableShimmer = false,
                        chartData = generateSimilarityData(),
                        onClick = onNavigateToSimilarity
                    )
                }
                
                // Multi-Chart View - PRO Feature
                item {
                    DualLayerFeatureCard(
                        icon = Icons.Default.Dashboard,
                        title = "Multi-Chart View",
                        description = "Analyze multiple timeframes simultaneously",
                        badge = "💎 PRO",
                        isPro = true,
                        confidence = 0.78f,
                        enableShimmer = false,
                        chartData = generateMultiChartData(),
                        onClick = onNavigateToMultiChart
                    )
                }
                
                // Info Card with Glassmorphic Design
                item {
                    GlassMorphicCard(
                        backgroundColor = Color(0xFF0D1219).copy(alpha = 0.85f),
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        glowIntensity = 0.4f
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlowingIcon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                glowColor = MaterialTheme.colorScheme.primary,
                                size = 24.dp,
                                glowIntensity = 0.6f
                            )
                            Text(
                                text = "All pattern recognition runs 100% offline on your device using OpenCV template matching",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dual-layer feature card with metallic outer shell and holographic inner preview
 * 
 * Design:
 * - Outer: GlassMorphicCard with metallic border
 * - Inner: HolographicChart preview + content
 * - Predictive heatmap for confidence visualization
 * - Enhanced styling for PRO features
 */
@Composable
private fun DualLayerFeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    badge: String? = null,
    isPro: Boolean = false,
    confidence: Float = 0.85f,
    enableShimmer: Boolean = false,
    chartData: List<Float> = emptyList(),
    onClick: () -> Unit
) {
    // Outer layer: GlassMorphicCard
    GlassMorphicCard(
        onClick = onClick,
        backgroundColor = if (isPro) {
            Color(0xFF0D1219).copy(alpha = 0.95f)
        } else {
            Color(0xFF0D1219).copy(alpha = 0.90f)
        },
        borderColor = if (isPro) NeonGold else NeonCyan,
        borderWidth = if (isPro) 1.5.dp else 1.dp,
        glowIntensity = if (isPro) 0.8f else 0.6f,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isPro) {
                    Modifier.layeredShadow(
                        shadows = dramaticDepthShadows,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier.layeredShadow(
                        shadows = subtleDepthShadows,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
                }
            )
    ) {
        // Inner metallic card layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = if (isPro) metallicAngularBrush else Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1D23),
                            Color(0xFF0D1219)
                        )
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Row: Icon + Title + Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        GlowingIcon(
                            imageVector = icon,
                            contentDescription = null,
                            glowColor = if (isPro) NeonGold else MaterialTheme.colorScheme.primary,
                            iconColor = Color.White,
                            size = 40.dp,
                            glowIntensity = if (isPro) 0.9f else 0.7f
                        )
                        
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            if (badge != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                if (isPro) {
                                    // PRO badge with neon glow
                                    Surface(
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                        color = NeonGold.copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            NeonGold.copy(alpha = 0.8f)
                                        ),
                                        modifier = Modifier.layeredShadow(
                                            shadows = listOf(
                                                LayeredShadow(
                                                    color = NeonGold.copy(alpha = 0.4f),
                                                    radius = 8.dp,
                                                    offset = androidx.compose.ui.geometry.Offset(0f, 0f)
                                                )
                                            ),
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                        )
                                    ) {
                                        NeonText(
                                            text = badge,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            glowColor = NeonGold,
                                            textColor = Color.White,
                                            glowIntensity = 0.9f,
                                            enablePulse = true
                                        )
                                    }
                                } else {
                                    // Standard badge
                                    Surface(
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = badge,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (isPro) NeonGold.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Description
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )
                
                // Holographic Chart Preview (if data available)
                if (chartData.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.3f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        HolographicChart(
                            dataPoints = chartData,
                            lineColor = if (isPro) NeonGold else NeonCyan,
                            showScanline = isPro
                        )
                    }
                }
                
                // Predictive Heatmap - Confidence Indicator
                PredictiveHeatmap(
                    confidence = confidence,
                    showPulse = isPro && confidence >= 0.85f
                )
            }
        }
    }
}

// ============================================================================
// SAMPLE DATA GENERATORS FOR HOLOGRAPHIC CHARTS
// ============================================================================

private fun generatePatternDistributionData(): List<Float> {
    return listOf(0.3f, 0.5f, 0.7f, 0.6f, 0.8f, 0.9f, 0.7f, 0.5f, 0.6f, 0.8f)
}

private fun generateIntelligenceData(): List<Float> {
    return listOf(0.4f, 0.6f, 0.8f, 0.9f, 0.85f, 0.75f, 0.8f, 0.9f, 0.95f, 0.9f)
}

private fun generatePredictionData(): List<Float> {
    return listOf(0.5f, 0.6f, 0.7f, 0.75f, 0.8f, 0.85f, 0.9f, 0.88f, 0.92f, 0.95f)
}

private fun generateBacktestData(): List<Float> {
    return listOf(0.6f, 0.5f, 0.7f, 0.8f, 0.75f, 0.8f, 0.85f, 0.9f, 0.85f, 0.88f)
}

private fun generateSimilarityData(): List<Float> {
    return listOf(0.4f, 0.5f, 0.6f, 0.7f, 0.75f, 0.8f, 0.82f, 0.85f, 0.83f, 0.82f)
}

private fun generateMultiChartData(): List<Float> {
    return listOf(0.3f, 0.4f, 0.5f, 0.6f, 0.65f, 0.7f, 0.75f, 0.78f, 0.8f, 0.78f)
}
