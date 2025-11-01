package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.*


val courseLesson08 = Lesson(
            id = 8,
            title = "Multiple Timeframe Analysis",
            description = "Align patterns across different timeframes for higher probability",
            content = """
                The same asset tells different stories on different timeframes. Master multiple timeframe analysis to dramatically improve your win rate.
                
                **The Timeframe Hierarchy:**
                
                **1. Higher Timeframe (HTF)** - Overall trend
                - Daily, Weekly, Monthly
                - Determines market bias
                - Major support/resistance levels
                - "Big picture" trend direction
                
                **2. Trading Timeframe (TF)** - Your main chart
                - 1H, 4H, Daily
                - Where you identify patterns
                - Entry/exit signals
                - Main analysis timeframe
                
                **3. Lower Timeframe (LTF)** - Entry timing
                - 5m, 15m, 1H
                - Precise entry points
                - Stop loss placement
                - Exit refinement
                
                **The Golden Rule:**
                "Trade in direction of higher timeframe trend"
                
                **Multi-Timeframe Strategy:**
                
                **Step 1: Start with HTF**
                - Identify overall trend (up/down/sideways)
                - Mark major support/resistance
                - Note key patterns forming
                
                **Step 2: Move to Trading TF**
                - Look for patterns aligned with HTF trend
                - Identify entry opportunities
                - Set targets based on HTF levels
                
                **Step 3: Drop to LTF**
                - Fine-tune entry point
                - Set precise stop loss
                - Monitor trade execution
                
                **Example Combinations:**
                
                **Scalping:** 1H → 15m → 5m
                **Day Trading:** Daily → 1H → 15m
                **Swing Trading:** Weekly → Daily → 4H
                **Position Trading:** Monthly → Weekly → Daily
                
                **Pattern Alignment:**
                - HTF Bull Flag + TF Ascending Triangle = High probability
                - HTF Downtrend + TF Bull Pattern = Low probability (counter-trend)
                - Multiple timeframes confirming = Highest confidence
            """.trimIndent(),
            keyPoints = listOf(
                "Always start with higher timeframe",
                "Trade in direction of HTF trend",
                "Use LTF for precise entries",
                "Pattern alignment increases probability",
                "HTF levels are most significant"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Timeframe Alignment",
                    description = "All timeframes confirm same direction",
                    identificationTips = listOf(
                        "Weekly: Uptrend with bull flag forming",
                        "Daily: Breakout above resistance",
                        "4H: Ascending triangle breakout",
                        "1H: Pullback to support for entry",
                        "All patterns confirm bullish bias"
                    )
                ),
                PatternExample(
                    patternName = "Top-Down Analysis",
                    description = "Systematic timeframe evaluation",
                    identificationTips = listOf(
                        "Monthly: Identify long-term trend",
                        "Weekly: Find intermediate patterns",
                        "Daily: Spot entry patterns",
                        "4H: Refine entry timing",
                        "1H: Execute with precision"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "Which timeframe should you analyze first?",
                        options = listOf(
                            "The lowest timeframe",
                            "The highest timeframe",
                            "Your trading timeframe",
                            "It doesn't matter"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Always start with the highest timeframe to identify the overall trend and major levels."
                    ),
                    Question(
                        question = "What does the lower timeframe (LTF) help with?",
                        options = listOf(
                            "Determining overall trend",
                            "Precise entry and stop placement",
                            "Long-term targets",
                            "Pattern identification only"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Lower timeframes are used for precise entry timing and tight stop loss placement."
                    ),
                    Question(
                        question = "The highest probability trades occur when:",
                        options = listOf(
                            "Only one timeframe shows a pattern",
                            "Multiple timeframes align in the same direction",
                            "Timeframes contradict each other",
                            "You ignore higher timeframes"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "When multiple timeframes confirm the same direction, it dramatically increases trade probability."
                    ),
                    Question(
                        question = "What is the Golden Rule of multiple timeframe analysis?",
                        options = listOf(
                            "Always trade the lowest timeframe",
                            "Trade in direction of higher timeframe trend",
                            "Ignore higher timeframes completely",
                            "Only use one timeframe at a time"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "The Golden Rule is to trade in the direction of the higher timeframe trend, which provides the overall market bias and increases success probability."
                    )
                )
            )
        )
