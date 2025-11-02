package com.lamontlabs.quantravision.planner

import android.content.Context
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.alerts.VoiceAnnouncer
import com.lamontlabs.quantravision.licensing.AdvancedFeatureGate
import com.lamontlabs.quantravision.regime.RegimeNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.abs

/**
 * PatternToPlanEngine
 * 
 * Generates EDUCATIONAL trade scenarios based on detected patterns.
 * 
 * ⚠️ CRITICAL LEGAL NOTICE ⚠️
 * This is an EDUCATIONAL TOOL ONLY. All scenarios are hypothetical examples
 * for learning technical analysis concepts. NOT financial advice, NOT trading
 * recommendations, NOT personalized investment strategies.
 * 
 * See legal/ADVANCED_FEATURES_DISCLAIMER.md for full legal terms.
 * 
 * Features:
 * - Theoretical entry/exit price calculations
 * - Educational stop loss levels (pattern invalidation)
 * - Measured move targets (theoretical take profit)
 * - Position sizing examples (1% risk calculation)
 * - Risk:Reward ratio analysis
 * - Voice announcements with disclaimers
 * 
 * All outputs include PROMINENT disclaimers and are prefixed with
 * "📚 Educational Scenario:"
 */
class PatternToPlanEngine(private val context: Context) {

    private val voiceAnnouncer: VoiceAnnouncer by lazy { VoiceAnnouncer(context) }
    
    /**
     * Educational trade scenario
     * 
     * Contains hypothetical calculations for educational purposes only
     */
    data class TradeScenario(
        val patternMatch: PatternMatch,
        val entryPrice: Double,
        val stopLoss: Double,
        val takeProfit: Double,
        val positionSizeExample: Double,
        val riskRewardRatio: Double,
        val accountRiskPercent: Double = 1.0,
        val educationalContext: String,
        val disclaimer: String = "⚠️ EDUCATIONAL SCENARIO - NOT TRADING ADVICE",
        val regimeContext: RegimeNavigator.MarketRegime? = null,
        val scenarioType: ScenarioType = ScenarioType.MODERATE
    )
    
    /**
     * Scenario variations for different risk appetites
     */
    data class ScenarioVariations(
        val conservative: TradeScenario,
        val moderate: TradeScenario,
        val aggressive: TradeScenario,
        val educationalContext: String = "📚 These are EDUCATIONAL examples showing different risk approaches. NOT recommendations."
    )
    
    enum class ScenarioType {
        CONSERVATIVE,  // Tighter stops, smaller R:R, higher win rate targets
        MODERATE,      // Balanced approach
        AGGRESSIVE     // Wider stops, larger R:R, swing for bigger gains
    }
    
    /**
     * Generate educational trade scenario from pattern match
     * 
     * ⚠️ REQUIRES: User must accept Advanced Features Disclaimer first
     * 
     * @param patternMatch Detected pattern to analyze
     * @param currentPrice Current market price (for calculations)
     * @param accountSize Example account size for position sizing (default $10,000)
     * @param riskPercent Risk percentage for position sizing (default 1%)
     * @param priceData Recent price data for regime analysis (optional)
     * @return Educational trade scenario
     * @throws IllegalStateException if user hasn't accepted disclaimer
     */
    suspend fun generateScenario(
        patternMatch: PatternMatch,
        currentPrice: Double,
        accountSize: Double = 10000.0,
        riskPercent: Double = 1.0,
        priceData: List<Double>? = null
    ): TradeScenario = withContext(Dispatchers.Default) {
        
        AdvancedFeatureGate.requireAcceptance(context, "Pattern-to-Plan Engine")
        
        try {
            val regime = priceData?.let { RegimeNavigator.analyzeRegime(context, it) }
            
            val prices = calculateEntryExitPrices(patternMatch, currentPrice)
            val positionSize = calculatePositionSize(
                accountSize = accountSize,
                riskPercent = riskPercent,
                entryPrice = prices.entry,
                stopLoss = prices.stopLoss
            )
            
            val rrRatio = calculateRiskRewardRatio(
                entry = prices.entry,
                stopLoss = prices.stopLoss,
                takeProfit = prices.takeProfit
            )
            
            val context = generateEducationalContext(
                patternMatch = patternMatch,
                prices = prices,
                positionSize = positionSize,
                rrRatio = rrRatio,
                regime = regime
            )
            
            TradeScenario(
                patternMatch = patternMatch,
                entryPrice = prices.entry,
                stopLoss = prices.stopLoss,
                takeProfit = prices.takeProfit,
                positionSizeExample = positionSize,
                riskRewardRatio = rrRatio,
                accountRiskPercent = riskPercent,
                educationalContext = context,
                regimeContext = regime
            )
            
        } catch (e: Exception) {
            Timber.e(e, "Error generating trade scenario")
            throw e
        }
    }
    
    /**
     * Generate multiple scenario variations (conservative, moderate, aggressive)
     * 
     * ⚠️ REQUIRES: User must accept Advanced Features Disclaimer first
     * 
     * @param patternMatch Detected pattern to analyze
     * @param currentPrice Current market price
     * @param accountSize Example account size (default $10,000)
     * @param priceData Recent price data for regime analysis (optional)
     * @return Three educational scenarios with different risk profiles
     * @throws IllegalStateException if user hasn't accepted disclaimer
     */
    suspend fun generateScenarioVariations(
        patternMatch: PatternMatch,
        currentPrice: Double,
        accountSize: Double = 10000.0,
        priceData: List<Double>? = null
    ): ScenarioVariations = withContext(Dispatchers.Default) {
        
        AdvancedFeatureGate.requireAcceptance(context, "Pattern-to-Plan Engine")
        
        try {
            val regime = priceData?.let { RegimeNavigator.analyzeRegime(context, it) }
            
            val conservativeScenario = generateScenarioForType(
                patternMatch, currentPrice, accountSize, ScenarioType.CONSERVATIVE, regime
            )
            
            val moderateScenario = generateScenarioForType(
                patternMatch, currentPrice, accountSize, ScenarioType.MODERATE, regime
            )
            
            val aggressiveScenario = generateScenarioForType(
                patternMatch, currentPrice, accountSize, ScenarioType.AGGRESSIVE, regime
            )
            
            ScenarioVariations(
                conservative = conservativeScenario,
                moderate = moderateScenario,
                aggressive = aggressiveScenario
            )
            
        } catch (e: Exception) {
            Timber.e(e, "Error generating scenario variations")
            throw e
        }
    }
    
    private suspend fun generateScenarioForType(
        patternMatch: PatternMatch,
        currentPrice: Double,
        accountSize: Double,
        scenarioType: ScenarioType,
        regime: RegimeNavigator.MarketRegime?
    ): TradeScenario = withContext(Dispatchers.Default) {
        
        val multipliers = when (scenarioType) {
            ScenarioType.CONSERVATIVE -> Triple(0.7, 1.3, 0.5)  // tighter entry, wider stop, smaller target
            ScenarioType.MODERATE -> Triple(1.0, 1.0, 1.0)      // standard
            ScenarioType.AGGRESSIVE -> Triple(1.3, 0.8, 1.8)    // wider entry, tighter stop, larger target
        }
        
        val riskPercent = when (scenarioType) {
            ScenarioType.CONSERVATIVE -> 0.5
            ScenarioType.MODERATE -> 1.0
            ScenarioType.AGGRESSIVE -> 2.0
        }
        
        val basePrices = calculateEntryExitPrices(patternMatch, currentPrice)
        
        val entry = basePrices.entry
        val stopLoss = when (scenarioType) {
            ScenarioType.CONSERVATIVE -> basePrices.entry + (basePrices.stopLoss - basePrices.entry) * multipliers.second
            ScenarioType.MODERATE -> basePrices.stopLoss
            ScenarioType.AGGRESSIVE -> basePrices.entry + (basePrices.stopLoss - basePrices.entry) * multipliers.second
        }
        val takeProfit = basePrices.entry + (basePrices.takeProfit - basePrices.entry) * multipliers.third
        
        val positionSize = calculatePositionSize(accountSize, riskPercent, entry, stopLoss)
        val rrRatio = calculateRiskRewardRatio(entry, stopLoss, takeProfit)
        
        val typeDesc = when (scenarioType) {
            ScenarioType.CONSERVATIVE -> "Conservative (Lower risk, higher win probability target)"
            ScenarioType.MODERATE -> "Moderate (Balanced risk/reward)"
            ScenarioType.AGGRESSIVE -> "Aggressive (Higher risk, larger profit potential)"
        }
        
        val educationalContext = buildString {
            append("$typeDesc\n")
            append("This scenario demonstrates a $scenarioType risk approach. ")
            if (rrRatio >= 2.0) {
                append("R:R ${formatRatio(rrRatio)} is favorable. ")
            } else if (rrRatio < 1.5) {
                append("R:R ${formatRatio(rrRatio)} is below typical 2:1 minimum. ")
            }
            regime?.let {
                append("Market: ${it.overallQuality}. ")
            }
            append("\n\nEDUCATIONAL ONLY - NOT advice.")
        }
        
        TradeScenario(
            patternMatch = patternMatch,
            entryPrice = entry,
            stopLoss = stopLoss,
            takeProfit = takeProfit,
            positionSizeExample = positionSize,
            riskRewardRatio = rrRatio,
            accountRiskPercent = riskPercent,
            educationalContext = educationalContext,
            regimeContext = regime,
            scenarioType = scenarioType
        )
    }
    
    /**
     * Announce scenario via voice with legal disclaimers
     * 
     * @param scenario Educational trade scenario to announce
     * @param includeDisclaimer Include full disclaimer in announcement (default true)
     */
    fun announceScenario(scenario: TradeScenario, includeDisclaimer: Boolean = true) {
        if (!voiceAnnouncer.isReady()) {
            Timber.w("Voice announcer not ready")
            return
        }
        
        val message = buildString {
            append("Educational scenario. ")
            append("Entry ${formatPrice(scenario.entryPrice)}. ")
            append("Stop ${formatPrice(scenario.stopLoss)}. ")
            append("Target ${formatPrice(scenario.takeProfit)}. ")
            append("Risk reward ratio ${formatRatio(scenario.riskRewardRatio)}. ")
            
            if (includeDisclaimer) {
                append("This is NOT trading advice. ")
                append("Consult a licensed financial advisor before trading. ")
            }
        }
        
        voiceAnnouncer.speak(message, "scenario_${System.currentTimeMillis()}")
    }
    
    /**
     * Format scenario as human-readable string
     * 
     * @param scenario Educational trade scenario
     * @return Formatted string with disclaimers
     */
    fun formatScenario(scenario: TradeScenario): String {
        return buildString {
            appendLine("📚 EDUCATIONAL SCENARIO - NOT ADVICE")
            appendLine("=" .repeat(50))
            appendLine()
            appendLine("Pattern: ${scenario.patternMatch.patternName}")
            appendLine("Confidence: ${formatPercent(scenario.patternMatch.confidence)}")
            appendLine("Timeframe: ${scenario.patternMatch.timeframe}")
            appendLine()
            appendLine("Theoretical Trade Parameters:")
            appendLine("  Entry Price:    ${formatPrice(scenario.entryPrice)}")
            appendLine("  Stop Loss:      ${formatPrice(scenario.stopLoss)}")
            appendLine("  Take Profit:    ${formatPrice(scenario.takeProfit)}")
            appendLine("  Risk/Reward:    ${formatRatio(scenario.riskRewardRatio)}")
            appendLine()
            appendLine("Position Sizing Example (${scenario.accountRiskPercent}% risk):")
            appendLine("  Shares/Units:   ${formatUnits(scenario.positionSizeExample)}")
            appendLine()
            appendLine("Context:")
            appendLine(scenario.educationalContext)
            appendLine()
            scenario.regimeContext?.let { regime ->
                appendLine("Market Regime:")
                appendLine("  ${regime.educationalContext}")
                appendLine()
            }
            appendLine("⚠️ DISCLAIMER:")
            appendLine("This is a HYPOTHETICAL EDUCATIONAL EXAMPLE only.")
            appendLine("NOT financial advice, investment recommendations, or trading signals.")
            appendLine("YOU are responsible for all trading decisions.")
            appendLine("Consult a licensed financial advisor before trading.")
            appendLine("See legal/ADVANCED_FEATURES_DISCLAIMER.md")
        }
    }
    
    private data class PricePoints(
        val entry: Double,
        val stopLoss: Double,
        val takeProfit: Double
    )
    
    private fun calculateEntryExitPrices(
        pattern: PatternMatch,
        currentPrice: Double
    ): PricePoints {
        
        val patternType = pattern.patternName.lowercase()
        
        return when {
            patternType.contains("head_and_shoulders") && !patternType.contains("inverse") -> {
                val neckline = currentPrice * 0.98
                val headHeight = currentPrice * 0.10
                PricePoints(
                    entry = neckline * 0.995,
                    stopLoss = neckline + (headHeight * 0.3),
                    takeProfit = neckline - headHeight
                )
            }
            
            patternType.contains("inverse_head_and_shoulders") -> {
                val neckline = currentPrice * 1.02
                val headDepth = currentPrice * 0.10
                PricePoints(
                    entry = neckline * 1.005,
                    stopLoss = neckline - (headDepth * 0.3),
                    takeProfit = neckline + headDepth
                )
            }
            
            patternType.contains("double_top") -> {
                val resistance = currentPrice * 1.05
                val height = currentPrice * 0.08
                PricePoints(
                    entry = resistance * 0.99,
                    stopLoss = resistance * 1.03,
                    takeProfit = resistance - height
                )
            }
            
            patternType.contains("double_bottom") -> {
                val support = currentPrice * 0.95
                val height = currentPrice * 0.08
                PricePoints(
                    entry = support * 1.01,
                    stopLoss = support * 0.97,
                    takeProfit = support + height
                )
            }
            
            patternType.contains("bull_flag") || patternType.contains("bull_pennant") -> {
                val flagTop = currentPrice * 1.02
                val poleHeight = currentPrice * 0.15
                PricePoints(
                    entry = flagTop * 1.005,
                    stopLoss = currentPrice * 0.97,
                    takeProfit = flagTop + poleHeight
                )
            }
            
            patternType.contains("bear_flag") || patternType.contains("bear_pennant") -> {
                val flagBottom = currentPrice * 0.98
                val poleHeight = currentPrice * 0.15
                PricePoints(
                    entry = flagBottom * 0.995,
                    stopLoss = currentPrice * 1.03,
                    takeProfit = flagBottom - poleHeight
                )
            }
            
            patternType.contains("ascending_triangle") -> {
                val resistance = currentPrice * 1.05
                val height = currentPrice * 0.10
                PricePoints(
                    entry = resistance * 1.01,
                    stopLoss = currentPrice * 0.95,
                    takeProfit = resistance + height
                )
            }
            
            patternType.contains("descending_triangle") -> {
                val support = currentPrice * 0.95
                val height = currentPrice * 0.10
                PricePoints(
                    entry = support * 0.99,
                    stopLoss = currentPrice * 1.05,
                    takeProfit = support - height
                )
            }
            
            patternType.contains("cup_and_handle") -> {
                val cupDepth = currentPrice * 0.20
                PricePoints(
                    entry = currentPrice * 1.02,
                    stopLoss = currentPrice * 0.95,
                    takeProfit = currentPrice + cupDepth
                )
            }
            
            patternType.contains("rising_wedge") -> {
                val wedgeTop = currentPrice * 1.03
                val height = currentPrice * 0.12
                PricePoints(
                    entry = wedgeTop * 0.99,
                    stopLoss = wedgeTop * 1.03,
                    takeProfit = wedgeTop - height
                )
            }
            
            patternType.contains("falling_wedge") -> {
                val wedgeBottom = currentPrice * 0.97
                val height = currentPrice * 0.12
                PricePoints(
                    entry = wedgeBottom * 1.01,
                    stopLoss = wedgeBottom * 0.97,
                    takeProfit = wedgeBottom + height
                )
            }
            
            else -> {
                val defaultRange = currentPrice * 0.05
                val isBullish = pattern.confidence > 0.7
                
                if (isBullish) {
                    PricePoints(
                        entry = currentPrice * 1.005,
                        stopLoss = currentPrice * 0.97,
                        takeProfit = currentPrice * 1.08
                    )
                } else {
                    PricePoints(
                        entry = currentPrice * 0.995,
                        stopLoss = currentPrice * 1.03,
                        takeProfit = currentPrice * 0.92
                    )
                }
            }
        }
    }
    
    private fun calculatePositionSize(
        accountSize: Double,
        riskPercent: Double,
        entryPrice: Double,
        stopLoss: Double
    ): Double {
        val riskAmount = accountSize * (riskPercent / 100.0)
        val riskPerShare = abs(entryPrice - stopLoss)
        
        return if (riskPerShare > 0) {
            riskAmount / riskPerShare
        } else {
            0.0
        }
    }
    
    private fun calculateRiskRewardRatio(
        entry: Double,
        stopLoss: Double,
        takeProfit: Double
    ): Double {
        val risk = abs(entry - stopLoss)
        val reward = abs(takeProfit - entry)
        
        return if (risk > 0) reward / risk else 0.0
    }
    
    private fun generateEducationalContext(
        patternMatch: PatternMatch,
        prices: PricePoints,
        positionSize: Double,
        rrRatio: Double,
        regime: RegimeNavigator.MarketRegime?
    ): String {
        return buildString {
            append("This scenario demonstrates how traders theoretically might approach ")
            append("a ${patternMatch.patternName} pattern. ")
            
            if (rrRatio >= 2.0) {
                append("The risk/reward ratio of ${formatRatio(rrRatio)} is considered favorable in textbooks. ")
            } else if (rrRatio < 1.5) {
                append("The risk/reward ratio of ${formatRatio(rrRatio)} is below the typical 2:1 minimum recommended in trading literature. ")
            }
            
            regime?.let {
                append("Market regime: ${it.overallQuality}. ")
            }
            
            append("\n\nREMEMBER: This is for EDUCATIONAL purposes only. ")
            append("Past pattern performance does NOT predict future results. ")
            append("All trading involves substantial risk of loss.")
        }
    }
    
    private fun formatPrice(price: Double): String = "$%.2f".format(price)
    
    private fun formatPercent(value: Double): String = "%.1f%%".format(value * 100)
    
    private fun formatRatio(ratio: Double): String = "%.2f:1".format(ratio)
    
    private fun formatUnits(units: Double): String = "%.2f".format(units)
    
    private fun VoiceAnnouncer.speak(text: String, utteranceId: String) {
        try {
            val params = android.os.Bundle().apply {
                putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            
            val ttsField = this::class.java.getDeclaredField("tts")
            ttsField.isAccessible = true
            val tts = ttsField.get(this) as? android.speech.tts.TextToSpeech
            
            tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_ADD, params, utteranceId)
            
        } catch (e: Exception) {
            Timber.e(e, "Error speaking scenario")
        }
    }
}
