package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.Lesson
import com.lamontlabs.quantravision.education.EducationCourse.PatternExample
import com.lamontlabs.quantravision.education.EducationCourse.Quiz
import com.lamontlabs.quantravision.education.EducationCourse.Question


val courseLesson24 = Lesson(
            id = 24,
            title = "Pattern Combinations & Playbooks",
            description = "Create systematic trading playbooks by combining multiple patterns for repeatable high-probability setups",
            content = """
                Professional traders don't trade random patterns - they use proven playbooks combining multiple confirmations. Building your playbook creates consistent, repeatable edge.
                
                **Building Trading Playbooks:**
                
                **Playbook Structure:**
                1. Market condition (trend, range, volatile)
                2. Primary pattern requirement
                3. Secondary confirmation pattern
                4. Volume requirement
                5. Indicator confirmation
                6. Entry trigger
                7. Stop placement rule
                8. Target and exit strategy
                
                **Example Playbook: "Trend Continuation Stack"**
                
                **Market Condition:** Strong uptrend (price above 50 & 200 MA)
                
                **Primary Pattern:** Bull flag on daily chart
                - Flagpole minimum 10% gain
                - Flag pullback 38.2-50% max
                - Duration: 1-3 weeks
                
                **Secondary Confirmation:**
                - 4H ascending triangle within flag
                - OR bullish engulfing at flag support
                
                **Volume Requirements:**
                - Decreasing during flag formation
                - 50%+ surge on breakout
                - OBV trending upward
                
                **Indicator Stack:**
                - RSI > 50 and rising
                - MACD positive and above signal
                - 20 MA > 50 MA > 200 MA
                
                **Entry Trigger:**
                - Break above flag resistance
                - 1H candlestick close above flag
                - Confirm with volume spike
                
                **Stop Loss:**
                - Below flag low
                - OR below 20 MA
                - Maximum 2% account risk
                
                **Targets:**
                - Target 1: Flagpole length (50% exit)
                - Target 2: 1.5x flagpole (30% exit)
                - Target 3: Trail remaining (20%)
                
                **Additional Playbooks:**
                
                **"Reversal Confluence" Playbook:**
                - HTF downtrend exhaustion
                - Falling wedge + bullish divergence
                - Inverse H&S on daily
                - Hammer candle at support
                - Volume climax sell-off
                - Entry: Neckline break
                
                **"Range Breakout" Playbook:**
                - Sideways consolidation 4+ weeks
                - Symmetrical triangle forming
                - Volume decreasing into apex
                - Breakout with gap up
                - Entry: Gap confirmation
                
                **Playbook Development Process:**
                1. Identify recurring winning setups
                2. Document exact conditions
                3. Backtest on historical charts
                4. Paper trade to refine
                5. Track statistics and refine
                6. Live trade with small size
                7. Scale up proven playbooks
            """.trimIndent(),
            keyPoints = listOf(
                "Playbooks combine multiple patterns and confirmations",
                "Document exact entry, exit, and risk rules",
                "Backtest playbooks before live trading",
                "Track statistics for each playbook",
                "Refine and improve successful playbooks over time"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Trend Continuation Stack",
                    description = "Multi-pattern confirmation playbook",
                    identificationTips = listOf(
                        "Daily bull flag in strong uptrend",
                        "4H triangle breakout within flag",
                        "1H bullish engulfing at support",
                        "Volume surge on breakout",
                        "RSI > 50, MACD bullish",
                        "Entry: Flag breakout with volume"
                    )
                ),
                PatternExample(
                    patternName = "Bottom Reversal Playbook",
                    description = "Multiple confirmations at major bottom",
                    identificationTips = listOf(
                        "Weekly downtrend exhaustion",
                        "Daily inverse H&S forming",
                        "Bullish RSI divergence",
                        "Volume climax on final low",
                        "Hammer at support level",
                        "Entry: Neckline break with volume"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What is a trading playbook?",
                        options = listOf(
                            "Random pattern collection",
                            "Systematic combination of patterns with exact rules",
                            "Single indicator strategy",
                            "Gut feeling trades"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "A trading playbook is a systematic combination of patterns and confirmations with exact, documented rules."
                    ),
                    Question(
                        question = "Before live trading a new playbook, you should:",
                        options = listOf(
                            "Risk all your capital immediately",
                            "Skip testing and jump in",
                            "Backtest and paper trade first",
                            "Only trade on gut feel"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Always backtest on historical data and paper trade new playbooks before risking real capital."
                    ),
                    Question(
                        question = "In the Trend Continuation playbook, what confirms entry?",
                        options = listOf(
                            "Any price movement",
                            "Flag breakout with 50%+ volume surge",
                            "Random timing",
                            "News headlines"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Entry is confirmed by flag breakout accompanied by 50%+ above average volume surge."
                    ),
                    Question(
                        question = "Why combine multiple patterns in a playbook?",
                        options = listOf(
                            "To make trading complex",
                            "To increase confirmation and probability",
                            "To confuse yourself",
                            "No reason"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Combining multiple patterns increases confirmation and dramatically improves trade probability."
                    )
                )
            )
        )
