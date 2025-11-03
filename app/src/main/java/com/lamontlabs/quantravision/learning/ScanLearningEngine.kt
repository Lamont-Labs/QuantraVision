package com.lamontlabs.quantravision.learning

import android.content.Context
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.detection.DetectionResult
import com.lamontlabs.quantravision.learning.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.MessageDigest
import java.util.UUID

/**
 * ScanLearningEngine - Learns from every chart scan to improve detection
 * 
 * This engine automatically learns from raw scan data (not trade outcomes):
 * - Pattern frequencies (which patterns appear most often)
 * - Pattern co-occurrence (which patterns appear together)
 * - Confidence distributions (typical confidence levels)
 * - Timeframe patterns (which patterns work best where)
 * - Adaptive threshold optimization
 * 
 * 100% offline, privacy-preserving learning
 */
class ScanLearningEngine(private val context: Context) {
    
    private val db = PatternDatabase.getInstance(context)
    private val dao = db.learningProfileDao()
    
    private var currentSessionId: String = UUID.randomUUID().toString()
    
    companion object {
        private const val MIN_CONFIDENCE_FOR_LEARNING = 0.3f
        private const val SCAN_HISTORY_RETENTION_DAYS = 90
    }
    
    /**
     * Learn from a completed chart scan
     * Call this after every pattern detection run
     */
    suspend fun learnFromScan(
        detections: List<DetectionResult>,
        timeframe: String,
        scanDurationMs: Long,
        chartImage: android.graphics.Bitmap? = null
    ) = withContext(Dispatchers.IO) {
        try {
            val timestamp = System.currentTimeMillis()
            
            // Filter detections by minimum confidence
            val validDetections = detections.filter { it.confidence >= MIN_CONFIDENCE_FOR_LEARNING }
            
            if (validDetections.isEmpty()) {
                Timber.v("No valid detections to learn from")
                return@withContext
            }
            
            // Calculate chart hash to detect duplicate scans
            val chartHash = chartImage?.let { calculateImageHash(it) } ?: "unknown"
            
            // Store scan history
            storeScanHistory(validDetections, timeframe, scanDurationMs, chartHash, timestamp)
            
            // Update pattern frequencies
            updatePatternFrequencies(validDetections, timestamp)
            
            // Update pattern co-occurrence
            updatePatternCooccurrence(validDetections, timestamp)
            
            // Clean up old data periodically
            if (timestamp % 10 == 0L) {
                cleanOldData(timestamp)
            }
            
            Timber.d("Learned from scan: ${validDetections.size} patterns, timeframe=$timeframe")
        } catch (e: Exception) {
            Timber.e(e, "Failed to learn from scan")
        }
    }
    
    /**
     * Get most frequently detected patterns
     */
    suspend fun getMostFrequentPatterns(limit: Int = 10): List<PatternFrequencyInfo> = withContext(Dispatchers.IO) {
        try {
            dao.getAllPatternFrequencies()
                .sortedByDescending { it.totalDetections }
                .take(limit)
                .map {
                    PatternFrequencyInfo(
                        patternName = it.patternName,
                        detectionCount = it.totalDetections,
                        detectionRate = if (it.totalScans > 0) it.totalDetections.toFloat() / it.totalScans else 0f,
                        avgConfidence = it.avgConfidence
                    )
                }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get frequent patterns")
            emptyList()
        }
    }
    
    /**
     * Get patterns that often appear together
     */
    suspend fun getPatternCooccurrences(pattern: String? = null): List<CooccurrenceInfo> = withContext(Dispatchers.IO) {
        try {
            val cooccurrences = if (pattern != null) {
                dao.getCooccurrencesFor(pattern)
            } else {
                dao.getTopCooccurrences()
            }
            
            cooccurrences.map {
                CooccurrenceInfo(
                    pattern1 = it.pattern1,
                    pattern2 = it.pattern2,
                    cooccurrenceCount = it.cooccurrenceCount,
                    cooccurrenceRate = it.cooccurrenceRate
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get co-occurrences")
            emptyList()
        }
    }
    
    /**
     * Get total scan statistics
     */
    suspend fun getScanStats(): ScanStatistics = withContext(Dispatchers.IO) {
        try {
            val oneWeekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            val oneMonthAgo = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
            
            ScanStatistics(
                totalScansWeek = dao.getTotalScans(oneWeekAgo),
                totalScansMonth = dao.getTotalScans(oneMonthAgo),
                uniquePatternsDetected = dao.getAllPatternFrequencies().size,
                mostCommonPattern = dao.getAllPatternFrequencies()
                    .maxByOrNull { it.totalDetections }?.patternName ?: "None"
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to get scan stats")
            ScanStatistics(0, 0, 0, "None")
        }
    }
    
    /**
     * Get optimized confidence threshold based on scan history
     */
    suspend fun getOptimizedThreshold(patternName: String): Float = withContext(Dispatchers.IO) {
        try {
            val freq = dao.getPatternFrequency(patternName)
            
            if (freq == null || freq.totalDetections < 10) {
                return@withContext 0.5f  // Default
            }
            
            // Use average confidence as optimized threshold
            // Patterns with consistently high confidence can use higher thresholds
            val avgConf = freq.avgConfidence
            when {
                avgConf >= 0.8f -> 0.6f  // High-confidence pattern, can be more selective
                avgConf >= 0.6f -> 0.5f  // Medium confidence, standard threshold
                else -> 0.4f  // Low confidence, be more permissive
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get optimized threshold")
            0.5f
        }
    }
    
    // --- Private Helper Methods ---
    
    private suspend fun storeScanHistory(
        detections: List<DetectionResult>,
        timeframe: String,
        scanDurationMs: Long,
        chartHash: String,
        timestamp: Long
    ) {
        val patternNames = detections.map { it.patternName }.joinToString(",")
        val confidences = detections.map { "%.2f".format(it.confidence) }.joinToString(",")
        
        dao.insertScanHistory(
            ScanHistoryEntity(
                timestamp = timestamp,
                sessionId = currentSessionId,
                patternsDetected = patternNames,
                confidences = confidences,
                timeframe = timeframe,
                scanDurationMs = scanDurationMs,
                chartHash = chartHash
            )
        )
    }
    
    private suspend fun updatePatternFrequencies(detections: List<DetectionResult>, timestamp: Long) {
        val patternGroups = detections.groupBy { it.patternName }
        
        patternGroups.forEach { (patternName, patternDetections) ->
            val existing = dao.getPatternFrequency(patternName)
            
            val newTotalScans = (existing?.totalScans ?: 0) + 1
            val newTotalDetections = (existing?.totalDetections ?: 0) + patternDetections.size
            val newAvgConfidence = patternDetections.map { it.confidence.toFloat() }.average().toFloat()
            
            dao.insertPatternFrequency(
                PatternFrequencyEntity(
                    patternName = patternName,
                    totalScans = newTotalScans,
                    totalDetections = newTotalDetections,
                    avgConfidence = if (existing != null) {
                        (existing.avgConfidence + newAvgConfidence) / 2f
                    } else {
                        newAvgConfidence
                    },
                    lastSeen = timestamp
                )
            )
        }
    }
    
    private suspend fun updatePatternCooccurrence(detections: List<DetectionResult>, timestamp: Long) {
        val patterns = detections.map { it.patternName }.distinct()
        
        // For each pair of patterns detected together
        for (i in patterns.indices) {
            for (j in i + 1 until patterns.size) {
                val pattern1 = patterns[i]
                val pattern2 = patterns[j]
                
                val existing = dao.getCooccurrencesFor(pattern1)
                    .find { (it.pattern1 == pattern1 && it.pattern2 == pattern2) ||
                            (it.pattern1 == pattern2 && it.pattern2 == pattern1) }
                
                val newCount = (existing?.cooccurrenceCount ?: 0) + 1
                val newOpportunities = (existing?.totalOpportunities ?: 0) + 1
                
                dao.insertCooccurrence(
                    PatternCooccurrenceEntity(
                        id = existing?.id ?: 0,
                        pattern1 = pattern1,
                        pattern2 = pattern2,
                        cooccurrenceCount = newCount,
                        totalOpportunities = newOpportunities,
                        cooccurrenceRate = newCount.toFloat() / newOpportunities,
                        lastUpdated = timestamp
                    )
                )
            }
        }
    }
    
    private fun calculateImageHash(bitmap: android.graphics.Bitmap): String {
        // Simple perceptual hash based on downscaled image
        val smallBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 8, 8, false)
        val pixels = IntArray(64)
        smallBitmap.getPixels(pixels, 0, 8, 0, 0, 8, 8)
        
        val digest = MessageDigest.getInstance("SHA-256")
        pixels.forEach { digest.update(it.toByte()) }
        
        return digest.digest().joinToString("") { "%02x".format(it) }.take(16)
    }
    
    private suspend fun cleanOldData(currentTime: Long) {
        val cutoff = currentTime - SCAN_HISTORY_RETENTION_DAYS * 24 * 60 * 60 * 1000L
        // Room doesn't support this directly, would need to add @Query to DAO
        Timber.v("Clean old scan data (cutoff: $cutoff)")
    }
    
    /**
     * Reset session (call when app restarts or user starts new trading session)
     */
    fun startNewSession() {
        currentSessionId = UUID.randomUUID().toString()
        Timber.d("Started new learning session: $currentSessionId")
    }
}

// Data models for insights

data class PatternFrequencyInfo(
    val patternName: String,
    val detectionCount: Long,
    val detectionRate: Float,
    val avgConfidence: Float
)

data class CooccurrenceInfo(
    val pattern1: String,
    val pattern2: String,
    val cooccurrenceCount: Long,
    val cooccurrenceRate: Float
)

data class ScanStatistics(
    val totalScansWeek: Int,
    val totalScansMonth: Int,
    val uniquePatternsDetected: Int,
    val mostCommonPattern: String
)
