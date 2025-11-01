package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.*


val courseLesson25 = Lesson(
            id = 25,
            title = "Psychology, Risk & Advanced Execution",
            description = "Master the mental game, risk management, and professional execution strategies for long-term trading success",
            content = """
                Technical skills get you into trades, but psychology and risk management keep you profitable long-term. This final lesson integrates everything into a complete professional approach.
                
                **Trading Psychology Mastery:**
                
                **1. The Four Trading Emotions:**
                - Fear: Causes missed opportunities and early exits
                - Greed: Leads to overleveraging and holding too long
                - Hope: Makes you hold losing trades
                - Regret: Causes revenge trading
                
                **Managing Emotions:**
                - Follow your playbook mechanically
                - Accept that losses are part of the process
                - Never trade to "make back" losses
                - Take breaks after 3 consecutive losses
                - Keep detailed journal of emotional states
                
                **2. Professional Risk Management:**
                
                **Position Sizing Formula:**
                Account Risk ÷ Trade Risk = Position Size
                - $10,000 account, 2% rule = $200 max risk
                - Entry $50, stop $48 = $2 risk per share
                - Position size: $200 ÷ $2 = 100 shares
                
                **The 2% Rule (Non-Negotiable):**
                - Never risk more than 2% on single trade
                - Protects account from catastrophic loss
                - Allows for 50 consecutive losses before ruin
                - Professional standard across all markets
                
                **Portfolio Heat Management:**
                - Maximum 6% total portfolio risk
                - No more than 3 correlated positions
                - Reduce size in losing streaks
                - Increase size gradually in winning streaks
                
                **3. Advanced Execution Strategies:**
                
                **Entry Techniques:**
                - Limit orders at key levels (better price)
                - Stop orders for breakouts (momentum)
                - Partial entries (average in)
                - Scale in: 50% at pattern, 50% on confirmation
                
                **Exit Strategies:**
                - Mechanical: Fixed targets from pattern
                - Trailing: Follow trend with moving stops
                - Time-based: Exit if no progress in X days
                - Partial scaling: 50% at target 1, trail remainder
                
                **4. Performance Tracking:**
                
                **Key Metrics to Track:**
                - Win rate (aim for 50%+)
                - Average win vs average loss (need > 1:1)
                - Profit factor (gross wins ÷ gross losses > 1.5)
                - Maximum drawdown (< 20%)
                - Risk-adjusted return (Sharpe ratio)
                
                **5. Professional Trading Routine:**
                
                **Pre-Market (30 minutes):**
                - Review overnight news and gaps
                - Check higher timeframe bias
                - Identify 3-5 watchlist setups
                - Set alerts for key levels
                
                **During Market:**
                - Execute only planned setups
                - No emotional trading
                - Follow risk rules religiously
                - One trade at a time initially
                
                **Post-Market (15 minutes):**
                - Journal all trades
                - Screenshot setups
                - Note what worked/didn't work
                - Plan next day watchlist
                
                **The Path to Mastery:**
                - 1000+ hours of screen time required
                - Mastery takes 2-3 years minimum
                - Paper trade until consistently profitable
                - Start small, scale gradually
                - Never stop learning and adapting
            """.trimIndent(),
            keyPoints = listOf(
                "Never risk more than 2% per trade - no exceptions",
                "Psychology and discipline matter more than patterns",
                "Track all trades and analyze statistics religiously",
                "Follow mechanical rules to overcome emotions",
                "Mastery takes years - be patient and persistent"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Complete Trade Example",
                    description = "Professional execution from start to finish",
                    identificationTips = listOf(
                        "Setup: Daily bull flag in uptrend",
                        "Account: $10,000, 2% rule = $200 max risk",
                        "Entry: $50.00 at flag breakout",
                        "Stop: $48.50 (below flag low)",
                        "Risk per share: $1.50",
                        "Position: $200 ÷ $1.50 = 133 shares",
                        "Target 1: $53.00 (50% exit)",
                        "Target 2: $55.50 (trail remainder)"
                    )
                ),
                PatternExample(
                    patternName = "Trading Journal Entry",
                    description = "Professional trade documentation",
                    identificationTips = listOf(
                        "Date, time, market conditions",
                        "Pattern identified and timeframe",
                        "Entry price, size, and rationale",
                        "Stop loss placement and risk %",
                        "Target levels and exit strategy",
                        "Emotional state and notes",
                        "Outcome and lessons learned"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What is the maximum risk per trade?",
                        options = listOf(
                            "5% of account",
                            "10% of account",
                            "2% of account",
                            "Whatever you feel"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "The 2% rule is the professional standard - never risk more than 2% of your account on a single trade."
                    ),
                    Question(
                        question = "How do you calculate position size?",
                        options = listOf(
                            "Random guess",
                            "Maximum you can afford",
                            "Account Risk ÷ Trade Risk",
                            "Buy as much as possible"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Position size = Account Risk (2% max) ÷ Trade Risk (entry to stop distance per share)."
                    ),
                    Question(
                        question = "What should you do after 3 consecutive losses?",
                        options = listOf(
                            "Trade larger to make it back",
                            "Take a break and review",
                            "Keep trading emotionally",
                            "Quit forever"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "After 3 consecutive losses, take a break to reset emotionally and review what's not working."
                    ),
                    Question(
                        question = "How long does mastery typically take?",
                        options = listOf(
                            "1 week",
                            "1 month",
                            "2-3 years of dedicated practice",
                            "It's impossible"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Trading mastery typically requires 2-3 years of dedicated practice and 1000+ hours of screen time."
                    )
                )
            )
        )
