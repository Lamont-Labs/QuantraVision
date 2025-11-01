package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.*


val courseLesson16 = Lesson(
            id = 16,
            title = "Rounding, Cup & Handle Structures",
            description = "Master gradual accumulation patterns that signal major bullish breakouts and trend continuations",
            content = """
                Rounding and cup patterns represent gradual accumulation periods where smart money quietly builds positions before major breakouts. These patterns show measured, controlled buying.
                
                **Rounding Bottom (Bullish Reversal):**
                - Gradual U-shaped curve at market bottom
                - Long formation period (months to years)
                - Represents slow accumulation phase
                - Volume decreases into the bottom, increases on right side
                - No sharp V-bottom - gradual transition
                - Breakout above resistance with volume confirms
                - Very reliable long-term reversal pattern
                
                **Cup & Handle (Bullish Continuation):**
                - Cup: U-shaped pattern after uptrend
                - Handle: Short consolidation/pullback on right side
                - Handle depth: 1/3 of cup depth maximum
                - Volume: Decreases during cup and handle formation
                - Breakout: Above handle resistance with volume surge
                - Target: Cup depth added to breakout point
                - William O'Neil's favorite pattern
                
                **Inverted Cup & Handle (Bearish):**
                - Upside-down cup formation
                - Less common than bullish version
                - Forms at market tops
                - Handle points upward before breakdown
                - Breakdown confirms reversal
                
                **Rounding Top (Bearish Reversal):**
                - Inverted U-shape at market tops
                - Gradual distribution by institutions
                - Volume pattern mirrors rounding bottom
                - Breakdown signals major reversal
                - Long-term bearish implication
                
                **Trading Guidelines:**
                - Entry: Breakout from handle or rounding pattern
                - Stop: Below handle low or pattern support
                - Target: Measured move from pattern depth
                - These are swing/position trade patterns
                - Patience required - patterns take time to form
            """.trimIndent(),
            keyPoints = listOf(
                "Rounding patterns show gradual accumulation or distribution",
                "Cup & Handle highly reliable continuation pattern",
                "Volume decreases during formation, surges on breakout",
                "Handle should be shallow - max 1/3 cup depth",
                "Long-term patterns requiring patience and discipline"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Cup & Handle",
                    description = "William O'Neil's signature bullish pattern",
                    identificationTips = listOf(
                        "U-shaped cup forms after uptrend",
                        "Cup depth typically 12-30%",
                        "Handle forms on right side of cup",
                        "Handle consolidation smaller than cup",
                        "Volume light during handle formation",
                        "Breakout above handle resistance confirms",
                        "Target = cup depth projected from breakout"
                    )
                ),
                PatternExample(
                    patternName = "Rounding Bottom",
                    description = "Long-term accumulation pattern",
                    identificationTips = listOf(
                        "Gradual U-shaped bottom formation",
                        "Takes months to develop fully",
                        "Volume lightest at bottom of U",
                        "Right side shows increasing volume",
                        "No sharp reversal - smooth transition",
                        "Very reliable when properly formed"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What is the maximum depth of a handle relative to the cup?",
                        options = listOf(
                            "Equal depth",
                            "1/2 the cup depth",
                            "1/3 the cup depth",
                            "Any depth"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "A proper handle should be no deeper than 1/3 of the cup depth to maintain pattern validity."
                    ),
                    Question(
                        question = "Cup & Handle is primarily a:",
                        options = listOf(
                            "Reversal pattern",
                            "Continuation pattern",
                            "Bilateral pattern",
                            "Topping pattern"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Cup & Handle is a bullish continuation pattern that forms during an uptrend pause."
                    ),
                    Question(
                        question = "What does a Rounding Bottom indicate?",
                        options = listOf(
                            "Quick reversal",
                            "Gradual accumulation by smart money",
                            "Market crash",
                            "Random price movement"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Rounding Bottom shows gradual accumulation by institutions over an extended period."
                    ),
                    Question(
                        question = "How is the Cup & Handle target calculated?",
                        options = listOf(
                            "Double the handle height",
                            "Cup depth added to breakout point",
                            "Random target selection",
                            "Previous resistance level"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Target is calculated by measuring cup depth and adding it to the breakout point."
                    )
                )
            )
        )
