package com.lamontlabs.quantravision.education

data class Lesson(
    val id: Int,
    val title: String,
    val category: String,
    val duration: String,
    val content: String,
    val quiz: Quiz
)

data class Quiz(
    val questions: List<QuizQuestion>
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String
)

object LessonRepository {
    fun getAllLessons(): List<Lesson> = lessons
    
    fun getLessonById(id: Int): Lesson? = lessons.firstOrNull { it.id == id }
    
    fun getLessonsByCategory(category: String): List<Lesson> = 
        lessons.filter { it.category == category }
    
    private val lessons = listOf(
        Lesson(
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
        ),
        
        Lesson(
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
        ),
        
        Lesson(
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
        ),
        
        Lesson(
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
        ),
        
        Lesson(
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
        ),
        
        Lesson(
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
        ),
        
        Lesson(
            id = 7,
            title = "Rectangle Patterns",
            category = "Continuation Patterns",
            duration = "7 min",
            content = """
                # Rectangle Patterns
                
                ## Overview
                
                Rectangle patterns (also called trading ranges or consolidation zones) are continuation patterns where price moves sideways between parallel support and resistance levels. They represent a pause in the trend before continuation.
                
                ## Structure
                
                ```
                Resistance _____________________
                          |  ↑  ↓  ↑  ↓  ↑  ↓ |
                          |  ↓  ↑  ↓  ↑  ↓  ↑ |
                          |  ↑  ↓  ↑  ↓  ↑  ↓ |
                Support   |_____________________|
                
                          ← Consolidation →
                ```
                
                ## Characteristics
                
                - **Horizontal Lines**: Clear support and resistance
                - **Sideways Movement**: Price oscillates between levels
                - **Multiple Touches**: At least 2 touches of each level
                - **Equal Highs/Lows**: Consistent price boundaries
                - **Duration**: Can last weeks to months
                
                ## Market Psychology
                
                ### Formation:
                1. **Initial Trend**: Strong directional move
                2. **Profit Taking**: Early traders lock gains
                3. **Accumulation/Distribution**: Smart money builds position
                4. **Equilibrium**: Supply meets demand
                5. **Breakout**: Trend resumes
                
                ### Buyer/Seller Dynamics:
                - **At Support**: Buyers consistently step in
                - **At Resistance**: Sellers consistently emerge
                - **Inside Range**: Traders range-trade
                - **Breakout**: One side overwhelms the other
                
                ## Trading Strategies
                
                ### Strategy 1: Range Trading
                
                **Buy Setup**:
                - Enter near support
                - Stop below support
                - Target near resistance
                
                **Sell Setup**:
                - Enter near resistance
                - Stop above resistance
                - Target near support
                
                **Pros**: Multiple opportunities
                **Cons**: Whipsaw risk, breakout losses
                
                ### Strategy 2: Breakout Trading
                
                **Long Breakout**:
                - Entry: Close above resistance
                - Stop: Below resistance (now support)
                - Target: Height of rectangle added to breakout
                
                **Short Breakout**:
                - Entry: Close below support
                - Stop: Above support (now resistance)
                - Target: Height of rectangle subtracted from breakdown
                
                **Pros**: Catch major moves
                **Cons**: False breakouts
                
                ## Volume Analysis
                
                ✅ **Ideal Volume Profile**:
                
                **During Formation**:
                - Generally declining volume
                - Shows consolidation, not distribution
                - Lower volume = healthier pattern
                
                **At Breakout**:
                - Volume surge (3x+ average)
                - Confirms genuine breakout
                - High volume = high probability
                
                **Volume Patterns**:
                - Low volume bounces = weak
                - High volume bounces = strong support/resistance
                
                ## Breakout Confirmation
                
                ✅ **Strong Breakout Signals**:
                1. **Close**: Price closes beyond level (not just wicks)
                2. **Volume**: Significant increase (2-3x normal)
                3. **Follow-Through**: Next candle continues direction
                4. **Retest**: Pullback to broken level holds
                
                ❌ **False Breakout Warnings**:
                - Low volume on break
                - Only wicks beyond level
                - Immediate reversal
                - No follow-through
                
                ## Price Targets
                
                ### Measured Move:
                1. Measure height of rectangle (resistance to support)
                2. Add to breakout point (upside) or subtract (downside)
                3. This is minimum expectation
                
                ### Extended Targets:
                - 1.5x or 2x rectangle height for strong trends
                - Previous swing highs/lows
                - Fibonacci extensions
                
                ## Rectangle Types
                
                ### Bullish Rectangle:
                - Forms during uptrend
                - Breakout typically upward
                - Continuation more likely
                
                ### Bearish Rectangle:
                - Forms during downtrend
                - Breakdown typically downward
                - Continuation expected
                
                ### Reversal Rectangle:
                - Less common
                - Forms at trend extremes
                - Can signal reversal
                
                ## Time Considerations
                
                - **Minimum**: 3 weeks
                - **Typical**: 1-3 months
                - **Maximum**: 6+ months (becomes major support/resistance)
                - **Longer = Stronger**: Extended rectangles = bigger breakout
                
                ## Common Mistakes
                
                ❌ Trading every touch (overtrading)
                ❌ Chasing price in middle of range
                ❌ Ignoring the prevailing trend
                ❌ Not waiting for breakout confirmation
                ❌ Setting stops too tight
                ❌ Entering on first touch of level
                
                ## Advanced Concepts
                
                ### Market Profile:
                - Value Area within rectangle
                - Point of Control (most volume)
                - Use for better entries
                
                ### Multiple Timeframes:
                - Rectangle on daily = strong level
                - Check weekly for bigger picture
                - Use hourly for entry timing
                
                ## Pro Tips
                
                ✅ Best rectangles have 4+ touches total
                ✅ Narrower rectangles = clearer levels
                ✅ Combine with trend direction
                ✅ Watch for volume divergence
                ✅ Be patient - wait for clear setup
                ✅ Breakout direction often matches prior trend
                ✅ False breakouts common - wait for close
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the minimum number of touches needed to confirm a rectangle pattern?",
                        options = listOf("1 on each level", "2 on each level", "5 on each level", "10 on each level"),
                        correctAnswer = 1,
                        explanation = "A valid rectangle needs at least 2 touches on both support and resistance to confirm the pattern, though more touches increase reliability."
                    ),
                    QuizQuestion(
                        question = "How do you calculate the price target after a rectangle breakout?",
                        options = listOf("10% move", "Measure rectangle height and project from breakout", "Double the width", "No target exists"),
                        correctAnswer = 1,
                        explanation = "Measure the height of the rectangle (from support to resistance) and project that same distance from the breakout point to estimate the minimum price target."
                    ),
                    QuizQuestion(
                        question = "What confirms a valid rectangle breakout?",
                        options = listOf("Any price movement beyond level", "Close beyond level with high volume and follow-through", "Time passing", "Social media buzz"),
                        correctAnswer = 1,
                        explanation = "A valid breakout requires a close beyond the level (not just a wick), high volume (2-3x normal), and follow-through in the next candle to avoid false breakouts."
                    )
                )
            )
        ),
        
        Lesson(
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
        ),
        
        Lesson(
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
        ),
        
        Lesson(
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
        ),
        
        Lesson(
            id = 11,
            title = "Candlestick Basics",
            category = "Candlestick Patterns",
            duration = "10 min",
            content = """
                # Candlestick Basics
                
                ## Introduction
                
                Candlestick charts originated in 18th century Japan and have become the most popular charting method worldwide. They provide more information than line charts, displaying open, high, low, and close prices in a visual format that reveals market sentiment.
                
                ## Candlestick Anatomy
                
                ```
                        High
                         |
                         | Upper Shadow (Wick)
                         |
                    ┌────┐
                    │    │ ← Real Body
                    │    │   (Open to Close)
                    └────┘
                         |
                         | Lower Shadow (Wick)
                         |
                        Low
                ```
                
                ### Components:
                
                1. **Real Body**:
                   - Rectangle between open and close
                   - Green/White = Close > Open (Bullish)
                   - Red/Black = Close < Open (Bearish)
                   - Size indicates price movement strength
                
                2. **Upper Shadow (Wick)**:
                   - Line above body
                   - Shows highest price reached
                   - Indicates rejection at higher prices
                
                3. **Lower Shadow (Wick)**:
                   - Line below body
                   - Shows lowest price reached
                   - Indicates support/buying pressure
                
                ## Bullish Candle
                
                ```
                      High
                       |
                  ┌────┐
                  │    │ Green/White
                  │    │ Close
                  └────┘ Open
                       |
                      Low
                ```
                
                **Meaning**:
                - Price closed higher than it opened
                - Buyers in control during period
                - Bullish sentiment
                - Strength = body size
                
                ## Bearish Candle
                
                ```
                      High
                       |
                  ┌────┐ Open
                  │    │ Red/Black
                  │    │ Close
                  └────┘
                       |
                      Low
                ```
                
                **Meaning**:
                - Price closed lower than it opened
                - Sellers in control during period
                - Bearish sentiment
                - Strength = body size
                
                ## Candle Characteristics
                
                ### Body Size:
                
                **Long Body**:
                - Strong price movement
                - Clear directional conviction
                - High momentum
                - Decisive market
                
                **Short Body**:
                - Minimal price change
                - Indecision
                - Low momentum
                - Consolidation
                
                **No Body (Doji)**:
                - Open equals close
                - Maximum indecision
                - Potential reversal
                - Requires confirmation
                
                ### Shadow Length:
                
                **Long Upper Shadow**:
                - High rejection
                - Sellers overcame buyers
                - Resistance present
                - Bearish signal
                
                **Long Lower Shadow**:
                - Low rejection
                - Buyers overcame sellers
                - Support present
                - Bullish signal
                
                **No Shadows**:
                - Strong conviction
                - No intraday reversal
                - Marubozu candle
                - Powerful signal
                
                ## Time Periods
                
                Candlesticks can represent any timeframe:
                
                - **1 minute**: Day trading
                - **5/15 minute**: Scalping
                - **1 hour**: Intraday trading
                - **4 hour**: Swing trading
                - **Daily**: Position trading
                - **Weekly**: Long-term investing
                - **Monthly**: Macro analysis
                
                ## Reading Market Sentiment
                
                ### Strong Bullish:
                ```
                  ┌──────┐
                  │      │ Large green body
                  │      │ Small/no shadows
                  │      │
                  └──────┘
                ```
                - Large green body
                - Minimal upper shadow
                - Small/no lower shadow
                - Decisive buying
                
                ### Strong Bearish:
                ```
                  ┌──────┐
                  │      │ Large red body
                  │      │ Small/no shadows
                  │      │
                  └──────┘
                ```
                - Large red body
                - Minimal lower shadow
                - Small/no upper shadow
                - Decisive selling
                
                ### Indecision:
                ```
                     |
                  ┌──┐
                  │  │ Small body
                  └──┘ Long shadows
                     |
                ```
                - Small body (any color)
                - Long upper/lower shadows
                - Equal buying/selling pressure
                - Potential reversal
                
                ## Context is Critical
                
                ### Location Matters:
                
                **At Resistance**:
                - Long upper shadow = bearish
                - Rejection of higher prices
                - Potential reversal
                
                **At Support**:
                - Long lower shadow = bullish
                - Rejection of lower prices
                - Potential bounce
                
                **In Uptrend**:
                - Large green candles = strength
                - Small red candles = healthy pullback
                
                **In Downtrend**:
                - Large red candles = weakness
                - Small green candles = weak bounce
                
                ## Volume Confirmation
                
                ✅ **Strong Signals**:
                - Large candle + high volume = conviction
                - Reversal candle + volume spike = reliable
                - Breakout candle + volume = confirmed
                
                ❌ **Weak Signals**:
                - Large candle + low volume = suspect
                - Reversal candle + no volume = unreliable
                - Pattern without volume = caution
                
                ## Common Candle Types
                
                ### Marubozu:
                - No shadows (or very small)
                - Full-bodied candle
                - Strong directional move
                - High conviction
                
                ### Doji:
                - No body (open = close)
                - Indecision candle
                - Potential reversal
                - Needs confirmation
                
                ### Hammer:
                - Small body at top
                - Long lower shadow (2x+ body)
                - Bullish reversal at bottom
                - Shows rejection of lows
                
                ### Shooting Star:
                - Small body at bottom
                - Long upper shadow (2x+ body)
                - Bearish reversal at top
                - Shows rejection of highs
                
                ## Pro Tips
                
                ✅ Never trade single candle in isolation
                ✅ Always consider trend context
                ✅ Volume confirmation is essential
                ✅ Combine with support/resistance
                ✅ Multiple timeframe analysis
                ✅ Look for candle patterns (groups)
                ✅ Shadow length reveals intraday battle
                ✅ Body size shows session conviction
                
                ## Next Steps
                
                In the following lessons, we'll explore specific candlestick patterns:
                - Doji and Spinning Tops
                - Hammer and Hanging Man
                - Engulfing Patterns
                - Star Patterns
                - And many more...
                
                Understanding basic candlestick anatomy is foundation for recognizing these powerful patterns!
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What does a long upper shadow on a candlestick indicate?",
                        options = listOf("Strong buying", "Rejection at higher prices by sellers", "Support found", "Trend continuation"),
                        correctAnswer = 1,
                        explanation = "A long upper shadow shows that price moved higher during the period but was rejected and pushed back down, indicating sellers overcame buyers at higher levels."
                    ),
                    QuizQuestion(
                        question = "What does a small body (real body) on a candlestick signify?",
                        options = listOf("Strong conviction", "Indecision or minimal price change", "Always bullish", "Always bearish"),
                        correctAnswer = 1,
                        explanation = "A small body indicates that the open and close prices were very close, showing indecision in the market with minimal directional movement during that period."
                    ),
                    QuizQuestion(
                        question = "Why is volume confirmation important when analyzing candlesticks?",
                        options = listOf("It's not important", "High volume with strong candles shows conviction and reliability", "Volume only matters for stocks", "Volume determines candle color"),
                        correctAnswer = 1,
                        explanation = "Volume confirms the strength of a candle's signal. High volume with a strong candle shows real conviction, while low volume may indicate a weak or unreliable signal."
                    )
                )
            )
        ),
        
        Lesson(
            id = 12,
            title = "Doji and Spinning Tops",
            category = "Candlestick Patterns",
            duration = "8 min",
            content = """
                # Doji and Spinning Tops
                
                ## Overview
                
                Doji and Spinning Tops are indecision candles that signal potential reversals or continuation patterns depending on context. They appear when bulls and bears battle to a near-draw, with neither side gaining clear control. These patterns are critical for identifying market turning points.
                
                ## Doji Candlestick
                
                ### Definition:
                A Doji forms when opening and closing prices are virtually equal, creating little to no body. It represents maximum indecision and equilibrium between buyers and sellers.
                
                ### Basic Structure:
                ```
                     High
                      |
                      | Upper Shadow
                      |
                     ─── ← Doji (Open = Close)
                      |
                      | Lower Shadow
                      |
                     Low
                ```
                
                ## Types of Doji
                
                ### 1. Neutral Doji
                ```
                      |
                      |
                     ─── Equal shadows
                      |
                      |
                ```
                - **Characteristics**: Equal or similar shadows
                - **Meaning**: Perfect indecision
                - **Context**: Most neutral signal
                
                ### 2. Long-Legged Doji
                ```
                      |
                      | Very long
                      | shadows
                     ─── 
                      |
                      | Very long
                      |
                ```
                - **Characteristics**: Very long upper and lower shadows
                - **Meaning**: Extreme volatility, indecision
                - **Strength**: Strong reversal signal
                - **Psychology**: Major battle between bulls and bears
                
                ### 3. Dragonfly Doji
                ```
                     ───
                      |
                      | Long lower shadow
                      | only
                      |
                ```
                - **Characteristics**: Long lower shadow, no upper shadow
                - **Meaning**: Bullish reversal signal
                - **Best Location**: Support levels, downtrend bottoms
                - **Psychology**: Sellers pushed down, buyers took control
                
                ### 4. Gravestone Doji
                ```
                      |
                      | Long upper shadow
                      | only
                     ───
                ```
                - **Characteristics**: Long upper shadow, no lower shadow
                - **Meaning**: Bearish reversal signal
                - **Best Location**: Resistance levels, uptrend tops
                - **Psychology**: Buyers pushed up, sellers took control
                
                ## Spinning Top Candlestick
                
                ### Definition:
                A Spinning Top has a small real body with long upper and lower shadows. Unlike Doji, it has a small body, showing slight directional bias but still indicating indecision.
                
                ### Structure:
                ```
                      |
                      | Upper shadow
                      |
                    ┌─┐
                    │ │ Small body (any color)
                    └─┘
                      |
                      | Lower shadow
                      |
                ```
                
                ### Characteristics:
                - **Body**: Small (1/3 or less of total range)
                - **Shadows**: Long (both sides ideally)
                - **Color**: Can be bullish or bearish (less important)
                - **Meaning**: Indecision, potential reversal
                
                ## Trading Doji Patterns
                
                ### Doji at Support (Bullish):
                
                **Setup**:
                1. Downtrend or pullback in progress
                2. Doji forms at support level
                3. Shows selling exhaustion
                
                **Entry**:
                - Wait for bullish confirmation candle
                - Enter above Doji high
                - Or enter on support hold
                
                **Stop Loss**:
                - Below Doji low
                - Or below support level
                
                **Target**:
                - Next resistance level
                - Previous swing high
                - Risk:Reward ratio of 2:1 minimum
                
                ### Doji at Resistance (Bearish):
                
                **Setup**:
                1. Uptrend or rally in progress
                2. Doji forms at resistance
                3. Shows buying exhaustion
                
                **Entry**:
                - Wait for bearish confirmation candle
                - Enter below Doji low
                - Or at resistance rejection
                
                **Stop Loss**:
                - Above Doji high
                - Or above resistance level
                
                **Target**:
                - Next support level
                - Previous swing low
                - Risk:Reward ratio of 2:1 minimum
                
                ## Trading Spinning Tops
                
                ### Reversal Setup:
                
                **Bullish Reversal**:
                - Spinning Top after downtrend
                - At support level
                - Followed by bullish candle
                - Enter above confirmation
                
                **Bearish Reversal**:
                - Spinning Top after uptrend
                - At resistance level
                - Followed by bearish candle
                - Enter below confirmation
                
                ### Continuation Setup:
                
                **In Uptrend**:
                - Spinning Top = healthy pause
                - Brief consolidation
                - Trend often continues
                - Enter on breakout above
                
                **In Downtrend**:
                - Spinning Top = weak bounce
                - Brief hesitation
                - Downtrend often continues
                - Enter on break below
                
                ## Context is Everything
                
                ### High Reliability Locations:
                
                ✅ **At Support/Resistance**:
                - Maximum effectiveness
                - Clear risk/reward
                - Better probability
                
                ✅ **After Extended Moves**:
                - Trend exhaustion signal
                - Reversal more likely
                - High conviction setup
                
                ✅ **With Volume**:
                - Volume spike on Doji day
                - Shows real battle
                - Stronger signal
                
                ### Low Reliability Locations:
                
                ❌ **Mid-Trend**:
                - Often just noise
                - Low reversal probability
                - Better to ignore
                
                ❌ **Low Volume**:
                - Weak signal
                - Lack of conviction
                - Often fails
                
                ❌ **Without Confirmation**:
                - Never trade alone
                - Need following candle
                - Patience required
                
                ## Confirmation Requirements
                
                ### For Doji:
                
                **Bullish Confirmation**:
                - Next candle closes above Doji high
                - Ideally a strong green candle
                - Increasing volume
                
                **Bearish Confirmation**:
                - Next candle closes below Doji low
                - Ideally a strong red candle
                - Increasing volume
                
                ### For Spinning Top:
                
                **Bullish Confirmation**:
                - Next candle strong bullish close
                - Breaks above resistance if present
                - Volume increase
                
                **Bearish Confirmation**:
                - Next candle strong bearish close
                - Breaks below support if present
                - Volume increase
                
                ## Psychology
                
                ### Doji Psychology:
                
                **At Top**:
                - Buyers lose conviction
                - Profit-taking emerges
                - Uncertainty grows
                - Reversal potential
                
                **At Bottom**:
                - Sellers lose conviction
                - Bargain hunters emerge
                - Fear subsides
                - Reversal potential
                
                ### Spinning Top Psychology:
                
                - Bulls and bears in balance
                - Neither side dominant
                - Market pausing
                - Decision pending
                
                ## Common Mistakes
                
                ❌ Trading Doji without confirmation
                ❌ Ignoring trend and location context
                ❌ Expecting every Doji to reverse
                ❌ Not using stop losses
                ❌ Confusing Doji with small body candles
                ❌ Trading mid-trend Dojis
                ❌ Forgetting volume confirmation
                
                ## Pro Tips
                
                ✅ Doji after strong trend = powerful signal
                ✅ Multiple Dojis = extreme indecision
                ✅ Combine with support/resistance
                ✅ Wait for confirmation candle (always!)
                ✅ Volume spike increases reliability
                ✅ Spinning Tops = warning, not signal
                ✅ Use with other indicators (RSI, MACD)
                ✅ Higher timeframes more reliable
                ✅ Dragonfly at support = very bullish
                ✅ Gravestone at resistance = very bearish
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the key characteristic of a Doji candlestick?",
                        options = listOf("Large body", "Open and close prices are virtually equal", "Only green candles", "No shadows"),
                        correctAnswer = 1,
                        explanation = "A Doji forms when the opening and closing prices are virtually equal, creating little to no real body. This represents maximum indecision in the market."
                    ),
                    QuizQuestion(
                        question = "Where is a Dragonfly Doji most effective as a bullish reversal signal?",
                        options = listOf("At resistance", "At support levels or downtrend bottoms", "Mid-trend", "Anywhere"),
                        correctAnswer = 1,
                        explanation = "A Dragonfly Doji (long lower shadow, no upper shadow) is most effective at support levels or downtrend bottoms, where it shows sellers pushed price down but buyers took control."
                    ),
                    QuizQuestion(
                        question = "Should you trade a Doji pattern immediately when it appears?",
                        options = listOf("Yes, always trade immediately", "No, always wait for confirmation in the next candle", "Only if it's green", "Only on Mondays"),
                        correctAnswer = 1,
                        explanation = "Never trade a Doji alone. Always wait for confirmation in the next candle (bullish candle closing above for long, bearish closing below for short) to avoid false signals."
                    )
                )
            )
        ),
        
        Lesson(
            id = 13,
            title = "Hammer and Hanging Man",
            category = "Candlestick Patterns",
            duration = "7 min",
            content = """
                # Hammer and Hanging Man
                
                ## Overview
                
                The Hammer and Hanging Man are single candlestick patterns with identical structure but opposite implications based on their location. They have small bodies at the top of the range and long lower shadows, creating a hammer-like or hanging man appearance.
                
                ## Pattern Structure
                
                ```
                    ┌──┐
                    │  │ Small body (top of range)
                    └──┘
                     |
                     |  Long lower shadow
                     |  (at least 2x body length)
                     |
                ```
                
                ### Characteristics:
                - **Body**: Small, at upper end of range
                - **Color**: Can be bullish or bearish (less important)
                - **Lower Shadow**: Long (2-3x body length minimum)
                - **Upper Shadow**: None or very small
                - **Range**: Lower shadow represents 2/3+ of total range
                
                ## The Hammer (Bullish Reversal)
                
                ### Location:
                - **Appears**: At bottom of downtrend
                - **Context**: After selling pressure
                - **Support**: Often at support levels
                - **Timing**: Trend exhaustion
                
                ### Formation Psychology:
                
                **During the Period**:
                1. Sellers push price significantly lower
                2. Bears appear to be in control
                3. Buyers step in at lows
                4. Price rallies back up
                5. Closes near the high
                
                **Interpretation**:
                - Selling pressure rejected
                - Buyers overwhelmed sellers
                - Potential bottom forming
                - Bullish reversal signal
                
                ### Trading the Hammer:
                
                **Entry Strategy**:
                - **Aggressive**: Enter at close of hammer candle
                - **Conservative**: Wait for bullish confirmation candle
                - **Best**: Enter above confirmation candle high
                
                **Stop Loss**:
                - Below hammer low (shadow low)
                - Tight stop = better risk/reward
                - If stopped out, pattern failed
                
                **Target**:
                - Next resistance level
                - Previous swing high
                - 2:1 or 3:1 risk/reward minimum
                
                **Volume**:
                - Higher volume = stronger signal
                - Volume spike ideal
                - Low volume = caution
                
                ## The Hanging Man (Bearish Reversal)
                
                ### Location:
                - **Appears**: At top of uptrend
                - **Context**: After buying pressure
                - **Resistance**: Often at resistance levels
                - **Timing**: Trend exhaustion
                
                ### Formation Psychology:
                
                **During the Period**:
                1. Buyers start strong
                2. Price pushed significantly lower during session
                3. Bears show strength
                4. Price recovers to close near high
                5. But damage done - warning sign
                
                **Interpretation**:
                - Buyers losing control
                - Sellers gaining strength
                - Potential top forming
                - Bearish reversal warning
                
                ### Trading the Hanging Man:
                
                **Entry Strategy**:
                - **Never Alone**: Must have confirmation
                - **Confirmation**: Next candle closes below Hanging Man body
                - **Entry**: Below confirmation candle low
                
                **Stop Loss**:
                - Above Hanging Man high
                - Or above resistance level
                - Wider stop than Hammer (more risky pattern)
                
                **Target**:
                - Next support level
                - Previous swing low
                - 2:1 risk/reward minimum
                
                **Volume**:
                - High volume on Hanging Man = warning
                - High volume on confirmation = strong signal
                
                ## Key Differences
                
                | Feature | Hammer | Hanging Man |
                |---------|--------|-------------|
                | Location | Bottom of downtrend | Top of uptrend |
                | Signal | Bullish reversal | Bearish reversal |
                | Reliability | High (75%+) | Moderate (60%) |
                | Confirmation | Helpful | Essential |
                | Body Color | Less important | Bearish better |
                
                ## Confirmation Requirements
                
                ### Hammer Confirmation (Helpful):
                
                ✅ **Strong Confirmation**:
                - Next candle bullish close
                - Closes above Hammer high
                - High volume
                - Gap up opening
                
                ### Hanging Man Confirmation (Essential):
                
                ✅ **Must Have Confirmation**:
                - Next candle bearish close
                - Closes below Hanging Man low
                - High volume
                - Gap down opening ideal
                
                **Important**: Hanging Man WITHOUT confirmation is not tradeable!
                
                ## Ideal Pattern Characteristics
                
                ### Perfect Hammer:
                ✅ At support level
                ✅ After clear downtrend
                ✅ Lower shadow 3x+ body length
                ✅ Minimal or no upper shadow
                ✅ Body at top of range
                ✅ High volume
                ✅ Bullish confirmation candle
                
                ### Perfect Hanging Man:
                ✅ At resistance level
                ✅ After clear uptrend
                ✅ Lower shadow 3x+ body length
                ✅ Minimal or no upper shadow
                ✅ Red/bearish body preferred
                ✅ High volume
                ✅ Bearish confirmation essential
                
                ## Common Variations
                
                ### Inverted Hammer:
                - Long upper shadow
                - Small body at bottom
                - Bullish at bottoms
                - Needs confirmation
                
                ### Shooting Star:
                - Long upper shadow
                - Small body at bottom
                - Bearish at tops
                - Similar to Hanging Man
                
                ## Context Matters
                
                ### High Reliability:
                ✅ At major support/resistance
                ✅ After extended trend
                ✅ With high volume
                ✅ Multiple timeframe confirmation
                ✅ Near key Fibonacci levels
                ✅ At psychological price levels
                
                ### Low Reliability:
                ❌ Mid-trend appearance
                ❌ Low volume
                ❌ Weak prior trend
                ❌ No confirmation
                ❌ Conflicting indicators
                
                ## Common Mistakes
                
                ❌ Trading Hanging Man without confirmation
                ❌ Ignoring the prevailing trend
                ❌ Setting stops too tight
                ❌ Expecting perfect proportions
                ❌ Confusing with Doji or Spinning Top
                ❌ Trading every Hammer that appears
                ❌ Ignoring volume signals
                ❌ Not waiting for proper location
                
                ## Advanced Tips
                
                ### Enhanced Reliability:
                
                ✅ **RSI Divergence**: Hammer with bullish RSI divergence = powerful
                ✅ **Volume Profile**: Hammer at high volume node = strong support
                ✅ **Multiple Timeframes**: Hammer on daily + weekly = very strong
                ✅ **Trendline**: Hammer at trendline = perfect confluence
                ✅ **Round Numbers**: Hammer at psychological level = stronger
                
                ## Pro Tips
                
                ✅ Hammer more reliable than Hanging Man
                ✅ Longer shadow = stronger rejection
                ✅ Body color less important than location
                ✅ Always use confirmation on Hanging Man
                ✅ Combine with support/resistance
                ✅ Volume spike increases probability
                ✅ Best on daily or weekly charts
                ✅ Perfect patterns are rare - use judgment
                ✅ Risk/reward should be 2:1 minimum
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the key difference between a Hammer and Hanging Man pattern?",
                        options = listOf("Body color", "Their location - Hammer at bottoms, Hanging Man at tops", "Shadow length", "Volume"),
                        correctAnswer = 1,
                        explanation = "The patterns have identical structure but opposite meanings based on location. A Hammer appears at downtrend bottoms (bullish), while a Hanging Man appears at uptrend tops (bearish)."
                    ),
                    QuizQuestion(
                        question = "Is confirmation required for a Hanging Man pattern?",
                        options = listOf("No, trade immediately", "Yes, confirmation is essential", "Only in bear markets", "Only if volume is high"),
                        correctAnswer = 1,
                        explanation = "Confirmation is essential for Hanging Man patterns. You must wait for the next candle to close below the Hanging Man's body to confirm the bearish reversal before trading."
                    ),
                    QuizQuestion(
                        question = "What is the minimum recommended length for the lower shadow relative to the body?",
                        options = listOf("Same as body", "At least 2-3 times the body length", "Half the body", "10 times the body"),
                        correctAnswer = 1,
                        explanation = "The lower shadow should be at least 2-3 times the length of the body to qualify as a proper Hammer or Hanging Man, showing significant rejection of lower prices."
                    )
                )
            )
        ),
        
        Lesson(
            id = 14,
            title = "Engulfing Patterns",
            category = "Candlestick Patterns",
            duration = "8 min",
            content = """
                # Engulfing Patterns
                
                ## Overview
                
                Engulfing patterns are two-candle reversal patterns where the second candle's body completely engulfs (encompasses) the first candle's body. They signal strong shifts in market sentiment and are among the most reliable candlestick reversal patterns.
                
                ## Bullish Engulfing Pattern
                
                ### Structure:
                ```
                   1st    2nd
                   ┌─┐   ┌───┐
                   │ │   │   │ Large green
                   └─┘   │   │ engulfs small red
                  Small  │   │
                  Red    └───┘
                  
                ← Downtrend  Reversal →
                ```
                
                ### Requirements:
                1. **Trend**: Appears in downtrend
                2. **First Candle**: Small bearish (red) candle
                3. **Second Candle**: Larger bullish (green) candle
                4. **Engulfing**: Second body completely engulfs first body
                5. **Opens**: Below first candle close
                6. **Closes**: Above first candle open
                
                ### Psychology:
                - **Day 1**: Bears in control, continuing downtrend
                - **Day 2**: Opens lower (bears still confident)
                - **Day 2**: Bulls take over, drive price higher
                - **Close**: Bulls completely reverse prior session
                - **Result**: Power shift from bears to bulls
                
                ## Bearish Engulfing Pattern
                
                ### Structure:
                ```
                   1st    2nd
                   ┌─┐   ┌───┐ Large red
                   │ │   │   │ engulfs small green
                   └─┘   │   │
                  Small  │   │
                  Green  └───┘
                  
                ← Uptrend  Reversal →
                ```
                
                ### Requirements:
                1. **Trend**: Appears in uptrend
                2. **First Candle**: Small bullish (green) candle
                3. **Second Candle**: Larger bearish (red) candle
                4. **Engulfing**: Second body completely engulfs first body
                5. **Opens**: Above first candle close
                6. **Closes**: Below first candle open
                
                ### Psychology:
                - **Day 1**: Bulls in control, continuing uptrend
                - **Day 2**: Opens higher (bulls still confident)
                - **Day 2**: Bears take over, drive price lower
                - **Close**: Bears completely reverse prior session
                - **Result**: Power shift from bulls to bears
                
                ## Trading Bullish Engulfing
                
                ### Entry Strategies:
                
                **Aggressive Entry**:
                - Enter at close of engulfing candle
                - Or at open of next candle
                - Higher risk, better price
                
                **Conservative Entry**:
                - Wait for pullback to engulfing candle high
                - Or wait for third candle confirmation
                - Lower risk, may miss move
                
                **Best Practice**:
                - Enter on break above engulfing candle high
                - Confirms buyers still in control
                - Good risk/reward balance
                
                ### Stop Loss:
                - Below engulfing candle low
                - Or below both candles' lows
                - Should be relatively tight
                
                ### Target:
                - Next resistance level
                - Previous swing high
                - 2:1 or 3:1 risk/reward ratio
                - Trailing stop for extended moves
                
                ## Trading Bearish Engulfing
                
                ### Entry Strategies:
                
                **Aggressive Entry**:
                - Short at close of engulfing candle
                - Or at open of next candle
                - Higher risk entry
                
                **Conservative Entry**:
                - Wait for pullback to engulfing candle low
                - Or wait for confirmation candle
                - Safer but may miss move
                
                **Best Practice**:
                - Short on break below engulfing candle low
                - Confirms sellers in control
                - Better probability
                
                ### Stop Loss:
                - Above engulfing candle high
                - Or above both candles' highs
                - Wider than bullish stops typically
                
                ### Target:
                - Next support level
                - Previous swing low
                - 2:1 risk/reward minimum
                - Trailing stop for big moves
                
                ## Pattern Strength Factors
                
                ### High Reliability Engulfing:
                
                ✅ **Ideal Characteristics**:
                - Large engulfing candle (3x+ first candle)
                - At major support/resistance
                - After extended trend
                - High volume on engulfing candle (2x+ average)
                - Complete body engulfment (not just wicks)
                - Gap between candles
                - Multiple timeframe confirmation
                
                ### Moderate Reliability:
                
                ⚠️ **Acceptable but Weaker**:
                - Moderate size engulfing candle
                - Mid-trend appearance
                - Average volume
                - Shadows engulf but bodies barely
                
                ### Low Reliability:
                
                ❌ **Avoid These**:
                - Small engulfing candle
                - Similar sized candles
                - Low volume
                - Against major trend
                - No support/resistance nearby
                
                ## Volume Analysis
                
                ### Bullish Engulfing Volume:
                ✅ **Strong Signal**:
                - Low volume on bearish candle (selling exhaustion)
                - High volume on bullish candle (2x+ average)
                - Shows conviction in reversal
                
                ### Bearish Engulfing Volume:
                ✅ **Strong Signal**:
                - Low volume on bullish candle (buying exhaustion)
                - High volume on bearish candle (2x+ average)
                - Confirms distribution
                
                ## Pattern Variations
                
                ### Last Engulfing:
                - Third+ engulfing pattern in series
                - Usually strongest
                - Final capitulation
                - High probability reversal
                
                ### Piercing Pattern (Bullish):
                - Similar to bullish engulfing
                - Second candle closes above 50% of first
                - Doesn't fully engulf
                - Slightly weaker signal
                
                ### Dark Cloud Cover (Bearish):
                - Similar to bearish engulfing
                - Second candle closes below 50% of first
                - Doesn't fully engulf
                - Slightly weaker signal
                
                ## Context and Location
                
                ### Best Locations:
                
                ✅ **High Probability Zones**:
                - Major support/resistance levels
                - Fibonacci retracement levels (38.2%, 61.8%)
                - Round psychological numbers
                - Previous swing highs/lows
                - Trendline touches
                - Gap fill levels
                
                ### Poor Locations:
                
                ❌ **Low Probability Zones**:
                - Middle of trading range
                - No clear trend
                - Far from support/resistance
                - Low volume areas
                
                ## Confirmation Signals
                
                ### Additional Confirmation:
                
                ✅ **Increases Probability**:
                - RSI showing divergence
                - MACD crossover
                - Stochastic in extreme zone
                - Bollinger Band touch
                - Moving average support/resistance
                - Multiple timeframe agreement
                
                ## Common Mistakes
                
                ❌ Trading every engulfing pattern
                ❌ Ignoring volume confirmation
                ❌ Not checking trend context
                ❌ Setting stops too tight
                ❌ Expecting perfect engulfment
                ❌ Trading against major trend
                ❌ Forgetting support/resistance
                ❌ Not using multiple timeframes
                
                ## Pro Tips
                
                ✅ Size matters - bigger engulfing = stronger signal
                ✅ Volume confirmation is critical
                ✅ Best at major support/resistance
                ✅ Multiple timeframe analysis essential
                ✅ Combine with momentum indicators
                ✅ Wait for proper location
                ✅ Gaps between candles add strength
                ✅ After extended trend = more reliable
                ✅ Third engulfing often strongest
                ✅ Always check overall market context
                ✅ Risk/reward should be 2:1 minimum
                ✅ Use trailing stops for big winners
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the key requirement for an Engulfing pattern?",
                        options = listOf("Both candles same color", "Second candle's body completely engulfs first candle's body", "First candle must be large", "Must occur on Monday"),
                        correctAnswer = 1,
                        explanation = "The defining characteristic of an Engulfing pattern is that the second candle's real body must completely engulf (encompass) the first candle's real body, showing a strong power shift."
                    ),
                    QuizQuestion(
                        question = "What volume pattern strengthens an Engulfing pattern?",
                        options = listOf("Low volume throughout", "High volume on engulfing candle (2x+ average)", "Volume doesn't matter", "Decreasing volume"),
                        correctAnswer = 1,
                        explanation = "High volume on the engulfing candle (ideally 2x or more above average) confirms strong conviction in the reversal and significantly increases pattern reliability."
                    ),
                    QuizQuestion(
                        question = "Where are Engulfing patterns most reliable?",
                        options = listOf("Anywhere in the chart", "At major support/resistance levels after extended trends", "Only in sideways markets", "During low volatility"),
                        correctAnswer = 1,
                        explanation = "Engulfing patterns are most reliable when they appear at major support/resistance levels after extended trends, where they signal exhaustion and potential reversal points."
                    )
                )
            )
        ),
        
        Lesson(
            id = 15,
            title = "Morning and Evening Star",
            category = "Candlestick Patterns",
            duration = "9 min",
            content = """
                # Morning Star and Evening Star Patterns
                
                ## Overview
                
                Morning Star and Evening Star are three-candle reversal patterns that signal potential trend changes. They are among the most reliable candlestick patterns, especially when appearing at key support or resistance levels with proper volume confirmation.
                
                ## Morning Star (Bullish Reversal)
                
                ### Structure:
                ```
                1st      2nd     3rd
                ┌──┐             ┌────┐
                │  │    ─ or ┌┐  │    │ Large
                │  │      Doji   │    │ Green
                │  │       ┌┐    │    │
                └──┘       └┘    └────┘
                Large    Small    Large
                Red      Body     Green
                
                ← Downtrend  |  Reversal →
                ```
                
                ### Three Candles:
                
                **First Candle** (Day 1):
                - Large bearish (red) candle
                - Continues the downtrend
                - Shows sellers in control
                - Strong momentum downward
                
                **Second Candle** (Day 2) - The "Star":
                - Small body (any color)
                - Often a Doji or Spinning Top
                - Gaps down from first candle (ideal)
                - Shows indecision/equilibrium
                - Buying and selling balanced
                
                **Third Candle** (Day 3):
                - Large bullish (green) candle
                - Closes well into first candle's body
                - Ideally closes above 50% of first candle
                - Confirms bullish reversal
                - Buyers take control
                
                ### Psychology:
                1. **Day 1**: Bears dominate, strong selling
                2. **Day 2**: Gap down shows continued pessimism, but small body shows weakening
                3. **Day 3**: Bulls overwhelm bears, gap up, strong close
                4. **Result**: Complete sentiment reversal
                
                ## Evening Star (Bearish Reversal)
                
                ### Structure:
                ```
                1st      2nd     3rd
                ┌────┐           ┌──┐
                │    │  ─ or ┌┐  │  │ Large
                │    │    Doji   │  │ Red
                │    │     ┌┐    │  │
                └────┘     └┘    └──┘
                Large    Small    Large
                Green    Body     Red
                
                ← Uptrend   |  Reversal →
                ```
                
                ### Three Candles:
                
                **First Candle** (Day 1):
                - Large bullish (green) candle
                - Continues the uptrend
                - Bulls in control
                - Strong upward momentum
                
                **Second Candle** (Day 2) - The "Star":
                - Small body (any color)
                - Often Doji or Spinning Top
                - Gaps up from first candle (ideal)
                - Shows hesitation at highs
                - Equilibrium reached
                
                **Third Candle** (Day 3):
                - Large bearish (red) candle
                - Closes well into first candle's body
                - Ideally closes below 50% of first candle
                - Confirms bearish reversal
                - Sellers take control
                
                ### Psychology:
                1. **Day 1**: Bulls dominate, euphoria
                2. **Day 2**: Gap up but can't sustain, warning
                3. **Day 3**: Bears overwhelm bulls, panic selling
                4. **Result**: Sentiment shift from bullish to bearish
                
                ## Trading the Morning Star
                
                ### Entry Strategy:
                
                **Aggressive**:
                - Enter at close of third candle
                - Risk: No additional confirmation
                - Benefit: Best price
                
                **Conservative**:
                - Wait for fourth candle to confirm
                - Enter on break above Morning Star high
                - Lower risk, better confirmation
                
                **Best Practice**:
                - Enter above third candle high
                - Confirms buyers maintaining control
                - Good risk/reward setup
                
                ### Stop Loss:
                - Below the low of second candle (star)
                - Or below low of entire pattern
                - Tight stop possible due to pattern structure
                
                ### Price Target:
                - Next resistance level
                - Previous swing high
                - 2:1 or 3:1 risk/reward ratio minimum
                - Fibonacci extension levels
                
                ## Trading the Evening Star
                
                ### Entry Strategy:
                
                **Aggressive**:
                - Short at close of third candle
                - Higher risk entry
                - Best price if pattern holds
                
                **Conservative**:
                - Wait for confirmation candle
                - Enter on break below Evening Star low
                - More reliable, less risk
                
                **Best Practice**:
                - Short below third candle low
                - Confirms sellers in control
                - Better probability
                
                ### Stop Loss:
                - Above the high of second candle (star)
                - Or above high of entire pattern
                - Account for volatility
                
                ### Price Target:
                - Next support level
                - Previous swing low
                - 2:1 risk/reward minimum
                - Measured move from prior trend
                
                ## Pattern Variations
                
                ### Doji Star:
                - Second candle is perfect Doji
                - Strongest version
                - Maximum indecision
                - Most reliable reversal
                
                ### Abandoned Baby (Rare):
                - Gaps on both sides of star
                - Star doesn't touch other candles
                - Very rare and very powerful
                - Extremely reliable
                
                ### Regular Star:
                - Second candle small body (not Doji)
                - Still valid pattern
                - Slightly less reliable
                - More common
                
                ## Reliability Factors
                
                ### High Reliability Signals:
                
                ✅ **Morning Star**:
                - At major support level
                - After extended downtrend
                - Star gaps down significantly
                - Third candle gaps up
                - Third candle closes >50% into first
                - High volume on third candle
                - Multiple timeframe confluence
                
                ✅ **Evening Star**:
                - At major resistance level
                - After extended uptrend
                - Star gaps up significantly
                - Third candle gaps down
                - Third candle closes <50% into first
                - High volume on third candle
                - Bearish divergence on indicators
                
                ### Volume Analysis:
                
                **Ideal Volume Pattern**:
                - **First Candle**: High volume (trend continuation)
                - **Second Candle**: Declining volume (indecision)
                - **Third Candle**: Increasing volume (reversal confirmation)
                - **Breakout**: Volume surge (strong signal)
                
                ## Context Requirements
                
                ### Best Locations:
                
                ✅ **High Probability**:
                - Major support/resistance levels
                - Trend exhaustion points
                - Fibonacci retracement levels (50%, 61.8%)
                - Round psychological numbers
                - Previous swing highs/lows
                - Key moving average levels
                
                ### Poor Locations:
                
                ❌ **Low Probability**:
                - Middle of trading range
                - No clear trend before pattern
                - Away from key levels
                - Low volume environment
                - Choppy, directionless market
                
                ## Confirmation Indicators
                
                ### Additional Confirmation:
                
                ✅ **Strengthens Signal**:
                - **RSI**: Oversold (Morning Star) / Overbought (Evening Star)
                - **MACD**: Bullish crossover / Bearish crossover
                - **Stochastic**: Turning from extreme
                - **Moving Averages**: Price crossing MA
                - **Trendline**: Break of trendline
                - **Volume**: Surge on third candle
                
                ## Common Mistakes
                
                ❌ Trading without proper trend context
                ❌ Ignoring the gap requirement (weaker without)
                ❌ Not waiting for third candle to close
                ❌ Expecting perfect symmetry
                ❌ Setting stops too tight
                ❌ Ignoring volume signals
                ❌ Trading mid-trend stars
                ❌ Not checking support/resistance
                ❌ Missing the bigger picture trend
                
                ## Comparison to Other Patterns
                
                | Feature | Morning/Evening Star | Engulfing | Hammer/Shooting Star |
                |---------|---------------------|-----------|---------------------|
                | Candles | 3 | 2 | 1 |
                | Reliability | Very High | High | Moderate-High |
                | Gaps | Preferred | Not required | Not applicable |
                | Time to Form | 3 periods | 2 periods | 1 period |
                
                ## Pro Tips
                
                ✅ Gaps increase reliability significantly
                ✅ Star candle as Doji = strongest signal
                ✅ Third candle should be decisive (large)
                ✅ Volume crucial - third candle needs surge
                ✅ Best at major support/resistance
                ✅ Combine with momentum indicators
                ✅ Multiple timeframe confirmation powerful
                ✅ Pattern works on all timeframes
                ✅ Patience - wait for full pattern
                ✅ Third candle closing >50% into first = ideal
                ✅ Morning Star more reliable than Evening Star
                ✅ Always consider broader market context
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "How many candles form a Morning Star or Evening Star pattern?",
                        options = listOf("1 candle", "2 candles", "3 candles", "5 candles"),
                        correctAnswer = 2,
                        explanation = "Morning Star and Evening Star patterns are three-candle patterns consisting of a large trending candle, a small star candle showing indecision, and a large reversal candle."
                    ),
                    QuizQuestion(
                        question = "What makes the 'Abandoned Baby' variation more powerful?",
                        options = listOf("Larger candles", "Gaps on both sides of the star candle", "More volume", "Longer timeframe"),
                        correctAnswer = 1,
                        explanation = "The Abandoned Baby variation is very powerful because the star candle gaps away from both the first and third candles, creating complete separation and showing extreme reversal momentum."
                    ),
                    QuizQuestion(
                        question = "Where should the third candle ideally close in a Morning Star pattern?",
                        options = listOf("At the low", "Above 50% into the first candle's body", "Below the first candle", "At the star level"),
                        correctAnswer = 1,
                        explanation = "The third candle in a Morning Star should ideally close above 50% of the first candle's body, showing that bulls have completely reversed the prior bearish momentum."
                    )
                )
            )
        ),
        
        Lesson(
            id = 16,
            title = "Three White Soldiers and Three Black Crows",
            category = "Candlestick Patterns",
            duration = "7 min",
            content = """
                # Three White Soldiers and Three Black Crows
                
                ## Overview
                
                Three White Soldiers and Three Black Crows are powerful three-candle reversal patterns that signal strong momentum shifts. They consist of three consecutive candles in the same direction, each opening within the previous candle's body and closing progressively higher (soldiers) or lower (crows).
                
                ## Three White Soldiers (Bullish Reversal)
                
                ### Structure:
                ```
                        3rd ┌────┐
                            │    │
                   2nd  ┌───┤    │
                        │   │    │
                1st ┌───┤   │    │
                    │   │   │    │
                    │   │   │    │
                    └───┴───┴────┘
                    
                ← Downtrend | Strong Reversal →
                ```
                
                ### Characteristics:
                
                **First Candle**:
                - Large bullish (green) candle
                - Appears after downtrend
                - Opens near low, closes near high
                - Minimal upper shadow
                - First sign of reversal
                
                **Second Candle**:
                - Large bullish (green) candle
                - Opens within first candle's body
                - Closes higher than first
                - Similar or larger size
                - Confirms buying pressure
                
                **Third Candle**:
                - Large bullish (green) candle
                - Opens within second candle's body
                - Closes higher than second
                - Similar size to previous
                - Completes the pattern
                
                ### Requirements:
                ✅ All three candles bullish (green)
                ✅ Each opens within previous candle's body
                ✅ Each closes progressively higher
                ✅ Large bodies (minimal shadows)
                ✅ Similar sizes (consistency)
                ✅ Appears after downtrend
                ✅ Increasing or high volume
                
                ### Psychology:
                1. **First Candle**: Bears losing control, bulls emerging
                2. **Second Candle**: Bulls gaining confidence, momentum building
                3. **Third Candle**: Bulls dominate, strong conviction
                4. **Result**: Clear trend reversal from bearish to bullish
                
                ## Three Black Crows (Bearish Reversal)
                
                ### Structure:
                ```
                    ┌───┬───┬────┐
                    │   │   │    │
                1st │   │   │ 3rd│
                    │   │ 2nd    │
                    │   └────    │
                    └────────────┘
                    
                ← Uptrend | Strong Reversal →
                ```
                
                ### Characteristics:
                
                **First Candle**:
                - Large bearish (red) candle
                - Appears after uptrend
                - Opens near high, closes near low
                - Minimal lower shadow
                - Warning sign
                
                **Second Candle**:
                - Large bearish (red) candle
                - Opens within first candle's body
                - Closes lower than first
                - Similar or larger size
                - Confirms selling pressure
                
                **Third Candle**:
                - Large bearish (red) candle
                - Opens within second candle's body
                - Closes lower than second
                - Similar size to previous
                - Completes the pattern
                
                ### Requirements:
                ✅ All three candles bearish (red)
                ✅ Each opens within previous candle's body
                ✅ Each closes progressively lower
                ✅ Large bodies (minimal shadows)
                ✅ Similar sizes (consistency)
                ✅ Appears after uptrend
                ✅ Increasing or high volume
                
                ### Psychology:
                1. **First Candle**: Bulls losing grip, bears awakening
                2. **Second Candle**: Bears gaining strength, fear building
                3. **Third Candle**: Bears dominate, panic selling
                4. **Result**: Clear trend reversal from bullish to bearish
                
                ## Trading Three White Soldiers
                
                ### Entry Strategy:
                
                **Aggressive**:
                - Enter during third candle
                - Or at close of third candle
                - Higher risk, better price
                
                **Conservative**:
                - Wait for fourth candle confirmation
                - Or wait for pullback
                - Enter on break above pattern high
                
                **Best Practice**:
                - Enter on break above third candle high
                - Stop below pattern low
                - 2:1 risk/reward minimum
                
                ### Stop Loss:
                - Below low of three candles
                - Or below low of first candle
                - Can be wider stop due to pattern size
                
                ### Target:
                - Next resistance level
                - Previous swing high
                - Measured move (pattern height added to breakout)
                - 3:1 risk/reward ideal
                
                ## Trading Three Black Crows
                
                ### Entry Strategy:
                
                **Aggressive**:
                - Short during third candle
                - Or at close of third candle
                - Higher risk entry
                
                **Conservative**:
                - Wait for pullback to pattern low
                - Or wait for confirmation candle
                - Enter on break below pattern low
                
                **Best Practice**:
                - Short on break below third candle low
                - Stop above pattern high
                - 2:1 risk/reward minimum
                
                ### Stop Loss:
                - Above high of three candles
                - Or above high of first candle
                - Wider stop acceptable for pattern
                
                ### Target:
                - Next support level
                - Previous swing low
                - Measured move (pattern height subtracted)
                - 3:1 risk/reward ideal
                
                ## Pattern Strength Indicators
                
                ### Strong Three White Soldiers:
                ✅ At major support level
                ✅ After extended downtrend (30%+ decline)
                ✅ Candles of equal size
                ✅ Minimal upper shadows
                ✅ Opens in lower half of prior candle
                ✅ Increasing volume each day
                ✅ No gaps between candles
                
                ### Strong Three Black Crows:
                ✅ At major resistance level
                ✅ After extended uptrend (30%+ rally)
                ✅ Candles of equal size
                ✅ Minimal lower shadows
                ✅ Opens in upper half of prior candle
                ✅ Increasing volume each day
                ✅ No gaps between candles
                
                ## Volume Analysis
                
                ### Ideal Volume Pattern:
                
                **Three White Soldiers**:
                - First candle: Above average volume
                - Second candle: Increasing volume
                - Third candle: High volume (confirmation)
                - Shows strong buying conviction
                
                **Three Black Crows**:
                - First candle: Above average volume
                - Second candle: Increasing volume
                - Third candle: High volume (confirmation)
                - Shows strong selling pressure
                
                ## Warning Signs (Weaker Patterns)
                
                ### Advance Block (Soldiers):
                ❌ **Weakening Pattern**:
                - Each candle smaller than previous
                - Long upper shadows appearing
                - Declining volume
                - Indicates weakening momentum
                - May not be sustainable
                - Proceed with caution
                
                ### Deliberation (Crows):
                ❌ **Weakening Pattern**:
                - Third candle significantly smaller
                - Long shadows appearing
                - Volume declining
                - May indicate exhaustion
                - Reversal may fail
                
                ## Common Mistakes
                
                ❌ Trading without clear prior trend
                ❌ Ignoring candle size consistency
                ❌ Accepting patterns with large shadows
                ❌ Not confirming with volume
                ❌ Entering before pattern completes
                ❌ Setting stops too tight
                ❌ Expecting perfect textbook patterns
                ❌ Missing the advance block/deliberation warning
                
                ## Pattern Variations
                
                ### Identical Three Soldiers:
                - All three candles nearly identical
                - Opens at same relative position
                - Very strong signal
                - Rare but powerful
                
                ### White Soldiers After Star:
                - Soldiers after Morning Star
                - Combined reversal signals
                - Extremely bullish
                - High probability setup
                
                ## Context and Timing
                
                ### Best Locations:
                ✅ Major support (Soldiers) / resistance (Crows)
                ✅ After extended trend (20-30%+)
                ✅ At Fibonacci levels
                ✅ Near round psychological numbers
                ✅ After capitulation/euphoria
                ✅ Multiple timeframe alignment
                
                ### Poor Locations:
                ❌ Mid-trend (not reversal)
                ❌ Consolidation zones
                ❌ No clear prior trend
                ❌ Low volume environment
                
                ## Pro Tips
                
                ✅ Pattern more reliable with increasing volume
                ✅ Candles should be consistent in size
                ✅ Minimal shadows = strong conviction
                ✅ Each open in lower/upper half of prior = stronger
                ✅ Best after 30%+ trend move
                ✅ Combine with support/resistance
                ✅ RSI divergence adds confirmation
                ✅ Pattern works on all timeframes
                ✅ Three White Soldiers more reliable than Crows
                ✅ Watch for advance block weakness
                ✅ Use trailing stops to maximize profits
                ✅ Always confirm prior trend exists
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is a key requirement for Three White Soldiers pattern?",
                        options = listOf("Gaps between candles", "Each candle opens within the previous candle's body", "Decreasing size", "Long shadows"),
                        correctAnswer = 1,
                        explanation = "A key requirement is that each successive candle must open within the previous candle's body, showing sustained and controlled buying pressure rather than gaps or erratic behavior."
                    ),
                    QuizQuestion(
                        question = "What does an 'Advance Block' indicate in a Three White Soldiers pattern?",
                        options = listOf("Very strong signal", "Weakening momentum - each candle smaller with long shadows", "Need more soldiers", "Perfect pattern"),
                        correctAnswer = 1,
                        explanation = "An Advance Block occurs when each candle becomes progressively smaller with long upper shadows, indicating weakening momentum and suggesting the rally may not be sustainable."
                    ),
                    QuizQuestion(
                        question = "What volume pattern strengthens Three Black Crows?",
                        options = listOf("Decreasing volume", "Constant volume", "Increasing volume through all three candles", "No volume"),
                        correctAnswer = 2,
                        explanation = "Increasing volume through all three candles confirms strong and growing selling pressure, making the bearish reversal more reliable and likely to continue."
                    )
                )
            )
        ),
        
        Lesson(
            id = 17,
            title = "Support and Resistance",
            category = "Technical Analysis Fundamentals",
            duration = "12 min",
            content = """
                # Support and Resistance
                
                ## Overview
                
                Support and Resistance are foundational concepts in technical analysis. They represent price levels where buying or selling pressure is strong enough to reverse or pause price movements. Understanding these levels is crucial for making informed trading decisions.
                
                ## What is Support?
                
                ### Definition:
                Support is a price level where buying pressure is expected to be strong enough to prevent the price from falling further. It acts as a "floor" that prevents price from declining.
                
                ### Visual Representation:
                ```
                Price
                  ↑
                  |     /\      /\
                  |    /  \    /  \
                  |   /    \  /    \
                  |  /      \/      \___
                  |_/_____________________ ← Support Level
                  |  Bounce  Bounce  Hold
                  └──────────────────────→ Time
                ```
                
                ### Why Support Forms:
                
                1. **Psychological Levels**: Round numbers (e.g., $50, $100)
                2. **Previous Lows**: Historical price points
                3. **Moving Averages**: Dynamic support levels
                4. **Trendlines**: Ascending trend support
                5. **Fibonacci Levels**: Mathematical support zones
                
                ### Psychology:
                - Traders remember prior lows
                - Buyers see value at lower prices
                - Short-sellers take profits
                - Bargain hunters accumulate
                - Fear of missing bottom
                
                ## What is Resistance?
                
                ### Definition:
                Resistance is a price level where selling pressure is expected to be strong enough to prevent the price from rising further. It acts as a "ceiling" that prevents price from advancing.
                
                ### Visual Representation:
                ```
                Price
                  ↑  _____________________  ← Resistance Level
                  |  Reject  Reject  Reject
                  |  \      /\      /
                  |   \    /  \    /
                  |    \  /    \  /
                  |     \/      \/
                  └──────────────────────→ Time
                ```
                
                ### Why Resistance Forms:
                
                1. **Psychological Levels**: Round numbers
                2. **Previous Highs**: Historical resistance
                3. **Moving Averages**: Dynamic resistance
                4. **Trendlines**: Descending trend resistance
                5. **Supply Zones**: Areas of selling interest
                
                ### Psychology:
                - Traders remember prior highs
                - Sellers emerge at higher prices
                - Profit-taking occurs
                - Trapped buyers exit breakeven
                - Fear of buying top
                
                ## Key Principles
                
                ### 1. Role Reversal:
                
                **Support Becomes Resistance**:
                ```
                Price         Prior Support → Now Resistance
                  ↑           _______________↓___
                  |              /\      Reject
                  |             /  \       |
                  |  Support   /    \      |
                  |  ________\/______\___  |
                  |  Break                 
                  └──────────────────────→ Time
                ```
                
                When support breaks, it often becomes resistance because:
                - Buyers trapped above feel relieved to exit breakeven
                - Sellers reinforce at known level
                - Psychological memory of the level
                
                **Resistance Becomes Support**:
                ```
                Price      Prior Resistance → Now Support
                  ↑           ___________
                  |          /           \___/\___
                  |  Resist /                 Hold
                  |  ______/
                  |  Breakout → New Support
                  └──────────────────────→ Time
                ```
                
                When resistance breaks, it often becomes support because:
                - Breakout buyers defend their entry
                - New buyers see value
                - Trapped short-sellers cover
                
                ### 2. Strength of Levels:
                
                **Strong Support/Resistance**:
                ✅ Multiple touches (3+)
                ✅ High volume at level
                ✅ Long time period
                ✅ Round psychological numbers
                ✅ Confluence with other indicators
                ✅ Clear reactions
                
                **Weak Support/Resistance**:
                ❌ Only 1-2 touches
                ❌ Low volume
                ❌ Short time period
                ❌ Unclear reactions
                ❌ No confluence
                
                ### 3. Zones vs. Lines:
                
                Support and Resistance are better viewed as ZONES rather than exact price lines:
                
                ```
                Resistance Zone: $100-$102
                ________________|||||||||____
                               Zone (not line)
                ```
                
                **Why Zones?**:
                - Prices rarely respect exact levels
                - Different traders have different perspectives
                - Spreads and slippage
                - Market inefficiency
                - Allows for flexibility
                
                ## Types of Support and Resistance
                
                ### 1. Horizontal Support/Resistance:
                - Based on prior swing highs/lows
                - Most common type
                - Clear visual levels
                - Easy to identify
                
                ### 2. Dynamic Support/Resistance:
                - Moving averages (20, 50, 200 SMA)
                - Changes with price
                - Adapts to trend
                - Examples: 50 EMA often acts as support in uptrends
                
                ### 3. Trendline Support/Resistance:
                - Diagonal lines connecting swing points
                - Shows trend direction
                - Can be ascending or descending
                - More advanced concept
                
                ### 4. Psychological Levels:
                - Round numbers ($50, $100, $1000)
                - Human psychology
                - Often self-fulfilling
                - Very common in forex
                
                ## Trading with Support and Resistance
                
                ### Strategy 1: Bounce Trading
                
                **At Support** (Buy):
                1. Identify strong support level
                2. Wait for price to approach support
                3. Look for reversal signals (hammer, bullish engulfing)
                4. Enter long near support
                5. Stop below support
                6. Target next resistance
                
                **At Resistance** (Sell):
                1. Identify strong resistance level
                2. Wait for price to approach resistance
                3. Look for reversal signals (shooting star, bearish engulfing)
                4. Enter short near resistance
                5. Stop above resistance
                6. Target next support
                
                ### Strategy 2: Breakout Trading
                
                **Support Breakout** (Short):
                1. Identify support level
                2. Watch for breakdown
                3. Wait for close below support
                4. Confirm with volume
                5. Enter short on retest
                6. Stop above broken support (now resistance)
                7. Target measured move or next support
                
                **Resistance Breakout** (Long):
                1. Identify resistance level
                2. Watch for breakout
                3. Wait for close above resistance
                4. Confirm with volume
                5. Enter long on pullback
                6. Stop below broken resistance (now support)
                7. Target measured move or next resistance
                
                ## Identifying Strong Levels
                
                ### Checklist for Valid Support:
                ✅ **Number of Touches**: 2-3+ bounces
                ✅ **Time Period**: Level held for weeks/months
                ✅ **Volume**: High volume reactions
                ✅ **Clarity**: Clear, obvious bounces
                ✅ **Confluence**: Aligns with MA, Fibonacci, etc.
                ✅ **Market Structure**: Makes sense in bigger picture
                
                ### Checklist for Valid Resistance:
                ✅ **Number of Touches**: 2-3+ rejections
                ✅ **Time Period**: Held for extended time
                ✅ **Volume**: High volume at rejections
                ✅ **Clarity**: Clear, obvious resistance
                ✅ **Confluence**: Multiple indicators align
                ✅ **Psychology**: Round number or significant high
                
                ## Common Mistakes
                
                ❌ Drawing too many lines (cluttered chart)
                ❌ Treating levels as exact prices vs. zones
                ❌ Ignoring volume at levels
                ❌ Not considering timeframes
                ❌ Trading every touch without confirmation
                ❌ Forgetting about role reversal
                ❌ Setting stops exactly at levels (use buffer)
                ❌ Expecting perfect bounces
                ❌ Drawing lines after the fact (hindsight bias)
                ❌ Not adapting as new levels form
                
                ## Advanced Concepts
                
                ### Confluence Zones:
                Multiple factors aligning at same price:
                - Support + 200 SMA + 61.8% Fib = Strong zone
                - Resistance + trendline + round number = Very strong
                
                ### Support/Resistance Clusters:
                - Multiple levels in close proximity
                - Creates strong zone
                - More difficult to break
                
                ### Failed Breaks (Fakeouts):
                - Price breaks level but quickly reverses
                - Traps breakout traders
                - Often leads to strong opposite move
                - Stop-hunting behavior
                
                ## Pro Tips
                
                ✅ Focus on major levels (ignore minor)
                ✅ Use higher timeframes for major levels
                ✅ Combine with price action patterns
                ✅ Volume confirms level strength
                ✅ Role reversal is powerful concept
                ✅ Zones are better than lines
                ✅ Horizontal levels more reliable than diagonal
                ✅ Old levels can remain relevant for years
                ✅ Watch for failed breakouts (traps)
                ✅ Multiple timeframe analysis essential
                ✅ Clean charts = clearer levels
                ✅ Mark major levels and revisit regularly
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What happens when a support level is broken?",
                        options = listOf("It disappears", "It often becomes resistance (role reversal)", "It becomes stronger support", "Nothing changes"),
                        correctAnswer = 1,
                        explanation = "When support is broken, it often becomes resistance through role reversal. Trapped buyers want to exit at breakeven, and sellers reinforce the level from below."
                    ),
                    QuizQuestion(
                        question = "Why are support and resistance better viewed as zones rather than exact lines?",
                        options = listOf("They look better", "Prices rarely respect exact levels due to spreads, slippage, and market inefficiency", "It's easier to draw", "Zones are always wrong"),
                        correctAnswer = 1,
                        explanation = "Support and resistance are better viewed as zones because prices rarely respect exact levels due to spreads, slippage, different trader perspectives, and market inefficiency."
                    ),
                    QuizQuestion(
                        question = "What makes a support or resistance level strong?",
                        options = listOf("It's recent", "Multiple touches, high volume, long time period, and confluence with other indicators", "It's on a Monday", "Low volume"),
                        correctAnswer = 1,
                        explanation = "Strong levels have multiple touches (3+), high volume at reactions, have held for extended time periods, and often have confluence with other technical indicators."
                    )
                )
            )
        ),
        
        Lesson(
            id = 18,
            title = "Trendlines and Channels",
            category = "Technical Analysis Fundamentals",
            duration = "11 min",
            content = """
                # Trendlines and Channels
                
                ## Overview
                
                Trendlines and channels are essential tools for identifying trend direction, support/resistance areas, and potential reversal points. They provide visual clarity on market structure and help traders make informed entry and exit decisions.
                
                ## What is a Trendline?
                
                ### Definition:
                A trendline is a straight line connecting two or more price points that acts as support (in uptrends) or resistance (in downtrends). It helps identify trend direction and potential reversal points.
                
                ## Uptrend Line
                
                ### Structure:
                ```
                Price
                  ↑           /•
                  |         /  
                  |       /•   
                  |     /      
                  |   /•       
                  | /_________ Uptrend Line (Support)
                  └──────────────────────→ Time
                  
                  Connects ascending lows
                ```
                
                ### Characteristics:
                - **Direction**: Slopes upward
                - **Connection**: Links swing lows
                - **Function**: Acts as support
                - **Touches**: Minimum 2, ideally 3+
                - **Breaks**: Signal potential trend reversal
                
                ### How to Draw:
                1. Identify clear uptrend
                2. Find two significant swing lows
                3. Connect them with straight line
                4. Extend line forward
                5. Validate with additional touches
                
                ## Downtrend Line
                
                ### Structure:
                ```
                Price __________ Downtrend Line (Resistance)
                  ↑   \      
                  |    \•     
                  |     \     
                  |      \•   
                  |       \   
                  |        \• 
                  └──────────────────────→ Time
                  
                  Connects descending highs
                ```
                
                ### Characteristics:
                - **Direction**: Slopes downward
                - **Connection**: Links swing highs
                - **Function**: Acts as resistance
                - **Touches**: Minimum 2, ideally 3+
                - **Breaks**: Signal potential trend reversal
                
                ### How to Draw:
                1. Identify clear downtrend
                2. Find two significant swing highs
                3. Connect them with straight line
                4. Extend line forward
                5. Validate with additional touches
                
                ## Trend Channels
                
                ### Definition:
                A channel consists of two parallel trendlines that contain price action, creating a "price corridor."
                
                ## Ascending Channel (Bullish)
                
                ### Structure:
                ```
                Upper Trendline (Resistance)
                 _________________________
                   /  •  /  •  /  •  /
                  /     /     /     /
                 /  •  /  •  /  •  /
                /_____/_____/_____/______
                Lower Trendline (Support)
                
                ← Rising Parallel Lines →
                ```
                
                ### Characteristics:
                - **Two Lines**: Both slope upward
                - **Parallel**: Equal distance apart
                - **Lower Line**: Support (connects lows)
                - **Upper Line**: Resistance (connects highs)
                - **Trend**: Bullish continuation
                
                ### Trading Strategy:
                - **Buy**: Near lower trendline (support)
                - **Sell**: Near upper trendline (resistance)
                - **Stop**: Below lower trendline
                - **Breakout**: Above upper line = acceleration
                - **Breakdown**: Below lower line = reversal
                
                ## Descending Channel (Bearish)
                
                ### Structure:
                ```
                Upper Trendline (Resistance)
                \_________________________
                 \  •  \  •  \  •  \
                  \     \     \     \
                   \  •  \  •  \  •  \
                    \_____\_____\_____\___
                    Lower Trendline (Support)
                
                ← Falling Parallel Lines →
                ```
                
                ### Characteristics:
                - **Two Lines**: Both slope downward
                - **Parallel**: Equal distance apart
                - **Upper Line**: Resistance (connects highs)
                - **Lower Line**: Support (connects lows)
                - **Trend**: Bearish continuation
                
                ### Trading Strategy:
                - **Sell**: Near upper trendline (resistance)
                - **Cover**: Near lower trendline (support)
                - **Stop**: Above upper trendline
                - **Breakdown**: Below lower line = acceleration
                - **Breakout**: Above upper line = reversal
                
                ## Horizontal Channel
                
                ### Structure:
                ```
                Resistance _______________
                          |  ↑↓  ↑↓  ↑↓ |
                          |            |
                Support   |____________|
                
                ← Range-bound Market →
                ```
                
                ### Characteristics:
                - **Two Lines**: Both horizontal
                - **Parallel**: Equal height
                - **Sideways**: No trending direction
                - **Range**: Defined boundaries
                
                ### Trading Strategy:
                - **Buy**: Near support, sell near resistance
                - **Range Trading**: Multiple opportunities
                - **Breakout**: Exit range = new trend
                
                ## Trendline Rules and Best Practices
                
                ### Rule 1: Minimum Touches
                ✅ **Valid Trendline**:
                - At least 2 touches to draw
                - 3+ touches for confirmation
                - More touches = stronger line
                
                ### Rule 2: Don't Force Fits
                ❌ **Invalid Trendlines**:
                - Forcing line through random points
                - Ignoring significant touches
                - Drawing lines with only 1 touch
                - Using minor/insignificant swings
                
                ### Rule 3: Angle Matters
                - **Steep angles** (>45°): Often break soon, unsustainable
                - **Moderate angles** (30-45°): Most reliable, sustainable
                - **Shallow angles** (<30°): Weak trend, consolidation
                
                ### Rule 4: Timeframe Relevance
                - **Higher timeframes**: More significant lines
                - **Daily/Weekly**: Major trendlines
                - **Hourly/4H**: Short-term lines
                - **Multiple timeframes**: Best approach
                
                ## Trading Trendline Breaks
                
                ### Uptrend Line Break (Bearish):
                
                **Setup**:
                1. Price touching uptrend line (support)
                2. Break below with conviction
                3. Close below line
                
                **Entry**:
                - **Conservative**: Wait for retest of broken line (now resistance)
                - **Aggressive**: Enter on break
                
                **Stop Loss**:
                - Above broken trendline
                - Or above recent swing high
                
                **Target**:
                - Next support level
                - Measured move
                
                ### Downtrend Line Break (Bullish):
                
                **Setup**:
                1. Price touching downtrend line (resistance)
                2. Break above with conviction
                3. Close above line
                
                **Entry**:
                - **Conservative**: Wait for retest of broken line (now support)
                - **Aggressive**: Enter on break
                
                **Stop Loss**:
                - Below broken trendline
                - Or below recent swing low
                
                **Target**:
                - Next resistance level
                - Measured move
                
                ## Channel Trading Strategies
                
                ### Strategy 1: Channel Bounce Trading
                
                **In Ascending Channel**:
                - Buy near lower line
                - Sell near upper line
                - Stop below channel
                - Repeat until breakout
                
                **In Descending Channel**:
                - Sell near upper line
                - Cover near lower line
                - Stop above channel
                - Repeat until breakdown
                
                ### Strategy 2: Channel Breakout Trading
                
                **Bullish Breakout** (Ascending Channel):
                - Watch for break above upper line
                - Confirms acceleration
                - Enter long on breakout
                - Target measured move (channel width added)
                
                **Bearish Breakdown** (Descending Channel):
                - Watch for break below lower line
                - Confirms acceleration
                - Enter short on breakdown
                - Target measured move (channel width subtracted)
                
                ## Advanced Trendline Concepts
                
                ### Internal Trendlines:
                - Lines within larger trendlines
                - Show trend momentum
                - Breaks signal acceleration/deceleration
                
                ### Fan Principle:
                - Multiple trendlines from same point
                - Each break leads to next line
                - Third break often signals reversal
                
                ### Speed Lines:
                - Trendlines at 1/3 and 2/3 retracement
                - Advanced technique
                - Shows trend strength
                
                ## Common Mistakes
                
                ❌ Drawing lines through price bodies (use wicks)
                ❌ Forcing lines that don't exist
                ❌ Using only 2 touches (need 3+ for validation)
                ❌ Ignoring failed trendlines
                ❌ Drawing too many lines (clutter)
                ❌ Not adjusting as new data appears
                ❌ Using exact touches (allow small penetrations)
                ❌ Mixing timeframes incorrectly
                
                ## Pro Tips
                
                ✅ Use logarithmic scale for long-term lines
                ✅ Allow minor penetrations (not every touch perfect)
                ✅ Redraw lines as new swings form
                ✅ Focus on major swings, ignore noise
                ✅ Combine with volume (breaks need volume)
                ✅ Higher timeframe lines more significant
                ✅ 3+ touches = very strong line
                ✅ Steeper lines break sooner
                ✅ Parallel channels most reliable
                ✅ Use channels for range-bound trading
                ✅ Breakout/breakdown needs confirmation
                ✅ Clean charts = clearer lines
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the minimum number of touches needed to draw a valid trendline?",
                        options = listOf("1 touch", "2 touches, 3+ for confirmation", "5 touches", "10 touches"),
                        correctAnswer = 1,
                        explanation = "You need at least 2 touches to draw a trendline, but 3 or more touches provide better confirmation and validation of the trend's strength and reliability."
                    ),
                    QuizQuestion(
                        question = "In an ascending channel, where should you buy and where should you sell?",
                        options = listOf("Buy upper, sell lower", "Buy lower trendline (support), sell upper trendline (resistance)", "Buy randomly", "Never trade channels"),
                        correctAnswer = 1,
                        explanation = "In an ascending channel, buy near the lower trendline (support) and sell/take profits near the upper trendline (resistance), trading the range within the channel."
                    ),
                    QuizQuestion(
                        question = "What angle makes a trendline most reliable and sustainable?",
                        options = listOf("Vertical (90°)", "Moderate angle (30-45°)", "Horizontal (0°)", "Any angle"),
                        correctAnswer = 1,
                        explanation = "Moderate angles of 30-45° are most reliable and sustainable. Steeper angles (>45°) often break quickly, while shallow angles (<30°) indicate weak trends."
                    )
                )
            )
        ),
        
        Lesson(
            id = 19,
            title = "Fibonacci Retracements",
            category = "Technical Analysis Advanced",
            duration = "10 min",
            content = """
                # Fibonacci Retracements
                
                ## Overview
                
                Fibonacci retracements are horizontal lines that indicate potential support and resistance levels based on the Fibonacci sequence. These levels are derived from mathematical ratios found throughout nature and are widely used by traders to identify potential reversal points.
                
                ## The Fibonacci Sequence
                
                ### Origin:
                The Fibonacci sequence: 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144...
                
                **Pattern**: Each number is the sum of the two preceding numbers.
                
                ### The Golden Ratio:
                - Dividing any number by the next number ≈ 0.618 (61.8%)
                - Dividing by the number two places higher ≈ 0.382 (38.2%)
                - Dividing by the number three places higher ≈ 0.236 (23.6%)
                
                These ratios appear throughout nature, architecture, and financial markets.
                
                ## Key Fibonacci Levels
                
                ### Primary Retracement Levels:
                
                **0%** - Start of move (recent swing)
                **23.6%** - Shallow retracement
                **38.2%** - Moderate retracement (first golden ratio)
                **50%** - Midpoint (not Fibonacci, but widely used)
                **61.8%** - Deep retracement (the Golden Ratio)
                **78.6%** - Very deep retracement (square root of 0.618)
                **100%** - Complete retracement (original swing point)
                
                ### Extension Levels (for targets):
                **127.2%** - First extension
                **161.8%** - Golden extension (most important)
                **200%** - Double extension
                **261.8%** - Major extension
                
                ## How to Apply Fibonacci Retracements
                
                ### In an Uptrend:
                
                ```
                100% ___•_______________ (High)
                 78.6% ___________
                 61.8% ___________  ← Golden Ratio
                 50%  ___________
                 38.2% ___________
                 23.6% ___________
                  0%  ___•_______________ (Low)
                  
                  ← Measure from Low to High →
                ```
                
                **How to Draw**:
                1. Identify significant upward move
                2. Find the swing low (starting point)
                3. Find the swing high (ending point)
                4. Draw Fibonacci from low to high
                5. Watch for support at retracement levels
                
                ### In a Downtrend:
                
                ```
                  0%  ___•_______________ (High)
                 23.6% ___________
                 38.2% ___________
                 50%  ___________
                 61.8% ___________  ← Golden Ratio
                 78.6% ___________
                100% ___•_______________ (Low)
                  
                  ← Measure from High to Low →
                ```
                
                **How to Draw**:
                1. Identify significant downward move
                2. Find the swing high (starting point)
                3. Find the swing low (ending point)
                4. Draw Fibonacci from high to low
                5. Watch for resistance at retracement levels
                
                ## Trading with Fibonacci
                
                ### Strategy 1: Buying Pullbacks (Uptrend)
                
                **Setup**:
                1. Strong uptrend identified
                2. Price begins to retrace
                3. Wait for price to approach Fib level
                
                **Entry Zones**:
                - **38.2%**: Aggressive entry (shallow retracement)
                - **50%**: Moderate entry (common retracement)
                - **61.8%**: Conservative entry (deep retracement)
                
                **Confirmation**:
                - Bullish reversal candle at Fib level
                - Volume spike
                - RSI divergence
                - Support from other indicators
                
                **Stop Loss**:
                - Below next Fib level
                - Or below 78.6% retracement
                
                **Target**:
                - Previous high (100% -> 0%)
                - Fibonacci extensions (127.2%, 161.8%)
                
                ### Strategy 2: Selling Rallies (Downtrend)
                
                **Setup**:
                1. Strong downtrend identified
                2. Price begins to retrace upward
                3. Wait for price to approach Fib level
                
                **Entry Zones**:
                - **38.2%**: Aggressive short (shallow rally)
                - **50%**: Moderate short
                - **61.8%**: Conservative short (deeper rally)
                
                **Confirmation**:
                - Bearish reversal candle at Fib level
                - Volume increase
                - Resistance confirmed
                
                **Stop Loss**:
                - Above next Fib level
                - Or above 78.6% retracement
                
                **Target**:
                - Previous low
                - Fibonacci extensions downward
                
                ## The Golden Ratio (61.8%)
                
                ### Why 61.8% is Special:
                - Most watched Fib level
                - Strongest support/resistance
                - "Last chance" entry point
                - Deep retracement before continuation
                
                ### Trading the 61.8%:
                
                **Bullish Setup**:
                - Price retraces to 61.8% in uptrend
                - Look for strong reversal signals
                - Often creates best risk/reward
                - High probability continuation point
                
                **Bearish Setup**:
                - Price rallies to 61.8% in downtrend
                - Watch for rejection signals
                - Strong resistance expected
                - Often marks rally exhaustion
                
                ## Confluence with Other Tools
                
                ### Fibonacci + Support/Resistance:
                ✅ **Very Strong**:
                - 61.8% Fib + previous support = high probability
                - 50% Fib + round number (e.g., $100) = strong level
                - Multiple Fib levels from different swings = cluster
                
                ### Fibonacci + Moving Averages:
                ✅ **Powerful Combination**:
                - 50% Fib + 50 SMA alignment = very strong
                - 61.8% Fib + 200 SMA = major support/resistance
                
                ### Fibonacci + Trendlines:
                ✅ **Triple Confluence**:
                - Fib level + trendline + support = highest probability
                - Multiple timeframe Fib alignment = very strong
                
                ## Fibonacci Extensions (Price Targets)
                
                ### How to Use:
                After a retracement, measure the original move to project targets:
                
                **In Uptrend**:
                1. Identify swing low to swing high
                2. Wait for retracement (to 38.2%, 50%, or 61.8%)
                3. Project extensions from retracement low:
                   - **127.2%**: First target
                   - **161.8%**: Major target (Golden extension)
                   - **200%**: Extended target
                
                **Example**:
                - Stock moves $50 → $100 (50-point move)
                - Retraces to $75 (50% retracement)
                - Targets: $125 (127.2%), $131 (161.8%), $150 (200%)
                
                ## Common Retracement Behaviors
                
                ### Strong Trends:
                - Retrace to 23.6% or 38.2%
                - Shallow retracements
                - Quick continuation
                - Strong momentum
                
                ### Normal Trends:
                - Retrace to 50%
                - Most common
                - Healthy pullback
                - Sustainable trend
                
                ### Weak Trends:
                - Retrace to 61.8% or deeper
                - Losing momentum
                - May be trend exhaustion
                - Caution advised
                
                ### Failed Trends:
                - Break through 78.6%
                - Approach 100% (full retracement)
                - Trend likely over
                - Reversal probable
                
                ## Advanced Techniques
                
                ### Multiple Timeframe Fibonacci:
                - Draw Fibs on daily, weekly, monthly
                - Cluster zones = very strong levels
                - Higher timeframe Fibs more significant
                
                ### Fibonacci Fans:
                - Diagonal lines from swing point
                - Dynamic support/resistance
                - Advanced technique
                
                ### Fibonacci Arcs:
                - Curved lines
                - Time and price combination
                - Specialized use
                
                ## Common Mistakes
                
                ❌ Using every small swing (use significant moves)
                ❌ Trading Fib levels without confirmation
                ❌ Expecting exact touches (use zones)
                ❌ Ignoring overall trend context
                ❌ Not combining with other tools
                ❌ Drawing Fibs incorrectly (wrong direction)
                ❌ Using too many Fib overlays (clutter)
                ❌ Forcing Fib fits on random price action
                
                ## Pro Tips
                
                ✅ Use significant swings (at least 10-20% moves)
                ✅ 61.8% is the most important level
                ✅ 50% very popular (self-fulfilling)
                ✅ Combine with support/resistance for confluence
                ✅ Wait for confirmation before entering
                ✅ Higher timeframes = more reliable levels
                ✅ Extensions (161.8%) excellent for targets
                ✅ Fib clusters from multiple swings = very strong
                ✅ Use zones, not exact lines
                ✅ Volume should increase at bounces
                ✅ Works best in trending markets
                ✅ Multiple timeframe analysis powerful
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the most important and widely watched Fibonacci retracement level?",
                        options = listOf("23.6%", "50%", "61.8% (The Golden Ratio)", "100%"),
                        correctAnswer = 2,
                        explanation = "The 61.8% level, known as the Golden Ratio, is the most important Fibonacci level. It represents the strongest support/resistance and is often the 'last chance' entry point before trend continuation."
                    ),
                    QuizQuestion(
                        question = "How do you draw Fibonacci retracements in an uptrend?",
                        options = listOf("High to low", "From swing low to swing high", "Randomly", "Only on Mondays"),
                        correctAnswer = 1,
                        explanation = "In an uptrend, draw Fibonacci retracements from the swing low (starting point) to the swing high (ending point) to identify potential support levels during pullbacks."
                    ),
                    QuizQuestion(
                        question = "What does it indicate when price retraces only to the 23.6% or 38.2% level?",
                        options = listOf("Weak trend", "Strong trend with shallow retracement", "Failed pattern", "No trend"),
                        correctAnswer = 1,
                        explanation = "Shallow retracements to 23.6% or 38.2% indicate a very strong trend with minimal pullback, suggesting powerful momentum and high probability of continuation."
                    )
                )
            )
        ),
        
        Lesson(
            id = 20,
            title = "Volume Analysis",
            category = "Technical Analysis Advanced",
            duration = "11 min",
            content = """
                # Volume Analysis
                
                ## Overview
                
                Volume is the number of shares or contracts traded during a specific period. It's a critical confirmation tool that validates price movements and patterns. Understanding volume helps traders distinguish between strong moves and weak moves that are likely to fail.
                
                ## Why Volume Matters
                
                ### The Fundamental Principle:
                **"Volume precedes price"**
                
                Volume shows the conviction behind price movements:
                - **High Volume**: Strong conviction, sustainable move
                - **Low Volume**: Weak conviction, unsustainable move
                - **Volume Confirms**: Validates breakouts and reversals
                - **Volume Diverges**: Warns of potential reversals
                
                ### Psychology:
                - High volume = many participants agreeing
                - Low volume = few participants, lack of consensus
                - Increasing volume = growing interest and momentum
                - Decreasing volume = waning interest, potential reversal
                
                ## Basic Volume Principles
                
                ### Principle 1: Confirm Trends
                
                **Healthy Uptrend**:
                ```
                Price: ↗️ (Rising)
                Volume: 📊📊📊 (Increasing on up days)
                         📊 (Decreasing on down days)
                
                = Strong, sustainable uptrend
                ```
                
                **Healthy Downtrend**:
                ```
                Price: ↘️ (Falling)
                Volume: 📊📊📊 (Increasing on down days)
                         📊 (Decreasing on up days)
                
                = Strong, sustainable downtrend
                ```
                
                ### Principle 2: Volume Divergence
                
                **Bearish Divergence** (Warning):
                ```
                Price:  Higher highs
                         /\    /\
                        /  \  /  \
                       
                Volume: 📊📊📊  📊📊
                        Declining volume = Warning
                
                = Uptrend losing strength
                ```
                
                **Bullish Divergence**:
                ```
                Price:  Lower lows
                        \  /  \  /
                         \/    \/
                       
                Volume: 📊📊📊  📊📊
                        Declining volume = Warning
                
                = Downtrend losing strength
                ```
                
                ## Volume in Price Patterns
                
                ### Breakouts (Most Critical):
                
                **Valid Breakout**:
                ```
                Resistance ____________
                          |  Pattern  | ↑ BREAKOUT
                          |__________|  📊📊📊📊
                                      High Volume!
                
                ✅ 2-3x average volume = Strong
                ✅ Confirms breakout validity
                ✅ Higher probability of success
                ```
                
                **False Breakout**:
                ```
                Resistance ____________
                          |  Pattern  | ↑ Breakout?
                          |__________|  📊
                                      Low Volume
                
                ❌ Low volume = Likely to fail
                ❌ Return to range probable
                ❌ Avoid trading
                ```
                
                ### Volume in Reversals:
                
                **Climax Volume (Bottom)**:
                - Massive volume spike at low
                - Capitulation selling
                - "Selling climax"
                - Often marks bottom
                - Followed by reversal
                
                **Exhaustion Volume (Top)**:
                - Very high volume at highs
                - Euphoric buying
                - "Buying climax"
                - Often marks top
                - Distribution occurring
                
                ## Advanced Volume Concepts
                
                ### On-Balance Volume (OBV):
                
                **Concept**:
                - Running total of volume
                - Add volume on up days
                - Subtract volume on down days
                - Creates cumulative line
                
                **Interpretation**:
                - **OBV Rising** + Price Rising = Confirmation
                - **OBV Falling** + Price Falling = Confirmation
                - **OBV Rising** + Price Falling = Bullish divergence
                - **OBV Falling** + Price Rising = Bearish divergence
                
                ### Volume Price Analysis (VPA):
                
                **High Volume + Up Bar**:
                - Professional buying
                - Strong accumulation
                - Bullish signal
                
                **High Volume + Down Bar**:
                - Professional selling
                - Distribution
                - Bearish signal
                
                **Low Volume + Up Bar**:
                - Weak buying
                - No support from professionals
                - Likely to fail
                
                **Low Volume + Down Bar**:
                - Weak selling
                - No conviction
                - May be bottoming
                
                ### Volume Spread Analysis (VSA):
                
                **Key Concepts**:
                1. **Spread**: High - Low range
                2. **Volume**: Total volume
                3. **Close**: Where candle closes
                
                **Combinations**:
                - **Wide Spread + High Volume + Close Near High** = Very bullish
                - **Wide Spread + High Volume + Close Near Low** = Very bearish
                - **Narrow Spread + High Volume** = Potential reversal
                - **Wide Spread + Low Volume** = Weak move
                
                ## Volume Patterns
                
                ### Pattern 1: Volume Climax
                
                **Buying Climax** (Top):
                - Highest volume in months
                - Price spike upward
                - Exhaustion gap possible
                - Followed by reversal
                
                **Selling Climax** (Bottom):
                - Highest volume in months
                - Price spike downward
                - Panic selling
                - Often the bottom
                
                ### Pattern 2: Volume Dry Up
                
                **Characteristics**:
                - Volume shrinks significantly
                - Price compresses
                - Indecision
                - Precedes major move
                
                **Trading**:
                - Watch for direction
                - Volume will spike on breakout
                - Trade the direction of volume surge
                
                ### Pattern 3: Increasing Volume Trend
                
                **In Uptrend**:
                - Each rally has higher volume
                - Pullbacks have lower volume
                - Confirms strong trend
                - Continue holding/buying
                
                **In Downtrend**:
                - Each decline has higher volume
                - Bounces have lower volume
                - Confirms strong downtrend
                - Continue holding short/avoid longs
                
                ## Volume Indicators
                
                ### Volume Moving Average:
                - Calculate MA of volume (e.g., 20-period)
                - Compare current volume to average
                - Above MA = Higher than normal
                - Below MA = Lower than normal
                - Use for breakout confirmation
                
                ### Volume Oscillator:
                - Difference between two volume MAs
                - Positive = Short-term volume > Long-term
                - Negative = Short-term volume < Long-term
                - Shows volume momentum
                
                ### Accumulation/Distribution Line:
                - Similar to OBV
                - Considers close location in range
                - Shows buying/selling pressure
                - Divergence signals important
                
                ## Volume Trading Rules
                
                ### Rule 1: Confirmation
                ✅ **Always confirm with volume**:
                - Breakouts need 2x+ volume
                - Reversals need volume spike
                - Trend continuation needs steady volume
                
                ### Rule 2: Divergence
                ⚠️ **Watch for divergence**:
                - Price up, volume down = Warning
                - Price down, volume down = Potential bottom
                - OBV divergence = Early warning
                
                ### Rule 3: Climaxes
                🚨 **Respect volume climaxes**:
                - Buying climax = Top likely
                - Selling climax = Bottom likely
                - Highest volume often marks turning points
                
                ## Practical Trading Strategies
                
                ### Strategy 1: Volume Confirmation Breakout
                
                **Setup**:
                1. Identify consolidation pattern
                2. Wait for breakout
                3. Check volume (must be 2x+ average)
                4. Enter on high volume breakout
                
                **Entry**: Breakout point with volume
                **Stop**: Opposite side of pattern
                **Target**: Measured move or next resistance
                
                ### Strategy 2: Volume Climax Reversal
                
                **Setup**:
                1. Identify extended trend
                2. Watch for volume spike (3x+ average)
                3. Price reversal candle
                4. Climax exhaustion confirmed
                
                **Entry**: After reversal candle
                **Stop**: Beyond climax point
                **Target**: Retracement levels (38.2%, 50%)
                
                ### Strategy 3: Volume Divergence
                
                **Setup**:
                1. Uptrend with rising prices
                2. Volume declining on rallies
                3. OBV showing divergence
                4. Reversal pattern forms
                
                **Entry**: Pattern confirmation
                **Stop**: Above recent high
                **Target**: Support levels
                
                ## Common Mistakes
                
                ❌ Trading breakouts on low volume
                ❌ Ignoring volume divergence
                ❌ Not using volume average for context
                ❌ Confusing volume with price
                ❌ Expecting exact volume levels
                ❌ Not considering market conditions
                ❌ Overcomplicating with too many indicators
                
                ## Pro Tips
                
                ✅ Volume should increase in direction of trend
                ✅ 2-3x average volume confirms breakouts
                ✅ Climax volume often marks reversals
                ✅ Declining volume in trend = warning
                ✅ OBV divergence is early warning signal
                ✅ Higher timeframes more significant
                ✅ Combine volume with price action
                ✅ Watch for volume spikes at key levels
                ✅ Low volume moves don't last
                ✅ Volume precedes price
                ✅ Use volume moving average as baseline
                ✅ Respect buying/selling climaxes
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What volume level confirms a valid breakout?",
                        options = listOf("Same as average", "2-3x average volume", "Below average", "Volume doesn't matter"),
                        correctAnswer = 1,
                        explanation = "A valid breakout requires 2-3x average volume or higher. This high volume confirms strong conviction and significantly increases the probability of breakout success."
                    ),
                    QuizQuestion(
                        question = "What does declining volume during an uptrend indicate?",
                        options = listOf("Very bullish", "Warning sign - uptrend losing strength", "Normal behavior", "Time to buy more"),
                        correctAnswer = 1,
                        explanation = "Declining volume during an uptrend is a warning sign of bearish divergence, indicating the uptrend is losing strength and participation, which often precedes a reversal."
                    ),
                    QuizQuestion(
                        question = "What is a 'selling climax' and what does it typically indicate?",
                        options = listOf("Start of downtrend", "Massive volume spike at lows, often marks the bottom", "Normal selling", "Time to sell"),
                        correctAnswer = 1,
                        explanation = "A selling climax is a massive volume spike at price lows representing panic selling and capitulation. It often marks the bottom of a decline as all weak hands exit."
                    )
                )
            )
        ),
        
        Lesson(
            id = 21,
            title = "Risk Management",
            category = "Trading Fundamentals",
            duration = "13 min",
            content = """
                # Risk Management
                
                ## Overview
                
                Risk management is the most important aspect of trading. It's not about how much you can make, but how much you can afford to lose. Proper risk management ensures you survive bad periods, protect your capital, and stay in the game long enough to profit from your edge.
                
                ## The Golden Rule
                
                **"Protect your capital at all costs"**
                
                Your trading capital is your business inventory. Without it, you cannot trade. Risk management ensures you never lose so much that you cannot recover.
                
                ## Core Principles
                
                ### Principle 1: Never Risk More Than You Can Afford to Lose
                
                **Account Risk Per Trade**:
                - **Conservative**: 0.5-1% of account per trade
                - **Moderate**: 1-2% of account per trade
                - **Aggressive**: 2-3% of account per trade (not recommended)
                - **Never Exceed**: 5% (this is reckless)
                
                **Example**:
                - Account size: \$10,000
                - Risk per trade: 1% = \$100
                - If stopped out, you lose only \$100
                - Need 100 consecutive losses to lose account (impossible with edge)
                
                ### Principle 2: Use Stop Losses (Always!)
                
                **Stop Loss**: Predetermined price level where you exit to prevent further losses.
                
                **Why Critical**:
                - Protects against catastrophic losses
                - Removes emotion from exit decision
                - Defines your risk before entry
                - Professional traders always use stops
                
                **Types of Stop Losses**:
                
                **1. Fixed Dollar/Percentage Stop**:
                ```
                Entry: \$100
                Stop: \$95 (5% below)
                Risk: \$5 per share
                ```
                
                **2. Technical Stop** (Better):
                ```
                Entry: \$100
                Stop: \$97 (below support)
                Risk: \$3 per share
                Based on chart structure
                ```
                
                **3. Volatility Stop** (Advanced):
                - Uses ATR (Average True Range)
                - Adapts to market conditions
                - Wider stops in volatile markets
                
                **4. Time Stop**:
                - Exit after specific time period
                - If trade not working, get out
                - Prevents capital being tied up
                
                ### Principle 3: Risk/Reward Ratio
                
                **Definition**: Potential profit compared to potential loss.
                
                **Minimum Acceptable**: 2:1
                - Risk \$100 to make \$200
                - Risk 1% to make 2%
                - Asymmetric risk/reward
                
                **Better Ratios**: 3:1 or higher
                - Risk \$100 to make \$300+
                - Even with 50% win rate, you profit
                
                **Example**:
                ```
                Entry: \$100
                Stop Loss: \$95 (risk \$5)
                Target: \$110 (reward \$10)
                Risk/Reward: 1:2 ✅
                
                Win Rate: 40%
                Expectancy: (0.40 × \$10) - (0.60 × \$5)
                          = \$4 - \$3 = +\$1 per trade ✅
                ```
                
                ## Risk Management Strategies
                
                ### Strategy 1: Position Sizing
                
                **Formula**:
                ```
                Position Size = (Account Risk $) / (Entry Price - Stop Price)
                
                Example:
                Account: \$10,000
                Risk per trade: 1% = \$100
                Entry: \$50
                Stop: \$48
                Risk per share: \$2
                
                Position Size = \$100 / \$2 = 50 shares
                
                Total Investment: 50 × \$50 = \$2,500
                Max Loss: 50 × \$2 = \$100 ✅
                ```
                
                ### Strategy 2: Diversification
                
                **Don't Put All Eggs in One Basket**:
                - **Never risk >20% of capital in one trade**
                - **Limit exposure per sector** (e.g., max 30% in tech)
                - **Trade multiple uncorrelated markets**
                - **Different strategies** reduce risk
                
                **Example**:
                - \$10,000 account
                - 5 positions maximum
                - \$2,000 max per position (20%)
                - 1% risk per trade = \$100
                - Total max loss from all positions: \$500 (5%)
                
                ### Strategy 3: Correlation Risk
                
                **Problem**: Multiple positions moving together
                
                **Example of Correlation Risk**:
                ```
                5 Tech Stocks:
                ❌ Apple, Microsoft, Google, Amazon, Meta
                All fall together in tech selloff
                Not truly diversified!
                
                ✅ Better:
                1 Tech (Apple)
                1 Healthcare (J&J)
                1 Energy (XOM)
                1 Financial (JPM)
                1 Consumer (WMT)
                True diversification
                ```
                
                ### Strategy 4: Maximum Daily/Weekly Loss
                
                **Daily Loss Limit**:
                - Stop trading after losing X% in one day
                - Example: Stop after -2% daily loss
                - Prevents revenge trading
                - Protects from emotional decisions
                
                **Weekly Loss Limit**:
                - Stop trading after losing X% in one week
                - Example: Stop after -5% weekly loss
                - Time to reassess strategy
                - Prevents blowing up account
                
                ## Advanced Risk Concepts
                
                ### The 1% Rule
                
                **Never risk more than 1% per trade**:
                
                **Math**:
                - 100 trades
                - Win rate: 50%
                - Risk/Reward: 1:2
                - Risk: 1% per trade
                
                **Results**:
                - 50 losses × 1% = -50%
                - 50 wins × 2% = +100%
                - Net: +50% gain
                
                **Even with 40% win rate**:
                - 60 losses × 1% = -60%
                - 40 wins × 2% = +80%
                - Net: +20% gain
                
                ### Scaling In and Out
                
                **Scaling In** (Add to winners):
                ```
                1st Position: \$100 entry, up to \$105
                Move stop to breakeven
                2nd Position: \$105 entry
                Add to winning trade (pyramid)
                ```
                
                **Scaling Out** (Take profits):
                ```
                Position: 100 shares
                Target 1: Sell 50 shares (+5%)
                Target 2: Sell 25 shares (+10%)
                Target 3: Sell 25 shares (+15%)
                Lock in profits progressively
                ```
                
                ### Trailing Stops
                
                **Definition**: Stop loss that moves with price
                
                **Example**:
                ```
                Entry: \$100
                Initial Stop: \$95
                Price rises to \$110
                Trailing Stop (5%): \$104.50
                Locks in \$4.50 profit
                
                Price continues to \$120
                Trailing Stop: \$114
                Locks in \$14 profit
                ```
                
                ## Risk of Ruin
                
                **Definition**: Probability of losing entire account
                
                **Factors**:
                1. Risk per trade
                2. Win rate
                3. Risk/reward ratio
                
                **Safe Parameters**:
                - 1% risk per trade
                - 50% win rate
                - 2:1 risk/reward
                - Risk of ruin: Near 0%
                
                **Dangerous Parameters**:
                - 10% risk per trade
                - 50% win rate
                - 1:1 risk/reward
                - Risk of ruin: >90% (will blow up!)
                
                ## Psychological Aspects
                
                ### Emotional Control:
                - **Stop losses prevent panic**
                - **Rules prevent impulsive decisions**
                - **Risk limits prevent revenge trading**
                - **Position sizing reduces stress**
                
                ### Discipline:
                - **Always honor stops** (no exceptions!)
                - **Never average down** on losers
                - **Cut losses quickly**
                - **Let winners run**
                
                ## Common Mistakes
                
                ❌ Moving stop losses further away
                ❌ Not using stops at all
                ❌ Risking too much per trade (>2%)
                ❌ Revenge trading after losses
                ❌ Adding to losing positions
                ❌ Ignoring correlation risk
                ❌ No daily/weekly loss limits
                ❌ Poor risk/reward ratios (<1.5:1)
                ❌ Overleveraging account
                ❌ Trading without position size calculation
                
                ## Risk Management Checklist
                
                ### Before Every Trade:
                ✅ Calculate position size based on risk
                ✅ Set stop loss before entry
                ✅ Identify target price
                ✅ Calculate risk/reward ratio (min 2:1)
                ✅ Confirm not exceeding daily/weekly limits
                ✅ Check correlation with other positions
                ✅ Verify account risk is ≤1-2%
                
                ## Pro Tips
                
                ✅ Risk management is more important than entry
                ✅ Survive first, profit second
                ✅ 1% rule keeps you in the game
                ✅ Always use stop losses (non-negotiable)
                ✅ Minimum 2:1 risk/reward ratio
                ✅ Calculate position size before entry
                ✅ Never risk what you can't afford to lose
                ✅ Diversify across different sectors
                ✅ Set daily and weekly loss limits
                ✅ Trailing stops protect profits
                ✅ Scale out to lock in gains
                ✅ Journal all trades to track risk metrics
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the maximum recommended risk per trade?",
                        options = listOf("10%", "5%", "1-2%", "25%"),
                        correctAnswer = 2,
                        explanation = "The maximum recommended risk per trade is 1-2% of your account. This ensures you can withstand losing streaks without blowing up your account and stay in the game long-term."
                    ),
                    QuizQuestion(
                        question = "What is the minimum acceptable risk/reward ratio?",
                        options = listOf("1:1", "2:1", "1:2", "3:3"),
                        correctAnswer = 1,
                        explanation = "The minimum acceptable risk/reward ratio is 2:1, meaning you should aim to make at least \$2 for every \$1 you risk. This ensures profitability even with less than 50% win rate."
                    ),
                    QuizQuestion(
                        question = "Why should you always use stop losses?",
                        options = listOf("They're optional", "To protect against catastrophic losses and remove emotion from exits", "To make more money", "Only for beginners"),
                        correctAnswer = 1,
                        explanation = "Stop losses are critical because they protect against catastrophic losses, remove emotion from exit decisions, and define your risk before entry. Professional traders always use them."
                    )
                )
            )
        ),
        
        Lesson(
            id = 22,
            title = "Position Sizing",
            category = "Trading Fundamentals",
            duration = "10 min",
            content = """
                # Position Sizing
                
                ## Overview
                
                Position sizing is determining how many shares, contracts, or lots to trade based on your account size, risk tolerance, and stop loss distance. It's one of the most critical yet overlooked aspects of trading. Proper position sizing ensures consistent risk across all trades and prevents catastrophic losses.
                
                ## Why Position Sizing Matters
                
                ### The Problem:
                Most traders focus on entry and exit, but ignore position size:
                
                **Wrong Approach**:
                ```
                Trade 1: Buy 100 shares at \$50 (risk \$100)
                Trade 2: Buy 200 shares at \$25 (risk \$100)
                Trade 3: Buy 50 shares at \$100 (risk \$250)
                
                ❌ Inconsistent risk per trade!
                ```
                
                **Right Approach**:
                ```
                Trade 1: Calculate size to risk \$100
                Trade 2: Calculate size to risk \$100
                Trade 3: Calculate size to risk \$100
                
                ✅ Consistent risk, better management
                ```
                
                ## Basic Position Sizing Formula
                
                ### The Formula:
                ```
                Position Size = Account Risk ÷ (Entry Price - Stop Loss Price)
                
                Where:
                - Account Risk = Account Size × Risk%
                - Entry Price = Your entry point
                - Stop Loss Price = Your stop loss level
                ```
                
                ### Example:
                ```
                Account Size: \$10,000
                Risk per trade: 1% = \$100
                Entry Price: \$50
                Stop Loss: \$48
                Risk per share: \$50 - \$48 = \$2
                
                Position Size = \$100 ÷ \$2 = 50 shares
                
                Verification:
                Total investment: 50 × \$50 = \$2,500
                Max loss: 50 × \$2 = \$100 ✅
                ```
                
                ## Position Sizing Methods
                
                ### Method 1: Fixed Percentage Risk
                
                **Concept**: Risk same percentage each trade
                
                **Advantages**:
                ✅ Consistent risk exposure
                ✅ Easy to calculate
                ✅ Accounts automatically scale
                ✅ Most common method
                
                **Example**:
                ```
                Account: \$10,000 → Risk 1% = \$100
                Account grows to \$12,000 → Risk 1% = \$120
                Account drops to \$8,000 → Risk 1% = \$80
                
                Position size adjusts automatically!
                ```
                
                ### Method 2: Fixed Dollar Risk
                
                **Concept**: Risk same dollar amount each trade
                
                **Advantages**:
                ✅ Simple to understand
                ✅ Consistent dollar risk
                
                **Disadvantages**:
                ❌ Doesn't scale with account
                ❌ Need to adjust manually
                
                **Example**:
                ```
                Always risk \$100 per trade
                Account size irrelevant
                ```
                
                ### Method 3: Fixed Ratio
                
                **Concept**: Increase position size after profit targets hit
                
                **Example**:
                ```
                Start: 1 contract
                After \$1,000 profit: 2 contracts
                After \$2,000 more profit: 3 contracts
                
                Scales with success
                ```
                
                ### Method 4: Kelly Criterion
                
                **Formula**:
                ```
                Kelly % = (Win Rate × Avg Win - Loss Rate × Avg Loss) / Avg Win
                
                Example:
                Win Rate: 60%
                Avg Win: \$300
                Avg Loss: \$100
                
                Kelly = (0.60 × 300 - 0.40 × 100) / 300
                      = (180 - 40) / 300
                      = 0.467 or 46.7%
                
                ⚠️ TOO AGGRESSIVE!
                Use half-Kelly: 23.3%
                Or quarter-Kelly: 11.7%
                ```
                
                ## Position Sizing for Different Instruments
                
                ### Stocks:
                ```
                Formula: Shares = Account Risk ÷ (Entry - Stop)
                
                Example:
                Account Risk: \$100
                Entry: \$50, Stop: \$47
                Risk/share: \$3
                Shares: \$100 ÷ \$3 = 33 shares
                ```
                
                ### Options:
                ```
                More complex due to leverage
                
                Example:
                Account Risk: \$100
                Option Price: \$2
                Max loss per contract: \$200 (100 shares × \$2)
                
                If risking full premium:
                Contracts = \$100 ÷ \$200 = 0.5
                → Buy 1 contract, but only risk \$100
                ```
                
                ### Forex:
                ```
                Formula: Lots = Account Risk ÷ (Pips at Risk × Pip Value)
                
                Example:
                Account Risk: \$100
                Entry: 1.1000, Stop: 1.0950
                Risk: 50 pips
                Pip Value (standard lot): \$10/pip
                
                Lots = \$100 ÷ (50 × \$10) = 0.2 lots
                ```
                
                ### Futures:
                ```
                Formula: Contracts = Account Risk ÷ (Ticks × Tick Value)
                
                Example:
                Account Risk: \$100
                Entry: 4000, Stop: 3990
                Risk: 10 ticks
                Tick Value: \$12.50
                
                Contracts = \$100 ÷ (10 × \$12.50) = 0.8
                → Trade 1 contract with reduced risk
                ```
                
                ## Advanced Position Sizing
                
                ### Volatility-Adjusted Sizing:
                
                **Concept**: Adjust position size based on volatility
                
                **Using ATR** (Average True Range):
                ```
                High ATR (volatile) = Smaller position
                Low ATR (calm) = Larger position
                
                Example:
                Normal Stop: 2 × ATR
                ATR = \$5 → Stop at \$10
                ATR = \$2 → Stop at \$4
                
                Position size adjusts to maintain consistent risk
                ```
                
                ### Conviction-Based Sizing:
                
                **Concept**: Size based on trade quality
                
                **A-Setup** (High confidence):
                - Risk 2% of account
                - All criteria met
                - Multiple confirmations
                
                **B-Setup** (Medium confidence):
                - Risk 1% of account
                - Most criteria met
                - Some confirmations
                
                **C-Setup** (Low confidence):
                - Risk 0.5% of account
                - Fewer criteria
                - Experimental trades
                
                ### Scaling Positions:
                
                **Scale In** (add to winners):
                ```
                Initial Position: Risk 0.5%
                If profitable: Add 0.5%
                If still profitable: Add 0.5%
                Total: 1.5% risk
                
                Only add when winning!
                Never add to losers!
                ```
                
                **Scale Out** (take profits):
                ```
                Position: 100 shares
                Target 1 (+5%): Sell 33 shares
                Target 2 (+10%): Sell 33 shares
                Target 3 (+15%): Sell 34 shares
                
                Lock in profits incrementally
                ```
                
                ## Risk Management Integration
                
                ### Maximum Position Size:
                
                **Never exceed 20% of account in single trade**:
                ```
                \$10,000 account
                Max position value: \$2,000
                
                Even if calculations suggest bigger position,
                cap at 20% for safety
                ```
                
                ### Maximum Portfolio Risk:
                
                **Total risk across all positions**:
                ```
                5 positions × 1% each = 5% total risk
                
                If one position is 2%:
                Remaining 4 can only be 3% combined
                
                Never exceed 5-10% total portfolio risk
                ```
                
                ## Position Sizing Calculator
                
                ### Step-by-Step:
                
                **Step 1**: Determine account risk
                ```
                Account: \$10,000
                Risk %: 1%
                Account Risk: \$100
                ```
                
                **Step 2**: Identify entry and stop
                ```
                Entry: \$50
                Stop Loss: \$47
                Risk per share: \$3
                ```
                
                **Step 3**: Calculate position size
                ```
                Position Size = \$100 ÷ \$3 = 33.3 shares
                Round to: 33 shares
                ```
                
                **Step 4**: Verify
                ```
                Total investment: 33 × \$50 = \$1,650
                Max loss: 33 × \$3 = \$99 ✅
                Portfolio exposure: \$1,650 ÷ \$10,000 = 16.5% ✅
                ```
                
                ## Common Mistakes
                
                ❌ Using same number of shares for every trade
                ❌ Not adjusting for stop loss distance
                ❌ Risking too much (>2% per trade)
                ❌ Exceeding 20% portfolio per position
                ❌ Not accounting for commission/slippage
                ❌ Adding to losing positions
                ❌ Not scaling position size with account
                ❌ Ignoring volatility differences
                ❌ Calculating incorrectly
                ❌ Not verifying total portfolio risk
                
                ## Pro Tips
                
                ✅ Always calculate position size before entry
                ✅ Risk percentage, not dollar amount (scales)
                ✅ Account for commission in calculations
                ✅ Use position sizing calculator/spreadsheet
                ✅ Never exceed 20% per position
                ✅ Adjust for volatility (ATR)
                ✅ Keep total portfolio risk <10%
                ✅ Round down, never round up
                ✅ Verify calculations before placing order
                ✅ Scale position with conviction
                ✅ Add to winners, not losers
                ✅ Journal position sizes to track
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the formula for calculating position size?",
                        options = listOf("Random guess", "Account Risk ÷ (Entry Price - Stop Loss Price)", "Always 100 shares", "Account Size ÷ 2"),
                        correctAnswer = 1,
                        explanation = "Position Size = Account Risk ÷ (Entry Price - Stop Loss Price). This ensures you risk the same dollar amount regardless of entry price or stop loss distance."
                    ),
                    QuizQuestion(
                        question = "What is the maximum recommended position size as a percentage of account?",
                        options = listOf("50%", "100%", "20%", "5%"),
                        correctAnswer = 2,
                        explanation = "Never exceed 20% of your account value in a single position. This prevents overconcentration and ensures diversification even if the position goes to zero."
                    ),
                    QuizQuestion(
                        question = "Should you add to winning or losing positions?",
                        options = listOf("Always add to losers (average down)", "Add to winners only (pyramid)", "Never add to any position", "Add randomly"),
                        correctAnswer = 1,
                        explanation = "Only add to winning positions (pyramiding). This increases exposure to what's working while maintaining your edge. Never add to losing positions (averaging down)."
                    )
                )
            )
        ),
        
        Lesson(
            id = 23,
            title = "Trade Psychology",
            category = "Trading Fundamentals",
            duration = "12 min",
            content = """
                # Trade Psychology
                
                ## Overview
                
                Trading psychology is the emotional and mental state that determines your trading success or failure. Studies show that trading success is 80% psychology and 20% strategy. You can have the best strategy in the world, but without proper psychology, you will fail.
                
                ## The Two Primary Emotions
                
                ### Fear:
                - **Fear of Loss**: Prevents entry or causes premature exit
                - **Fear of Missing Out (FOMO)**: Causes impulsive entries
                - **Fear of Being Wrong**: Prevents cutting losses
                - **Fear of Letting Profits Go**: Causes early profit-taking
                
                ### Greed:
                - **Over-trading**: Taking too many trades
                - **Over-sizing**: Position sizes too large
                - **Holding Too Long**: Not taking profits
                - **Revenge Trading**: Trying to win back losses
                
                ## Common Psychological Traps
                
                ### 1. Revenge Trading
                
                **Definition**: Trading to win back losses
                
                **Symptoms**:
                - Taking trades outside your plan
                - Increasing position size after losses
                - Abandoning strategy
                - Emotional decision-making
                
                **Example**:
                ```
                Lose \$100 on Trade 1
                "I need to make it back NOW!"
                Take risky Trade 2 with \$300 position
                Lose \$150
                Now down \$250 and desperate
                Continue cycle... blow up account
                ```
                
                **Solution**:
                ✅ Set daily/weekly loss limits
                ✅ Step away after 2-3 losses
                ✅ Never increase size after losses
                ✅ Accept losses as cost of business
                
                ### 2. FOMO (Fear of Missing Out)
                
                **Definition**: Entering trades because "everyone else is making money"
                
                **Symptoms**:
                - Chasing breakouts
                - Entering without proper setup
                - Buying tops/selling bottoms
                - Social media influenced trades
                
                **Example**:
                ```
                Stock up 50% in week
                Everyone talking about it
                You buy at peak
                Stock crashes 20% next day
                FOMO cost you money
                ```
                
                **Solution**:
                ✅ Wait for your setup
                ✅ Never chase parabolic moves
                ✅ Avoid social media during trading
                ✅ Trade your plan, not emotions
                
                ### 3. Analysis Paralysis
                
                **Definition**: Over-analyzing to point of inaction
                
                **Symptoms**:
                - Checking 20+ indicators
                - Never pulling the trigger
                - Waiting for "perfect" setup
                - Missing opportunities
                
                **Example**:
                ```
                Perfect setup appears
                "Let me check one more thing..."
                Add another indicator
                Check news
                Check opinion
                Setup gone, opportunity missed
                ```
                
                **Solution**:
                ✅ Define clear entry rules
                ✅ Limit indicators (3-5 max)
                ✅ Trust your analysis
                ✅ Pull trigger when rules met
                
                ### 4. Overconfidence
                
                **Definition**: Believing you can't lose after wins
                
                **Symptoms**:
                - Increasing position sizes
                - Taking low-quality setups
                - Ignoring risk management
                - Feeling invincible
                
                **Example**:
                ```
                5 winning trades in row
                "I can't lose!"
                Risk 10% on next trade
                Trade fails
                Lose week's profits
                ```
                
                **Solution**:
                ✅ Maintain same risk per trade
                ✅ Follow rules during win streaks
                ✅ Remember regression to mean
                ✅ Stay humble always
                
                ## Building Mental Discipline
                
                ### The Trading Plan:
                
                **Create Written Rules**:
                ```
                Entry Rules:
                - RSI <30
                - Bullish candlestick pattern
                - Above 200 SMA
                
                Exit Rules:
                - Stop: Below pattern low
                - Target 1: 2R
                - Target 2: 3R
                
                Risk Management:
                - Max risk: 1% per trade
                - Max positions: 5
                - Daily loss limit: 2%
                ```
                
                **Follow Rules 100%**:
                - No exceptions
                - No "gut feelings"
                - No revenge trades
                - No FOMO entries
                
                ### The Trading Journal:
                
                **Track Everything**:
                ```
                Date: 2025-01-15
                Setup: Bull Flag
                Entry: \$50.00
                Stop: \$48.50
                Target: \$53.00
                Risk: \$100 (1%)
                Result: +\$200 (2R)
                Emotional State: Calm, patient
                Mistakes: None
                Lessons: Waiting for setup paid off
                ```
                
                **Review Weekly**:
                - What worked?
                - What didn't work?
                - Emotional patterns?
                - Areas to improve?
                
                ## Emotional Control Techniques
                
                ### Technique 1: Pre-Trade Routine
                
                **Before Every Trade**:
                1. Take 3 deep breaths
                2. Review trading plan
                3. Calculate position size
                4. Set alerts/stops
                5. Visualize trade
                6. Execute with confidence
                
                ### Technique 2: Meditation
                
                **Benefits**:
                - Reduces stress
                - Improves focus
                - Enhances discipline
                - Better decision-making
                
                **Practice**:
                - 10 minutes daily
                - Before trading session
                - Focus on breathing
                - Clear your mind
                
                ### Technique 3: Physical Exercise
                
                **Benefits**:
                - Reduces cortisol (stress hormone)
                - Increases endorphins
                - Improves sleep
                - Better mental clarity
                
                **Recommendation**:
                - 30 minutes daily
                - Morning preferred
                - Cardio or weights
                - Before trading
                
                ### Technique 4: Breaks
                
                **Take Breaks**:
                - After big win: Step away
                - After big loss: Step away
                - After 2-3 losses: Stop trading
                - Screen fatigue: 15-min break every 2 hours
                
                ## The Professional Mindset
                
                ### Think Like a Casino:
                
                **Casinos don't worry about individual hands**:
                - They play the long game
                - They have edge
                - They know probabilities
                - Variance doesn't scare them
                
                **You should too**:
                - Focus on process, not outcome
                - 1,000 trades matter, not 1
                - Edge plays out over time
                - Accept variance
                
                ### Detach from Outcomes:
                
                **Bad Mindset**:
                ```
                "I NEED this trade to win"
                "I can't afford to lose"
                "This HAS to work"
                → Emotional trading → Mistakes
                ```
                
                **Good Mindset**:
                ```
                "This trade has 60% probability"
                "I might lose, that's okay"
                "I'll follow my rules"
                → Calm trading → Success
                ```
                
                ## Dealing with Losses
                
                ### Accept Losses:
                
                **Facts**:
                - Losses are inevitable
                - Even best traders lose 40-50%
                - Losses are cost of doing business
                - One loss means nothing
                
                **Reframe Losses**:
                ```
                ❌ "I'm a bad trader"
                ✅ "This trade didn't work"
                
                ❌ "I lost money"
                ✅ "I paid for education"
                
                ❌ "My strategy failed"
                ✅ "Variance happened"
                ```
                
                ### Learn from Losses:
                
                **After Every Loss**:
                1. Did I follow my rules? (Yes = Good loss!)
                2. What could I improve?
                3. Was setup actually valid?
                4. Update journal
                5. Move on (don't dwell)
                
                ## Advanced Psychology
                
                ### The Peak-End Rule:
                
                **Concept**: We remember peaks and ends
                
                **Application**:
                - End sessions on positive note
                - Don't force trades at day's end
                - Stop after big win (end high)
                - Review good trades weekly
                
                ### Loss Aversion:
                
                **Concept**: Losses hurt 2x more than gains feel good
                
                **Problem**: Holding losers, cutting winners early
                
                **Solution**:
                - Use stop losses (automate)
                - Use profit targets (automate)
                - Follow rules, not emotions
                
                ## Common Mistakes
                
                ❌ Trading while emotional (angry, sad, stressed)
                ❌ Revenge trading after losses
                ❌ FOMO entries without setup
                ❌ Overconfidence after wins
                ❌ Not having trading plan
                ❌ Not journaling trades
                ❌ Ignoring warning signs
                ❌ No daily loss limits
                ❌ Not taking breaks
                ❌ Comparing to other traders
                
                ## Pro Tips
                
                ✅ Psychology is 80% of trading success
                ✅ Have written trading plan (follow it!)
                ✅ Journal every trade with emotions
                ✅ Set daily loss limits (hard stop)
                ✅ Step away after 2-3 losses
                ✅ Meditate daily (10+ minutes)
                ✅ Exercise before trading
                ✅ Sleep 7-8 hours
                ✅ Accept losses as normal
                ✅ Never trade to "make back" losses
                ✅ Detach from individual trades
                ✅ Think in probabilities, not certainties
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What percentage of trading success is attributed to psychology?",
                        options = listOf("20%", "50%", "80%", "100%"),
                        correctAnswer = 2,
                        explanation = "Studies show that trading success is approximately 80% psychology and 20% strategy. Even the best strategy will fail without proper psychological discipline and emotional control."
                    ),
                    QuizQuestion(
                        question = "What is revenge trading and why is it dangerous?",
                        options = listOf("A good strategy", "Trading to win back losses, leading to bigger losses", "Trading for fun", "Trading with a plan"),
                        correctAnswer = 1,
                        explanation = "Revenge trading is attempting to quickly win back losses by taking impulsive, high-risk trades outside your plan. It's driven by emotion and typically leads to even larger losses."
                    ),
                    QuizQuestion(
                        question = "How should professional traders think about individual trades?",
                        options = listOf("Each trade must win", "Detach from outcomes, focus on process and probabilities", "Get emotional about each trade", "Ignore all trades"),
                        correctAnswer = 1,
                        explanation = "Professional traders detach from individual trade outcomes and focus on the process. They think in probabilities, understanding that their edge plays out over hundreds of trades, not one."
                    )
                )
            )
        ),
        
        Lesson(
            id = 24,
            title = "Backtesting Strategies",
            category = "Trading Advanced",
            duration = "11 min",
            content = """
                # Backtesting Strategies
                
                ## Overview
                
                Backtesting is the process of testing a trading strategy using historical data to determine its viability before risking real money. It's one of the most important steps in developing a profitable trading system. Without backtesting, you're essentially gambling.
                
                ## Why Backtest?
                
                ### Benefits:
                
                ✅ **Validate Strategy**: Prove strategy works
                ✅ **Identify Edge**: Quantify your advantage
                ✅ **Build Confidence**: Trust your system
                ✅ **Optimize Parameters**: Find best settings
                ✅ **Understand Drawdowns**: Know maximum losses
                ✅ **Eliminate Losers**: Filter bad strategies early
                ✅ **Save Money**: Lose in simulation, not reality
                
                ### The Reality:
                ```
                Without Backtesting:
                - Hope strategy works
                - Risk real money immediately
                - Lose confidence after losses
                - Abandon strategy too soon
                - Never know if edge exists
                
                With Backtesting:
                - Know strategy works (or doesn't)
                - Start with confidence
                - Expect normal losses
                - Stick with strategy
                - Proven edge
                ```
                
                ## Types of Backtesting
                
                ### 1. Manual Backtesting
                
                **Method**: Manually go through historical charts
                
                **Process**:
                1. Open historical chart
                2. Cover right side (future data)
                3. Move forward bar-by-bar
                4. Identify setups
                5. Record trades
                6. Calculate results
                
                **Advantages**:
                ✅ Free
                ✅ Understand market dynamics
                ✅ Build pattern recognition
                ✅ No programming needed
                
                **Disadvantages**:
                ❌ Time-consuming
                ❌ Prone to bias
                ❌ Limited sample size
                ❌ Manual errors
                
                ### 2. Automated Backtesting
                
                **Method**: Use software to test strategy
                
                **Popular Tools**:
                - TradingView (Pine Script)
                - MetaTrader (MQL4/MQL5)
                - Python (Backtrader, Zipline)
                - Amibroker
                - NinjaTrader
                
                **Advantages**:
                ✅ Fast (test years in minutes)
                ✅ Accurate
                ✅ Large sample sizes
                ✅ Repeatable
                ✅ Statistical analysis
                
                **Disadvantages**:
                ❌ Requires coding skills
                ❌ May cost money
                ❌ Learning curve
                ❌ Garbage in = garbage out
                
                ## Key Metrics to Track
                
                ### 1. Win Rate
                
                **Formula**: (Winning Trades ÷ Total Trades) × 100%
                
                **Example**:
                ```
                100 trades
                60 winners
                40 losers
                
                Win Rate = (60 ÷ 100) × 100% = 60%
                ```
                
                **Interpretation**:
                - **>60%**: Excellent
                - **50-60%**: Good
                - **40-50%**: Acceptable (with good R:R)
                - **<40%**: Poor (unless exceptional R:R)
                
                ### 2. Average Win/Loss
                
                **Average Win**: Total Profit ÷ Number of Wins
                **Average Loss**: Total Loss ÷ Number of Losses
                
                **Example**:
                ```
                60 wins: Total +\$6,000 → Avg Win = \$100
                40 losses: Total -\$2,000 → Avg Loss = \$50
                
                Win/Loss Ratio = \$100 ÷ \$50 = 2.0
                ```
                
                ### 3. Expectancy
                
                **Formula**:
                ```
                Expectancy = (Win Rate × Avg Win) - (Loss Rate × Avg Loss)
                ```
                
                **Example**:
                ```
                Win Rate: 60% (0.60)
                Avg Win: \$100
                Loss Rate: 40% (0.40)
                Avg Loss: \$50
                
                Expectancy = (0.60 × \$100) - (0.40 × \$50)
                           = \$60 - \$20
                           = \$40 per trade
                ```
                
                **Interpretation**:
                - **Positive**: Strategy has edge
                - **Negative**: Strategy loses money
                - **Higher = Better**: More profit per trade
                
                ### 4. Profit Factor
                
                **Formula**: Gross Profit ÷ Gross Loss
                
                **Example**:
                ```
                Gross Profit: \$6,000
                Gross Loss: \$2,000
                
                Profit Factor = \$6,000 ÷ \$2,000 = 3.0
                ```
                
                **Interpretation**:
                - **>2.0**: Excellent
                - **1.5-2.0**: Good
                - **1.0-1.5**: Acceptable
                - **<1.0**: Losing strategy
                
                ### 5. Maximum Drawdown
                
                **Definition**: Largest peak-to-trough decline
                
                **Example**:
                ```
                Account Peak: \$10,000
                Account Trough: \$7,500
                
                Max Drawdown = \$10,000 - \$7,500 = \$2,500 (25%)
                ```
                
                **Importance**: Shows worst-case scenario
                
                ### 6. Sharpe Ratio
                
                **Formula**: (Return - Risk Free Rate) ÷ Standard Deviation
                
                **Interpretation**:
                - **>3.0**: Excellent
                - **2.0-3.0**: Very good
                - **1.0-2.0**: Good
                - **<1.0**: Poor risk-adjusted returns
                
                ## Backtesting Process
                
                ### Step 1: Define Your Strategy
                
                **Entry Rules** (be specific!):
                ```
                ❌ "Buy when price goes up"
                ✅ "Buy when:
                    - RSI crosses above 30
                    - AND price above 200 SMA
                    - AND bullish engulfing candle
                    - AND volume >1.5x average"
                ```
                
                **Exit Rules**:
                ```
                Stop Loss: 2 × ATR below entry
                Take Profit 1: 2R (sell 50%)
                Take Profit 2: 3R (sell 50%)
                Trailing Stop: 1 × ATR
                ```
                
                ### Step 2: Gather Data
                
                **Requirements**:
                - Historical price data
                - Volume data
                - Indicator data
                - Sufficient time period (2+ years)
                - Multiple market conditions
                
                **Sources**:
                - Yahoo Finance (free)
                - TradingView (free/paid)
                - Quandl (free/paid)
                - Broker platforms
                
                ### Step 3: Test the Strategy
                
                **Manual Method**:
                1. Go to start date
                2. Hide future bars
                3. Move forward bar-by-bar
                4. Identify setups
                5. Record entry/exit/result
                6. Continue through all data
                
                **Automated Method**:
                1. Code strategy rules
                2. Run backtest
                3. Review results
                4. Analyze statistics
                
                ### Step 4: Analyze Results
                
                **Key Questions**:
                - Is expectancy positive?
                - Is win rate acceptable?
                - What's maximum drawdown?
                - How many trades?
                - Does it work in all market conditions?
                - Is profit factor >1.5?
                
                ### Step 5: Optimize (Carefully!)
                
                **Optimization**: Adjusting parameters for better results
                
                **Caution**:
                ```
                ❌ Curve Fitting: Optimizing to perfection
                   - Works on past data only
                   - Fails on future data
                   - Over-optimization
                
                ✅ Reasonable Optimization:
                   - Test range of values
                   - Find robust settings
                   - Not too specific
                ```
                
                **Example**:
                ```
                RSI Period:
                ❌ Test only 14: Too specific
                ❌ Find 14.37 is "perfect": Curve fitting
                ✅ Test 10, 12, 14, 16, 18: Find 12-16 works
                ✅ Use 14 (middle): Robust
                ```
                
                ## Common Pitfalls
                
                ### 1. Look-Ahead Bias
                
                **Problem**: Using future data in past
                
                **Example**:
                ```
                ❌ "Buy if price will be higher in 3 days"
                → You don't know future in real-time!
                
                ✅ "Buy if RSI <30 today"
                → You know this in real-time
                ```
                
                ### 2. Survivorship Bias
                
                **Problem**: Only testing stocks that survived
                
                **Example**:
                ```
                ❌ Test strategy on S&P 500 (2025 list)
                → Missing stocks that failed/delisted
                → Results too optimistic
                
                ✅ Test on historical S&P 500 constituents
                → Include delisted stocks
                → Realistic results
                ```
                
                ### 3. Cherry-Picking
                
                **Problem**: Testing only favorable periods
                
                **Example**:
                ```
                ❌ Test only bull market 2020-2021
                → Strategy looks great!
                → Fails in bear market
                
                ✅ Test 2015-2025 (bull + bear + sideways)
                → Realistic performance
                ```
                
                ### 4. Overfitting/Curve Fitting
                
                **Problem**: Optimizing too much
                
                **Example**:
                ```
                ❌ "RSI 14.73 + MA 23.41 + Volume 1.632x"
                → Perfect on past, fails on future
                
                ✅ "RSI 14 + MA 20 + Volume 1.5x"
                → Round numbers, robust
                ```
                
                ## Sample Size Matters
                
                **Minimum Trades**: 100+
                **Ideal**: 200-300+
                
                **Why**:
                ```
                10 trades: Not statistically significant
                30 trades: Small sample, high variance
                100 trades: Starting to be meaningful
                300+ trades: Statistically robust
                ```
                
                ## Forward Testing
                
                **After Backtesting**:
                1. **Paper Trade**: Test on live data, no real money
                2. **Monitor**: Track real-time performance
                3. **Compare**: Does it match backtest?
                4. **Adjust**: If needed (carefully)
                
                **Duration**: 1-3 months minimum
                
                ## Common Mistakes
                
                ❌ Not backtesting at all
                ❌ Testing on insufficient data (<1 year)
                ❌ Too few trades (<50)
                ❌ Look-ahead bias
                ❌ Survivorship bias
                ❌ Curve fitting/over-optimization
                ❌ Cherry-picking time periods
                ❌ Ignoring transaction costs
                ❌ Not forward testing
                ❌ Trusting single backtest
                
                ## Pro Tips
                
                ✅ Test minimum 2 years of data
                ✅ Include different market conditions
                ✅ Require 100+ trades minimum
                ✅ Account for commissions/slippage
                ✅ Be wary of "too good" results
                ✅ Use out-of-sample testing
                ✅ Forward test before real money
                ✅ Keep strategies simple (more robust)
                ✅ Document everything
                ✅ Retest periodically
                ✅ Positive expectancy is key
                ✅ Maximum drawdown must be tolerable
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is expectancy in backtesting?",
                        options = listOf("Win rate", "Average profit per trade", "Total profit", "Number of trades"),
                        correctAnswer = 1,
                        explanation = "Expectancy is the average profit (or loss) you can expect per trade, calculated as: (Win Rate × Avg Win) - (Loss Rate × Avg Loss). Positive expectancy means the strategy has an edge."
                    ),
                    QuizQuestion(
                        question = "What is curve fitting and why is it dangerous?",
                        options = listOf("Good optimization", "Over-optimizing to past data, causing future failure", "A type of chart", "Normal backtesting"),
                        correctAnswer = 1,
                        explanation = "Curve fitting is over-optimizing parameters to fit historical data perfectly. It creates strategies that work great on past data but fail on future data because they're too specifically tailored to past market conditions."
                    ),
                    QuizQuestion(
                        question = "What is the minimum number of trades for a statistically meaningful backtest?",
                        options = listOf("10 trades", "25 trades", "100+ trades", "5 trades"),
                        correctAnswer = 2,
                        explanation = "A minimum of 100 trades is needed for a backtest to be statistically meaningful. Fewer trades have too much variance and don't provide reliable results. Ideally, 200-300+ trades should be tested."
                    )
                )
            )
        ),
        
        Lesson(
            id = 25,
            title = "Building Your Trading Plan",
            category = "Trading Fundamentals",
            duration = "14 min",
            content = """
                # Building Your Trading Plan
                
                ## Overview
                
                A trading plan is your complete blueprint for trading success. It's a written document that defines your approach, rules, and processes. Professional traders all have detailed trading plans. Without one, you're not trading—you're gambling.
                
                ## Why You Need a Trading Plan
                
                ### The Reality:
                ```
                Without a Plan:
                - Random entries and exits
                - Emotional decision-making
                - Inconsistent results
                - No way to improve
                - Trading based on "feelings"
                
                With a Plan:
                - Clear rules and criteria
                - Objective decisions
                - Consistent approach
                - Track and improve
                - Professional trading
                ```
                
                ### Benefits:
                ✅ Removes emotion from trading
                ✅ Provides clear decision framework
                ✅ Enables performance tracking
                ✅ Builds discipline and confidence
                ✅ Allows systematic improvement
                ✅ Prevents impulsive trades
                ✅ Defines your edge clearly
                
                ## Components of a Trading Plan
                
                ### 1. Trading Goals
                
                **Define Clear Goals**:
                
                **Financial Goals**:
                ```
                ❌ "Make a lot of money"
                ✅ "Grow account 15% this year"
                ✅ "Average 3% monthly return"
                ✅ "Generate \$2,000/month income"
                ```
                
                **Performance Goals**:
                ```
                ✅ "Follow trading plan 100%"
                ✅ "Journal every trade"
                ✅ "Maximum 3% drawdown per month"
                ✅ "Complete 100 backtested trades"
                ```
                
                **SMART Goals**:
                - **S**pecific: Clear and detailed
                - **M**easurable: Track progress
                - **A**chievable: Realistic
                - **R**elevant: Aligned with objectives
                - **T**ime-bound: Deadline defined
                
                ### 2. Market Selection
                
                **What Will You Trade?**
                
                **Asset Classes**:
                ```
                Options:
                ☐ Stocks (US, International)
                ☐ Forex (Currency Pairs)
                ☐ Futures (Commodities, Indices)
                ☐ Cryptocurrencies
                ☐ Options
                ☐ ETFs
                
                My Choice: Stocks (Large Cap US)
                ```
                
                **Specific Criteria**:
                ```
                For Stocks:
                - Market Cap: >\$10 billion
                - Average Volume: >5 million shares/day
                - Price: >\$20 per share
                - Sector: Technology, Healthcare, Finance
                
                Why: Liquidity, lower manipulation risk
                ```
                
                ### 3. Trading Style and Timeframe
                
                **Choose Your Style**:
                
                **Scalping**:
                - Timeframe: Seconds to minutes
                - Holding: Minutes to hours
                - Charts: 1-min, 5-min
                - Trades/Day: 10-100+
                - Intensity: Very high
                
                **Day Trading**:
                - Timeframe: Minutes to hours
                - Holding: No overnight
                - Charts: 5-min, 15-min, hourly
                - Trades/Day: 1-10
                - Intensity: High
                
                **Swing Trading**:
                - Timeframe: Hours to days
                - Holding: Days to weeks
                - Charts: Hourly, 4-hour, daily
                - Trades/Week: 2-10
                - Intensity: Moderate
                
                **Position Trading**:
                - Timeframe: Days to months
                - Holding: Weeks to months
                - Charts: Daily, weekly
                - Trades/Month: 1-5
                - Intensity: Low
                
                **My Choice**: Swing Trading (daily charts)
                
                ### 4. Trading Strategy
                
                **Define Your Edge**:
                
                **Strategy Name**: Trend Continuation Pullback
                
                **Entry Rules** (specific!):
                ```
                ALL must be true:
                1. Price above 200-day SMA (uptrend)
                2. Pullback to 50% or 61.8% Fibonacci
                3. RSI <40 (oversold)
                4. Bullish reversal candle (hammer, engulfing)
                5. Volume on reversal >1.5× average
                6. Price bounces off support level
                ```
                
                **Entry Timing**:
                ```
                - Conservative: Enter on break above reversal candle
                - Aggressive: Enter at close of reversal candle
                
                I use: Conservative (breakout confirmation)
                ```
                
                **Exit Rules**:
                
                **Stop Loss**:
                ```
                - Place below reversal candle low
                - Or 2 × ATR below entry
                - Never move stop loss against position
                
                Example:
                Entry: \$50
                Reversal low: \$48
                Stop: \$47.80 (below low)
                ```
                
                **Take Profit**:
                ```
                Target 1 (50% position): 2R (2× risk)
                Entry: \$50, Risk: \$2.20, Target: \$54.40
                
                Target 2 (30% position): 3R
                Target: \$56.60
                
                Target 3 (20% position): Trailing stop
                Trail: 1 × ATR below price
                ```
                
                ### 5. Risk Management
                
                **Risk Rules**:
                ```
                Per Trade Risk:
                - Maximum: 1% of account
                - Never exceed 2%
                
                Portfolio Risk:
                - Maximum positions: 5
                - Total risk: Not exceed 5%
                - Per sector: Maximum 40%
                
                Position Size:
                - Calculate: Risk $ ÷ (Entry - Stop)
                - Maximum: 20% of account value
                - Verify before every trade
                
                Loss Limits:
                - Daily: 2% (stop trading)
                - Weekly: 5% (stop trading)
                - Monthly: 10% (reassess strategy)
                ```
                
                ### 6. Trading Routine
                
                **Daily Routine**:
                
                **Pre-Market** (30 minutes before open):
                ```
                ☐ Review market news
                ☐ Check economic calendar
                ☐ Scan for setups
                ☐ Review open positions
                ☐ Check pre-market movers
                ☐ Update watchlist
                ☐ Review trading plan
                ```
                
                **During Market**:
                ```
                ☐ Monitor positions
                ☐ Watch for entry signals
                ☐ Execute trades per plan
                ☐ Set alerts on key levels
                ☐ Take breaks (every 2 hours)
                ☐ Stay disciplined
                ```
                
                **Post-Market**:
                ```
                ☐ Journal all trades
                ☐ Review P&L
                ☐ Update spreadsheet
                ☐ Screen for tomorrow's setups
                ☐ Check if rules followed
                ☐ Identify improvements
                ```
                
                **Weekly Review**:
                ```
                ☐ Calculate win rate
                ☐ Calculate expectancy
                ☐ Review journal
                ☐ Identify patterns
                ☐ Update plan if needed
                ☐ Plan next week
                ```
                
                ### 7. Trading Journal Template
                
                **For Each Trade**:
                ```
                Date: _______
                Symbol: _______
                Setup: _______
                Entry: \$_______ @ _______
                Stop: \$_______ (Risk: \$_______)
                Target 1: \$_______
                Target 2: \$_______
                Position Size: _______ shares
                Risk %: _______%
                
                Pre-Trade:
                ☐ All entry criteria met?
                ☐ Risk calculated?
                ☐ Stop set?
                
                Outcome:
                Exit: \$_______ @ _______
                P&L: \$_______ (_____%)
                R-Multiple: _______
                
                Emotional State:
                Before: _______
                During: _______
                After: _______
                
                Mistakes:
                _______________________
                
                Lessons Learned:
                _______________________
                ```
                
                ### 8. Performance Metrics
                
                **Track These**:
                ```
                Win Rate: _____%
                Average Win: \$_______
                Average Loss: \$_______
                Expectancy: \$_______
                Profit Factor: _______
                Total Trades: _______
                Max Drawdown: _____%
                Monthly Return: _____%
                Sharpe Ratio: _______
                
                Best Trade: \$_______
                Worst Trade: \$_______
                Largest Win Streak: _______
                Largest Loss Streak: _______
                
                Rule Adherence: _____%
                ```
                
                ### 9. Rules for Discipline
                
                **Mandatory Rules**:
                ```
                ✅ I will only trade my defined setups
                ✅ I will always use stop losses
                ✅ I will never risk more than 1% per trade
                ✅ I will stop trading after daily loss limit
                ✅ I will journal every trade
                ✅ I will never move stops against position
                ✅ I will never add to losing positions
                ✅ I will follow my plan 100%
                
                ❌ I will NOT trade on emotion
                ❌ I will NOT revenge trade
                ❌ I will NOT overtrade
                ❌ I will NOT ignore my stops
                ❌ I will NOT trade without a setup
                ❌ I will NOT check social media while trading
                ❌ I will NOT trade if tired/stressed
                ```
                
                ## Creating Your Plan
                
                ### Step 1: Write It Down
                
                **Document Everything**:
                - Type it out (don't just think it)
                - Be specific (no vague rules)
                - Include screenshots of setups
                - Print and keep visible
                
                ### Step 2: Backtest Your Strategy
                
                **Validate Your Edge**:
                - Test on 2+ years data
                - Minimum 100 trades
                - Calculate all metrics
                - Ensure positive expectancy
                
                ### Step 3: Start Small
                
                **Paper Trade First**:
                - 1-3 months paper trading
                - Follow plan exactly
                - Track all metrics
                - Build confidence
                
                **Then Go Live**:
                - Start with smallest size
                - Gradually increase as profitable
                - Maintain discipline
                
                ### Step 4: Review and Adapt
                
                **Continuous Improvement**:
                - Weekly reviews
                - Monthly analysis
                - Quarterly plan updates
                - Annual comprehensive review
                
                **What to Review**:
                - Which setups work best?
                - Which timeframes most profitable?
                - Emotional patterns?
                - Common mistakes?
                - Ways to improve?
                
                ## Common Mistakes
                
                ❌ Not having a written plan
                ❌ Plan too vague ("buy low, sell high")
                ❌ Not following the plan
                ❌ Changing plan after every loss
                ❌ No backtesting
                ❌ No journaling
                ❌ Not tracking metrics
                ❌ Overcomplicating (100-page plan)
                ❌ Never reviewing/improving
                ❌ Not adapting to changing markets
                
                ## Pro Tips
                
                ✅ Start simple (can always add complexity)
                ✅ Write plan down (essential!)
                ✅ Backtest before live trading
                ✅ Paper trade 1-3 months
                ✅ Review plan weekly
                ✅ Follow plan 100% (no exceptions)
                ✅ Journal every single trade
                ✅ Track all performance metrics
                ✅ Update plan based on data
                ✅ Keep plan visible at desk
                ✅ Trading plan is your "business plan"
                ✅ Success = Plan + Discipline + Time
            """.trimIndent(),
            quiz = Quiz(
                questions = listOf(
                    QuizQuestion(
                        question = "What is the most important characteristic of a trading plan?",
                        options = listOf("It's verbal only", "It's written down with specific, clear rules", "It changes daily", "It's very complex"),
                        correctAnswer = 1,
                        explanation = "A trading plan must be written down with specific, clear, and objective rules. Verbal plans are easily forgotten or modified by emotion. Written plans provide accountability and consistency."
                    ),
                    QuizQuestion(
                        question = "Before trading real money, what should you do with your trading plan?",
                        options = listOf("Nothing, start immediately", "Backtest it and paper trade for 1-3 months", "Tell your friends", "Change it daily"),
                        correctAnswer = 1,
                        explanation = "Before risking real money, you must backtest the strategy on historical data and paper trade for 1-3 months to validate the edge, build confidence, and ensure you can follow the plan consistently."
                    ),
                    QuizQuestion(
                        question = "How often should you review and update your trading plan?",
                        options = listOf("Never", "After every trade", "Weekly reviews with periodic updates based on data", "Only when losing"),
                        correctAnswer = 2,
                        explanation = "You should review your trading plan weekly to track performance and identify patterns, with updates made periodically based on data and analysis. Avoid changing it impulsively after losses."
                    )
                )
            )
        )
    )
}
