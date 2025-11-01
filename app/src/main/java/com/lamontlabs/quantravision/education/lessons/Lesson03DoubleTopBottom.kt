package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson03DoubleTopBottom = Lesson(
            id = 3,
            title = "Double Top and Double Bottom",
            category = "Reversal Patterns",
            duration = "7 min",
            content = """
                # Double Top and Double Bottom Patterns
                
                ## Double Top (Bearish Reversal)
                
                A Double Top forms after an uptrend and consists of two peaks at approximately the same price level, separated by a moderate trough.
                
                ### Structure:
                ```
                      Peak 1      Peak 2
                        /\          /\
                       /  \        /  \
                      /    \      /    \
                     /      \    /      \
                    /        \  /        \
                   /          \/          \
                  /        Trough          \
                 /__________________________\
                       Support Level
                ```
                
                ### Formation:
                1. **First Peak**: Price reaches resistance
                2. **Pullback**: Retreat to support (10-20%)
                3. **Second Peak**: Fails to break previous high
                4. **Breakdown**: Price breaks support level
                
                ## Double Bottom (Bullish Reversal)
                
                The inverse pattern, signaling the end of a downtrend with two lows at similar price levels.
                
                ### Structure:
                ```
                   Resistance Level
                  ___________________________
                 \                           /
                  \          Peak           /
                   \        /  \          /
                    \      /    \        /
                     \    /      \      /
                      \  /        \    /
                       \/          \  /
                    Trough 1    Trough 2
                ```
                
                ## Trading Guidelines
                
                ### Double Top:
                - **Entry**: Break below support level
                - **Stop**: Above second peak
                - **Target**: Distance from peaks to support, projected down
                
                ### Double Bottom:
                - **Entry**: Break above resistance level
                - **Stop**: Below second trough
                - **Target**: Distance from troughs to resistance, projected up
                
                ## Confirmation Signals
                
                ✅ **Time Between Peaks**: Typically 1-3 months
                ✅ **Volume**: Decreasing on second peak/trough
                ✅ **Breakout Volume**: High on breakout
                ✅ **Retest**: Price often retests broken level
                
                ## Psychology
                
                ### Double Top:
                - First peak: Enthusiasm remains
                - Pullback: Taking profits
                - Second peak: Failure to break = sentiment shift
                - Breakdown: Panic selling begins
                
                ### Double Bottom:
                - First trough: Capitulation
                - Rally: Short covering
                - Second trough: Buying opportunity recognized
                - Breakout: Bulls regain control
                
                ## Variations
                
                - **Triple Top/Bottom**: Three peaks/troughs instead of two
                - **Adam & Eve**: Sharp peak followed by rounded peak
                - **Eve & Adam**: Rounded peak followed by sharp peak
                
                ## Common Pitfalls
                
                ❌ Trading before breakout confirmation
                ❌ Expecting exact symmetry
                ❌ Ignoring volume divergence
                ❌ Setting stops too tight
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the ideal time between peaks in a Double Top pattern?",
                        options = listOf("1-3 days", "1-3 weeks", "1-3 months", "1-3 years"),
                        correctAnswer = 2,
                        explanation = "Double Tops typically form over 1-3 months. Too short indicates indecision, too long may not be a valid pattern."
                    ),
                    QuizQuestion(
                        question = "Where should you place your stop loss when trading a Double Top breakdown?",
                        options = listOf("Below support", "Above second peak", "At entry price", "No stop needed"),
                        correctAnswer = 1,
                        explanation = "Place stop loss above the second peak to protect against failed breakdown and allow for minor whipsaws."
                    ),
                    QuizQuestion(
                        question = "What volume pattern confirms a valid Double Bottom?",
                        options = listOf("High on both troughs", "Decreasing on second trough, high on breakout", "Always increasing", "Volume doesn't matter"),
                        correctAnswer = 1,
                        explanation = "Lower volume on the second trough shows weakening selling pressure, while high volume on breakout confirms buyers are in control."
                    )
                )
            )
        )
