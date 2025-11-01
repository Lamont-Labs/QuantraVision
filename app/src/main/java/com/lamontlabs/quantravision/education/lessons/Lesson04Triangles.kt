package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson04Triangles = Lesson(
            id = 4,
            title = "Triangle Patterns",
            category = "Continuation Patterns",
            duration = "10 min",
            content = """
                # Triangle Patterns
                
                ## Overview
                
                Triangle patterns are among the most common continuation patterns in technical analysis. They represent a period of consolidation before the prevailing trend resumes. There are three main types: Ascending, Descending, and Symmetrical triangles.
                
                ## Ascending Triangle (Bullish)
                
                ### Structure:
                ```
                Resistance ___________________
                          /  /  /  /  /  /  
                         /  /  /  /  /  /   
                        /  /  /  /  /  /    
                       /  /  /  /  /  /     
                      /  /  /  /  /  /      
                     /  /  /  /  /  /       
                    /  /  /  /  /  /        
                   Ascending Support
                ```
                
                ### Characteristics:
                - **Flat Resistance**: Horizontal resistance line
                - **Rising Support**: Series of higher lows
                - **Breakout**: Typically upward through resistance
                - **Psychology**: Buyers becoming more aggressive
                
                ## Descending Triangle (Bearish)
                
                ### Structure:
                ```
                  Descending Resistance
                   \  \  \  \  \  \  \
                    \  \  \  \  \  \  \
                     \  \  \  \  \  \  \
                      \  \  \  \  \  \  \
                       \  \  \  \  \  \  \
                        \  \  \  \  \  \  \
                Support___________________
                ```
                
                ### Characteristics:
                - **Flat Support**: Horizontal support line
                - **Falling Resistance**: Series of lower highs
                - **Breakout**: Typically downward through support
                - **Psychology**: Sellers becoming more aggressive
                
                ## Symmetrical Triangle (Neutral)
                
                ### Structure:
                ```
                      /\  /\  /\  /\
                     /  \/  \/  \/  \
                    /              \
                   /                \
                  /                  \
                 /____________________\
                ```
                
                ### Characteristics:
                - **Converging Lines**: Both support and resistance converging
                - **Equal Pressure**: Bulls and bears in equilibrium
                - **Breakout**: Can be either direction (continuation more likely)
                - **Psychology**: Indecision resolving into trend continuation
                
                ## Trading Guidelines
                
                ### Entry Points:
                1. **Breakout Entry**: Enter when price breaks trendline
                2. **Retest Entry**: Wait for pullback to test broken level
                3. **Early Entry**: Enter at support bounce (higher risk)
                
                ### Stop Loss Placement:
                - **Ascending**: Below most recent swing low
                - **Descending**: Above most recent swing high
                - **Symmetrical**: Opposite side of triangle
                
                ### Price Targets:
                - Measure widest part of triangle (base)
                - Project that distance from breakout point
                - Alternative: Measure to apex, add to breakout
                
                ## Volume Patterns
                
                ✅ **Ideal Volume Profile**:
                - Volume decreases as pattern forms
                - Price compresses toward apex
                - Volume surges on breakout (confirmation)
                - Higher volume = higher reliability
                
                ## Time Considerations
                
                - **Formation Time**: 1-3 months typically
                - **Breakout Point**: Usually between 50-75% to apex
                - **Too Early**: Pattern may fail
                - **Too Late**: Minimal profit potential
                
                ## Common Mistakes
                
                ❌ Trading false breakouts without volume
                ❌ Waiting too long (near apex = weak signal)
                ❌ Ignoring the prevailing trend
                ❌ Wrong triangle type identification
                ❌ Not waiting for confirmation
                
                ## Pro Tips
                
                ✅ Use multiple timeframes for confirmation
                ✅ Combine with momentum indicators (RSI, MACD)
                ✅ Watch for decreasing volume during formation
                ✅ Be patient - let pattern fully develop
                ✅ Consider market context and trend strength
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "In an Ascending Triangle, what does the pattern of higher lows indicate?",
                        options = listOf("Sellers are in control", "Buyers are becoming more aggressive", "Pattern will fail", "Volume is decreasing"),
                        correctAnswer = 1,
                        explanation = "Higher lows in an Ascending Triangle show that buyers are willing to pay progressively higher prices, indicating increasing buying pressure and bullish sentiment."
                    ),
                    QuizQuestion(
                        question = "What is the ideal breakout point in a triangle pattern?",
                        options = listOf("At the apex", "Between 50-75% to the apex", "Immediately after formation", "Doesn't matter"),
                        correctAnswer = 1,
                        explanation = "Breakouts occurring between 50-75% of the distance to the apex tend to be most reliable. Too early may be false, too late has minimal profit potential."
                    ),
                    QuizQuestion(
                        question = "How do you calculate the price target for a triangle breakout?",
                        options = listOf("10% above resistance", "Measure base width, project from breakout", "Double the height", "There is no reliable target"),
                        correctAnswer = 1,
                        explanation = "Measure the widest part (base) of the triangle and project that same distance from the breakout point to estimate the price target."
                    )
                )
            )
        )
