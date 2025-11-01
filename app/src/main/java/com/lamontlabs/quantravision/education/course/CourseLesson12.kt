package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.*


val courseLesson12 = Lesson(
            id = 12,
            title = "Rising & Falling Wedges",
            description = "Master wedge patterns for early reversal detection and trend exhaustion signals",
            content = """
                Wedge patterns are converging price structures that signal trend exhaustion and impending reversals. Unlike triangle patterns where trendlines converge from opposite directions, wedge patterns have both trendlines sloping in the same direction, creating a unique visual signature.
                
                **Rising Wedge (Bearish Reversal):**
                - Both support and resistance trendlines slope upward together
                - Narrowing price range within an upward-sloping channel
                - Typically forms during extended uptrends as buying momentum weakens
                - Each new high requires more effort - buyers becoming exhausted
                - Volume consistently decreases as pattern develops (critical characteristic)
                - Eventually breaks downward through support line (opposite to slope)
                - Target: Measure distance from first touch to last, project downward
                - The steeper the wedge, the more aggressive the eventual breakdown
                - Often forms over several weeks to months
                
                **Falling Wedge (Bullish Reversal):**
                - Both trendlines slope downward in parallel convergence
                - Converging pattern that forms within established downtrends
                - Shows sellers losing control as price volatility contracts
                - Each new low is less convincing - selling pressure fading
                - Diminishing volume during formation signals exhaustion
                - Breaks upward through resistance (opposite to downward slope)
                - Target: Height of pattern at widest point projected upward
                - Common at major market bottoms after capitulation
                
                **Key Distinguishing Characteristics:**
                - Both lines must slope in the SAME direction (unlike triangles)
                - Minimum of 3 touches required on each trendline for validity
                - Wedges are primarily reversal patterns, not continuation patterns
                - Breakout occurs opposite to wedge slope direction
                - Volume contraction throughout formation signals weakening trend momentum
                - Wedges are typically steeper and narrower than triangle patterns
                - Pattern invalidated if breakout occurs in direction of slope
                
                **Professional Trading Strategies:**
                - Entry: Enter on confirmed breakout with strong volume surge
                - Wait for candle close beyond trendline, not just a wick
                - Stop loss: Place just beyond the opposite trendline for protection
                - Target: Pattern height measured at widest point, or previous swing level
                - Confirmation: Look for reversal candlestick pattern at breakout point
                - Best trades occur when wedge forms within context of larger trend change
                - Retest of broken trendline often provides excellent second entry opportunity
                
                **Pattern Psychology:**
                Rising wedges show bulls making progressively weaker highs despite upward movement - unsustainable momentum that leads to reversal. Falling wedges show bears running out of selling pressure as lows become shallower - capitulation nearing completion.
            """.trimIndent(),
            keyPoints = listOf(
                "Rising wedges break downward - bearish reversals",
                "Falling wedges break upward - bullish reversals",
                "Both trendlines slope in same direction",
                "Volume decreases during formation",
                "Breakout confirms trend exhaustion and reversal"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Rising Wedge",
                    description = "Bearish reversal after uptrend exhaustion",
                    identificationTips = listOf(
                        "Both lines slope upward, converging",
                        "Higher highs with decreasing momentum",
                        "Volume steadily declining",
                        "Steeper than typical ascending triangle",
                        "Breakdown through lower trendline confirms"
                    )
                ),
                PatternExample(
                    patternName = "Falling Wedge",
                    description = "Bullish reversal after downtrend exhaustion",
                    identificationTips = listOf(
                        "Both lines slope downward, narrowing",
                        "Lower lows with weakening selling pressure",
                        "Contracting volatility and volume",
                        "Often forms at major bottoms",
                        "Upside breakout signals reversal"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "In which direction do the trendlines slope in a Rising Wedge?",
                        options = listOf(
                            "Both downward",
                            "Both upward",
                            "One up, one down",
                            "Horizontal"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "In a Rising Wedge, both the support and resistance lines slope upward while converging."
                    ),
                    Question(
                        question = "A Rising Wedge typically breaks in which direction?",
                        options = listOf(
                            "Upward continuation",
                            "Downward reversal",
                            "Sideways",
                            "Either direction equally"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Rising Wedges are bearish reversal patterns that typically break downward, signaling trend exhaustion."
                    ),
                    Question(
                        question = "What happens to volume as a wedge pattern forms?",
                        options = listOf(
                            "Steadily increases",
                            "Remains constant",
                            "Decreases and contracts",
                            "Becomes extremely volatile"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Volume typically decreases as a wedge forms, reflecting diminishing conviction before reversal."
                    ),
                    Question(
                        question = "What does a Falling Wedge signal?",
                        options = listOf(
                            "Bearish continuation",
                            "Bullish reversal",
                            "Neutral consolidation",
                            "Market crash"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "A Falling Wedge is a bullish reversal pattern that signals downtrend exhaustion and potential upside."
                    )
                )
            )
        )
