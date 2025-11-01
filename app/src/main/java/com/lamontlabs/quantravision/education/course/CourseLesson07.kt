package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.*


val courseLesson07 = Lesson(
            id = 7,
            title = "Volume Analysis & Patterns",
            description = "Use volume to confirm patterns and find opportunities",
            content = """
                Volume is the fuel that drives price movements and the heartbeat of the market. Without proper volume analysis, pattern trading is like driving with your eyes closed - you might get lucky sometimes, but you're missing critical information that determines success or failure.
                
                **Why Volume Matters for Pattern Trading:**
                - Confirms pattern validity - high volume breakouts are 70% more reliable
                - Shows conviction behind price moves - reveals smart money activity
                - Identifies accumulation (smart money buying) and distribution (smart money selling)
                - Spots reversals early - volume divergence warns days before price turns
                - Validates breakouts - separates real moves from false breakouts
                - Reveals institutional participation - retail can't create volume spikes
                
                **Core Volume Principles:**
                
                **1. Volume Precedes Price - The Leading Indicator**
                - Smart money and institutions accumulate BEFORE price breaks out
                - Rising volume while price consolidates = accumulation phase
                - Falling volume on price rally = weak momentum, likely to fail
                - Volume surge precedes major moves by hours or days
                - Look for increasing volume as patterns near completion
                
                **2. Breakout Confirmation - The Critical Rule**
                - Valid breakout requires minimum 50% above 20-day average volume
                - Ideal breakout has 100%+ above average volume
                - Low volume breakout is likely a false move that reverses quickly
                - Volume surge indicates institutional participation - big money confirming the move
                - Without volume confirmation, do NOT enter the trade
                
                **Important Volume Patterns:**
                
                **3. Volume Climax - Exhaustion Signal**
                - Extreme volume spike (2-3x normal or more)
                - Often marks complete exhaustion of buyers or sellers
                - Signals high probability reversal point
                - Capitulation bottom: Panic selling volume spike followed by reversal
                - Euphoria top: Buying frenzy volume spike before major top
                
                **4. Volume Divergence - Early Warning System**
                - Price makes new high but volume decreases = bearish divergence
                - Price makes new low but volume decreases = bullish divergence
                - Shows weakening participation and conviction
                - Reversal is likely within days
                - One of the most reliable early warning signals
                
                **5. On-Balance Volume (OBV) - Cumulative Indicator**
                - Running total: adds volume on up days, subtracts on down days
                - Rising OBV + rising price = healthy confirmed uptrend
                - Falling OBV + rising price = bearish divergence warning
                - Leading indicator - often changes direction before price
                - Use to confirm pattern direction
                
                **6. Volume Profile - Price Level Analysis**
                - Shows total volume traded at each price level
                - High volume areas become strong support/resistance zones
                - Low volume areas (value gaps) - price moves through quickly
                - Point of Control (POC) = price with most volume, strong magnet
                - Useful for identifying key battle zones
                
                **Practical Trading Application:**
                - Always verify volume before entering ANY pattern trade
                - Avoid patterns that develop or break out on weak volume
                - Look for volume spikes at key support/resistance levels for confirmation
                - Use volume divergence as signal to tighten stops or exit early
                - Combine volume with price patterns for highest probability setups
            """.trimIndent(),
            keyPoints = listOf(
                "Volume confirms price action",
                "Breakouts need volume surge",
                "Volume divergence warns of reversal",
                "Climax volume shows exhaustion",
                "Volume profile identifies key levels"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Volume Climax Bottom",
                    description = "Capitulation selling marks bottom",
                    identificationTips = listOf(
                        "Massive volume spike (3x+ average)",
                        "Wide-range down candle",
                        "Followed by reversal candle",
                        "Volume returns to normal",
                        "Often marks panic selling exhaustion"
                    )
                ),
                PatternExample(
                    patternName = "Breakout with Volume",
                    description = "Valid breakout confirmation",
                    identificationTips = listOf(
                        "Price breaks resistance/support",
                        "Volume 50%+ above average",
                        "Close beyond breakout level",
                        "Subsequent candles stay above/below",
                        "Retest on lower volume"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What does a breakout with low volume indicate?",
                        options = listOf(
                            "Very strong move",
                            "Likely false breakout",
                            "Institutional buying",
                            "Nothing significant"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Low volume on a breakout suggests weak conviction and likely a false move that will reverse."
                    ),
                    Question(
                        question = "What is volume divergence?",
                        options = listOf(
                            "Volume and price both rising",
                            "Price makes new high/low but volume decreases",
                            "Volume doubles every day",
                            "Volume is always zero"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Volume divergence occurs when price makes new extremes but volume weakens, warning of trend exhaustion."
                    ),
                    Question(
                        question = "A volume climax often signals:",
                        options = listOf(
                            "Trend continuation",
                            "Normal market behavior",
                            "Potential exhaustion and reversal",
                            "Time to hold position"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Volume climax shows extreme participation and often marks exhaustion, signaling a potential reversal."
                    ),
                    Question(
                        question = "What is On-Balance Volume (OBV) used for?",
                        options = listOf(
                            "Measuring price changes only",
                            "Cumulative volume indicator to confirm trends and spot divergences",
                            "Counting the number of trades",
                            "Predicting exact future prices"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "OBV is a cumulative volume indicator that helps confirm trend strength and identify divergences as early warning signals."
                    )
                )
            )
        )
