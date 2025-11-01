package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson06Wedges = Lesson(
            id = 6,
            title = "Wedge Patterns",
            category = "Reversal Patterns",
            duration = "9 min",
            content = """
                # Wedge Patterns
                
                ## Overview
                
                Wedge patterns are reversal patterns characterized by converging trendlines that slope in the same direction. Unlike triangles, both trendlines slope either up (Rising Wedge) or down (Falling Wedge), signaling potential trend exhaustion.
                
                ## Rising Wedge (Bearish)
                
                ### Structure:
                ```
                              /  /
                             /  /
                            /  /
                           /  /
                          /  /
                         /  /
                        /  /
                       /  /
                      /  /
                ```
                
                ### Characteristics:
                - **Both Lines Rise**: Support and resistance both ascending
                - **Converging**: Lines come together
                - **Narrowing Range**: Price action compresses
                - **Breakout**: Downward (bearish reversal)
                - **Appearance**: In uptrends or as topping pattern
                
                ### Psychology:
                - Higher highs show bulls in control
                - Narrowing range shows weakening momentum
                - Lower volume shows decreasing conviction
                - Breakout shows bears taking control
                
                ## Falling Wedge (Bullish)
                
                ### Structure:
                ```
                 \  \
                  \  \
                   \  \
                    \  \
                     \  \
                      \  \
                       \  \
                        \  \
                         \  \
                ```
                
                ### Characteristics:
                - **Both Lines Fall**: Support and resistance both descending
                - **Converging**: Lines come together
                - **Compression**: Price range narrows
                - **Breakout**: Upward (bullish reversal)
                - **Appearance**: In downtrends or as bottoming pattern
                
                ### Psychology:
                - Lower lows show bears in control
                - Narrowing range shows weakening selling
                - Decreasing volume shows capitulation
                - Breakout shows bulls returning
                
                ## Trading Guidelines
                
                ### Rising Wedge Trading:
                
                **Entry**:
                - Wait for breakdown below support line
                - Confirm with volume increase
                - Consider retest entry for better risk/reward
                
                **Stop Loss**:
                - Above most recent high within wedge
                - Or above resistance line
                
                **Target**:
                - Beginning of the wedge (full retracement)
                - Or measure wedge height, project downward
                
                ### Falling Wedge Trading:
                
                **Entry**:
                - Wait for breakout above resistance line
                - Confirm with volume spike
                - Retest of resistance as new support
                
                **Stop Loss**:
                - Below most recent low within wedge
                - Or below support line
                
                **Target**:
                - Beginning of the wedge
                - Or measure wedge height, project upward
                
                ## Volume Patterns
                
                ✅ **Classic Volume Profile**:
                
                **Rising Wedge**:
                - Declining volume as pattern forms
                - Shows weakening buying pressure
                - Volume spike on downside break
                
                **Falling Wedge**:
                - Declining volume as pattern forms
                - Shows selling exhaustion
                - Volume surge on upside breakout
                
                ## Time Considerations
                
                - **Formation**: 3 weeks to several months
                - **Longer = Stronger**: Extended wedges more reliable
                - **Breakout Timing**: Usually in latter 2/3 of pattern
                - **Steepness**: Steeper wedge = more dramatic reversal
                
                ## Key Differences from Triangles
                
                | Feature | Wedge | Triangle |
                |---------|-------|----------|
                | Direction | Both lines same direction | Lines opposite |
                | Implication | Reversal | Continuation |
                | Slope | Steep angle | Moderate angle |
                | Duration | Longer formation | Shorter formation |
                
                ## Common Mistakes
                
                ❌ Confusing with ascending/descending triangles
                ❌ Trading against the wedge (thinking continuation)
                ❌ Entering before clear breakout
                ❌ Ignoring volume divergence signals
                ❌ Poor stop loss placement
                ❌ Missing the reversal signal
                
                ## Advanced Patterns
                
                ### Ending Diagonal (Elliott Wave)
                - Similar to rising wedge in uptrend
                - Five-wave structure
                - Terminal pattern
                - Dramatic reversal often follows
                
                ### Leading Diagonal
                - Similar to falling wedge
                - Start of new trend
                - Less common than ending
                
                ## Confirmation Signals
                
                ✅ **Strong Confirmation**:
                - Multiple touches on each line (3+)
                - Clear volume divergence
                - RSI divergence (momentum weakening)
                - MACD divergence
                - Breakout with volume spike
                
                ## Real-World Application
                
                ### Market Context:
                - **Rising Wedge**: Often seen at market tops
                - **Falling Wedge**: Common in market bottoms
                - **News Impact**: Can accelerate breakout
                - **Time of Year**: End-of-quarter patterns
                
                ## Pro Tips
                
                ✅ Rising wedges more reliable in uptrends
                ✅ Falling wedges more reliable in downtrends
                ✅ Watch for momentum divergence (RSI, MACD)
                ✅ Combine with support/resistance levels
                ✅ Be patient - let pattern complete
                ✅ Volume confirmation is critical
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What makes a Rising Wedge bearish despite higher highs being made?",
                        options = listOf("It's not bearish", "Narrowing range and declining volume show weakening momentum", "The color of candles", "Time of day"),
                        correctAnswer = 1,
                        explanation = "Despite higher highs, the narrowing price range and typically declining volume indicate that buying pressure is weakening, making a downside reversal likely."
                    ),
                    QuizQuestion(
                        question = "How does a wedge differ from a triangle pattern?",
                        options = listOf("No difference", "Both trendlines slope in same direction", "Wedges are always bullish", "Wedges don't break out"),
                        correctAnswer = 1,
                        explanation = "In wedges, both trendlines slope in the same direction (up or down), whereas triangles have trendlines moving in opposite directions."
                    ),
                    QuizQuestion(
                        question = "What is the typical price target for a Falling Wedge breakout?",
                        options = listOf("5% gain", "Back to the beginning of the wedge", "No reliable target", "Apex of the wedge"),
                        correctAnswer = 1,
                        explanation = "The typical target is the beginning of the wedge pattern, often achieving a full retracement, or you can measure the wedge height and project it upward from the breakout point."
                    )
                )
            )
        )
