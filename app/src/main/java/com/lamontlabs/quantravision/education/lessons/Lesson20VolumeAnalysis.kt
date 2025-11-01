package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson20VolumeAnalysis = Lesson(
            id = 20,
            title = "Volume Analysis",
            category = "Technical Analysis Advanced",
            duration = "11 min",
            content = """
                # Volume Analysis
                
                ## Overview
                
                Volume is the number of shares or contracts traded during a specific period. It's a critical confirmation tool that validates price movements and patterns. Understanding volume helps traders distinguish between strong moves and weak moves that are likely to fail.
                
                ## Why Volume Matters
                
                ### The Fundamental Principle:
                **"Volume precedes price"**
                
                Volume shows the conviction behind price movements:
                - **High Volume**: Strong conviction, sustainable move
                - **Low Volume**: Weak conviction, unsustainable move
                - **Volume Confirms**: Validates breakouts and reversals
                - **Volume Diverges**: Warns of potential reversals
                
                ### Psychology:
                - High volume = many participants agreeing
                - Low volume = few participants, lack of consensus
                - Increasing volume = growing interest and momentum
                - Decreasing volume = waning interest, potential reversal
                
                ## Basic Volume Principles
                
                ### Principle 1: Confirm Trends
                
                **Healthy Uptrend**:
                ```
                Price: ↗️ (Rising)
                Volume: 📊📊📊 (Increasing on up days)
                         📊 (Decreasing on down days)
                
                = Strong, sustainable uptrend
                ```
                
                **Healthy Downtrend**:
                ```
                Price: ↘️ (Falling)
                Volume: 📊📊📊 (Increasing on down days)
                         📊 (Decreasing on up days)
                
                = Strong, sustainable downtrend
                ```
                
                ### Principle 2: Volume Divergence
                
                **Bearish Divergence** (Warning):
                ```
                Price:  Higher highs
                         /\    /\
                        /  \  /  \
                       
                Volume: 📊📊📊  📊📊
                        Declining volume = Warning
                
                = Uptrend losing strength
                ```
                
                **Bullish Divergence**:
                ```
                Price:  Lower lows
                        \  /  \  /
                         \/    \/
                       
                Volume: 📊📊📊  📊📊
                        Declining volume = Warning
                
                = Downtrend losing strength
                ```
                
                ## Volume in Price Patterns
                
                ### Breakouts (Most Critical):
                
                **Valid Breakout**:
                ```
                Resistance ____________
                          |  Pattern  | ↑ BREAKOUT
                          |__________|  📊📊📊📊
                                      High Volume!
                
                ✅ 2-3x average volume = Strong
                ✅ Confirms breakout validity
                ✅ Higher probability of success
                ```
                
                **False Breakout**:
                ```
                Resistance ____________
                          |  Pattern  | ↑ Breakout?
                          |__________|  📊
                                      Low Volume
                
                ❌ Low volume = Likely to fail
                ❌ Return to range probable
                ❌ Avoid trading
                ```
                
                ### Volume in Reversals:
                
                **Climax Volume (Bottom)**:
                - Massive volume spike at low
                - Capitulation selling
                - "Selling climax"
                - Often marks bottom
                - Followed by reversal
                
                **Exhaustion Volume (Top)**:
                - Very high volume at highs
                - Euphoric buying
                - "Buying climax"
                - Often marks top
                - Distribution occurring
                
                ## Advanced Volume Concepts
                
                ### On-Balance Volume (OBV):
                
                **Concept**:
                - Running total of volume
                - Add volume on up days
                - Subtract volume on down days
                - Creates cumulative line
                
                **Interpretation**:
                - **OBV Rising** + Price Rising = Confirmation
                - **OBV Falling** + Price Falling = Confirmation
                - **OBV Rising** + Price Falling = Bullish divergence
                - **OBV Falling** + Price Rising = Bearish divergence
                
                ### Volume Price Analysis (VPA):
                
                **High Volume + Up Bar**:
                - Professional buying
                - Strong accumulation
                - Bullish signal
                
                **High Volume + Down Bar**:
                - Professional selling
                - Distribution
                - Bearish signal
                
                **Low Volume + Up Bar**:
                - Weak buying
                - No support from professionals
                - Likely to fail
                
                **Low Volume + Down Bar**:
                - Weak selling
                - No conviction
                - May be bottoming
                
                ### Volume Spread Analysis (VSA):
                
                **Key Concepts**:
                1. **Spread**: High - Low range
                2. **Volume**: Total volume
                3. **Close**: Where candle closes
                
                **Combinations**:
                - **Wide Spread + High Volume + Close Near High** = Very bullish
                - **Wide Spread + High Volume + Close Near Low** = Very bearish
                - **Narrow Spread + High Volume** = Potential reversal
                - **Wide Spread + Low Volume** = Weak move
                
                ## Volume Patterns
                
                ### Pattern 1: Volume Climax
                
                **Buying Climax** (Top):
                - Highest volume in months
                - Price spike upward
                - Exhaustion gap possible
                - Followed by reversal
                
                **Selling Climax** (Bottom):
                - Highest volume in months
                - Price spike downward
                - Panic selling
                - Often the bottom
                
                ### Pattern 2: Volume Dry Up
                
                **Characteristics**:
                - Volume shrinks significantly
                - Price compresses
                - Indecision
                - Precedes major move
                
                **Trading**:
                - Watch for direction
                - Volume will spike on breakout
                - Trade the direction of volume surge
                
                ### Pattern 3: Increasing Volume Trend
                
                **In Uptrend**:
                - Each rally has higher volume
                - Pullbacks have lower volume
                - Confirms strong trend
                - Continue holding/buying
                
                **In Downtrend**:
                - Each decline has higher volume
                - Bounces have lower volume
                - Confirms strong downtrend
                - Continue holding short/avoid longs
                
                ## Volume Indicators
                
                ### Volume Moving Average:
                - Calculate MA of volume (e.g., 20-period)
                - Compare current volume to average
                - Above MA = Higher than normal
                - Below MA = Lower than normal
                - Use for breakout confirmation
                
                ### Volume Oscillator:
                - Difference between two volume MAs
                - Positive = Short-term volume > Long-term
                - Negative = Short-term volume < Long-term
                - Shows volume momentum
                
                ### Accumulation/Distribution Line:
                - Similar to OBV
                - Considers close location in range
                - Shows buying/selling pressure
                - Divergence signals important
                
                ## Volume Trading Rules
                
                ### Rule 1: Confirmation
                ✅ **Always confirm with volume**:
                - Breakouts need 2x+ volume
                - Reversals need volume spike
                - Trend continuation needs steady volume
                
                ### Rule 2: Divergence
                ⚠️ **Watch for divergence**:
                - Price up, volume down = Warning
                - Price down, volume down = Potential bottom
                - OBV divergence = Early warning
                
                ### Rule 3: Climaxes
                🚨 **Respect volume climaxes**:
                - Buying climax = Top likely
                - Selling climax = Bottom likely
                - Highest volume often marks turning points
                
                ## Practical Trading Strategies
                
                ### Strategy 1: Volume Confirmation Breakout
                
                **Setup**:
                1. Identify consolidation pattern
                2. Wait for breakout
                3. Check volume (must be 2x+ average)
                4. Enter on high volume breakout
                
                **Entry**: Breakout point with volume
                **Stop**: Opposite side of pattern
                **Target**: Measured move or next resistance
                
                ### Strategy 2: Volume Climax Reversal
                
                **Setup**:
                1. Identify extended trend
                2. Watch for volume spike (3x+ average)
                3. Price reversal candle
                4. Climax exhaustion confirmed
                
                **Entry**: After reversal candle
                **Stop**: Beyond climax point
                **Target**: Retracement levels (38.2%, 50%)
                
                ### Strategy 3: Volume Divergence
                
                **Setup**:
                1. Uptrend with rising prices
                2. Volume declining on rallies
                3. OBV showing divergence
                4. Reversal pattern forms
                
                **Entry**: Pattern confirmation
                **Stop**: Above recent high
                **Target**: Support levels
                
                ## Common Mistakes
                
                ❌ Trading breakouts on low volume
                ❌ Ignoring volume divergence
                ❌ Not using volume average for context
                ❌ Confusing volume with price
                ❌ Expecting exact volume levels
                ❌ Not considering market conditions
                ❌ Overcomplicating with too many indicators
                
                ## Pro Tips
                
                ✅ Volume should increase in direction of trend
                ✅ 2-3x average volume confirms breakouts
                ✅ Climax volume often marks reversals
                ✅ Declining volume in trend = warning
                ✅ OBV divergence is early warning signal
                ✅ Higher timeframes more significant
                ✅ Combine volume with price action
                ✅ Watch for volume spikes at key levels
                ✅ Low volume moves don't last
                ✅ Volume precedes price
                ✅ Use volume moving average as baseline
                ✅ Respect buying/selling climaxes
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What volume level confirms a valid breakout?",
                        options = listOf("Same as average", "2-3x average volume", "Below average", "Volume doesn't matter"),
                        correctAnswer = 1,
                        explanation = "A valid breakout requires 2-3x average volume or higher. This high volume confirms strong conviction and significantly increases the probability of breakout success."
                    ),
                    QuizQuestion(
                        question = "What does declining volume during an uptrend indicate?",
                        options = listOf("Very bullish", "Warning sign - uptrend losing strength", "Normal behavior", "Time to buy more"),
                        correctAnswer = 1,
                        explanation = "Declining volume during an uptrend is a warning sign of bearish divergence, indicating the uptrend is losing strength and participation, which often precedes a reversal."
                    ),
                    QuizQuestion(
                        question = "What is a 'selling climax' and what does it typically indicate?",
                        options = listOf("Start of downtrend", "Massive volume spike at lows, often marks the bottom", "Normal selling", "Time to sell"),
                        correctAnswer = 1,
                        explanation = "A selling climax is a massive volume spike at price lows representing panic selling and capitulation. It often marks the bottom of a decline as all weak hands exit."
                    )
                )
            )
        )
