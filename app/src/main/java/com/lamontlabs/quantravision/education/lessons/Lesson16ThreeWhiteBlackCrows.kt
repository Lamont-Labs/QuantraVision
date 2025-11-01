package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson16ThreeWhiteBlackCrows = Lesson(
            id = 16,
            title = "Three White Soldiers and Three Black Crows",
            category = "Candlestick Patterns",
            duration = "7 min",
            content = """
                # Three White Soldiers and Three Black Crows
                
                ## Overview
                
                Three White Soldiers and Three Black Crows are powerful three-candle reversal patterns that signal strong momentum shifts. They consist of three consecutive candles in the same direction, each opening within the previous candle's body and closing progressively higher (soldiers) or lower (crows).
                
                ## Three White Soldiers (Bullish Reversal)
                
                ### Structure:
                ```
                        3rd ┌────┐
                            │    │
                   2nd  ┌───┤    │
                        │   │    │
                1st ┌───┤   │    │
                    │   │   │    │
                    │   │   │    │
                    └───┴───┴────┘
                    
                ← Downtrend | Strong Reversal →
                ```
                
                ### Characteristics:
                
                **First Candle**:
                - Large bullish (green) candle
                - Appears after downtrend
                - Opens near low, closes near high
                - Minimal upper shadow
                - First sign of reversal
                
                **Second Candle**:
                - Large bullish (green) candle
                - Opens within first candle's body
                - Closes higher than first
                - Similar or larger size
                - Confirms buying pressure
                
                **Third Candle**:
                - Large bullish (green) candle
                - Opens within second candle's body
                - Closes higher than second
                - Similar size to previous
                - Completes the pattern
                
                ### Requirements:
                ✅ All three candles bullish (green)
                ✅ Each opens within previous candle's body
                ✅ Each closes progressively higher
                ✅ Large bodies (minimal shadows)
                ✅ Similar sizes (consistency)
                ✅ Appears after downtrend
                ✅ Increasing or high volume
                
                ### Psychology:
                1. **First Candle**: Bears losing control, bulls emerging
                2. **Second Candle**: Bulls gaining confidence, momentum building
                3. **Third Candle**: Bulls dominate, strong conviction
                4. **Result**: Clear trend reversal from bearish to bullish
                
                ## Three Black Crows (Bearish Reversal)
                
                ### Structure:
                ```
                    ┌───┬───┬────┐
                    │   │   │    │
                1st │   │   │ 3rd│
                    │   │ 2nd    │
                    │   └────    │
                    └────────────┘
                    
                ← Uptrend | Strong Reversal →
                ```
                
                ### Characteristics:
                
                **First Candle**:
                - Large bearish (red) candle
                - Appears after uptrend
                - Opens near high, closes near low
                - Minimal lower shadow
                - Warning sign
                
                **Second Candle**:
                - Large bearish (red) candle
                - Opens within first candle's body
                - Closes lower than first
                - Similar or larger size
                - Confirms selling pressure
                
                **Third Candle**:
                - Large bearish (red) candle
                - Opens within second candle's body
                - Closes lower than second
                - Similar size to previous
                - Completes the pattern
                
                ### Requirements:
                ✅ All three candles bearish (red)
                ✅ Each opens within previous candle's body
                ✅ Each closes progressively lower
                ✅ Large bodies (minimal shadows)
                ✅ Similar sizes (consistency)
                ✅ Appears after uptrend
                ✅ Increasing or high volume
                
                ### Psychology:
                1. **First Candle**: Bulls losing grip, bears awakening
                2. **Second Candle**: Bears gaining strength, fear building
                3. **Third Candle**: Bears dominate, panic selling
                4. **Result**: Clear trend reversal from bullish to bearish
                
                ## Trading Three White Soldiers
                
                ### Entry Strategy:
                
                **Aggressive**:
                - Enter during third candle
                - Or at close of third candle
                - Higher risk, better price
                
                **Conservative**:
                - Wait for fourth candle confirmation
                - Or wait for pullback
                - Enter on break above pattern high
                
                **Best Practice**:
                - Enter on break above third candle high
                - Stop below pattern low
                - 2:1 risk/reward minimum
                
                ### Stop Loss:
                - Below low of three candles
                - Or below low of first candle
                - Can be wider stop due to pattern size
                
                ### Target:
                - Next resistance level
                - Previous swing high
                - Measured move (pattern height added to breakout)
                - 3:1 risk/reward ideal
                
                ## Trading Three Black Crows
                
                ### Entry Strategy:
                
                **Aggressive**:
                - Short during third candle
                - Or at close of third candle
                - Higher risk entry
                
                **Conservative**:
                - Wait for pullback to pattern low
                - Or wait for confirmation candle
                - Enter on break below pattern low
                
                **Best Practice**:
                - Short on break below third candle low
                - Stop above pattern high
                - 2:1 risk/reward minimum
                
                ### Stop Loss:
                - Above high of three candles
                - Or above high of first candle
                - Wider stop acceptable for pattern
                
                ### Target:
                - Next support level
                - Previous swing low
                - Measured move (pattern height subtracted)
                - 3:1 risk/reward ideal
                
                ## Pattern Strength Indicators
                
                ### Strong Three White Soldiers:
                ✅ At major support level
                ✅ After extended downtrend (30%+ decline)
                ✅ Candles of equal size
                ✅ Minimal upper shadows
                ✅ Opens in lower half of prior candle
                ✅ Increasing volume each day
                ✅ No gaps between candles
                
                ### Strong Three Black Crows:
                ✅ At major resistance level
                ✅ After extended uptrend (30%+ rally)
                ✅ Candles of equal size
                ✅ Minimal lower shadows
                ✅ Opens in upper half of prior candle
                ✅ Increasing volume each day
                ✅ No gaps between candles
                
                ## Volume Analysis
                
                ### Ideal Volume Pattern:
                
                **Three White Soldiers**:
                - First candle: Above average volume
                - Second candle: Increasing volume
                - Third candle: High volume (confirmation)
                - Shows strong buying conviction
                
                **Three Black Crows**:
                - First candle: Above average volume
                - Second candle: Increasing volume
                - Third candle: High volume (confirmation)
                - Shows strong selling pressure
                
                ## Warning Signs (Weaker Patterns)
                
                ### Advance Block (Soldiers):
                ❌ **Weakening Pattern**:
                - Each candle smaller than previous
                - Long upper shadows appearing
                - Declining volume
                - Indicates weakening momentum
                - May not be sustainable
                - Proceed with caution
                
                ### Deliberation (Crows):
                ❌ **Weakening Pattern**:
                - Third candle significantly smaller
                - Long shadows appearing
                - Volume declining
                - May indicate exhaustion
                - Reversal may fail
                
                ## Common Mistakes
                
                ❌ Trading without clear prior trend
                ❌ Ignoring candle size consistency
                ❌ Accepting patterns with large shadows
                ❌ Not confirming with volume
                ❌ Entering before pattern completes
                ❌ Setting stops too tight
                ❌ Expecting perfect textbook patterns
                ❌ Missing the advance block/deliberation warning
                
                ## Pattern Variations
                
                ### Identical Three Soldiers:
                - All three candles nearly identical
                - Opens at same relative position
                - Very strong signal
                - Rare but powerful
                
                ### White Soldiers After Star:
                - Soldiers after Morning Star
                - Combined reversal signals
                - Extremely bullish
                - High probability setup
                
                ## Context and Timing
                
                ### Best Locations:
                ✅ Major support (Soldiers) / resistance (Crows)
                ✅ After extended trend (20-30%+)
                ✅ At Fibonacci levels
                ✅ Near round psychological numbers
                ✅ After capitulation/euphoria
                ✅ Multiple timeframe alignment
                
                ### Poor Locations:
                ❌ Mid-trend (not reversal)
                ❌ Consolidation zones
                ❌ No clear prior trend
                ❌ Low volume environment
                
                ## Pro Tips
                
                ✅ Pattern more reliable with increasing volume
                ✅ Candles should be consistent in size
                ✅ Minimal shadows = strong conviction
                ✅ Each open in lower/upper half of prior = stronger
                ✅ Best after 30%+ trend move
                ✅ Combine with support/resistance
                ✅ RSI divergence adds confirmation
                ✅ Pattern works on all timeframes
                ✅ Three White Soldiers more reliable than Crows
                ✅ Watch for advance block weakness
                ✅ Use trailing stops to maximize profits
                ✅ Always confirm prior trend exists
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is a key requirement for Three White Soldiers pattern?",
                        options = listOf("Gaps between candles", "Each candle opens within the previous candle's body", "Decreasing size", "Long shadows"),
                        correctAnswer = 1,
                        explanation = "A key requirement is that each successive candle must open within the previous candle's body, showing sustained and controlled buying pressure rather than gaps or erratic behavior."
                    ),
                    QuizQuestion(
                        question = "What does an 'Advance Block' indicate in a Three White Soldiers pattern?",
                        options = listOf("Very strong signal", "Weakening momentum - each candle smaller with long shadows", "Need more soldiers", "Perfect pattern"),
                        correctAnswer = 1,
                        explanation = "An Advance Block occurs when each candle becomes progressively smaller with long upper shadows, indicating weakening momentum and suggesting the rally may not be sustainable."
                    ),
                    QuizQuestion(
                        question = "What volume pattern strengthens Three Black Crows?",
                        options = listOf("Decreasing volume", "Constant volume", "Increasing volume through all three candles", "No volume"),
                        correctAnswer = 2,
                        explanation = "Increasing volume through all three candles confirms strong and growing selling pressure, making the bearish reversal more reliable and likely to continue."
                    )
                )
            )
        )
