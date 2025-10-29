package com.lamontlabs.quantravision.education

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Complete 25-lesson interactive education course
 * Comprehensive pattern trading curriculum from fundamentals to advanced strategies
 */
object EducationCourse {

    data class Lesson(
        val id: Int,
        val title: String,
        val description: String,
        val content: String,
        val keyPoints: List<String>,
        val examples: List<PatternExample>,
        val quiz: Quiz
    )

    data class PatternExample(
        val patternName: String,
        val description: String,
        val identificationTips: List<String>
    )

    data class Quiz(
        val questions: List<Question>
    )

    data class Question(
        val question: String,
        val options: List<String>,
        val correctAnswerIndex: Int,
        val explanation: String
    )

    data class LessonProgress(
        val lessonId: Int,
        val completed: Boolean,
        val quizScore: Int,
        val bonusHighlightsEarned: Int
    )

    data class Certificate(
        val userName: String,
        val completionDate: Long,
        val averageScore: Int,
        val totalLessons: Int
    )

    /**
     * All 25 lessons - Complete course
     */
    fun getAllLessons(): List<Lesson> {
        return listOf(
            getLesson1(),
            getLesson2(),
            getLesson3(),
            getLesson4(),
            getLesson5(),
            getLesson6(),
            getLesson7(),
            getLesson8(),
            getLesson9(),
            getLesson10(),
            getLesson11(),
            getLesson12(),
            getLesson13(),
            getLesson14(),
            getLesson15(),
            getLesson16(),
            getLesson17(),
            getLesson18(),
            getLesson19(),
            getLesson20(),
            getLesson21(),
            getLesson22(),
            getLesson23(),
            getLesson24(),
            getLesson25()
        )
    }

    /**
     * Lesson 1: Introduction to Chart Patterns
     */
    private fun getLesson1(): Lesson {
        return Lesson(
            id = 1,
            title = "Introduction to Chart Patterns",
            description = "Learn what chart patterns are and why they matter in trading",
            content = """
                Chart patterns are recognizable formations on price charts that traders use to predict future price movements with higher probability than random guessing. These visual formations represent the battle between buyers and sellers, creating predictable geometric shapes that repeat throughout market history.
                
                **Why Patterns Work:**
                - They represent collective psychology of market participants - fear, greed, hope, and panic manifest in price action
                - Historical repetition creates predictable outcomes - human behavior doesn't change, so patterns repeat
                - They provide visual context for price action - easier to spot opportunities and risk zones
                - Markets move in trends and consolidations - patterns help identify which phase you're in
                - Institutional traders use the same patterns - creating self-fulfilling prophecies when many traders act on the same signals
                
                **Pattern Categories:**
                1. **Continuation Patterns** - Suggest the current trend will continue after brief consolidation (flags, pennants, rectangles)
                2. **Reversal Patterns** - Indicate potential trend change from up to down or vice versa (head and shoulders, double tops/bottoms)
                3. **Bilateral Patterns** - Can break in either direction depending on market pressure (symmetrical triangles, rectangles)
                
                **Key Concepts You Must Understand:**
                - Support: Price level where buying pressure prevents further decline - acts as a floor
                - Resistance: Price level where selling pressure prevents further rise - acts as a ceiling
                - Breakout: Price moves decisively beyond established support/resistance with increased volume
                - Volume: Confirms pattern validity (higher volume on breakout = stronger, more reliable signal)
                - Timeframe: Same pattern has different implications on 5-minute vs daily chart
                
                **Practical Application:**
                Patterns work best when combined with volume analysis, support/resistance levels, and trend direction. Never trade a pattern in isolation - always confirm with multiple factors. The most reliable patterns occur at key support or resistance levels with increasing volume.
                
                **Common Beginner Mistakes:**
                - Trading before breakout confirmation (getting caught in false moves)
                - Ignoring volume (low volume breakouts often fail)
                - Not using stop losses (patterns can fail - protect your capital)
                - Forcing patterns that aren't there (seeing what you want to see)
                
                **Recognition Tips:**
                Start by identifying major support and resistance levels on your chart. Then look for price consolidating between these levels. Patterns take time to form - be patient. The cleaner and more textbook the pattern, the more reliable it tends to be.
            """.trimIndent(),
            keyPoints = listOf(
                "Patterns reflect market psychology",
                "Continuation vs Reversal patterns",
                "Support and resistance are critical",
                "Volume confirms pattern validity",
                "Timeframes matter - same pattern, different implications"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Head and Shoulders",
                    description = "Classic reversal pattern with three peaks",
                    identificationTips = listOf(
                        "Middle peak (head) higher than shoulders",
                        "Neckline connects troughs",
                        "Volume decreases on head formation",
                        "Breakout below neckline confirms reversal"
                    )
                ),
                PatternExample(
                    patternName = "Bull Flag",
                    description = "Continuation pattern showing brief consolidation in uptrend",
                    identificationTips = listOf(
                        "Sharp upward move forms flagpole",
                        "Brief downward consolidation forms flag",
                        "Flag slopes against trend direction",
                        "Volume decreases during flag formation",
                        "Breakout above flag resistance continues uptrend",
                        "Volume spike confirms breakout validity"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What do chart patterns primarily represent?",
                        options = listOf(
                            "Random price movements",
                            "Collective market psychology",
                            "Computer algorithms",
                            "Government manipulation"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Chart patterns represent the collective psychology and behavior of all market participants."
                    ),
                    Question(
                        question = "Which pattern type suggests the current trend will continue?",
                        options = listOf(
                            "Reversal patterns",
                            "Bilateral patterns",
                            "Continuation patterns",
                            "Harmonic patterns"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Continuation patterns like flags and pennants suggest the existing trend will resume after consolidation."
                    ),
                    Question(
                        question = "What confirms the validity of a chart pattern?",
                        options = listOf(
                            "Pattern size",
                            "Trading volume",
                            "Color of candles",
                            "Day of the week"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Increased trading volume during pattern formation and breakout confirms its validity and strength."
                    ),
                    Question(
                        question = "What is a breakout in chart patterns?",
                        options = listOf(
                            "A pattern that doesn't work",
                            "Price moving beyond established support or resistance",
                            "A gap in the chart",
                            "When volume decreases significantly"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "A breakout occurs when price moves beyond established support or resistance levels, often signaling a new trend."
                    )
                )
            )
        )
    }

    /**
     * Lesson 2: Candlestick Patterns Basics
     */
    private fun getLesson2(): Lesson {
        return Lesson(
            id = 2,
            title = "Candlestick Patterns Basics",
            description = "Master fundamental single and multi-candle patterns",
            content = """
                Candlestick patterns are the foundation of technical analysis, originating from 18th century Japanese rice traders who used them to predict rice prices. These patterns provide crucial insight into market sentiment and potential reversals or continuations.
                
                **Anatomy of a Candlestick:**
                - Body: Range between open and close price - shows the main battle
                - Wick/Shadow: High and low extremes - shows rejected prices
                - Color: Green/white (bullish - close above open) / Red/black (bearish - close below open)
                - Size: Larger bodies show stronger conviction, small bodies show indecision
                
                **Key Single-Candle Patterns:**
                
                **1. Doji** - Indecision Signal
                - Open equals close (tiny or no body)
                - Long wicks indicate price rejection at both ends
                - Signals potential reversal when appearing at trend extremes
                - In uptrend: warns of exhaustion
                - In downtrend: hints at potential bottom
                - Requires confirmation from next candle
                
                **2. Hammer** - Bullish Reversal
                - Small body at the top of candle
                - Long lower wick (at least 2x the body size)
                - Appears after significant downtrend
                - Shows sellers pushed price low but buyers regained control
                - Confirmation: Next candle closes higher
                
                **3. Shooting Star** - Bearish Reversal
                - Small body at bottom of candle
                - Long upper wick showing rejection of higher prices
                - Appears after uptrend
                - Indicates buyers pushed high but sellers took control
                - Mirror opposite of hammer
                
                **4. Marubozu** - Strong Momentum
                - No wicks or very small wicks
                - Large body showing strong directional move
                - Bullish marubozu: opens at low, closes at high
                - Bearish marubozu: opens at high, closes at low
                - Shows dominant buyers or sellers
                
                **Multi-Candle Patterns:**
                
                **5. Engulfing** - Powerful Reversal
                - Second candle's body completely engulfs first candle's body
                - Bullish engulfing: Small red then large green (after downtrend)
                - Bearish engulfing: Small green then large red (after uptrend)
                - Larger the engulfing candle, stronger the signal
                - Higher volume on engulfing candle confirms validity
                
                **6. Morning/Evening Star** - Major Reversal
                - Three-candle formation signaling trend reversal
                - Morning star (bullish): Down candle, small body (gap down), up candle (gap up)
                - Evening star (bearish): Up candle, small body (gap up), down candle (gap down)
                - Middle candle shows indecision, third candle confirms new direction
                - One of the most reliable reversal patterns
                
                **Trading Tips:**
                Look for candlestick patterns at key support/resistance levels for best reliability. Always confirm patterns with volume - higher volume increases probability of success. Wait for confirmation candle before entering trades. Combine candlesticks with chart patterns for highest probability setups.
            """.trimIndent(),
            keyPoints = listOf(
                "Body shows open-to-close range",
                "Wicks show rejection of prices",
                "Doji signals indecision",
                "Engulfing patterns very reliable",
                "Context matters - where pattern forms"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Bullish Engulfing",
                    description = "Strong reversal after downtrend",
                    identificationTips = listOf(
                        "Occurs at support or after downtrend",
                        "Green candle fully covers red candle",
                        "Larger the engulfing, stronger the signal",
                        "Confirm with volume increase"
                    )
                ),
                PatternExample(
                    patternName = "Doji",
                    description = "Indecision candle with equal open/close",
                    identificationTips = listOf(
                        "Body is very small or nonexistent",
                        "At trend extremes = potential reversal",
                        "In consolidation = continued indecision",
                        "Needs confirmation from next candle"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What does a Doji candlestick indicate?",
                        options = listOf(
                            "Strong bullish momentum",
                            "Market indecision",
                            "Definite reversal",
                            "High volatility only"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "A Doji shows market indecision where buyers and sellers are balanced (open equals close)."
                    ),
                    Question(
                        question = "Where should a Hammer pattern appear?",
                        options = listOf(
                            "At the top of an uptrend",
                            "After a downtrend",
                            "In the middle of consolidation",
                            "Only on daily timeframes"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "A Hammer is a bullish reversal pattern that appears after a downtrend, signaling potential bottom."
                    ),
                    Question(
                        question = "In a Bullish Engulfing pattern, what happens?",
                        options = listOf(
                            "A red candle engulfs a green candle",
                            "A green candle completely covers a red candle",
                            "Two candles have the same size",
                            "Three candles form a triangle"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "A Bullish Engulfing has a green candle that completely engulfs the previous red candle, showing strong buying."
                    ),
                    Question(
                        question = "What part of a candlestick shows the high and low extremes?",
                        options = listOf(
                            "The body",
                            "The wick or shadow",
                            "The color",
                            "The opening price"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "The wick (or shadow) shows the high and low extremes of the trading period, indicating price rejection levels."
                    )
                )
            )
        )
    }

    /**
     * Lesson 3: Trend Patterns & Reversals
     */
    private fun getLesson3(): Lesson {
        return Lesson(
            id = 3,
            title = "Trend Patterns & Reversals",
            description = "Identify major trend continuation and reversal formations",
            content = """
                Understanding trend patterns is critical for successful trading. These patterns tell you when to stay in a trend and when to prepare for a reversal. The ability to distinguish between continuation and reversal patterns can dramatically improve your win rate.
                
                **Reversal Patterns - Trend Change Signals:**
                
                **1. Head and Shoulders** (Bearish Reversal)
                - Three peaks: left shoulder, head (highest peak), right shoulder (similar height to left)
                - Neckline connects the two troughs between peaks
                - Price target: Measure distance from head to neckline, project that distance down from breakdown point
                - Volume pattern: Decreases as head forms (weakening), increases sharply on neckline breakdown
                - Confirms when price closes below neckline
                - One of the most reliable bearish reversal patterns
                
                **2. Inverse Head and Shoulders** (Bullish Reversal)
                - Mirror image of standard H&S pattern
                - Three troughs: left shoulder, head (lowest point), right shoulder
                - Neckline connects peaks between troughs
                - Breakout above neckline with volume confirms bullish reversal
                - Target: Head to neckline distance projected upward
                - Forms at market bottoms after downtrends
                
                **3. Double Top/Bottom** - Failed Breakout Reversal
                - Two attempts to break resistance (top) or support (bottom) both fail
                - Double top forms "M" shape, double bottom forms "W" shape
                - Second peak/trough at approximately same level as first (within 3-5%)
                - Strong reversal signal when second attempt fails
                - Confirms when price breaks support (double top) or resistance (double bottom)
                - Time between peaks: typically weeks to months
                
                **4. Triple Top/Bottom** - Even Stronger Reversal
                - Three failed attempts to break a level
                - Rare but extremely reliable when formed
                - Shows exhaustion of trend - buyers/sellers completely spent
                
                **Continuation Patterns - Trend Resumes:**
                
                **5. Flags** - Brief pause in strong trend
                - Rectangular consolidation pattern sloping against trend direction
                - Forms after sharp, impulsive move (the flagpole)
                - Bull flag: slopes downward in uptrend
                - Bear flag: slopes upward in downtrend
                - Duration: Usually 1-3 weeks
                - Breakout continues original trend direction
                - Volume decreases during flag, surges on breakout
                
                **6. Pennants** - Similar to flags but triangular
                - Converging trendlines forming small symmetrical triangle
                - Forms very quickly (typically 1-3 weeks)
                - Follows sharp directional move
                - High probability trend continuation
                - Breakout usually in direction of prior trend
                
                **Trading Application:**
                Always trade continuation patterns in the direction of the prevailing trend. For reversal patterns, wait for confirmed breakout with volume before entering. Set stop losses just beyond pattern boundaries. The larger the pattern (in time and price), the more significant and reliable the signal.
            """.trimIndent(),
            keyPoints = listOf(
                "Reversal patterns indicate trend change",
                "Head and shoulders very reliable",
                "Double tops/bottoms need confirmation",
                "Flags and pennants show trend continuation",
                "Measure target from pattern height"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Head and Shoulders",
                    description = "Most reliable bearish reversal pattern",
                    identificationTips = listOf(
                        "Head must be highest peak",
                        "Shoulders approximately equal height",
                        "Neckline may slope up or down",
                        "Wait for neckline break confirmation",
                        "Volume crucial for confirmation"
                    )
                ),
                PatternExample(
                    patternName = "Bull Flag",
                    description = "Bullish continuation after strong rally",
                    identificationTips = listOf(
                        "Sharp upward move (flagpole)",
                        "Brief downward consolidation (flag)",
                        "Parallel channel slope against trend",
                        "Breakout above flag continues rally",
                        "Volume decreases during flag, spikes on breakout"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "In a Head and Shoulders pattern, which peak is the highest?",
                        options = listOf(
                            "Left shoulder",
                            "Right shoulder",
                            "The head",
                            "All are equal"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "The head is the highest peak in a Head and Shoulders pattern, flanked by lower shoulders."
                    ),
                    Question(
                        question = "What does a Bull Flag pattern indicate?",
                        options = listOf(
                            "Trend reversal to downtrend",
                            "Market indecision",
                            "Bullish trend continuation",
                            "End of all trading"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "A Bull Flag is a continuation pattern indicating the uptrend will resume after brief consolidation."
                    ),
                    Question(
                        question = "How do you calculate the target for a Head and Shoulders?",
                        options = listOf(
                            "Distance from neckline to head, projected downward",
                            "Double the shoulder height",
                            "50% of the pattern width",
                            "There is no target calculation"
                        ),
                        correctAnswerIndex = 0,
                        explanation = "Measure the distance from neckline to head peak, then project that distance down from the breakout point."
                    ),
                    Question(
                        question = "What is the difference between continuation and reversal patterns?",
                        options = listOf(
                            "There is no difference",
                            "Continuation patterns suggest trend will resume, reversal patterns signal trend change",
                            "Continuation patterns are always triangles",
                            "Reversal patterns only work on daily charts"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Continuation patterns like flags suggest the trend will continue after consolidation, while reversal patterns like Head and Shoulders indicate a trend change."
                    )
                )
            )
        )
    }

    /**
     * Lesson 4: Triangle Patterns
     */
    private fun getLesson4(): Lesson {
        return Lesson(
            id = 4,
            title = "Triangle Patterns",
            description = "Master ascending, descending, and symmetrical triangles",
            content = """
                Triangle patterns are among the most common continuation patterns, forming as price consolidates before the next significant move. They represent a battle between buyers and sellers that eventually results in a decisive breakout.
                
                **Types of Triangles and Their Implications:**
                
                **1. Ascending Triangle** (Typically Bullish)
                - Flat horizontal resistance at the top
                - Rising bottom support showing progressively higher lows
                - Typically breaks upward (70% probability in uptrend)
                - Shows buyers becoming increasingly aggressive with each test
                - Each low is higher than the previous - buyers stepping in earlier
                - Forms most often as continuation pattern in uptrends
                - Requires minimum of 2 touches on resistance, 2 rising lows
                
                **2. Descending Triangle** (Typically Bearish)
                - Flat horizontal support at the bottom
                - Declining top resistance creating lower highs
                - Usually breaks downward (70% probability in downtrend)
                - Shows sellers becoming more aggressive
                - Each high is lower - sellers more willing to sell at lower prices
                - Forms as continuation in downtrends, occasionally at tops
                - Breakdown often sharp and swift
                
                **3. Symmetrical Triangle** (Bilateral/Neutral)
                - Converging trendlines from both sides
                - Lower highs AND higher lows simultaneously
                - Can break in either direction
                - Direction typically continues the prior trend (65% probability)
                - Represents equilibrium - neither bulls nor bears in control
                - Breakout usually occurs 2/3 to 3/4 through the pattern
                - Most common as mid-trend consolidation
                
                **Trading Triangle Patterns Successfully:**
                - Wait for confirmed breakout - price must close beyond trendline, not just wick through
                - Volume should increase 50-100% on breakout - validates the move
                - Measure target: Take height of triangle at widest point, project from breakout
                - False breakouts are common - wait for daily/4H close beyond the line
                - Best entry: Initial breakout OR retest of broken trendline
                - Stop loss: Place just inside triangle on opposite side
                - Pattern typically takes 1-3 months to form (can be shorter on lower timeframes)
                
                **Common Mistakes to Avoid:**
                - Trading before confirmed breakout (getting whipsawed inside the triangle)
                - Ignoring volume on breakout (weak volume breakouts frequently fail and reverse)
                - Trading against the prior trend (always favor breakout direction of existing trend)
                - Entering too late - after triangle has already broken and extended too far
                - Not using stop losses (triangles can fail - protect your capital)
                
                **Recognition Tips:**
                Look for at least 4 touchpoints total (2 on each side). The more touches, the more valid the pattern. Volume should decrease as the triangle forms, then spike on breakout.
            """.trimIndent(),
            keyPoints = listOf(
                "Ascending triangles are bullish",
                "Descending triangles are bearish",
                "Symmetrical can go either way",
                "Volume confirms breakout",
                "Measure target from widest point"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Ascending Triangle",
                    description = "Bullish pattern with flat resistance",
                    identificationTips = listOf(
                        "At least two touches on flat top",
                        "Rising lows show buying pressure",
                        "Forms after uptrend (continuation)",
                        "Breakout above resistance with volume",
                        "Can take 1-3 months to form"
                    )
                ),
                PatternExample(
                    patternName = "Symmetrical Triangle",
                    description = "Neutral consolidation pattern",
                    identificationTips = listOf(
                        "Lower highs and higher lows",
                        "Converging trendlines",
                        "Usually continues prior trend",
                        "Breakout typically 2/3 into pattern",
                        "Decreasing volume until breakout"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What characterizes an Ascending Triangle?",
                        options = listOf(
                            "Flat bottom, declining top",
                            "Flat top, rising bottom",
                            "Both lines rising",
                            "Both lines descending"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Ascending Triangles have a flat top resistance and rising bottom support, showing increasing buying pressure."
                    ),
                    Question(
                        question = "Which triangle pattern is considered bilateral?",
                        options = listOf(
                            "Ascending Triangle",
                            "Descending Triangle",
                            "Symmetrical Triangle",
                            "Right Triangle"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Symmetrical Triangles are bilateral patterns that can break in either direction."
                    ),
                    Question(
                        question = "Where is the breakout most likely to occur in a triangle?",
                        options = listOf(
                            "At the very start",
                            "About 2/3 through the pattern",
                            "Only at the apex",
                            "Randomly anywhere"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Breakouts typically occur about 2/3 of the way through the triangle pattern, well before the apex."
                    ),
                    Question(
                        question = "How do you calculate the price target for a triangle pattern?",
                        options = listOf(
                            "Measure the pattern width at any point",
                            "Measure the height at the widest point and project from breakout",
                            "Double the triangle size",
                            "Triangles have no measurable targets"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Measure the height of the triangle at its widest point, then project that distance from the breakout point to find the target."
                    )
                )
            )
        )
    }

    /**
     * Lesson 5: Wedge Patterns
     */
    private fun getLesson5(): Lesson {
        return Lesson(
            id = 5,
            title = "Wedge Patterns",
            description = "Learn rising and falling wedges for reversal signals",
            content = """
                Wedge patterns are powerful reversal indicators that often catch traders by surprise. Unlike triangles, wedges signal trend exhaustion rather than continuation, making them excellent early warning signals for major reversals.
                
                **Rising Wedge** (Bearish Reversal Pattern)
                - Both support and resistance trendlines slope upward in same direction
                - Progressively narrowing price action between the lines
                - Typically appears at the top of extended uptrend
                - Signals buyers are losing steam - each new high requires more effort
                - Volume consistently decreases as pattern develops
                - Breaks downward through support line (opposite to the slope)
                - The steeper the wedge, the more imminent the breakdown
                - Often forms over several weeks to months
                
                **Falling Wedge** (Bullish Reversal Pattern)
                - Both support and resistance lines slope downward together
                - Price consolidates in narrowing descending range
                - Forms most often at bottom of significant downtrend
                - Shows sellers becoming exhausted - each new low weaker than last
                - Volume contracts throughout pattern formation
                - Breaks upward through resistance (opposite to slope direction)
                - Confirms when price closes above resistance with volume expansion
                - Common at major market bottoms
                
                **Key Differences from Triangle Patterns:**
                - Wedges are reversal patterns (against the trend), triangles usually continuation
                - Both trendlines slope in the SAME direction (wedges) vs converging from opposite directions (triangles)
                - Wedges show trend exhaustion, triangles show consolidation
                - Wedges are typically steeper and narrower than triangles
                - Wedges break opposite to their slope, triangles can break either way
                
                **Trading Wedge Patterns Successfully:**
                - Entry: Enter on confirmed breakout opposite to wedge slope direction
                - Wait for candle close beyond trendline - don't chase wicks
                - Stop loss: Place just beyond the opposite trendline
                - Target: Beginning of wedge formation (measure full pattern height)
                - Confirmation: Look for volume spike of 50%+ on breakout
                - Retest entry: Wedge often retests broken trendline - provides second entry opportunity
                
                **Pattern Psychology:**
                - Rising wedge: Bulls make higher highs but with diminishing momentum - unsustainable
                - Each rally is weaker, showing buying pressure fading
                - Falling wedge: Bears push lower but with less conviction each time
                - Sellers running out of ammunition as lows become shallow
                - Narrowing range shows decreasing conviction and impending resolution
                
                **Common Mistakes:**
                - Entering before breakout confirmation
                - Confusing wedges with flags (flags are rectangular, wedges are triangular)
                - Missing the retest entry opportunity
                - Not respecting the volume requirement
            """.trimIndent(),
            keyPoints = listOf(
                "Rising wedges are bearish reversals",
                "Falling wedges are bullish reversals",
                "Both trendlines slope same direction",
                "Volume decreases as wedge forms",
                "Breakout opposite to slope direction"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Rising Wedge",
                    description = "Bearish reversal at trend top",
                    identificationTips = listOf(
                        "Both lines slope upward",
                        "Price makes higher highs but weakly",
                        "Volume diminishes throughout",
                        "Breaks downward through support",
                        "Often preceded by strong rally"
                    )
                ),
                PatternExample(
                    patternName = "Falling Wedge",
                    description = "Bullish reversal at trend bottom",
                    identificationTips = listOf(
                        "Both lines slope downward",
                        "Lower lows but with less conviction",
                        "Contracting volatility",
                        "Breaks upward through resistance",
                        "Forms after significant decline"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "A Rising Wedge is typically a:",
                        options = listOf(
                            "Bullish continuation",
                            "Bearish reversal",
                            "Neutral pattern",
                            "Bullish reversal"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Rising Wedges are bearish reversal patterns that signal upward momentum is exhausted."
                    ),
                    Question(
                        question = "In which direction do both trendlines slope in a Falling Wedge?",
                        options = listOf(
                            "Upward",
                            "Downward",
                            "One up, one down",
                            "Horizontal"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "In a Falling Wedge, both the support and resistance lines slope downward, converging."
                    ),
                    Question(
                        question = "What happens to volume as a wedge pattern forms?",
                        options = listOf(
                            "Steadily increases",
                            "Stays constant",
                            "Decreases",
                            "Becomes extremely volatile"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Volume typically decreases as a wedge forms, showing diminishing conviction before the reversal."
                    ),
                    Question(
                        question = "What is the key difference between wedges and triangles?",
                        options = listOf(
                            "Wedges are continuation, triangles are reversal",
                            "Wedges have both lines sloping the same direction, signaling reversal",
                            "There is no difference",
                            "Wedges only form on hourly charts"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Wedges have both trendlines sloping in the same direction and signal reversal, while triangles have converging lines and often continue the trend."
                    )
                )
            )
        )
    }

    /**
     * Lesson 6: Harmonic Patterns Fundamentals
     */
    private fun getLesson6(): Lesson {
        return Lesson(
            id = 6,
            title = "Harmonic Patterns Fundamentals",
            description = "Introduction to Fibonacci-based harmonic patterns",
            content = """
                Harmonic patterns use precise Fibonacci ratios to identify exact reversal points with mathematical precision. They're among the most accurate trading patterns but require rigorous measurements and patience. When executed correctly, harmonics offer exceptional risk-to-reward ratios.
                
                **What Are Harmonic Patterns?**
                - Based on natural Fibonacci ratios found throughout nature and markets (0.382, 0.618, 1.618, etc.)
                - Geometric price structures that form specific shapes
                - Predict Potential Reversal Zones (PRZ) where price should reverse
                - Require exact ratio relationships - patterns must meet strict criteria
                - Higher accuracy than traditional chart patterns when properly identified
                - All follow 5-point structure labeled X-A-B-C-D
                
                **Critical Fibonacci Ratios in Harmonics:**
                - 0.382 (38.2% retracement) - shallow pullback
                - 0.618 (61.8% retracement) - Golden Ratio, most important
                - 0.786 (78.6% retracement) - deep retracement
                - 1.27 (127% extension) - moderate projection
                - 1.618 (161.8% extension) - Golden Ratio extension
                - 2.24 and 2.618 - extreme extensions for Butterfly/Crab
                
                **The Major Harmonic Patterns:**
                
                **1. Gartley Pattern (Most Common)**
                - The original harmonic pattern from 1935
                - 5-point structure: X-A-B-C-D
                - AB leg = 61.8% retracement of XA
                - BC leg = 38.2% to 88.6% retracement of AB
                - CD leg = 127% to 161.8% extension of BC
                - Point D completes at 78.6% retracement of XA
                - D point is the PRZ (Potential Reversal Zone) - your entry
                
                **2. Bat Pattern**
                - Similar to Gartley but tighter ratios
                - AB = 38.2% to 50% of XA (shallower than Gartley)
                - CD completes at 88.6% of XA (deeper than Gartley)
                - Tighter retracement means closer stop loss
                - Higher risk-reward ratio than Gartley
                
                **3. Butterfly Pattern**
                - Extended harmonic structure
                - AB = 78.6% of XA
                - CD = 127% to 261.8% extension of BC
                - Point D extends BEYOND X point (key difference)
                - Signals extreme price exhaustion
                
                **Trading Harmonic Patterns:**
                - Must wait for complete pattern formation at Point D
                - Enter long/short at Point D PRZ with limit order
                - Place stop loss just beyond X point
                - First target: Point C, Second target: Point A
                - Always combine with candlestick reversal confirmation at D
                - Use Fibonacci drawing tools for precise measurements
                
                **Why Harmonics Work:**
                Mathematical precision creates high-probability setups. When multiple Fibonacci ratios converge at Point D, it creates powerful support/resistance where reversals naturally occur.
            """.trimIndent(),
            keyPoints = listOf(
                "Based on Fibonacci ratios",
                "Require precise measurements",
                "Point D is the trading opportunity",
                "5-point structure (X-A-B-C-D)",
                "Very high accuracy when correct"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Bullish Gartley",
                    description = "Most popular harmonic pattern",
                    identificationTips = listOf(
                        "Starts with sharp move (XA)",
                        "AB retraces 61.8% of XA",
                        "BC is 38.2-88.6% of AB",
                        "CD = 127-161.8% of BC",
                        "D aligns with 78.6% of XA",
                        "Enter at D, target C then A"
                    )
                ),
                PatternExample(
                    patternName = "Bat Pattern",
                    description = "Precise retracement harmonic",
                    identificationTips = listOf(
                        "AB = 38.2-50% of XA",
                        "BC = 38.2-88.6% of AB",
                        "CD = 161.8-261.8% of BC",
                        "D = 88.6% of XA exactly",
                        "Tighter stop loss than Gartley"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What are harmonic patterns based on?",
                        options = listOf(
                            "Random numbers",
                            "Fibonacci ratios",
                            "Trading volume",
                            "Planetary alignments"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Harmonic patterns are based on Fibonacci ratios found throughout nature and markets."
                    ),
                    Question(
                        question = "In a harmonic pattern, what is Point D?",
                        options = listOf(
                            "The starting point",
                            "The midpoint",
                            "The Potential Reversal Zone",
                            "The stop loss point"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Point D is the Potential Reversal Zone (PRZ) where traders enter positions expecting a reversal."
                    ),
                    Question(
                        question = "In a Gartley pattern, AB retraces what percentage of XA?",
                        options = listOf(
                            "38.2%",
                            "50%",
                            "61.8%",
                            "78.6%"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "In a Gartley pattern, the AB leg retraces 61.8% (Golden Ratio) of the XA leg."
                    ),
                    Question(
                        question = "What is the Golden Ratio in Fibonacci analysis?",
                        options = listOf(
                            "0.382 (38.2%)",
                            "0.500 (50%)",
                            "0.618 (61.8%)",
                            "1.000 (100%)"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "The Golden Ratio is 0.618 (61.8%), a key Fibonacci level used extensively in harmonic pattern analysis."
                    )
                )
            )
        )
    }

    /**
     * Lesson 7: Volume Analysis & Patterns
     */
    private fun getLesson7(): Lesson {
        return Lesson(
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
    }

    /**
     * Lesson 8: Multiple Timeframe Analysis
     */
    private fun getLesson8(): Lesson {
        return Lesson(
            id = 8,
            title = "Multiple Timeframe Analysis",
            description = "Align patterns across different timeframes for higher probability",
            content = """
                The same asset tells different stories on different timeframes. Master multiple timeframe analysis to dramatically improve your win rate.
                
                **The Timeframe Hierarchy:**
                
                **1. Higher Timeframe (HTF)** - Overall trend
                - Daily, Weekly, Monthly
                - Determines market bias
                - Major support/resistance levels
                - "Big picture" trend direction
                
                **2. Trading Timeframe (TF)** - Your main chart
                - 1H, 4H, Daily
                - Where you identify patterns
                - Entry/exit signals
                - Main analysis timeframe
                
                **3. Lower Timeframe (LTF)** - Entry timing
                - 5m, 15m, 1H
                - Precise entry points
                - Stop loss placement
                - Exit refinement
                
                **The Golden Rule:**
                "Trade in direction of higher timeframe trend"
                
                **Multi-Timeframe Strategy:**
                
                **Step 1: Start with HTF**
                - Identify overall trend (up/down/sideways)
                - Mark major support/resistance
                - Note key patterns forming
                
                **Step 2: Move to Trading TF**
                - Look for patterns aligned with HTF trend
                - Identify entry opportunities
                - Set targets based on HTF levels
                
                **Step 3: Drop to LTF**
                - Fine-tune entry point
                - Set precise stop loss
                - Monitor trade execution
                
                **Example Combinations:**
                
                **Scalping:** 1H → 15m → 5m
                **Day Trading:** Daily → 1H → 15m
                **Swing Trading:** Weekly → Daily → 4H
                **Position Trading:** Monthly → Weekly → Daily
                
                **Pattern Alignment:**
                - HTF Bull Flag + TF Ascending Triangle = High probability
                - HTF Downtrend + TF Bull Pattern = Low probability (counter-trend)
                - Multiple timeframes confirming = Highest confidence
            """.trimIndent(),
            keyPoints = listOf(
                "Always start with higher timeframe",
                "Trade in direction of HTF trend",
                "Use LTF for precise entries",
                "Pattern alignment increases probability",
                "HTF levels are most significant"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Timeframe Alignment",
                    description = "All timeframes confirm same direction",
                    identificationTips = listOf(
                        "Weekly: Uptrend with bull flag forming",
                        "Daily: Breakout above resistance",
                        "4H: Ascending triangle breakout",
                        "1H: Pullback to support for entry",
                        "All patterns confirm bullish bias"
                    )
                ),
                PatternExample(
                    patternName = "Top-Down Analysis",
                    description = "Systematic timeframe evaluation",
                    identificationTips = listOf(
                        "Monthly: Identify long-term trend",
                        "Weekly: Find intermediate patterns",
                        "Daily: Spot entry patterns",
                        "4H: Refine entry timing",
                        "1H: Execute with precision"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "Which timeframe should you analyze first?",
                        options = listOf(
                            "The lowest timeframe",
                            "The highest timeframe",
                            "Your trading timeframe",
                            "It doesn't matter"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Always start with the highest timeframe to identify the overall trend and major levels."
                    ),
                    Question(
                        question = "What does the lower timeframe (LTF) help with?",
                        options = listOf(
                            "Determining overall trend",
                            "Precise entry and stop placement",
                            "Long-term targets",
                            "Pattern identification only"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Lower timeframes are used for precise entry timing and tight stop loss placement."
                    ),
                    Question(
                        question = "The highest probability trades occur when:",
                        options = listOf(
                            "Only one timeframe shows a pattern",
                            "Multiple timeframes align in the same direction",
                            "Timeframes contradict each other",
                            "You ignore higher timeframes"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "When multiple timeframes confirm the same direction, it dramatically increases trade probability."
                    ),
                    Question(
                        question = "What is the Golden Rule of multiple timeframe analysis?",
                        options = listOf(
                            "Always trade the lowest timeframe",
                            "Trade in direction of higher timeframe trend",
                            "Ignore higher timeframes completely",
                            "Only use one timeframe at a time"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "The Golden Rule is to trade in the direction of the higher timeframe trend, which provides the overall market bias and increases success probability."
                    )
                )
            )
        )
    }

    /**
     * Lesson 9: Pattern Psychology & Market Sentiment
     */
    private fun getLesson9(): Lesson {
        return Lesson(
            id = 9,
            title = "Pattern Psychology & Market Sentiment",
            description = "Understand the psychology behind patterns and crowd behavior",
            content = """
                Chart patterns work because they reflect human psychology. Understanding WHY patterns form helps you trade them better.
                
                **The Psychology Behind Patterns:**
                
                **Fear and Greed Cycle:**
                1. **Optimism** - Market starts rising
                2. **Excitement** - More buyers join
                3. **Thrill** - Everyone is buying
                4. **Euphoria** - Top (everyone is in)
                5. **Anxiety** - First signs of trouble
                6. **Denial** - "It's just a pullback"
                7. **Fear** - Reality sets in
                8. **Desperation** - Trying to get out
                9. **Panic** - Capitulation
                10. **Despondency** - Bottom
                11. **Depression** - Market bottoms
                12. **Hope** - Cycle repeats
                
                **Pattern Formation Psychology:**
                
                **Head and Shoulders:**
                - Left Shoulder: Bulls still in control
                - Head: Final push by bulls (false hope)
                - Right Shoulder: Buyers exhausted
                - Neckline break: Bears take control
                - Psychology: Weakening buying pressure
                
                **Double Top:**
                - First peak: Bulls test resistance
                - Pullback: Profit taking
                - Second peak: Failed attempt (can't break)
                - Psychology: "Fool me once..."
                - Traders remember first rejection
                
                **Bull Flag:**
                - Flagpole: FOMO buying
                - Flag: Profit taking, consolidation
                - Breakout: Late buyers and continuation
                - Psychology: Brief rest in strong trend
                
                **Market Sentiment Indicators:**
                
                **1. Put/Call Ratio**
                - High ratio = Fear (bearish bets)
                - Low ratio = Greed (bullish bets)
                - Extreme readings = Contrarian signals
                
                **2. VIX (Fear Index)**
                - Low VIX = Complacency
                - High VIX = Fear/Panic
                - Spikes often mark bottoms
                
                **3. News Sentiment**
                - Extreme positive = Top warning
                - Extreme negative = Bottom signal
                - "Buy when there's blood in the streets"
                
                **Crowd Psychology:**
                - Early adopters profit (pattern forms)
                - Majority enters (pattern completes)
                - Late crowd gets trapped (reversal)
                - Understanding this = Trading edge
            """.trimIndent(),
            keyPoints = listOf(
                "Patterns reflect crowd psychology",
                "Fear and greed drive formations",
                "Sentiment extremes signal reversals",
                "Understanding psychology improves timing",
                "Contrarian thinking at extremes"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Capitulation Bottom",
                    description = "Panic selling creates opportunity",
                    identificationTips = listOf(
                        "Extreme negative news coverage",
                        "High VIX spike",
                        "Volume climax on selling",
                        "Wide-range down candle",
                        "Followed by reversal",
                        "Psychology: Maximum fear and despair"
                    )
                ),
                PatternExample(
                    patternName = "Euphoria Top",
                    description = "Excessive optimism marks peak",
                    identificationTips = listOf(
                        "Mainstream media touting gains",
                        "Everyone talking about profits",
                        "Low VIX (complacency)",
                        "Parabolic price rise",
                        "Reversal patterns forming",
                        "Psychology: 'This time is different'"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What emotion typically marks market tops?",
                        options = listOf(
                            "Fear",
                            "Panic",
                            "Euphoria",
                            "Depression"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Euphoria and excessive optimism typically mark market tops when everyone is invested."
                    ),
                    Question(
                        question = "A high VIX reading indicates:",
                        options = listOf(
                            "Market complacency",
                            "Fear and uncertainty",
                            "Bull market peak",
                            "Normal conditions"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "High VIX indicates fear and uncertainty in the market, often marking potential bottoms."
                    ),
                    Question(
                        question = "Why does a Head and Shoulders pattern form?",
                        options = listOf(
                            "Random chance",
                            "Progressively weakening buying pressure",
                            "Government manipulation",
                            "Computer algorithms only"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "H&S forms as buying pressure weakens with each attempt, reflecting exhaustion of bullish sentiment."
                    ),
                    Question(
                        question = "What does the VIX (Fear Index) measure?",
                        options = listOf(
                            "Stock prices directly",
                            "Market volatility and fear/uncertainty levels",
                            "Trading volume only",
                            "Company earnings"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "The VIX measures market volatility and fear levels - high VIX indicates fear/panic, low VIX shows complacency."
                    )
                )
            )
        )
    }

    /**
     * Lesson 10: Advanced Pattern Trading Strategies
     */
    private fun getLesson10(): Lesson {
        return Lesson(
            id = 10,
            title = "Advanced Pattern Trading Strategies",
            description = "Master professional techniques for consistent profitability",
            content = """
                Transform from pattern spotter to professional trader with advanced strategies and risk management.
                
                **Risk Management Rules:**
                
                **1. The 2% Rule**
                - Never risk more than 2% per trade
                - Account: $10,000 → Max risk: $200
                - Protects capital from blow-up
                - Mandatory for long-term survival
                
                **2. Position Sizing**
                - Risk ÷ Stop Distance = Position Size
                - Example: $200 risk, $2 stop = 100 shares
                - Adjust size based on volatility
                - Smaller size = wider stop (volatile assets)
                
                **3. Risk:Reward Ratio**
                - Minimum 1:2 (risk $100 to make $200)
                - Better: 1:3 or higher
                - Win rate can be lower with good R:R
                - 40% win rate + 1:3 R:R = Profitable
                
                **Advanced Entry Strategies:**
                
                **1. Breakout Entry**
                - Enter immediately on breakout
                - Pros: Catch full move
                - Cons: Higher false breakout risk
                - Best for: Strong momentum patterns
                
                **2. Retest Entry**
                - Wait for price to retest broken level
                - Pros: Better entry, tighter stop
                - Cons: May miss move
                - Best for: Volatile markets
                
                **3. Partial Entry**
                - Enter 50% at breakout
                - Enter 50% on retest
                - Pros: Balanced approach
                - Cons: More complex management
                
                **Exit Strategies:**
                
                **1. Profit Targets**
                - Based on pattern measurement
                - Or prior support/resistance
                - Scale out: 50% at first target, rest at second
                
                **2. Trailing Stop**
                - Move stop to breakeven after 1R profit
                - Trail stop below swing lows (uptrend)
                - Locks in profits while staying in trend
                
                **3. Time Stop**
                - Exit if pattern takes too long
                - Example: Breakout should move within 3 bars
                - Prevents capital sitting in dead trades
                
                **Pattern Combination Strategies:**
                
                **Multiple Pattern Confirmation:**
                - HTF: Bull flag
                - MTF: Ascending triangle
                - LTF: Bullish engulfing
                - Result: High probability setup
                
                **Pattern + Indicator:**
                - Pattern for setup
                - RSI for confirmation
                - Example: Bull flag + RSI oversold = Strong entry
                
                **Professional Trading Plan:**
                
                **Pre-Market:**
                1. Review higher timeframes
                2. Identify key levels
                3. Mark potential patterns
                4. Set alerts
                
                **During Market:**
                1. Wait for setup
                2. Confirm with checklist
                3. Execute with discipline
                4. Manage position
                
                **Post-Market:**
                1. Journal trades
                2. Review performance
                3. Analyze mistakes
                4. Update watchlist
                
                **The Professional Edge:**
                - Patience (wait for best setups)
                - Discipline (follow rules)
                - Consistency (repeat process)
                - Adaptation (market changes)
                - Review (learn from all trades)
            """.trimIndent(),
            keyPoints = listOf(
                "Risk management is priority #1",
                "Never risk more than 2% per trade",
                "Minimum 1:2 risk:reward ratio",
                "Combine multiple confirmations",
                "Professional discipline = Consistent profits"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Triple Confirmation Setup",
                    description = "High probability pattern trade",
                    identificationTips = listOf(
                        "Daily: Bull flag forming",
                        "4H: Ascending triangle breakout",
                        "1H: Bullish engulfing at support",
                        "Volume: Increasing on breakout",
                        "RSI: Above 50 showing strength",
                        "Risk:Reward: 1:3 minimum",
                        "Position size: 2% risk maximum"
                    )
                ),
                PatternExample(
                    patternName = "Professional Trade Management",
                    description = "Complete trade execution example",
                    identificationTips = listOf(
                        "Entry: Triangle breakout with volume",
                        "Stop: Below pattern at -2% account risk",
                        "Target 1: Pattern height at 1:2 R:R",
                        "Target 2: Previous resistance at 1:3 R:R",
                        "Scale: Exit 50% at Target 1",
                        "Trail: Move stop to breakeven",
                        "Exit: Take remaining at Target 2 or trail stop"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What is the maximum risk per trade for proper risk management?",
                        options = listOf(
                            "10% of account",
                            "5% of account",
                            "2% of account",
                            "Whatever feels right"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "The 2% rule is a cornerstone of risk management, protecting your capital from catastrophic losses."
                    ),
                    Question(
                        question = "What is the minimum acceptable risk:reward ratio?",
                        options = listOf(
                            "1:1",
                            "1:2",
                            "1:3",
                            "2:1"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "A minimum 1:2 risk:reward ratio ensures you can be profitable even with a 50% win rate."
                    ),
                    Question(
                        question = "What is the best type of entry for volatile markets?",
                        options = listOf(
                            "Immediate breakout entry",
                            "Retest entry with confirmation",
                            "Random entry timing",
                            "No entry at all"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "In volatile markets, waiting for a retest provides better entry and tighter stop placement."
                    ),
                    Question(
                        question = "What makes a high probability setup?",
                        options = listOf(
                            "Single pattern on one timeframe",
                            "Multiple confirmations across timeframes",
                            "Gut feeling",
                            "News headlines"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Multiple confirmations across timeframes, volume, and indicators create highest probability setups."
                    )
                )
            )
        )
    }

    /**
     * Lesson 11: Symmetrical Triangles & Diamonds
     */
    private fun getLesson11(): Lesson {
        return Lesson(
            id = 11,
            title = "Symmetrical Triangles & Diamonds",
            description = "Master bilateral consolidation patterns and probability weighting",
            content = """
                Symmetrical triangles and diamond patterns represent periods of market indecision where neither bulls nor bears have clear control. These bilateral patterns can break in either direction, but statistical analysis shows they usually continue the prevailing trend.
                
                **Symmetrical Triangle Structure:**
                - Converging trendlines with progressively lower highs and higher lows
                - Both buyers and sellers become less aggressive with each swing
                - Decreasing volume as pattern develops shows declining interest and volatility
                - Breakout direction remains uncertain until actual breakout occurs
                - Typically forms mid-trend as continuation pattern (consolidation before next leg)
                - Requires minimum 4 touchpoints (2 on each trendline) to be valid
                - Pattern usually completes in 1-3 months on daily charts
                
                **Diamond Pattern Characteristics:**
                - Rare but highly reliable reversal formation
                - Two-phase structure: broadening formation followed by contraction
                - Creates diamond or rhombus shape when trendlines are drawn
                - Resembles head & shoulders but with symmetrical shoulders on both sides
                - Powerful reversal signal when it occurs at major tops or bottoms
                - Volume pattern: increases during broadening phase, decreases during contraction
                - Often marks significant market turning points
                
                **Trading Symmetrical Triangles Successfully:**
                - Patience is key - wait for confirmed breakout (candle close beyond trendline)
                - Volume must increase significantly on breakout (50%+ above average validates the move)
                - Measure pattern height at the widest point (beginning of triangle)
                - Project that height from breakout point to calculate price target
                - Place stop loss on opposite side of triangle just inside the pattern
                - Best entry often comes on retest of broken trendline
                - Avoid trading inside the triangle - wait for clear directional move
                
                **Probability Weighting and Statistics:**
                - In established uptrends: approximately 65% probability of upward breakout
                - In established downtrends: approximately 65% probability of downward breakout
                - In no clear trend: 50/50 probability - harder to trade
                - Breakout usually occurs at 2/3 to 3/4 through the pattern width
                - If price reaches apex without breaking, pattern loses validity
                - False breakouts are common - always require volume confirmation
                - Higher timeframe triangles more reliable than lower timeframes
                
                **Common Trading Mistakes:**
                - Entering before breakout (getting whipsawed inside the consolidation)
                - Ignoring volume on breakout (most false breakouts have weak volume)
                - Trading against the prevailing trend (always favor trend direction)
                - Setting unrealistic targets beyond measured move
                - Not using stop losses
            """.trimIndent(),
            keyPoints = listOf(
                "Symmetrical triangles show equilibrium between buyers and sellers",
                "Breakout direction follows prevailing trend 65% of the time",
                "Volume confirmation essential - decreases during pattern, increases on breakout",
                "Diamond patterns are rare but highly reliable reversal signals",
                "Measure and project pattern height for price targets"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Symmetrical Triangle",
                    description = "Bilateral consolidation pattern",
                    identificationTips = listOf(
                        "Draw converging trendlines connecting highs and lows",
                        "At least 2 higher lows and 2 lower highs required",
                        "Volume should decrease as pattern forms",
                        "Breakout typically occurs at 2/3 of pattern width",
                        "Measure widest point for target calculation"
                    )
                ),
                PatternExample(
                    patternName = "Diamond Top",
                    description = "Rare but powerful reversal pattern",
                    identificationTips = listOf(
                        "Broadening phase followed by narrowing phase",
                        "Forms after strong uptrend",
                        "Volume expands then contracts with price",
                        "Breakdown confirms reversal",
                        "Target = pattern height projected downward"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What is the breakout probability for symmetrical triangles in an uptrend?",
                        options = listOf(
                            "50% either direction",
                            "65% upward",
                            "80% upward",
                            "100% upward"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Symmetrical triangles in uptrends have approximately 65% probability of breaking upward, continuing the trend."
                    ),
                    Question(
                        question = "When does a symmetrical triangle typically break out?",
                        options = listOf(
                            "At the beginning of the pattern",
                            "Exactly at the apex",
                            "At 2/3 to 3/4 of the pattern width",
                            "Never predictable"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Breakouts typically occur at 2/3 to 3/4 of the pattern width, before reaching the apex."
                    ),
                    Question(
                        question = "What makes a diamond pattern particularly significant?",
                        options = listOf(
                            "It's very common",
                            "It's rare and highly reliable as reversal",
                            "It always breaks upward",
                            "No volume is needed"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Diamond patterns are rare but when they occur, they're highly reliable reversal signals."
                    ),
                    Question(
                        question = "How should volume behave during a symmetrical triangle?",
                        options = listOf(
                            "Constantly increasing",
                            "Decrease during formation, increase on breakout",
                            "Remain constant",
                            "Volume doesn't matter"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Volume should decrease as the pattern forms (consolidation) and increase sharply on breakout (confirmation)."
                    )
                )
            )
        )
    }

    /**
     * Lesson 12: Rising & Falling Wedges
     */
    private fun getLesson12(): Lesson {
        return Lesson(
            id = 12,
            title = "Rising & Falling Wedges",
            description = "Master wedge patterns for early reversal detection and trend exhaustion signals",
            content = """
                Wedge patterns are converging price structures that signal trend exhaustion and impending reversals. Unlike triangle patterns where trendlines converge from opposite directions, wedge patterns have both trendlines sloping in the same direction, creating a unique visual signature.
                
                **Rising Wedge (Bearish Reversal):**
                - Both support and resistance trendlines slope upward together
                - Narrowing price range within an upward-sloping channel
                - Typically forms during extended uptrends as buying momentum weakens
                - Each new high requires more effort - buyers becoming exhausted
                - Volume consistently decreases as pattern develops (critical characteristic)
                - Eventually breaks downward through support line (opposite to slope)
                - Target: Measure distance from first touch to last, project downward
                - The steeper the wedge, the more aggressive the eventual breakdown
                - Often forms over several weeks to months
                
                **Falling Wedge (Bullish Reversal):**
                - Both trendlines slope downward in parallel convergence
                - Converging pattern that forms within established downtrends
                - Shows sellers losing control as price volatility contracts
                - Each new low is less convincing - selling pressure fading
                - Diminishing volume during formation signals exhaustion
                - Breaks upward through resistance (opposite to downward slope)
                - Target: Height of pattern at widest point projected upward
                - Common at major market bottoms after capitulation
                
                **Key Distinguishing Characteristics:**
                - Both lines must slope in the SAME direction (unlike triangles)
                - Minimum of 3 touches required on each trendline for validity
                - Wedges are primarily reversal patterns, not continuation patterns
                - Breakout occurs opposite to wedge slope direction
                - Volume contraction throughout formation signals weakening trend momentum
                - Wedges are typically steeper and narrower than triangle patterns
                - Pattern invalidated if breakout occurs in direction of slope
                
                **Professional Trading Strategies:**
                - Entry: Enter on confirmed breakout with strong volume surge
                - Wait for candle close beyond trendline, not just a wick
                - Stop loss: Place just beyond the opposite trendline for protection
                - Target: Pattern height measured at widest point, or previous swing level
                - Confirmation: Look for reversal candlestick pattern at breakout point
                - Best trades occur when wedge forms within context of larger trend change
                - Retest of broken trendline often provides excellent second entry opportunity
                
                **Pattern Psychology:**
                Rising wedges show bulls making progressively weaker highs despite upward movement - unsustainable momentum that leads to reversal. Falling wedges show bears running out of selling pressure as lows become shallower - capitulation nearing completion.
            """.trimIndent(),
            keyPoints = listOf(
                "Rising wedges break downward - bearish reversals",
                "Falling wedges break upward - bullish reversals",
                "Both trendlines slope in same direction",
                "Volume decreases during formation",
                "Breakout confirms trend exhaustion and reversal"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Rising Wedge",
                    description = "Bearish reversal after uptrend exhaustion",
                    identificationTips = listOf(
                        "Both lines slope upward, converging",
                        "Higher highs with decreasing momentum",
                        "Volume steadily declining",
                        "Steeper than typical ascending triangle",
                        "Breakdown through lower trendline confirms"
                    )
                ),
                PatternExample(
                    patternName = "Falling Wedge",
                    description = "Bullish reversal after downtrend exhaustion",
                    identificationTips = listOf(
                        "Both lines slope downward, narrowing",
                        "Lower lows with weakening selling pressure",
                        "Contracting volatility and volume",
                        "Often forms at major bottoms",
                        "Upside breakout signals reversal"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "In which direction do the trendlines slope in a Rising Wedge?",
                        options = listOf(
                            "Both downward",
                            "Both upward",
                            "One up, one down",
                            "Horizontal"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "In a Rising Wedge, both the support and resistance lines slope upward while converging."
                    ),
                    Question(
                        question = "A Rising Wedge typically breaks in which direction?",
                        options = listOf(
                            "Upward continuation",
                            "Downward reversal",
                            "Sideways",
                            "Either direction equally"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Rising Wedges are bearish reversal patterns that typically break downward, signaling trend exhaustion."
                    ),
                    Question(
                        question = "What happens to volume as a wedge pattern forms?",
                        options = listOf(
                            "Steadily increases",
                            "Remains constant",
                            "Decreases and contracts",
                            "Becomes extremely volatile"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Volume typically decreases as a wedge forms, reflecting diminishing conviction before reversal."
                    ),
                    Question(
                        question = "What does a Falling Wedge signal?",
                        options = listOf(
                            "Bearish continuation",
                            "Bullish reversal",
                            "Neutral consolidation",
                            "Market crash"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "A Falling Wedge is a bullish reversal pattern that signals downtrend exhaustion and potential upside."
                    )
                )
            )
        )
    }

    /**
     * Lesson 13: Broadening & Megaphone Patterns
     */
    private fun getLesson13(): Lesson {
        return Lesson(
            id = 13,
            title = "Broadening & Megaphone Patterns",
            description = "Identify expanding volatility patterns that signal market tops and increased uncertainty",
            content = """
                Broadening patterns, also called megaphone patterns, are rare but significant formations where price swings progressively expand over time, creating diverging trendlines. These patterns signal increasing volatility, market uncertainty, and emotional trading behavior - the opposite of consolidating triangle patterns.
                
                **Broadening Top (Bearish Reversal):**
                - Expanding price range with diverging trendlines that widen over time
                - Series of higher highs and lower lows - each swing exceeds the previous
                - Typically forms at major market tops after extended bull market rallies
                - Volume increases dramatically with each successive swing
                - Requires minimum three distinct peaks and two troughs to confirm pattern
                - Breakdown below the second trough confirms bearish reversal
                - Often marks euphoric market tops before major corrections
                - Pattern can take weeks or months to fully form
                
                **Broadening Bottom (Bullish Reversal):**
                - Similar expanding structure but forms at market bottoms instead of tops
                - Expanding volatility reflects panic selling and capitulation behavior
                - Significantly less common than broadening tops
                - Shows increasing fear and uncertainty at market lows
                - Breakout above second peak confirms bullish reversal and trend change
                - Often associated with market bottoms after prolonged downtrends
                
                **Megaphone Pattern Characteristics:**
                - Exact opposite of triangle patterns (expanding versus contracting)
                - Reflects highly emotional and unstable market conditions
                - Wild price swings between panic selling and fear-driven rallies
                - Neither bulls nor bears maintain control - constant battle
                - Extremely difficult to trade profitably inside the pattern due to whipsaws
                - Best approached after pattern completion with clear breakout confirmation
                - High false breakout rate makes aggressive trading dangerous
                - Represents loss of market efficiency and rational pricing
                
                **Professional Trading Strategies:**
                - Never trade inside the pattern - volatility will whipsaw your positions
                - Wait patiently for clear, confirmed breakout with strong volume
                - Entry: Only after decisive break of support or resistance with volume
                - Stop loss: Must be conservative and wide - pattern has high failure rate
                - Target: Pattern height at widest point, or previous major support/resistance level
                - Position sizing: Use smaller size due to unreliability and high risk
                - Risk management absolutely critical - expect the unexpected
                
                **Market Psychology and Implications:**
                - Reflects complete loss of control and conviction among market participants
                - Wild swings between fear and greed create emotional roller coaster
                - Often occurs during major market transitions or uncertainty periods
                - Retail traders typically get whipsawed repeatedly trying to catch swings
                - Professional institutional traders step back and wait for pattern resolution
                - Pattern indicates market is searching for fair value but can't find equilibrium
                
                **Common Mistakes:**
                - Trading inside the pattern trying to catch each swing (recipe for losses)
                - Using tight stops (will get stopped out on volatility)
                - Ignoring the high failure rate
                - Over-leveraging positions
            """.trimIndent(),
            keyPoints = listOf(
                "Expanding price range with diverging trendlines",
                "Broadening tops signal major reversals at market highs",
                "Increasing volume and volatility characterize pattern",
                "Difficult to trade - wait for breakout confirmation",
                "Reflects emotional market with no clear control"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Broadening Top",
                    description = "Expanding volatility pattern at market peak",
                    identificationTips = listOf(
                        "Three peaks, each higher than previous",
                        "Two troughs, each lower than previous",
                        "Diverging trendlines expanding outward",
                        "Volume increases with each swing",
                        "Breakdown below second trough confirms",
                        "Commonly marks major market tops"
                    )
                ),
                PatternExample(
                    patternName = "Megaphone Pattern",
                    description = "Expanding range showing market instability",
                    identificationTips = listOf(
                        "Opposite of triangle - getting wider",
                        "Wild swings with no clear winner",
                        "Emotional trading dominates",
                        "Avoid trading until clear breakout",
                        "High false breakout rate",
                        "Conservative position sizing essential"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "How do broadening patterns differ from triangles?",
                        options = listOf(
                            "They contract over time",
                            "They expand with diverging trendlines",
                            "They are horizontal",
                            "They have no trendlines"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Broadening patterns expand over time with diverging trendlines, opposite of triangles which converge."
                    ),
                    Question(
                        question = "Where do broadening tops typically form?",
                        options = listOf(
                            "At market bottoms",
                            "During consolidation",
                            "At major market tops",
                            "In sideways markets"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Broadening tops typically form at major market tops after extended rallies, signaling reversal."
                    ),
                    Question(
                        question = "What is the best trading approach for megaphone patterns?",
                        options = listOf(
                            "Trade aggressively inside the pattern",
                            "Wait for clear breakout confirmation",
                            "Short the highs and buy the lows",
                            "Ignore the pattern completely"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Megaphone patterns are too volatile to trade inside - wait for clear breakout confirmation."
                    ),
                    Question(
                        question = "What does a broadening pattern indicate about market psychology?",
                        options = listOf(
                            "Strong conviction and control",
                            "Calm and stable conditions",
                            "Increasing uncertainty and emotion",
                            "Professional dominance"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Broadening patterns reflect increasing uncertainty, emotion, and loss of control in the market."
                    )
                )
            )
        )
    }

    /**
     * Lesson 14: Head & Shoulders Variations
     */
    private fun getLesson14(): Lesson {
        return Lesson(
            id = 14,
            title = "Head & Shoulders Variations",
            description = "Master classic and inverse H&S patterns plus complex variations for reliable reversal trading",
            content = """
                Head and Shoulders patterns are among the most reliable and widely recognized reversal formations in all of technical analysis. When properly identified and traded, they offer excellent risk-to-reward ratios. Understanding the various pattern configurations increases your pattern recognition skills and creates more trading opportunities.
                
                **Classic Head & Shoulders (Bearish Reversal):**
                - Three sequential peaks: left shoulder, head (highest peak), right shoulder (similar height to left)
                - Neckline drawn connecting the two troughs between the three peaks
                - Volume pattern crucial: typically decreases from left shoulder through head formation
                - Breakdown decisively below neckline with increased volume confirms the reversal
                - Price target: Measure vertical distance from head peak to neckline, project that distance downward from breakdown point
                - Often marks major market tops after extended uptrends
                - Pattern can take several weeks to months to fully develop
                - The more symmetrical the shoulders, the more reliable the pattern
                
                **Inverse Head & Shoulders (Bullish Reversal):**
                - Perfect mirror image forming at market bottoms instead of tops
                - Three sequential troughs: left shoulder, head (lowest/deepest trough), right shoulder
                - Center trough (head) must be distinctly lower than both shoulders
                - Volume typically increases as pattern develops (opposite of bearish H&S)
                - Breakout above neckline with strong volume confirms bullish reversal
                - Target: Measure head-to-neckline distance, project upward from breakout
                - Marks significant market bottoms after prolonged downtrends
                - One of the most reliable bottom formations in technical analysis
                
                **Complex Head & Shoulders Variations:**
                
                **Multiple Shoulders Configuration:**
                - Two or more shoulders on left side, right side, or both
                - Pattern remains valid if structure maintains overall symmetry
                - Neckline must connect all troughs between shoulders and head
                - Confirms exactly same way as classic three-peak pattern
                - More complex patterns can be more reliable when properly formed
                
                **Sloping Neckline Variation:**
                - Neckline angles upward or downward instead of horizontal
                - Upward-sloping neckline = slightly less bearish (some buying strength remains)
                - Downward-sloping neckline = more aggressively bearish signal
                - Still functions as valid reversal pattern regardless of slope
                - Target measured perpendicular from neckline
                
                **Failed Head & Shoulders (Trap Pattern):**
                - Price breaks through neckline but quickly reverses back inside pattern
                - Strong invalidation and bullish signal when bearish H&S fails
                - Often leads to powerful explosive move in opposite direction
                - Classic trap for breakout traders who entered without confirmation
                - Use failed patterns as contrarian signals
                
                **Professional Trading Guidelines:**
                - Entry: Enter on confirmed neckline break with volume surge
                - Stop loss: Place just above right shoulder (bearish H&S) or below right shoulder (bullish)
                - Take partial profits at measured target, let remainder run
                - Watch for neckline retest - provides excellent second entry opportunity
                - Combine with candlestick confirmation for highest probability
            """.trimIndent(),
            keyPoints = listOf(
                "Most reliable reversal pattern in technical analysis",
                "Volume pattern crucial - decreases to head, increases on break",
                "Neckline break confirms pattern completion",
                "Measured target from head to neckline distance",
                "Variations valid if maintain essential structure"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Classic Head & Shoulders",
                    description = "Bearish reversal marking market top",
                    identificationTips = listOf(
                        "Left shoulder forms after uptrend",
                        "Head exceeds left shoulder high",
                        "Right shoulder lower than head",
                        "Volume lighter on right shoulder",
                        "Neckline break on increased volume",
                        "Target = head-to-neckline distance"
                    )
                ),
                PatternExample(
                    patternName = "Inverse Head & Shoulders",
                    description = "Bullish reversal marking market bottom",
                    identificationTips = listOf(
                        "Forms after downtrend",
                        "Head is lowest point (deepest trough)",
                        "Right shoulder higher than head",
                        "Volume increases through formation",
                        "Breakout above neckline confirms",
                        "Highly reliable bottom signal"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "In a classic H&S pattern, which peak is the highest?",
                        options = listOf(
                            "Left shoulder",
                            "The head",
                            "Right shoulder",
                            "All equal height"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "The head is always the highest peak in a Head & Shoulders pattern, with shoulders lower on both sides."
                    ),
                    Question(
                        question = "How do you calculate the price target for H&S?",
                        options = listOf(
                            "Measure neckline to head, project from breakout",
                            "Double the pattern width",
                            "Guess based on previous support",
                            "There is no specific target"
                        ),
                        correctAnswerIndex = 0,
                        explanation = "Measure the distance from neckline to head peak, then project that distance from the breakout point."
                    ),
                    Question(
                        question = "What confirms a Head & Shoulders pattern?",
                        options = listOf(
                            "Formation of the head",
                            "Completion of right shoulder",
                            "Break below neckline with volume",
                            "Just the shoulder formation"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Pattern is only confirmed when price breaks below the neckline with increased volume."
                    ),
                    Question(
                        question = "An Inverse Head & Shoulders forms at:",
                        options = listOf(
                            "Market tops",
                            "Market bottoms",
                            "Consolidation zones",
                            "Random locations"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Inverse H&S is a bullish reversal pattern that forms at market bottoms after downtrends."
                    )
                )
            )
        )
    }

    /**
     * Lesson 15: Double & Triple Tops/Bottoms
     */
    private fun getLesson15(): Lesson {
        return Lesson(
            id = 15,
            title = "Double & Triple Tops/Bottoms",
            description = "Identify and trade powerful reversal patterns formed by multiple tests of support and resistance",
            content = """
                Double and triple tops/bottoms are classic reversal patterns that occur when price tests a key level multiple times and fails to break through, signaling trend exhaustion.
                
                **Double Top (Bearish Reversal):**
                - Two peaks at approximately same price level
                - Forms "M" shape on chart
                - Second peak often slightly lower than first
                - Neckline connects the trough between peaks
                - Breakdown below neckline confirms reversal
                - Target: Distance from neckline to peaks, projected down
                - Volume typically lower on second peak
                
                **Double Bottom (Bullish Reversal):**
                - Two troughs at similar price level
                - Forms "W" shape on chart
                - Second bottom may be slightly higher
                - Neckline connects peak between troughs
                - Breakout above neckline confirms reversal
                - Target: Distance from neckline to troughs, projected up
                - Volume increases on second bottom and breakout
                
                **Triple Top (Bearish Reversal):**
                - Three peaks at approximately same resistance
                - More reliable than double top
                - Shows strong resistance level
                - Volume decreases with each peak
                - Breakdown confirms after third rejection
                
                **Triple Bottom (Bullish Reversal):**
                - Three troughs at similar support level
                - Highly reliable reversal pattern
                - Multiple tests prove support strength
                - Volume typically increases on breakout
                - Stronger signal than double bottom
                
                **Trading Rules:**
                - Wait for neckline break confirmation
                - Entry: On breakout with volume increase
                - Stop: Beyond the pattern (above tops or below bottoms)
                - Target: Measured move from pattern height
                - False breakouts common - wait for close beyond neckline
            """.trimIndent(),
            keyPoints = listOf(
                "Double/triple tops form M/triple-peak, bottoms form W/triple-trough",
                "Multiple tests show strong support/resistance levels",
                "Neckline break confirms pattern completion",
                "Triple patterns more reliable than double patterns",
                "Volume confirmation essential for validity"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Double Top",
                    description = "Two failed attempts to break resistance",
                    identificationTips = listOf(
                        "Two peaks at similar price level",
                        "Forms after uptrend",
                        "Second peak volume lighter than first",
                        "Trough between peaks forms neckline",
                        "Break below neckline on volume confirms",
                        "Measure pattern height for target"
                    )
                ),
                PatternExample(
                    patternName = "Triple Bottom",
                    description = "Three successful tests of support level",
                    identificationTips = listOf(
                        "Three troughs at approximately same price",
                        "Forms after downtrend",
                        "Each test shows buyers defending level",
                        "Volume increases on third bottom",
                        "Breakout above neckline confirms reversal",
                        "Very reliable bullish reversal signal"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What shape does a Double Top create on a chart?",
                        options = listOf(
                            "W shape",
                            "M shape",
                            "V shape",
                            "Triangle shape"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "A Double Top creates an 'M' shape with two peaks and a trough in the middle."
                    ),
                    Question(
                        question = "Which pattern is generally more reliable?",
                        options = listOf(
                            "Double Top",
                            "Triple Top",
                            "Both equally reliable",
                            "Neither is reliable"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Triple patterns are generally more reliable as they show multiple confirmations of the support/resistance level."
                    ),
                    Question(
                        question = "How is the target calculated for a Double Bottom?",
                        options = listOf(
                            "Distance from neckline to bottoms, projected upward",
                            "Arbitrary guess",
                            "Previous high",
                            "No specific target"
                        ),
                        correctAnswerIndex = 0,
                        explanation = "Measure the distance from neckline to the bottoms, then project that distance upward from the breakout point."
                    ),
                    Question(
                        question = "What confirms a Double Bottom pattern?",
                        options = listOf(
                            "Formation of first bottom",
                            "Formation of second bottom",
                            "Breakout above neckline with volume",
                            "Any price movement"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "The pattern is only confirmed when price breaks above the neckline with increased volume."
                    )
                )
            )
        )
    }

    /**
     * Lesson 16: Rounding, Cup & Handle Structures
     */
    private fun getLesson16(): Lesson {
        return Lesson(
            id = 16,
            title = "Rounding, Cup & Handle Structures",
            description = "Master gradual accumulation patterns that signal major bullish breakouts and trend continuations",
            content = """
                Rounding and cup patterns represent gradual accumulation periods where smart money quietly builds positions before major breakouts. These patterns show measured, controlled buying.
                
                **Rounding Bottom (Bullish Reversal):**
                - Gradual U-shaped curve at market bottom
                - Long formation period (months to years)
                - Represents slow accumulation phase
                - Volume decreases into the bottom, increases on right side
                - No sharp V-bottom - gradual transition
                - Breakout above resistance with volume confirms
                - Very reliable long-term reversal pattern
                
                **Cup & Handle (Bullish Continuation):**
                - Cup: U-shaped pattern after uptrend
                - Handle: Short consolidation/pullback on right side
                - Handle depth: 1/3 of cup depth maximum
                - Volume: Decreases during cup and handle formation
                - Breakout: Above handle resistance with volume surge
                - Target: Cup depth added to breakout point
                - William O'Neil's favorite pattern
                
                **Inverted Cup & Handle (Bearish):**
                - Upside-down cup formation
                - Less common than bullish version
                - Forms at market tops
                - Handle points upward before breakdown
                - Breakdown confirms reversal
                
                **Rounding Top (Bearish Reversal):**
                - Inverted U-shape at market tops
                - Gradual distribution by institutions
                - Volume pattern mirrors rounding bottom
                - Breakdown signals major reversal
                - Long-term bearish implication
                
                **Trading Guidelines:**
                - Entry: Breakout from handle or rounding pattern
                - Stop: Below handle low or pattern support
                - Target: Measured move from pattern depth
                - These are swing/position trade patterns
                - Patience required - patterns take time to form
            """.trimIndent(),
            keyPoints = listOf(
                "Rounding patterns show gradual accumulation or distribution",
                "Cup & Handle highly reliable continuation pattern",
                "Volume decreases during formation, surges on breakout",
                "Handle should be shallow - max 1/3 cup depth",
                "Long-term patterns requiring patience and discipline"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Cup & Handle",
                    description = "William O'Neil's signature bullish pattern",
                    identificationTips = listOf(
                        "U-shaped cup forms after uptrend",
                        "Cup depth typically 12-30%",
                        "Handle forms on right side of cup",
                        "Handle consolidation smaller than cup",
                        "Volume light during handle formation",
                        "Breakout above handle resistance confirms",
                        "Target = cup depth projected from breakout"
                    )
                ),
                PatternExample(
                    patternName = "Rounding Bottom",
                    description = "Long-term accumulation pattern",
                    identificationTips = listOf(
                        "Gradual U-shaped bottom formation",
                        "Takes months to develop fully",
                        "Volume lightest at bottom of U",
                        "Right side shows increasing volume",
                        "No sharp reversal - smooth transition",
                        "Very reliable when properly formed"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What is the maximum depth of a handle relative to the cup?",
                        options = listOf(
                            "Equal depth",
                            "1/2 the cup depth",
                            "1/3 the cup depth",
                            "Any depth"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "A proper handle should be no deeper than 1/3 of the cup depth to maintain pattern validity."
                    ),
                    Question(
                        question = "Cup & Handle is primarily a:",
                        options = listOf(
                            "Reversal pattern",
                            "Continuation pattern",
                            "Bilateral pattern",
                            "Topping pattern"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Cup & Handle is a bullish continuation pattern that forms during an uptrend pause."
                    ),
                    Question(
                        question = "What does a Rounding Bottom indicate?",
                        options = listOf(
                            "Quick reversal",
                            "Gradual accumulation by smart money",
                            "Market crash",
                            "Random price movement"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Rounding Bottom shows gradual accumulation by institutions over an extended period."
                    ),
                    Question(
                        question = "How is the Cup & Handle target calculated?",
                        options = listOf(
                            "Double the handle height",
                            "Cup depth added to breakout point",
                            "Random target selection",
                            "Previous resistance level"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Target is calculated by measuring cup depth and adding it to the breakout point."
                    )
                )
            )
        )
    }

    /**
     * Lesson 17: Gap Patterns (Breakaway, Runaway, Exhaustion)
     */
    private fun getLesson17(): Lesson {
        return Lesson(
            id = 17,
            title = "Gap Patterns (Breakaway, Runaway, Exhaustion)",
            description = "Understand different gap types and how to trade them for maximum profit and minimal risk",
            content = """
                Gaps are price discontinuities where no trading occurs between two price levels. Different gap types provide distinct trading opportunities and signals.
                
                **Breakaway Gap:**
                - Occurs at start of new trend
                - Breaks out of consolidation or pattern
                - High volume accompanies gap
                - Rarely filled - signals strong momentum
                - Entry: After gap confirmation
                - Stop: Below gap (bullish) or above (bearish)
                - Most reliable gap type
                - Marks beginning of significant moves
                
                **Runaway Gap (Continuation Gap):**
                - Occurs mid-trend during strong momentum
                - Shows powerful buying/selling pressure
                - Moderate to high volume
                - Often appears halfway through move
                - Target: Measure from start to gap, project equal distance
                - Multiple runaway gaps = very strong trend
                - Rarely filled until trend exhausts
                
                **Exhaustion Gap:**
                - Appears at end of trend
                - Final push before reversal
                - Often filled quickly (days to weeks)
                - High volume but fails to follow through
                - Signal to exit positions or take profits
                - Marks climactic buying/selling
                - Often accompanied by reversal patterns
                
                **Common Gap:**
                - Occurs within trading ranges
                - Low volume, low significance
                - Usually filled quickly
                - Not actionable - ignore these gaps
                - Common in low liquidity situations
                
                **Gap Trading Rules:**
                - Don't chase exhaustion gaps
                - Trade with breakaway and runaway gaps
                - Volume confirms gap significance
                - Failed gaps (filled quickly) reverse trade
                - Gaps act as support (bullish) or resistance (bearish)
            """.trimIndent(),
            keyPoints = listOf(
                "Breakaway gaps start trends - rarely filled",
                "Runaway gaps appear mid-trend - measure halfway point",
                "Exhaustion gaps signal trend end - often filled quickly",
                "Volume confirms gap significance and reliability",
                "Unfilled gaps become support/resistance levels"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Breakaway Gap",
                    description = "Gap marking new trend beginning",
                    identificationTips = listOf(
                        "Gaps out of consolidation pattern",
                        "High volume confirms strength",
                        "Price doesn't return to fill gap",
                        "Marks start of significant move",
                        "Trade in direction of gap",
                        "Stop below/above gap zone"
                    )
                ),
                PatternExample(
                    patternName = "Exhaustion Gap",
                    description = "Final gap before reversal",
                    identificationTips = listOf(
                        "Appears after extended trend",
                        "Initial high volume but momentum fades",
                        "Often filled within days or weeks",
                        "Accompanied by reversal candlesticks",
                        "Signal to exit trend trades",
                        "Look for opposite direction entry"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "Which gap type marks the start of a new trend?",
                        options = listOf(
                            "Common Gap",
                            "Breakaway Gap",
                            "Exhaustion Gap",
                            "Runaway Gap"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Breakaway Gaps occur at the start of new trends, breaking out of consolidation with high volume."
                    ),
                    Question(
                        question = "What typically happens to Exhaustion Gaps?",
                        options = listOf(
                            "They never get filled",
                            "They get filled quickly",
                            "They expand wider",
                            "They disappear"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Exhaustion Gaps typically get filled quickly (days to weeks) as the trend reverses."
                    ),
                    Question(
                        question = "Runaway Gaps often appear where in a trend?",
                        options = listOf(
                            "At the beginning",
                            "At the end",
                            "Halfway through the move",
                            "Randomly placed"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Runaway Gaps typically appear about halfway through a trend, helping measure the potential move."
                    ),
                    Question(
                        question = "What confirms the significance of a gap?",
                        options = listOf(
                            "Time of day",
                            "Trading volume",
                            "Day of week",
                            "Color of candle"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "High trading volume confirms gap significance - low volume gaps are often meaningless."
                    )
                )
            )
        )
    }

    /**
     * Lesson 18: Harmonic Pattern Foundations
     */
    private fun getLesson18(): Lesson {
        return Lesson(
            id = 18,
            title = "Harmonic Pattern Foundations",
            description = "Master Fibonacci-based harmonic patterns for precise reversal zone identification and high-probability trades",
            content = """
                Harmonic patterns use precise Fibonacci ratios to create geometric price structures that identify exact potential reversal zones with mathematical precision. These patterns are among the most sophisticated trading tools available and offer exceptionally high accuracy when correctly identified and executed. Unlike traditional chart patterns, harmonics require strict ratio adherence.
                
                **Critical Fibonacci Ratios in Harmonic Trading:**
                - 0.382 (38.2% retracement) - shallow pullback level
                - 0.618 (61.8% - The Golden Ratio) - most important retracement
                - 0.786 (78.6% - square root of 0.618) - deep retracement zone
                - 1.27 (127% extension) - moderate price projection
                - 1.618 (161.8% - Golden Ratio extension) - strong extension target
                - 2.24 (224% extension) - extreme extension for Butterfly/Crab
                - 2.618 (261.8% extension) - maximum extension level
                
                **XABCD 5-Point Pattern Structure:**
                All harmonic patterns follow this mandatory 5-point structure:
                - X: Pattern origin point - the starting reference
                - A: First significant directional move from X (impulse leg)
                - B: Retracement of XA leg by specific Fibonacci ratio
                - C: Retracement of AB leg by specific Fibonacci ratio
                - D: Completion point and Potential Reversal Zone (PRZ) - your entry location
                
                Each leg (XA, AB, BC, CD) must meet precise Fibonacci relationships for pattern validity.
                
                **Potential Reversal Zone (PRZ) - The Key Concept:**
                - Point D is where multiple Fibonacci ratio projections converge
                - Convergence creates highest probability reversal location
                - This is your exact entry zone for harmonic pattern trades
                - Must combine PRZ arrival with bullish/bearish candlestick confirmation
                - Stop loss placement: just beyond X point for protection
                - More Fibonacci convergences at D = stronger reversal probability
                
                **Strict Harmonic Pattern Rules:**
                - Patterns must meet exact Fibonacci ratios with maximum ±5% tolerance
                - PRZ must be identified by minimum 3 Fibonacci ratio convergences
                - Volume should ideally decrease as price approaches PRZ
                - Reversal candlestick pattern at Point D confirms entry signal
                - First target: Point C, Second target: Point A
                - Pattern invalid if ratios exceed tolerance - don't force trades
                
                **Why Harmonic Patterns Work So Effectively:**
                - Based on natural geometric proportions found throughout nature and markets
                - Self-fulfilling prophecy - thousands of traders watch same levels
                - Institutional trading algorithms programmed to recognize harmonic patterns
                - Precise entry and exit levels dramatically reduce trading risk
                - High win rate (65-75%) when patterns properly identified and executed
                - Mathematical precision removes emotional decision-making
                
                **Practical Application:**
                Use Fibonacci drawing tools on your trading platform to measure each leg precisely. Patient traders who wait for perfect pattern completion and PRZ confirmation achieve consistently profitable results.
            """.trimIndent(),
            keyPoints = listOf(
                "All harmonics use XABCD 5-point structure",
                "Fibonacci ratios must be precise (±5% tolerance)",
                "Point D is PRZ - optimal entry location",
                "Multiple Fibonacci convergences increase reliability",
                "Combine with candlestick confirmation for best results"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Bullish Harmonic Structure",
                    description = "5-point XABCD formation for reversals",
                    identificationTips = listOf(
                        "X marks pattern beginning",
                        "XA is initial impulse move downward",
                        "B retraces XA by specific ratio",
                        "C retraces AB by specific ratio",
                        "D completes at PRZ with Fib convergence",
                        "Enter long at D with confirmation"
                    )
                ),
                PatternExample(
                    patternName = "Fibonacci Convergence Zone",
                    description = "Multiple ratios confirming reversal",
                    identificationTips = listOf(
                        "1.27 extension of BC",
                        "0.786 retracement of XA",
                        "1.618 extension of AB",
                        "All levels converge at point D",
                        "Creates high-probability PRZ",
                        "Strongest when 3+ levels align"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What does XABCD represent in harmonic patterns?",
                        options = listOf(
                            "Random price points",
                            "5-point geometric structure",
                            "Volume levels",
                            "Time periods"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "XABCD represents the 5-point geometric structure that all harmonic patterns follow."
                    ),
                    Question(
                        question = "Where is the optimal entry point in a harmonic pattern?",
                        options = listOf(
                            "Point X",
                            "Point A",
                            "Point C",
                            "Point D (PRZ)"
                        ),
                        correctAnswerIndex = 3,
                        explanation = "Point D, the Potential Reversal Zone (PRZ), is the optimal entry point where Fibonacci ratios converge."
                    ),
                    Question(
                        question = "What is the Golden Ratio in Fibonacci?",
                        options = listOf(
                            "0.382",
                            "0.500",
                            "0.618",
                            "0.786"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "The Golden Ratio is 0.618 (61.8%), a fundamental ratio found throughout nature and markets."
                    ),
                    Question(
                        question = "What tolerance is acceptable for Fibonacci ratios in harmonics?",
                        options = listOf(
                            "±1%",
                            "±5%",
                            "±10%",
                            "±20%"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Harmonic patterns require precise ratios with maximum ±5% tolerance for pattern validity."
                    )
                )
            )
        )
    }

    /**
     * Lesson 19: Gartley & Bat Patterns
     */
    private fun getLesson19(): Lesson {
        return Lesson(
            id = 19,
            title = "Gartley & Bat Patterns",
            description = "Trade the most popular harmonic patterns with precise Fibonacci-based entry and exit strategies",
            content = """
                The Gartley and Bat patterns are the most commonly traded harmonic formations, offering high-probability reversal setups with exact entry points.
                
                **Gartley Pattern (222):**
                Named after H.M. Gartley who introduced it in 1935:
                - AB retracement: 61.8% of XA
                - BC retracement: 38.2%-88.6% of AB
                - CD extension: 127%-161.8% of BC
                - Point D: 78.6% retracement of XA
                - PRZ at convergence of CD and XA ratios
                - Most common harmonic pattern
                
                **Bullish Gartley:**
                - Forms after downtrend
                - X at high, A at low
                - Enter long at point D (PRZ)
                - Stop: Just beyond X point
                - Target 1: Point C
                - Target 2: Point A
                
                **Bearish Gartley:**
                - Forms after uptrend
                - X at low, A at high
                - Enter short at point D (PRZ)
                - Same targeting as bullish version
                
                **Bat Pattern:**
                More aggressive than Gartley with tighter retracement:
                - AB retracement: 38.2%-50% of XA
                - BC retracement: 38.2%-88.6% of AB
                - CD extension: 161.8%-261.8% of BC
                - Point D: 88.6% retracement of XA (critical)
                - Tighter stop loss than Gartley
                - Named for bat-wing appearance
                
                **Key Differences:**
                - Bat has shallower B point (38.2-50% vs 61.8%)
                - Bat D point at 88.6% vs Gartley 78.6%
                - Bat offers tighter risk/reward
                - Gartley more common, Bat more precise
                
                **Trading Rules:**
                - Must have exact Fibonacci ratios
                - Wait for candlestick confirmation at D
                - Enter only in PRZ convergence zone
                - Scale out at targets C and A
                - Move stop to breakeven after target 1
            """.trimIndent(),
            keyPoints = listOf(
                "Gartley: AB=61.8% XA, D=78.6% XA retracement",
                "Bat: AB=38.2-50% XA, D=88.6% XA retracement",
                "Both require precise Fibonacci ratios for validity",
                "Enter at point D with candlestick confirmation",
                "Target points C and A for profit taking"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Bullish Gartley",
                    description = "Most popular harmonic pattern",
                    identificationTips = listOf(
                        "Identify XA leg downward move",
                        "B retraces to 61.8% of XA",
                        "C retraces 38.2-88.6% of AB",
                        "D completes at 78.6% of XA",
                        "CD = 127-161.8% extension of BC",
                        "Enter long at D with bullish reversal candle"
                    )
                ),
                PatternExample(
                    patternName = "Bearish Bat",
                    description = "Precise harmonic with tight stop",
                    identificationTips = listOf(
                        "XA leg moves upward",
                        "B retraces 38.2-50% of XA (shallower)",
                        "C retraces 38.2-88.6% of AB",
                        "D must hit 88.6% of XA exactly",
                        "CD = 161.8-261.8% of BC",
                        "Very precise - offers best risk/reward"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "In a Gartley pattern, point B retraces what % of XA?",
                        options = listOf(
                            "38.2%",
                            "50%",
                            "61.8%",
                            "78.6%"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "In a Gartley pattern, point B retraces 61.8% (Golden Ratio) of the XA leg."
                    ),
                    Question(
                        question = "What is the critical D point retracement for a Bat pattern?",
                        options = listOf(
                            "61.8%",
                            "78.6%",
                            "88.6%",
                            "100%"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Bat patterns require point D to complete at exactly 88.6% retracement of XA - this is critical."
                    ),
                    Question(
                        question = "Which pattern offers a tighter stop loss?",
                        options = listOf(
                            "Gartley",
                            "Bat",
                            "Both equal",
                            "Neither has a stop"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Bat pattern offers tighter stop loss due to its deeper D point (88.6%) closer to the reversal zone."
                    ),
                    Question(
                        question = "What are the profit targets for harmonic patterns?",
                        options = listOf(
                            "Random levels",
                            "Points C and A",
                            "Only point X",
                            "No specific targets"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Standard harmonic targets are first to point C (partial exit) then point A (final target)."
                    )
                )
            )
        )
    }

    /**
     * Lesson 20: Butterfly & Crab Patterns
     */
    private fun getLesson20(): Lesson {
        return Lesson(
            id = 20,
            title = "Butterfly & Crab Patterns",
            description = "Master advanced harmonic patterns with extended price projections for extreme reversal opportunities",
            content = """
                Butterfly and Crab patterns are advanced harmonics featuring extended D points that project beyond point X, offering aggressive reversal trades with exceptional risk/reward ratios.
                
                **Butterfly Pattern:**
                Extended harmonic with D point beyond X:
                - AB retracement: 78.6% of XA
                - BC retracement: 38.2%-88.6% of AB
                - CD extension: 161.8%-224% of BC
                - Point D: 127%-161.8% extension of XA
                - D extends beyond X point (key characteristic)
                - Named for symmetrical wing appearance
                - Very reliable at trend extremes
                
                **Bullish Butterfly:**
                - Forms after extended downtrend
                - X at high, A at low point
                - D point drops below X (extreme oversold)
                - Enter long at D with reversal confirmation
                - Stop: 2-3% beyond D point
                - Target: Points C, A, then X
                
                **Bearish Butterfly:**
                - Forms after extended uptrend
                - X at low, A at high
                - D extends above X (extreme overbought)
                - Enter short at D PRZ
                - Targets back to C, A, X levels
                
                **Crab Pattern:**
                Most extreme harmonic pattern:
                - AB retracement: 38.2%-61.8% of XA
                - BC retracement: 38.2%-88.6% of AB
                - CD extension: 224%-361.8% of BC
                - Point D: 161.8% extension of XA (critical)
                - D significantly beyond X
                - Highest risk/reward of all harmonics
                - Named for crab claw appearance
                
                **Trading Crab Patterns:**
                - Most aggressive harmonic setup
                - Enter at exact 161.8% XA extension
                - Tight stop beyond D point
                - First target at 38.2% retracement
                - Can achieve 3:1+ risk/reward
                - Requires patience - forms rarely
            """.trimIndent(),
            keyPoints = listOf(
                "Butterfly: D at 127-161.8% extension of XA",
                "Crab: D at 161.8% extension of XA exactly",
                "Both patterns extend D beyond X point",
                "Offer exceptional risk/reward ratios",
                "Require precise Fibonacci measurements for validity"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Bullish Butterfly",
                    description = "Extended harmonic for extreme reversals",
                    identificationTips = listOf(
                        "AB = 78.6% retracement of XA",
                        "BC = 38.2-88.6% of AB",
                        "CD = 161.8-224% extension of BC",
                        "D completes at 127-161.8% of XA",
                        "D point extends below X",
                        "Enter with bullish reversal at D"
                    )
                ),
                PatternExample(
                    patternName = "Bearish Crab",
                    description = "Most aggressive harmonic pattern",
                    identificationTips = listOf(
                        "AB = 38.2-61.8% of XA",
                        "BC = 38.2-88.6% of AB",
                        "CD = 224-361.8% of BC",
                        "D must complete at 161.8% of XA",
                        "Extremely extended beyond X",
                        "Best risk/reward when perfect"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What distinguishes Butterfly and Crab from other harmonics?",
                        options = listOf(
                            "They have no point D",
                            "Point D extends beyond X",
                            "They only work on daily charts",
                            "They have no Fibonacci ratios"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Butterfly and Crab patterns are unique because point D extends beyond the X point, creating extreme setups."
                    ),
                    Question(
                        question = "What is the critical D point for a Crab pattern?",
                        options = listOf(
                            "78.6% of XA",
                            "127% of XA",
                            "161.8% of XA",
                            "224% of XA"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "The Crab pattern requires D to complete at exactly 161.8% extension of XA - this is the defining ratio."
                    ),
                    Question(
                        question = "Which pattern offers the best risk/reward ratio?",
                        options = listOf(
                            "Gartley",
                            "Bat",
                            "Butterfly",
                            "Crab"
                        ),
                        correctAnswerIndex = 3,
                        explanation = "The Crab pattern offers the best risk/reward ratio when properly formed, often 3:1 or better."
                    ),
                    Question(
                        question = "In a Butterfly pattern, AB retraces what % of XA?",
                        options = listOf(
                            "38.2%",
                            "61.8%",
                            "78.6%",
                            "88.6%"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "In a Butterfly pattern, the AB leg retraces 78.6% of the XA leg."
                    )
                )
            )
        )
    }

    /**
     * Lesson 21: Volume & Momentum Confirmation
     */
    private fun getLesson21(): Lesson {
        return Lesson(
            id = 21,
            title = "Volume & Momentum Confirmation",
            description = "Use volume analysis and momentum indicators to confirm pattern validity and filter false breakouts",
            content = """
                Volume and momentum are critical confirmation tools that separate profitable pattern trades from costly false breakouts. Never trade patterns without confirmation.
                
                **Volume Confirmation Principles:**
                
                **1. Volume Precedes Price**
                - Accumulation shows in rising volume before breakout
                - Smart money positions before retail notices
                - Increasing volume = stronger pattern
                - Decreasing volume = weakening pattern
                
                **2. Breakout Volume Rules**
                - Valid breakout needs 50%+ above average volume
                - Higher volume = higher probability success
                - Low volume breakout = likely false move
                - Volume surge confirms institutional participation
                
                **3. On-Balance Volume (OBV)**
                - Cumulative volume indicator
                - Rising OBV + rising price = healthy uptrend
                - Falling OBV + rising price = bearish divergence
                - Leads price changes by days or weeks
                - Confirms accumulation/distribution
                
                **Momentum Indicators:**
                
                **4. RSI (Relative Strength Index)**
                - Measures momentum from 0-100
                - Above 50 = bullish momentum
                - Below 50 = bearish momentum
                - Bullish pattern + RSI > 50 = high probability
                - Divergence warns of reversal
                
                **5. MACD (Moving Average Convergence Divergence)**
                - Trend following momentum indicator
                - MACD line crosses signal line = momentum shift
                - Histogram shows momentum strength
                - Bullish crossover confirms pattern breakout
                - Zero line cross confirms trend change
                
                **6. Momentum Divergence**
                - Price makes new high, momentum doesn't = bearish
                - Price makes new low, momentum doesn't = bullish
                - Early warning signal for reversals
                - Combine with reversal patterns for best trades
                
                **Confirmation Checklist:**
                - Pattern structure valid ✓
                - Volume increasing into breakout ✓
                - Momentum indicator confirms direction ✓
                - No bearish divergences ✓
                - Risk/reward minimum 1:2 ✓
            """.trimIndent(),
            keyPoints = listOf(
                "Breakout needs 50%+ above average volume",
                "OBV confirms accumulation or distribution",
                "RSI above 50 confirms bullish momentum",
                "MACD crossovers validate pattern breakouts",
                "Momentum divergence warns of reversals"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Volume-Confirmed Breakout",
                    description = "Triangle breakout with volume surge",
                    identificationTips = listOf(
                        "Ascending triangle forms over 6 weeks",
                        "Volume decreases during consolidation",
                        "Breakout with 2x average volume",
                        "OBV trending up throughout pattern",
                        "RSI above 50 and rising",
                        "High probability continuation"
                    )
                ),
                PatternExample(
                    patternName = "Bearish Divergence Warning",
                    description = "Price high but momentum failing",
                    identificationTips = listOf(
                        "Price makes new high",
                        "RSI makes lower high (divergence)",
                        "MACD histogram declining",
                        "Volume decreasing on rally",
                        "Warning sign for reversal",
                        "Prepare to exit long positions"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What volume increase confirms a valid breakout?",
                        options = listOf(
                            "Any increase",
                            "10% above average",
                            "50% above average",
                            "Volume doesn't matter"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "A valid breakout should have at least 50% above average volume to confirm institutional participation."
                    ),
                    Question(
                        question = "What does bearish divergence indicate?",
                        options = listOf(
                            "Strong uptrend continuation",
                            "Price high but weakening momentum",
                            "Random market noise",
                            "Volume is increasing"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Bearish divergence occurs when price makes new highs but momentum indicators fail to confirm, warning of reversal."
                    ),
                    Question(
                        question = "RSI above 50 indicates:",
                        options = listOf(
                            "Bearish momentum",
                            "Neutral market",
                            "Bullish momentum",
                            "Overbought condition"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "RSI above 50 indicates bullish momentum, confirming upward price movements and patterns."
                    ),
                    Question(
                        question = "What does OBV measure?",
                        options = listOf(
                            "Price only",
                            "Cumulative volume flow",
                            "Moving averages",
                            "Candlestick patterns"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "OBV (On-Balance Volume) measures cumulative volume flow, confirming accumulation or distribution."
                    )
                )
            )
        )
    }

    /**
     * Lesson 22: Fibonacci Integration
     */
    private fun getLesson22(): Lesson {
        return Lesson(
            id = 22,
            title = "Fibonacci Integration",
            description = "Combine Fibonacci retracements and extensions with patterns for precise entry, exit, and target levels",
            content = """
                Fibonacci ratios enhance pattern trading by providing mathematical precision for entries, stops, and targets. Integration of Fibonacci with patterns creates high-probability setups.
                
                **Fibonacci Retracement Levels:**
                - 23.6% - Shallow retracement
                - 38.2% - Moderate retracement
                - 50% - Psychological midpoint
                - 61.8% - Golden Ratio (most important)
                - 78.6% - Deep retracement
                
                **Using Retracements with Patterns:**
                
                **Triangle Patterns:**
                - Measure from breakout point
                - First target: 38.2% of prior trend
                - Second target: 61.8% extension
                - Final target: 100% measured move
                
                **Head & Shoulders:**
                - Measure head to neckline
                - Target 1: 61.8% of that distance
                - Target 2: 100% measured move
                - Target 3: 161.8% extension (rare)
                
                **Fibonacci Extension Levels:**
                - 127.2% - First extension target
                - 161.8% - Golden Ratio extension
                - 200% - Double the move
                - 261.8% - Advanced target
                
                **Pattern Entry Refinement:**
                
                **Flag Patterns:**
                - Flag pullback to 38.2-50% of pole
                - Entry at 61.8% retracement max
                - Deeper pullback invalidates pattern
                - Extension targets at 127% and 161.8%
                
                **Wedge Reversals:**
                - Entry at wedge breakout
                - Target 1: 38.2% retracement back
                - Target 2: 61.8% retracement
                - Final target: Pattern origin point
                
                **Confluence Zones:**
                Multiple Fibonacci levels + pattern = high probability
                - Pattern support + 61.8% retracement = strong buy zone
                - Pattern resistance + 161.8% extension = take profit
                - 3+ levels converging = institutional zone
                
                **Professional Fibonacci Trading:**
                - Always measure from significant swing points
                - Use multiple timeframe Fibonacci levels
                - Look for confluence with pattern levels
                - Adjust stop loss to Fibonacci levels
                - Scale out at each Fibonacci target
            """.trimIndent(),
            keyPoints = listOf(
                "61.8% Golden Ratio most reliable retracement level",
                "Fibonacci extensions provide precise profit targets",
                "Confluence of Fibonacci + pattern levels = high probability",
                "Use multiple timeframes for stronger zones",
                "Scale out at Fibonacci targets - don't exit all at once"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Fibonacci Flag Entry",
                    description = "Flag pattern with Fibonacci precision",
                    identificationTips = listOf(
                        "Measure flagpole from breakout to high",
                        "Flag pullback to 38.2-50% ideal entry",
                        "Entry confirmed at 61.8% retracement max",
                        "Stop below flag at 78.6% level",
                        "Target 1: 127% extension of pole",
                        "Target 2: 161.8% extension"
                    )
                ),
                PatternExample(
                    patternName = "Confluence Zone Trading",
                    description = "Multiple Fibonacci levels converging",
                    identificationTips = listOf(
                        "Pattern support at $100",
                        "61.8% retracement at $100.50",
                        "200-day MA at $99.75",
                        "All levels converge $99.75-$100.50",
                        "High probability reversal zone",
                        "Tight stop below zone, strong R:R"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "Which Fibonacci retracement is the Golden Ratio?",
                        options = listOf(
                            "38.2%",
                            "50%",
                            "61.8%",
                            "78.6%"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "61.8% is the Golden Ratio, the most reliable and watched Fibonacci retracement level."
                    ),
                    Question(
                        question = "What is a confluence zone?",
                        options = listOf(
                            "Random price level",
                            "Multiple Fibonacci and pattern levels converging",
                            "Single support line",
                            "Volume indicator"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Confluence zone is where multiple Fibonacci levels, pattern levels, or other support/resistance converge."
                    ),
                    Question(
                        question = "For a flag pattern, what is the ideal pullback depth?",
                        options = listOf(
                            "23.6-38.2%",
                            "38.2-50%",
                            "61.8-78.6%",
                            "Over 78.6%"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Ideal flag pullback is 38.2-50% of the pole; deeper pullbacks may invalidate the pattern."
                    ),
                    Question(
                        question = "What is the first Fibonacci extension target?",
                        options = listOf(
                            "100%",
                            "127.2%",
                            "161.8%",
                            "200%"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "127.2% is typically the first Fibonacci extension target beyond the 100% measured move."
                    )
                )
            )
        )
    }

    /**
     * Lesson 23: Multi-Timeframe Alignment
     */
    private fun getLesson23(): Lesson {
        return Lesson(
            id = 23,
            title = "Multi-Timeframe Alignment",
            description = "Analyze patterns across multiple timeframes to find highest-probability trades with optimal timing",
            content = """
                Multi-timeframe analysis is the difference between amateur and professional pattern trading. The best trades occur when patterns align across multiple timeframes.
                
                **Timeframe Hierarchy:**
                
                **Higher Timeframe (HTF) - Weekly/Daily:**
                - Determines overall trend direction
                - Identifies major support/resistance
                - Provides context for all trades
                - Trade WITH the HTF trend only
                - HTF patterns = major moves
                
                **Medium Timeframe (MTF) - 4H/1H:**
                - Entry pattern timeframe
                - Confirms HTF direction
                - Identifies setup and structure
                - Best risk/reward on this timeframe
                - Patterns here = swing trades
                
                **Lower Timeframe (LTF) - 15m/5m:**
                - Precise entry timing
                - Tight stop placement
                - Entry confirmation signals
                - Exit management
                - Patterns here = intraday scalps
                
                **Top-Down Analysis Process:**
                
                **Step 1: Weekly Chart**
                - Identify overall trend (up/down/sideways)
                - Mark major support/resistance zones
                - Note any HTF patterns forming
                - Determine bias: bullish, bearish, or neutral
                
                **Step 2: Daily Chart**
                - Confirm weekly trend direction
                - Identify MTF pattern setups
                - Mark intermediate levels
                - Look for pattern formation
                
                **Step 3: 4-Hour Chart**
                - Find specific entry pattern
                - Confirm alignment with higher TFs
                - Identify exact entry zone
                - Set profit targets
                
                **Step 4: 1-Hour Chart**
                - Time precise entry
                - Place tight stop loss
                - Confirm with candlestick pattern
                - Execute trade
                
                **Alignment Examples:**
                
                **Perfect Bullish Alignment:**
                - Weekly: Uptrend, inverse H&S forming
                - Daily: Ascending triangle breakout
                - 4H: Bull flag consolidation
                - 1H: Bullish engulfing at flag support
                - Result: Very high probability long trade
                
                **Conflicting Signals (Avoid):**
                - Weekly: Downtrend
                - Daily: Bullish pattern
                - Result: Counter-trend trade = high risk
                - Action: Wait for alignment or skip
            """.trimIndent(),
            keyPoints = listOf(
                "Always analyze higher timeframes first for context",
                "Best trades have pattern alignment across all timeframes",
                "HTF determines trend, LTF provides entry timing",
                "Never trade against higher timeframe trend",
                "Use LTF for precise entries, HTF for targets"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Perfect Multi-TF Alignment",
                    description = "All timeframes confirm bullish direction",
                    identificationTips = listOf(
                        "Weekly: Strong uptrend, no resistance above",
                        "Daily: Cup & Handle pattern forming",
                        "4H: Ascending triangle breakout",
                        "1H: Bullish flag at support",
                        "Entry: 1H flag breakout",
                        "Targets: Daily cup depth = major profit"
                    )
                ),
                PatternExample(
                    patternName = "Top-Down Trade Setup",
                    description = "Professional analysis workflow",
                    identificationTips = listOf(
                        "Start weekly: Identify major trend",
                        "Daily: Find pattern in trend direction",
                        "4H: Wait for entry pattern formation",
                        "1H: Execute with confirmation",
                        "Stop: Below 1H pattern low",
                        "Target: Daily pattern objective"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "Which timeframe should you analyze first?",
                        options = listOf(
                            "1-hour chart",
                            "15-minute chart",
                            "Weekly/Daily (highest)",
                            "Any order is fine"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Always start with the highest timeframe (weekly/daily) to determine overall trend and context."
                    ),
                    Question(
                        question = "What happens when daily shows bullish but weekly shows bearish?",
                        options = listOf(
                            "Take the daily bullish trade",
                            "Avoid the trade - conflicting signals",
                            "Short the market",
                            "Trade both directions"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "When timeframes conflict, avoid the trade. Best trades have all timeframes aligned in same direction."
                    ),
                    Question(
                        question = "What is the lower timeframe (1H, 15m) best used for?",
                        options = listOf(
                            "Determining overall trend",
                            "Precise entry timing and stops",
                            "Long-term targets",
                            "Ignoring higher timeframes"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Lower timeframes are used for precise entry timing, tight stop placement, and execution."
                    ),
                    Question(
                        question = "The highest probability trades occur when:",
                        options = listOf(
                            "Only one timeframe shows a pattern",
                            "Timeframes conflict with each other",
                            "Multiple timeframes align in same direction",
                            "You ignore higher timeframes"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Highest probability trades occur when multiple timeframes all align and confirm the same direction."
                    )
                )
            )
        )
    }

    /**
     * Lesson 24: Pattern Combinations & Playbooks
     */
    private fun getLesson24(): Lesson {
        return Lesson(
            id = 24,
            title = "Pattern Combinations & Playbooks",
            description = "Create systematic trading playbooks by combining multiple patterns for repeatable high-probability setups",
            content = """
                Professional traders don't trade random patterns - they use proven playbooks combining multiple confirmations. Building your playbook creates consistent, repeatable edge.
                
                **Building Trading Playbooks:**
                
                **Playbook Structure:**
                1. Market condition (trend, range, volatile)
                2. Primary pattern requirement
                3. Secondary confirmation pattern
                4. Volume requirement
                5. Indicator confirmation
                6. Entry trigger
                7. Stop placement rule
                8. Target and exit strategy
                
                **Example Playbook: "Trend Continuation Stack"**
                
                **Market Condition:** Strong uptrend (price above 50 & 200 MA)
                
                **Primary Pattern:** Bull flag on daily chart
                - Flagpole minimum 10% gain
                - Flag pullback 38.2-50% max
                - Duration: 1-3 weeks
                
                **Secondary Confirmation:**
                - 4H ascending triangle within flag
                - OR bullish engulfing at flag support
                
                **Volume Requirements:**
                - Decreasing during flag formation
                - 50%+ surge on breakout
                - OBV trending upward
                
                **Indicator Stack:**
                - RSI > 50 and rising
                - MACD positive and above signal
                - 20 MA > 50 MA > 200 MA
                
                **Entry Trigger:**
                - Break above flag resistance
                - 1H candlestick close above flag
                - Confirm with volume spike
                
                **Stop Loss:**
                - Below flag low
                - OR below 20 MA
                - Maximum 2% account risk
                
                **Targets:**
                - Target 1: Flagpole length (50% exit)
                - Target 2: 1.5x flagpole (30% exit)
                - Target 3: Trail remaining (20%)
                
                **Additional Playbooks:**
                
                **"Reversal Confluence" Playbook:**
                - HTF downtrend exhaustion
                - Falling wedge + bullish divergence
                - Inverse H&S on daily
                - Hammer candle at support
                - Volume climax sell-off
                - Entry: Neckline break
                
                **"Range Breakout" Playbook:**
                - Sideways consolidation 4+ weeks
                - Symmetrical triangle forming
                - Volume decreasing into apex
                - Breakout with gap up
                - Entry: Gap confirmation
                
                **Playbook Development Process:**
                1. Identify recurring winning setups
                2. Document exact conditions
                3. Backtest on historical charts
                4. Paper trade to refine
                5. Track statistics and refine
                6. Live trade with small size
                7. Scale up proven playbooks
            """.trimIndent(),
            keyPoints = listOf(
                "Playbooks combine multiple patterns and confirmations",
                "Document exact entry, exit, and risk rules",
                "Backtest playbooks before live trading",
                "Track statistics for each playbook",
                "Refine and improve successful playbooks over time"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Trend Continuation Stack",
                    description = "Multi-pattern confirmation playbook",
                    identificationTips = listOf(
                        "Daily bull flag in strong uptrend",
                        "4H triangle breakout within flag",
                        "1H bullish engulfing at support",
                        "Volume surge on breakout",
                        "RSI > 50, MACD bullish",
                        "Entry: Flag breakout with volume"
                    )
                ),
                PatternExample(
                    patternName = "Bottom Reversal Playbook",
                    description = "Multiple confirmations at major bottom",
                    identificationTips = listOf(
                        "Weekly downtrend exhaustion",
                        "Daily inverse H&S forming",
                        "Bullish RSI divergence",
                        "Volume climax on final low",
                        "Hammer at support level",
                        "Entry: Neckline break with volume"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What is a trading playbook?",
                        options = listOf(
                            "Random pattern collection",
                            "Systematic combination of patterns with exact rules",
                            "Single indicator strategy",
                            "Gut feeling trades"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "A trading playbook is a systematic combination of patterns and confirmations with exact, documented rules."
                    ),
                    Question(
                        question = "Before live trading a new playbook, you should:",
                        options = listOf(
                            "Risk all your capital immediately",
                            "Skip testing and jump in",
                            "Backtest and paper trade first",
                            "Only trade on gut feel"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Always backtest on historical data and paper trade new playbooks before risking real capital."
                    ),
                    Question(
                        question = "In the Trend Continuation playbook, what confirms entry?",
                        options = listOf(
                            "Any price movement",
                            "Flag breakout with 50%+ volume surge",
                            "Random timing",
                            "News headlines"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Entry is confirmed by flag breakout accompanied by 50%+ above average volume surge."
                    ),
                    Question(
                        question = "Why combine multiple patterns in a playbook?",
                        options = listOf(
                            "To make trading complex",
                            "To increase confirmation and probability",
                            "To confuse yourself",
                            "No reason"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Combining multiple patterns increases confirmation and dramatically improves trade probability."
                    )
                )
            )
        )
    }

    /**
     * Lesson 25: Psychology, Risk & Advanced Execution
     */
    private fun getLesson25(): Lesson {
        return Lesson(
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
    }

    /**
     * Save lesson progress
     */
    suspend fun saveLessonProgress(context: Context, progress: LessonProgress) = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "education_progress.json")
        val allProgress = loadAllProgress(context).toMutableList()
        allProgress.removeIf { it.lessonId == progress.lessonId }
        allProgress.add(progress)
        file.writeText(allProgress.joinToString("\n") { 
            "${it.lessonId},${it.completed},${it.quizScore},${it.bonusHighlightsEarned}"
        })
    }

    /**
     * Load all lesson progress
     */
    suspend fun loadAllProgress(context: Context): List<LessonProgress> = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "education_progress.json")
        if (!file.exists()) return@withContext emptyList()
        
        file.readLines().mapNotNull { line ->
            val parts = line.split(",")
            if (parts.size == 4) {
                LessonProgress(
                    lessonId = parts[0].toInt(),
                    completed = parts[1].toBoolean(),
                    quizScore = parts[2].toInt(),
                    bonusHighlightsEarned = parts[3].toInt()
                )
            } else null
        }
    }

    /**
     * Check if eligible for certificate (70%+ average)
     */
    fun isEligibleForCertificate(allProgress: List<LessonProgress>): Boolean {
        if (allProgress.size < 25) return false
        val avgScore = allProgress.map { it.quizScore }.average()
        return avgScore >= 70.0
    }

    /**
     * Generate certificate
     */
    fun generateCertificate(userName: String, allProgress: List<LessonProgress>): Certificate {
        return Certificate(
            userName = userName,
            completionDate = System.currentTimeMillis(),
            averageScore = allProgress.map { it.quizScore }.average().toInt(),
            totalLessons = allProgress.size
        )
    }
}
