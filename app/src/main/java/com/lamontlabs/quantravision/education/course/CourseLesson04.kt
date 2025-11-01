package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.*


val courseLesson04 = Lesson(
            id = 4,
            title = "Triangle Patterns",
            description = "Master ascending, descending, and symmetrical triangles",
            content = """
                Triangle patterns are among the most common continuation patterns, forming as price consolidates before the next significant move. They represent a battle between buyers and sellers that eventually results in a decisive breakout.
                
                **Types of Triangles and Their Implications:**
                
                **1. Ascending Triangle** (Typically Bullish)
                - Flat horizontal resistance at the top
                - Rising bottom support showing progressively higher lows
                - Typically breaks upward (70% probability in uptrend)
                - Shows buyers becoming increasingly aggressive with each test
                - Each low is higher than the previous - buyers stepping in earlier
                - Forms most often as continuation pattern in uptrends
                - Requires minimum of 2 touches on resistance, 2 rising lows
                
                **2. Descending Triangle** (Typically Bearish)
                - Flat horizontal support at the bottom
                - Declining top resistance creating lower highs
                - Usually breaks downward (70% probability in downtrend)
                - Shows sellers becoming more aggressive
                - Each high is lower - sellers more willing to sell at lower prices
                - Forms as continuation in downtrends, occasionally at tops
                - Breakdown often sharp and swift
                
                **3. Symmetrical Triangle** (Bilateral/Neutral)
                - Converging trendlines from both sides
                - Lower highs AND higher lows simultaneously
                - Can break in either direction
                - Direction typically continues the prior trend (65% probability)
                - Represents equilibrium - neither bulls nor bears in control
                - Breakout usually occurs 2/3 to 3/4 through the pattern
                - Most common as mid-trend consolidation
                
                **Trading Triangle Patterns Successfully:**
                - Wait for confirmed breakout - price must close beyond trendline, not just wick through
                - Volume should increase 50-100% on breakout - validates the move
                - Measure target: Take height of triangle at widest point, project from breakout
                - False breakouts are common - wait for daily/4H close beyond the line
                - Best entry: Initial breakout OR retest of broken trendline
                - Stop loss: Place just inside triangle on opposite side
                - Pattern typically takes 1-3 months to form (can be shorter on lower timeframes)
                
                **Common Mistakes to Avoid:**
                - Trading before confirmed breakout (getting whipsawed inside the triangle)
                - Ignoring volume on breakout (weak volume breakouts frequently fail and reverse)
                - Trading against the prior trend (always favor breakout direction of existing trend)
                - Entering too late - after triangle has already broken and extended too far
                - Not using stop losses (triangles can fail - protect your capital)
                
                **Recognition Tips:**
                Look for at least 4 touchpoints total (2 on each side). The more touches, the more valid the pattern. Volume should decrease as the triangle forms, then spike on breakout.
            """.trimIndent(),
            keyPoints = listOf(
                "Ascending triangles are bullish",
                "Descending triangles are bearish",
                "Symmetrical can go either way",
                "Volume confirms breakout",
                "Measure target from widest point"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Ascending Triangle",
                    description = "Bullish pattern with flat resistance",
                    identificationTips = listOf(
                        "At least two touches on flat top",
                        "Rising lows show buying pressure",
                        "Forms after uptrend (continuation)",
                        "Breakout above resistance with volume",
                        "Can take 1-3 months to form"
                    )
                ),
                PatternExample(
                    patternName = "Symmetrical Triangle",
                    description = "Neutral consolidation pattern",
                    identificationTips = listOf(
                        "Lower highs and higher lows",
                        "Converging trendlines",
                        "Usually continues prior trend",
                        "Breakout typically 2/3 into pattern",
                        "Decreasing volume until breakout"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What characterizes an Ascending Triangle?",
                        options = listOf(
                            "Flat bottom, declining top",
                            "Flat top, rising bottom",
                            "Both lines rising",
                            "Both lines descending"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Ascending Triangles have a flat top resistance and rising bottom support, showing increasing buying pressure."
                    ),
                    Question(
                        question = "Which triangle pattern is considered bilateral?",
                        options = listOf(
                            "Ascending Triangle",
                            "Descending Triangle",
                            "Symmetrical Triangle",
                            "Right Triangle"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Symmetrical Triangles are bilateral patterns that can break in either direction."
                    ),
                    Question(
                        question = "Where is the breakout most likely to occur in a triangle?",
                        options = listOf(
                            "At the very start",
                            "About 2/3 through the pattern",
                            "Only at the apex",
                            "Randomly anywhere"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Breakouts typically occur about 2/3 of the way through the triangle pattern, well before the apex."
                    ),
                    Question(
                        question = "How do you calculate the price target for a triangle pattern?",
                        options = listOf(
                            "Measure the pattern width at any point",
                            "Measure the height at the widest point and project from breakout",
                            "Double the triangle size",
                            "Triangles have no measurable targets"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Measure the height of the triangle at its widest point, then project that distance from the breakout point to find the target."
                    )
                )
            )
        )
