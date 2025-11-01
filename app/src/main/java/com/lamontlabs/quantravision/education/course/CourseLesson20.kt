package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.*


val courseLesson20 = Lesson(
            id = 20,
            title = "Butterfly & Crab Patterns",
            description = "Master advanced harmonic patterns with extended price projections for extreme reversal opportunities",
            content = """
                Butterfly and Crab patterns are advanced harmonics featuring extended D points that project beyond point X, offering aggressive reversal trades with exceptional risk/reward ratios.
                
                **Butterfly Pattern:**
                Extended harmonic with D point beyond X:
                - AB retracement: 78.6% of XA
                - BC retracement: 38.2%-88.6% of AB
                - CD extension: 161.8%-224% of BC
                - Point D: 127%-161.8% extension of XA
                - D extends beyond X point (key characteristic)
                - Named for symmetrical wing appearance
                - Very reliable at trend extremes
                
                **Bullish Butterfly:**
                - Forms after extended downtrend
                - X at high, A at low point
                - D point drops below X (extreme oversold)
                - Enter long at D with reversal confirmation
                - Stop: 2-3% beyond D point
                - Target: Points C, A, then X
                
                **Bearish Butterfly:**
                - Forms after extended uptrend
                - X at low, A at high
                - D extends above X (extreme overbought)
                - Enter short at D PRZ
                - Targets back to C, A, X levels
                
                **Crab Pattern:**
                Most extreme harmonic pattern:
                - AB retracement: 38.2%-61.8% of XA
                - BC retracement: 38.2%-88.6% of AB
                - CD extension: 224%-361.8% of BC
                - Point D: 161.8% extension of XA (critical)
                - D significantly beyond X
                - Highest risk/reward of all harmonics
                - Named for crab claw appearance
                
                **Trading Crab Patterns:**
                - Most aggressive harmonic setup
                - Enter at exact 161.8% XA extension
                - Tight stop beyond D point
                - First target at 38.2% retracement
                - Can achieve 3:1+ risk/reward
                - Requires patience - forms rarely
            """.trimIndent(),
            keyPoints = listOf(
                "Butterfly: D at 127-161.8% extension of XA",
                "Crab: D at 161.8% extension of XA exactly",
                "Both patterns extend D beyond X point",
                "Offer exceptional risk/reward ratios",
                "Require precise Fibonacci measurements for validity"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Bullish Butterfly",
                    description = "Extended harmonic for extreme reversals",
                    identificationTips = listOf(
                        "AB = 78.6% retracement of XA",
                        "BC = 38.2-88.6% of AB",
                        "CD = 161.8-224% extension of BC",
                        "D completes at 127-161.8% of XA",
                        "D point extends below X",
                        "Enter with bullish reversal at D"
                    )
                ),
                PatternExample(
                    patternName = "Bearish Crab",
                    description = "Most aggressive harmonic pattern",
                    identificationTips = listOf(
                        "AB = 38.2-61.8% of XA",
                        "BC = 38.2-88.6% of AB",
                        "CD = 224-361.8% of BC",
                        "D must complete at 161.8% of XA",
                        "Extremely extended beyond X",
                        "Best risk/reward when perfect"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What distinguishes Butterfly and Crab from other harmonics?",
                        options = listOf(
                            "They have no point D",
                            "Point D extends beyond X",
                            "They only work on daily charts",
                            "They have no Fibonacci ratios"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Butterfly and Crab patterns are unique because point D extends beyond the X point, creating extreme setups."
                    ),
                    Question(
                        question = "What is the critical D point for a Crab pattern?",
                        options = listOf(
                            "78.6% of XA",
                            "127% of XA",
                            "161.8% of XA",
                            "224% of XA"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "The Crab pattern requires D to complete at exactly 161.8% extension of XA - this is the defining ratio."
                    ),
                    Question(
                        question = "Which pattern offers the best risk/reward ratio?",
                        options = listOf(
                            "Gartley",
                            "Bat",
                            "Butterfly",
                            "Crab"
                        ),
                        correctAnswerIndex = 3,
                        explanation = "The Crab pattern offers the best risk/reward ratio when properly formed, often 3:1 or better."
                    ),
                    Question(
                        question = "In a Butterfly pattern, AB retraces what % of XA?",
                        options = listOf(
                            "38.2%",
                            "61.8%",
                            "78.6%",
                            "88.6%"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "In a Butterfly pattern, the AB leg retraces 78.6% of the XA leg."
                    )
                )
            )
        )
