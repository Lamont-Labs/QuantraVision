package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson09RoundingBottomTop = Lesson(
            id = 9,
            title = "Rounding Bottom and Top",
            category = "Reversal Patterns",
            duration = "8 min",
            content = """
                # Rounding Bottom and Top Patterns
                
                ## Overview
                
                Rounding patterns (also called saucers or bowls) are gradual reversal patterns that form over extended periods. They represent slow, methodical changes in sentiment rather than sudden reversals, making them reliable when properly identified.
                
                ## Rounding Bottom (Bullish)
                
                ### Structure:
                ```
                                    /‾‾‾\
                                   /     \
                                  /       \
                Downtrend →     /         \
                               /           \
                              /             \
                             /               \
                            /                 \
                           \_________________/
                              Accumulation
                                  ↓
                              Breakout ↑
                ```
                
                ### Characteristics:
                - **U-Shape**: Smooth, gradual curve
                - **Duration**: 3-12 months typical
                - **Depth**: Varies, often substantial
                - **Volume**: Creates bowl shape (high-low-high)
                - **Psychology**: Gradual sentiment shift
                
                ### Formation Stages:
                
                1. **Downtrend** (Left Side):
                   - Declining prices
                   - High volume initially
                   - Selling pressure
                
                2. **Bottom** (Middle):
                   - Selling exhaustion
                   - Lowest volume
                   - Accumulation begins
                   - Trading range emerges
                
                3. **Recovery** (Right Side):
                   - Slow price rise
                   - Increasing volume
                   - Buying interest returns
                   - Confidence building
                
                4. **Breakout**:
                   - Breaks resistance
                   - Volume surges
                   - New uptrend confirmed
                
                ## Rounding Top (Bearish)
                
                ### Structure:
                ```
                           ___________________
                          /                   \
                         /                     \
                        /                       \
                Uptrend                          \
                                                  \
                                                   \
                                                    \
                                                     \
                                                      \___
                                                   Distribution
                                                        ↓
                                                   Breakdown ↓
                ```
                
                ### Characteristics:
                - **Inverted U**: Gradual dome formation
                - **Duration**: 3-12 months
                - **Formation**: At market tops
                - **Volume**: Inverse bowl (low-high-low)
                - **Psychology**: Enthusiasm to fear
                
                ### Formation Stages:
                
                1. **Uptrend** (Left Side):
                   - Rising prices
                   - Strong buying
                   - Optimism high
                
                2. **Top** (Middle):
                   - Buying exhaustion
                   - Higher volume
                   - Distribution begins
                   - Sideways churn
                
                3. **Decline** (Right Side):
                   - Slow price fall
                   - Declining volume
                   - Selling emerges
                   - Concern grows
                
                4. **Breakdown**:
                   - Breaks support
                   - Volume increases
                   - Downtrend confirmed
                
                ## Trading Guidelines
                
                ### Rounding Bottom Trading:
                
                **Early Entry** (Advanced):
                - Enter during bottom formation
                - Buy when volume starts increasing
                - Requires patience and conviction
                - Better average price
                
                **Breakout Entry** (Conservative):
                - Wait for resistance break
                - Confirm with volume
                - Higher probability
                - Less time in trade
                
                **Stop Loss**:
                - Below recent support
                - Or below bottom of pattern
                - Wide stop needed due to pattern size
                
                **Target**:
                - Depth of pattern added to breakout
                - Often exceeds measured move
                - Previous highs
                
                ### Rounding Top Trading:
                
                **Entry Points**:
                - Breakdown below support
                - Right side decline confirmation
                - Volume increase on breakdown
                
                **Stop Loss**:
                - Above recent resistance
                - Or above top of pattern
                - Wider stops required
                
                **Target**:
                - Depth of pattern subtracted from breakdown
                - Previous support levels
                - May exceed measured move
                
                ## Volume Characteristics
                
                ### Rounding Bottom Volume:
                ```
                Volume:  High → Low → High
                          |      |      |
                        Selling Base Buying
                ```
                
                - **Left**: High (selling pressure)
                - **Bottom**: Low (equilibrium)
                - **Right**: Increasing (buying pressure)
                - **Breakout**: Spike (confirmation)
                
                ### Rounding Top Volume:
                ```
                Volume:  Low → High → Low
                         |      |      |
                       Buying  Top  Selling
                ```
                
                - **Left**: Low-moderate (easy gains)
                - **Top**: High (distribution)
                - **Right**: Declining (sellers emerge)
                - **Breakdown**: Spike (confirmation)
                
                ## Pattern Quality
                
                ✅ **High Quality Indicators**:
                - Smooth, symmetrical curve
                - Clear volume pattern
                - Extended duration (3+ months)
                - Multiple retests of key levels
                - Clean breakout/breakdown
                
                ❌ **Low Quality Warnings**:
                - Jagged, irregular shape
                - No volume pattern
                - Too fast formation (<6 weeks)
                - Failed breakouts
                - Low volume on break
                
                ## Common Mistakes
                
                ❌ Expecting perfect symmetry
                ❌ Trading too early in formation
                ❌ Ignoring volume divergence
                ❌ Setting stops too tight
                ❌ Impatience (pattern takes time)
                ❌ Missing the gradual nature
                
                ## Time Considerations
                
                - **Minimum**: 6-8 weeks
                - **Typical**: 3-6 months
                - **Extended**: Up to 12+ months
                - **Longer = Stronger**: Extended patterns more reliable
                - **Patience Required**: Not for day traders
                
                ## Variations
                
                ### Dormant Bottom:
                - Flat extended base
                - Minimal rounding
                - Sudden breakout
                
                ### Spike Bottom:
                - Sharp decline into round
                - Panic selling starts pattern
                - More volatile
                
                ### Platform Top:
                - Flat top section
                - Extended distribution
                - Gradual rollover
                
                ## Market Psychology
                
                ### Rounding Bottom:
                - **Fear → Hope → Confidence**
                - Gradual shift from pessimism to optimism
                - Smart money accumulates throughout
                - Public enters late (at breakout)
                
                ### Rounding Top:
                - **Greed → Doubt → Fear**
                - Gradual shift from optimism to pessimism
                - Smart money distributes throughout
                - Public holds too long
                
                ## Pro Tips
                
                ✅ These patterns are rare but reliable
                ✅ Best on weekly/monthly charts
                ✅ Combine with fundamentals (turnaround stories)
                ✅ Volume pattern is critical confirmation
                ✅ Be patient - don't rush the pattern
                ✅ Look for "handle" after rounding bottom
                ✅ Multiple timeframe analysis essential
                ✅ Consider sector/market trends
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the typical duration for a Rounding Bottom pattern to form?",
                        options = listOf("1-2 days", "1-2 weeks", "3-12 months", "5+ years"),
                        correctAnswer = 2,
                        explanation = "Rounding Bottom patterns typically take 3-12 months to form as they represent gradual, methodical shifts in market sentiment rather than sudden reversals."
                    ),
                    QuizQuestion(
                        question = "What volume pattern confirms a Rounding Bottom?",
                        options = listOf("High throughout", "High-Low-High (bowl shape)", "Low throughout", "Random volume"),
                        correctAnswer = 1,
                        explanation = "A Rounding Bottom should show high volume on the left (selling), low volume at the bottom (equilibrium), and increasing volume on the right (buying), forming a bowl shape."
                    ),
                    QuizQuestion(
                        question = "How does a Rounding Top differ from a Rounding Bottom in market psychology?",
                        options = listOf("No difference", "Gradual shift from greed to fear vs fear to confidence", "Faster formation", "No volume pattern"),
                        correctAnswer = 1,
                        explanation = "A Rounding Top represents a gradual shift from greed/optimism to doubt and fear (bearish), while a Rounding Bottom shows a shift from fear to hope to confidence (bullish)."
                    )
                )
            )
        )
