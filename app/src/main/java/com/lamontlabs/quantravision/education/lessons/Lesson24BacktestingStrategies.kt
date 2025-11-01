package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson24BacktestingStrategies = Lesson(
            id = 24,
            title = "Backtesting Strategies",
            category = "Trading Advanced",
            duration = "11 min",
            content = """
                # Backtesting Strategies
                
                ## Overview
                
                Backtesting is the process of testing a trading strategy using historical data to determine its viability before risking real money. It's one of the most important steps in developing a profitable trading system. Without backtesting, you're essentially gambling.
                
                ## Why Backtest?
                
                ### Benefits:
                
                ✅ **Validate Strategy**: Prove strategy works
                ✅ **Identify Edge**: Quantify your advantage
                ✅ **Build Confidence**: Trust your system
                ✅ **Optimize Parameters**: Find best settings
                ✅ **Understand Drawdowns**: Know maximum losses
                ✅ **Eliminate Losers**: Filter bad strategies early
                ✅ **Save Money**: Lose in simulation, not reality
                
                ### The Reality:
                ```
                Without Backtesting:
                - Hope strategy works
                - Risk real money immediately
                - Lose confidence after losses
                - Abandon strategy too soon
                - Never know if edge exists
                
                With Backtesting:
                - Know strategy works (or doesn't)
                - Start with confidence
                - Expect normal losses
                - Stick with strategy
                - Proven edge
                ```
                
                ## Types of Backtesting
                
                ### 1. Manual Backtesting
                
                **Method**: Manually go through historical charts
                
                **Process**:
                1. Open historical chart
                2. Cover right side (future data)
                3. Move forward bar-by-bar
                4. Identify setups
                5. Record trades
                6. Calculate results
                
                **Advantages**:
                ✅ Free
                ✅ Understand market dynamics
                ✅ Build pattern recognition
                ✅ No programming needed
                
                **Disadvantages**:
                ❌ Time-consuming
                ❌ Prone to bias
                ❌ Limited sample size
                ❌ Manual errors
                
                ### 2. Automated Backtesting
                
                **Method**: Use software to test strategy
                
                **Popular Tools**:
                - TradingView (Pine Script)
                - MetaTrader (MQL4/MQL5)
                - Python (Backtrader, Zipline)
                - Amibroker
                - NinjaTrader
                
                **Advantages**:
                ✅ Fast (test years in minutes)
                ✅ Accurate
                ✅ Large sample sizes
                ✅ Repeatable
                ✅ Statistical analysis
                
                **Disadvantages**:
                ❌ Requires coding skills
                ❌ May cost money
                ❌ Learning curve
                ❌ Garbage in = garbage out
                
                ## Key Metrics to Track
                
                ### 1. Win Rate
                
                **Formula**: (Winning Trades ÷ Total Trades) × 100%
                
                **Example**:
                ```
                100 trades
                60 winners
                40 losers
                
                Win Rate = (60 ÷ 100) × 100% = 60%
                ```
                
                **Interpretation**:
                - **>60%**: Excellent
                - **50-60%**: Good
                - **40-50%**: Acceptable (with good R:R)
                - **<40%**: Poor (unless exceptional R:R)
                
                ### 2. Average Win/Loss
                
                **Average Win**: Total Profit ÷ Number of Wins
                **Average Loss**: Total Loss ÷ Number of Losses
                
                **Example**:
                ```
                60 wins: Total +\$6,000 → Avg Win = \$100
                40 losses: Total -\$2,000 → Avg Loss = \$50
                
                Win/Loss Ratio = \$100 ÷ \$50 = 2.0
                ```
                
                ### 3. Expectancy
                
                **Formula**:
                ```
                Expectancy = (Win Rate × Avg Win) - (Loss Rate × Avg Loss)
                ```
                
                **Example**:
                ```
                Win Rate: 60% (0.60)
                Avg Win: \$100
                Loss Rate: 40% (0.40)
                Avg Loss: \$50
                
                Expectancy = (0.60 × \$100) - (0.40 × \$50)
                           = \$60 - \$20
                           = \$40 per trade
                ```
                
                **Interpretation**:
                - **Positive**: Strategy has edge
                - **Negative**: Strategy loses money
                - **Higher = Better**: More profit per trade
                
                ### 4. Profit Factor
                
                **Formula**: Gross Profit ÷ Gross Loss
                
                **Example**:
                ```
                Gross Profit: \$6,000
                Gross Loss: \$2,000
                
                Profit Factor = \$6,000 ÷ \$2,000 = 3.0
                ```
                
                **Interpretation**:
                - **>2.0**: Excellent
                - **1.5-2.0**: Good
                - **1.0-1.5**: Acceptable
                - **<1.0**: Losing strategy
                
                ### 5. Maximum Drawdown
                
                **Definition**: Largest peak-to-trough decline
                
                **Example**:
                ```
                Account Peak: \$10,000
                Account Trough: \$7,500
                
                Max Drawdown = \$10,000 - \$7,500 = \$2,500 (25%)
                ```
                
                **Importance**: Shows worst-case scenario
                
                ### 6. Sharpe Ratio
                
                **Formula**: (Return - Risk Free Rate) ÷ Standard Deviation
                
                **Interpretation**:
                - **>3.0**: Excellent
                - **2.0-3.0**: Very good
                - **1.0-2.0**: Good
                - **<1.0**: Poor risk-adjusted returns
                
                ## Backtesting Process
                
                ### Step 1: Define Your Strategy
                
                **Entry Rules** (be specific!):
                ```
                ❌ "Buy when price goes up"
                ✅ "Buy when:
                    - RSI crosses above 30
                    - AND price above 200 SMA
                    - AND bullish engulfing candle
                    - AND volume >1.5x average"
                ```
                
                **Exit Rules**:
                ```
                Stop Loss: 2 × ATR below entry
                Take Profit 1: 2R (sell 50%)
                Take Profit 2: 3R (sell 50%)
                Trailing Stop: 1 × ATR
                ```
                
                ### Step 2: Gather Data
                
                **Requirements**:
                - Historical price data
                - Volume data
                - Indicator data
                - Sufficient time period (2+ years)
                - Multiple market conditions
                
                **Sources**:
                - Yahoo Finance (free)
                - TradingView (free/paid)
                - Quandl (free/paid)
                - Broker platforms
                
                ### Step 3: Test the Strategy
                
                **Manual Method**:
                1. Go to start date
                2. Hide future bars
                3. Move forward bar-by-bar
                4. Identify setups
                5. Record entry/exit/result
                6. Continue through all data
                
                **Automated Method**:
                1. Code strategy rules
                2. Run backtest
                3. Review results
                4. Analyze statistics
                
                ### Step 4: Analyze Results
                
                **Key Questions**:
                - Is expectancy positive?
                - Is win rate acceptable?
                - What's maximum drawdown?
                - How many trades?
                - Does it work in all market conditions?
                - Is profit factor >1.5?
                
                ### Step 5: Optimize (Carefully!)
                
                **Optimization**: Adjusting parameters for better results
                
                **Caution**:
                ```
                ❌ Curve Fitting: Optimizing to perfection
                   - Works on past data only
                   - Fails on future data
                   - Over-optimization
                
                ✅ Reasonable Optimization:
                   - Test range of values
                   - Find robust settings
                   - Not too specific
                ```
                
                **Example**:
                ```
                RSI Period:
                ❌ Test only 14: Too specific
                ❌ Find 14.37 is "perfect": Curve fitting
                ✅ Test 10, 12, 14, 16, 18: Find 12-16 works
                ✅ Use 14 (middle): Robust
                ```
                
                ## Common Pitfalls
                
                ### 1. Look-Ahead Bias
                
                **Problem**: Using future data in past
                
                **Example**:
                ```
                ❌ "Buy if price will be higher in 3 days"
                → You don't know future in real-time!
                
                ✅ "Buy if RSI <30 today"
                → You know this in real-time
                ```
                
                ### 2. Survivorship Bias
                
                **Problem**: Only testing stocks that survived
                
                **Example**:
                ```
                ❌ Test strategy on S&P 500 (2025 list)
                → Missing stocks that failed/delisted
                → Results too optimistic
                
                ✅ Test on historical S&P 500 constituents
                → Include delisted stocks
                → Realistic results
                ```
                
                ### 3. Cherry-Picking
                
                **Problem**: Testing only favorable periods
                
                **Example**:
                ```
                ❌ Test only bull market 2020-2021
                → Strategy looks great!
                → Fails in bear market
                
                ✅ Test 2015-2025 (bull + bear + sideways)
                → Realistic performance
                ```
                
                ### 4. Overfitting/Curve Fitting
                
                **Problem**: Optimizing too much
                
                **Example**:
                ```
                ❌ "RSI 14.73 + MA 23.41 + Volume 1.632x"
                → Perfect on past, fails on future
                
                ✅ "RSI 14 + MA 20 + Volume 1.5x"
                → Round numbers, robust
                ```
                
                ## Sample Size Matters
                
                **Minimum Trades**: 100+
                **Ideal**: 200-300+
                
                **Why**:
                ```
                10 trades: Not statistically significant
                30 trades: Small sample, high variance
                100 trades: Starting to be meaningful
                300+ trades: Statistically robust
                ```
                
                ## Forward Testing
                
                **After Backtesting**:
                1. **Paper Trade**: Test on live data, no real money
                2. **Monitor**: Track real-time performance
                3. **Compare**: Does it match backtest?
                4. **Adjust**: If needed (carefully)
                
                **Duration**: 1-3 months minimum
                
                ## Common Mistakes
                
                ❌ Not backtesting at all
                ❌ Testing on insufficient data (<1 year)
                ❌ Too few trades (<50)
                ❌ Look-ahead bias
                ❌ Survivorship bias
                ❌ Curve fitting/over-optimization
                ❌ Cherry-picking time periods
                ❌ Ignoring transaction costs
                ❌ Not forward testing
                ❌ Trusting single backtest
                
                ## Pro Tips
                
                ✅ Test minimum 2 years of data
                ✅ Include different market conditions
                ✅ Require 100+ trades minimum
                ✅ Account for commissions/slippage
                ✅ Be wary of "too good" results
                ✅ Use out-of-sample testing
                ✅ Forward test before real money
                ✅ Keep strategies simple (more robust)
                ✅ Document everything
                ✅ Retest periodically
                ✅ Positive expectancy is key
                ✅ Maximum drawdown must be tolerable
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is expectancy in backtesting?",
                        options = listOf("Win rate", "Average profit per trade", "Total profit", "Number of trades"),
                        correctAnswer = 1,
                        explanation = "Expectancy is the average profit (or loss) you can expect per trade, calculated as: (Win Rate × Avg Win) - (Loss Rate × Avg Loss). Positive expectancy means the strategy has an edge."
                    ),
                    QuizQuestion(
                        question = "What is curve fitting and why is it dangerous?",
                        options = listOf("Good optimization", "Over-optimizing to past data, causing future failure", "A type of chart", "Normal backtesting"),
                        correctAnswer = 1,
                        explanation = "Curve fitting is over-optimizing parameters to fit historical data perfectly. It creates strategies that work great on past data but fail on future data because they're too specifically tailored to past market conditions."
                    ),
                    QuizQuestion(
                        question = "What is the minimum number of trades for a statistically meaningful backtest?",
                        options = listOf("10 trades", "25 trades", "100+ trades", "5 trades"),
                        correctAnswer = 2,
                        explanation = "A minimum of 100 trades is needed for a backtest to be statistically meaningful. Fewer trades have too much variance and don't provide reliable results. Ideally, 200-300+ trades should be tested."
                    )
                )
            )
        )
