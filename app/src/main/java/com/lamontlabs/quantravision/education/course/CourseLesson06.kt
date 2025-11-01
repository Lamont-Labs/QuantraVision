package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.*


val courseLesson06 = Lesson(
            id = 6,
            title = "Harmonic Patterns Fundamentals",
            description = "Introduction to Fibonacci-based harmonic patterns",
            content = """
                Harmonic patterns use precise Fibonacci ratios to identify exact reversal points with mathematical precision. They're among the most accurate trading patterns but require rigorous measurements and patience. When executed correctly, harmonics offer exceptional risk-to-reward ratios.
                
                **What Are Harmonic Patterns?**
                - Based on natural Fibonacci ratios found throughout nature and markets (0.382, 0.618, 1.618, etc.)
                - Geometric price structures that form specific shapes
                - Predict Potential Reversal Zones (PRZ) where price should reverse
                - Require exact ratio relationships - patterns must meet strict criteria
                - Higher accuracy than traditional chart patterns when properly identified
                - All follow 5-point structure labeled X-A-B-C-D
                
                **Critical Fibonacci Ratios in Harmonics:**
                - 0.382 (38.2% retracement) - shallow pullback
                - 0.618 (61.8% retracement) - Golden Ratio, most important
                - 0.786 (78.6% retracement) - deep retracement
                - 1.27 (127% extension) - moderate projection
                - 1.618 (161.8% extension) - Golden Ratio extension
                - 2.24 and 2.618 - extreme extensions for Butterfly/Crab
                
                **The Major Harmonic Patterns:**
                
                **1. Gartley Pattern (Most Common)**
                - The original harmonic pattern from 1935
                - 5-point structure: X-A-B-C-D
                - AB leg = 61.8% retracement of XA
                - BC leg = 38.2% to 88.6% retracement of AB
                - CD leg = 127% to 161.8% extension of BC
                - Point D completes at 78.6% retracement of XA
                - D point is the PRZ (Potential Reversal Zone) - your entry
                
                **2. Bat Pattern**
                - Similar to Gartley but tighter ratios
                - AB = 38.2% to 50% of XA (shallower than Gartley)
                - CD completes at 88.6% of XA (deeper than Gartley)
                - Tighter retracement means closer stop loss
                - Higher risk-reward ratio than Gartley
                
                **3. Butterfly Pattern**
                - Extended harmonic structure
                - AB = 78.6% of XA
                - CD = 127% to 261.8% extension of BC
                - Point D extends BEYOND X point (key difference)
                - Signals extreme price exhaustion
                
                **Trading Harmonic Patterns:**
                - Must wait for complete pattern formation at Point D
                - Enter long/short at Point D PRZ with limit order
                - Place stop loss just beyond X point
                - First target: Point C, Second target: Point A
                - Always combine with candlestick reversal confirmation at D
                - Use Fibonacci drawing tools for precise measurements
                
                **Why Harmonics Work:**
                Mathematical precision creates high-probability setups. When multiple Fibonacci ratios converge at Point D, it creates powerful support/resistance where reversals naturally occur.
            """.trimIndent(),
            keyPoints = listOf(
                "Based on Fibonacci ratios",
                "Require precise measurements",
                "Point D is the trading opportunity",
                "5-point structure (X-A-B-C-D)",
                "Very high accuracy when correct"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Bullish Gartley",
                    description = "Most popular harmonic pattern",
                    identificationTips = listOf(
                        "Starts with sharp move (XA)",
                        "AB retraces 61.8% of XA",
                        "BC is 38.2-88.6% of AB",
                        "CD = 127-161.8% of BC",
                        "D aligns with 78.6% of XA",
                        "Enter at D, target C then A"
                    )
                ),
                PatternExample(
                    patternName = "Bat Pattern",
                    description = "Precise retracement harmonic",
                    identificationTips = listOf(
                        "AB = 38.2-50% of XA",
                        "BC = 38.2-88.6% of AB",
                        "CD = 161.8-261.8% of BC",
                        "D = 88.6% of XA exactly",
                        "Tighter stop loss than Gartley"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What are harmonic patterns based on?",
                        options = listOf(
                            "Random numbers",
                            "Fibonacci ratios",
                            "Trading volume",
                            "Planetary alignments"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Harmonic patterns are based on Fibonacci ratios found throughout nature and markets."
                    ),
                    Question(
                        question = "In a harmonic pattern, what is Point D?",
                        options = listOf(
                            "The starting point",
                            "The midpoint",
                            "The Potential Reversal Zone",
                            "The stop loss point"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Point D is the Potential Reversal Zone (PRZ) where traders enter positions expecting a reversal."
                    ),
                    Question(
                        question = "In a Gartley pattern, AB retraces what percentage of XA?",
                        options = listOf(
                            "38.2%",
                            "50%",
                            "61.8%",
                            "78.6%"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "In a Gartley pattern, the AB leg retraces 61.8% (Golden Ratio) of the XA leg."
                    ),
                    Question(
                        question = "What is the Golden Ratio in Fibonacci analysis?",
                        options = listOf(
                            "0.382 (38.2%)",
                            "0.500 (50%)",
                            "0.618 (61.8%)",
                            "1.000 (100%)"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "The Golden Ratio is 0.618 (61.8%), a key Fibonacci level used extensively in harmonic pattern analysis."
                    )
                )
            )
        )
