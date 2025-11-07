package com.lamontlabs.quantravision.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Advanced Visual Components for QuantraVision
 * Maximum detail metallic/neon design system
 */

// ============================================================================
// STATIC BRAND BACKGROUND - Simple gradient matching logo image
// ============================================================================

/**
 * Static brand background with gradient matching the branded logo image
 * No animations, clean and minimal
 */
@Composable
fun StaticBrandBackground(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF010409), // Deep space blue-black (top)
                        Color(0xFF0D1825), // Mid blue (center)
                        Color(0xFF010409)  // Deep space blue-black (bottom)
                    )
                )
            )
    )
}

/**
 * Container for logo images with matching gradient background
 * Creates seamless blend so logos appear to float
 */
@Composable
fun FloatingLogoContainer(
    painter: androidx.compose.ui.graphics.painter.Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Matching gradient background
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0D1825), // Center matches UI mid-tone
                            Color(0xFF010409)  // Edges fade to dark
                        ),
                        radius = 800f
                    )
                )
        )
        
        // Logo image on top
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = imageModifier,
            contentScale = ContentScale.Fit
        )
    }
}

// ============================================================================
// QUANTUM GRID BACKGROUND - Dark Technical Grid with Neon Accents
// ============================================================================

/**
 * Quantum grid background with animated neon grid lines
 * Creates depth and technical aesthetic matching trading terminals
 * Uses AnimationSpecs for centralized configuration and respects PerformanceProfile
 */
@Composable
fun QuantumGridBackground(
    modifier: Modifier = Modifier,
    gridColor: Color = NeonCyan.copy(alpha = 0.15f),
    animateGrid: Boolean = true
) {
    val performanceProfile = LocalPerformanceProfile.current
    val infiniteTransition = rememberInfiniteTransition(label = "gridAnimation")
    
    val shouldAnimate = animateGrid && performanceProfile.enableGridAnimation
    
    val gridOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = AnimationSpecs.GRID_SPACING_DP,
        animationSpec = infiniteRepeatable(
            animation = tween(
                (AnimationSpecs.GRID_ANIMATION_DURATION * performanceProfile.animationFrameRateMultiplier).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "gridScroll"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val gridSpacing = AnimationSpecs.GRID_SPACING_DP.dp.toPx()
        val offset = if (shouldAnimate) gridOffset else 0f
        
        // Dark quantum gradient background - matches branded logo image
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF010409), // Deep space blue-black (top)
                    Color(0xFF0D1825), // Mid blue (center)
                    Color(0xFF010409)  // Deep space blue-black (bottom)
                )
            )
        )
        
        // Vertical grid lines
        var x = offset
        while (x < size.width + gridSpacing) {
            val alpha = if (x.toInt() % (gridSpacing.toInt() * 5) == 0) 0.3f else 0.15f
            drawLine(
                color = gridColor.copy(alpha = alpha),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = if (alpha > 0.2f) 2f else 1f
            )
            x += gridSpacing
        }
        
        // Horizontal grid lines
        var y = offset
        while (y < size.height + gridSpacing) {
            val alpha = if (y.toInt() % (gridSpacing.toInt() * 5) == 0) 0.3f else 0.15f
            drawLine(
                color = gridColor.copy(alpha = alpha),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = if (alpha > 0.2f) 2f else 1f
            )
            y += gridSpacing
        }
        
        // Radial glow from center
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    NeonCyan.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(size.width / 2f, size.height / 3f)
            )
        )
        
        // Dark gradient overlay for better text contrast (WCAG accessibility)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.3f),
                    Color.Black.copy(alpha = 0.2f),
                    Color.Black.copy(alpha = 0.3f)
                )
            )
        )
    }
}

// ============================================================================
// CANDLESTICK PARALLAX - Animated Trading Chart Background
// ============================================================================

/**
 * Parallax candlestick chart background effect
 * Creates depth with animated chart patterns in background
 * Uses AnimationSpecs for centralized configuration and respects PerformanceProfile
 */
@Composable
fun CandlestickParallax(
    modifier: Modifier = Modifier,
    candleColor: Color = NeonCyan.copy(alpha = 0.1f),
    animate: Boolean = true
) {
    val performanceProfile = LocalPerformanceProfile.current
    val infiniteTransition = rememberInfiniteTransition(label = "candleAnimation")
    
    val shouldAnimate = animate && performanceProfile.enableSecondaryAnimations
    
    val scrollOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                (AnimationSpecs.CANDLESTICK_SCROLL_DURATION * performanceProfile.animationFrameRateMultiplier).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "candleScroll"
    )
    
    // Generate stable random candlestick data
    val candleData = remember {
        List(50) {
            CandleData(
                open = Random.nextFloat() * 0.5f + 0.3f,
                close = Random.nextFloat() * 0.5f + 0.3f,
                high = Random.nextFloat() * 0.2f + 0.7f,
                low = Random.nextFloat() * 0.3f + 0.1f
            )
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val candleWidth = 20.dp.toPx()
        val candleSpacing = 30.dp.toPx()
        val offset = if (shouldAnimate) scrollOffset else 0f
        
        candleData.forEachIndexed { index, candle ->
            val x = (index * candleSpacing) - offset
            
            if (x >= -candleSpacing && x <= size.width + candleSpacing) {
                val bodyTop = size.height * (1f - candle.open.coerceAtMost(candle.close))
                val bodyBottom = size.height * (1f - candle.open.coerceAtLeast(candle.close))
                val wickTop = size.height * (1f - candle.high)
                val wickBottom = size.height * (1f - candle.low)
                
                val isGreen = candle.close > candle.open
                val color = if (isGreen) 
                    Color(0xFF00FF88).copy(alpha = 0.1f) 
                else 
                    Color(0xFFFF4444).copy(alpha = 0.1f)
                
                // Wick
                drawLine(
                    color = color,
                    start = Offset(x + candleWidth / 2, wickTop),
                    end = Offset(x + candleWidth / 2, wickBottom),
                    strokeWidth = 2f
                )
                
                // Body
                drawRect(
                    color = color,
                    topLeft = Offset(x, bodyTop),
                    size = Size(candleWidth, bodyBottom - bodyTop)
                )
            }
        }
    }
}

data class CandleData(
    val open: Float,
    val close: Float,
    val high: Float,
    val low: Float
)

// ============================================================================
// HOLOGRAPHIC CHART OVERLAY - Futuristic Data Visualization
// ============================================================================

/**
 * Holographic chart with scanline effect
 * Creates futuristic trading terminal aesthetic
 * Uses AnimationSpecs for centralized configuration and respects PerformanceProfile
 */
@Composable
fun HolographicChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = NeonCyan,
    showScanline: Boolean = true
) {
    val performanceProfile = LocalPerformanceProfile.current
    val infiniteTransition = rememberInfiniteTransition(label = "scanline")
    
    val shouldShowScanline = showScanline && performanceProfile.enableSecondaryAnimations
    
    val scanlineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                (AnimationSpecs.SCANLINE_DURATION * performanceProfile.animationFrameRateMultiplier).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanlinePosition"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        if (dataPoints.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val spacing = width / (dataPoints.size - 1).coerceAtLeast(1)
        
        // Draw holographic grid
        for (i in 0..10) {
            drawLine(
                color = lineColor.copy(alpha = 0.05f),
                start = Offset(0f, height * i / 10f),
                end = Offset(width, height * i / 10f),
                strokeWidth = 1f
            )
        }
        
        // Draw data line with glow
        val path = Path()
        dataPoints.forEachIndexed { index, value ->
            val x = index * spacing
            val y = height * (1f - value.coerceIn(0f, 1f))
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        // Glow layer
        drawPath(
            path = path,
            color = lineColor.copy(alpha = 0.3f),
            style = Stroke(width = 8f)
        )
        
        // Main line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3f)
        )
        
        // Scanline effect
        if (shouldShowScanline) {
            val scanY = height * scanlineY
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(0f, scanY),
                end = Offset(width, scanY),
                strokeWidth = 2f
            )
        }
        
        // Dark semi-transparent background for better text contrast (WCAG accessibility)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.25f),
                    Color.Black.copy(alpha = 0.15f),
                    Color.Black.copy(alpha = 0.25f)
                )
            )
        )
    }
}

// ============================================================================
// DEPTH MASK OVERLAY - Atmospheric Fog Effect
// ============================================================================

/**
 * Depth mask creates atmospheric fog/distance effect
 * Adds cinematic depth to UI layers
 */
@Composable
fun DepthMaskOverlay(
    modifier: Modifier = Modifier,
    intensity: Float = 0.3f
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0f),
                        Color.Black.copy(alpha = intensity * 0.5f),
                        Color.Black.copy(alpha = intensity)
                    )
                )
            )
    )
}

// ============================================================================
// METALLIC HERO BADGE - Premium Logo Display
// ============================================================================

/**
 * Metallic hero badge with pulsing glow
 * For displaying the QV logo with premium effects
 * Uses AnimationSpecs for centralized configuration and respects PerformanceProfile
 */
@Composable
fun MetallicHeroBadge(
    modifier: Modifier = Modifier,
    pulseSync: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val performanceProfile = LocalPerformanceProfile.current
    val infiniteTransition = rememberInfiniteTransition(label = "badgePulse")
    
    val shouldPulse = pulseSync && performanceProfile.enablePulseAnimations
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                (AnimationSpecs.BADGE_PULSE_DURATION * performanceProfile.animationFrameRateMultiplier).toInt(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsing"
    )
    
    Box(
        modifier = modifier
    ) {
        // Outer glow rings (only when pulse is enabled)
        if (shouldPulse) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = NeonCyan.copy(alpha = pulseAlpha * 0.2f),
                    radius = size.minDimension / 2f * 1.2f
                )
                drawCircle(
                    color = NeonCyan.copy(alpha = pulseAlpha * 0.3f),
                    radius = size.minDimension / 2f
                )
            }
        }
        
        // Content (logo)
        Box(
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}

// ============================================================================
// SIGNAL TICKER - Live AI Metrics Display
// ============================================================================

/**
 * Animated signal ticker for displaying live AI metrics
 * Creates terminal-like data stream effect
 * Uses AnimationSpecs for centralized configuration and respects PerformanceProfile
 */
@Composable
fun SignalTicker(
    signals: List<String>,
    modifier: Modifier = Modifier,
    tickerColor: Color = NeonCyan
) {
    val performanceProfile = LocalPerformanceProfile.current
    val infiniteTransition = rememberInfiniteTransition(label = "ticker")
    
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                (AnimationSpecs.TICKER_SCROLL_DURATION * performanceProfile.animationFrameRateMultiplier).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "tickerScroll"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF0A0E1A).copy(alpha = 0.8f),
                        Color.Transparent
                    )
                )
            )
    ) {
        // Ticker text would go here - simplified for now
        // In full implementation, would scroll text horizontally
    }
}

// ============================================================================
// NEON BOUNDING BOX - Pattern Detection Highlight
// ============================================================================

/**
 * Animated neon bounding box for highlighting detected patterns
 * Creates futuristic scan/detection visual effect
 * Uses AnimationSpecs for centralized configuration and respects PerformanceProfile
 */
@Composable
fun NeonBoundingBox(
    modifier: Modifier = Modifier,
    borderColor: Color = NeonCyan,
    animated: Boolean = true
) {
    val performanceProfile = LocalPerformanceProfile.current
    val infiniteTransition = rememberInfiniteTransition(label = "boundingBox")
    
    val shouldAnimate = animated && performanceProfile.enableSecondaryAnimations
    
    val cornerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                (AnimationSpecs.BOUNDING_BOX_CORNER_ANIMATION_DURATION * performanceProfile.animationFrameRateMultiplier).toInt(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cornerAnimation"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 3f
        val cornerLength = if (shouldAnimate) 30.dp.toPx() + cornerOffset else 30.dp.toPx()
        
        // Top-left corner
        drawLine(
            color = borderColor,
            start = Offset(0f, 0f),
            end = Offset(cornerLength, 0f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = borderColor,
            start = Offset(0f, 0f),
            end = Offset(0f, cornerLength),
            strokeWidth = strokeWidth
        )
        
        // Top-right corner
        drawLine(
            color = borderColor,
            start = Offset(size.width, 0f),
            end = Offset(size.width - cornerLength, 0f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = borderColor,
            start = Offset(size.width, 0f),
            end = Offset(size.width, cornerLength),
            strokeWidth = strokeWidth
        )
        
        // Bottom-left corner
        drawLine(
            color = borderColor,
            start = Offset(0f, size.height),
            end = Offset(cornerLength, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = borderColor,
            start = Offset(0f, size.height),
            end = Offset(0f, size.height - cornerLength),
            strokeWidth = strokeWidth
        )
        
        // Bottom-right corner
        drawLine(
            color = borderColor,
            start = Offset(size.width, size.height),
            end = Offset(size.width - cornerLength, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = borderColor,
            start = Offset(size.width, size.height),
            end = Offset(size.width, size.height - cornerLength),
            strokeWidth = strokeWidth
        )
        
        // Center crosshair
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val crosshairSize = 10.dp.toPx()
        
        drawLine(
            color = borderColor.copy(alpha = 0.5f),
            start = Offset(centerX - crosshairSize, centerY),
            end = Offset(centerX + crosshairSize, centerY),
            strokeWidth = 1f
        )
        drawLine(
            color = borderColor.copy(alpha = 0.5f),
            start = Offset(centerX, centerY - crosshairSize),
            end = Offset(centerX, centerY + crosshairSize),
            strokeWidth = 1f
        )
    }
}

// ============================================================================
// REGIME TIMELINE BAND - Market Regime Visualization
// ============================================================================

/**
 * Visual timeline band showing market regime changes over time
 * Color-coded segments indicate different market conditions
 * 
 * @param modifier Modifier for customization
 * @param regimeData List of regime periods with colors
 */
@Composable
fun RegimeTimelineBand(
    modifier: Modifier = Modifier,
    regimeData: List<RegimePeriod> = listOf(
        RegimePeriod("Bull", Color(0xFF00FF88), 0.3f),
        RegimePeriod("Ranging", Color(0xFFFFB347), 0.25f),
        RegimePeriod("Bear", Color(0xFFFF4444), 0.2f),
        RegimePeriod("Volatile", Color(0xFF00F0FF), 0.25f)
    )
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var currentX = 0f
            
            regimeData.forEach { period ->
                val segmentWidth = size.width * period.proportion
                
                // Draw segment with gradient
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            period.color.copy(alpha = 0.3f),
                            period.color,
                            period.color.copy(alpha = 0.3f)
                        ),
                        startX = currentX,
                        endX = currentX + segmentWidth
                    ),
                    topLeft = Offset(currentX, 0f),
                    size = Size(segmentWidth, size.height)
                )
                
                // Draw separator line
                drawLine(
                    color = Color.White.copy(alpha = 0.3f),
                    start = Offset(currentX, 0f),
                    end = Offset(currentX, size.height),
                    strokeWidth = 1f
                )
                
                currentX += segmentWidth
            }
        }
    }
}

/**
 * Data class for regime timeline periods
 */
data class RegimePeriod(
    val label: String,
    val color: Color,
    val proportion: Float
)

// ============================================================================
// PREDICTIVE HEATMAP - Pattern Forecast Confidence
// ============================================================================

/**
 * Color-coded heatmap showing pattern prediction confidence levels
 * Uses gradient from low (red) to high (green) confidence
 * Uses AnimationSpecs for centralized configuration and respects PerformanceProfile
 * 
 * @param modifier Modifier for customization
 * @param confidence Confidence level 0.0 to 1.0
 * @param showPulse Whether to animate pulsing effect for high confidence
 */
@Composable
fun PredictiveHeatmap(
    confidence: Float,
    modifier: Modifier = Modifier,
    showPulse: Boolean = true
) {
    val performanceProfile = LocalPerformanceProfile.current
    val infiniteTransition = rememberInfiniteTransition(label = "heatmapPulse")
    
    val shouldPulse = showPulse && performanceProfile.enablePulseAnimations && confidence >= 0.8f
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                (AnimationSpecs.HEATMAP_PULSE_DURATION * performanceProfile.animationFrameRateMultiplier).toInt(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsing"
    )
    
    val heatmapColor = when {
        confidence >= 0.8f -> Color(0xFF00FF88) // High - Bright Green
        confidence >= 0.6f -> Color(0xFFFFB347) // Medium - Orange
        confidence >= 0.4f -> Color(0xFFFFFF00) // Low-Med - Yellow
        else -> Color(0xFFFF4444) // Low - Red
    }
    
    val alpha = if (shouldPulse) pulseAlpha else 0.7f
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        heatmapColor.copy(alpha = alpha),
                        Color.Transparent
                    )
                )
            )
    )
}
