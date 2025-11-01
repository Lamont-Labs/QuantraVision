package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson14EngulfingPatterns = Lesson(
            id = 14,
            title = "Engulfing Patterns",
            category = "Candlestick Patterns",
            duration = "8 min",
            content = """
                # Engulfing Patterns
                
                ## Overview
                
                Engulfing patterns are two-candle reversal patterns where the second candle's body completely engulfs (encompasses) the first candle's body. They signal strong shifts in market sentiment and are among the most reliable candlestick reversal patterns.
                
                ## Bullish Engulfing Pattern
                
                ### Structure:
                ```
                   1st    2nd
                   ┌─┐   ┌───┐
                   │ │   │   │ Large green
                   └─┘   │   │ engulfs small red
                  Small  │   │
                  Red    └───┘
                  
                ← Downtrend  Reversal →
                ```
                
                ### Requirements:
                1. **Trend**: Appears in downtrend
                2. **First Candle**: Small bearish (red) candle
                3. **Second Candle**: Larger bullish (green) candle
                4. **Engulfing**: Second body completely engulfs first body
                5. **Opens**: Below first candle close
                6. **Closes**: Above first candle open
                
                ### Psychology:
                - **Day 1**: Bears in control, continuing downtrend
                - **Day 2**: Opens lower (bears still confident)
                - **Day 2**: Bulls take over, drive price higher
                - **Close**: Bulls completely reverse prior session
                - **Result**: Power shift from bears to bulls
                
                ## Bearish Engulfing Pattern
                
                ### Structure:
                ```
                   1st    2nd
                   ┌─┐   ┌───┐ Large red
                   │ │   │   │ engulfs small green
                   └─┘   │   │
                  Small  │   │
                  Green  └───┘
                  
                ← Uptrend  Reversal →
                ```
                
                ### Requirements:
                1. **Trend**: Appears in uptrend
                2. **First Candle**: Small bullish (green) candle
                3. **Second Candle**: Larger bearish (red) candle
                4. **Engulfing**: Second body completely engulfs first body
                5. **Opens**: Above first candle close
                6. **Closes**: Below first candle open
                
                ### Psychology:
                - **Day 1**: Bulls in control, continuing uptrend
                - **Day 2**: Opens higher (bulls still confident)
                - **Day 2**: Bears take over, drive price lower
                - **Close**: Bears completely reverse prior session
                - **Result**: Power shift from bulls to bears
                
                ## Trading Bullish Engulfing
                
                ### Entry Strategies:
                
                **Aggressive Entry**:
                - Enter at close of engulfing candle
                - Or at open of next candle
                - Higher risk, better price
                
                **Conservative Entry**:
                - Wait for pullback to engulfing candle high
                - Or wait for third candle confirmation
                - Lower risk, may miss move
                
                **Best Practice**:
                - Enter on break above engulfing candle high
                - Confirms buyers still in control
                - Good risk/reward balance
                
                ### Stop Loss:
                - Below engulfing candle low
                - Or below both candles' lows
                - Should be relatively tight
                
                ### Target:
                - Next resistance level
                - Previous swing high
                - 2:1 or 3:1 risk/reward ratio
                - Trailing stop for extended moves
                
                ## Trading Bearish Engulfing
                
                ### Entry Strategies:
                
                **Aggressive Entry**:
                - Short at close of engulfing candle
                - Or at open of next candle
                - Higher risk entry
                
                **Conservative Entry**:
                - Wait for pullback to engulfing candle low
                - Or wait for confirmation candle
                - Safer but may miss move
                
                **Best Practice**:
                - Short on break below engulfing candle low
                - Confirms sellers in control
                - Better probability
                
                ### Stop Loss:
                - Above engulfing candle high
                - Or above both candles' highs
                - Wider than bullish stops typically
                
                ### Target:
                - Next support level
                - Previous swing low
                - 2:1 risk/reward minimum
                - Trailing stop for big moves
                
                ## Pattern Strength Factors
                
                ### High Reliability Engulfing:
                
                ✅ **Ideal Characteristics**:
                - Large engulfing candle (3x+ first candle)
                - At major support/resistance
                - After extended trend
                - High volume on engulfing candle (2x+ average)
                - Complete body engulfment (not just wicks)
                - Gap between candles
                - Multiple timeframe confirmation
                
                ### Moderate Reliability:
                
                ⚠️ **Acceptable but Weaker**:
                - Moderate size engulfing candle
                - Mid-trend appearance
                - Average volume
                - Shadows engulf but bodies barely
                
                ### Low Reliability:
                
                ❌ **Avoid These**:
                - Small engulfing candle
                - Similar sized candles
                - Low volume
                - Against major trend
                - No support/resistance nearby
                
                ## Volume Analysis
                
                ### Bullish Engulfing Volume:
                ✅ **Strong Signal**:
                - Low volume on bearish candle (selling exhaustion)
                - High volume on bullish candle (2x+ average)
                - Shows conviction in reversal
                
                ### Bearish Engulfing Volume:
                ✅ **Strong Signal**:
                - Low volume on bullish candle (buying exhaustion)
                - High volume on bearish candle (2x+ average)
                - Confirms distribution
                
                ## Pattern Variations
                
                ### Last Engulfing:
                - Third+ engulfing pattern in series
                - Usually strongest
                - Final capitulation
                - High probability reversal
                
                ### Piercing Pattern (Bullish):
                - Similar to bullish engulfing
                - Second candle closes above 50% of first
                - Doesn't fully engulf
                - Slightly weaker signal
                
                ### Dark Cloud Cover (Bearish):
                - Similar to bearish engulfing
                - Second candle closes below 50% of first
                - Doesn't fully engulf
                - Slightly weaker signal
                
                ## Context and Location
                
                ### Best Locations:
                
                ✅ **High Probability Zones**:
                - Major support/resistance levels
                - Fibonacci retracement levels (38.2%, 61.8%)
                - Round psychological numbers
                - Previous swing highs/lows
                - Trendline touches
                - Gap fill levels
                
                ### Poor Locations:
                
                ❌ **Low Probability Zones**:
                - Middle of trading range
                - No clear trend
                - Far from support/resistance
                - Low volume areas
                
                ## Confirmation Signals
                
                ### Additional Confirmation:
                
                ✅ **Increases Probability**:
                - RSI showing divergence
                - MACD crossover
                - Stochastic in extreme zone
                - Bollinger Band touch
                - Moving average support/resistance
                - Multiple timeframe agreement
                
                ## Common Mistakes
                
                ❌ Trading every engulfing pattern
                ❌ Ignoring volume confirmation
                ❌ Not checking trend context
                ❌ Setting stops too tight
                ❌ Expecting perfect engulfment
                ❌ Trading against major trend
                ❌ Forgetting support/resistance
                ❌ Not using multiple timeframes
                
                ## Pro Tips
                
                ✅ Size matters - bigger engulfing = stronger signal
                ✅ Volume confirmation is critical
                ✅ Best at major support/resistance
                ✅ Multiple timeframe analysis essential
                ✅ Combine with momentum indicators
                ✅ Wait for proper location
                ✅ Gaps between candles add strength
                ✅ After extended trend = more reliable
                ✅ Third engulfing often strongest
                ✅ Always check overall market context
                ✅ Risk/reward should be 2:1 minimum
                ✅ Use trailing stops for big winners
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the key requirement for an Engulfing pattern?",
                        options = listOf("Both candles same color", "Second candle's body completely engulfs first candle's body", "First candle must be large", "Must occur on Monday"),
                        correctAnswer = 1,
                        explanation = "The defining characteristic of an Engulfing pattern is that the second candle's real body must completely engulf (encompass) the first candle's real body, showing a strong power shift."
                    ),
                    QuizQuestion(
                        question = "What volume pattern strengthens an Engulfing pattern?",
                        options = listOf("Low volume throughout", "High volume on engulfing candle (2x+ average)", "Volume doesn't matter", "Decreasing volume"),
                        correctAnswer = 1,
                        explanation = "High volume on the engulfing candle (ideally 2x or more above average) confirms strong conviction in the reversal and significantly increases pattern reliability."
                    ),
                    QuizQuestion(
                        question = "Where are Engulfing patterns most reliable?",
                        options = listOf("Anywhere in the chart", "At major support/resistance levels after extended trends", "Only in sideways markets", "During low volatility"),
                        correctAnswer = 1,
                        explanation = "Engulfing patterns are most reliable when they appear at major support/resistance levels after extended trends, where they signal exhaustion and potential reversal points."
                    )
                )
            )
        )
