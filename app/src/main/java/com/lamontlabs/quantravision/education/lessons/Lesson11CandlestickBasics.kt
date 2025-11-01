package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson11CandlestickBasics = Lesson(
            id = 11,
            title = "Candlestick Basics",
            category = "Candlestick Patterns",
            duration = "10 min",
            content = """
                # Candlestick Basics
                
                ## Introduction
                
                Candlestick charts originated in 18th century Japan and have become the most popular charting method worldwide. They provide more information than line charts, displaying open, high, low, and close prices in a visual format that reveals market sentiment.
                
                ## Candlestick Anatomy
                
                ```
                        High
                         |
                         | Upper Shadow (Wick)
                         |
                    ┌────┐
                    │    │ ← Real Body
                    │    │   (Open to Close)
                    └────┘
                         |
                         | Lower Shadow (Wick)
                         |
                        Low
                ```
                
                ### Components:
                
                1. **Real Body**:
                   - Rectangle between open and close
                   - Green/White = Close > Open (Bullish)
                   - Red/Black = Close < Open (Bearish)
                   - Size indicates price movement strength
                
                2. **Upper Shadow (Wick)**:
                   - Line above body
                   - Shows highest price reached
                   - Indicates rejection at higher prices
                
                3. **Lower Shadow (Wick)**:
                   - Line below body
                   - Shows lowest price reached
                   - Indicates support/buying pressure
                
                ## Bullish Candle
                
                ```
                      High
                       |
                  ┌────┐
                  │    │ Green/White
                  │    │ Close
                  └────┘ Open
                       |
                      Low
                ```
                
                **Meaning**:
                - Price closed higher than it opened
                - Buyers in control during period
                - Bullish sentiment
                - Strength = body size
                
                ## Bearish Candle
                
                ```
                      High
                       |
                  ┌────┐ Open
                  │    │ Red/Black
                  │    │ Close
                  └────┘
                       |
                      Low
                ```
                
                **Meaning**:
                - Price closed lower than it opened
                - Sellers in control during period
                - Bearish sentiment
                - Strength = body size
                
                ## Candle Characteristics
                
                ### Body Size:
                
                **Long Body**:
                - Strong price movement
                - Clear directional conviction
                - High momentum
                - Decisive market
                
                **Short Body**:
                - Minimal price change
                - Indecision
                - Low momentum
                - Consolidation
                
                **No Body (Doji)**:
                - Open equals close
                - Maximum indecision
                - Potential reversal
                - Requires confirmation
                
                ### Shadow Length:
                
                **Long Upper Shadow**:
                - High rejection
                - Sellers overcame buyers
                - Resistance present
                - Bearish signal
                
                **Long Lower Shadow**:
                - Low rejection
                - Buyers overcame sellers
                - Support present
                - Bullish signal
                
                **No Shadows**:
                - Strong conviction
                - No intraday reversal
                - Marubozu candle
                - Powerful signal
                
                ## Time Periods
                
                Candlesticks can represent any timeframe:
                
                - **1 minute**: Day trading
                - **5/15 minute**: Scalping
                - **1 hour**: Intraday trading
                - **4 hour**: Swing trading
                - **Daily**: Position trading
                - **Weekly**: Long-term investing
                - **Monthly**: Macro analysis
                
                ## Reading Market Sentiment
                
                ### Strong Bullish:
                ```
                  ┌──────┐
                  │      │ Large green body
                  │      │ Small/no shadows
                  │      │
                  └──────┘
                ```
                - Large green body
                - Minimal upper shadow
                - Small/no lower shadow
                - Decisive buying
                
                ### Strong Bearish:
                ```
                  ┌──────┐
                  │      │ Large red body
                  │      │ Small/no shadows
                  │      │
                  └──────┘
                ```
                - Large red body
                - Minimal lower shadow
                - Small/no upper shadow
                - Decisive selling
                
                ### Indecision:
                ```
                     |
                  ┌──┐
                  │  │ Small body
                  └──┘ Long shadows
                     |
                ```
                - Small body (any color)
                - Long upper/lower shadows
                - Equal buying/selling pressure
                - Potential reversal
                
                ## Context is Critical
                
                ### Location Matters:
                
                **At Resistance**:
                - Long upper shadow = bearish
                - Rejection of higher prices
                - Potential reversal
                
                **At Support**:
                - Long lower shadow = bullish
                - Rejection of lower prices
                - Potential bounce
                
                **In Uptrend**:
                - Large green candles = strength
                - Small red candles = healthy pullback
                
                **In Downtrend**:
                - Large red candles = weakness
                - Small green candles = weak bounce
                
                ## Volume Confirmation
                
                ✅ **Strong Signals**:
                - Large candle + high volume = conviction
                - Reversal candle + volume spike = reliable
                - Breakout candle + volume = confirmed
                
                ❌ **Weak Signals**:
                - Large candle + low volume = suspect
                - Reversal candle + no volume = unreliable
                - Pattern without volume = caution
                
                ## Common Candle Types
                
                ### Marubozu:
                - No shadows (or very small)
                - Full-bodied candle
                - Strong directional move
                - High conviction
                
                ### Doji:
                - No body (open = close)
                - Indecision candle
                - Potential reversal
                - Needs confirmation
                
                ### Hammer:
                - Small body at top
                - Long lower shadow (2x+ body)
                - Bullish reversal at bottom
                - Shows rejection of lows
                
                ### Shooting Star:
                - Small body at bottom
                - Long upper shadow (2x+ body)
                - Bearish reversal at top
                - Shows rejection of highs
                
                ## Pro Tips
                
                ✅ Never trade single candle in isolation
                ✅ Always consider trend context
                ✅ Volume confirmation is essential
                ✅ Combine with support/resistance
                ✅ Multiple timeframe analysis
                ✅ Look for candle patterns (groups)
                ✅ Shadow length reveals intraday battle
                ✅ Body size shows session conviction
                
                ## Next Steps
                
                In the following lessons, we'll explore specific candlestick patterns:
                - Doji and Spinning Tops
                - Hammer and Hanging Man
                - Engulfing Patterns
                - Star Patterns
                - And many more...
                
                Understanding basic candlestick anatomy is foundation for recognizing these powerful patterns!
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What does a long upper shadow on a candlestick indicate?",
                        options = listOf("Strong buying", "Rejection at higher prices by sellers", "Support found", "Trend continuation"),
                        correctAnswer = 1,
                        explanation = "A long upper shadow shows that price moved higher during the period but was rejected and pushed back down, indicating sellers overcame buyers at higher levels."
                    ),
                    QuizQuestion(
                        question = "What does a small body (real body) on a candlestick signify?",
                        options = listOf("Strong conviction", "Indecision or minimal price change", "Always bullish", "Always bearish"),
                        correctAnswer = 1,
                        explanation = "A small body indicates that the open and close prices were very close, showing indecision in the market with minimal directional movement during that period."
                    ),
                    QuizQuestion(
                        question = "Why is volume confirmation important when analyzing candlesticks?",
                        options = listOf("It's not important", "High volume with strong candles shows conviction and reliability", "Volume only matters for stocks", "Volume determines candle color"),
                        correctAnswer = 1,
                        explanation = "Volume confirms the strength of a candle's signal. High volume with a strong candle shows real conviction, while low volume may indicate a weak or unreliable signal."
                    )
                )
            )
        )
