package com.lamontlabs.quantravision.analysis

import android.content.Context
import android.graphics.Bitmap
import com.lamontlabs.quantravision.PatternDetector
import com.lamontlabs.quantravision.alerts.PatternStrength
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Pattern detection system using OpenCV template-based detection.
 * Detects 102 chart patterns using multi-scale template matching.
 *
 * Note: Previously used hybrid ML+Template detection, but YOLOv8 was removed
 * for licensing compliance (AGPL-3.0 conflicts with commercial use).
 * 
 * Name kept as "HybridPatternDetector" for backward compatibility.
 * Uses 100% Apache 2.0 licensed OpenCV for commercial compliance.
 */
class HybridPatternDetector(private val context: Context) {
    
    private val templateDetector = PatternDetector(context)
    
    private var templateDetectionEnabled = true
    
    enum class DetectionMethod {
        @Deprecated("ML_YOLO removed for AGPL-3.0 licensing compliance")
        ML_YOLO,          // Machine learning based (chart-agnostic) - DEPRECATED/UNUSED
        TEMPLATE_OPENCV   // Template matching (candlestick optimized)
    }
    
    data class HybridDetection(
        val patternName: String,
        val confidence: Float,           // 0.0 - 1.0
        val method: DetectionMethod,
        val boundingBox: android.graphics.RectF?,
        val metadata: Map<String, Any> = emptyMap()
    ) {
        val isPremiumML: Boolean
            get() = method == DetectionMethod.ML_YOLO
        
        val displayConfidence: String
            get() = "${(confidence * 100).toInt()}%"
        
        val confidenceTier: String
            get() = when {
                confidence >= 0.9f -> "Very High"
                confidence >= 0.75f -> "High"
                confidence >= 0.6f -> "Medium"
                else -> "Low"
            }
        
        val strength: PatternStrength.StrengthLevel
            get() = PatternStrength.calculateStrength(confidence.toDouble())
        
        val strengthInfo: PatternStrength.StrengthInfo
            get() = PatternStrength.getStrengthInfo(confidence.toDouble())
    }
    
    /**
     * Initialize detection system.
     * OpenCV template-based detection is always available.
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        Timber.i("Pattern detector initialized - Template detection available")
        true
    }
    
    /**
     * Run template-based detection on chart screenshot.
     * Uses OpenCV multi-scale template matching.
     * 
     * @param bitmap The chart image to analyze
     * @param originPath The source file path or identifier for provenance tracking
     */
    suspend fun detect(bitmap: Bitmap, originPath: String = "unknown"): List<HybridDetection> = withContext(Dispatchers.Default) {
        // Run template detection
        val results = if (templateDetectionEnabled) {
            runTemplateDetection(bitmap, originPath)
        } else {
            emptyList()
        }
        
        // Remove duplicates (keep highest confidence per pattern)
        val deduplicated = deduplicateResults(results)
        
        // Sort by confidence descending
        deduplicated.sortedByDescending { it.confidence }
    }
    
    /**
     * Run OpenCV template-based detection
     */
    private suspend fun runTemplateDetection(bitmap: Bitmap, originPath: String): List<HybridDetection> {
        return try {
            // Use new bitmap-based API from PatternDetector with originPath
            val patternMatches = templateDetector.detectFromBitmap(bitmap, originPath)
            
            // Convert PatternMatch results to HybridDetection format
            patternMatches.map { match ->
                // Log warning if detectionBounds is null
                if (match.detectionBounds == null) {
                    Timber.w("Template detection succeeded but bounds unavailable for pattern: ${match.patternName}")
                }
                
                // Parse detectionBounds string ("x,y,w,h") to RectF
                val boundingBox = match.detectionBounds?.let { bounds ->
                    try {
                        val parts = bounds.split(",").map { it.toFloat() }
                        if (parts.size == 4) {
                            android.graphics.RectF(
                                parts[0],           // left (x)
                                parts[1],           // top (y)
                                parts[0] + parts[2], // right (x + width)
                                parts[1] + parts[3]  // bottom (y + height)
                            )
                        } else {
                            Timber.w("Malformed detectionBounds (expected 4 parts, got ${parts.size}): $bounds")
                            null
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to parse detectionBounds: $bounds")
                        null
                    }
                }
                
                // Track whether bounds are available
                val boundsAvailable = boundingBox != null
                
                HybridDetection(
                    patternName = match.patternName,
                    confidence = match.confidence.toFloat(),
                    method = DetectionMethod.TEMPLATE_OPENCV,
                    boundingBox = boundingBox,
                    metadata = mapOf(
                        "timeframe" to match.timeframe,
                        "scale" to match.scale,
                        "consensusScore" to match.consensusScore,
                        "windowMs" to match.windowMs,
                        "timestamp" to match.timestamp,
                        "detectionMethod" to "multi-scale-template-matching",
                        "originPath" to match.originPath,
                        "boundsAvailable" to boundsAvailable
                    )
                )
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Template detection failed")
            emptyList()
        }
    }
    
    /**
     * Remove duplicate detections.
     * Keeps highest confidence detection for each pattern name.
     */
    private fun deduplicateResults(results: List<HybridDetection>): List<HybridDetection> {
        val uniquePatterns = mutableMapOf<String, HybridDetection>()
        
        for (detection in results) {
            val existing = uniquePatterns[detection.patternName]
            
            // Keep the detection with higher confidence, or add if new pattern
            if (existing == null || detection.confidence > existing.confidence) {
                uniquePatterns[detection.patternName] = detection
            }
        }
        
        return uniquePatterns.values.toList()
    }
    
    /**
     * Enable/disable template-based detection
     */
    fun setTemplateDetectionEnabled(enabled: Boolean) {
        templateDetectionEnabled = enabled
        Timber.d("Template detection ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Get detection statistics
     */
    fun getStats(): DetectionStats {
        return DetectionStats(
            mlEnabled = false,  // ML detection removed for licensing compliance
            templateEnabled = templateDetectionEnabled,
            mlPatternCount = 0,  // YOLOv8 removed
            templatePatternCount = 102
        )
    }
    
    data class DetectionStats(
        val mlEnabled: Boolean,
        val templateEnabled: Boolean,
        val mlPatternCount: Int,
        val templatePatternCount: Int
    ) {
        val totalPatternCount = mlPatternCount + templatePatternCount
    }
    
    /**
     * Clean up resources
     */
    fun close() {
        // No resources to clean up for template-based detection
    }
}
