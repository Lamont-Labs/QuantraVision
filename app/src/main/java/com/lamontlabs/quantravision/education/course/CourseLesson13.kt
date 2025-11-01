package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.*


val courseLesson13 = Lesson(
            id = 13,
            title = "Broadening & Megaphone Patterns",
            description = "Identify expanding volatility patterns that signal market tops and increased uncertainty",
            content = """
                Broadening patterns, also called megaphone patterns, are rare but significant formations where price swings progressively expand over time, creating diverging trendlines. These patterns signal increasing volatility, market uncertainty, and emotional trading behavior - the opposite of consolidating triangle patterns.
                
                **Broadening Top (Bearish Reversal):**
                - Expanding price range with diverging trendlines that widen over time
                - Series of higher highs and lower lows - each swing exceeds the previous
                - Typically forms at major market tops after extended bull market rallies
                - Volume increases dramatically with each successive swing
                - Requires minimum three distinct peaks and two troughs to confirm pattern
                - Breakdown below the second trough confirms bearish reversal
                - Often marks euphoric market tops before major corrections
                - Pattern can take weeks or months to fully form
                
                **Broadening Bottom (Bullish Reversal):**
                - Similar expanding structure but forms at market bottoms instead of tops
                - Expanding volatility reflects panic selling and capitulation behavior
                - Significantly less common than broadening tops
                - Shows increasing fear and uncertainty at market lows
                - Breakout above second peak confirms bullish reversal and trend change
                - Often associated with market bottoms after prolonged downtrends
                
                **Megaphone Pattern Characteristics:**
                - Exact opposite of triangle patterns (expanding versus contracting)
                - Reflects highly emotional and unstable market conditions
                - Wild price swings between panic selling and fear-driven rallies
                - Neither bulls nor bears maintain control - constant battle
                - Extremely difficult to trade profitably inside the pattern due to whipsaws
                - Best approached after pattern completion with clear breakout confirmation
                - High false breakout rate makes aggressive trading dangerous
                - Represents loss of market efficiency and rational pricing
                
                **Professional Trading Strategies:**
                - Never trade inside the pattern - volatility will whipsaw your positions
                - Wait patiently for clear, confirmed breakout with strong volume
                - Entry: Only after decisive break of support or resistance with volume
                - Stop loss: Must be conservative and wide - pattern has high failure rate
                - Target: Pattern height at widest point, or previous major support/resistance level
                - Position sizing: Use smaller size due to unreliability and high risk
                - Risk management absolutely critical - expect the unexpected
                
                **Market Psychology and Implications:**
                - Reflects complete loss of control and conviction among market participants
                - Wild swings between fear and greed create emotional roller coaster
                - Often occurs during major market transitions or uncertainty periods
                - Retail traders typically get whipsawed repeatedly trying to catch swings
                - Professional institutional traders step back and wait for pattern resolution
                - Pattern indicates market is searching for fair value but can't find equilibrium
                
                **Common Mistakes:**
                - Trading inside the pattern trying to catch each swing (recipe for losses)
                - Using tight stops (will get stopped out on volatility)
                - Ignoring the high failure rate
                - Over-leveraging positions
            """.trimIndent(),
            keyPoints = listOf(
                "Expanding price range with diverging trendlines",
                "Broadening tops signal major reversals at market highs",
                "Increasing volume and volatility characterize pattern",
                "Difficult to trade - wait for breakout confirmation",
                "Reflects emotional market with no clear control"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Broadening Top",
                    description = "Expanding volatility pattern at market peak",
                    identificationTips = listOf(
                        "Three peaks, each higher than previous",
                        "Two troughs, each lower than previous",
                        "Diverging trendlines expanding outward",
                        "Volume increases with each swing",
                        "Breakdown below second trough confirms",
                        "Commonly marks major market tops"
                    )
                ),
                PatternExample(
                    patternName = "Megaphone Pattern",
                    description = "Expanding range showing market instability",
                    identificationTips = listOf(
                        "Opposite of triangle - getting wider",
                        "Wild swings with no clear winner",
                        "Emotional trading dominates",
                        "Avoid trading until clear breakout",
                        "High false breakout rate",
                        "Conservative position sizing essential"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "How do broadening patterns differ from triangles?",
                        options = listOf(
                            "They contract over time",
                            "They expand with diverging trendlines",
                            "They are horizontal",
                            "They have no trendlines"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Broadening patterns expand over time with diverging trendlines, opposite of triangles which converge."
                    ),
                    Question(
                        question = "Where do broadening tops typically form?",
                        options = listOf(
                            "At market bottoms",
                            "During consolidation",
                            "At major market tops",
                            "In sideways markets"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Broadening tops typically form at major market tops after extended rallies, signaling reversal."
                    ),
                    Question(
                        question = "What is the best trading approach for megaphone patterns?",
                        options = listOf(
                            "Trade aggressively inside the pattern",
                            "Wait for clear breakout confirmation",
                            "Short the highs and buy the lows",
                            "Ignore the pattern completely"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Megaphone patterns are too volatile to trade inside - wait for clear breakout confirmation."
                    ),
                    Question(
                        question = "What does a broadening pattern indicate about market psychology?",
                        options = listOf(
                            "Strong conviction and control",
                            "Calm and stable conditions",
                            "Increasing uncertainty and emotion",
                            "Professional dominance"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Broadening patterns reflect increasing uncertainty, emotion, and loss of control in the market."
                    )
                )
            )
        )
