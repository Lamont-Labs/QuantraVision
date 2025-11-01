package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson21RiskManagement = Lesson(
            id = 21,
            title = "Risk Management",
            category = "Trading Fundamentals",
            duration = "13 min",
            content = """
                # Risk Management
                
                ## Overview
                
                Risk management is the most important aspect of trading. It's not about how much you can make, but how much you can afford to lose. Proper risk management ensures you survive bad periods, protect your capital, and stay in the game long enough to profit from your edge.
                
                ## The Golden Rule
                
                **"Protect your capital at all costs"**
                
                Your trading capital is your business inventory. Without it, you cannot trade. Risk management ensures you never lose so much that you cannot recover.
                
                ## Core Principles
                
                ### Principle 1: Never Risk More Than You Can Afford to Lose
                
                **Account Risk Per Trade**:
                - **Conservative**: 0.5-1% of account per trade
                - **Moderate**: 1-2% of account per trade
                - **Aggressive**: 2-3% of account per trade (not recommended)
                - **Never Exceed**: 5% (this is reckless)
                
                **Example**:
                - Account size: \$10,000
                - Risk per trade: 1% = \$100
                - If stopped out, you lose only \$100
                - Need 100 consecutive losses to lose account (impossible with edge)
                
                ### Principle 2: Use Stop Losses (Always!)
                
                **Stop Loss**: Predetermined price level where you exit to prevent further losses.
                
                **Why Critical**:
                - Protects against catastrophic losses
                - Removes emotion from exit decision
                - Defines your risk before entry
                - Professional traders always use stops
                
                **Types of Stop Losses**:
                
                **1. Fixed Dollar/Percentage Stop**:
                ```
                Entry: \$100
                Stop: \$95 (5% below)
                Risk: \$5 per share
                ```
                
                **2. Technical Stop** (Better):
                ```
                Entry: \$100
                Stop: \$97 (below support)
                Risk: \$3 per share
                Based on chart structure
                ```
                
                **3. Volatility Stop** (Advanced):
                - Uses ATR (Average True Range)
                - Adapts to market conditions
                - Wider stops in volatile markets
                
                **4. Time Stop**:
                - Exit after specific time period
                - If trade not working, get out
                - Prevents capital being tied up
                
                ### Principle 3: Risk/Reward Ratio
                
                **Definition**: Potential profit compared to potential loss.
                
                **Minimum Acceptable**: 2:1
                - Risk \$100 to make \$200
                - Risk 1% to make 2%
                - Asymmetric risk/reward
                
                **Better Ratios**: 3:1 or higher
                - Risk \$100 to make \$300+
                - Even with 50% win rate, you profit
                
                **Example**:
                ```
                Entry: \$100
                Stop Loss: \$95 (risk \$5)
                Target: \$110 (reward \$10)
                Risk/Reward: 1:2 ✅
                
                Win Rate: 40%
                Expectancy: (0.40 × \$10) - (0.60 × \$5)
                          = \$4 - \$3 = +\$1 per trade ✅
                ```
                
                ## Risk Management Strategies
                
                ### Strategy 1: Position Sizing
                
                **Formula**:
                ```
                Position Size = (Account Risk $) / (Entry Price - Stop Price)
                
                Example:
                Account: \$10,000
                Risk per trade: 1% = \$100
                Entry: \$50
                Stop: \$48
                Risk per share: \$2
                
                Position Size = \$100 / \$2 = 50 shares
                
                Total Investment: 50 × \$50 = \$2,500
                Max Loss: 50 × \$2 = \$100 ✅
                ```
                
                ### Strategy 2: Diversification
                
                **Don't Put All Eggs in One Basket**:
                - **Never risk >20% of capital in one trade**
                - **Limit exposure per sector** (e.g., max 30% in tech)
                - **Trade multiple uncorrelated markets**
                - **Different strategies** reduce risk
                
                **Example**:
                - \$10,000 account
                - 5 positions maximum
                - \$2,000 max per position (20%)
                - 1% risk per trade = \$100
                - Total max loss from all positions: \$500 (5%)
                
                ### Strategy 3: Correlation Risk
                
                **Problem**: Multiple positions moving together
                
                **Example of Correlation Risk**:
                ```
                5 Tech Stocks:
                ❌ Apple, Microsoft, Google, Amazon, Meta
                All fall together in tech selloff
                Not truly diversified!
                
                ✅ Better:
                1 Tech (Apple)
                1 Healthcare (J&J)
                1 Energy (XOM)
                1 Financial (JPM)
                1 Consumer (WMT)
                True diversification
                ```
                
                ### Strategy 4: Maximum Daily/Weekly Loss
                
                **Daily Loss Limit**:
                - Stop trading after losing X% in one day
                - Example: Stop after -2% daily loss
                - Prevents revenge trading
                - Protects from emotional decisions
                
                **Weekly Loss Limit**:
                - Stop trading after losing X% in one week
                - Example: Stop after -5% weekly loss
                - Time to reassess strategy
                - Prevents blowing up account
                
                ## Advanced Risk Concepts
                
                ### The 1% Rule
                
                **Never risk more than 1% per trade**:
                
                **Math**:
                - 100 trades
                - Win rate: 50%
                - Risk/Reward: 1:2
                - Risk: 1% per trade
                
                **Results**:
                - 50 losses × 1% = -50%
                - 50 wins × 2% = +100%
                - Net: +50% gain
                
                **Even with 40% win rate**:
                - 60 losses × 1% = -60%
                - 40 wins × 2% = +80%
                - Net: +20% gain
                
                ### Scaling In and Out
                
                **Scaling In** (Add to winners):
                ```
                1st Position: \$100 entry, up to \$105
                Move stop to breakeven
                2nd Position: \$105 entry
                Add to winning trade (pyramid)
                ```
                
                **Scaling Out** (Take profits):
                ```
                Position: 100 shares
                Target 1: Sell 50 shares (+5%)
                Target 2: Sell 25 shares (+10%)
                Target 3: Sell 25 shares (+15%)
                Lock in profits progressively
                ```
                
                ### Trailing Stops
                
                **Definition**: Stop loss that moves with price
                
                **Example**:
                ```
                Entry: \$100
                Initial Stop: \$95
                Price rises to \$110
                Trailing Stop (5%): \$104.50
                Locks in \$4.50 profit
                
                Price continues to \$120
                Trailing Stop: \$114
                Locks in \$14 profit
                ```
                
                ## Risk of Ruin
                
                **Definition**: Probability of losing entire account
                
                **Factors**:
                1. Risk per trade
                2. Win rate
                3. Risk/reward ratio
                
                **Safe Parameters**:
                - 1% risk per trade
                - 50% win rate
                - 2:1 risk/reward
                - Risk of ruin: Near 0%
                
                **Dangerous Parameters**:
                - 10% risk per trade
                - 50% win rate
                - 1:1 risk/reward
                - Risk of ruin: >90% (will blow up!)
                
                ## Psychological Aspects
                
                ### Emotional Control:
                - **Stop losses prevent panic**
                - **Rules prevent impulsive decisions**
                - **Risk limits prevent revenge trading**
                - **Position sizing reduces stress**
                
                ### Discipline:
                - **Always honor stops** (no exceptions!)
                - **Never average down** on losers
                - **Cut losses quickly**
                - **Let winners run**
                
                ## Common Mistakes
                
                ❌ Moving stop losses further away
                ❌ Not using stops at all
                ❌ Risking too much per trade (>2%)
                ❌ Revenge trading after losses
                ❌ Adding to losing positions
                ❌ Ignoring correlation risk
                ❌ No daily/weekly loss limits
                ❌ Poor risk/reward ratios (<1.5:1)
                ❌ Overleveraging account
                ❌ Trading without position size calculation
                
                ## Risk Management Checklist
                
                ### Before Every Trade:
                ✅ Calculate position size based on risk
                ✅ Set stop loss before entry
                ✅ Identify target price
                ✅ Calculate risk/reward ratio (min 2:1)
                ✅ Confirm not exceeding daily/weekly limits
                ✅ Check correlation with other positions
                ✅ Verify account risk is ≤1-2%
                
                ## Pro Tips
                
                ✅ Risk management is more important than entry
                ✅ Survive first, profit second
                ✅ 1% rule keeps you in the game
                ✅ Always use stop losses (non-negotiable)
                ✅ Minimum 2:1 risk/reward ratio
                ✅ Calculate position size before entry
                ✅ Never risk what you can't afford to lose
                ✅ Diversify across different sectors
                ✅ Set daily and weekly loss limits
                ✅ Trailing stops protect profits
                ✅ Scale out to lock in gains
                ✅ Journal all trades to track risk metrics
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the maximum recommended risk per trade?",
                        options = listOf("10%", "5%", "1-2%", "25%"),
                        correctAnswer = 2,
                        explanation = "The maximum recommended risk per trade is 1-2% of your account. This ensures you can withstand losing streaks without blowing up your account and stay in the game long-term."
                    ),
                    QuizQuestion(
                        question = "What is the minimum acceptable risk/reward ratio?",
                        options = listOf("1:1", "2:1", "1:2", "3:3"),
                        correctAnswer = 1,
                        explanation = "The minimum acceptable risk/reward ratio is 2:1, meaning you should aim to make at least \$2 for every \$1 you risk. This ensures profitability even with less than 50% win rate."
                    ),
                    QuizQuestion(
                        question = "Why should you always use stop losses?",
                        options = listOf("They're optional", "To protect against catastrophic losses and remove emotion from exits", "To make more money", "Only for beginners"),
                        correctAnswer = 1,
                        explanation = "Stop losses are critical because they protect against catastrophic losses, remove emotion from exit decisions, and define your risk before entry. Professional traders always use them."
                    )
                )
            )
        )
