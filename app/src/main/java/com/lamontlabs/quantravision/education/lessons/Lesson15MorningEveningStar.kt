package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson15MorningEveningStar = Lesson(
            id = 15,
            title = "Morning and Evening Star",
            category = "Candlestick Patterns",
            duration = "9 min",
            content = """
                # Morning Star and Evening Star Patterns
                
                ## Overview
                
                Morning Star and Evening Star are three-candle reversal patterns that signal potential trend changes. They are among the most reliable candlestick patterns, especially when appearing at key support or resistance levels with proper volume confirmation.
                
                ## Morning Star (Bullish Reversal)
                
                ### Structure:
                ```
                1st      2nd     3rd
                ┌──┐             ┌────┐
                │  │    ─ or ┌┐  │    │ Large
                │  │      Doji   │    │ Green
                │  │       ┌┐    │    │
                └──┘       └┘    └────┘
                Large    Small    Large
                Red      Body     Green
                
                ← Downtrend  |  Reversal →
                ```
                
                ### Three Candles:
                
                **First Candle** (Day 1):
                - Large bearish (red) candle
                - Continues the downtrend
                - Shows sellers in control
                - Strong momentum downward
                
                **Second Candle** (Day 2) - The "Star":
                - Small body (any color)
                - Often a Doji or Spinning Top
                - Gaps down from first candle (ideal)
                - Shows indecision/equilibrium
                - Buying and selling balanced
                
                **Third Candle** (Day 3):
                - Large bullish (green) candle
                - Closes well into first candle's body
                - Ideally closes above 50% of first candle
                - Confirms bullish reversal
                - Buyers take control
                
                ### Psychology:
                1. **Day 1**: Bears dominate, strong selling
                2. **Day 2**: Gap down shows continued pessimism, but small body shows weakening
                3. **Day 3**: Bulls overwhelm bears, gap up, strong close
                4. **Result**: Complete sentiment reversal
                
                ## Evening Star (Bearish Reversal)
                
                ### Structure:
                ```
                1st      2nd     3rd
                ┌────┐           ┌──┐
                │    │  ─ or ┌┐  │  │ Large
                │    │    Doji   │  │ Red
                │    │     ┌┐    │  │
                └────┘     └┘    └──┘
                Large    Small    Large
                Green    Body     Red
                
                ← Uptrend   |  Reversal →
                ```
                
                ### Three Candles:
                
                **First Candle** (Day 1):
                - Large bullish (green) candle
                - Continues the uptrend
                - Bulls in control
                - Strong upward momentum
                
                **Second Candle** (Day 2) - The "Star":
                - Small body (any color)
                - Often Doji or Spinning Top
                - Gaps up from first candle (ideal)
                - Shows hesitation at highs
                - Equilibrium reached
                
                **Third Candle** (Day 3):
                - Large bearish (red) candle
                - Closes well into first candle's body
                - Ideally closes below 50% of first candle
                - Confirms bearish reversal
                - Sellers take control
                
                ### Psychology:
                1. **Day 1**: Bulls dominate, euphoria
                2. **Day 2**: Gap up but can't sustain, warning
                3. **Day 3**: Bears overwhelm bulls, panic selling
                4. **Result**: Sentiment shift from bullish to bearish
                
                ## Trading the Morning Star
                
                ### Entry Strategy:
                
                **Aggressive**:
                - Enter at close of third candle
                - Risk: No additional confirmation
                - Benefit: Best price
                
                **Conservative**:
                - Wait for fourth candle to confirm
                - Enter on break above Morning Star high
                - Lower risk, better confirmation
                
                **Best Practice**:
                - Enter above third candle high
                - Confirms buyers maintaining control
                - Good risk/reward setup
                
                ### Stop Loss:
                - Below the low of second candle (star)
                - Or below low of entire pattern
                - Tight stop possible due to pattern structure
                
                ### Price Target:
                - Next resistance level
                - Previous swing high
                - 2:1 or 3:1 risk/reward ratio minimum
                - Fibonacci extension levels
                
                ## Trading the Evening Star
                
                ### Entry Strategy:
                
                **Aggressive**:
                - Short at close of third candle
                - Higher risk entry
                - Best price if pattern holds
                
                **Conservative**:
                - Wait for confirmation candle
                - Enter on break below Evening Star low
                - More reliable, less risk
                
                **Best Practice**:
                - Short below third candle low
                - Confirms sellers in control
                - Better probability
                
                ### Stop Loss:
                - Above the high of second candle (star)
                - Or above high of entire pattern
                - Account for volatility
                
                ### Price Target:
                - Next support level
                - Previous swing low
                - 2:1 risk/reward minimum
                - Measured move from prior trend
                
                ## Pattern Variations
                
                ### Doji Star:
                - Second candle is perfect Doji
                - Strongest version
                - Maximum indecision
                - Most reliable reversal
                
                ### Abandoned Baby (Rare):
                - Gaps on both sides of star
                - Star doesn't touch other candles
                - Very rare and very powerful
                - Extremely reliable
                
                ### Regular Star:
                - Second candle small body (not Doji)
                - Still valid pattern
                - Slightly less reliable
                - More common
                
                ## Reliability Factors
                
                ### High Reliability Signals:
                
                ✅ **Morning Star**:
                - At major support level
                - After extended downtrend
                - Star gaps down significantly
                - Third candle gaps up
                - Third candle closes >50% into first
                - High volume on third candle
                - Multiple timeframe confluence
                
                ✅ **Evening Star**:
                - At major resistance level
                - After extended uptrend
                - Star gaps up significantly
                - Third candle gaps down
                - Third candle closes <50% into first
                - High volume on third candle
                - Bearish divergence on indicators
                
                ### Volume Analysis:
                
                **Ideal Volume Pattern**:
                - **First Candle**: High volume (trend continuation)
                - **Second Candle**: Declining volume (indecision)
                - **Third Candle**: Increasing volume (reversal confirmation)
                - **Breakout**: Volume surge (strong signal)
                
                ## Context Requirements
                
                ### Best Locations:
                
                ✅ **High Probability**:
                - Major support/resistance levels
                - Trend exhaustion points
                - Fibonacci retracement levels (50%, 61.8%)
                - Round psychological numbers
                - Previous swing highs/lows
                - Key moving average levels
                
                ### Poor Locations:
                
                ❌ **Low Probability**:
                - Middle of trading range
                - No clear trend before pattern
                - Away from key levels
                - Low volume environment
                - Choppy, directionless market
                
                ## Confirmation Indicators
                
                ### Additional Confirmation:
                
                ✅ **Strengthens Signal**:
                - **RSI**: Oversold (Morning Star) / Overbought (Evening Star)
                - **MACD**: Bullish crossover / Bearish crossover
                - **Stochastic**: Turning from extreme
                - **Moving Averages**: Price crossing MA
                - **Trendline**: Break of trendline
                - **Volume**: Surge on third candle
                
                ## Common Mistakes
                
                ❌ Trading without proper trend context
                ❌ Ignoring the gap requirement (weaker without)
                ❌ Not waiting for third candle to close
                ❌ Expecting perfect symmetry
                ❌ Setting stops too tight
                ❌ Ignoring volume signals
                ❌ Trading mid-trend stars
                ❌ Not checking support/resistance
                ❌ Missing the bigger picture trend
                
                ## Comparison to Other Patterns
                
                | Feature | Morning/Evening Star | Engulfing | Hammer/Shooting Star |
                |---------|---------------------|-----------|---------------------|
                | Candles | 3 | 2 | 1 |
                | Reliability | Very High | High | Moderate-High |
                | Gaps | Preferred | Not required | Not applicable |
                | Time to Form | 3 periods | 2 periods | 1 period |
                
                ## Pro Tips
                
                ✅ Gaps increase reliability significantly
                ✅ Star candle as Doji = strongest signal
                ✅ Third candle should be decisive (large)
                ✅ Volume crucial - third candle needs surge
                ✅ Best at major support/resistance
                ✅ Combine with momentum indicators
                ✅ Multiple timeframe confirmation powerful
                ✅ Pattern works on all timeframes
                ✅ Patience - wait for full pattern
                ✅ Third candle closing >50% into first = ideal
                ✅ Morning Star more reliable than Evening Star
                ✅ Always consider broader market context
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "How many candles form a Morning Star or Evening Star pattern?",
                        options = listOf("1 candle", "2 candles", "3 candles", "5 candles"),
                        correctAnswer = 2,
                        explanation = "Morning Star and Evening Star patterns are three-candle patterns consisting of a large trending candle, a small star candle showing indecision, and a large reversal candle."
                    ),
                    QuizQuestion(
                        question = "What makes the 'Abandoned Baby' variation more powerful?",
                        options = listOf("Larger candles", "Gaps on both sides of the star candle", "More volume", "Longer timeframe"),
                        correctAnswer = 1,
                        explanation = "The Abandoned Baby variation is very powerful because the star candle gaps away from both the first and third candles, creating complete separation and showing extreme reversal momentum."
                    ),
                    QuizQuestion(
                        question = "Where should the third candle ideally close in a Morning Star pattern?",
                        options = listOf("At the low", "Above 50% into the first candle's body", "Below the first candle", "At the star level"),
                        correctAnswer = 1,
                        explanation = "The third candle in a Morning Star should ideally close above 50% of the first candle's body, showing that bulls have completely reversed the prior bearish momentum."
                    )
                )
            )
        )
