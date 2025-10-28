package com.lamontlabs.quantravision.comparison

import android.content.Context
import com.lamontlabs.quantravision.PatternMatch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * MultiChartComparison
 * Compare pattern detections across multiple charts/symbols
 * Detect cross-asset correlations and divergences
 */
object MultiChartComparison {

    data class ChartAnalysis(
        val symbol: String,
        val timeframe: String,
        val patterns: List<PatternMatch>,
        val dominantTrend: String // "bullish", "bearish", "neutral"
    )

    data class CrossChartCorrelation(
        val pattern: String,
        val symbols: List<String>,
        val avgConfidence: Double,
        val correlation: String // "strong", "moderate", "weak"
    )

    data class Divergence(
        val pattern1: String,
        val pattern2: String,
        val symbol1: String,
        val symbol2: String,
        val significance: Double
    )

    /**
     * Compare multiple charts side by side
     */
    fun compareCharts(
        context: Context,
        charts: List<ChartAnalysis>
    ): ComparisonResult {
        val correlations = findCorrelations(charts)
        val divergences = findDivergences(charts)
        val commonPatterns = findCommonPatterns(charts)

        return ComparisonResult(
            charts = charts,
            correlations = correlations,
            divergences = divergences,
            commonPatterns = commonPatterns
        )
    }

    data class ComparisonResult(
        val charts: List<ChartAnalysis>,
        val correlations: List<CrossChartCorrelation>,
        val divergences: List<Divergence>,
        val commonPatterns: Map<String, Int>
    )

    /**
     * Find patterns that appear across multiple charts
     */
    private fun findCorrelations(charts: List<ChartAnalysis>): List<CrossChartCorrelation> {
        val patternsByName = mutableMapOf<String, MutableList<Pair<String, Double>>>()

        charts.forEach { chart ->
            chart.patterns.forEach { match ->
                patternsByName
                    .getOrPut(match.patternName) { mutableListOf() }
                    .add(chart.symbol to match.confidence)
            }
        }

        return patternsByName
            .filter { it.value.size >= 2 } // At least 2 charts
            .map { (pattern, symbolsWithConf) ->
                val avgConf = symbolsWithConf.map { it.second }.average()
                val correlation = when {
                    symbolsWithConf.size >= charts.size * 0.75 -> "strong"
                    symbolsWithConf.size >= charts.size * 0.5 -> "moderate"
                    else -> "weak"
                }

                CrossChartCorrelation(
                    pattern = pattern,
                    symbols = symbolsWithConf.map { it.first },
                    avgConfidence = avgConf,
                    correlation = correlation
                )
            }
            .sortedByDescending { it.symbols.size }
    }

    /**
     * Find divergences between charts
     */
    private fun findDivergences(charts: List<ChartAnalysis>): List<Divergence> {
        val divergences = mutableListOf<Divergence>()

        for (i in charts.indices) {
            for (j in i + 1 until charts.size) {
                val chart1 = charts[i]
                val chart2 = charts[j]

                // Check for opposing trends
                if (chart1.dominantTrend == "bullish" && chart2.dominantTrend == "bearish") {
                    divergences.add(
                        Divergence(
                            pattern1 = chart1.patterns.firstOrNull()?.patternName ?: "Unknown",
                            pattern2 = chart2.patterns.firstOrNull()?.patternName ?: "Unknown",
                            symbol1 = chart1.symbol,
                            symbol2 = chart2.symbol,
                            significance = 0.8
                        )
                    )
                }
            }
        }

        return divergences
    }

    /**
     * Find patterns common across all charts
     */
    private fun findCommonPatterns(charts: List<ChartAnalysis>): Map<String, Int> {
        val patternCounts = mutableMapOf<String, Int>()

        charts.forEach { chart ->
            chart.patterns.forEach { match ->
                patternCounts[match.patternName] = (patternCounts[match.patternName] ?: 0) + 1
            }
        }

        return patternCounts.filter { it.value >= 2 }
    }

    /**
     * Calculate trend alignment score
     */
    fun calculateTrendAlignment(charts: List<ChartAnalysis>): Double {
        if (charts.isEmpty()) return 0.0

        val trendCounts = charts.groupingBy { it.dominantTrend }.eachCount()
        val maxCount = trendCounts.values.maxOrNull() ?: 0

        return maxCount.toDouble() / charts.size
    }

    /**
     * Find leading indicators (patterns that appear first)
     */
    fun findLeadingIndicators(
        charts: List<ChartAnalysis>,
        timeWindow: Long = 3600000L // 1 hour
    ): List<String> {
        val leadingSymbols = mutableListOf<String>()

        val sortedByTime = charts
            .flatMap { chart -> chart.patterns.map { chart.symbol to it } }
            .sortedBy { it.second.timestamp }

        if (sortedByTime.size >= 2) {
            val first = sortedByTime.first()
            val others = sortedByTime.drop(1)

            val isLeading = others.all { (_, match) ->
                match.timestamp - first.second.timestamp > timeWindow
            }

            if (isLeading) {
                leadingSymbols.add(first.first)
            }
        }

        return leadingSymbols
    }

    /**
     * Save comparison result
     */
    fun saveComparison(context: Context, result: ComparisonResult) {
        val json = JSONObject().apply {
            put("timestamp", System.currentTimeMillis())
            
            val chartsJson = JSONArray()
            result.charts.forEach { chart ->
                chartsJson.put(JSONObject().apply {
                    put("symbol", chart.symbol)
                    put("timeframe", chart.timeframe)
                    put("patternCount", chart.patterns.size)
                    put("trend", chart.dominantTrend)
                })
            }
            put("charts", chartsJson)

            val correlationsJson = JSONArray()
            result.correlations.forEach { corr ->
                correlationsJson.put(JSONObject().apply {
                    put("pattern", corr.pattern)
                    put("symbols", JSONArray(corr.symbols))
                    put("avgConfidence", corr.avgConfidence)
                    put("correlation", corr.correlation)
                })
            }
            put("correlations", correlationsJson)
        }

        File(context.filesDir, "chart_comparison.json").writeText(json.toString(2))
    }
}
