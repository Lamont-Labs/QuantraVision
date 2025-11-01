package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson02HeadShoulders = Lesson(
            id = 2,
            title = "Head and Shoulders Pattern",
            category = "Reversal Patterns",
            duration = "8 min",
            content = """
                # Head and Shoulders Pattern
                
                ## Overview
                
                The Head and Shoulders is one of the most reliable reversal patterns, signaling the end of an uptrend. It consists of three peaks: a higher peak (head) between two lower peaks (shoulders).
                
                ## Structure
                
                ```
                    Left    Head    Right
                   Shoulder        Shoulder
                      /\      /\      /\
                     /  \    /  \    /  \
                    /    \  /    \  /    \
                   /      \/      \/      \
                  /                        \
                 /__________________________\
                     Neckline (Support)
                ```
                
                ### Components:
                
                1. **Left Shoulder**: First peak during uptrend
                2. **Head**: Higher peak following a pullback
                3. **Right Shoulder**: Third peak at similar height to left shoulder
                4. **Neckline**: Support level connecting the two troughs
                
                ## Psychology
                
                - **Left Shoulder**: Bulls push prices higher
                - **Head**: New high but momentum weakening
                - **Right Shoulder**: Bulls fail to reach previous high
                - **Breakdown**: Bears take control
                
                ## Trading the Pattern
                
                ### Entry Points:
                - **Conservative**: Wait for neckline break and retest
                - **Aggressive**: Enter at right shoulder formation
                
                ### Stop Loss:
                - Place above right shoulder or recent swing high
                
                ### Target:
                - Measure head to neckline distance
                - Project same distance below neckline
                
                ## Key Indicators
                
                ✅ **Volume Pattern**:
                - Left shoulder: High volume
                - Head: Declining volume
                - Right shoulder: Lower volume
                - Breakdown: High volume (confirmation)
                
                ## Common Mistakes
                
                ❌ Entering before neckline break
                ❌ Ignoring volume confirmation
                ❌ Expecting perfect symmetry
                ❌ Missing the inverse pattern (bullish)
                
                ## Inverse Head and Shoulders
                
                The upside-down version signals bullish reversal at trend bottoms, with the same principles applied in reverse.
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "In a Head and Shoulders pattern, which component should have the lowest volume?",
                        options = listOf("Left Shoulder", "Head", "Right Shoulder", "Breakdown"),
                        correctAnswer = 2,
                        explanation = "The right shoulder typically shows the lowest volume, indicating weakening buying pressure and increased likelihood of reversal."
                    ),
                    QuizQuestion(
                        question = "How do you calculate the price target for a Head and Shoulders pattern?",
                        options = listOf("Double the head height", "Head to neckline distance projected below", "Always 10% below", "There is no target"),
                        correctAnswer = 1,
                        explanation = "Measure the distance from the head to the neckline, then project that same distance downward from the neckline breakdown point."
                    ),
                    QuizQuestion(
                        question = "What confirms a valid Head and Shoulders breakdown?",
                        options = listOf("Time passing", "High volume on neckline break", "Social media mentions", "Round numbers"),
                        correctAnswer = 1,
                        explanation = "High volume on the neckline break confirms that there is strong selling pressure and increases the probability of the pattern succeeding."
                    )
                )
            )
        )
