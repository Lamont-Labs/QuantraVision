package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.*


val courseLesson23 = Lesson(
            id = 23,
            title = "Multi-Timeframe Alignment",
            description = "Analyze patterns across multiple timeframes to find highest-probability trades with optimal timing",
            content = """
                Multi-timeframe analysis is the difference between amateur and professional pattern trading. The best trades occur when patterns align across multiple timeframes.
                
                **Timeframe Hierarchy:**
                
                **Higher Timeframe (HTF) - Weekly/Daily:**
                - Determines overall trend direction
                - Identifies major support/resistance
                - Provides context for all trades
                - Trade WITH the HTF trend only
                - HTF patterns = major moves
                
                **Medium Timeframe (MTF) - 4H/1H:**
                - Entry pattern timeframe
                - Confirms HTF direction
                - Identifies setup and structure
                - Best risk/reward on this timeframe
                - Patterns here = swing trades
                
                **Lower Timeframe (LTF) - 15m/5m:**
                - Precise entry timing
                - Tight stop placement
                - Entry confirmation signals
                - Exit management
                - Patterns here = intraday scalps
                
                **Top-Down Analysis Process:**
                
                **Step 1: Weekly Chart**
                - Identify overall trend (up/down/sideways)
                - Mark major support/resistance zones
                - Note any HTF patterns forming
                - Determine bias: bullish, bearish, or neutral
                
                **Step 2: Daily Chart**
                - Confirm weekly trend direction
                - Identify MTF pattern setups
                - Mark intermediate levels
                - Look for pattern formation
                
                **Step 3: 4-Hour Chart**
                - Find specific entry pattern
                - Confirm alignment with higher TFs
                - Identify exact entry zone
                - Set profit targets
                
                **Step 4: 1-Hour Chart**
                - Time precise entry
                - Place tight stop loss
                - Confirm with candlestick pattern
                - Execute trade
                
                **Alignment Examples:**
                
                **Perfect Bullish Alignment:**
                - Weekly: Uptrend, inverse H&S forming
                - Daily: Ascending triangle breakout
                - 4H: Bull flag consolidation
                - 1H: Bullish engulfing at flag support
                - Result: Very high probability long trade
                
                **Conflicting Signals (Avoid):**
                - Weekly: Downtrend
                - Daily: Bullish pattern
                - Result: Counter-trend trade = high risk
                - Action: Wait for alignment or skip
            """.trimIndent(),
            keyPoints = listOf(
                "Always analyze higher timeframes first for context",
                "Best trades have pattern alignment across all timeframes",
                "HTF determines trend, LTF provides entry timing",
                "Never trade against higher timeframe trend",
                "Use LTF for precise entries, HTF for targets"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Perfect Multi-TF Alignment",
                    description = "All timeframes confirm bullish direction",
                    identificationTips = listOf(
                        "Weekly: Strong uptrend, no resistance above",
                        "Daily: Cup & Handle pattern forming",
                        "4H: Ascending triangle breakout",
                        "1H: Bullish flag at support",
                        "Entry: 1H flag breakout",
                        "Targets: Daily cup depth = major profit"
                    )
                ),
                PatternExample(
                    patternName = "Top-Down Trade Setup",
                    description = "Professional analysis workflow",
                    identificationTips = listOf(
                        "Start weekly: Identify major trend",
                        "Daily: Find pattern in trend direction",
                        "4H: Wait for entry pattern formation",
                        "1H: Execute with confirmation",
                        "Stop: Below 1H pattern low",
                        "Target: Daily pattern objective"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "Which timeframe should you analyze first?",
                        options = listOf(
                            "1-hour chart",
                            "15-minute chart",
                            "Weekly/Daily (highest)",
                            "Any order is fine"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Always start with the highest timeframe (weekly/daily) to determine overall trend and context."
                    ),
                    Question(
                        question = "What happens when daily shows bullish but weekly shows bearish?",
                        options = listOf(
                            "Take the daily bullish trade",
                            "Avoid the trade - conflicting signals",
                            "Short the market",
                            "Trade both directions"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "When timeframes conflict, avoid the trade. Best trades have all timeframes aligned in same direction."
                    ),
                    Question(
                        question = "What is the lower timeframe (1H, 15m) best used for?",
                        options = listOf(
                            "Determining overall trend",
                            "Precise entry timing and stops",
                            "Long-term targets",
                            "Ignoring higher timeframes"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Lower timeframes are used for precise entry timing, tight stop placement, and execution."
                    ),
                    Question(
                        question = "The highest probability trades occur when:",
                        options = listOf(
                            "Only one timeframe shows a pattern",
                            "Timeframes conflict with each other",
                            "Multiple timeframes align in same direction",
                            "You ignore higher timeframes"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Highest probability trades occur when multiple timeframes all align and confirm the same direction."
                    )
                )
            )
        )
