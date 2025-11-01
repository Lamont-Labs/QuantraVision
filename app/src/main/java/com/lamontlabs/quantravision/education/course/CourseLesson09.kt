package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.*


val courseLesson09 = Lesson(
            id = 9,
            title = "Pattern Psychology & Market Sentiment",
            description = "Understand the psychology behind patterns and crowd behavior",
            content = """
                Chart patterns work because they reflect human psychology. Understanding WHY patterns form helps you trade them better.
                
                **The Psychology Behind Patterns:**
                
                **Fear and Greed Cycle:**
                1. **Optimism** - Market starts rising
                2. **Excitement** - More buyers join
                3. **Thrill** - Everyone is buying
                4. **Euphoria** - Top (everyone is in)
                5. **Anxiety** - First signs of trouble
                6. **Denial** - "It's just a pullback"
                7. **Fear** - Reality sets in
                8. **Desperation** - Trying to get out
                9. **Panic** - Capitulation
                10. **Despondency** - Bottom
                11. **Depression** - Market bottoms
                12. **Hope** - Cycle repeats
                
                **Pattern Formation Psychology:**
                
                **Head and Shoulders:**
                - Left Shoulder: Bulls still in control
                - Head: Final push by bulls (false hope)
                - Right Shoulder: Buyers exhausted
                - Neckline break: Bears take control
                - Psychology: Weakening buying pressure
                
                **Double Top:**
                - First peak: Bulls test resistance
                - Pullback: Profit taking
                - Second peak: Failed attempt (can't break)
                - Psychology: "Fool me once..."
                - Traders remember first rejection
                
                **Bull Flag:**
                - Flagpole: FOMO buying
                - Flag: Profit taking, consolidation
                - Breakout: Late buyers and continuation
                - Psychology: Brief rest in strong trend
                
                **Market Sentiment Indicators:**
                
                **1. Put/Call Ratio**
                - High ratio = Fear (bearish bets)
                - Low ratio = Greed (bullish bets)
                - Extreme readings = Contrarian signals
                
                **2. VIX (Fear Index)**
                - Low VIX = Complacency
                - High VIX = Fear/Panic
                - Spikes often mark bottoms
                
                **3. News Sentiment**
                - Extreme positive = Top warning
                - Extreme negative = Bottom signal
                - "Buy when there's blood in the streets"
                
                **Crowd Psychology:**
                - Early adopters profit (pattern forms)
                - Majority enters (pattern completes)
                - Late crowd gets trapped (reversal)
                - Understanding this = Trading edge
            """.trimIndent(),
            keyPoints = listOf(
                "Patterns reflect crowd psychology",
                "Fear and greed drive formations",
                "Sentiment extremes signal reversals",
                "Understanding psychology improves timing",
                "Contrarian thinking at extremes"
            ),
            examples = listOf(
                PatternExample(
                    patternName = "Capitulation Bottom",
                    description = "Panic selling creates opportunity",
                    identificationTips = listOf(
                        "Extreme negative news coverage",
                        "High VIX spike",
                        "Volume climax on selling",
                        "Wide-range down candle",
                        "Followed by reversal",
                        "Psychology: Maximum fear and despair"
                    )
                ),
                PatternExample(
                    patternName = "Euphoria Top",
                    description = "Excessive optimism marks peak",
                    identificationTips = listOf(
                        "Mainstream media touting gains",
                        "Everyone talking about profits",
                        "Low VIX (complacency)",
                        "Parabolic price rise",
                        "Reversal patterns forming",
                        "Psychology: 'This time is different'"
                    )
                )
            ),
            quiz = Quiz(
                questions = listOf(
                    Question(
                        question = "What emotion typically marks market tops?",
                        options = listOf(
                            "Fear",
                            "Panic",
                            "Euphoria",
                            "Depression"
                        ),
                        correctAnswerIndex = 2,
                        explanation = "Euphoria and excessive optimism typically mark market tops when everyone is invested."
                    ),
                    Question(
                        question = "A high VIX reading indicates:",
                        options = listOf(
                            "Market complacency",
                            "Fear and uncertainty",
                            "Bull market peak",
                            "Normal conditions"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "High VIX indicates fear and uncertainty in the market, often marking potential bottoms."
                    ),
                    Question(
                        question = "Why does a Head and Shoulders pattern form?",
                        options = listOf(
                            "Random chance",
                            "Progressively weakening buying pressure",
                            "Government manipulation",
                            "Computer algorithms only"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "H&S forms as buying pressure weakens with each attempt, reflecting exhaustion of bullish sentiment."
                    ),
                    Question(
                        question = "What does the VIX (Fear Index) measure?",
                        options = listOf(
                            "Stock prices directly",
                            "Market volatility and fear/uncertainty levels",
                            "Trading volume only",
                            "Company earnings"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "The VIX measures market volatility and fear levels - high VIX indicates fear/panic, low VIX shows complacency."
                    )
                )
            )
        )
