package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson22PositionSizing = Lesson(
            id = 22,
            title = "Position Sizing",
            category = "Trading Fundamentals",
            duration = "10 min",
            content = """
                # Position Sizing
                
                ## Overview
                
                Position sizing is determining how many shares, contracts, or lots to trade based on your account size, risk tolerance, and stop loss distance. It's one of the most critical yet overlooked aspects of trading. Proper position sizing ensures consistent risk across all trades and prevents catastrophic losses.
                
                ## Why Position Sizing Matters
                
                ### The Problem:
                Most traders focus on entry and exit, but ignore position size:
                
                **Wrong Approach**:
                ```
                Trade 1: Buy 100 shares at \$50 (risk \$100)
                Trade 2: Buy 200 shares at \$25 (risk \$100)
                Trade 3: Buy 50 shares at \$100 (risk \$250)
                
                ❌ Inconsistent risk per trade!
                ```
                
                **Right Approach**:
                ```
                Trade 1: Calculate size to risk \$100
                Trade 2: Calculate size to risk \$100
                Trade 3: Calculate size to risk \$100
                
                ✅ Consistent risk, better management
                ```
                
                ## Basic Position Sizing Formula
                
                ### The Formula:
                ```
                Position Size = Account Risk ÷ (Entry Price - Stop Loss Price)
                
                Where:
                - Account Risk = Account Size × Risk%
                - Entry Price = Your entry point
                - Stop Loss Price = Your stop loss level
                ```
                
                ### Example:
                ```
                Account Size: \$10,000
                Risk per trade: 1% = \$100
                Entry Price: \$50
                Stop Loss: \$48
                Risk per share: \$50 - \$48 = \$2
                
                Position Size = \$100 ÷ \$2 = 50 shares
                
                Verification:
                Total investment: 50 × \$50 = \$2,500
                Max loss: 50 × \$2 = \$100 ✅
                ```
                
                ## Position Sizing Methods
                
                ### Method 1: Fixed Percentage Risk
                
                **Concept**: Risk same percentage each trade
                
                **Advantages**:
                ✅ Consistent risk exposure
                ✅ Easy to calculate
                ✅ Accounts automatically scale
                ✅ Most common method
                
                **Example**:
                ```
                Account: \$10,000 → Risk 1% = \$100
                Account grows to \$12,000 → Risk 1% = \$120
                Account drops to \$8,000 → Risk 1% = \$80
                
                Position size adjusts automatically!
                ```
                
                ### Method 2: Fixed Dollar Risk
                
                **Concept**: Risk same dollar amount each trade
                
                **Advantages**:
                ✅ Simple to understand
                ✅ Consistent dollar risk
                
                **Disadvantages**:
                ❌ Doesn't scale with account
                ❌ Need to adjust manually
                
                **Example**:
                ```
                Always risk \$100 per trade
                Account size irrelevant
                ```
                
                ### Method 3: Fixed Ratio
                
                **Concept**: Increase position size after profit targets hit
                
                **Example**:
                ```
                Start: 1 contract
                After \$1,000 profit: 2 contracts
                After \$2,000 more profit: 3 contracts
                
                Scales with success
                ```
                
                ### Method 4: Kelly Criterion
                
                **Formula**:
                ```
                Kelly % = (Win Rate × Avg Win - Loss Rate × Avg Loss) / Avg Win
                
                Example:
                Win Rate: 60%
                Avg Win: \$300
                Avg Loss: \$100
                
                Kelly = (0.60 × 300 - 0.40 × 100) / 300
                      = (180 - 40) / 300
                      = 0.467 or 46.7%
                
                ⚠️ TOO AGGRESSIVE!
                Use half-Kelly: 23.3%
                Or quarter-Kelly: 11.7%
                ```
                
                ## Position Sizing for Different Instruments
                
                ### Stocks:
                ```
                Formula: Shares = Account Risk ÷ (Entry - Stop)
                
                Example:
                Account Risk: \$100
                Entry: \$50, Stop: \$47
                Risk/share: \$3
                Shares: \$100 ÷ \$3 = 33 shares
                ```
                
                ### Options:
                ```
                More complex due to leverage
                
                Example:
                Account Risk: \$100
                Option Price: \$2
                Max loss per contract: \$200 (100 shares × \$2)
                
                If risking full premium:
                Contracts = \$100 ÷ \$200 = 0.5
                → Buy 1 contract, but only risk \$100
                ```
                
                ### Forex:
                ```
                Formula: Lots = Account Risk ÷ (Pips at Risk × Pip Value)
                
                Example:
                Account Risk: \$100
                Entry: 1.1000, Stop: 1.0950
                Risk: 50 pips
                Pip Value (standard lot): \$10/pip
                
                Lots = \$100 ÷ (50 × \$10) = 0.2 lots
                ```
                
                ### Futures:
                ```
                Formula: Contracts = Account Risk ÷ (Ticks × Tick Value)
                
                Example:
                Account Risk: \$100
                Entry: 4000, Stop: 3990
                Risk: 10 ticks
                Tick Value: \$12.50
                
                Contracts = \$100 ÷ (10 × \$12.50) = 0.8
                → Trade 1 contract with reduced risk
                ```
                
                ## Advanced Position Sizing
                
                ### Volatility-Adjusted Sizing:
                
                **Concept**: Adjust position size based on volatility
                
                **Using ATR** (Average True Range):
                ```
                High ATR (volatile) = Smaller position
                Low ATR (calm) = Larger position
                
                Example:
                Normal Stop: 2 × ATR
                ATR = \$5 → Stop at \$10
                ATR = \$2 → Stop at \$4
                
                Position size adjusts to maintain consistent risk
                ```
                
                ### Conviction-Based Sizing:
                
                **Concept**: Size based on trade quality
                
                **A-Setup** (High confidence):
                - Risk 2% of account
                - All criteria met
                - Multiple confirmations
                
                **B-Setup** (Medium confidence):
                - Risk 1% of account
                - Most criteria met
                - Some confirmations
                
                **C-Setup** (Low confidence):
                - Risk 0.5% of account
                - Fewer criteria
                - Experimental trades
                
                ### Scaling Positions:
                
                **Scale In** (add to winners):
                ```
                Initial Position: Risk 0.5%
                If profitable: Add 0.5%
                If still profitable: Add 0.5%
                Total: 1.5% risk
                
                Only add when winning!
                Never add to losers!
                ```
                
                **Scale Out** (take profits):
                ```
                Position: 100 shares
                Target 1 (+5%): Sell 33 shares
                Target 2 (+10%): Sell 33 shares
                Target 3 (+15%): Sell 34 shares
                
                Lock in profits incrementally
                ```
                
                ## Risk Management Integration
                
                ### Maximum Position Size:
                
                **Never exceed 20% of account in single trade**:
                ```
                \$10,000 account
                Max position value: \$2,000
                
                Even if calculations suggest bigger position,
                cap at 20% for safety
                ```
                
                ### Maximum Portfolio Risk:
                
                **Total risk across all positions**:
                ```
                5 positions × 1% each = 5% total risk
                
                If one position is 2%:
                Remaining 4 can only be 3% combined
                
                Never exceed 5-10% total portfolio risk
                ```
                
                ## Position Sizing Calculator
                
                ### Step-by-Step:
                
                **Step 1**: Determine account risk
                ```
                Account: \$10,000
                Risk %: 1%
                Account Risk: \$100
                ```
                
                **Step 2**: Identify entry and stop
                ```
                Entry: \$50
                Stop Loss: \$47
                Risk per share: \$3
                ```
                
                **Step 3**: Calculate position size
                ```
                Position Size = \$100 ÷ \$3 = 33.3 shares
                Round to: 33 shares
                ```
                
                **Step 4**: Verify
                ```
                Total investment: 33 × \$50 = \$1,650
                Max loss: 33 × \$3 = \$99 ✅
                Portfolio exposure: \$1,650 ÷ \$10,000 = 16.5% ✅
                ```
                
                ## Common Mistakes
                
                ❌ Using same number of shares for every trade
                ❌ Not adjusting for stop loss distance
                ❌ Risking too much (>2% per trade)
                ❌ Exceeding 20% portfolio per position
                ❌ Not accounting for commission/slippage
                ❌ Adding to losing positions
                ❌ Not scaling position size with account
                ❌ Ignoring volatility differences
                ❌ Calculating incorrectly
                ❌ Not verifying total portfolio risk
                
                ## Pro Tips
                
                ✅ Always calculate position size before entry
                ✅ Risk percentage, not dollar amount (scales)
                ✅ Account for commission in calculations
                ✅ Use position sizing calculator/spreadsheet
                ✅ Never exceed 20% per position
                ✅ Adjust for volatility (ATR)
                ✅ Keep total portfolio risk <10%
                ✅ Round down, never round up
                ✅ Verify calculations before placing order
                ✅ Scale position with conviction
                ✅ Add to winners, not losers
                ✅ Journal position sizes to track
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the formula for calculating position size?",
                        options = listOf("Random guess", "Account Risk ÷ (Entry Price - Stop Loss Price)", "Always 100 shares", "Account Size ÷ 2"),
                        correctAnswer = 1,
                        explanation = "Position Size = Account Risk ÷ (Entry Price - Stop Loss Price). This ensures you risk the same dollar amount regardless of entry price or stop loss distance."
                    ),
                    QuizQuestion(
                        question = "What is the maximum recommended position size as a percentage of account?",
                        options = listOf("50%", "100%", "20%", "5%"),
                        correctAnswer = 2,
                        explanation = "Never exceed 20% of your account value in a single position. This prevents overconcentration and ensures diversification even if the position goes to zero."
                    ),
                    QuizQuestion(
                        question = "Should you add to winning or losing positions?",
                        options = listOf("Always add to losers (average down)", "Add to winners only (pyramid)", "Never add to any position", "Add randomly"),
                        correctAnswer = 1,
                        explanation = "Only add to winning positions (pyramiding). This increases exposure to what's working while maintaining your edge. Never add to losing positions (averaging down)."
                    )
                )
            )
        )
