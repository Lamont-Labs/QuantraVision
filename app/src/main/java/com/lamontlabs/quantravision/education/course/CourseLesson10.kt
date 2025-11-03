package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.Lesson
import com.lamontlabs.quantravision.education.EducationCourse.PatternExample
import com.lamontlabs.quantravision.education.EducationCourse.Quiz
import com.lamontlabs.quantravision.education.EducationCourse.Question


val courseLesson10 = Lesson(
            id = 10,
            title = "Advanced Pattern Trading Strategies",
            description = "Master professional techniques for consistent profitability",
            content = """
                Transform from pattern spotter to professional trader with advanced strategies and risk management.
                
                **Risk Management Rules:**
                
                **1. The 2% Rule**
                - Never risk more than 2% per trade
                - Account: $10,000 → Max risk: $200
                - Protects capital from blow-up
                - Mandatory for long-term survival
                
                **2. Position Sizing**
                - Risk ÷ Stop Distance = Position Size
                - Example: $200 risk, $2 stop = 100 shares
                - Adjust size based on volatility
                - Smaller size = wider stop (volatile assets)
                
                **3. Risk:Reward Ratio**
                - Minimum 1:2 (risk $100 to make $200)
                - Better: 1:3 or higher
                - Win rate can be lower with good R:R
                - 40% win rate + 1:3 R:R = Profitable
                
                **Advanced Entry Strategies:**
                
                **1. Breakout Entry**
                - Enter immediately on breakout
                - Pros: Catch full move
                - Cons: Higher false breakout risk
                - Best for: Strong momentum patterns
                
                **2. Retest Entry**
                - Wait for price to retest broken level
                - Pros: Better entry, tighter stop
                - Cons: May miss move
                - Best for: Volatile markets
                
                **3. Partial Entry**
                - Enter 50% at breakout
                - Enter 50% on retest
                - Pros: Balanced approach
                - Cons: More complex management
                
                **Exit Strategies:**
                
                **1. Profit Targets**
                - Based on pattern measurement
                - Or prior support/resistance
                - Scale out: 50% at first target, rest at second
                
                **2. Trailing Stop**
                - Move stop to breakeven after 1R profit
                - Trail stop below swing lows (uptrend)
                - Locks in profits while staying in trend
                
                **3. Time Stop**
                - Exit if pattern takes too long
                - Example: Breakout should move within 3 bars
                - Prevents capital sitting in dead trades
                
                **Pattern Combination Strategies:**
                
                **Multiple Pattern Confirmation:**
                - HTF: Bull flag
                - MTF: Ascending triangle
                - LTF: Bullish engulfing
                - Result: High probability setup
                
                **Pattern + Indicator:**
                - Pattern for setup
                - RSI for confirmation
                - Example: Bull flag + RSI oversold = Strong entry
                
                **Professional Trading Plan:**
                
                **Pre-Market:**
                1. Review higher timeframes
                2. Identify key levels
                3. Mark potential patterns
                4. Set alerts
                
                **During Market:**
                1. Wait for setup
                2. Confirm with checklist
                3. Execute with discipline
                4. Manage position
                
                **Post-Market:**
                1. Journal trades
                2. Review performance
                3. Analyze mistakes
                4. Update watchlist
                
                **The Professional Edge:**
                - Patience (wait for best setups)
                - Discipline (follow rules)
                - Consistency (repeat process)
                - Adaptation (market changes)
                - Review (learn from all trades)
            """.trimIndent(),
            keyPoints = listOf(
                "Risk management is priority #1",
                "Never risk more than 2% per trade",
                "Minimum 1:2 risk:reward ratio",
                "Combine multiple confirmations",
                "Professional discipline = Consistent profits"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Triple Confirmation Setup",
                    description = "High probability pattern trade",
                    identificationTips = listOf(
                        "Daily: Bull flag forming",
                        "4H: Ascending triangle breakout",
                        "1H: Bullish engulfing at support",
                        "Volume: Increasing on breakout",
                        "RSI: Above 50 showing strength",
                        "Risk:Reward: 1:3 minimum",
                        "Position size: 2% risk maximum"
                    )
                ),
                PatternExample(
                    patternName = "Professional Trade Management",
                    description = "Complete trade execution example",
                    identificationTips = listOf(
                        "Entry: Triangle breakout with volume",
                        "Stop: Below pattern at -2% account risk",
                        "Target 1: Pattern height at 1:2 R:R",
                        "Target 2: Previous resistance at 1:3 R:R",
                        "Scale: Exit 50% at Target 1",
                        "Trail: Move stop to breakeven",
                        "Exit: Take remaining at Target 2 or trail stop"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What is the maximum risk per trade for proper risk management?",
                        options = listOf(
                            "10% of account",
                            "5% of account",
                            "2% of account",
                            "Whatever feels right"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "The 2% rule is a cornerstone of risk management, protecting your capital from catastrophic losses."
                    ),
                    Question(
                        question = "What is the minimum acceptable risk:reward ratio?",
                        options = listOf(
                            "1:1",
                            "1:2",
                            "1:3",
                            "2:1"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "A minimum 1:2 risk:reward ratio ensures you can be profitable even with a 50% win rate."
                    ),
                    Question(
                        question = "What is the best type of entry for volatile markets?",
                        options = listOf(
                            "Immediate breakout entry",
                            "Retest entry with confirmation",
                            "Random entry timing",
                            "No entry at all"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "In volatile markets, waiting for a retest provides better entry and tighter stop placement."
                    ),
                    Question(
                        question = "What makes a high probability setup?",
                        options = listOf(
                            "Single pattern on one timeframe",
                            "Multiple confirmations across timeframes",
                            "Gut feeling",
                            "News headlines"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Multiple confirmations across timeframes, volume, and indicators create highest probability setups."
                    )
                )
            )
        )
