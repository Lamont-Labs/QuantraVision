package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson25TradingPlan = Lesson(
            id = 25,
            title = "Building Your Trading Plan",
            category = "Trading Fundamentals",
            duration = "14 min",
            content = """
                # Building Your Trading Plan
                
                ## Overview
                
                A trading plan is your complete blueprint for trading success. It's a written document that defines your approach, rules, and processes. Professional traders all have detailed trading plans. Without one, you're not trading—you're gambling.
                
                ## Why You Need a Trading Plan
                
                ### The Reality:
                ```
                Without a Plan:
                - Random entries and exits
                - Emotional decision-making
                - Inconsistent results
                - No way to improve
                - Trading based on "feelings"
                
                With a Plan:
                - Clear rules and criteria
                - Objective decisions
                - Consistent approach
                - Track and improve
                - Professional trading
                ```
                
                ### Benefits:
                ✅ Removes emotion from trading
                ✅ Provides clear decision framework
                ✅ Enables performance tracking
                ✅ Builds discipline and confidence
                ✅ Allows systematic improvement
                ✅ Prevents impulsive trades
                ✅ Defines your edge clearly
                
                ## Components of a Trading Plan
                
                ### 1. Trading Goals
                
                **Define Clear Goals**:
                
                **Financial Goals**:
                ```
                ❌ "Make a lot of money"
                ✅ "Grow account 15% this year"
                ✅ "Average 3% monthly return"
                ✅ "Generate \$2,000/month income"
                ```
                
                **Performance Goals**:
                ```
                ✅ "Follow trading plan 100%"
                ✅ "Journal every trade"
                ✅ "Maximum 3% drawdown per month"
                ✅ "Complete 100 backtested trades"
                ```
                
                **SMART Goals**:
                - **S**pecific: Clear and detailed
                - **M**easurable: Track progress
                - **A**chievable: Realistic
                - **R**elevant: Aligned with objectives
                - **T**ime-bound: Deadline defined
                
                ### 2. Market Selection
                
                **What Will You Trade?**
                
                **Asset Classes**:
                ```
                Options:
                ☐ Stocks (US, International)
                ☐ Forex (Currency Pairs)
                ☐ Futures (Commodities, Indices)
                ☐ Cryptocurrencies
                ☐ Options
                ☐ ETFs
                
                My Choice: Stocks (Large Cap US)
                ```
                
                **Specific Criteria**:
                ```
                For Stocks:
                - Market Cap: >\$10 billion
                - Average Volume: >5 million shares/day
                - Price: >\$20 per share
                - Sector: Technology, Healthcare, Finance
                
                Why: Liquidity, lower manipulation risk
                ```
                
                ### 3. Trading Style and Timeframe
                
                **Choose Your Style**:
                
                **Scalping**:
                - Timeframe: Seconds to minutes
                - Holding: Minutes to hours
                - Charts: 1-min, 5-min
                - Trades/Day: 10-100+
                - Intensity: Very high
                
                **Day Trading**:
                - Timeframe: Minutes to hours
                - Holding: No overnight
                - Charts: 5-min, 15-min, hourly
                - Trades/Day: 1-10
                - Intensity: High
                
                **Swing Trading**:
                - Timeframe: Hours to days
                - Holding: Days to weeks
                - Charts: Hourly, 4-hour, daily
                - Trades/Week: 2-10
                - Intensity: Moderate
                
                **Position Trading**:
                - Timeframe: Days to months
                - Holding: Weeks to months
                - Charts: Daily, weekly
                - Trades/Month: 1-5
                - Intensity: Low
                
                **My Choice**: Swing Trading (daily charts)
                
                ### 4. Trading Strategy
                
                **Define Your Edge**:
                
                **Strategy Name**: Trend Continuation Pullback
                
                **Entry Rules** (specific!):
                ```
                ALL must be true:
                1. Price above 200-day SMA (uptrend)
                2. Pullback to 50% or 61.8% Fibonacci
                3. RSI <40 (oversold)
                4. Bullish reversal candle (hammer, engulfing)
                5. Volume on reversal >1.5× average
                6. Price bounces off support level
                ```
                
                **Entry Timing**:
                ```
                - Conservative: Enter on break above reversal candle
                - Aggressive: Enter at close of reversal candle
                
                I use: Conservative (breakout confirmation)
                ```
                
                **Exit Rules**:
                
                **Stop Loss**:
                ```
                - Place below reversal candle low
                - Or 2 × ATR below entry
                - Never move stop loss against position
                
                Example:
                Entry: \$50
                Reversal low: \$48
                Stop: \$47.80 (below low)
                ```
                
                **Take Profit**:
                ```
                Target 1 (50% position): 2R (2× risk)
                Entry: \$50, Risk: \$2.20, Target: \$54.40
                
                Target 2 (30% position): 3R
                Target: \$56.60
                
                Target 3 (20% position): Trailing stop
                Trail: 1 × ATR below price
                ```
                
                ### 5. Risk Management
                
                **Risk Rules**:
                ```
                Per Trade Risk:
                - Maximum: 1% of account
                - Never exceed 2%
                
                Portfolio Risk:
                - Maximum positions: 5
                - Total risk: Not exceed 5%
                - Per sector: Maximum 40%
                
                Position Size:
                - Calculate: Risk $ ÷ (Entry - Stop)
                - Maximum: 20% of account value
                - Verify before every trade
                
                Loss Limits:
                - Daily: 2% (stop trading)
                - Weekly: 5% (stop trading)
                - Monthly: 10% (reassess strategy)
                ```
                
                ### 6. Trading Routine
                
                **Daily Routine**:
                
                **Pre-Market** (30 minutes before open):
                ```
                ☐ Review market news
                ☐ Check economic calendar
                ☐ Scan for setups
                ☐ Review open positions
                ☐ Check pre-market movers
                ☐ Update watchlist
                ☐ Review trading plan
                ```
                
                **During Market**:
                ```
                ☐ Monitor positions
                ☐ Watch for entry signals
                ☐ Execute trades per plan
                ☐ Set alerts on key levels
                ☐ Take breaks (every 2 hours)
                ☐ Stay disciplined
                ```
                
                **Post-Market**:
                ```
                ☐ Journal all trades
                ☐ Review P&L
                ☐ Update spreadsheet
                ☐ Screen for tomorrow's setups
                ☐ Check if rules followed
                ☐ Identify improvements
                ```
                
                **Weekly Review**:
                ```
                ☐ Calculate win rate
                ☐ Calculate expectancy
                ☐ Review journal
                ☐ Identify patterns
                ☐ Update plan if needed
                ☐ Plan next week
                ```
                
                ### 7. Trading Journal Template
                
                **For Each Trade**:
                ```
                Date: _______
                Symbol: _______
                Setup: _______
                Entry: \$_______ @ _______
                Stop: \$_______ (Risk: \$_______)
                Target 1: \$_______
                Target 2: \$_______
                Position Size: _______ shares
                Risk %: _______%
                
                Pre-Trade:
                ☐ All entry criteria met?
                ☐ Risk calculated?
                ☐ Stop set?
                
                Outcome:
                Exit: \$_______ @ _______
                P&L: \$_______ (_____%)
                R-Multiple: _______
                
                Emotional State:
                Before: _______
                During: _______
                After: _______
                
                Mistakes:
                _______________________
                
                Lessons Learned:
                _______________________
                ```
                
                ### 8. Performance Metrics
                
                **Track These**:
                ```
                Win Rate: _____%
                Average Win: \$_______
                Average Loss: \$_______
                Expectancy: \$_______
                Profit Factor: _______
                Total Trades: _______
                Max Drawdown: _____%
                Monthly Return: _____%
                Sharpe Ratio: _______
                
                Best Trade: \$_______
                Worst Trade: \$_______
                Largest Win Streak: _______
                Largest Loss Streak: _______
                
                Rule Adherence: _____%
                ```
                
                ### 9. Rules for Discipline
                
                **Mandatory Rules**:
                ```
                ✅ I will only trade my defined setups
                ✅ I will always use stop losses
                ✅ I will never risk more than 1% per trade
                ✅ I will stop trading after daily loss limit
                ✅ I will journal every trade
                ✅ I will never move stops against position
                ✅ I will never add to losing positions
                ✅ I will follow my plan 100%
                
                ❌ I will NOT trade on emotion
                ❌ I will NOT revenge trade
                ❌ I will NOT overtrade
                ❌ I will NOT ignore my stops
                ❌ I will NOT trade without a setup
                ❌ I will NOT check social media while trading
                ❌ I will NOT trade if tired/stressed
                ```
                
                ## Creating Your Plan
                
                ### Step 1: Write It Down
                
                **Document Everything**:
                - Type it out (don't just think it)
                - Be specific (no vague rules)
                - Include screenshots of setups
                - Print and keep visible
                
                ### Step 2: Backtest Your Strategy
                
                **Validate Your Edge**:
                - Test on 2+ years data
                - Minimum 100 trades
                - Calculate all metrics
                - Ensure positive expectancy
                
                ### Step 3: Start Small
                
                **Paper Trade First**:
                - 1-3 months paper trading
                - Follow plan exactly
                - Track all metrics
                - Build confidence
                
                **Then Go Live**:
                - Start with smallest size
                - Gradually increase as profitable
                - Maintain discipline
                
                ### Step 4: Review and Adapt
                
                **Continuous Improvement**:
                - Weekly reviews
                - Monthly analysis
                - Quarterly plan updates
                - Annual comprehensive review
                
                **What to Review**:
                - Which setups work best?
                - Which timeframes most profitable?
                - Emotional patterns?
                - Common mistakes?
                - Ways to improve?
                
                ## Common Mistakes
                
                ❌ Not having a written plan
                ❌ Plan too vague ("buy low, sell high")
                ❌ Not following the plan
                ❌ Changing plan after every loss
                ❌ No backtesting
                ❌ No journaling
                ❌ Not tracking metrics
                ❌ Overcomplicating (100-page plan)
                ❌ Never reviewing/improving
                ❌ Not adapting to changing markets
                
                ## Pro Tips
                
                ✅ Start simple (can always add complexity)
                ✅ Write plan down (essential!)
                ✅ Backtest before live trading
                ✅ Paper trade 1-3 months
                ✅ Review plan weekly
                ✅ Follow plan 100% (no exceptions)
                ✅ Journal every single trade
                ✅ Track all performance metrics
                ✅ Update plan based on data
                ✅ Keep plan visible at desk
                ✅ Trading plan is your "business plan"
                ✅ Success = Plan + Discipline + Time
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the most important characteristic of a trading plan?",
                        options = listOf("It's verbal only", "It's written down with specific, clear rules", "It changes daily", "It's very complex"),
                        correctAnswer = 1,
                        explanation = "A trading plan must be written down with specific, clear, and objective rules. Verbal plans are easily forgotten or modified by emotion. Written plans provide accountability and consistency."
                    ),
                    QuizQuestion(
                        question = "Before trading real money, what should you do with your trading plan?",
                        options = listOf("Nothing, start immediately", "Backtest it and paper trade for 1-3 months", "Tell your friends", "Change it daily"),
                        correctAnswer = 1,
                        explanation = "Before risking real money, you must backtest the strategy on historical data and paper trade for 1-3 months to validate the edge, build confidence, and ensure you can follow the plan consistently."
                    ),
                    QuizQuestion(
                        question = "How often should you review and update your trading plan?",
                        options = listOf("Never", "After every trade", "Weekly reviews with periodic updates based on data", "Only when losing"),
                        correctAnswer = 2,
                        explanation = "You should review your trading plan weekly to track performance and identify patterns, with updates made periodically based on data and analysis. Avoid changing it impulsively after losses."
                    )
                )
            )
        )
        )
