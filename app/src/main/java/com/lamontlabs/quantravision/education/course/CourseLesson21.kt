package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.Lesson
import com.lamontlabs.quantravision.education.EducationCourse.PatternExample
import com.lamontlabs.quantravision.education.EducationCourse.Quiz
import com.lamontlabs.quantravision.education.EducationCourse.Question


val courseLesson21 = Lesson(
            id = 21,
            title = "Volume & Momentum Confirmation",
            description = "Use volume analysis and momentum indicators to confirm pattern validity and filter false breakouts",
            content = """
                Volume and momentum are critical confirmation tools that separate profitable pattern trades from costly false breakouts. Never trade patterns without confirmation.
                
                **Volume Confirmation Principles:**
                
                **1. Volume Precedes Price**
                - Accumulation shows in rising volume before breakout
                - Smart money positions before retail notices
                - Increasing volume = stronger pattern
                - Decreasing volume = weakening pattern
                
                **2. Breakout Volume Rules**
                - Valid breakout needs 50%+ above average volume
                - Higher volume = higher probability success
                - Low volume breakout = likely false move
                - Volume surge confirms institutional participation
                
                **3. On-Balance Volume (OBV)**
                - Cumulative volume indicator
                - Rising OBV + rising price = healthy uptrend
                - Falling OBV + rising price = bearish divergence
                - Leads price changes by days or weeks
                - Confirms accumulation/distribution
                
                **Momentum Indicators:**
                
                **4. RSI (Relative Strength Index)**
                - Measures momentum from 0-100
                - Above 50 = bullish momentum
                - Below 50 = bearish momentum
                - Bullish pattern + RSI > 50 = high probability
                - Divergence warns of reversal
                
                **5. MACD (Moving Average Convergence Divergence)**
                - Trend following momentum indicator
                - MACD line crosses signal line = momentum shift
                - Histogram shows momentum strength
                - Bullish crossover confirms pattern breakout
                - Zero line cross confirms trend change
                
                **6. Momentum Divergence**
                - Price makes new high, momentum doesn't = bearish
                - Price makes new low, momentum doesn't = bullish
                - Early warning signal for reversals
                - Combine with reversal patterns for best trades
                
                **Confirmation Checklist:**
                - Pattern structure valid ✓
                - Volume increasing into breakout ✓
                - Momentum indicator confirms direction ✓
                - No bearish divergences ✓
                - Risk/reward minimum 1:2 ✓
            """.trimIndent(),
            keyPoints = listOf(
                "Breakout needs 50%+ above average volume",
                "OBV confirms accumulation or distribution",
                "RSI above 50 confirms bullish momentum",
                "MACD crossovers validate pattern breakouts",
                "Momentum divergence warns of reversals"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Volume-Confirmed Breakout",
                    description = "Triangle breakout with volume surge",
                    identificationTips = listOf(
                        "Ascending triangle forms over 6 weeks",
                        "Volume decreases during consolidation",
                        "Breakout with 2x average volume",
                        "OBV trending up throughout pattern",
                        "RSI above 50 and rising",
                        "High probability continuation"
                    )
                ),
                PatternExample(
                    patternName = "Bearish Divergence Warning",
                    description = "Price high but momentum failing",
                    identificationTips = listOf(
                        "Price makes new high",
                        "RSI makes lower high (divergence)",
                        "MACD histogram declining",
                        "Volume decreasing on rally",
                        "Warning sign for reversal",
                        "Prepare to exit long positions"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What volume increase confirms a valid breakout?",
                        options = listOf(
                            "Any increase",
                            "10% above average",
                            "50% above average",
                            "Volume doesn't matter"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "A valid breakout should have at least 50% above average volume to confirm institutional participation."
                    ),
                    Question(
                        question = "What does bearish divergence indicate?",
                        options = listOf(
                            "Strong uptrend continuation",
                            "Price high but weakening momentum",
                            "Random market noise",
                            "Volume is increasing"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Bearish divergence occurs when price makes new highs but momentum indicators fail to confirm, warning of reversal."
                    ),
                    Question(
                        question = "RSI above 50 indicates:",
                        options = listOf(
                            "Bearish momentum",
                            "Neutral market",
                            "Bullish momentum",
                            "Overbought condition"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "RSI above 50 indicates bullish momentum, confirming upward price movements and patterns."
                    ),
                    Question(
                        question = "What does OBV measure?",
                        options = listOf(
                            "Price only",
                            "Cumulative volume flow",
                            "Moving averages",
                            "Candlestick patterns"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "OBV (On-Balance Volume) measures cumulative volume flow, confirming accumulation or distribution."
                    )
                )
            )
        )
