package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson07Rectangles = Lesson(
            id = 7,
            title = "Rectangle Patterns",
            category = "Continuation Patterns",
            duration = "7 min",
            content = """
                # Rectangle Patterns
                
                ## Overview
                
                Rectangle patterns (also called trading ranges or consolidation zones) are continuation patterns where price moves sideways between parallel support and resistance levels. They represent a pause in the trend before continuation.
                
                ## Structure
                
                ```
                Resistance _____________________
                          |  ↑  ↓  ↑  ↓  ↑  ↓ |
                          |  ↓  ↑  ↓  ↑  ↓  ↑ |
                          |  ↑  ↓  ↑  ↓  ↑  ↓ |
                Support   |_____________________|
                
                          ← Consolidation →
                ```
                
                ## Characteristics
                
                - **Horizontal Lines**: Clear support and resistance
                - **Sideways Movement**: Price oscillates between levels
                - **Multiple Touches**: At least 2 touches of each level
                - **Equal Highs/Lows**: Consistent price boundaries
                - **Duration**: Can last weeks to months
                
                ## Market Psychology
                
                ### Formation:
                1. **Initial Trend**: Strong directional move
                2. **Profit Taking**: Early traders lock gains
                3. **Accumulation/Distribution**: Smart money builds position
                4. **Equilibrium**: Supply meets demand
                5. **Breakout**: Trend resumes
                
                ### Buyer/Seller Dynamics:
                - **At Support**: Buyers consistently step in
                - **At Resistance**: Sellers consistently emerge
                - **Inside Range**: Traders range-trade
                - **Breakout**: One side overwhelms the other
                
                ## Trading Strategies
                
                ### Strategy 1: Range Trading
                
                **Buy Setup**:
                - Enter near support
                - Stop below support
                - Target near resistance
                
                **Sell Setup**:
                - Enter near resistance
                - Stop above resistance
                - Target near support
                
                **Pros**: Multiple opportunities
                **Cons**: Whipsaw risk, breakout losses
                
                ### Strategy 2: Breakout Trading
                
                **Long Breakout**:
                - Entry: Close above resistance
                - Stop: Below resistance (now support)
                - Target: Height of rectangle added to breakout
                
                **Short Breakout**:
                - Entry: Close below support
                - Stop: Above support (now resistance)
                - Target: Height of rectangle subtracted from breakdown
                
                **Pros**: Catch major moves
                **Cons**: False breakouts
                
                ## Volume Analysis
                
                ✅ **Ideal Volume Profile**:
                
                **During Formation**:
                - Generally declining volume
                - Shows consolidation, not distribution
                - Lower volume = healthier pattern
                
                **At Breakout**:
                - Volume surge (3x+ average)
                - Confirms genuine breakout
                - High volume = high probability
                
                **Volume Patterns**:
                - Low volume bounces = weak
                - High volume bounces = strong support/resistance
                
                ## Breakout Confirmation
                
                ✅ **Strong Breakout Signals**:
                1. **Close**: Price closes beyond level (not just wicks)
                2. **Volume**: Significant increase (2-3x normal)
                3. **Follow-Through**: Next candle continues direction
                4. **Retest**: Pullback to broken level holds
                
                ❌ **False Breakout Warnings**:
                - Low volume on break
                - Only wicks beyond level
                - Immediate reversal
                - No follow-through
                
                ## Price Targets
                
                ### Measured Move:
                1. Measure height of rectangle (resistance to support)
                2. Add to breakout point (upside) or subtract (downside)
                3. This is minimum expectation
                
                ### Extended Targets:
                - 1.5x or 2x rectangle height for strong trends
                - Previous swing highs/lows
                - Fibonacci extensions
                
                ## Rectangle Types
                
                ### Bullish Rectangle:
                - Forms during uptrend
                - Breakout typically upward
                - Continuation more likely
                
                ### Bearish Rectangle:
                - Forms during downtrend
                - Breakdown typically downward
                - Continuation expected
                
                ### Reversal Rectangle:
                - Less common
                - Forms at trend extremes
                - Can signal reversal
                
                ## Time Considerations
                
                - **Minimum**: 3 weeks
                - **Typical**: 1-3 months
                - **Maximum**: 6+ months (becomes major support/resistance)
                - **Longer = Stronger**: Extended rectangles = bigger breakout
                
                ## Common Mistakes
                
                ❌ Trading every touch (overtrading)
                ❌ Chasing price in middle of range
                ❌ Ignoring the prevailing trend
                ❌ Not waiting for breakout confirmation
                ❌ Setting stops too tight
                ❌ Entering on first touch of level
                
                ## Advanced Concepts
                
                ### Market Profile:
                - Value Area within rectangle
                - Point of Control (most volume)
                - Use for better entries
                
                ### Multiple Timeframes:
                - Rectangle on daily = strong level
                - Check weekly for bigger picture
                - Use hourly for entry timing
                
                ## Pro Tips
                
                ✅ Best rectangles have 4+ touches total
                ✅ Narrower rectangles = clearer levels
                ✅ Combine with trend direction
                ✅ Watch for volume divergence
                ✅ Be patient - wait for clear setup
                ✅ Breakout direction often matches prior trend
                ✅ False breakouts common - wait for close
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the minimum number of touches needed to confirm a rectangle pattern?",
                        options = listOf("1 on each level", "2 on each level", "5 on each level", "10 on each level"),
                        correctAnswer = 1,
                        explanation = "A valid rectangle needs at least 2 touches on both support and resistance to confirm the pattern, though more touches increase reliability."
                    ),
                    QuizQuestion(
                        question = "How do you calculate the price target after a rectangle breakout?",
                        options = listOf("10% move", "Measure rectangle height and project from breakout", "Double the width", "No target exists"),
                        correctAnswer = 1,
                        explanation = "Measure the height of the rectangle (from support to resistance) and project that same distance from the breakout point to estimate the minimum price target."
                    ),
                    QuizQuestion(
                        question = "What confirms a valid rectangle breakout?",
                        options = listOf("Any price movement beyond level", "Close beyond level with high volume and follow-through", "Time passing", "Social media buzz"),
                        correctAnswer = 1,
                        explanation = "A valid breakout requires a close beyond the level (not just a wick), high volume (2-3x normal), and follow-through in the next candle to avoid false breakouts."
                    )
                )
            )
        )
