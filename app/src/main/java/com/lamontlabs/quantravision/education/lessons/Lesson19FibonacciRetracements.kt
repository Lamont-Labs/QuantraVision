package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson19FibonacciRetracements = Lesson(
            id = 19,
            title = "Fibonacci Retracements",
            category = "Technical Analysis Advanced",
            duration = "10 min",
            content = """
                # Fibonacci Retracements
                
                ## Overview
                
                Fibonacci retracements are horizontal lines that indicate potential support and resistance levels based on the Fibonacci sequence. These levels are derived from mathematical ratios found throughout nature and are widely used by traders to identify potential reversal points.
                
                ## The Fibonacci Sequence
                
                ### Origin:
                The Fibonacci sequence: 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144...
                
                **Pattern**: Each number is the sum of the two preceding numbers.
                
                ### The Golden Ratio:
                - Dividing any number by the next number ≈ 0.618 (61.8%)
                - Dividing by the number two places higher ≈ 0.382 (38.2%)
                - Dividing by the number three places higher ≈ 0.236 (23.6%)
                
                These ratios appear throughout nature, architecture, and financial markets.
                
                ## Key Fibonacci Levels
                
                ### Primary Retracement Levels:
                
                **0%** - Start of move (recent swing)
                **23.6%** - Shallow retracement
                **38.2%** - Moderate retracement (first golden ratio)
                **50%** - Midpoint (not Fibonacci, but widely used)
                **61.8%** - Deep retracement (the Golden Ratio)
                **78.6%** - Very deep retracement (square root of 0.618)
                **100%** - Complete retracement (original swing point)
                
                ### Extension Levels (for targets):
                **127.2%** - First extension
                **161.8%** - Golden extension (most important)
                **200%** - Double extension
                **261.8%** - Major extension
                
                ## How to Apply Fibonacci Retracements
                
                ### In an Uptrend:
                
                ```
                100% ___•_______________ (High)
                 78.6% ___________
                 61.8% ___________  ← Golden Ratio
                 50%  ___________
                 38.2% ___________
                 23.6% ___________
                  0%  ___•_______________ (Low)
                  
                  ← Measure from Low to High →
                ```
                
                **How to Draw**:
                1. Identify significant upward move
                2. Find the swing low (starting point)
                3. Find the swing high (ending point)
                4. Draw Fibonacci from low to high
                5. Watch for support at retracement levels
                
                ### In a Downtrend:
                
                ```
                  0%  ___•_______________ (High)
                 23.6% ___________
                 38.2% ___________
                 50%  ___________
                 61.8% ___________  ← Golden Ratio
                 78.6% ___________
                100% ___•_______________ (Low)
                  
                  ← Measure from High to Low →
                ```
                
                **How to Draw**:
                1. Identify significant downward move
                2. Find the swing high (starting point)
                3. Find the swing low (ending point)
                4. Draw Fibonacci from high to low
                5. Watch for resistance at retracement levels
                
                ## Trading with Fibonacci
                
                ### Strategy 1: Buying Pullbacks (Uptrend)
                
                **Setup**:
                1. Strong uptrend identified
                2. Price begins to retrace
                3. Wait for price to approach Fib level
                
                **Entry Zones**:
                - **38.2%**: Aggressive entry (shallow retracement)
                - **50%**: Moderate entry (common retracement)
                - **61.8%**: Conservative entry (deep retracement)
                
                **Confirmation**:
                - Bullish reversal candle at Fib level
                - Volume spike
                - RSI divergence
                - Support from other indicators
                
                **Stop Loss**:
                - Below next Fib level
                - Or below 78.6% retracement
                
                **Target**:
                - Previous high (100% -> 0%)
                - Fibonacci extensions (127.2%, 161.8%)
                
                ### Strategy 2: Selling Rallies (Downtrend)
                
                **Setup**:
                1. Strong downtrend identified
                2. Price begins to retrace upward
                3. Wait for price to approach Fib level
                
                **Entry Zones**:
                - **38.2%**: Aggressive short (shallow rally)
                - **50%**: Moderate short
                - **61.8%**: Conservative short (deeper rally)
                
                **Confirmation**:
                - Bearish reversal candle at Fib level
                - Volume increase
                - Resistance confirmed
                
                **Stop Loss**:
                - Above next Fib level
                - Or above 78.6% retracement
                
                **Target**:
                - Previous low
                - Fibonacci extensions downward
                
                ## The Golden Ratio (61.8%)
                
                ### Why 61.8% is Special:
                - Most watched Fib level
                - Strongest support/resistance
                - "Last chance" entry point
                - Deep retracement before continuation
                
                ### Trading the 61.8%:
                
                **Bullish Setup**:
                - Price retraces to 61.8% in uptrend
                - Look for strong reversal signals
                - Often creates best risk/reward
                - High probability continuation point
                
                **Bearish Setup**:
                - Price rallies to 61.8% in downtrend
                - Watch for rejection signals
                - Strong resistance expected
                - Often marks rally exhaustion
                
                ## Confluence with Other Tools
                
                ### Fibonacci + Support/Resistance:
                ✅ **Very Strong**:
                - 61.8% Fib + previous support = high probability
                - 50% Fib + round number (e.g., $100) = strong level
                - Multiple Fib levels from different swings = cluster
                
                ### Fibonacci + Moving Averages:
                ✅ **Powerful Combination**:
                - 50% Fib + 50 SMA alignment = very strong
                - 61.8% Fib + 200 SMA = major support/resistance
                
                ### Fibonacci + Trendlines:
                ✅ **Triple Confluence**:
                - Fib level + trendline + support = highest probability
                - Multiple timeframe Fib alignment = very strong
                
                ## Fibonacci Extensions (Price Targets)
                
                ### How to Use:
                After a retracement, measure the original move to project targets:
                
                **In Uptrend**:
                1. Identify swing low to swing high
                2. Wait for retracement (to 38.2%, 50%, or 61.8%)
                3. Project extensions from retracement low:
                   - **127.2%**: First target
                   - **161.8%**: Major target (Golden extension)
                   - **200%**: Extended target
                
                **Example**:
                - Stock moves $50 → $100 (50-point move)
                - Retraces to $75 (50% retracement)
                - Targets: $125 (127.2%), $131 (161.8%), $150 (200%)
                
                ## Common Retracement Behaviors
                
                ### Strong Trends:
                - Retrace to 23.6% or 38.2%
                - Shallow retracements
                - Quick continuation
                - Strong momentum
                
                ### Normal Trends:
                - Retrace to 50%
                - Most common
                - Healthy pullback
                - Sustainable trend
                
                ### Weak Trends:
                - Retrace to 61.8% or deeper
                - Losing momentum
                - May be trend exhaustion
                - Caution advised
                
                ### Failed Trends:
                - Break through 78.6%
                - Approach 100% (full retracement)
                - Trend likely over
                - Reversal probable
                
                ## Advanced Techniques
                
                ### Multiple Timeframe Fibonacci:
                - Draw Fibs on daily, weekly, monthly
                - Cluster zones = very strong levels
                - Higher timeframe Fibs more significant
                
                ### Fibonacci Fans:
                - Diagonal lines from swing point
                - Dynamic support/resistance
                - Advanced technique
                
                ### Fibonacci Arcs:
                - Curved lines
                - Time and price combination
                - Specialized use
                
                ## Common Mistakes
                
                ❌ Using every small swing (use significant moves)
                ❌ Trading Fib levels without confirmation
                ❌ Expecting exact touches (use zones)
                ❌ Ignoring overall trend context
                ❌ Not combining with other tools
                ❌ Drawing Fibs incorrectly (wrong direction)
                ❌ Using too many Fib overlays (clutter)
                ❌ Forcing Fib fits on random price action
                
                ## Pro Tips
                
                ✅ Use significant swings (at least 10-20% moves)
                ✅ 61.8% is the most important level
                ✅ 50% very popular (self-fulfilling)
                ✅ Combine with support/resistance for confluence
                ✅ Wait for confirmation before entering
                ✅ Higher timeframes = more reliable levels
                ✅ Extensions (161.8%) excellent for targets
                ✅ Fib clusters from multiple swings = very strong
                ✅ Use zones, not exact lines
                ✅ Volume should increase at bounces
                ✅ Works best in trending markets
                ✅ Multiple timeframe analysis powerful
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the most important and widely watched Fibonacci retracement level?",
                        options = listOf("23.6%", "50%", "61.8% (The Golden Ratio)", "100%"),
                        correctAnswer = 2,
                        explanation = "The 61.8% level, known as the Golden Ratio, is the most important Fibonacci level. It represents the strongest support/resistance and is often the 'last chance' entry point before trend continuation."
                    ),
                    QuizQuestion(
                        question = "How do you draw Fibonacci retracements in an uptrend?",
                        options = listOf("High to low", "From swing low to swing high", "Randomly", "Only on Mondays"),
                        correctAnswer = 1,
                        explanation = "In an uptrend, draw Fibonacci retracements from the swing low (starting point) to the swing high (ending point) to identify potential support levels during pullbacks."
                    ),
                    QuizQuestion(
                        question = "What does it indicate when price retraces only to the 23.6% or 38.2% level?",
                        options = listOf("Weak trend", "Strong trend with shallow retracement", "Failed pattern", "No trend"),
                        correctAnswer = 1,
                        explanation = "Shallow retracements to 23.6% or 38.2% indicate a very strong trend with minimal pullback, suggesting powerful momentum and high probability of continuation."
                    )
                )
            )
        )
