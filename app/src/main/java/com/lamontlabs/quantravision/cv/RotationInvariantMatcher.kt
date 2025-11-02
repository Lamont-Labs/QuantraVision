package com.lamontlabs.quantravision.cv

import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import org.opencv.core.Core
import org.opencv.core.Size
import timber.log.Timber

/**
 * Rotation-invariant template matching for tilt tolerance.
 * 
 * Tests templates at multiple rotation angles (-5°, 0°, +5°) to handle:
 * - Tilted phone/device during screenshot capture
 * - Slightly rotated chart displays
 * - Non-orthogonal chart alignments
 * 
 * Performance optimization:
 * - Only applies rotation search to high-confidence matches (>0.7)
 * - Skips rotation search for low-confidence patterns to avoid slowdown
 * - Uses OpenCV's warpAffine (Apache 2.0 licensed)
 * 
 * Legal: OpenCV warpAffine function, Apache 2.0 compliant.
 * 
 * Expected impact:
 * - 5-10% improvement in detection rate for tilted charts
 * - Minimal performance overhead due to selective application
 */
object RotationInvariantMatcher {
    
    // Rotation angles to test (in degrees)
    private val ROTATION_ANGLES = listOf(-5.0, 0.0, 5.0)
    
    // Only apply rotation search to matches above this confidence
    private const val ROTATION_CONFIDENCE_THRESHOLD = 0.7
    
    // Performance tracking
    private var totalMatches = 0L
    private var rotationSearchesPerformed = 0L
    private var rotationImprovements = 0L
    private var totalRotationTimeMs = 0L
    
    /**
     * Match template with rotation invariance.
     * 
     * @param image Input image (grayscale Mat)
     * @param template Template to match (grayscale Mat)
     * @param initialConfidence Initial confidence from non-rotated matching
     * @return Best match result across all rotation angles
     */
    fun matchWithRotation(
        image: Mat,
        template: Mat,
        initialConfidence: Double
    ): RotationMatchResult {
        val startTime = System.currentTimeMillis()
        totalMatches++
        
        // Skip rotation search if initial confidence is low
        if (initialConfidence < ROTATION_CONFIDENCE_THRESHOLD) {
            return RotationMatchResult(
                confidence = initialConfidence,
                angle = 0.0,
                x = -1.0,
                y = -1.0,
                rotationApplied = false
            )
        }
        
        rotationSearchesPerformed++
        
        var bestConfidence = initialConfidence
        var bestAngle = 0.0
        var bestX = -1.0
        var bestY = -1.0
        
        // Test each rotation angle
        ROTATION_ANGLES.forEach { angle ->
            var rotatedTemplate: Mat? = null
            var result: Mat? = null
            
            try {
                // Rotate template
                rotatedTemplate = rotateImage(template, angle)
                
                // Perform template matching
                result = Mat()
                Imgproc.matchTemplate(image, rotatedTemplate, result, Imgproc.TM_CCOEFF_NORMED)
                
                // Find best match
                val mmr = Core.minMaxLoc(result)
                val confidence = mmr.maxVal
                
                // Update best match if improved
                if (confidence > bestConfidence) {
                    bestConfidence = confidence
                    bestAngle = angle
                    bestX = mmr.maxLoc.x
                    bestY = mmr.maxLoc.y
                    
                    if (angle != 0.0) {
                        rotationImprovements++
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "RotationInvariantMatcher: Matching failed at angle $angle")
            } finally {
                result?.release()
                rotatedTemplate?.release()
            }
        }
        
        // Track performance
        val elapsedMs = System.currentTimeMillis() - startTime
        totalRotationTimeMs += elapsedMs
        
        if (rotationSearchesPerformed % 100L == 0L) {
            logPerformanceStats()
        }
        
        return RotationMatchResult(
            confidence = bestConfidence,
            angle = bestAngle,
            x = bestX,
            y = bestY,
            rotationApplied = true
        )
    }
    
    /**
     * Rotate image by given angle using warpAffine.
     * 
     * @param image Input Mat to rotate
     * @param angleDegrees Rotation angle in degrees (positive = counter-clockwise)
     * @return Rotated Mat
     */
    private fun rotateImage(image: Mat, angleDegrees: Double): Mat {
        if (angleDegrees == 0.0) {
            return image.clone()
        }
        
        val rotated = Mat()
        
        try {
            // Calculate rotation center (image center)
            val center = Point(image.cols() / 2.0, image.rows() / 2.0)
            
            // Get rotation matrix
            val rotationMatrix = Imgproc.getRotationMatrix2D(center, angleDegrees, 1.0)
            
            // Apply warpAffine transformation
            Imgproc.warpAffine(
                image,
                rotated,
                rotationMatrix,
                Size(image.cols().toDouble(), image.rows().toDouble()),
                Imgproc.INTER_LINEAR,
                Core.BORDER_CONSTANT,
                org.opencv.core.Scalar(0.0)
            )
            
            // Release rotation matrix
            rotationMatrix.release()
            
            return rotated
            
        } catch (e: Exception) {
            rotated.release()
            Timber.e(e, "RotationInvariantMatcher: Rotation failed for angle $angleDegrees")
            return image.clone()
        }
    }
    
    /**
     * Check if rotation search should be applied based on confidence.
     * 
     * @param confidence Initial match confidence
     * @return true if rotation search is recommended, false otherwise
     */
    fun shouldApplyRotationSearch(confidence: Double): Boolean {
        return confidence >= ROTATION_CONFIDENCE_THRESHOLD
    }
    
    /**
     * Match template at specific rotation angles (custom angle list).
     * 
     * @param image Input image
     * @param template Template to match
     * @param angles List of angles to test (in degrees)
     * @return Best match result
     */
    fun matchWithCustomAngles(
        image: Mat,
        template: Mat,
        angles: List<Double>
    ): RotationMatchResult {
        var bestConfidence = 0.0
        var bestAngle = 0.0
        var bestX = -1.0
        var bestY = -1.0
        
        angles.forEach { angle ->
            var rotatedTemplate: Mat? = null
            var result: Mat? = null
            
            try {
                rotatedTemplate = rotateImage(template, angle)
                result = Mat()
                Imgproc.matchTemplate(image, rotatedTemplate, result, Imgproc.TM_CCOEFF_NORMED)
                
                val mmr = Core.minMaxLoc(result)
                val confidence = mmr.maxVal
                
                if (confidence > bestConfidence) {
                    bestConfidence = confidence
                    bestAngle = angle
                    bestX = mmr.maxLoc.x
                    bestY = mmr.maxLoc.y
                }
                
            } catch (e: Exception) {
                Timber.e(e, "RotationInvariantMatcher: Custom angle matching failed at $angle")
            } finally {
                result?.release()
                rotatedTemplate?.release()
            }
        }
        
        return RotationMatchResult(
            confidence = bestConfidence,
            angle = bestAngle,
            x = bestX,
            y = bestY,
            rotationApplied = true
        )
    }
    
    /**
     * Log performance statistics.
     */
    private fun logPerformanceStats() {
        val searchRate = if (totalMatches > 0) {
            (rotationSearchesPerformed * 100.0 / totalMatches)
        } else {
            0.0
        }
        
        val improvementRate = if (rotationSearchesPerformed > 0) {
            (rotationImprovements * 100.0 / rotationSearchesPerformed)
        } else {
            0.0
        }
        
        val avgTimeMs = if (rotationSearchesPerformed > 0) {
            totalRotationTimeMs.toDouble() / rotationSearchesPerformed
        } else {
            0.0
        }
        
        Timber.d(
            "RotationInvariantMatcher: ${rotationSearchesPerformed} searches " +
            "(${String.format("%.1f", searchRate)}% of matches), " +
            "${rotationImprovements} improvements " +
            "(${String.format("%.1f", improvementRate)}%), " +
            "avg ${String.format("%.2f", avgTimeMs)}ms"
        )
    }
    
    /**
     * Get performance statistics.
     * 
     * @return Quadruple of (totalMatches, rotationSearches, improvements, avgTimeMs)
     */
    fun getPerformanceStats(): Stats {
        val avgTimeMs = if (rotationSearchesPerformed > 0) {
            totalRotationTimeMs.toDouble() / rotationSearchesPerformed
        } else {
            0.0
        }
        
        return Stats(
            totalMatches = totalMatches,
            rotationSearches = rotationSearchesPerformed,
            improvements = rotationImprovements,
            avgTimeMs = avgTimeMs
        )
    }
    
    /**
     * Reset performance statistics.
     */
    fun resetStats() {
        totalMatches = 0L
        rotationSearchesPerformed = 0L
        rotationImprovements = 0L
        totalRotationTimeMs = 0L
    }
    
    /**
     * Result of rotation-invariant matching.
     */
    data class RotationMatchResult(
        val confidence: Double,
        val angle: Double,
        val x: Double,
        val y: Double,
        val rotationApplied: Boolean
    )
    
    /**
     * Performance statistics.
     */
    data class Stats(
        val totalMatches: Long,
        val rotationSearches: Long,
        val improvements: Long,
        val avgTimeMs: Double
    )
}
