package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson13HammerHangingMan = Lesson(
            id = 13,
            title = "Hammer and Hanging Man",
            category = "Candlestick Patterns",
            duration = "7 min",
            content = """
                # Hammer and Hanging Man
                
                ## Overview
                
                The Hammer and Hanging Man are single candlestick patterns with identical structure but opposite implications based on their location. They have small bodies at the top of the range and long lower shadows, creating a hammer-like or hanging man appearance.
                
                ## Pattern Structure
                
                ```
                    ┌──┐
                    │  │ Small body (top of range)
                    └──┘
                     |
                     |  Long lower shadow
                     |  (at least 2x body length)
                     |
                ```
                
                ### Characteristics:
                - **Body**: Small, at upper end of range
                - **Color**: Can be bullish or bearish (less important)
                - **Lower Shadow**: Long (2-3x body length minimum)
                - **Upper Shadow**: None or very small
                - **Range**: Lower shadow represents 2/3+ of total range
                
                ## The Hammer (Bullish Reversal)
                
                ### Location:
                - **Appears**: At bottom of downtrend
                - **Context**: After selling pressure
                - **Support**: Often at support levels
                - **Timing**: Trend exhaustion
                
                ### Formation Psychology:
                
                **During the Period**:
                1. Sellers push price significantly lower
                2. Bears appear to be in control
                3. Buyers step in at lows
                4. Price rallies back up
                5. Closes near the high
                
                **Interpretation**:
                - Selling pressure rejected
                - Buyers overwhelmed sellers
                - Potential bottom forming
                - Bullish reversal signal
                
                ### Trading the Hammer:
                
                **Entry Strategy**:
                - **Aggressive**: Enter at close of hammer candle
                - **Conservative**: Wait for bullish confirmation candle
                - **Best**: Enter above confirmation candle high
                
                **Stop Loss**:
                - Below hammer low (shadow low)
                - Tight stop = better risk/reward
                - If stopped out, pattern failed
                
                **Target**:
                - Next resistance level
                - Previous swing high
                - 2:1 or 3:1 risk/reward minimum
                
                **Volume**:
                - Higher volume = stronger signal
                - Volume spike ideal
                - Low volume = caution
                
                ## The Hanging Man (Bearish Reversal)
                
                ### Location:
                - **Appears**: At top of uptrend
                - **Context**: After buying pressure
                - **Resistance**: Often at resistance levels
                - **Timing**: Trend exhaustion
                
                ### Formation Psychology:
                
                **During the Period**:
                1. Buyers start strong
                2. Price pushed significantly lower during session
                3. Bears show strength
                4. Price recovers to close near high
                5. But damage done - warning sign
                
                **Interpretation**:
                - Buyers losing control
                - Sellers gaining strength
                - Potential top forming
                - Bearish reversal warning
                
                ### Trading the Hanging Man:
                
                **Entry Strategy**:
                - **Never Alone**: Must have confirmation
                - **Confirmation**: Next candle closes below Hanging Man body
                - **Entry**: Below confirmation candle low
                
                **Stop Loss**:
                - Above Hanging Man high
                - Or above resistance level
                - Wider stop than Hammer (more risky pattern)
                
                **Target**:
                - Next support level
                - Previous swing low
                - 2:1 risk/reward minimum
                
                **Volume**:
                - High volume on Hanging Man = warning
                - High volume on confirmation = strong signal
                
                ## Key Differences
                
                | Feature | Hammer | Hanging Man |
                |---------|--------|-------------|
                | Location | Bottom of downtrend | Top of uptrend |
                | Signal | Bullish reversal | Bearish reversal |
                | Reliability | High (75%+) | Moderate (60%) |
                | Confirmation | Helpful | Essential |
                | Body Color | Less important | Bearish better |
                
                ## Confirmation Requirements
                
                ### Hammer Confirmation (Helpful):
                
                ✅ **Strong Confirmation**:
                - Next candle bullish close
                - Closes above Hammer high
                - High volume
                - Gap up opening
                
                ### Hanging Man Confirmation (Essential):
                
                ✅ **Must Have Confirmation**:
                - Next candle bearish close
                - Closes below Hanging Man low
                - High volume
                - Gap down opening ideal
                
                **Important**: Hanging Man WITHOUT confirmation is not tradeable!
                
                ## Ideal Pattern Characteristics
                
                ### Perfect Hammer:
                ✅ At support level
                ✅ After clear downtrend
                ✅ Lower shadow 3x+ body length
                ✅ Minimal or no upper shadow
                ✅ Body at top of range
                ✅ High volume
                ✅ Bullish confirmation candle
                
                ### Perfect Hanging Man:
                ✅ At resistance level
                ✅ After clear uptrend
                ✅ Lower shadow 3x+ body length
                ✅ Minimal or no upper shadow
                ✅ Red/bearish body preferred
                ✅ High volume
                ✅ Bearish confirmation essential
                
                ## Common Variations
                
                ### Inverted Hammer:
                - Long upper shadow
                - Small body at bottom
                - Bullish at bottoms
                - Needs confirmation
                
                ### Shooting Star:
                - Long upper shadow
                - Small body at bottom
                - Bearish at tops
                - Similar to Hanging Man
                
                ## Context Matters
                
                ### High Reliability:
                ✅ At major support/resistance
                ✅ After extended trend
                ✅ With high volume
                ✅ Multiple timeframe confirmation
                ✅ Near key Fibonacci levels
                ✅ At psychological price levels
                
                ### Low Reliability:
                ❌ Mid-trend appearance
                ❌ Low volume
                ❌ Weak prior trend
                ❌ No confirmation
                ❌ Conflicting indicators
                
                ## Common Mistakes
                
                ❌ Trading Hanging Man without confirmation
                ❌ Ignoring the prevailing trend
                ❌ Setting stops too tight
                ❌ Expecting perfect proportions
                ❌ Confusing with Doji or Spinning Top
                ❌ Trading every Hammer that appears
                ❌ Ignoring volume signals
                ❌ Not waiting for proper location
                
                ## Advanced Tips
                
                ### Enhanced Reliability:
                
                ✅ **RSI Divergence**: Hammer with bullish RSI divergence = powerful
                ✅ **Volume Profile**: Hammer at high volume node = strong support
                ✅ **Multiple Timeframes**: Hammer on daily + weekly = very strong
                ✅ **Trendline**: Hammer at trendline = perfect confluence
                ✅ **Round Numbers**: Hammer at psychological level = stronger
                
                ## Pro Tips
                
                ✅ Hammer more reliable than Hanging Man
                ✅ Longer shadow = stronger rejection
                ✅ Body color less important than location
                ✅ Always use confirmation on Hanging Man
                ✅ Combine with support/resistance
                ✅ Volume spike increases probability
                ✅ Best on daily or weekly charts
                ✅ Perfect patterns are rare - use judgment
                ✅ Risk/reward should be 2:1 minimum
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the key difference between a Hammer and Hanging Man pattern?",
                        options = listOf("Body color", "Their location - Hammer at bottoms, Hanging Man at tops", "Shadow length", "Volume"),
                        correctAnswer = 1,
                        explanation = "The patterns have identical structure but opposite meanings based on location. A Hammer appears at downtrend bottoms (bullish), while a Hanging Man appears at uptrend tops (bearish)."
                    ),
                    QuizQuestion(
                        question = "Is confirmation required for a Hanging Man pattern?",
                        options = listOf("No, trade immediately", "Yes, confirmation is essential", "Only in bear markets", "Only if volume is high"),
                        correctAnswer = 1,
                        explanation = "Confirmation is essential for Hanging Man patterns. You must wait for the next candle to close below the Hanging Man's body to confirm the bearish reversal before trading."
                    ),
                    QuizQuestion(
                        question = "What is the minimum recommended length for the lower shadow relative to the body?",
                        options = listOf("Same as body", "At least 2-3 times the body length", "Half the body", "10 times the body"),
                        correctAnswer = 1,
                        explanation = "The lower shadow should be at least 2-3 times the length of the body to qualify as a proper Hammer or Hanging Man, showing significant rejection of lower prices."
                    )
                )
            )
        )
