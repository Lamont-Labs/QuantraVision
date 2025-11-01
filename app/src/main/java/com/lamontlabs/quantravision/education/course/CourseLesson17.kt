package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.*


val courseLesson17 = Lesson(
            id = 17,
            title = "Gap Patterns (Breakaway, Runaway, Exhaustion)",
            description = "Understand different gap types and how to trade them for maximum profit and minimal risk",
            content = """
                Gaps are price discontinuities where no trading occurs between two price levels. Different gap types provide distinct trading opportunities and signals.
                
                **Breakaway Gap:**
                - Occurs at start of new trend
                - Breaks out of consolidation or pattern
                - High volume accompanies gap
                - Rarely filled - signals strong momentum
                - Entry: After gap confirmation
                - Stop: Below gap (bullish) or above (bearish)
                - Most reliable gap type
                - Marks beginning of significant moves
                
                **Runaway Gap (Continuation Gap):**
                - Occurs mid-trend during strong momentum
                - Shows powerful buying/selling pressure
                - Moderate to high volume
                - Often appears halfway through move
                - Target: Measure from start to gap, project equal distance
                - Multiple runaway gaps = very strong trend
                - Rarely filled until trend exhausts
                
                **Exhaustion Gap:**
                - Appears at end of trend
                - Final push before reversal
                - Often filled quickly (days to weeks)
                - High volume but fails to follow through
                - Signal to exit positions or take profits
                - Marks climactic buying/selling
                - Often accompanied by reversal patterns
                
                **Common Gap:**
                - Occurs within trading ranges
                - Low volume, low significance
                - Usually filled quickly
                - Not actionable - ignore these gaps
                - Common in low liquidity situations
                
                **Gap Trading Rules:**
                - Don't chase exhaustion gaps
                - Trade with breakaway and runaway gaps
                - Volume confirms gap significance
                - Failed gaps (filled quickly) reverse trade
                - Gaps act as support (bullish) or resistance (bearish)
            """.trimIndent(),
            keyPoints = listOf(
                "Breakaway gaps start trends - rarely filled",
                "Runaway gaps appear mid-trend - measure halfway point",
                "Exhaustion gaps signal trend end - often filled quickly",
                "Volume confirms gap significance and reliability",
                "Unfilled gaps become support/resistance levels"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Breakaway Gap",
                    description = "Gap marking new trend beginning",
                    identificationTips = listOf(
                        "Gaps out of consolidation pattern",
                        "High volume confirms strength",
                        "Price doesn't return to fill gap",
                        "Marks start of significant move",
                        "Trade in direction of gap",
                        "Stop below/above gap zone"
                    )
                ),
                PatternExample(
                    patternName = "Exhaustion Gap",
                    description = "Final gap before reversal",
                    identificationTips = listOf(
                        "Appears after extended trend",
                        "Initial high volume but momentum fades",
                        "Often filled within days or weeks",
                        "Accompanied by reversal candlesticks",
                        "Signal to exit trend trades",
                        "Look for opposite direction entry"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "Which gap type marks the start of a new trend?",
                        options = listOf(
                            "Common Gap",
                            "Breakaway Gap",
                            "Exhaustion Gap",
                            "Runaway Gap"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Breakaway Gaps occur at the start of new trends, breaking out of consolidation with high volume."
                    ),
                    Question(
                        question = "What typically happens to Exhaustion Gaps?",
                        options = listOf(
                            "They never get filled",
                            "They get filled quickly",
                            "They expand wider",
                            "They disappear"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Exhaustion Gaps typically get filled quickly (days to weeks) as the trend reverses."
                    ),
                    Question(
                        question = "Runaway Gaps often appear where in a trend?",
                        options = listOf(
                            "At the beginning",
                            "At the end",
                            "Halfway through the move",
                            "Randomly placed"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Runaway Gaps typically appear about halfway through a trend, helping measure the potential move."
                    ),
                    Question(
                        question = "What confirms the significance of a gap?",
                        options = listOf(
                            "Time of day",
                            "Trading volume",
                            "Day of week",
                            "Color of candle"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "High trading volume confirms gap significance - low volume gaps are often meaningless."
                    )
                )
            )
        )
