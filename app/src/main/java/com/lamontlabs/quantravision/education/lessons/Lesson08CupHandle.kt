package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson08CupHandle = Lesson(
            id = 8,
            title = "Cup and Handle Pattern",
            category = "Continuation Patterns",
            duration = "8 min",
            content = """
                # Cup and Handle Pattern
                
                ## Overview
                
                The Cup and Handle is a bullish continuation pattern that resembles a tea cup when viewed on a chart. It consists of two parts: a rounded bottom (the cup) followed by a small consolidation (the handle). This pattern indicates strong accumulation and often leads to significant breakouts.
                
                ## Structure
                
                ```
                Prior   ___           ___  Breakout
                Uptrend    \         /   |---→
                            \       /    |
                             \_____/     |
                              Cup     Handle
                
                    ← 7-65 weeks → ← 1-4 weeks →
                ```
                
                ## The Cup
                
                ### Characteristics:
                - **U-Shape**: Rounded bottom, not V-shaped
                - **Depth**: 12-33% retracement of prior advance
                - **Duration**: 7 weeks to 65 weeks (1-14 months)
                - **Volume**: Declining on left side, rising on right
                - **Symmetry**: Relatively equal sides
                
                ### Psychology:
                1. **Left Side**: Profit-taking after rally
                2. **Bottom**: Selling exhaustion, accumulation
                3. **Right Side**: Renewed buying interest
                4. **Formation**: Smart money accumulates
                
                ## The Handle
                
                ### Characteristics:
                - **Pullback**: 10-15% retracement typical
                - **Duration**: 1-4 weeks (much shorter than cup)
                - **Shape**: Downward drift or small flag
                - **Position**: Upper half of cup
                - **Volume**: Declining during formation
                
                ### Psychology:
                - Final shakeout of weak hands
                - Last profit-taking opportunity
                - Preparation for breakout
                - Tight consolidation shows strength
                
                ## Ideal Cup and Handle
                
                ✅ **Perfect Pattern Checklist**:
                
                1. **Prior Trend**: Established uptrend (30%+ gain)
                2. **Cup Depth**: 12-33% correction
                3. **Cup Shape**: Smooth, rounded bottom
                4. **Cup Duration**: 7-65 weeks
                5. **Handle Depth**: Less than 50% of cup depth
                6. **Handle Duration**: 1-4 weeks (shorter than cup)
                7. **Volume**: Declining in cup and handle
                8. **Breakout Volume**: 40-50% above average
                
                ## Trading the Pattern
                
                ### Entry Points:
                
                **Aggressive Entry**:
                - During handle formation
                - Buy near handle support
                - Higher risk, better price
                
                **Conservative Entry**:
                - Breakout above handle resistance
                - Wait for close above prior high
                - Lower risk, confirmation present
                
                **Retest Entry**:
                - After breakout, wait for pullback
                - Enter when support holds
                - Best risk/reward ratio
                
                ### Stop Loss Placement:
                
                1. **Tight**: Below handle low
                2. **Moderate**: Below cup right side low
                3. **Wide**: Below cup bottom
                
                Choose based on risk tolerance and timeframe.
                
                ### Price Targets:
                
                **Minimum Target**:
                - Depth of cup added to breakout point
                - Most conservative estimate
                
                **Extended Target**:
                - 20-30% above breakout (typical)
                - Previous resistance levels
                - Fibonacci extensions
                
                ## Volume Analysis
                
                ✅ **Classic Volume Pattern**:
                
                **Cup Left Side**: 
                - High volume at top
                - Declining as price falls
                
                **Cup Bottom**: 
                - Lowest volume (selling exhaustion)
                - Accumulation zone
                
                **Cup Right Side**: 
                - Gradually increasing
                - Building momentum
                
                **Handle**: 
                - Low, declining volume
                - Tight, orderly consolidation
                
                **Breakout**: 
                - Massive volume spike (50%+ above average)
                - Strong conviction
                
                ## Pattern Variations
                
                ### Cup with Handle:
                - Standard bullish pattern
                - Most reliable form
                
                ### Cup without Handle:
                - Less reliable
                - Immediate breakout
                - Higher failure rate
                
                ### High Handle:
                - Handle in upper third of cup
                - Very bullish
                - Shows strength
                
                ### Low Handle:
                - Handle in lower half
                - Weaker pattern
                - Higher risk
                
                ### Inverted Cup and Handle:
                - Bearish version (rare)
                - Upside-down structure
                - Distribution pattern
                
                ## Common Mistakes
                
                ❌ V-shaped bottom (too sharp = weak)
                ❌ Cup too deep (>50% = reversal pattern)
                ❌ Handle too long (>4 weeks = weakness)
                ❌ Handle too deep (>50% cup = failure risk)
                ❌ No volume on breakout (false signal)
                ❌ Trading before handle forms
                
                ## Success Factors
                
                ✅ **High Probability Setup**:
                - Strong uptrend before cup
                - Smooth, rounded cup (U-shape)
                - Proper depth (12-33%)
                - Short handle (1-4 weeks)
                - Declining volume throughout
                - Volume surge on breakout (50%+)
                - Breakout to new highs
                
                ## Historical Context
                
                - Developed by William O'Neil
                - Used to identify major winners
                - Common in leading stocks
                - Strong track record (60-70% success)
                - Best in bull markets
                
                ## Pro Tips
                
                ✅ Look for this in market leaders
                ✅ Combine with fundamental strength
                ✅ Best in stage 2 uptrends
                ✅ Patience - let full pattern develop
                ✅ Volume confirmation is critical
                ✅ Use multiple timeframes
                ✅ Handle should be tight and orderly
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the ideal depth range for a Cup in the Cup and Handle pattern?",
                        options = listOf("5-10%", "12-33%", "50-70%", "Over 80%"),
                        correctAnswer = 1,
                        explanation = "The ideal cup depth is 12-33% retracement of the prior advance. Shallower may not be significant enough, deeper than 50% suggests a reversal pattern rather than continuation."
                    ),
                    QuizQuestion(
                        question = "How long should the handle typically form compared to the cup?",
                        options = listOf("Same duration", "Much shorter (1-4 weeks)", "Much longer", "Duration doesn't matter"),
                        correctAnswer = 1,
                        explanation = "The handle should be much shorter than the cup, typically forming over 1-4 weeks while the cup takes 7-65 weeks. A handle that's too long indicates weakness."
                    ),
                    QuizQuestion(
                        question = "What volume pattern confirms a valid Cup and Handle breakout?",
                        options = listOf("Low volume throughout", "40-50% above average volume on breakout", "Constant volume", "Volume doesn't matter"),
                        correctAnswer = 1,
                        explanation = "A valid breakout requires a significant volume increase, typically 40-50% or more above average, confirming strong buying conviction and reducing false breakout risk."
                    )
                )
            )
        )
