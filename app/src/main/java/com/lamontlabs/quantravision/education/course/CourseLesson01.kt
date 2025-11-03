package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.Lesson
import com.lamontlabs.quantravision.education.EducationCourse.PatternExample
import com.lamontlabs.quantravision.education.EducationCourse.Quiz
import com.lamontlabs.quantravision.education.EducationCourse.Question


val courseLesson01 = Lesson(
            id = 1,
            title = "Introduction to Chart Patterns",
            description = "Learn what chart patterns are and why they matter in trading",
            content = """
                Chart patterns are recognizable formations on price charts that traders use to predict future price movements with higher probability than random guessing. These visual formations represent the battle between buyers and sellers, creating predictable geometric shapes that repeat throughout market history.
                
                **Why Patterns Work:**
                - They represent collective psychology of market participants - fear, greed, hope, and panic manifest in price action
                - Historical repetition creates predictable outcomes - human behavior doesn't change, so patterns repeat
                - They provide visual context for price action - easier to spot opportunities and risk zones
                - Markets move in trends and consolidations - patterns help identify which phase you're in
                - Institutional traders use the same patterns - creating self-fulfilling prophecies when many traders act on the same signals
                
                **Pattern Categories:**
                1. **Continuation Patterns** - Suggest the current trend will continue after brief consolidation (flags, pennants, rectangles)
                2. **Reversal Patterns** - Indicate potential trend change from up to down or vice versa (head and shoulders, double tops/bottoms)
                3. **Bilateral Patterns** - Can break in either direction depending on market pressure (symmetrical triangles, rectangles)
                
                **Key Concepts You Must Understand:**
                - Support: Price level where buying pressure prevents further decline - acts as a floor
                - Resistance: Price level where selling pressure prevents further rise - acts as a ceiling
                - Breakout: Price moves decisively beyond established support/resistance with increased volume
                - Volume: Confirms pattern validity (higher volume on breakout = stronger, more reliable signal)
                - Timeframe: Same pattern has different implications on 5-minute vs daily chart
                
                **Practical Application:**
                Patterns work best when combined with volume analysis, support/resistance levels, and trend direction. Never trade a pattern in isolation - always confirm with multiple factors. The most reliable patterns occur at key support or resistance levels with increasing volume.
                
                **Common Beginner Mistakes:**
                - Trading before breakout confirmation (getting caught in false moves)
                - Ignoring volume (low volume breakouts often fail)
                - Not using stop losses (patterns can fail - protect your capital)
                - Forcing patterns that aren't there (seeing what you want to see)
                
                **Recognition Tips:**
                Start by identifying major support and resistance levels on your chart. Then look for price consolidating between these levels. Patterns take time to form - be patient. The cleaner and more textbook the pattern, the more reliable it tends to be.
            """.trimIndent(),
            keyPoints = listOf(
                "Patterns reflect market psychology",
                "Continuation vs Reversal patterns",
                "Support and resistance are critical",
                "Volume confirms pattern validity",
                "Timeframes matter - same pattern, different implications"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Head and Shoulders",
                    description = "Classic reversal pattern with three peaks",
                    identificationTips = listOf(
                        "Middle peak (head) higher than shoulders",
                        "Neckline connects troughs",
                        "Volume decreases on head formation",
                        "Breakout below neckline confirms reversal"
                    )
                ),
                PatternExample(
                    patternName = "Bull Flag",
                    description = "Continuation pattern showing brief consolidation in uptrend",
                    identificationTips = listOf(
                        "Sharp upward move forms flagpole",
                        "Brief downward consolidation forms flag",
                        "Flag slopes against trend direction",
                        "Volume decreases during flag formation",
                        "Breakout above flag resistance continues uptrend",
                        "Volume spike confirms breakout validity"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What do chart patterns primarily represent?",
                        options = listOf(
                            "Random price movements",
                            "Collective market psychology",
                            "Computer algorithms",
                            "Government manipulation"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Chart patterns represent the collective psychology and behavior of all market participants."
                    ),
                    Question(
                        question = "Which pattern type suggests the current trend will continue?",
                        options = listOf(
                            "Reversal patterns",
                            "Bilateral patterns",
                            "Continuation patterns",
                            "Harmonic patterns"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Continuation patterns like flags and pennants suggest the existing trend will resume after consolidation."
                    ),
                    Question(
                        question = "What confirms the validity of a chart pattern?",
                        options = listOf(
                            "Pattern size",
                            "Trading volume",
                            "Color of candles",
                            "Day of the week"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Increased trading volume during pattern formation and breakout confirms its validity and strength."
                    ),
                    Question(
                        question = "What is a breakout in chart patterns?",
                        options = listOf(
                            "A pattern that doesn't work",
                            "Price moving beyond established support or resistance",
                            "A gap in the chart",
                            "When volume decreases significantly"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "A breakout occurs when price moves beyond established support or resistance levels, often signaling a new trend."
                    )
                )
            )
        )
