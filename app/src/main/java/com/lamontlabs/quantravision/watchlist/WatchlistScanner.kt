package com.lamontlabs.quantravision.watchlist

import android.content.Context
import android.graphics.Bitmap
import com.lamontlabs.quantravision.PatternDetector
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.alerts.PatternStrength
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class WatchlistScanner(
    private val context: Context,
    private val patternDetector: PatternDetector
) {

    data class WatchlistAlert(
        val symbol: String,
        val patterns: List<PatternMatch>,
        val topPattern: PatternMatch,
        val strength: PatternStrength.StrengthLevel,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    data class ScanResult(
        val scannedCount: Int,
        val patternsFound: Int,
        val alerts: List<WatchlistAlert>,
        val bullishCount: Int,
        val bearishCount: Int,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    private val isScanning = AtomicBoolean(false)
    private var scanJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    fun startAutoScan(
        intervalMinutes: Int = 5,
        onScanComplete: (ScanResult) -> Unit
    ) {
        if (isScanning.get()) {
            Timber.w("WatchlistScanner: Auto-scan already running")
            return
        }
        
        isScanning.set(true)
        scanJob = scope.launch {
            while (isActive && isScanning.get()) {
                try {
                    val result = scanWatchlist()
                    withContext(Dispatchers.Main) {
                        onScanComplete(result)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "WatchlistScanner: Error during auto-scan")
                }
                
                delay(intervalMinutes * 60 * 1000L)
            }
        }
        
        Timber.d("WatchlistScanner: Auto-scan started (interval: ${intervalMinutes}m)")
    }
    
    fun stopAutoScan() {
        isScanning.set(false)
        scanJob?.cancel()
        scanJob = null
        Timber.d("WatchlistScanner: Auto-scan stopped")
    }
    
    suspend fun scanWatchlist(): ScanResult = withContext(Dispatchers.IO) {
        val watchlist = SmartWatchlist.getAll(context)
        
        if (watchlist.isEmpty()) {
            return@withContext ScanResult(
                scannedCount = 0,
                patternsFound = 0,
                alerts = emptyList(),
                bullishCount = 0,
                bearishCount = 0
            )
        }
        
        val alerts = mutableListOf<WatchlistAlert>()
        var totalPatterns = 0
        var bullishCount = 0
        var bearishCount = 0
        
        watchlist.forEach { item ->
            try {
                val patterns = getRecentPatterns(item.symbol)
                
                if (patterns.isNotEmpty()) {
                    totalPatterns += patterns.size
                    
                    val topPattern = patterns.maxByOrNull { it.confidence }
                    if (topPattern != null && topPattern.confidence >= item.alertThreshold) {
                        val strength = PatternStrength.calculateStrength(topPattern.confidence)
                        
                        alerts.add(
                            WatchlistAlert(
                                symbol = item.symbol,
                                patterns = patterns,
                                topPattern = topPattern,
                                strength = strength
                            )
                        )
                        
                        if (isBullishPattern(topPattern.patternName)) {
                            bullishCount++
                        } else if (isBearishPattern(topPattern.patternName)) {
                            bearishCount++
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "WatchlistScanner: Error scanning ${item.symbol}")
            }
        }
        
        val result = ScanResult(
            scannedCount = watchlist.size,
            patternsFound = totalPatterns,
            alerts = alerts.sortedByDescending { it.topPattern.confidence },
            bullishCount = bullishCount,
            bearishCount = bearishCount
        )
        
        Timber.d("WatchlistScanner: Scan complete - ${result.scannedCount} symbols, ${result.patternsFound} patterns, ${result.alerts.size} alerts")
        
        return@withContext result
    }
    
    private suspend fun getRecentPatterns(symbol: String): List<PatternMatch> {
        val db = patternDetector.getDatabase()
        val allPatterns = db.patternDao().getAll()
        
        val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
        
        return allPatterns.filter { 
            it.timestamp >= fiveMinutesAgo &&
            it.confidence >= 0.60
        }
    }
    
    private fun isBullishPattern(patternName: String): Boolean {
        val bullishKeywords = listOf(
            "bull", "ascending", "cup", "inverse head",
            "double bottom", "rising", "bullish"
        )
        return bullishKeywords.any { patternName.contains(it, ignoreCase = true) }
    }
    
    private fun isBearishPattern(patternName: String): Boolean {
        val bearishKeywords = listOf(
            "bear", "descending", "head & shoulders", "head and shoulders",
            "double top", "falling", "bearish"
        )
        return bearishKeywords.any { patternName.contains(it, ignoreCase = true) }
    }
    
    fun getTopOpportunities(limit: Int = 5): List<WatchlistAlert> {
        val watchlist = SmartWatchlist.getAll(context)
        val opportunities = mutableListOf<WatchlistAlert>()
        
        watchlist.forEach { item ->
            val patterns = getRecentPatterns(item.symbol)
            if (patterns.isNotEmpty()) {
                val topPattern = patterns.maxByOrNull { it.confidence }
                if (topPattern != null && topPattern.confidence >= 0.70) {
                    opportunities.add(
                        WatchlistAlert(
                            symbol = item.symbol,
                            patterns = patterns,
                            topPattern = topPattern,
                            strength = PatternStrength.calculateStrength(topPattern.confidence)
                        )
                    )
                }
            }
        }
        
        return opportunities
            .sortedByDescending { it.topPattern.confidence }
            .take(limit)
    }
    
    fun cleanup() {
        stopAutoScan()
        scope.cancel()
        Timber.d("WatchlistScanner: Cleanup complete")
    }
}
