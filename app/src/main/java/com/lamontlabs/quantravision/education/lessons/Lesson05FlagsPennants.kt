package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson05FlagsPennants = Lesson(
            id = 5,
            title = "Flags and Pennants",
            category = "Continuation Patterns",
            duration = "8 min",
            content = """
                # Flags and Pennants
                
                ## Overview
                
                Flags and Pennants are short-term continuation patterns that mark brief consolidations before the trend resumes. They appear after strong price moves (the "flagpole") and indicate temporary profit-taking before continuation.
                
                ## Bull Flag Pattern
                
                ### Structure:
                ```
                         Flagpole
                            |
                            |
                            |
                            |  ____
                            | /    \  Flag
                            |/______\
                            
                        Breakout ↑
                ```
                
                ### Characteristics:
                - **Sharp Rally**: Strong upward move (flagpole)
                - **Consolidation**: Slight downward drift (flag)
                - **Parallel Lines**: Flag slopes against trend
                - **Duration**: 1-4 weeks typically
                - **Breakout**: Continuation of uptrend
                
                ## Bear Flag Pattern
                
                ### Structure:
                ```
                        Breakout ↓
                            
                            ______
                           /      \  Flag
                          /________\
                         |
                         |
                         |
                         |
                      Flagpole
                ```
                
                ### Characteristics:
                - **Sharp Decline**: Strong downward move
                - **Consolidation**: Slight upward drift
                - **Counter-trend**: Flag slopes against main trend
                - **Continuation**: Resumes downtrend
                
                ## Pennant Pattern
                
                ### Structure:
                ```
                    Flagpole
                       |
                       |     /\
                       |    /  \
                       |   /    \
                       |  /______\
                       |  Pennant
                       
                   Breakout
                ```
                
                ### Characteristics:
                - **Symmetrical**: Converging trendlines
                - **Smaller**: More compact than flags
                - **Faster**: Forms over 1-3 weeks
                - **Similar Signal**: Continuation pattern
                
                ## Key Differences
                
                | Feature | Flag | Pennant |
                |---------|------|---------|
                | Shape | Rectangular/Parallelogram | Small Triangle |
                | Trendlines | Parallel | Converging |
                | Duration | 1-4 weeks | 1-3 weeks |
                | Size | Larger | Smaller |
                
                ## Trading the Pattern
                
                ### Entry Strategies:
                
                1. **Aggressive**: Enter near support/resistance of flag
                2. **Conservative**: Wait for breakout above/below flag
                3. **Retest**: Enter after breakout and retest
                
                ### Stop Loss:
                - **Bull Flag**: Below flag's lower boundary
                - **Bear Flag**: Above flag's upper boundary
                - **Pennant**: Opposite side of pennant
                
                ### Price Target:
                - Measure flagpole height
                - Add to breakout point (bull) or subtract (bear)
                - Often achieves 100% of flagpole height
                
                ## Volume Analysis
                
                ✅ **Ideal Volume Pattern**:
                1. **Flagpole**: Very high volume (strong move)
                2. **Flag/Pennant**: Declining volume (consolidation)
                3. **Breakout**: Increasing volume (confirmation)
                
                Volume confirmation is crucial for these patterns!
                
                ## Psychology
                
                ### Bull Flag:
                - Flagpole: FOMO buying, strong momentum
                - Flag: Early profits taken, weak hands exit
                - Breakout: New buyers enter, trend resumes
                
                ### Bear Flag:
                - Flagpole: Panic selling, strong momentum
                - Flag: Short covering, temporary relief
                - Breakout: Sellers return, trend continues
                
                ## Reliability Factors
                
                ✅ **High Reliability**:
                - Clear, strong flagpole
                - Distinct consolidation period
                - High volume on flagpole and breakout
                - Forms in strong trending market
                - Duration under 4 weeks
                
                ❌ **Low Reliability**:
                - Weak initial move
                - Extended consolidation (>4 weeks becomes rectangle)
                - Low volume throughout
                - Against major trend
                
                ## Common Mistakes
                
                ❌ Confusing flags with full reversals
                ❌ Entering without volume confirmation
                ❌ Holding through extended consolidation
                ❌ Ignoring the strength of flagpole
                ❌ Setting target too conservatively
                
                ## Pro Tips
                
                ✅ Flag should retrace 38-50% of flagpole max
                ✅ Steeper flag = weaker pattern
                ✅ Look for flags in strong trending markets
                ✅ Multiple timeframe confirmation
                ✅ News-driven flagpoles are powerful
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the ideal volume pattern during a flag formation?",
                        options = listOf("Increasing throughout", "High on flagpole, declining during flag, increasing on breakout", "Constant volume", "Low throughout"),
                        correctAnswer = 1,
                        explanation = "The flagpole should show high volume (strong move), volume should decline during the flag (consolidation), and increase again on breakout (confirmation)."
                    ),
                    QuizQuestion(
                        question = "How long should a flag pattern typically take to form?",
                        options = listOf("1 day", "1-4 weeks", "3-6 months", "Over 1 year"),
                        correctAnswer = 1,
                        explanation = "Flags typically form over 1-4 weeks. If consolidation extends beyond 4 weeks, the pattern may evolve into a rectangle or lose its continuation characteristics."
                    ),
                    QuizQuestion(
                        question = "How do you calculate the price target for a bull flag breakout?",
                        options = listOf("10% above flag", "Measure flagpole height and add to breakout point", "Double the flag height", "No reliable target exists"),
                        correctAnswer = 1,
                        explanation = "Measure the height of the flagpole (initial strong move) and project that same distance upward from the breakout point to estimate the price target."
                    )
                )
            )
        )
