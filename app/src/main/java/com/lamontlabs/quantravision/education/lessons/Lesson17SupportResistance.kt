package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson17SupportResistance = Lesson(
            id = 17,
            title = "Support and Resistance",
            category = "Technical Analysis Fundamentals",
            duration = "12 min",
            content = """
                # Support and Resistance
                
                ## Overview
                
                Support and Resistance are foundational concepts in technical analysis. They represent price levels where buying or selling pressure is strong enough to reverse or pause price movements. Understanding these levels is crucial for making informed trading decisions.
                
                ## What is Support?
                
                ### Definition:
                Support is a price level where buying pressure is expected to be strong enough to prevent the price from falling further. It acts as a "floor" that prevents price from declining.
                
                ### Visual Representation:
                ```
                Price
                  ↑
                  |     /\      /\
                  |    /  \    /  \
                  |   /    \  /    \
                  |  /      \/      \___
                  |_/_____________________ ← Support Level
                  |  Bounce  Bounce  Hold
                  └──────────────────────→ Time
                ```
                
                ### Why Support Forms:
                
                1. **Psychological Levels**: Round numbers (e.g., $50, $100)
                2. **Previous Lows**: Historical price points
                3. **Moving Averages**: Dynamic support levels
                4. **Trendlines**: Ascending trend support
                5. **Fibonacci Levels**: Mathematical support zones
                
                ### Psychology:
                - Traders remember prior lows
                - Buyers see value at lower prices
                - Short-sellers take profits
                - Bargain hunters accumulate
                - Fear of missing bottom
                
                ## What is Resistance?
                
                ### Definition:
                Resistance is a price level where selling pressure is expected to be strong enough to prevent the price from rising further. It acts as a "ceiling" that prevents price from advancing.
                
                ### Visual Representation:
                ```
                Price
                  ↑  _____________________  ← Resistance Level
                  |  Reject  Reject  Reject
                  |  \      /\      /
                  |   \    /  \    /
                  |    \  /    \  /
                  |     \/      \/
                  └──────────────────────→ Time
                ```
                
                ### Why Resistance Forms:
                
                1. **Psychological Levels**: Round numbers
                2. **Previous Highs**: Historical resistance
                3. **Moving Averages**: Dynamic resistance
                4. **Trendlines**: Descending trend resistance
                5. **Supply Zones**: Areas of selling interest
                
                ### Psychology:
                - Traders remember prior highs
                - Sellers emerge at higher prices
                - Profit-taking occurs
                - Trapped buyers exit breakeven
                - Fear of buying top
                
                ## Key Principles
                
                ### 1. Role Reversal:
                
                **Support Becomes Resistance**:
                ```
                Price         Prior Support → Now Resistance
                  ↑           _______________↓___
                  |              /\      Reject
                  |             /  \       |
                  |  Support   /    \      |
                  |  ________\/______\___  |
                  |  Break                 
                  └──────────────────────→ Time
                ```
                
                When support breaks, it often becomes resistance because:
                - Buyers trapped above feel relieved to exit breakeven
                - Sellers reinforce at known level
                - Psychological memory of the level
                
                **Resistance Becomes Support**:
                ```
                Price      Prior Resistance → Now Support
                  ↑           ___________
                  |          /           \___/\___
                  |  Resist /                 Hold
                  |  ______/
                  |  Breakout → New Support
                  └──────────────────────→ Time
                ```
                
                When resistance breaks, it often becomes support because:
                - Breakout buyers defend their entry
                - New buyers see value
                - Trapped short-sellers cover
                
                ### 2. Strength of Levels:
                
                **Strong Support/Resistance**:
                ✅ Multiple touches (3+)
                ✅ High volume at level
                ✅ Long time period
                ✅ Round psychological numbers
                ✅ Confluence with other indicators
                ✅ Clear reactions
                
                **Weak Support/Resistance**:
                ❌ Only 1-2 touches
                ❌ Low volume
                ❌ Short time period
                ❌ Unclear reactions
                ❌ No confluence
                
                ### 3. Zones vs. Lines:
                
                Support and Resistance are better viewed as ZONES rather than exact price lines:
                
                ```
                Resistance Zone: $100-$102
                ________________|||||||||____
                               Zone (not line)
                ```
                
                **Why Zones?**:
                - Prices rarely respect exact levels
                - Different traders have different perspectives
                - Spreads and slippage
                - Market inefficiency
                - Allows for flexibility
                
                ## Types of Support and Resistance
                
                ### 1. Horizontal Support/Resistance:
                - Based on prior swing highs/lows
                - Most common type
                - Clear visual levels
                - Easy to identify
                
                ### 2. Dynamic Support/Resistance:
                - Moving averages (20, 50, 200 SMA)
                - Changes with price
                - Adapts to trend
                - Examples: 50 EMA often acts as support in uptrends
                
                ### 3. Trendline Support/Resistance:
                - Diagonal lines connecting swing points
                - Shows trend direction
                - Can be ascending or descending
                - More advanced concept
                
                ### 4. Psychological Levels:
                - Round numbers ($50, $100, $1000)
                - Human psychology
                - Often self-fulfilling
                - Very common in forex
                
                ## Trading with Support and Resistance
                
                ### Strategy 1: Bounce Trading
                
                **At Support** (Buy):
                1. Identify strong support level
                2. Wait for price to approach support
                3. Look for reversal signals (hammer, bullish engulfing)
                4. Enter long near support
                5. Stop below support
                6. Target next resistance
                
                **At Resistance** (Sell):
                1. Identify strong resistance level
                2. Wait for price to approach resistance
                3. Look for reversal signals (shooting star, bearish engulfing)
                4. Enter short near resistance
                5. Stop above resistance
                6. Target next support
                
                ### Strategy 2: Breakout Trading
                
                **Support Breakout** (Short):
                1. Identify support level
                2. Watch for breakdown
                3. Wait for close below support
                4. Confirm with volume
                5. Enter short on retest
                6. Stop above broken support (now resistance)
                7. Target measured move or next support
                
                **Resistance Breakout** (Long):
                1. Identify resistance level
                2. Watch for breakout
                3. Wait for close above resistance
                4. Confirm with volume
                5. Enter long on pullback
                6. Stop below broken resistance (now support)
                7. Target measured move or next resistance
                
                ## Identifying Strong Levels
                
                ### Checklist for Valid Support:
                ✅ **Number of Touches**: 2-3+ bounces
                ✅ **Time Period**: Level held for weeks/months
                ✅ **Volume**: High volume reactions
                ✅ **Clarity**: Clear, obvious bounces
                ✅ **Confluence**: Aligns with MA, Fibonacci, etc.
                ✅ **Market Structure**: Makes sense in bigger picture
                
                ### Checklist for Valid Resistance:
                ✅ **Number of Touches**: 2-3+ rejections
                ✅ **Time Period**: Held for extended time
                ✅ **Volume**: High volume at rejections
                ✅ **Clarity**: Clear, obvious resistance
                ✅ **Confluence**: Multiple indicators align
                ✅ **Psychology**: Round number or significant high
                
                ## Common Mistakes
                
                ❌ Drawing too many lines (cluttered chart)
                ❌ Treating levels as exact prices vs. zones
                ❌ Ignoring volume at levels
                ❌ Not considering timeframes
                ❌ Trading every touch without confirmation
                ❌ Forgetting about role reversal
                ❌ Setting stops exactly at levels (use buffer)
                ❌ Expecting perfect bounces
                ❌ Drawing lines after the fact (hindsight bias)
                ❌ Not adapting as new levels form
                
                ## Advanced Concepts
                
                ### Confluence Zones:
                Multiple factors aligning at same price:
                - Support + 200 SMA + 61.8% Fib = Strong zone
                - Resistance + trendline + round number = Very strong
                
                ### Support/Resistance Clusters:
                - Multiple levels in close proximity
                - Creates strong zone
                - More difficult to break
                
                ### Failed Breaks (Fakeouts):
                - Price breaks level but quickly reverses
                - Traps breakout traders
                - Often leads to strong opposite move
                - Stop-hunting behavior
                
                ## Pro Tips
                
                ✅ Focus on major levels (ignore minor)
                ✅ Use higher timeframes for major levels
                ✅ Combine with price action patterns
                ✅ Volume confirms level strength
                ✅ Role reversal is powerful concept
                ✅ Zones are better than lines
                ✅ Horizontal levels more reliable than diagonal
                ✅ Old levels can remain relevant for years
                ✅ Watch for failed breakouts (traps)
                ✅ Multiple timeframe analysis essential
                ✅ Clean charts = clearer levels
                ✅ Mark major levels and revisit regularly
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What happens when a support level is broken?",
                        options = listOf("It disappears", "It often becomes resistance (role reversal)", "It becomes stronger support", "Nothing changes"),
                        correctAnswer = 1,
                        explanation = "When support is broken, it often becomes resistance through role reversal. Trapped buyers want to exit at breakeven, and sellers reinforce the level from below."
                    ),
                    QuizQuestion(
                        question = "Why are support and resistance better viewed as zones rather than exact lines?",
                        options = listOf("They look better", "Prices rarely respect exact levels due to spreads, slippage, and market inefficiency", "It's easier to draw", "Zones are always wrong"),
                        correctAnswer = 1,
                        explanation = "Support and resistance are better viewed as zones because prices rarely respect exact levels due to spreads, slippage, different trader perspectives, and market inefficiency."
                    ),
                    QuizQuestion(
                        question = "What makes a support or resistance level strong?",
                        options = listOf("It's recent", "Multiple touches, high volume, long time period, and confluence with other indicators", "It's on a Monday", "Low volume"),
                        correctAnswer = 1,
                        explanation = "Strong levels have multiple touches (3+), high volume at reactions, have held for extended time periods, and often have confluence with other technical indicators."
                    )
                )
            )
        )
