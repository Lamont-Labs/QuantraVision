package com.lamontlabs.quantravision.education.lessons

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz
import com.lamontlabs.quantravision.education.model.QuizQuestion

val lesson01Intro = Lesson(
            id = 1,
            title = "Introduction to Chart Patterns",
            category = "Basics",
            duration = "5 min",
            content = """
                # Introduction to Chart Patterns
                
                ## What Are Chart Patterns?
                
                Chart patterns are recognizable configurations in price charts that technical analysts use to predict future price movements. These patterns are formed by the movement of securities' prices over time and can be categorized into continuation and reversal patterns.
                
                ## Why Do Patterns Work?
                
                Patterns work because they represent the collective psychology of market participants:
                - **Fear and Greed**: Patterns capture emotional extremes
                - **Market Memory**: Similar setups tend to produce similar outcomes
                - **Self-Fulfilling**: Traders act on patterns, making them more likely to succeed
                
                ## Types of Patterns
                
                ### Reversal Patterns
                - Head and Shoulders
                - Double Top/Bottom
                - Triple Top/Bottom
                - Rounding Top/Bottom
                
                ### Continuation Patterns
                - Triangles (Ascending, Descending, Symmetrical)
                - Flags and Pennants
                - Rectangles
                - Wedges
                
                ## Key Components
                
                1. **Support**: Price level where buying pressure exceeds selling pressure
                2. **Resistance**: Price level where selling pressure exceeds buying pressure
                3. **Breakout**: Price moving beyond support/resistance
                4. **Volume**: Confirms pattern validity
                
                ## Next Steps
                
                In the following lessons, you'll learn to identify each pattern type, understand their psychology, and apply them in real trading scenarios.
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What are the two main categories of chart patterns?",
                        options = listOf("Bullish and Bearish", "Reversal and Continuation", "Short and Long", "Simple and Complex"),
                        correctAnswer = 1,
                        explanation = "Chart patterns are primarily categorized as Reversal patterns (indicate trend change) and Continuation patterns (indicate trend continuation)."
                    ),
                    QuizQuestion(
                        question = "Why do chart patterns work?",
                        options = listOf("They are guaranteed", "They capture market psychology", "They are random", "They only work in bull markets"),
                        correctAnswer = 1,
                        explanation = "Patterns work because they capture the collective psychology of market participants, including fear, greed, and market memory."
                    ),
                    QuizQuestion(
                        question = "What confirms the validity of a pattern?",
                        options = listOf("Time of day", "Volume", "Color of candles", "Day of week"),
                        correctAnswer = 1,
                        explanation = "Volume is a critical confirmation indicator. High volume on breakouts suggests strong conviction and increases pattern reliability."
                    )
                )
            )
        )
