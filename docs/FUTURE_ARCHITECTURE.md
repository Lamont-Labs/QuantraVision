# Future Architecture: Apex-Inspired Intelligence System

**Status:** Design specification only - NOT IMPLEMENTED  
**Last Updated:** November 2025  
**Estimated Implementation:** 6-11 weeks active development

## Overview

This document details the planned Apex-inspired intelligence system for QuantraVision - a complete architectural redesign replacing template matching with institutional-grade geometric pattern detection and multi-layer validation.

**Inspiration:** QuantraCore Apex v3.7u - institutional desktop trading intelligence engine  
**Adaptation:** Mobile-optimized standalone system for offline chart analysis  
**Goal:** Transform QuantraVision from 40-60% accuracy template matching to 70-85% geometric detection with sophisticated validation

## Architectural Philosophy

### Core Principles

1. **Deterministic:** Same input → same output → same proof hash
2. **Fail-Closed:** When uncertain, reduce confidence rather than hallucinate
3. **Auditable:** Every decision logged with reasoning in proof trail
4. **Offline-First:** All intelligence on-device, zero cloud dependencies
5. **Quality > Quantity:** 15-20 reliable patterns better than 109 inconsistent ones

### Desktop Apex vs Mobile Adaptation

| Aspect | Desktop Apex | Mobile QuantraVision |
|--------|-------------|---------------------|
| **Protocols** | 80 tier + 25 learning | 15-20 essential mobile protocols |
| **Data Source** | Live market feeds (WebSocket) | Chart screenshots (MediaProjection) |
| **Learning** | High-velocity continuous | Batch updates every N scans |
| **Trading** | Full OMS + broker integration | Analysis only, no execution |
| **Hardware** | NucBox desktop (4+ cores, 8GB RAM) | Samsung S23 FE mobile |
| **Runtime** | Always-on server | On-demand scans |
| **Purpose** | Live signal generation | Screenshot pattern analysis |

## System Architecture

### High-Level Flow

```
┌─────────────┐
│ User Taps   │
│ "Scan"      │
└──────┬──────┘
       │
       v
┌─────────────────────────────────────────┐
│  MediaProjection: Capture Screenshot    │
└──────┬──────────────────────────────────┘
       │
       v
┌─────────────────────────────────────────┐
│  Geometric Pattern Detection Engine     │
│  - OpenCV contour/shape analysis        │
│  - Peak/trough extraction               │
│  - Trendline detection                  │
│  - Pattern rule matching                │
│  - Confidence scoring                   │
└──────┬──────────────────────────────────┘
       │
       v
┌─────────────────────────────────────────┐
│  OCR Indicator Extraction               │
│  - ML Kit Text Recognition              │
│  - Extract RSI, MACD, Volume, etc.      │
│  (Already implemented)                  │
└──────┬──────────────────────────────────┘
       │
       v
┌─────────────────────────────────────────┐
│  Trait Extraction                       │
│  - Convert patterns → traits            │
│  - Convert indicators → traits          │
│  - Assign confidence scores             │
└──────┬──────────────────────────────────┘
       │
       v
┌─────────────────────────────────────────┐
│  Microtrait Expansion                   │
│  - Decompose each trait into 3-8        │
│    granular microtraits                 │
│  - Enable nuanced scoring               │
└──────┬──────────────────────────────────┘
       │
       v
┌─────────────────────────────────────────┐
│  Protocol Stack (15-20 rules)           │
│  - Input validation                     │
│  - Momentum alignment                   │
│  - Volume confirmation                  │
│  - Entropy detection                    │
│  - Suppression check                    │
│  - Drift adjustment                     │
│  - Risk/reward validation               │
│  - Final verdict                        │
└──────┬──────────────────────────────────┘
       │
       v
┌─────────────────────────────────────────┐
│  Enhanced QuantraScore Calculator       │
│  - Weighted trait fusion                │
│  - Microtrait contributions             │
│  - Entropy penalty                      │
│  - Suppression penalty                  │
│  - Drift multiplier                     │
│  - Normalize to 0-100                   │
└──────┬──────────────────────────────────┘
       │
       v
┌─────────────────────────────────────────┐
│  Hybrid Explanation Generator           │
│  - Fast: Template-based (simple cases)  │
│  - Smart: Gemma 2B LLM (complex cases)  │
│  - Plain English trade recommendations  │
└──────┬──────────────────────────────────┘
       │
       v
┌─────────────────────────────────────────┐
│  Proof Logger                           │
│  - Hash input + decisions + output      │
│  - Store to Room database               │
│  - Enable audit trail                   │
└──────┬──────────────────────────────────┘
       │
       v
┌─────────────────────────────────────────┐
│  Display Result to User                 │
│  - Pattern names                        │
│  - QuantraScore breakdown               │
│  - Trait/microtrait contributions       │
│  - Protocol verdicts                    │
│  - Explanation                          │
│  - Entry/stop/target levels             │
└─────────────────────────────────────────┘
```

## Component Specifications

### 1. Geometric Pattern Detection Engine

**File Structure:**
```
app/src/main/java/com/lamontlabs/quantravision/detection/geometric/
├── GeometricPatternDetector.kt       # Main orchestrator
├── ContourAnalyzer.kt                # OpenCV shape extraction
├── PeakTroughFinder.kt               # Local extrema detection
├── TrendlineDetector.kt              # Support/resistance lines
├── patterns/
│   ├── HeadAndShouldersDetector.kt   # Specific pattern implementations
│   ├── DoubleTopDetector.kt
│   ├── TriangleDetector.kt
│   ├── FlagDetector.kt
│   └── ... (15-20 total)
└── PatternConfidenceScorer.kt        # Quality validation
```

**Core Algorithms:**

**Peak/Trough Detection:**
```kotlin
class PeakTroughFinder {
    fun findPeaks(
        prices: List<Float>, 
        windowSize: Int = 5
    ): List<Point> {
        val peaks = mutableListOf<Point>()
        
        for (i in windowSize until prices.size - windowSize) {
            val isPeak = (i-windowSize..i+windowSize).all { j ->
                j == i || prices[i] >= prices[j]
            }
            
            if (isPeak) {
                peaks.add(Point(i, prices[i]))
            }
        }
        
        return peaks
    }
    
    fun findTroughs(prices: List<Float>, windowSize: Int = 5): List<Point>
    // Similar logic, find local minima
}
```

**Head & Shoulders Pattern Detection:**
```kotlin
class HeadAndShouldersDetector : PatternDetector {
    override fun detect(chart: ChartData): Pattern? {
        val peaks = peakFinder.findPeaks(chart.prices)
        val troughs = peakFinder.findTroughs(chart.prices)
        
        if (peaks.size < 3 || troughs.size < 2) return null
        
        // Find candidate H&S formations
        for (i in 1 until peaks.size - 1) {
            val leftShoulder = peaks[i - 1]
            val head = peaks[i]
            val rightShoulder = peaks[i + 1]
            
            // Validation rules
            val headIsHighest = 
                head.y > leftShoulder.y * 1.05 && 
                head.y > rightShoulder.y * 1.05
            
            val shouldersSymmetric = 
                abs(leftShoulder.y - rightShoulder.y) / leftShoulder.y < 0.1
            
            val neckline = findNeckline(troughs, leftShoulder, rightShoulder)
            val necklineValid = neckline != null
            
            if (headIsHighest && shouldersSymmetric && necklineValid) {
                val confidence = calculateConfidence(
                    headProminence = (head.y - leftShoulder.y) / leftShoulder.y,
                    shoulderSymmetry = abs(leftShoulder.y - rightShoulder.y) / leftShoulder.y,
                    necklineClean = neckline!!.r2Score
                )
                
                return Pattern(
                    type = PatternType.HEAD_AND_SHOULDERS,
                    confidence = confidence,
                    keyPoints = mapOf(
                        "left_shoulder" to leftShoulder,
                        "head" to head,
                        "right_shoulder" to rightShoulder,
                        "neckline" to neckline
                    ),
                    metadata = mapOf(
                        "target" to calculateTarget(head, neckline),
                        "stop" to head.y * 1.02
                    )
                )
            }
        }
        
        return null
    }
    
    private fun calculateConfidence(
        headProminence: Float,
        shoulderSymmetry: Float,
        necklineClean: Float
    ): Float {
        // Weighted scoring
        val prominenceScore = (headProminence * 10).coerceIn(0f, 1f)
        val symmetryScore = (1f - shoulderSymmetry * 10).coerceIn(0f, 1f)
        val necklineScore = necklineClean
        
        return (prominenceScore * 0.4f + 
                symmetryScore * 0.3f + 
                necklineScore * 0.3f).coerceIn(0f, 1f)
    }
}
```

**Target Patterns:**

1. **Head & Shoulders** (Reversal)
   - 3 peaks: center highest
   - 2 troughs forming neckline
   - Declining volume on right shoulder ideal

2. **Double Top/Bottom** (Reversal)
   - 2 similar peaks/troughs
   - Valley/peak between them
   - Neckline break confirms

3. **Triangles** (Continuation/Breakout)
   - Ascending: rising support + flat resistance
   - Descending: flat support + falling resistance
   - Symmetrical: converging trendlines

4. **Flags & Pennants** (Continuation)
   - Sharp move (flagpole)
   - Consolidation (flag/pennant)
   - Breakout in original direction

5. **Wedges** (Reversal/Continuation)
   - Rising wedge: converging upward lines (bearish)
   - Falling wedge: converging downward lines (bullish)

6. **Channels** (Continuation)
   - Parallel support/resistance
   - Trade bounces within channel

7. **Cup & Handle** (Continuation)
   - U-shaped cup
   - Small consolidation (handle)
   - Bullish breakout

8. **Support/Resistance** (Zones)
   - Horizontal price levels
   - Multiple touches
   - Breakout/bounce opportunities

(Total: 15-20 core patterns)

### 2. Trait & Microtrait System

**Data Structures:**

```kotlin
enum class TraitType {
    // Pattern-based
    REVERSAL_PATTERN,
    CONTINUATION_PATTERN,
    BREAKOUT_PATTERN,
    
    // Indicator-based
    MOMENTUM_INDICATOR,
    VOLATILITY_INDICATOR,
    VOLUME_INDICATOR,
    TREND_INDICATOR,
    
    // Context
    SUPPORT_RESISTANCE,
    CHART_STRUCTURE
}

enum class TraitSource {
    GEOMETRIC_DETECTION,
    OCR_EXTRACTION,
    CHART_ANALYSIS
}

data class Trait(
    val id: String,
    val type: TraitType,
    val name: String,                    // "head_and_shoulders", "rsi_overbought"
    val confidence: Float,               // 0.0 - 1.0
    val source: TraitSource,
    val timestamp: Long,
    val metadata: Map<String, Any> = emptyMap()
)

data class Microtrait(
    val id: String,
    val parentTraitId: String,
    val name: String,                    // "h&s_right_shoulder_weak_volume"
    val weight: Float,                   // Learned from historical performance
    val value: Float,                    // 0.0 - 1.0
    val description: String
)
```

**TraitExtractor:**

```kotlin
class TraitExtractor(
    private val geometricDetector: GeometricPatternDetector,
    private val indicatorExtractor: IndicatorExtractor  // Existing OCR system
) {
    suspend fun extractTraits(screenshot: Bitmap): List<Trait> {
        val traits = mutableListOf<Trait>()
        
        // Pattern-based traits
        val patterns = geometricDetector.detectPatterns(screenshot)
        patterns.forEach { pattern ->
            traits.add(Trait(
                id = UUID.randomUUID().toString(),
                type = pattern.category.toTraitType(),
                name = pattern.type.name.lowercase(),
                confidence = pattern.confidence,
                source = TraitSource.GEOMETRIC_DETECTION,
                timestamp = System.currentTimeMillis(),
                metadata = pattern.keyPoints
            ))
        }
        
        // Indicator-based traits (using existing OCR system)
        val indicators = indicatorExtractor.extractIndicators(screenshot)
        
        indicators.rsi?.let { rsi ->
            traits.add(Trait(
                id = UUID.randomUUID().toString(),
                type = TraitType.MOMENTUM_INDICATOR,
                name = when {
                    rsi >= 70 -> "rsi_overbought"
                    rsi <= 30 -> "rsi_oversold"
                    else -> "rsi_neutral"
                },
                confidence = 0.95f,  // OCR confidence
                source = TraitSource.OCR_EXTRACTION,
                timestamp = System.currentTimeMillis(),
                metadata = mapOf("value" to rsi)
            ))
        }
        
        indicators.macd?.let { macd ->
            traits.add(Trait(
                id = UUID.randomUUID().toString(),
                type = TraitType.MOMENTUM_INDICATOR,
                name = when {
                    macd.histogram > 0 -> "macd_bullish"
                    macd.histogram < 0 -> "macd_bearish"
                    else -> "macd_neutral"
                },
                confidence = 0.90f,
                source = TraitSource.OCR_EXTRACTION,
                timestamp = System.currentTimeMillis(),
                metadata = mapOf(
                    "histogram" to macd.histogram,
                    "signal" to macd.signal
                )
            ))
        }
        
        indicators.volume?.let { volume ->
            traits.add(Trait(
                id = UUID.randomUUID().toString(),
                type = TraitType.VOLUME_INDICATOR,
                name = when {
                    volume.relativeStrength > 1.5 -> "volume_high"
                    volume.relativeStrength < 0.5 -> "volume_low"
                    else -> "volume_normal"
                },
                confidence = 0.85f,
                source = TraitSource.OCR_EXTRACTION,
                timestamp = System.currentTimeMillis(),
                metadata = mapOf("relative" to volume.relativeStrength)
            ))
        }
        
        // Additional traits from chart structure, support/resistance, etc.
        
        return traits
    }
}
```

**MicrotraitExpander:**

```kotlin
class MicrotraitExpander(
    private val weightsConfig: MicrotraitWeightsConfig
) {
    fun expandTraits(traits: List<Trait>): List<Microtrait> {
        val microtraits = mutableListOf<Microtrait>()
        
        traits.forEach { trait ->
            microtraits.addAll(expandTrait(trait))
        }
        
        return microtraits
    }
    
    private fun expandTrait(trait: Trait): List<Microtrait> {
        return when (trait.name) {
            "head_and_shoulders" -> expandHeadAndShoulders(trait)
            "rsi_overbought" -> expandRSIOverbought(trait)
            "macd_bearish" -> expandMACDBearish(trait)
            "volume_high" -> expandVolumeHigh(trait)
            // ... other trait expansions
            else -> emptyList()
        }
    }
    
    private fun expandHeadAndShoulders(trait: Trait): List<Microtrait> {
        val microtraits = mutableListOf<Microtrait>()
        val metadata = trait.metadata
        
        // Head prominence
        val head = metadata["head"] as? Point
        val leftShoulder = metadata["left_shoulder"] as? Point
        if (head != null && leftShoulder != null) {
            val prominence = (head.y - leftShoulder.y) / leftShoulder.y
            microtraits.add(Microtrait(
                id = UUID.randomUUID().toString(),
                parentTraitId = trait.id,
                name = "h&s_head_prominence",
                weight = weightsConfig.getWeight("h&s_head_prominence"),
                value = (prominence * 5).coerceIn(0f, 1f),  // Normalize
                description = "Head is ${(prominence * 100).toInt()}% higher than shoulders"
            ))
        }
        
        // Shoulder symmetry
        val rightShoulder = metadata["right_shoulder"] as? Point
        if (leftShoulder != null && rightShoulder != null) {
            val symmetry = 1f - abs(leftShoulder.y - rightShoulder.y) / leftShoulder.y
            microtraits.add(Microtrait(
                id = UUID.randomUUID().toString(),
                parentTraitId = trait.id,
                name = "h&s_shoulder_symmetry",
                weight = weightsConfig.getWeight("h&s_shoulder_symmetry"),
                value = symmetry,
                description = "Shoulders are ${(symmetry * 100).toInt()}% symmetric"
            ))
        }
        
        // Neckline quality
        val neckline = metadata["neckline"] as? Trendline
        neckline?.let {
            microtraits.add(Microtrait(
                id = UUID.randomUUID().toString(),
                parentTraitId = trait.id,
                name = "h&s_neckline_clean",
                weight = weightsConfig.getWeight("h&s_neckline_clean"),
                value = it.r2Score,
                description = "Neckline fits trough points well (R²=${it.r2Score})"
            ))
        }
        
        // Target ratio
        val target = metadata["target"] as? Float
        val currentPrice = metadata["current_price"] as? Float
        if (target != null && currentPrice != null) {
            val potentialMove = abs(target - currentPrice) / currentPrice
            microtraits.add(Microtrait(
                id = UUID.randomUUID().toString(),
                parentTraitId = trait.id,
                name = "h&s_target_ratio",
                weight = weightsConfig.getWeight("h&s_target_ratio"),
                value = (potentialMove * 5).coerceIn(0f, 1f),
                description = "Measured move suggests ${(potentialMove * 100).toInt()}% potential"
            ))
        }
        
        return microtraits
    }
    
    private fun expandRSIOverbought(trait: Trait): List<Microtrait> {
        val microtraits = mutableListOf<Microtrait>()
        val rsiValue = trait.metadata["value"] as? Float ?: return emptyList()
        
        // Distance from overbought threshold
        microtraits.add(Microtrait(
            id = UUID.randomUUID().toString(),
            parentTraitId = trait.id,
            name = "rsi_approaching_extreme",
            weight = weightsConfig.getWeight("rsi_approaching_extreme"),
            value = ((rsiValue - 70) / 30).coerceIn(0f, 1f),  // 70-100 range
            description = "RSI at $rsiValue is ${rsiValue - 70} points into overbought"
        ))
        
        // Check for divergence (requires price data)
        val divergence = detectRSIDivergence(trait.metadata)
        if (divergence != null) {
            microtraits.add(Microtrait(
                id = UUID.randomUUID().toString(),
                parentTraitId = trait.id,
                name = "rsi_bearish_divergence",
                weight = weightsConfig.getWeight("rsi_bearish_divergence"),
                value = divergence.strength,
                description = "Price making higher highs while RSI makes lower highs"
            ))
        }
        
        return microtraits
    }
    
    // Similar expansion methods for other traits...
}
```

**Weights Configuration:**

```json
{
  "version": "1.0",
  "last_updated": "2025-11-21",
  "microtraits": {
    "h&s_head_prominence": 0.35,
    "h&s_shoulder_symmetry": 0.25,
    "h&s_neckline_clean": 0.20,
    "h&s_target_ratio": 0.15,
    "h&s_volume_confirmation": 0.25,
    
    "rsi_approaching_extreme": 0.40,
    "rsi_bearish_divergence": 0.60,
    
    "macd_histogram_strength": 0.50,
    "macd_signal_cross": 0.30,
    "macd_centerline_position": 0.20,
    
    "volume_relative_strength": 0.60,
    "volume_trend_confirmation": 0.40
  },
  "learning_enabled": true,
  "update_frequency_scans": 50
}
```

### 3. Mobile Protocol Stack

**Protocol Interface:**

```kotlin
interface Protocol {
    val id: String              // "P01", "P03", etc.
    val name: String
    val version: String
    val description: String
    
    /**
     * Apply protocol logic to signal context
     * @param context Mutable signal context with traits, scores, state
     * @return Result with pass/fail, score modifier, reasoning
     */
    fun apply(context: SignalContext): ProtocolResult
}

data class SignalContext(
    // Input data
    val screenshot: Bitmap,
    val traits: List<Trait>,
    val microtraits: List<Microtrait>,
    val timestamp: Long,
    
    // Mutable state (modified by protocols)
    var score: Float = 0f,
    var entropy: Float = 0f,
    var suppression: Float = 0f,
    var drift: Float = 1f,
    
    // Audit trail
    val trace: MutableList<ProtocolDecision> = mutableListOf(),
    
    // Configuration
    val config: ProtocolConfig
)

data class ProtocolResult(
    val passed: Boolean,
    val scoreModifier: Float,        // Multiplier (0.5 = 50% reduction, 1.5 = 50% boost)
    val reason: String,
    val metadata: Map<String, Any> = emptyMap()
)

data class ProtocolDecision(
    val protocolId: String,
    val protocolName: String,
    val timestamp: Long,
    val passed: Boolean,
    val scoreModifier: Float,
    val scoreBefore: Float,
    val scoreAfter: Float,
    val reason: String,
    val metadata: Map<String, Any> = emptyMap()
)
```

**Protocol Implementations:**

```kotlin
// P01 - Input Validation
class InputValidationProtocol : Protocol {
    override val id = "P01"
    override val name = "Input Validation"
    override val version = "1.0"
    override val description = "Ensures all required data is present and valid"
    
    override fun apply(context: SignalContext): ProtocolResult {
        // Check for minimum traits
        if (context.traits.isEmpty()) {
            return ProtocolResult(
                passed = false,
                scoreModifier = 0f,  // Zero out score
                reason = "No traits extracted - invalid input"
            )
        }
        
        // Check for minimum confidence
        val avgConfidence = context.traits.map { it.confidence }.average()
        if (avgConfidence < 0.3f) {
            return ProtocolResult(
                passed = false,
                scoreModifier = 0.5f,  // 50% penalty
                reason = "Average trait confidence too low ($avgConfidence)"
            )
        }
        
        // Validate screenshot integrity
        if (context.screenshot.width < 100 || context.screenshot.height < 100) {
            return ProtocolResult(
                passed = false,
                scoreModifier = 0f,
                reason = "Screenshot too small to analyze"
            )
        }
        
        return ProtocolResult(
            passed = true,
            scoreModifier = 1.0f,  // No change
            reason = "Input validation passed"
        )
    }
}

// P03 - Momentum Alignment
class MomentumAlignmentProtocol : Protocol {
    override val id = "P03"
    override val name = "Momentum Alignment"
    override val version = "1.0"
    override val description = "Validates pattern direction matches momentum indicators"
    
    override fun apply(context: SignalContext): ProtocolResult {
        val patternTraits = context.traits.filter { 
            it.type == TraitType.REVERSAL_PATTERN || 
            it.type == TraitType.CONTINUATION_PATTERN 
        }
        
        val momentumTraits = context.traits.filter { 
            it.type == TraitType.MOMENTUM_INDICATOR 
        }
        
        if (patternTraits.isEmpty() || momentumTraits.isEmpty()) {
            return ProtocolResult(
                passed = true,
                scoreModifier = 1.0f,
                reason = "Insufficient data for momentum alignment check"
            )
        }
        
        // Check if reversal pattern has divergence support
        val hasReversalPattern = patternTraits.any { it.name.contains("head_and_shoulders") }
        val hasDivergence = context.microtraits.any { 
            it.name.contains("divergence") && it.value > 0.5f 
        }
        
        if (hasReversalPattern && !hasDivergence) {
            return ProtocolResult(
                passed = true,
                scoreModifier = 0.8f,  // 20% penalty
                reason = "Reversal pattern without momentum divergence"
            )
        }
        
        // Check continuation pattern has momentum support
        val hasContinuation = patternTraits.any { 
            it.name.contains("flag") || it.name.contains("pennant") 
        }
        val momentumInDirection = momentumTraits.any { 
            it.name.contains("bullish") || it.name.contains("bearish")
        }
        
        if (hasContinuation && !momentumInDirection) {
            return ProtocolResult(
                passed = true,
                scoreModifier = 0.75f,  // 25% penalty
                reason = "Continuation pattern without momentum in trend direction"
            )
        }
        
        return ProtocolResult(
            passed = true,
            scoreModifier = 1.1f,  // 10% bonus for good alignment
            reason = "Momentum aligns with pattern direction"
        )
    }
}

// P07 - Entropy Controller
class EntropyControlProtocol(
    private val entropyCalculator: EntropyCalculator
) : Protocol {
    override val id = "P07"
    override val name = "Entropy Controller"
    override val version = "1.0"
    override val description = "Detects conflicting signals and applies penalties"
    
    override fun apply(context: SignalContext): ProtocolResult {
        val entropy = entropyCalculator.calculate(context.traits)
        context.entropy = entropy  // Update context
        
        when {
            entropy < 0.3f -> {
                return ProtocolResult(
                    passed = true,
                    scoreModifier = 1.0f,
                    reason = "Low entropy - signals align well",
                    metadata = mapOf("entropy" to entropy)
                )
            }
            entropy < 0.6f -> {
                return ProtocolResult(
                    passed = true,
                    scoreModifier = 0.85f,  // 15% penalty
                    reason = "Moderate entropy - some signal conflict",
                    metadata = mapOf("entropy" to entropy)
                )
            }
            else -> {
                return ProtocolResult(
                    passed = true,
                    scoreModifier = 0.6f,  // 40% penalty
                    reason = "High entropy - significant signal conflict",
                    metadata = mapOf("entropy" to entropy)
                )
            }
        }
    }
}

// P13 - Suppression Check
class SuppressionCheckProtocol(
    private val suppressionFilter: SuppressionFilter
) : Protocol {
    override val id = "P13"
    override val name = "Suppression Check"
    override val version = "1.0"
    override val description = "Reduces score for patterns similar to recent false positives"
    
    override fun apply(context: SignalContext): ProtocolResult = runBlocking {
        val suppressionPenalty = suppressionFilter.checkSuppression(context.traits)
        context.suppression = suppressionPenalty
        
        if (suppressionPenalty < 0.1f) {
            return@runBlocking ProtocolResult(
                passed = true,
                scoreModifier = 1.0f,
                reason = "No suppression - pattern not flagged",
                metadata = mapOf("suppression" to suppressionPenalty)
            )
        }
        
        val modifier = 1.0f - suppressionPenalty
        return@runBlocking ProtocolResult(
            passed = true,
            scoreModifier = modifier,
            reason = "Pattern suppressed ${(suppressionPenalty * 100).toInt()}% due to recent false positives",
            metadata = mapOf("suppression" to suppressionPenalty)
        )
    }
}

// P20 - Final Verdict
class FinalVerdictProtocol : Protocol {
    override val id = "P20"
    override val name = "Final Verdict"
    override val version = "1.0"
    override val description = "Generates final score normalization and verdict"
    
    override fun apply(context: SignalContext): ProtocolResult {
        // Normalize score to 0-100 range
        val normalizedScore = (context.score * 100).coerceIn(0f, 100f)
        context.score = normalizedScore
        
        val verdict = when {
            normalizedScore >= 80 -> "STRONG_BULLISH/BEARISH"
            normalizedScore >= 65 -> "MODERATE_BULLISH/BEARISH"
            normalizedScore >= 55 -> "WEAK_BULLISH/BEARISH"
            normalizedScore >= 45 -> "NEUTRAL"
            else -> "LOW_CONFIDENCE"
        }
        
        return ProtocolResult(
            passed = true,
            scoreModifier = 1.0f,
            reason = "Final score: $normalizedScore/100 - Verdict: $verdict",
            metadata = mapOf(
                "final_score" to normalizedScore,
                "verdict" to verdict
            )
        )
    }
}
```

**Protocol Engine:**

```kotlin
class ProtocolEngine(
    private val protocols: List<Protocol>
) {
    fun process(context: SignalContext): SignalContext {
        protocols.forEach { protocol ->
            val scoreBefore = context.score
            val result = protocol.apply(context)
            
            // Apply score modification
            context.score *= result.scoreModifier
            
            // Record decision in audit trail
            context.trace.add(ProtocolDecision(
                protocolId = protocol.id,
                protocolName = protocol.name,
                timestamp = System.currentTimeMillis(),
                passed = result.passed,
                scoreModifier = result.scoreModifier,
                scoreBefore = scoreBefore,
                scoreAfter = context.score,
                reason = result.reason,
                metadata = result.metadata
            ))
            
            // Fail-closed: if protocol blocks, stop processing
            if (!result.passed && result.scoreModifier == 0f) {
                Timber.w("Protocol ${protocol.id} blocked signal: ${result.reason}")
                break
            }
        }
        
        return context
    }
}
```

**Full Protocol List (15-20):**

1. P01 - Input Validation
2. P03 - Momentum Alignment
3. P05 - Volume Confirmation
4. P07 - Entropy Controller
5. P09 - Multi-Timeframe Consistency (if supported)
6. P11 - Sector/Market Context (advanced)
7. P13 - Suppression Check
8. P15 - Drift Adjustment
9. P17 - Continuation Validator
10. P19 - Risk/Reward Validator
11. P20 - Final Verdict

(Expand to 15-20 based on specific needs)

### 4. Enhanced QuantraScore Calculator

```kotlin
class ApexQuantraScoreCalculator(
    private val traitWeights: TraitWeightsConfig,
    private val microtraitWeights: MicrotraitWeightsConfig
) {
    fun calculate(context: SignalContext): QuantraScore {
        // 1. Base trait scoring (weighted sum)
        val traitScore = context.traits.sumOf { trait ->
            (trait.confidence * traitWeights.getWeight(trait.type)).toDouble()
        }.toFloat()
        
        // 2. Microtrait contributions
        val microtraitBonus = context.microtraits.sumOf { microtrait ->
            (microtrait.value * microtrait.weight).toDouble()
        }.toFloat()
        
        // 3. Combine base + microtraits
        val rawScore = traitScore + microtraitBonus
        
        // 4. Apply protocol modifiers (already applied in ProtocolEngine)
        // context.score contains post-protocol score
        
        // 5. Calculate confidence
        val confidence = calculateConfidence(context)
        
        // 6. Determine verdict
        val verdict = determineVerdict(context.score)
        
        // 7. Generate component breakdown
        val breakdown = ScoreBreakdown(
            traitContribution = traitScore,
            microtraitContribution = microtraitBonus,
            entropyPenalty = 1.0f - (context.entropy * 0.3f),
            suppressionPenalty = 1.0f - context.suppression,
            driftAdjustment = context.drift,
            protocolModifiers = context.trace.map { 
                it.protocolId to it.scoreModifier 
            }.toMap()
        )
        
        // 8. Generate reasoning
        val reasoning = generateReasoning(context, verdict)
        
        return QuantraScore(
            value = context.score,
            confidence = confidence,
            verdict = verdict,
            components = breakdown,
            reasoning = reasoning,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun calculateConfidence(context: SignalContext): Float {
        // Factors affecting confidence:
        // 1. Average trait confidence
        val avgTraitConfidence = context.traits.map { it.confidence }.average().toFloat()
        
        // 2. Number of supporting signals
        val signalCount = context.traits.size
        val countScore = (signalCount / 10f).coerceIn(0f, 1f)  // More signals = higher confidence
        
        // 3. Entropy (low entropy = high confidence)
        val entropyScore = 1.0f - context.entropy
        
        // 4. Protocol pass rate
        val protocolPassRate = context.trace.count { it.passed }.toFloat() / context.trace.size
        
        // Weighted combination
        return (avgTraitConfidence * 0.4f +
                countScore * 0.2f +
                entropyScore * 0.2f +
                protocolPassRate * 0.2f).coerceIn(0f, 1f)
    }
    
    private fun determineVerdict(score: Float): Verdict {
        return when {
            score >= 80 -> {
                // Determine direction from traits
                val bullishSignals = /* count bullish traits */
                val bearishSignals = /* count bearish traits */
                if (bullishSignals > bearishSignals) Verdict.STRONG_BULLISH 
                else Verdict.STRONG_BEARISH
            }
            score >= 65 -> {
                if (/* bullish */) Verdict.MODERATE_BULLISH 
                else Verdict.MODERATE_BEARISH
            }
            score >= 55 -> {
                if (/* bullish */) Verdict.WEAK_BULLISH 
                else Verdict.WEAK_BEARISH
            }
            else -> Verdict.NEUTRAL
        }
    }
    
    private fun generateReasoning(context: SignalContext, verdict: Verdict): String {
        val sb = StringBuilder()
        
        // Top contributing traits
        val topTraits = context.traits.sortedByDescending { it.confidence }.take(3)
        sb.append("Key signals: ")
        sb.append(topTraits.joinToString(", ") { it.name.replace("_", " ") })
        sb.append(". ")
        
        // Verdict interpretation
        sb.append("Overall ${verdict.name.replace("_", " ").lowercase()} setup. ")
        
        // Entropy context
        when {
            context.entropy > 0.6f -> sb.append("High signal conflict reduces confidence. ")
            context.entropy > 0.3f -> sb.append("Some conflicting signals present. ")
        }
        
        // Suppression context
        if (context.suppression > 0.2f) {
            sb.append("Similar pattern has failed recently - proceed with caution. ")
        }
        
        // Drift context
        if (context.drift < 0.8f) {
            sb.append("Pattern effectiveness has declined recently. ")
        }
        
        return sb.toString()
    }
}

data class QuantraScore(
    val value: Float,                    // 0-100
    val confidence: Float,               // 0-1
    val verdict: Verdict,
    val components: ScoreBreakdown,
    val reasoning: String,
    val timestamp: Long
)

enum class Verdict {
    STRONG_BULLISH,
    MODERATE_BULLISH,
    WEAK_BULLISH,
    NEUTRAL,
    WEAK_BEARISH,
    MODERATE_BEARISH,
    STRONG_BEARISH
}

data class ScoreBreakdown(
    val traitContribution: Float,
    val microtraitContribution: Float,
    val entropyPenalty: Float,
    val suppressionPenalty: Float,
    val driftAdjustment: Float,
    val protocolModifiers: Map<String, Float>
)
```

### 5. Hybrid Explanation System

See FUTURE_ARCHITECTURE.md section "6. Hybrid Explanation System" for complete implementation details.

### 6. Proof Logging System

See FUTURE_ARCHITECTURE.md section "7. Deterministic Proof Logging" for complete implementation details.

## Performance Targets

**Scan-to-Result Time:**
- Geometric detection: 500-1000ms
- OCR extraction: 300-500ms (existing system)
- Trait/microtrait expansion: 50-100ms
- Protocol stack execution: 100-200ms
- QuantraScore calculation: 50ms
- Template explanation: <50ms
- **Total (fast path): 1-2 seconds**

**LLM Explanation (smart path):**
- Gemma 2B inference: 10-30 seconds (acceptable for complex cases)

**Memory Usage:**
- Base app: ~200MB
- Gemma 2B loaded: ~1GB
- Total peak: ~1.2GB (within S23 FE 8GB RAM)

**Storage:**
- Base app: ~300MB
- Gemma 2B model: ~800MB
- Total: ~1.1GB initial install
- Proof logs: ~10MB per 1000 scans (grows slowly)

## Testing Strategy

### Unit Tests

- Each pattern detector algorithm
- Trait extraction logic
- Microtrait expansion rules
- Each protocol implementation
- QuantraScore calculation
- Entropy/suppression/drift calculators

### Integration Tests

- Full scan flow (screenshot → result)
- Protocol stack execution order
- Database operations (suppression, proof logs)
- LLM integration (mock for speed)

### Accuracy Validation

**Geometric Detection:**
- Curated test set of 100+ chart screenshots
- Manual ground truth labels
- Target: 70-85% precision/recall
- Test across multiple platforms (TradingView, Robinhood, Webull, etc.)

**QuantraScore Reliability:**
- Compare to manual trader analysis
- Validate that high scores correlate with successful setups
- Requires real-world usage data

**Explanation Quality:**
- Human evaluation of clarity and actionability
- A/B testing template vs LLM explanations

## Implementation Priority

**Phase 1 (Must-Have):**
1. Geometric detection for 10 core patterns
2. Trait extraction
3. Basic protocol stack (5-7 protocols)
4. Simple QuantraScore
5. Template explanations

**Phase 2 (Important):**
6. Microtrait expansion
7. Full protocol stack (15-20)
8. Entropy/suppression systems
9. Enhanced QuantraScore
10. Proof logging

**Phase 3 (Nice-to-Have):**
11. Drift tracking
12. LLM integration (Gemma 2B)
13. Advanced UI (score breakdown, protocol trace viewer)

**Phase 4 (Optional):**
14. Additional patterns (15-20 total)
15. Monster-runner predictor
16. Performance optimizations

## Open Questions & Risks

**Accuracy:**
- Will geometric detection actually hit 70-85% on real-world diverse charts?
- How much training data needed to tune pattern rules?

**Performance:**
- Can we keep scan time under 3 seconds on midrange devices?
- Will Gemma 2B inference be fast enough on mobile?

**User Experience:**
- Will traders understand and trust the Apex methodology?
- Is proof logging valuable or just complexity?

**Market:**
- Will the added sophistication justify development time?
- Does market care about institutional-grade intelligence?

## Conclusion

This architecture represents a complete transformation of QuantraVision from basic template matching to institutional-grade pattern detection with multi-layer validation. It's inspired by QuantraCore Apex but adapted for mobile offline operation.

**Status:** Design complete, implementation not started. Requires 6-11 weeks active development with consistent desktop access.

**Success depends on:**
1. Geometric detection accuracy meeting 70-85% target
2. Protocol stack providing meaningful validation
3. User trust in AI-powered analysis
4. Market willingness to pay for sophistication
5. Developer time availability

**Next Step:** If development resumes, start with Phase 1 (geometric detection + basic protocols) and validate accuracy before proceeding to advanced features.
