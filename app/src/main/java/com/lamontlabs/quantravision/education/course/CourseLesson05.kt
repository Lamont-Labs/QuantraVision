package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.*


val courseLesson05 = Lesson(
            id = 5,
            title = "Wedge Patterns",
            description = "Learn rising and falling wedges for reversal signals",
            content = """
                Wedge patterns are powerful reversal indicators that often catch traders by surprise. Unlike triangles, wedges signal trend exhaustion rather than continuation, making them excellent early warning signals for major reversals.
                
                **Rising Wedge** (Bearish Reversal Pattern)
                - Both support and resistance trendlines slope upward in same direction
                - Progressively narrowing price action between the lines
                - Typically appears at the top of extended uptrend
                - Signals buyers are losing steam - each new high requires more effort
                - Volume consistently decreases as pattern develops
                - Breaks downward through support line (opposite to the slope)
                - The steeper the wedge, the more imminent the breakdown
                - Often forms over several weeks to months
                
                **Falling Wedge** (Bullish Reversal Pattern)
                - Both support and resistance lines slope downward together
                - Price consolidates in narrowing descending range
                - Forms most often at bottom of significant downtrend
                - Shows sellers becoming exhausted - each new low weaker than last
                - Volume contracts throughout pattern formation
                - Breaks upward through resistance (opposite to slope direction)
                - Confirms when price closes above resistance with volume expansion
                - Common at major market bottoms
                
                **Key Differences from Triangle Patterns:**
                - Wedges are reversal patterns (against the trend), triangles usually continuation
                - Both trendlines slope in the SAME direction (wedges) vs converging from opposite directions (triangles)
                - Wedges show trend exhaustion, triangles show consolidation
                - Wedges are typically steeper and narrower than triangles
                - Wedges break opposite to their slope, triangles can break either way
                
                **Trading Wedge Patterns Successfully:**
                - Entry: Enter on confirmed breakout opposite to wedge slope direction
                - Wait for candle close beyond trendline - don't chase wicks
                - Stop loss: Place just beyond the opposite trendline
                - Target: Beginning of wedge formation (measure full pattern height)
                - Confirmation: Look for volume spike of 50%+ on breakout
                - Retest entry: Wedge often retests broken trendline - provides second entry opportunity
                
                **Pattern Psychology:**
                - Rising wedge: Bulls make higher highs but with diminishing momentum - unsustainable
                - Each rally is weaker, showing buying pressure fading
                - Falling wedge: Bears push lower but with less conviction each time
                - Sellers running out of ammunition as lows become shallow
                - Narrowing range shows decreasing conviction and impending resolution
                
                **Common Mistakes:**
                - Entering before breakout confirmation
                - Confusing wedges with flags (flags are rectangular, wedges are triangular)
                - Missing the retest entry opportunity
                - Not respecting the volume requirement
            """.trimIndent(),
            keyPoints = listOf(
                "Rising wedges are bearish reversals",
                "Falling wedges are bullish reversals",
                "Both trendlines slope same direction",
                "Volume decreases as wedge forms",
                "Breakout opposite to slope direction"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Rising Wedge",
                    description = "Bearish reversal at trend top",
                    identificationTips = listOf(
                        "Both lines slope upward",
                        "Price makes higher highs but weakly",
                        "Volume diminishes throughout",
                        "Breaks downward through support",
                        "Often preceded by strong rally"
                    )
                ),
                PatternExample(
                    patternName = "Falling Wedge",
                    description = "Bullish reversal at trend bottom",
                    identificationTips = listOf(
                        "Both lines slope downward",
                        "Lower lows but with less conviction",
                        "Contracting volatility",
                        "Breaks upward through resistance",
                        "Forms after significant decline"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "A Rising Wedge is typically a:",
                        options = listOf(
                            "Bullish continuation",
                            "Bearish reversal",
                            "Neutral pattern",
                            "Bullish reversal"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Rising Wedges are bearish reversal patterns that signal upward momentum is exhausted."
                    ),
                    Question(
                        question = "In which direction do both trendlines slope in a Falling Wedge?",
                        options = listOf(
                            "Upward",
                            "Downward",
                            "One up, one down",
                            "Horizontal"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "In a Falling Wedge, both the support and resistance lines slope downward, converging."
                    ),
                    Question(
                        question = "What happens to volume as a wedge pattern forms?",
                        options = listOf(
                            "Steadily increases",
                            "Stays constant",
                            "Decreases",
                            "Becomes extremely volatile"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Volume typically decreases as a wedge forms, showing diminishing conviction before the reversal."
                    ),
                    Question(
                        question = "What is the key difference between wedges and triangles?",
                        options = listOf(
                            "Wedges are continuation, triangles are reversal",
                            "Wedges have both lines sloping the same direction, signaling reversal",
                            "There is no difference",
                            "Wedges only form on hourly charts"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Wedges have both trendlines sloping in the same direction and signal reversal, while triangles have converging lines and often continue the trend."
                    )
                )
            )
        )
