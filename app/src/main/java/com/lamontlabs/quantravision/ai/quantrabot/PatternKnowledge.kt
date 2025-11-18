package com.lamontlabs.quantravision.ai.quantrabot

import com.google.gson.annotations.SerializedName

/**
 * Represents expert knowledge about a technical analysis pattern.
 * Used by QuantraBot to validate patterns and provide intelligent explanations.
 */
data class PatternKnowledge(
    @SerializedName("pattern_id")
    val patternId: String,
    
    @SerializedName("pattern_name")
    val patternName: String,
    
    @SerializedName("category")
    val category: PatternCategory,
    
    @SerializedName("bias")
    val bias: MarketBias,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("validation_rules")
    val validationRules: ValidationRules,
    
    @SerializedName("invalidation_signals")
    val invalidationSignals: List<String>,
    
    @SerializedName("common_mistakes")
    val commonMistakes: List<String>,
    
    @SerializedName("trade_setup")
    val tradeSetup: TradeSetup
)

data class ValidationRules(
    @SerializedName("structure")
    val structure: List<String>,
    
    @SerializedName("volume")
    val volume: List<String>,
    
    @SerializedName("indicators")
    val indicators: List<String>,
    
    @SerializedName("timeframe")
    val timeframe: List<String>
)

data class TradeSetup(
    @SerializedName("entry")
    val entry: String,
    
    @SerializedName("stop_loss")
    val stopLoss: String,
    
    @SerializedName("target")
    val target: String
)

enum class PatternCategory {
    @SerializedName("reversal")
    REVERSAL,
    
    @SerializedName("continuation")
    CONTINUATION,
    
    @SerializedName("breakout")
    BREAKOUT,
    
    @SerializedName("candlestick")
    CANDLESTICK
}

enum class MarketBias {
    @SerializedName("bullish")
    BULLISH,
    
    @SerializedName("bearish")
    BEARISH,
    
    @SerializedName("neutral")
    NEUTRAL
}
