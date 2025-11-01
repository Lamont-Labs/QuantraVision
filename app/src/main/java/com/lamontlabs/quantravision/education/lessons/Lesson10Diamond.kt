package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson10Diamond = Lesson(
            id = 10,
            title = "Diamond Pattern",
            category = "Reversal Patterns",
            duration = "7 min",
            content = """
                # Diamond Pattern
                
                ## Overview
                
                The Diamond pattern is a rare but reliable reversal pattern that combines elements of triangles and head-and-shoulders formations. It appears at market tops (Diamond Top) or bottoms (Diamond Bottom), signaling potential trend reversal. The pattern resembles a diamond or rhombus shape.
                
                ## Diamond Top (Bearish)
                
                ### Structure:
                ```
                        Left  Peak  Right
                         \    /\    /
                          \  /  \  /
                           \/    \/
                           /\    /\
                          /  \  /  \
                         /    \/    \
                    Broadening  Narrowing
                ```
                
                ### Formation:
                
                1. **Broadening Phase** (Left Half):
                   - Expanding price swings
                   - Diverging trendlines
                   - Increasing volatility
                   - Higher highs, lower lows
                
                2. **Narrowing Phase** (Right Half):
                   - Converging price swings
                   - Compressing trendlines
                   - Decreasing volatility
                   - Lower highs, higher lows
                
                3. **Breakdown**:
                   - Price breaks lower trendline
                   - Volume increases
                   - Bearish reversal confirmed
                
                ### Characteristics:
                - **Duration**: 6-12 weeks typical
                - **Rarity**: Uncommon pattern
                - **Location**: Market tops
                - **Volume**: High at extremes, low in middle
                - **Reliability**: High when volume confirms
                
                ## Diamond Bottom (Bullish)
                
                ### Structure:
                ```
                    Broadening  Narrowing
                         \    /\    /
                          \  /  \  /
                           \/    \/
                           /\    /\
                          /  \  /  \
                         /    \/    \
                        Left Trough Right
                ```
                
                ### Formation:
                - Same structure as top, inverted
                - Appears at market bottoms
                - Less common than Diamond Top
                - Bullish reversal signal
                
                ### Characteristics:
                - Expanding then contracting volatility
                - Volume increases on breakout
                - Strong reversal when confirmed
                
                ## Trading the Diamond Top
                
                ### Entry Points:
                
                **Aggressive**:
                - Short at resistance touches
                - During right half formation
                - Higher risk approach
                
                **Conservative**:
                - Wait for breakdown below support
                - Confirm with volume
                - Enter on retest of broken support
                
                ### Stop Loss:
                - Above most recent swing high
                - Or above resistance line
                - Consider volatility (wider stops needed)
                
                ### Price Target:
                - Measure vertical height of diamond
                - Project that distance from breakdown point
                - Alternative: 50-70% of prior uptrend
                
                ## Trading the Diamond Bottom
                
                ### Entry Points:
                
                **Aggressive**:
                - Buy at support touches
                - During right half compression
                - Requires skill and timing
                
                **Conservative**:
                - Wait for upside breakout
                - Volume confirmation essential
                - Retest entry for better risk/reward
                
                ### Stop Loss:
                - Below recent swing low
                - Or below support line
                - Account for volatility
                
                ### Price Target:
                - Measure diamond height
                - Add to breakout point
                - Previous resistance levels
                
                ## Volume Analysis
                
                ✅ **Classic Volume Pattern**:
                
                **Broadening Phase**:
                - High volume at price extremes
                - Shows emotional trading
                - Wide swings, active participation
                
                **Narrowing Phase**:
                - Declining volume
                - Uncertainty, indecision
                - Volatility compression
                
                **Breakout**:
                - Volume surge (critical confirmation)
                - 50%+ above average ideal
                - Without volume = unreliable
                
                ## Pattern Recognition
                
                ### Key Features:
                
                ✅ **Valid Diamond**:
                - Clear broadening phase
                - Distinct narrowing phase
                - At least 4 reversal points
                - Symmetrical appearance
                - Volume pattern present
                
                ❌ **Invalid Pattern**:
                - Lopsided or asymmetrical
                - Missing broadening/narrowing
                - Insufficient reversal points
                - No volume confirmation
                
                ## Market Psychology
                
                ### Diamond Top Psychology:
                
                1. **Initial**: Confidence high, volatility increasing
                2. **Peak**: Extreme optimism, wild swings
                3. **Compression**: Uncertainty emerges, indecision
                4. **Breakdown**: Panic, trend reversal
                
                **Emotion Flow**: Greed → Confusion → Fear
                
                ### Diamond Bottom Psychology:
                
                1. **Initial**: Fear high, selling accelerates
                2. **Trough**: Capitulation, extreme volatility
                3. **Compression**: Uncertainty, stabilization
                4. **Breakout**: Hope returns, reversal confirmed
                
                **Emotion Flow**: Fear → Confusion → Hope
                
                ## Common Mistakes
                
                ❌ Trading before pattern completes
                ❌ Ignoring volume patterns
                ❌ Expecting perfect symmetry
                ❌ Confusing with other patterns (H&S, triangles)
                ❌ Setting stops too tight (volatility trap)
                ❌ Not waiting for breakout confirmation
                ❌ Missing the broadening phase
                
                ## Reliability Factors
                
                ✅ **High Reliability When**:
                - Clear, symmetrical diamond shape
                - Distinct broadening and narrowing phases
                - Strong prior trend
                - Clear volume pattern
                - Breakout with high volume
                - Forms at significant top/bottom
                
                ❌ **Lower Reliability When**:
                - Asymmetrical or unclear shape
                - Missing clear phases
                - Weak prior trend
                - No volume pattern
                - Low volume breakout
                - Appears mid-trend
                
                ## Time Considerations
                
                - **Minimum**: 4-6 weeks
                - **Typical**: 6-12 weeks
                - **Maximum**: 4-5 months
                - **Too Fast**: Less reliable
                - **Too Slow**: May evolve into other pattern
                
                ## Comparison to Other Patterns
                
                | Feature | Diamond | H&S | Triangle |
                |---------|---------|-----|----------|
                | Shape | Broadening then narrowing | Three peaks | Converging lines |
                | Phases | Two distinct | Single formation | One compression |
                | Rarity | Rare | Common | Very common |
                | Reliability | High | Very high | Moderate |
                
                ## Advanced Concepts
                
                ### Failed Diamonds:
                - Breakout in opposite direction
                - Becomes continuation pattern
                - Stop losses critical
                
                ### Nested Patterns:
                - Smaller patterns within diamond
                - Can trade both
                - More complex analysis
                
                ## Pro Tips
                
                ✅ Diamonds are rare - be selective
                ✅ Require patience to fully form
                ✅ Volume confirmation is critical
                ✅ Best on daily/weekly timeframes
                ✅ Look for at major market turns
                ✅ Combine with momentum indicators
                ✅ Wait for clear breakout
                ✅ Use multiple timeframe confirmation
                ✅ Consider broader market context
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What are the two distinct phases of a Diamond pattern?",
                        options = listOf("Up and down", "Broadening then narrowing", "Left and right", "Fast and slow"),
                        correctAnswer = 1,
                        explanation = "A Diamond pattern has two distinct phases: first a broadening phase with expanding volatility (diverging trendlines), then a narrowing phase with compressing volatility (converging trendlines)."
                    ),
                    QuizQuestion(
                        question = "Where do Diamond patterns typically appear?",
                        options = listOf("Mid-trend", "At major market tops or bottoms", "Only in bear markets", "Randomly"),
                        correctAnswer = 1,
                        explanation = "Diamond patterns are reversal patterns that typically appear at significant market tops (Diamond Top) or bottoms (Diamond Bottom), signaling potential trend reversals."
                    ),
                    QuizQuestion(
                        question = "What confirms a valid Diamond pattern breakout?",
                        options = listOf("Time passing", "Volume surge of 50%+ above average", "Price color", "Day of week"),
                        correctAnswer = 1,
                        explanation = "A valid Diamond breakout requires a significant volume increase, typically 50% or more above average volume, to confirm the reversal and reduce false breakout risk."
                    )
                )
            )
        )
