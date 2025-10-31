package com.lamontlabs.quantravision.watchlist

import android.content.Context
import com.lamontlabs.quantravision.PatternMatch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * SmartWatchlist
 * Enhanced watchlist with confluence alerts and pattern clustering
 */
object SmartWatchlist {

    data class WatchItem(
        val symbol: String,
        val alertThreshold: Double,
        val patterns: Set<String>, // Specific patterns to watch
        val notificationSound: String,
        val vibrate: Boolean,
        val added: String
    )

    data class ConfluenceAlert(
        val symbol: String,
        val patterns: List<String>,
        val avgConfidence: Double,
        val timestamp: Long
    )

    data class PatternCluster(
        val patterns: List<String>,
        val confidence: Double,
        val timeframe: String,
        val type: String // "bullish", "bearish", "neutral"
    )

    private const val FILE = "smart_watchlist.json"

    fun add(
        context: Context,
        symbol: String,
        threshold: Double = 0.7,
        patterns: Set<String> = emptySet(),
        sound: String = "default",
        vibrate: Boolean = true
    ) {
        val watchlist = loadWatchlist(context).toMutableList()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        
        watchlist.add(
            WatchItem(
                symbol = symbol,
                alertThreshold = threshold,
                patterns = patterns,
                notificationSound = sound,
                vibrate = vibrate,
                added = today
            )
        )

        saveWatchlist(context, watchlist)
    }

    fun remove(context: Context, symbol: String) {
        val watchlist = loadWatchlist(context).toMutableList()
        watchlist.removeAll { it.symbol == symbol }
        saveWatchlist(context, watchlist)
    }

    fun getAll(context: Context): List<WatchItem> {
        return loadWatchlist(context)
    }

    fun checkForAlerts(context: Context, matches: List<PatternMatch>): List<ConfluenceAlert> {
        val watchlist = loadWatchlist(context)
        val alerts = mutableListOf<ConfluenceAlert>()

        watchlist.forEach { item ->
            // Filter matches that exceed threshold
            val relevantMatches = matches.filter { match ->
                match.confidence >= item.alertThreshold &&
                (item.patterns.isEmpty() || item.patterns.contains(match.patternName))
            }

            // Check for confluence (multiple patterns)
            if (relevantMatches.size >= 2) {
                alerts.add(
                    ConfluenceAlert(
                        symbol = item.symbol,
                        patterns = relevantMatches.map { it.patternName },
                        avgConfidence = relevantMatches.map { it.confidence }.average(),
                        timestamp = System.currentTimeMillis()
                    )
                )
            } else if (relevantMatches.size == 1) {
                // Single strong signal
                val match = relevantMatches.first()
                if (match.confidence >= 0.85) {
                    alerts.add(
                        ConfluenceAlert(
                            symbol = item.symbol,
                            patterns = listOf(match.patternName),
                            avgConfidence = match.confidence,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        return alerts
    }

    fun detectPatternClusters(matches: List<PatternMatch>): List<PatternCluster> {
        val clusters = mutableListOf<PatternCluster>()

        // Group by timeframe
        val byTimeframe = matches.groupBy { it.timeframe }

        byTimeframe.forEach { (timeframe, tfMatches) ->
            if (tfMatches.size >= 3) {
                // Determine cluster type
                val type = determineClusterType(tfMatches.map { it.patternName })
                
                clusters.add(
                    PatternCluster(
                        patterns = tfMatches.map { it.patternName },
                        confidence = tfMatches.map { it.confidence }.average(),
                        timeframe = timeframe,
                        type = type
                    )
                )
            }
        }

        return clusters
    }

    private fun determineClusterType(patterns: List<String>): String {
        val bullishPatterns = setOf("bull_flag", "inverse_hs", "ascending_triangle", "double_bottom")
        val bearishPatterns = setOf("bear_flag", "head_shoulders", "descending_triangle", "double_top")

        val bullishCount = patterns.count { pattern -> 
            bullishPatterns.any { pattern.contains(it, ignoreCase = true) }
        }
        val bearishCount = patterns.count { pattern ->
            bearishPatterns.any { pattern.contains(it, ignoreCase = true) }
        }

        return when {
            bullishCount > bearishCount * 1.5 -> "bullish"
            bearishCount > bullishCount * 1.5 -> "bearish"
            else -> "neutral"
        }
    }

    private fun loadWatchlist(context: Context): List<WatchItem> {
        val file = File(context.filesDir, FILE)
        if (!file.exists()) return emptyList()

        val json = JSONArray(file.readText())
        val items = mutableListOf<WatchItem>()

        for (i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            val patternsJson = obj.optJSONArray("patterns") ?: JSONArray()
            val patterns = mutableSetOf<String>()
            for (j in 0 until patternsJson.length()) {
                patterns.add(patternsJson.getString(j))
            }

            items.add(
                WatchItem(
                    symbol = obj.getString("symbol"),
                    alertThreshold = obj.getDouble("alertThreshold"),
                    patterns = patterns,
                    notificationSound = obj.optString("notificationSound", "default"),
                    vibrate = obj.optBoolean("vibrate", true),
                    added = obj.getString("added")
                )
            )
        }

        return items
    }

    private fun saveWatchlist(context: Context, items: List<WatchItem>) {
        val json = JSONArray()
        items.forEach { item ->
            val patternsJson = JSONArray()
            item.patterns.forEach { patternsJson.put(it) }

            json.put(JSONObject().apply {
                put("symbol", item.symbol)
                put("alertThreshold", item.alertThreshold)
                put("patterns", patternsJson)
                put("notificationSound", item.notificationSound)
                put("vibrate", item.vibrate)
                put("added", item.added)
            })
        }

        File(context.filesDir, FILE).writeText(json.toString(2))
    }
    
    fun createScanner(context: Context, patternDetector: com.lamontlabs.quantravision.PatternDetector): WatchlistScanner {
        return WatchlistScanner(context, patternDetector)
    }
}
