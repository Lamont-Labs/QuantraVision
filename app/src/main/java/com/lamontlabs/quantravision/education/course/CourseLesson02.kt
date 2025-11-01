package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.*


val courseLesson02 = Lesson(
            id = 2,
            title = "Candlestick Patterns Basics",
            description = "Master fundamental single and multi-candle patterns",
            content = """
                Candlestick patterns are the foundation of technical analysis, originating from 18th century Japanese rice traders who used them to predict rice prices. These patterns provide crucial insight into market sentiment and potential reversals or continuations.
                
                **Anatomy of a Candlestick:**
                - Body: Range between open and close price - shows the main battle
                - Wick/Shadow: High and low extremes - shows rejected prices
                - Color: Green/white (bullish - close above open) / Red/black (bearish - close below open)
                - Size: Larger bodies show stronger conviction, small bodies show indecision
                
                **Key Single-Candle Patterns:**
                
                **1. Doji** - Indecision Signal
                - Open equals close (tiny or no body)
                - Long wicks indicate price rejection at both ends
                - Signals potential reversal when appearing at trend extremes
                - In uptrend: warns of exhaustion
                - In downtrend: hints at potential bottom
                - Requires confirmation from next candle
                
                **2. Hammer** - Bullish Reversal
                - Small body at the top of candle
                - Long lower wick (at least 2x the body size)
                - Appears after significant downtrend
                - Shows sellers pushed price low but buyers regained control
                - Confirmation: Next candle closes higher
                
                **3. Shooting Star** - Bearish Reversal
                - Small body at bottom of candle
                - Long upper wick showing rejection of higher prices
                - Appears after uptrend
                - Indicates buyers pushed high but sellers took control
                - Mirror opposite of hammer
                
                **4. Marubozu** - Strong Momentum
                - No wicks or very small wicks
                - Large body showing strong directional move
                - Bullish marubozu: opens at low, closes at high
                - Bearish marubozu: opens at high, closes at low
                - Shows dominant buyers or sellers
                
                **Multi-Candle Patterns:**
                
                **5. Engulfing** - Powerful Reversal
                - Second candle's body completely engulfs first candle's body
                - Bullish engulfing: Small red then large green (after downtrend)
                - Bearish engulfing: Small green then large red (after uptrend)
                - Larger the engulfing candle, stronger the signal
                - Higher volume on engulfing candle confirms validity
                
                **6. Morning/Evening Star** - Major Reversal
                - Three-candle formation signaling trend reversal
                - Morning star (bullish): Down candle, small body (gap down), up candle (gap up)
                - Evening star (bearish): Up candle, small body (gap up), down candle (gap down)
                - Middle candle shows indecision, third candle confirms new direction
                - One of the most reliable reversal patterns
                
                **Trading Tips:**
                Look for candlestick patterns at key support/resistance levels for best reliability. Always confirm patterns with volume - higher volume increases probability of success. Wait for confirmation candle before entering trades. Combine candlesticks with chart patterns for highest probability setups.
            """.trimIndent(),
            keyPoints = listOf(
                "Body shows open-to-close range",
                "Wicks show rejection of prices",
                "Doji signals indecision",
                "Engulfing patterns very reliable",
                "Context matters - where pattern forms"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Bullish Engulfing",
                    description = "Strong reversal after downtrend",
                    identificationTips = listOf(
                        "Occurs at support or after downtrend",
                        "Green candle fully covers red candle",
                        "Larger the engulfing, stronger the signal",
                        "Confirm with volume increase"
                    )
                ),
                PatternExample(
                    patternName = "Doji",
                    description = "Indecision candle with equal open/close",
                    identificationTips = listOf(
                        "Body is very small or nonexistent",
                        "At trend extremes = potential reversal",
                        "In consolidation = continued indecision",
                        "Needs confirmation from next candle"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What does a Doji candlestick indicate?",
                        options = listOf(
                            "Strong bullish momentum",
                            "Market indecision",
                            "Definite reversal",
                            "High volatility only"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "A Doji shows market indecision where buyers and sellers are balanced (open equals close)."
                    ),
                    Question(
                        question = "Where should a Hammer pattern appear?",
                        options = listOf(
                            "At the top of an uptrend",
                            "After a downtrend",
                            "In the middle of consolidation",
                            "Only on daily timeframes"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "A Hammer is a bullish reversal pattern that appears after a downtrend, signaling potential bottom."
                    ),
                    Question(
                        question = "In a Bullish Engulfing pattern, what happens?",
                        options = listOf(
                            "A red candle engulfs a green candle",
                            "A green candle completely covers a red candle",
                            "Two candles have the same size",
                            "Three candles form a triangle"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "A Bullish Engulfing has a green candle that completely engulfs the previous red candle, showing strong buying."
                    ),
                    Question(
                        question = "What part of a candlestick shows the high and low extremes?",
                        options = listOf(
                            "The body",
                            "The wick or shadow",
                            "The color",
                            "The opening price"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "The wick (or shadow) shows the high and low extremes of the trading period, indicating price rejection levels."
                    )
                )
            )
        )
