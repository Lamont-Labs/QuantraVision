package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.Lesson
import com.lamontlabs.quantravision.education.EducationCourse.PatternExample
import com.lamontlabs.quantravision.education.EducationCourse.Quiz
import com.lamontlabs.quantravision.education.EducationCourse.Question


val courseLesson18 = Lesson(
            id = 18,
            title = "Harmonic Pattern Foundations",
            description = "Master Fibonacci-based harmonic patterns for precise reversal zone identification and high-probability trades",
            content = """
                Harmonic patterns use precise Fibonacci ratios to create geometric price structures that identify exact potential reversal zones with mathematical precision. These patterns are among the most sophisticated trading tools available and offer exceptionally high accuracy when correctly identified and executed. Unlike traditional chart patterns, harmonics require strict ratio adherence.
                
                **Critical Fibonacci Ratios in Harmonic Trading:**
                - 0.382 (38.2% retracement) - shallow pullback level
                - 0.618 (61.8% - The Golden Ratio) - most important retracement
                - 0.786 (78.6% - square root of 0.618) - deep retracement zone
                - 1.27 (127% extension) - moderate price projection
                - 1.618 (161.8% - Golden Ratio extension) - strong extension target
                - 2.24 (224% extension) - extreme extension for Butterfly/Crab
                - 2.618 (261.8% extension) - maximum extension level
                
                **XABCD 5-Point Pattern Structure:**
                All harmonic patterns follow this mandatory 5-point structure:
                - X: Pattern origin point - the starting reference
                - A: First significant directional move from X (impulse leg)
                - B: Retracement of XA leg by specific Fibonacci ratio
                - C: Retracement of AB leg by specific Fibonacci ratio
                - D: Completion point and Potential Reversal Zone (PRZ) - your entry location
                
                Each leg (XA, AB, BC, CD) must meet precise Fibonacci relationships for pattern validity.
                
                **Potential Reversal Zone (PRZ) - The Key Concept:**
                - Point D is where multiple Fibonacci ratio projections converge
                - Convergence creates highest probability reversal location
                - This is your exact entry zone for harmonic pattern trades
                - Must combine PRZ arrival with bullish/bearish candlestick confirmation
                - Stop loss placement: just beyond X point for protection
                - More Fibonacci convergences at D = stronger reversal probability
                
                **Strict Harmonic Pattern Rules:**
                - Patterns must meet exact Fibonacci ratios with maximum ±5% tolerance
                - PRZ must be identified by minimum 3 Fibonacci ratio convergences
                - Volume should ideally decrease as price approaches PRZ
                - Reversal candlestick pattern at Point D confirms entry signal
                - First target: Point C, Second target: Point A
                - Pattern invalid if ratios exceed tolerance - don't force trades
                
                **Why Harmonic Patterns Work So Effectively:**
                - Based on natural geometric proportions found throughout nature and markets
                - Self-fulfilling prophecy - thousands of traders watch same levels
                - Institutional trading algorithms programmed to recognize harmonic patterns
                - Precise entry and exit levels dramatically reduce trading risk
                - High win rate (65-75%) when patterns properly identified and executed
                - Mathematical precision removes emotional decision-making
                
                **Practical Application:**
                Use Fibonacci drawing tools on your trading platform to measure each leg precisely. Patient traders who wait for perfect pattern completion and PRZ confirmation achieve consistently profitable results.
            """.trimIndent(),
            keyPoints = listOf(
                "All harmonics use XABCD 5-point structure",
                "Fibonacci ratios must be precise (±5% tolerance)",
                "Point D is PRZ - optimal entry location",
                "Multiple Fibonacci convergences increase reliability",
                "Combine with candlestick confirmation for best results"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Bullish Harmonic Structure",
                    description = "5-point XABCD formation for reversals",
                    identificationTips = listOf(
                        "X marks pattern beginning",
                        "XA is initial impulse move downward",
                        "B retraces XA by specific ratio",
                        "C retraces AB by specific ratio",
                        "D completes at PRZ with Fib convergence",
                        "Enter long at D with confirmation"
                    )
                ),
                PatternExample(
                    patternName = "Fibonacci Convergence Zone",
                    description = "Multiple ratios confirming reversal",
                    identificationTips = listOf(
                        "1.27 extension of BC",
                        "0.786 retracement of XA",
                        "1.618 extension of AB",
                        "All levels converge at point D",
                        "Creates high-probability PRZ",
                        "Strongest when 3+ levels align"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What does XABCD represent in harmonic patterns?",
                        options = listOf(
                            "Random price points",
                            "5-point geometric structure",
                            "Volume levels",
                            "Time periods"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "XABCD represents the 5-point geometric structure that all harmonic patterns follow."
                    ),
                    Question(
                        question = "Where is the optimal entry point in a harmonic pattern?",
                        options = listOf(
                            "Point X",
                            "Point A",
                            "Point C",
                            "Point D (PRZ)"
                        ),
                        correctAnswerIndex = 3,
                        explanation = "Point D, the Potential Reversal Zone (PRZ), is the optimal entry point where Fibonacci ratios converge."
                    ),
                    Question(
                        question = "What is the Golden Ratio in Fibonacci?",
                        options = listOf(
                            "0.382",
                            "0.500",
                            "0.618",
                            "0.786"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "The Golden Ratio is 0.618 (61.8%), a fundamental ratio found throughout nature and markets."
                    ),
                    Question(
                        question = "What tolerance is acceptable for Fibonacci ratios in harmonics?",
                        options = listOf(
                            "±1%",
                            "±5%",
                            "±10%",
                            "±20%"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Harmonic patterns require precise ratios with maximum ±5% tolerance for pattern validity."
                    )
                )
            )
        )
