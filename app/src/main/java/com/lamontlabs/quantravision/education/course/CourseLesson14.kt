package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.Lesson
import com.lamontlabs.quantravision.education.EducationCourse.PatternExample
import com.lamontlabs.quantravision.education.EducationCourse.Quiz
import com.lamontlabs.quantravision.education.EducationCourse.Question


val courseLesson14 = Lesson(
            id = 14,
            title = "Head & Shoulders Variations",
            description = "Master classic and inverse H&S patterns plus complex variations for reliable reversal trading",
            content = """
                Head and Shoulders patterns are among the most reliable and widely recognized reversal formations in all of technical analysis. When properly identified and traded, they offer excellent risk-to-reward ratios. Understanding the various pattern configurations increases your pattern recognition skills and creates more trading opportunities.
                
                **Classic Head & Shoulders (Bearish Reversal):**
                - Three sequential peaks: left shoulder, head (highest peak), right shoulder (similar height to left)
                - Neckline drawn connecting the two troughs between the three peaks
                - Volume pattern crucial: typically decreases from left shoulder through head formation
                - Breakdown decisively below neckline with increased volume confirms the reversal
                - Price target: Measure vertical distance from head peak to neckline, project that distance downward from breakdown point
                - Often marks major market tops after extended uptrends
                - Pattern can take several weeks to months to fully develop
                - The more symmetrical the shoulders, the more reliable the pattern
                
                **Inverse Head & Shoulders (Bullish Reversal):**
                - Perfect mirror image forming at market bottoms instead of tops
                - Three sequential troughs: left shoulder, head (lowest/deepest trough), right shoulder
                - Center trough (head) must be distinctly lower than both shoulders
                - Volume typically increases as pattern develops (opposite of bearish H&S)
                - Breakout above neckline with strong volume confirms bullish reversal
                - Target: Measure head-to-neckline distance, project upward from breakout
                - Marks significant market bottoms after prolonged downtrends
                - One of the most reliable bottom formations in technical analysis
                
                **Complex Head & Shoulders Variations:**
                
                **Multiple Shoulders Configuration:**
                - Two or more shoulders on left side, right side, or both
                - Pattern remains valid if structure maintains overall symmetry
                - Neckline must connect all troughs between shoulders and head
                - Confirms exactly same way as classic three-peak pattern
                - More complex patterns can be more reliable when properly formed
                
                **Sloping Neckline Variation:**
                - Neckline angles upward or downward instead of horizontal
                - Upward-sloping neckline = slightly less bearish (some buying strength remains)
                - Downward-sloping neckline = more aggressively bearish signal
                - Still functions as valid reversal pattern regardless of slope
                - Target measured perpendicular from neckline
                
                **Failed Head & Shoulders (Trap Pattern):**
                - Price breaks through neckline but quickly reverses back inside pattern
                - Strong invalidation and bullish signal when bearish H&S fails
                - Often leads to powerful explosive move in opposite direction
                - Classic trap for breakout traders who entered without confirmation
                - Use failed patterns as contrarian signals
                
                **Professional Trading Guidelines:**
                - Entry: Enter on confirmed neckline break with volume surge
                - Stop loss: Place just above right shoulder (bearish H&S) or below right shoulder (bullish)
                - Take partial profits at measured target, let remainder run
                - Watch for neckline retest - provides excellent second entry opportunity
                - Combine with candlestick confirmation for highest probability
            """.trimIndent(),
            keyPoints = listOf(
                "Most reliable reversal pattern in technical analysis",
                "Volume pattern crucial - decreases to head, increases on break",
                "Neckline break confirms pattern completion",
                "Measured target from head to neckline distance",
                "Variations valid if maintain essential structure"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Classic Head & Shoulders",
                    description = "Bearish reversal marking market top",
                    identificationTips = listOf(
                        "Left shoulder forms after uptrend",
                        "Head exceeds left shoulder high",
                        "Right shoulder lower than head",
                        "Volume lighter on right shoulder",
                        "Neckline break on increased volume",
                        "Target = head-to-neckline distance"
                    )
                ),
                PatternExample(
                    patternName = "Inverse Head & Shoulders",
                    description = "Bullish reversal marking market bottom",
                    identificationTips = listOf(
                        "Forms after downtrend",
                        "Head is lowest point (deepest trough)",
                        "Right shoulder higher than head",
                        "Volume increases through formation",
                        "Breakout above neckline confirms",
                        "Highly reliable bottom signal"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "In a classic H&S pattern, which peak is the highest?",
                        options = listOf(
                            "Left shoulder",
                            "The head",
                            "Right shoulder",
                            "All equal height"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "The head is always the highest peak in a Head & Shoulders pattern, with shoulders lower on both sides."
                    ),
                    Question(
                        question = "How do you calculate the price target for H&S?",
                        options = listOf(
                            "Measure neckline to head, project from breakout",
                            "Double the pattern width",
                            "Guess based on previous support",
                            "There is no specific target"
                        ),
                        correctAnswerIndex = 0,
                        explanation = "Measure the distance from neckline to head peak, then project that distance from the breakout point."
                    ),
                    Question(
                        question = "What confirms a Head & Shoulders pattern?",
                        options = listOf(
                            "Formation of the head",
                            "Completion of right shoulder",
                            "Break below neckline with volume",
                            "Just the shoulder formation"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Pattern is only confirmed when price breaks below the neckline with increased volume."
                    ),
                    Question(
                        question = "An Inverse Head & Shoulders forms at:",
                        options = listOf(
                            "Market tops",
                            "Market bottoms",
                            "Consolidation zones",
                            "Random locations"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Inverse H&S is a bullish reversal pattern that forms at market bottoms after downtrends."
                    )
                )
            )
        )
