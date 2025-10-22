package com.lamontlabs.quantravision.glossary

/**
 * Offline glossary of common patterns for retail users.
 * Deterministic strings only. Not localized in this seed.
 */
object Glossary {

    data class Entry(val name: String, val definition: String, val notes: String)

    val entries: List<Entry> = listOf(
        Entry(
            "Head & Shoulders",
            "Reversal with a higher peak between two lower peaks. Neckline break confirms.",
            "Reliability improves on increasing volume at left shoulder and head, fading on right."
        ),
        Entry(
            "Ascending Triangle",
            "Higher lows compress into flat resistance. Breakout continuation pattern.",
            "False break filters: require close above resistance and sustained momentum."
        ),
        Entry(
            "RSI Divergence",
            "Oscillator diverges from price, indicating momentum weakening.",
            "Use in conjunction with a structure like wedge/triangle to reduce noise."
        ),
        Entry(
            "Bullish Flag",
            "Sharp rise then tight downward channel. Continuation after breakout.",
            "Measured move often equals flagpole height."
        ),
        Entry(
            "Cup and Handle",
            "Rounded base with shallow pullback handle. Bullish after breakout.",
            "Handle should be <1/3 of cup depth for higher quality."
        )
    )

    fun find(name: String): Entry? = entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
}
