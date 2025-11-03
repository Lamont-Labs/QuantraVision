package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.Lesson
import com.lamontlabs.quantravision.education.EducationCourse.PatternExample
import com.lamontlabs.quantravision.education.EducationCourse.Quiz
import com.lamontlabs.quantravision.education.EducationCourse.Question


val courseLesson19 = Lesson(
            id = 19,
            title = "Gartley & Bat Patterns",
            description = "Trade the most popular harmonic patterns with precise Fibonacci-based entry and exit strategies",
            content = """
                The Gartley and Bat patterns are the most commonly traded harmonic formations, offering high-probability reversal setups with exact entry points.
                
                **Gartley Pattern (222):**
                Named after H.M. Gartley who introduced it in 1935:
                - AB retracement: 61.8% of XA
                - BC retracement: 38.2%-88.6% of AB
                - CD extension: 127%-161.8% of BC
                - Point D: 78.6% retracement of XA
                - PRZ at convergence of CD and XA ratios
                - Most common harmonic pattern
                
                **Bullish Gartley:**
                - Forms after downtrend
                - X at high, A at low
                - Enter long at point D (PRZ)
                - Stop: Just beyond X point
                - Target 1: Point C
                - Target 2: Point A
                
                **Bearish Gartley:**
                - Forms after uptrend
                - X at low, A at high
                - Enter short at point D (PRZ)
                - Same targeting as bullish version
                
                **Bat Pattern:**
                More aggressive than Gartley with tighter retracement:
                - AB retracement: 38.2%-50% of XA
                - BC retracement: 38.2%-88.6% of AB
                - CD extension: 161.8%-261.8% of BC
                - Point D: 88.6% retracement of XA (critical)
                - Tighter stop loss than Gartley
                - Named for bat-wing appearance
                
                **Key Differences:**
                - Bat has shallower B point (38.2-50% vs 61.8%)
                - Bat D point at 88.6% vs Gartley 78.6%
                - Bat offers tighter risk/reward
                - Gartley more common, Bat more precise
                
                **Trading Rules:**
                - Must have exact Fibonacci ratios
                - Wait for candlestick confirmation at D
                - Enter only in PRZ convergence zone
                - Scale out at targets C and A
                - Move stop to breakeven after target 1
            """.trimIndent(),
            keyPoints = listOf(
                "Gartley: AB=61.8% XA, D=78.6% XA retracement",
                "Bat: AB=38.2-50% XA, D=88.6% XA retracement",
                "Both require precise Fibonacci ratios for validity",
                "Enter at point D with candlestick confirmation",
                "Target points C and A for profit taking"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Bullish Gartley",
                    description = "Most popular harmonic pattern",
                    identificationTips = listOf(
                        "Identify XA leg downward move",
                        "B retraces to 61.8% of XA",
                        "C retraces 38.2-88.6% of AB",
                        "D completes at 78.6% of XA",
                        "CD = 127-161.8% extension of BC",
                        "Enter long at D with bullish reversal candle"
                    )
                ),
                PatternExample(
                    patternName = "Bearish Bat",
                    description = "Precise harmonic with tight stop",
                    identificationTips = listOf(
                        "XA leg moves upward",
                        "B retraces 38.2-50% of XA (shallower)",
                        "C retraces 38.2-88.6% of AB",
                        "D must hit 88.6% of XA exactly",
                        "CD = 161.8-261.8% of BC",
                        "Very precise - offers best risk/reward"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "In a Gartley pattern, point B retraces what % of XA?",
                        options = listOf(
                            "38.2%",
                            "50%",
                            "61.8%",
                            "78.6%"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "In a Gartley pattern, point B retraces 61.8% (Golden Ratio) of the XA leg."
                    ),
                    Question(
                        question = "What is the critical D point retracement for a Bat pattern?",
                        options = listOf(
                            "61.8%",
                            "78.6%",
                            "88.6%",
                            "100%"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Bat patterns require point D to complete at exactly 88.6% retracement of XA - this is critical."
                    ),
                    Question(
                        question = "Which pattern offers a tighter stop loss?",
                        options = listOf(
                            "Gartley",
                            "Bat",
                            "Both equal",
                            "Neither has a stop"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Bat pattern offers tighter stop loss due to its deeper D point (88.6%) closer to the reversal zone."
                    ),
                    Question(
                        question = "What are the profit targets for harmonic patterns?",
                        options = listOf(
                            "Random levels",
                            "Points C and A",
                            "Only point X",
                            "No specific targets"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Standard harmonic targets are first to point C (partial exit) then point A (final target)."
                    )
                )
            )
        )
