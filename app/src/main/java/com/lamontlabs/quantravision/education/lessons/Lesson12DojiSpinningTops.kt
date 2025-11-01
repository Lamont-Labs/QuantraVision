package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson12DojiSpinningTops = Lesson(
            id = 12,
            title = "Doji and Spinning Tops",
            category = "Candlestick Patterns",
            duration = "8 min",
            content = """
                # Doji and Spinning Tops
                
                ## Overview
                
                Doji and Spinning Tops are indecision candles that signal potential reversals or continuation patterns depending on context. They appear when bulls and bears battle to a near-draw, with neither side gaining clear control. These patterns are critical for identifying market turning points.
                
                ## Doji Candlestick
                
                ### Definition:
                A Doji forms when opening and closing prices are virtually equal, creating little to no body. It represents maximum indecision and equilibrium between buyers and sellers.
                
                ### Basic Structure:
                ```
                     High
                      |
                      | Upper Shadow
                      |
                     ─── ← Doji (Open = Close)
                      |
                      | Lower Shadow
                      |
                     Low
                ```
                
                ## Types of Doji
                
                ### 1. Neutral Doji
                ```
                      |
                      |
                     ─── Equal shadows
                      |
                      |
                ```
                - **Characteristics**: Equal or similar shadows
                - **Meaning**: Perfect indecision
                - **Context**: Most neutral signal
                
                ### 2. Long-Legged Doji
                ```
                      |
                      | Very long
                      | shadows
                     ─── 
                      |
                      | Very long
                      |
                ```
                - **Characteristics**: Very long upper and lower shadows
                - **Meaning**: Extreme volatility, indecision
                - **Strength**: Strong reversal signal
                - **Psychology**: Major battle between bulls and bears
                
                ### 3. Dragonfly Doji
                ```
                     ───
                      |
                      | Long lower shadow
                      | only
                      |
                ```
                - **Characteristics**: Long lower shadow, no upper shadow
                - **Meaning**: Bullish reversal signal
                - **Best Location**: Support levels, downtrend bottoms
                - **Psychology**: Sellers pushed down, buyers took control
                
                ### 4. Gravestone Doji
                ```
                      |
                      | Long upper shadow
                      | only
                     ───
                ```
                - **Characteristics**: Long upper shadow, no lower shadow
                - **Meaning**: Bearish reversal signal
                - **Best Location**: Resistance levels, uptrend tops
                - **Psychology**: Buyers pushed up, sellers took control
                
                ## Spinning Top Candlestick
                
                ### Definition:
                A Spinning Top has a small real body with long upper and lower shadows. Unlike Doji, it has a small body, showing slight directional bias but still indicating indecision.
                
                ### Structure:
                ```
                      |
                      | Upper shadow
                      |
                    ┌─┐
                    │ │ Small body (any color)
                    └─┘
                      |
                      | Lower shadow
                      |
                ```
                
                ### Characteristics:
                - **Body**: Small (1/3 or less of total range)
                - **Shadows**: Long (both sides ideally)
                - **Color**: Can be bullish or bearish (less important)
                - **Meaning**: Indecision, potential reversal
                
                ## Trading Doji Patterns
                
                ### Doji at Support (Bullish):
                
                **Setup**:
                1. Downtrend or pullback in progress
                2. Doji forms at support level
                3. Shows selling exhaustion
                
                **Entry**:
                - Wait for bullish confirmation candle
                - Enter above Doji high
                - Or enter on support hold
                
                **Stop Loss**:
                - Below Doji low
                - Or below support level
                
                **Target**:
                - Next resistance level
                - Previous swing high
                - Risk:Reward ratio of 2:1 minimum
                
                ### Doji at Resistance (Bearish):
                
                **Setup**:
                1. Uptrend or rally in progress
                2. Doji forms at resistance
                3. Shows buying exhaustion
                
                **Entry**:
                - Wait for bearish confirmation candle
                - Enter below Doji low
                - Or at resistance rejection
                
                **Stop Loss**:
                - Above Doji high
                - Or above resistance level
                
                **Target**:
                - Next support level
                - Previous swing low
                - Risk:Reward ratio of 2:1 minimum
                
                ## Trading Spinning Tops
                
                ### Reversal Setup:
                
                **Bullish Reversal**:
                - Spinning Top after downtrend
                - At support level
                - Followed by bullish candle
                - Enter above confirmation
                
                **Bearish Reversal**:
                - Spinning Top after uptrend
                - At resistance level
                - Followed by bearish candle
                - Enter below confirmation
                
                ### Continuation Setup:
                
                **In Uptrend**:
                - Spinning Top = healthy pause
                - Brief consolidation
                - Trend often continues
                - Enter on breakout above
                
                **In Downtrend**:
                - Spinning Top = weak bounce
                - Brief hesitation
                - Downtrend often continues
                - Enter on break below
                
                ## Context is Everything
                
                ### High Reliability Locations:
                
                ✅ **At Support/Resistance**:
                - Maximum effectiveness
                - Clear risk/reward
                - Better probability
                
                ✅ **After Extended Moves**:
                - Trend exhaustion signal
                - Reversal more likely
                - High conviction setup
                
                ✅ **With Volume**:
                - Volume spike on Doji day
                - Shows real battle
                - Stronger signal
                
                ### Low Reliability Locations:
                
                ❌ **Mid-Trend**:
                - Often just noise
                - Low reversal probability
                - Better to ignore
                
                ❌ **Low Volume**:
                - Weak signal
                - Lack of conviction
                - Often fails
                
                ❌ **Without Confirmation**:
                - Never trade alone
                - Need following candle
                - Patience required
                
                ## Confirmation Requirements
                
                ### For Doji:
                
                **Bullish Confirmation**:
                - Next candle closes above Doji high
                - Ideally a strong green candle
                - Increasing volume
                
                **Bearish Confirmation**:
                - Next candle closes below Doji low
                - Ideally a strong red candle
                - Increasing volume
                
                ### For Spinning Top:
                
                **Bullish Confirmation**:
                - Next candle strong bullish close
                - Breaks above resistance if present
                - Volume increase
                
                **Bearish Confirmation**:
                - Next candle strong bearish close
                - Breaks below support if present
                - Volume increase
                
                ## Psychology
                
                ### Doji Psychology:
                
                **At Top**:
                - Buyers lose conviction
                - Profit-taking emerges
                - Uncertainty grows
                - Reversal potential
                
                **At Bottom**:
                - Sellers lose conviction
                - Bargain hunters emerge
                - Fear subsides
                - Reversal potential
                
                ### Spinning Top Psychology:
                
                - Bulls and bears in balance
                - Neither side dominant
                - Market pausing
                - Decision pending
                
                ## Common Mistakes
                
                ❌ Trading Doji without confirmation
                ❌ Ignoring trend and location context
                ❌ Expecting every Doji to reverse
                ❌ Not using stop losses
                ❌ Confusing Doji with small body candles
                ❌ Trading mid-trend Dojis
                ❌ Forgetting volume confirmation
                
                ## Pro Tips
                
                ✅ Doji after strong trend = powerful signal
                ✅ Multiple Dojis = extreme indecision
                ✅ Combine with support/resistance
                ✅ Wait for confirmation candle (always!)
                ✅ Volume spike increases reliability
                ✅ Spinning Tops = warning, not signal
                ✅ Use with other indicators (RSI, MACD)
                ✅ Higher timeframes more reliable
                ✅ Dragonfly at support = very bullish
                ✅ Gravestone at resistance = very bearish
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the key characteristic of a Doji candlestick?",
                        options = listOf("Large body", "Open and close prices are virtually equal", "Only green candles", "No shadows"),
                        correctAnswer = 1,
                        explanation = "A Doji forms when the opening and closing prices are virtually equal, creating little to no real body. This represents maximum indecision in the market."
                    ),
                    QuizQuestion(
                        question = "Where is a Dragonfly Doji most effective as a bullish reversal signal?",
                        options = listOf("At resistance", "At support levels or downtrend bottoms", "Mid-trend", "Anywhere"),
                        correctAnswer = 1,
                        explanation = "A Dragonfly Doji (long lower shadow, no upper shadow) is most effective at support levels or downtrend bottoms, where it shows sellers pushed price down but buyers took control."
                    ),
                    QuizQuestion(
                        question = "Should you trade a Doji pattern immediately when it appears?",
                        options = listOf("Yes, always trade immediately", "No, always wait for confirmation in the next candle", "Only if it's green", "Only on Mondays"),
                        correctAnswer = 1,
                        explanation = "Never trade a Doji alone. Always wait for confirmation in the next candle (bullish candle closing above for long, bearish closing below for short) to avoid false signals."
                    )
                )
            )
        )
