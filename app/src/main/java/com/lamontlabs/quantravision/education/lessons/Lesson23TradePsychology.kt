package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson23TradePsychology = Lesson(
            id = 23,
            title = "Trade Psychology",
            category = "Trading Fundamentals",
            duration = "12 min",
            content = """
                # Trade Psychology
                
                ## Overview
                
                Trading psychology is the emotional and mental state that determines your trading success or failure. Studies show that trading success is 80% psychology and 20% strategy. You can have the best strategy in the world, but without proper psychology, you will fail.
                
                ## The Two Primary Emotions
                
                ### Fear:
                - **Fear of Loss**: Prevents entry or causes premature exit
                - **Fear of Missing Out (FOMO)**: Causes impulsive entries
                - **Fear of Being Wrong**: Prevents cutting losses
                - **Fear of Letting Profits Go**: Causes early profit-taking
                
                ### Greed:
                - **Over-trading**: Taking too many trades
                - **Over-sizing**: Position sizes too large
                - **Holding Too Long**: Not taking profits
                - **Revenge Trading**: Trying to win back losses
                
                ## Common Psychological Traps
                
                ### 1. Revenge Trading
                
                **Definition**: Trading to win back losses
                
                **Symptoms**:
                - Taking trades outside your plan
                - Increasing position size after losses
                - Abandoning strategy
                - Emotional decision-making
                
                **Example**:
                ```
                Lose \$100 on Trade 1
                "I need to make it back NOW!"
                Take risky Trade 2 with \$300 position
                Lose \$150
                Now down \$250 and desperate
                Continue cycle... blow up account
                ```
                
                **Solution**:
                ✅ Set daily/weekly loss limits
                ✅ Step away after 2-3 losses
                ✅ Never increase size after losses
                ✅ Accept losses as cost of business
                
                ### 2. FOMO (Fear of Missing Out)
                
                **Definition**: Entering trades because "everyone else is making money"
                
                **Symptoms**:
                - Chasing breakouts
                - Entering without proper setup
                - Buying tops/selling bottoms
                - Social media influenced trades
                
                **Example**:
                ```
                Stock up 50% in week
                Everyone talking about it
                You buy at peak
                Stock crashes 20% next day
                FOMO cost you money
                ```
                
                **Solution**:
                ✅ Wait for your setup
                ✅ Never chase parabolic moves
                ✅ Avoid social media during trading
                ✅ Trade your plan, not emotions
                
                ### 3. Analysis Paralysis
                
                **Definition**: Over-analyzing to point of inaction
                
                **Symptoms**:
                - Checking 20+ indicators
                - Never pulling the trigger
                - Waiting for "perfect" setup
                - Missing opportunities
                
                **Example**:
                ```
                Perfect setup appears
                "Let me check one more thing..."
                Add another indicator
                Check news
                Check opinion
                Setup gone, opportunity missed
                ```
                
                **Solution**:
                ✅ Define clear entry rules
                ✅ Limit indicators (3-5 max)
                ✅ Trust your analysis
                ✅ Pull trigger when rules met
                
                ### 4. Overconfidence
                
                **Definition**: Believing you can't lose after wins
                
                **Symptoms**:
                - Increasing position sizes
                - Taking low-quality setups
                - Ignoring risk management
                - Feeling invincible
                
                **Example**:
                ```
                5 winning trades in row
                "I can't lose!"
                Risk 10% on next trade
                Trade fails
                Lose week's profits
                ```
                
                **Solution**:
                ✅ Maintain same risk per trade
                ✅ Follow rules during win streaks
                ✅ Remember regression to mean
                ✅ Stay humble always
                
                ## Building Mental Discipline
                
                ### The Trading Plan:
                
                **Create Written Rules**:
                ```
                Entry Rules:
                - RSI <30
                - Bullish candlestick pattern
                - Above 200 SMA
                
                Exit Rules:
                - Stop: Below pattern low
                - Target 1: 2R
                - Target 2: 3R
                
                Risk Management:
                - Max risk: 1% per trade
                - Max positions: 5
                - Daily loss limit: 2%
                ```
                
                **Follow Rules 100%**:
                - No exceptions
                - No "gut feelings"
                - No revenge trades
                - No FOMO entries
                
                ### The Trading Journal:
                
                **Track Everything**:
                ```
                Date: 2025-01-15
                Setup: Bull Flag
                Entry: \$50.00
                Stop: \$48.50
                Target: \$53.00
                Risk: \$100 (1%)
                Result: +\$200 (2R)
                Emotional State: Calm, patient
                Mistakes: None
                Lessons: Waiting for setup paid off
                ```
                
                **Review Weekly**:
                - What worked?
                - What didn't work?
                - Emotional patterns?
                - Areas to improve?
                
                ## Emotional Control Techniques
                
                ### Technique 1: Pre-Trade Routine
                
                **Before Every Trade**:
                1. Take 3 deep breaths
                2. Review trading plan
                3. Calculate position size
                4. Set alerts/stops
                5. Visualize trade
                6. Execute with confidence
                
                ### Technique 2: Meditation
                
                **Benefits**:
                - Reduces stress
                - Improves focus
                - Enhances discipline
                - Better decision-making
                
                **Practice**:
                - 10 minutes daily
                - Before trading session
                - Focus on breathing
                - Clear your mind
                
                ### Technique 3: Physical Exercise
                
                **Benefits**:
                - Reduces cortisol (stress hormone)
                - Increases endorphins
                - Improves sleep
                - Better mental clarity
                
                **Recommendation**:
                - 30 minutes daily
                - Morning preferred
                - Cardio or weights
                - Before trading
                
                ### Technique 4: Breaks
                
                **Take Breaks**:
                - After big win: Step away
                - After big loss: Step away
                - After 2-3 losses: Stop trading
                - Screen fatigue: 15-min break every 2 hours
                
                ## The Professional Mindset
                
                ### Think Like a Casino:
                
                **Casinos don't worry about individual hands**:
                - They play the long game
                - They have edge
                - They know probabilities
                - Variance doesn't scare them
                
                **You should too**:
                - Focus on process, not outcome
                - 1,000 trades matter, not 1
                - Edge plays out over time
                - Accept variance
                
                ### Detach from Outcomes:
                
                **Bad Mindset**:
                ```
                "I NEED this trade to win"
                "I can't afford to lose"
                "This HAS to work"
                → Emotional trading → Mistakes
                ```
                
                **Good Mindset**:
                ```
                "This trade has 60% probability"
                "I might lose, that's okay"
                "I'll follow my rules"
                → Calm trading → Success
                ```
                
                ## Dealing with Losses
                
                ### Accept Losses:
                
                **Facts**:
                - Losses are inevitable
                - Even best traders lose 40-50%
                - Losses are cost of doing business
                - One loss means nothing
                
                **Reframe Losses**:
                ```
                ❌ "I'm a bad trader"
                ✅ "This trade didn't work"
                
                ❌ "I lost money"
                ✅ "I paid for education"
                
                ❌ "My strategy failed"
                ✅ "Variance happened"
                ```
                
                ### Learn from Losses:
                
                **After Every Loss**:
                1. Did I follow my rules? (Yes = Good loss!)
                2. What could I improve?
                3. Was setup actually valid?
                4. Update journal
                5. Move on (don't dwell)
                
                ## Advanced Psychology
                
                ### The Peak-End Rule:
                
                **Concept**: We remember peaks and ends
                
                **Application**:
                - End sessions on positive note
                - Don't force trades at day's end
                - Stop after big win (end high)
                - Review good trades weekly
                
                ### Loss Aversion:
                
                **Concept**: Losses hurt 2x more than gains feel good
                
                **Problem**: Holding losers, cutting winners early
                
                **Solution**:
                - Use stop losses (automate)
                - Use profit targets (automate)
                - Follow rules, not emotions
                
                ## Common Mistakes
                
                ❌ Trading while emotional (angry, sad, stressed)
                ❌ Revenge trading after losses
                ❌ FOMO entries without setup
                ❌ Overconfidence after wins
                ❌ Not having trading plan
                ❌ Not journaling trades
                ❌ Ignoring warning signs
                ❌ No daily loss limits
                ❌ Not taking breaks
                ❌ Comparing to other traders
                
                ## Pro Tips
                
                ✅ Psychology is 80% of trading success
                ✅ Have written trading plan (follow it!)
                ✅ Journal every trade with emotions
                ✅ Set daily loss limits (hard stop)
                ✅ Step away after 2-3 losses
                ✅ Meditate daily (10+ minutes)
                ✅ Exercise before trading
                ✅ Sleep 7-8 hours
                ✅ Accept losses as normal
                ✅ Never trade to "make back" losses
                ✅ Detach from individual trades
                ✅ Think in probabilities, not certainties
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What percentage of trading success is attributed to psychology?",
                        options = listOf("20%", "50%", "80%", "100%"),
                        correctAnswer = 2,
                        explanation = "Studies show that trading success is approximately 80% psychology and 20% strategy. Even the best strategy will fail without proper psychological discipline and emotional control."
                    ),
                    QuizQuestion(
                        question = "What is revenge trading and why is it dangerous?",
                        options = listOf("A good strategy", "Trading to win back losses, leading to bigger losses", "Trading for fun", "Trading with a plan"),
                        correctAnswer = 1,
                        explanation = "Revenge trading is attempting to quickly win back losses by taking impulsive, high-risk trades outside your plan. It's driven by emotion and typically leads to even larger losses."
                    ),
                    QuizQuestion(
                        question = "How should professional traders think about individual trades?",
                        options = listOf("Each trade must win", "Detach from outcomes, focus on process and probabilities", "Get emotional about each trade", "Ignore all trades"),
                        correctAnswer = 1,
                        explanation = "Professional traders detach from individual trade outcomes and focus on the process. They think in probabilities, understanding that their edge plays out over hundreds of trades, not one."
                    )
                )
            )
        )
