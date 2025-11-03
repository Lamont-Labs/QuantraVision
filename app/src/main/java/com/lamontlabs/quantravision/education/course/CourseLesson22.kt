package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.Lesson
import com.lamontlabs.quantravision.education.EducationCourse.PatternExample
import com.lamontlabs.quantravision.education.EducationCourse.Quiz
import com.lamontlabs.quantravision.education.EducationCourse.Question


val courseLesson22 = Lesson(
            id = 22,
            title = "Fibonacci Integration",
            description = "Combine Fibonacci retracements and extensions with patterns for precise entry, exit, and target levels",
            content = """
                Fibonacci ratios enhance pattern trading by providing mathematical precision for entries, stops, and targets. Integration of Fibonacci with patterns creates high-probability setups.
                
                **Fibonacci Retracement Levels:**
                - 23.6% - Shallow retracement
                - 38.2% - Moderate retracement
                - 50% - Psychological midpoint
                - 61.8% - Golden Ratio (most important)
                - 78.6% - Deep retracement
                
                **Using Retracements with Patterns:**
                
                **Triangle Patterns:**
                - Measure from breakout point
                - First target: 38.2% of prior trend
                - Second target: 61.8% extension
                - Final target: 100% measured move
                
                **Head & Shoulders:**
                - Measure head to neckline
                - Target 1: 61.8% of that distance
                - Target 2: 100% measured move
                - Target 3: 161.8% extension (rare)
                
                **Fibonacci Extension Levels:**
                - 127.2% - First extension target
                - 161.8% - Golden Ratio extension
                - 200% - Double the move
                - 261.8% - Advanced target
                
                **Pattern Entry Refinement:**
                
                **Flag Patterns:**
                - Flag pullback to 38.2-50% of pole
                - Entry at 61.8% retracement max
                - Deeper pullback invalidates pattern
                - Extension targets at 127% and 161.8%
                
                **Wedge Reversals:**
                - Entry at wedge breakout
                - Target 1: 38.2% retracement back
                - Target 2: 61.8% retracement
                - Final target: Pattern origin point
                
                **Confluence Zones:**
                Multiple Fibonacci levels + pattern = high probability
                - Pattern support + 61.8% retracement = strong buy zone
                - Pattern resistance + 161.8% extension = take profit
                - 3+ levels converging = institutional zone
                
                **Professional Fibonacci Trading:**
                - Always measure from significant swing points
                - Use multiple timeframe Fibonacci levels
                - Look for confluence with pattern levels
                - Adjust stop loss to Fibonacci levels
                - Scale out at each Fibonacci target
            """.trimIndent(),
            keyPoints = listOf(
                "61.8% Golden Ratio most reliable retracement level",
                "Fibonacci extensions provide precise profit targets",
                "Confluence of Fibonacci + pattern levels = high probability",
                "Use multiple timeframes for stronger zones",
                "Scale out at Fibonacci targets - don't exit all at once"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Fibonacci Flag Entry",
                    description = "Flag pattern with Fibonacci precision",
                    identificationTips = listOf(
                        "Measure flagpole from breakout to high",
                        "Flag pullback to 38.2-50% ideal entry",
                        "Entry confirmed at 61.8% retracement max",
                        "Stop below flag at 78.6% level",
                        "Target 1: 127% extension of pole",
                        "Target 2: 161.8% extension"
                    )
                ),
                PatternExample(
                    patternName = "Confluence Zone Trading",
                    description = "Multiple Fibonacci levels converging",
                    identificationTips = listOf(
                        "Pattern support at $100",
                        "61.8% retracement at $100.50",
                        "200-day MA at $99.75",
                        "All levels converge $99.75-$100.50",
                        "High probability reversal zone",
                        "Tight stop below zone, strong R:R"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "Which Fibonacci retracement is the Golden Ratio?",
                        options = listOf(
                            "38.2%",
                            "50%",
                            "61.8%",
                            "78.6%"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "61.8% is the Golden Ratio, the most reliable and watched Fibonacci retracement level."
                    ),
                    Question(
                        question = "What is a confluence zone?",
                        options = listOf(
                            "Random price level",
                            "Multiple Fibonacci and pattern levels converging",
                            "Single support line",
                            "Volume indicator"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Confluence zone is where multiple Fibonacci levels, pattern levels, or other support/resistance converge."
                    ),
                    Question(
                        question = "For a flag pattern, what is the ideal pullback depth?",
                        options = listOf(
                            "23.6-38.2%",
                            "38.2-50%",
                            "61.8-78.6%",
                            "Over 78.6%"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Ideal flag pullback is 38.2-50% of the pole; deeper pullbacks may invalidate the pattern."
                    ),
                    Question(
                        question = "What is the first Fibonacci extension target?",
                        options = listOf(
                            "100%",
                            "127.2%",
                            "161.8%",
                            "200%"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "127.2% is typically the first Fibonacci extension target beyond the 100% measured move."
                    )
                )
            )
        )
