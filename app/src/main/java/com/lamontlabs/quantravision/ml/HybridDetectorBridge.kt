package com.lamontlabs.quantravision.ml

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import com.lamontlabs.quantravision.analysis.HybridPatternDetector
import com.lamontlabs.quantravision.detection.DetectionResult
import com.lamontlabs.quantravision.ml.fusion.BayesianFusionEngine
import com.lamontlabs.quantravision.ml.fusion.FusedPattern
import com.lamontlabs.quantravision.ml.fusion.MLDetection
import com.lamontlabs.quantravision.ml.fusion.TemplateDetection
import com.lamontlabs.quantravision.ml.fusion.TemporalStabilizer
import com.lamontlabs.quantravision.ml.fusion.BoundingBox
import com.lamontlabs.quantravision.ml.inference.DeltaDetectionOptimizer
import com.lamontlabs.quantravision.ml.optimization.PowerPolicyManager
import com.lamontlabs.quantravision.learning.ScanLearningEngine
import com.lamontlabs.quantravision.licensing.ProFeatureGate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * HybridDetectorBridge - Integration layer with optimization wrappers (ACTIVE IN PRODUCTION)
 * 
 * PRODUCTION STATUS: PARTIALLY ACTIVE
 * ====================================
 * This bridge wraps the existing HybridPatternDetector (which uses PatternDetector.kt internally)
 * with optimization layers. The core detection still uses OpenCV template matching (102 patterns).
 * The optimization layers (fusion, temporal stabilization, delta detection) are ACTIVE but wrap
 * the existing template-based detection rather than ML inference.
 * 
 * CURRENT ARCHITECTURE:
 * =====================
 * User/Service → HybridDetectorBridge → HybridPatternDetector → PatternDetector (OpenCV templates)
 *                       ↓                                              
 *              Optimization Layers (fusion, temporal, delta, power)
 * 
 * ACTIVE OPTIMIZATION LAYERS:
 * ===========================
 * - Phase 2: Bayesian fusion and temporal stabilization (ACTIVE, wraps template results)
 * - Phase 3: Delta detection for frame-skipping (ACTIVE, reduces redundant processing)
 * - Phase 5: Adaptive power management (ACTIVE, adjusts FPS based on battery/thermal)
 * 
 * FUTURE ENHANCEMENT (OPTIONAL):
 * ================================
 * The infrastructure supports future ML model integration (Apache 2.0 licensed only):
 * - Phase 1: Could add GPU-accelerated ML inference alongside template matching
 * - Phase 4: Could enable incremental learning from user feedback
 * - Fusion engine ready to combine ML and template detections with Bayesian probability
 * 
 * NOTE: Current system is 100% Apache 2.0 licensed. Any future ML models must also be
 * Apache 2.0 licensed. Previous YOLOv8 references removed due to AGPL-3.0 licensing conflict.
 * 
 * Usage:
 * ```kotlin
 * val bridge = HybridDetectorBridge(context)
 * val results = bridge.detectPatternsOptimized(chartBitmap)
 * ```
 */
class HybridDetectorBridge(private val context: Context) {
    
    // Existing detector - currently uses PatternDetector.kt with OpenCV template matching
    // Note: HybridPatternDetector name is historical; it currently only does template matching
    private val existingDetector = HybridPatternDetector(context)
    
    // New optimization layers
    private val fusionEngine = BayesianFusionEngine()
    private val temporalStabilizer = TemporalStabilizer()
    private val deltaOptimizer = DeltaDetectionOptimizer()
    private val powerManager = PowerPolicyManager(context)
    
    // Scan Learning Engine (PRO feature)
    private val scanLearning = ScanLearningEngine(context)
    private val scanLearningEnabled = ProFeatureGate.hasAccess(context)
    
    private var frameCount = 0
    private var optimizationEnabled = true
    
    /**
     * Detect patterns with all available optimizations
     * 
     * This method wraps the existing detector with optimization layers:
     * 1. Delta detection (skip if unchanged)
     * 2. Adaptive power policy
     * 3. OpenCV template detection (102 patterns, Apache 2.0 licensed)
     * 4. Bayesian fusion
     * 5. Temporal stabilization
     */
    suspend fun detectPatternsOptimized(chartImage: Bitmap): List<DetectionResult> = 
        withContext(Dispatchers.Default) {
            frameCount++
            
            val scanStartTime = SystemClock.elapsedRealtime()
            
            if (!optimizationEnabled) {
                // Fallback to existing detector
                return@withContext existingDetector.detectPatterns(chartImage)
            }
            
            // Phase 3: Check if we can skip processing (delta detection)
            if (!deltaOptimizer.shouldProcess(chartImage)) {
                val cached = deltaOptimizer.getCachedDetections()
                if (cached != null) {
                    Timber.v("Using cached detections (${cached.size} patterns)")
                    return@withContext convertFusedToDetectionResults(cached)
                }
            }
            
            // Phase 5: Get adaptive power policy
            val policy = powerManager.getOptimalPolicy()
            Timber.v("Power policy: ${policy.description}")
            
            // Run existing detection (this uses HybridPatternDetector)
            val detectionResults = existingDetector.detectPatterns(chartImage)
            
            // Convert existing results to fusion format
            val mlDetections = convertToMLDetections(detectionResults.filter { it.source == "ML" })
            val templateDetections = convertToTemplateDetections(detectionResults.filter { it.source != "ML" })
            
            // Phase 2: Bayesian fusion (combine ML + template with probability)
            val fusedDetections = fusionEngine.fuseDetections(mlDetections, templateDetections)
            
            // Phase 2: Temporal stabilization (multi-frame consensus)
            val stableDetections = temporalStabilizer.stabilize(fusedDetections)
            
            // Update cache
            deltaOptimizer.updateCache(stableDetections)
            
            // Convert back to DetectionResult format
            val optimizedResults = convertFusedToDetectionResults(stableDetections)
            
            if (ProFeatureGate.isActive(context)) {
                val scanDuration = SystemClock.elapsedRealtime() - scanStartTime
                
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        scanLearning.learnFromScan(
                            detections = optimizedResults,
                            timeframe = "unknown",
                            scanDurationMs = scanDuration,
                            chartImage = chartImage
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Scan learning failed (non-critical)")
                    }
                }
            }
            
            Timber.d("Detected ${optimizedResults.size} patterns with optimizations " +
                    "(cache hit rate: ${deltaOptimizer.getCacheHitRate()}, policy: ${policy.name})")
            
            return@withContext optimizedResults
        }
    
    /**
     * Convert DetectionResult to MLDetection format
     */
    private fun convertToMLDetections(results: List<DetectionResult>): List<MLDetection> {
        return results.map { result ->
            MLDetection(
                patternType = result.patternName,
                confidence = result.confidence,
                bbox = BoundingBox(
                    left = result.bbox?.left?.toInt() ?: 0,
                    top = result.bbox?.top?.toInt() ?: 0,
                    right = result.bbox?.right?.toInt() ?: 100,
                    bottom = result.bbox?.bottom?.toInt() ?: 100
                )
            )
        }
    }
    
    /**
     * Convert DetectionResult to TemplateDetection format
     */
    private fun convertToTemplateDetections(results: List<DetectionResult>): List<TemplateDetection> {
        return results.map { result ->
            TemplateDetection(
                patternType = result.patternName,
                confidence = result.confidence,
                bbox = BoundingBox(
                    left = result.bbox?.left?.toInt() ?: 0,
                    top = result.bbox?.top?.toInt() ?: 0,
                    right = result.bbox?.right?.toInt() ?: 100,
                    bottom = result.bbox?.bottom?.toInt() ?: 100
                )
            )
        }
    }
    
    /**
     * Convert FusedPattern back to DetectionResult format
     */
    private fun convertFusedToDetectionResults(fusedPatterns: List<FusedPattern>): List<DetectionResult> {
        return fusedPatterns.map { pattern ->
            DetectionResult(
                patternName = pattern.patternType,
                confidence = pattern.confidence,
                bbox = android.graphics.RectF(
                    pattern.bbox.left.toFloat(),
                    pattern.bbox.top.toFloat(),
                    pattern.bbox.right.toFloat(),
                    pattern.bbox.bottom.toFloat()
                ),
                source = pattern.sources.joinToString("+"),
                reasoning = pattern.reasoning,
                timestamp = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Enable or disable optimizations
     */
    fun setOptimizationsEnabled(enabled: Boolean) {
        optimizationEnabled = enabled
        Timber.i("AI optimizations ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Reset all optimizers (e.g., when chart changes)
     */
    fun reset() {
        temporalStabilizer.reset()
        deltaOptimizer.reset()
        frameCount = 0
        Timber.i("HybridDetectorBridge reset")
    }
    
    /**
     * Get performance statistics
     */
    fun getPerformanceStats(): BridgeStats {
        return BridgeStats(
            frameCount = frameCount,
            cacheHitRate = deltaOptimizer.getCacheHitRate(),
            temporalHistorySize = temporalStabilizer.getHistorySize(),
            optimizationsEnabled = optimizationEnabled
        )
    }
}

/**
 * Performance statistics for bridge
 */
data class BridgeStats(
    val frameCount: Int,
    val cacheHitRate: Float,
    val temporalHistorySize: Int,
    val optimizationsEnabled: Boolean
) {
    override fun toString(): String {
        return "BridgeStats: $frameCount frames, ${(cacheHitRate * 100).toInt()}% cache hit, " +
                "temporal: $temporalHistorySize frames, optimizations: $optimizationsEnabled"
    }
}
