package com.lamontlabs.quantravision.analysis

import android.content.Context
import android.graphics.Bitmap
import com.lamontlabs.quantravision.PatternDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Hybrid pattern detection system combining:
 * 1. ML-based detection (YOLOv8) for 6 chart-agnostic premium patterns
 * 2. Template-based detection (OpenCV) for 102 additional patterns
 *
 * Provides dual-tier detection with confidence differentiation:
 * - Tier 1 (ML): High confidence, platform-agnostic (TradingView, MetaTrader, Robinhood, etc.)
 * - Tier 2 (Template): Fast matching, optimized for candlestick charts
 */
class HybridPatternDetector(private val context: Context) {
    
    private val yoloDetector = YoloV8Detector(context)
    private val templateDetector = PatternDetector(context)
    
    private var mlDetectionEnabled = true
    private var templateDetectionEnabled = true
    
    enum class DetectionMethod {
        ML_YOLO,          // Machine learning based (chart-agnostic)
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
    }
    
    /**
     * Initialize both detection systems.
     * ML detector gracefully falls back if model file missing.
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        val mlLoaded = yoloDetector.loadModel()
        Timber.i("Hybrid detector initialized - ML: $mlLoaded, Template: available")
        true
    }
    
    /**
     * Run hybrid detection on chart screenshot.
     * Executes both ML and template detection in parallel for performance.
     * 
     * @param bitmap The chart image to analyze
     * @param originPath The source file path or identifier for provenance tracking
     */
    suspend fun detect(bitmap: Bitmap, originPath: String = "unknown"): List<HybridDetection> = withContext(Dispatchers.Default) {
        val results = mutableListOf<HybridDetection>()
        
        // Run both detectors in parallel
        val mlJob = async { 
            if (mlDetectionEnabled) runMLDetection(bitmap, originPath) else emptyList()
        }
        val templateJob = async { 
            if (templateDetectionEnabled) runTemplateDetection(bitmap, originPath) else emptyList()
        }
        
        val mlResults = mlJob.await()
        val templateResults = templateJob.await()
        
        // Combine results
        results.addAll(mlResults)
        results.addAll(templateResults)
        
        // Remove duplicates (ML takes precedence over template for same pattern)
        val deduplicated = deduplicateResults(results)
        
        // Sort by confidence descending
        deduplicated.sortedByDescending { it.confidence }
    }
    
    /**
     * Run YOLOv8 ML-based detection
     */
    private suspend fun runMLDetection(bitmap: Bitmap, originPath: String): List<HybridDetection> {
        return try {
            val mlDetections = yoloDetector.detect(bitmap)
            
            mlDetections.map { detection ->
                HybridDetection(
                    patternName = detection.patternName,
                    confidence = detection.confidence,
                    method = DetectionMethod.ML_YOLO,
                    boundingBox = detection.boundingBox,
                    metadata = mapOf(
                        "classIndex" to detection.classIndex,
                        "modelVersion" to "YOLOv8s-v1.0",
                        "chartAgnostic" to true,
                        "originPath" to originPath
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "ML detection failed")
            emptyList()
        }
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
     * ML detections take precedence over template detections for same pattern.
     */
    private fun deduplicateResults(results: List<HybridDetection>): List<HybridDetection> {
        val uniquePatterns = mutableMapOf<String, HybridDetection>()
        
        for (detection in results) {
            val existing = uniquePatterns[detection.patternName]
            
            when {
                existing == null -> {
                    uniquePatterns[detection.patternName] = detection
                }
                // ML takes precedence over template
                detection.method == DetectionMethod.ML_YOLO && 
                existing.method == DetectionMethod.TEMPLATE_OPENCV -> {
                    uniquePatterns[detection.patternName] = detection
                }
                // Higher confidence wins
                detection.confidence > existing.confidence -> {
                    uniquePatterns[detection.patternName] = detection
                }
            }
        }
        
        return uniquePatterns.values.toList()
    }
    
    /**
     * Enable/disable ML-based detection
     */
    fun setMLDetectionEnabled(enabled: Boolean) {
        mlDetectionEnabled = enabled
        Timber.d("ML detection ${if (enabled) "enabled" else "disabled"}")
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
            mlEnabled = mlDetectionEnabled,
            templateEnabled = templateDetectionEnabled,
            mlPatternCount = YoloV8Detector.PATTERN_LABELS.size,
            templatePatternCount = 102  // 108 total - 6 covered by ML
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
        yoloDetector.close()
    }
}
