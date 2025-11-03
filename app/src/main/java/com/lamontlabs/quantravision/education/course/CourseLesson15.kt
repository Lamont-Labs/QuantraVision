package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.Lesson
import com.lamontlabs.quantravision.education.EducationCourse.PatternExample
import com.lamontlabs.quantravision.education.EducationCourse.Quiz
import com.lamontlabs.quantravision.education.EducationCourse.Question


val courseLesson15 = Lesson(
            id = 15,
            title = "Double & Triple Tops/Bottoms",
            description = "Identify and trade powerful reversal patterns formed by multiple tests of support and resistance",
            content = """
                Double and triple tops/bottoms are classic reversal patterns that occur when price tests a key level multiple times and fails to break through, signaling trend exhaustion.
                
                **Double Top (Bearish Reversal):**
                - Two peaks at approximately same price level
                - Forms "M" shape on chart
                - Second peak often slightly lower than first
                - Neckline connects the trough between peaks
                - Breakdown below neckline confirms reversal
                - Target: Distance from neckline to peaks, projected down
                - Volume typically lower on second peak
                
                **Double Bottom (Bullish Reversal):**
                - Two troughs at similar price level
                - Forms "W" shape on chart
                - Second bottom may be slightly higher
                - Neckline connects peak between troughs
                - Breakout above neckline confirms reversal
                - Target: Distance from neckline to troughs, projected up
                - Volume increases on second bottom and breakout
                
                **Triple Top (Bearish Reversal):**
                - Three peaks at approximately same resistance
                - More reliable than double top
                - Shows strong resistance level
                - Volume decreases with each peak
                - Breakdown confirms after third rejection
                
                **Triple Bottom (Bullish Reversal):**
                - Three troughs at similar support level
                - Highly reliable reversal pattern
                - Multiple tests prove support strength
                - Volume typically increases on breakout
                - Stronger signal than double bottom
                
                **Trading Rules:**
                - Wait for neckline break confirmation
                - Entry: On breakout with volume increase
                - Stop: Beyond the pattern (above tops or below bottoms)
                - Target: Measured move from pattern height
                - False breakouts common - wait for close beyond neckline
            """.trimIndent(),
            keyPoints = listOf(
                "Double/triple tops form M/triple-peak, bottoms form W/triple-trough",
                "Multiple tests show strong support/resistance levels",
                "Neckline break confirms pattern completion",
                "Triple patterns more reliable than double patterns",
                "Volume confirmation essential for validity"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Double Top",
                    description = "Two failed attempts to break resistance",
                    identificationTips = listOf(
                        "Two peaks at similar price level",
                        "Forms after uptrend",
                        "Second peak volume lighter than first",
                        "Trough between peaks forms neckline",
                        "Break below neckline on volume confirms",
                        "Measure pattern height for target"
                    )
                ),
                PatternExample(
                    patternName = "Triple Bottom",
                    description = "Three successful tests of support level",
                    identificationTips = listOf(
                        "Three troughs at approximately same price",
                        "Forms after downtrend",
                        "Each test shows buyers defending level",
                        "Volume increases on third bottom",
                        "Breakout above neckline confirms reversal",
                        "Very reliable bullish reversal signal"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What shape does a Double Top create on a chart?",
                        options = listOf(
                            "W shape",
                            "M shape",
                            "V shape",
                            "Triangle shape"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "A Double Top creates an 'M' shape with two peaks and a trough in the middle."
                    ),
                    Question(
                        question = "Which pattern is generally more reliable?",
                        options = listOf(
                            "Double Top",
                            "Triple Top",
                            "Both equally reliable",
                            "Neither is reliable"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Triple patterns are generally more reliable as they show multiple confirmations of the support/resistance level."
                    ),
                    Question(
                        question = "How is the target calculated for a Double Bottom?",
                        options = listOf(
                            "Distance from neckline to bottoms, projected upward",
                            "Arbitrary guess",
                            "Previous high",
                            "No specific target"
                        ),
                        correctAnswerIndex = 0,
                        explanation = "Measure the distance from neckline to the bottoms, then project that distance upward from the breakout point."
                    ),
                    Question(
                        question = "What confirms a Double Bottom pattern?",
                        options = listOf(
                            "Formation of first bottom",
                            "Formation of second bottom",
                            "Breakout above neckline with volume",
                            "Any price movement"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "The pattern is only confirmed when price breaks above the neckline with increased volume."
                    )
                )
            )
        )
