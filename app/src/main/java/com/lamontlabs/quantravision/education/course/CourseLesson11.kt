package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.Lesson
import com.lamontlabs.quantravision.education.EducationCourse.PatternExample
import com.lamontlabs.quantravision.education.EducationCourse.Quiz
import com.lamontlabs.quantravision.education.EducationCourse.Question


val courseLesson11 = Lesson(
            id = 11,
            title = "Symmetrical Triangles & Diamonds",
            description = "Master bilateral consolidation patterns and probability weighting",
            content = """
                Symmetrical triangles and diamond patterns represent periods of market indecision where neither bulls nor bears have clear control. These bilateral patterns can break in either direction, but statistical analysis shows they usually continue the prevailing trend.
                
                **Symmetrical Triangle Structure:**
                - Converging trendlines with progressively lower highs and higher lows
                - Both buyers and sellers become less aggressive with each swing
                - Decreasing volume as pattern develops shows declining interest and volatility
                - Breakout direction remains uncertain until actual breakout occurs
                - Typically forms mid-trend as continuation pattern (consolidation before next leg)
                - Requires minimum 4 touchpoints (2 on each trendline) to be valid
                - Pattern usually completes in 1-3 months on daily charts
                
                **Diamond Pattern Characteristics:**
                - Rare but highly reliable reversal formation
                - Two-phase structure: broadening formation followed by contraction
                - Creates diamond or rhombus shape when trendlines are drawn
                - Resembles head & shoulders but with symmetrical shoulders on both sides
                - Powerful reversal signal when it occurs at major tops or bottoms
                - Volume pattern: increases during broadening phase, decreases during contraction
                - Often marks significant market turning points
                
                **Trading Symmetrical Triangles Successfully:**
                - Patience is key - wait for confirmed breakout (candle close beyond trendline)
                - Volume must increase significantly on breakout (50%+ above average validates the move)
                - Measure pattern height at the widest point (beginning of triangle)
                - Project that height from breakout point to calculate price target
                - Place stop loss on opposite side of triangle just inside the pattern
                - Best entry often comes on retest of broken trendline
                - Avoid trading inside the triangle - wait for clear directional move
                
                **Probability Weighting and Statistics:**
                - In established uptrends: approximately 65% probability of upward breakout
                - In established downtrends: approximately 65% probability of downward breakout
                - In no clear trend: 50/50 probability - harder to trade
                - Breakout usually occurs at 2/3 to 3/4 through the pattern width
                - If price reaches apex without breaking, pattern loses validity
                - False breakouts are common - always require volume confirmation
                - Higher timeframe triangles more reliable than lower timeframes
                
                **Common Trading Mistakes:**
                - Entering before breakout (getting whipsawed inside the consolidation)
                - Ignoring volume on breakout (most false breakouts have weak volume)
                - Trading against the prevailing trend (always favor trend direction)
                - Setting unrealistic targets beyond measured move
                - Not using stop losses
            """.trimIndent(),
            keyPoints = listOf(
                "Symmetrical triangles show equilibrium between buyers and sellers",
                "Breakout direction follows prevailing trend 65% of the time",
                "Volume confirmation essential - decreases during pattern, increases on breakout",
                "Diamond patterns are rare but highly reliable reversal signals",
                "Measure and project pattern height for price targets"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Symmetrical Triangle",
                    description = "Bilateral consolidation pattern",
                    identificationTips = listOf(
                        "Draw converging trendlines connecting highs and lows",
                        "At least 2 higher lows and 2 lower highs required",
                        "Volume should decrease as pattern forms",
                        "Breakout typically occurs at 2/3 of pattern width",
                        "Measure widest point for target calculation"
                    )
                ),
                PatternExample(
                    patternName = "Diamond Top",
                    description = "Rare but powerful reversal pattern",
                    identificationTips = listOf(
                        "Broadening phase followed by narrowing phase",
                        "Forms after strong uptrend",
                        "Volume expands then contracts with price",
                        "Breakdown confirms reversal",
                        "Target = pattern height projected downward"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What is the breakout probability for symmetrical triangles in an uptrend?",
                        options = listOf(
                            "50% either direction",
                            "65% upward",
                            "80% upward",
                            "100% upward"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Symmetrical triangles in uptrends have approximately 65% probability of breaking upward, continuing the trend."
                    ),
                    Question(
                        question = "When does a symmetrical triangle typically break out?",
                        options = listOf(
                            "At the beginning of the pattern",
                            "Exactly at the apex",
                            "At 2/3 to 3/4 of the pattern width",
                            "Never predictable"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Breakouts typically occur at 2/3 to 3/4 of the pattern width, before reaching the apex."
                    ),
                    Question(
                        question = "What makes a diamond pattern particularly significant?",
                        options = listOf(
                            "It's very common",
                            "It's rare and highly reliable as reversal",
                            "It always breaks upward",
                            "No volume is needed"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Diamond patterns are rare but when they occur, they're highly reliable reversal signals."
                    ),
                    Question(
                        question = "How should volume behave during a symmetrical triangle?",
                        options = listOf(
                            "Constantly increasing",
                            "Decrease during formation, increase on breakout",
                            "Remain constant",
                            "Volume doesn't matter"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Volume should decrease as the pattern forms (consolidation) and increase sharply on breakout (confirmation)."
                    )
                )
            )
        )
