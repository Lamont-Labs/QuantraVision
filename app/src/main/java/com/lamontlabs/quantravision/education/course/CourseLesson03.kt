package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.*


val courseLesson03 = Lesson(
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
