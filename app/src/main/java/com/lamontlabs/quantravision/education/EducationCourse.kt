package com.lamontlabs.quantravision.education

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Extended 10-lesson education course (PLANNED - not currently active)
 * Currently using InteractiveCourse.kt with 3 lessons
 * This file contains full 10-lesson curriculum for future expansion
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
     * All 10 lessons - PLANNED (not currently active)
     * Active course uses InteractiveCourse.kt with 3 lessons
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
            getLesson10()
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
                Chart patterns are recognizable formations on price charts that traders use to predict future price movements.
                
                **Why Patterns Work:**
                - They represent collective psychology of market participants
                - Historical repetition creates predictable outcomes
                - They provide visual context for price action
                
                **Pattern Categories:**
                1. **Continuation Patterns** - Suggest trend will continue
                2. **Reversal Patterns** - Indicate potential trend change
                3. **Bilateral Patterns** - Can break either direction
                
                **Key Concepts:**
                - Support: Price level where buying pressure prevents further decline
                - Resistance: Price level where selling pressure prevents further rise
                - Breakout: Price moves beyond established support/resistance
                - Volume: Confirms pattern validity (higher volume = stronger signal)
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
                Candlestick patterns are the foundation of technical analysis, originating from 18th century Japanese rice traders.
                
                **Anatomy of a Candlestick:**
                - Body: Range between open and close
                - Wick/Shadow: High and low extremes
                - Color: Green (bullish) / Red (bearish)
                
                **Key Single-Candle Patterns:**
                
                **1. Doji** - Indecision
                - Open = Close (tiny body)
                - Long wicks indicate rejection
                - Signals potential reversal at extremes
                
                **2. Hammer** - Bullish Reversal
                - Small body at top
                - Long lower wick (2x body)
                - Appears after downtrend
                
                **3. Shooting Star** - Bearish Reversal
                - Small body at bottom
                - Long upper wick
                - Appears after uptrend
                
                **Multi-Candle Patterns:**
                
                **4. Engulfing** - Strong Reversal
                - Second candle completely engulfs first
                - Bullish engulfing: Red then green
                - Bearish engulfing: Green then red
                
                **5. Morning/Evening Star** - Major Reversal
                - Three-candle formation
                - Gap, small body, gap opposite direction
                - High reliability pattern
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
                Understanding trend patterns is critical for successful trading. These patterns tell you when to stay in a trend and when to exit.
                
                **Reversal Patterns:**
                
                **1. Head and Shoulders** (Bearish Reversal)
                - Three peaks: left shoulder, head (highest), right shoulder
                - Neckline connects two troughs
                - Target: Distance from head to neckline, projected down
                - Volume: Decreases on head, increases on breakdown
                
                **2. Inverse Head and Shoulders** (Bullish Reversal)
                - Mirror image of H&S
                - Three troughs with middle being lowest
                - Breakout above neckline confirms reversal
                
                **3. Double Top/Bottom**
                - Two attempts to break resistance/support fail
                - "M" shape (top) or "W" shape (bottom)
                - Second peak/trough at similar level to first
                - Strong reversal signal
                
                **Continuation Patterns:**
                
                **4. Flags** - Brief consolidation in strong trend
                - Rectangular shape against trend direction
                - Forms after sharp move (flagpole)
                - Breakout continues original trend
                
                **5. Pennants** - Similar to flags but triangular
                - Converging trendlines
                - Forms quickly (1-3 weeks)
                - High probability continuation
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
                Triangle patterns are among the most common continuation patterns, forming as price consolidates before the next move.
                
                **Types of Triangles:**
                
                **1. Ascending Triangle** (Bullish)
                - Flat top resistance
                - Rising bottom support
                - Typically breaks upward
                - Shows buyers getting more aggressive
                
                **2. Descending Triangle** (Bearish)
                - Flat bottom support
                - Declining top resistance
                - Usually breaks downward
                - Sellers becoming more aggressive
                
                **3. Symmetrical Triangle** (Bilateral)
                - Converging trendlines
                - Lower highs AND higher lows
                - Can break either direction
                - Direction depends on prior trend
                
                **Trading Triangles:**
                - Wait for breakout confirmation
                - Volume should increase on breakout
                - Target = height of triangle at widest point
                - False breakouts common - wait for close beyond line
                - Best entry: breakout + retest of broken line
                
                **Common Mistakes:**
                - Trading before breakout (caught in whipsaw)
                - Ignoring volume (weak breakouts fail)
                - Wrong direction trade (trade with prior trend)
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
                Wedge patterns are powerful reversal indicators that often catch traders by surprise. Unlike triangles, wedges signal exhaustion.
                
                **Rising Wedge** (Bearish Reversal)
                - Both support and resistance slope upward
                - Narrowing price action
                - Appears at top of uptrend
                - Signals buyers losing steam
                - Volume decreases as pattern forms
                - Breaks downward 
                
                **Falling Wedge** (Bullish Reversal)
                - Both lines slope downward
                - Price consolidates in narrowing range
                - Forms at bottom of downtrend
                - Shows sellers exhausting
                - Volume contracts then expands
                - Breaks upward
                
                **Key Differences from Triangles:**
                - Wedges are reversal patterns
                - Both lines slope same direction
                - Wedges show exhaustion, not continuation
                - Typically steeper than triangles
                
                **Trading Wedges:**
                - Entry: On breakout opposite to wedge slope
                - Stop: Beyond the opposite trendline
                - Target: Start of wedge formation
                - Confirmation: Volume spike on breakout
                
                **Psychology:**
                - Rising wedge: Bulls weaker with each new high
                - Falling wedge: Bears weaker with each new low
                - Narrowing range shows decreasing conviction
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
                Harmonic patterns use Fibonacci ratios to identify precise reversal points. They're highly accurate but require exact measurements.
                
                **What Are Harmonic Patterns?**
                - Based on natural Fibonacci ratios (0.382, 0.618, 1.618, etc.)
                - Geometric price structures
                - Predict reversal zones (PRZ)
                - Require specific ratio relationships
                
                **Key Fibonacci Ratios:**
                - 0.382 (38.2% retracement)
                - 0.618 (61.8% retracement - Golden Ratio)
                - 0.786 (78.6% retracement)
                - 1.27 (127% extension)
                - 1.618 (161.8% extension)
                
                **Common Harmonic Patterns:**
                
                **1. Gartley Pattern**
                - Most common harmonic
                - 5-point structure: X-A-B-C-D
                - AB = 61.8% of XA
                - BC = 38.2-88.6% of AB
                - CD = 127-161.8% of BC
                - D is PRZ (reversal point)
                
                **2. Bat Pattern**
                - Similar to Gartley
                - AB = 38.2-50% of XA
                - CD = 88.6% of XA
                - Tighter retracement
                
                **3. Butterfly Pattern**
                - Extended structure
                - AB = 78.6% of XA
                - CD = 127-261.8% of BC
                - D extends beyond X
                
                **Trading Harmonics:**
                - Wait for pattern completion
                - Enter at Point D (PRZ)
                - Stop beyond X point
                - Target Point C or A
                - Combine with candlestick confirmation
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
                Volume is the fuel that drives price movements. Without volume analysis, pattern trading is incomplete.
                
                **Why Volume Matters:**
                - Confirms pattern validity
                - Shows conviction behind moves
                - Identifies accumulation/distribution
                - Spots reversals early
                - Validates breakouts
                
                **Volume Principles:**
                
                **1. Volume Precedes Price**
                - Smart money accumulates before breakouts
                - Rising volume on weak price = accumulation
                - Falling volume on rally = weak momentum
                
                **2. Breakout Confirmation**
                - Valid breakout = 50%+ above average volume
                - Low volume breakout = likely false move
                - Volume surge = institutional participation
                
                **Volume Patterns:**
                
                **3. Volume Climax**
                - Extreme volume spike
                - Often marks exhaustion
                - Signals potential reversal
                - Examples: Capitulation (bottom), Euphoria (top)
                
                **4. Volume Divergence**
                - Price makes new high/low
                - Volume decreases
                - Shows weakening trend
                - Reversal likely
                
                **5. On-Balance Volume (OBV)**
                - Cumulative volume indicator
                - Rising OBV + rising price = healthy trend
                - Divergence = warning sign
                
                **6. Volume Profile**
                - Shows volume at each price level
                - High volume areas = support/resistance
                - Low volume areas = price moves quickly through
                
                **Trading with Volume:**
                - Enter when volume confirms pattern
                - Avoid patterns with weak volume
                - Look for volume spikes at key levels
                - Use volume divergence for early exits
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
        if (allProgress.size < 10) return false
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
