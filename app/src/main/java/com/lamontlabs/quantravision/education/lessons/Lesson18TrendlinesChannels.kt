package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson18TrendlinesChannels = Lesson(
            id = 18,
            title = "Trendlines and Channels",
            category = "Technical Analysis Fundamentals",
            duration = "11 min",
            content = """
                # Trendlines and Channels
                
                ## Overview
                
                Trendlines and channels are essential tools for identifying trend direction, support/resistance areas, and potential reversal points. They provide visual clarity on market structure and help traders make informed entry and exit decisions.
                
                ## What is a Trendline?
                
                ### Definition:
                A trendline is a straight line connecting two or more price points that acts as support (in uptrends) or resistance (in downtrends). It helps identify trend direction and potential reversal points.
                
                ## Uptrend Line
                
                ### Structure:
                ```
                Price
                  ↑           /•
                  |         /  
                  |       /•   
                  |     /      
                  |   /•       
                  | /_________ Uptrend Line (Support)
                  └──────────────────────→ Time
                  
                  Connects ascending lows
                ```
                
                ### Characteristics:
                - **Direction**: Slopes upward
                - **Connection**: Links swing lows
                - **Function**: Acts as support
                - **Touches**: Minimum 2, ideally 3+
                - **Breaks**: Signal potential trend reversal
                
                ### How to Draw:
                1. Identify clear uptrend
                2. Find two significant swing lows
                3. Connect them with straight line
                4. Extend line forward
                5. Validate with additional touches
                
                ## Downtrend Line
                
                ### Structure:
                ```
                Price __________ Downtrend Line (Resistance)
                  ↑   \      
                  |    \•     
                  |     \     
                  |      \•   
                  |       \   
                  |        \• 
                  └──────────────────────→ Time
                  
                  Connects descending highs
                ```
                
                ### Characteristics:
                - **Direction**: Slopes downward
                - **Connection**: Links swing highs
                - **Function**: Acts as resistance
                - **Touches**: Minimum 2, ideally 3+
                - **Breaks**: Signal potential trend reversal
                
                ### How to Draw:
                1. Identify clear downtrend
                2. Find two significant swing highs
                3. Connect them with straight line
                4. Extend line forward
                5. Validate with additional touches
                
                ## Trend Channels
                
                ### Definition:
                A channel consists of two parallel trendlines that contain price action, creating a "price corridor."
                
                ## Ascending Channel (Bullish)
                
                ### Structure:
                ```
                Upper Trendline (Resistance)
                 _________________________
                   /  •  /  •  /  •  /
                  /     /     /     /
                 /  •  /  •  /  •  /
                /_____/_____/_____/______
                Lower Trendline (Support)
                
                ← Rising Parallel Lines →
                ```
                
                ### Characteristics:
                - **Two Lines**: Both slope upward
                - **Parallel**: Equal distance apart
                - **Lower Line**: Support (connects lows)
                - **Upper Line**: Resistance (connects highs)
                - **Trend**: Bullish continuation
                
                ### Trading Strategy:
                - **Buy**: Near lower trendline (support)
                - **Sell**: Near upper trendline (resistance)
                - **Stop**: Below lower trendline
                - **Breakout**: Above upper line = acceleration
                - **Breakdown**: Below lower line = reversal
                
                ## Descending Channel (Bearish)
                
                ### Structure:
                ```
                Upper Trendline (Resistance)
                \_________________________
                 \  •  \  •  \  •  \
                  \     \     \     \
                   \  •  \  •  \  •  \
                    \_____\_____\_____\___
                    Lower Trendline (Support)
                
                ← Falling Parallel Lines →
                ```
                
                ### Characteristics:
                - **Two Lines**: Both slope downward
                - **Parallel**: Equal distance apart
                - **Upper Line**: Resistance (connects highs)
                - **Lower Line**: Support (connects lows)
                - **Trend**: Bearish continuation
                
                ### Trading Strategy:
                - **Sell**: Near upper trendline (resistance)
                - **Cover**: Near lower trendline (support)
                - **Stop**: Above upper trendline
                - **Breakdown**: Below lower line = acceleration
                - **Breakout**: Above upper line = reversal
                
                ## Horizontal Channel
                
                ### Structure:
                ```
                Resistance _______________
                          |  ↑↓  ↑↓  ↑↓ |
                          |            |
                Support   |____________|
                
                ← Range-bound Market →
                ```
                
                ### Characteristics:
                - **Two Lines**: Both horizontal
                - **Parallel**: Equal height
                - **Sideways**: No trending direction
                - **Range**: Defined boundaries
                
                ### Trading Strategy:
                - **Buy**: Near support, sell near resistance
                - **Range Trading**: Multiple opportunities
                - **Breakout**: Exit range = new trend
                
                ## Trendline Rules and Best Practices
                
                ### Rule 1: Minimum Touches
                ✅ **Valid Trendline**:
                - At least 2 touches to draw
                - 3+ touches for confirmation
                - More touches = stronger line
                
                ### Rule 2: Don't Force Fits
                ❌ **Invalid Trendlines**:
                - Forcing line through random points
                - Ignoring significant touches
                - Drawing lines with only 1 touch
                - Using minor/insignificant swings
                
                ### Rule 3: Angle Matters
                - **Steep angles** (>45°): Often break soon, unsustainable
                - **Moderate angles** (30-45°): Most reliable, sustainable
                - **Shallow angles** (<30°): Weak trend, consolidation
                
                ### Rule 4: Timeframe Relevance
                - **Higher timeframes**: More significant lines
                - **Daily/Weekly**: Major trendlines
                - **Hourly/4H**: Short-term lines
                - **Multiple timeframes**: Best approach
                
                ## Trading Trendline Breaks
                
                ### Uptrend Line Break (Bearish):
                
                **Setup**:
                1. Price touching uptrend line (support)
                2. Break below with conviction
                3. Close below line
                
                **Entry**:
                - **Conservative**: Wait for retest of broken line (now resistance)
                - **Aggressive**: Enter on break
                
                **Stop Loss**:
                - Above broken trendline
                - Or above recent swing high
                
                **Target**:
                - Next support level
                - Measured move
                
                ### Downtrend Line Break (Bullish):
                
                **Setup**:
                1. Price touching downtrend line (resistance)
                2. Break above with conviction
                3. Close above line
                
                **Entry**:
                - **Conservative**: Wait for retest of broken line (now support)
                - **Aggressive**: Enter on break
                
                **Stop Loss**:
                - Below broken trendline
                - Or below recent swing low
                
                **Target**:
                - Next resistance level
                - Measured move
                
                ## Channel Trading Strategies
                
                ### Strategy 1: Channel Bounce Trading
                
                **In Ascending Channel**:
                - Buy near lower line
                - Sell near upper line
                - Stop below channel
                - Repeat until breakout
                
                **In Descending Channel**:
                - Sell near upper line
                - Cover near lower line
                - Stop above channel
                - Repeat until breakdown
                
                ### Strategy 2: Channel Breakout Trading
                
                **Bullish Breakout** (Ascending Channel):
                - Watch for break above upper line
                - Confirms acceleration
                - Enter long on breakout
                - Target measured move (channel width added)
                
                **Bearish Breakdown** (Descending Channel):
                - Watch for break below lower line
                - Confirms acceleration
                - Enter short on breakdown
                - Target measured move (channel width subtracted)
                
                ## Advanced Trendline Concepts
                
                ### Internal Trendlines:
                - Lines within larger trendlines
                - Show trend momentum
                - Breaks signal acceleration/deceleration
                
                ### Fan Principle:
                - Multiple trendlines from same point
                - Each break leads to next line
                - Third break often signals reversal
                
                ### Speed Lines:
                - Trendlines at 1/3 and 2/3 retracement
                - Advanced technique
                - Shows trend strength
                
                ## Common Mistakes
                
                ❌ Drawing lines through price bodies (use wicks)
                ❌ Forcing lines that don't exist
                ❌ Using only 2 touches (need 3+ for validation)
                ❌ Ignoring failed trendlines
                ❌ Drawing too many lines (clutter)
                ❌ Not adjusting as new data appears
                ❌ Using exact touches (allow small penetrations)
                ❌ Mixing timeframes incorrectly
                
                ## Pro Tips
                
                ✅ Use logarithmic scale for long-term lines
                ✅ Allow minor penetrations (not every touch perfect)
                ✅ Redraw lines as new swings form
                ✅ Focus on major swings, ignore noise
                ✅ Combine with volume (breaks need volume)
                ✅ Higher timeframe lines more significant
                ✅ 3+ touches = very strong line
                ✅ Steeper lines break sooner
                ✅ Parallel channels most reliable
                ✅ Use channels for range-bound trading
                ✅ Breakout/breakdown needs confirmation
                ✅ Clean charts = clearer lines
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the minimum number of touches needed to draw a valid trendline?",
                        options = listOf("1 touch", "2 touches, 3+ for confirmation", "5 touches", "10 touches"),
                        correctAnswer = 1,
                        explanation = "You need at least 2 touches to draw a trendline, but 3 or more touches provide better confirmation and validation of the trend's strength and reliability."
                    ),
                    QuizQuestion(
                        question = "In an ascending channel, where should you buy and where should you sell?",
                        options = listOf("Buy upper, sell lower", "Buy lower trendline (support), sell upper trendline (resistance)", "Buy randomly", "Never trade channels"),
                        correctAnswer = 1,
                        explanation = "In an ascending channel, buy near the lower trendline (support) and sell/take profits near the upper trendline (resistance), trading the range within the channel."
                    ),
                    QuizQuestion(
                        question = "What angle makes a trendline most reliable and sustainable?",
                        options = listOf("Vertical (90°)", "Moderate angle (30-45°)", "Horizontal (0°)", "Any angle"),
                        correctAnswer = 1,
                        explanation = "Moderate angles of 30-45° are most reliable and sustainable. Steeper angles (>45°) often break quickly, while shallow angles (<30°) indicate weak trends."
                    )
                )
            )
        )
