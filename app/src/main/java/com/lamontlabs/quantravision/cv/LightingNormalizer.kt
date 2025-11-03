package com.lamontlabs.quantravision.cv

import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import timber.log.Timber

/**
 * Lighting normalization using CLAHE (Contrast Limited Adaptive Histogram Equalization).
 * 
 * Handles dark/light mode charts by normalizing brightness and contrast before template matching.
 * Uses OpenCV's CLAHE algorithm (Apache 2.0 licensed) for adaptive histogram equalization.
 * 
 * Legal: OpenCV CLAHE implementation, Apache 2.0 compliant.
 * 
 * Benefits:
 * - Works on any chart color scheme (dark mode, light mode, custom themes)
 * - Reduces false negatives caused by poor lighting conditions
 * - Improves pattern visibility in low-contrast regions
 * 
 * Performance:
 * - CLAHE is O(N) where N is number of pixels
 * - Adds ~5-10ms per frame on typical mobile devices
 * - Can be cached if input image hasn't changed
 */
object LightingNormalizer {
    
    // CLAHE parameters optimized for chart pattern detection
    private const val CLIP_LIMIT = 2.0  // Contrast limiting threshold
    private const val TILE_GRID_SIZE = 8  // Grid size for adaptive histogram
    
    // Brightness/contrast auto-adjustment thresholds
    private const val MIN_BRIGHTNESS = 50.0   // Below this, boost brightness
    private const val MAX_BRIGHTNESS = 205.0  // Above this, reduce brightness
    private const val TARGET_BRIGHTNESS = 127.5  // Ideal mid-gray
    
    // Performance tracking
    private var totalNormalizations = 0L
    private var totalTimeMs = 0L
    
    /**
     * Normalize lighting in grayscale image using CLAHE.
     * 
     * @param input Grayscale Mat (CV_8UC1)
     * @return Normalized Mat with improved contrast and brightness
     */
    fun normalize(input: Mat): Mat {
        val startTime = System.currentTimeMillis()
        val output = Mat()
        
        try {
            // Validate input
            if (input.empty()) {
                Timber.w("LightingNormalizer: Empty input Mat")
                return input.clone()
            }
            
            if (input.channels() != 1) {
                Timber.w("LightingNormalizer: Input must be grayscale (CV_8UC1), got ${input.channels()} channels")
                return input.clone()
            }
            
            // Step 1: Auto-adjust brightness if needed
            val adjusted = autoAdjustBrightness(input)
            
            // Step 2: Apply CLAHE for adaptive contrast enhancement
            val clahe = Imgproc.createCLAHE(CLIP_LIMIT, Size(TILE_GRID_SIZE.toDouble(), TILE_GRID_SIZE.toDouble()))
            clahe.apply(adjusted, output)
            clahe.collectGarbage()
            
            // Track performance
            val elapsedMs = System.currentTimeMillis() - startTime
            totalNormalizations++
            totalTimeMs += elapsedMs
            
            if (totalNormalizations % 100L == 0L) {
                val avgMs = totalTimeMs.toDouble() / totalNormalizations
                Timber.d("LightingNormalizer: ${totalNormalizations} normalizations, avg ${String.format("%.2f", avgMs)}ms")
            }
            
            // Clean up
            if (adjusted !== input) {
                adjusted.release()
            }
            
            return output
            
        } catch (e: Exception) {
            Timber.e(e, "LightingNormalizer: Normalization failed")
            output.release()
            return input.clone()
        }
    }
    
    /**
     * Auto-adjust brightness to target mid-gray if image is too dark or too bright.
     * 
     * @param input Grayscale Mat
     * @return Brightness-adjusted Mat (may be same as input if no adjustment needed)
     */
    private fun autoAdjustBrightness(input: Mat): Mat {
        try {
            // Calculate mean brightness
            val mean = org.opencv.core.Core.mean(input)
            val brightness = mean.`val`[0]
            
            // Determine if adjustment is needed
            val needsAdjustment = brightness < MIN_BRIGHTNESS || brightness > MAX_BRIGHTNESS
            
            if (!needsAdjustment) {
                return input
            }
            
            // Calculate adjustment factor
            val delta = TARGET_BRIGHTNESS - brightness
            val adjusted = Mat()
            
            // Add brightness offset
            org.opencv.core.Core.add(input, org.opencv.core.Scalar(delta), adjusted)
            
            Timber.d("LightingNormalizer: Brightness adjusted from ${String.format("%.1f", brightness)} to target ${TARGET_BRIGHTNESS}")
            
            return adjusted
            
        } catch (e: Exception) {
            Timber.e(e, "LightingNormalizer: Brightness adjustment failed")
            return input
        }
    }
    
    /**
     * Normalize with custom CLAHE parameters.
     * Useful for fine-tuning on specific chart types.
     * 
     * @param input Grayscale Mat
     * @param clipLimit Contrast limiting threshold (default 2.0)
     * @param tileGridSize Grid size for adaptive histogram (default 8)
     * @return Normalized Mat
     */
    fun normalizeCustom(input: Mat, clipLimit: Double, tileGridSize: Int): Mat {
        val output = Mat()
        
        try {
            if (input.empty() || input.channels() != 1) {
                Timber.w("LightingNormalizer: Invalid input for custom normalization")
                return input.clone()
            }
            
            val adjusted = autoAdjustBrightness(input)
            val clahe = Imgproc.createCLAHE(clipLimit, Size(tileGridSize.toDouble(), tileGridSize.toDouble()))
            clahe.apply(adjusted, output)
            clahe.collectGarbage()
            
            if (adjusted !== input) {
                adjusted.release()
            }
            
            return output
            
        } catch (e: Exception) {
            Timber.e(e, "LightingNormalizer: Custom normalization failed")
            output.release()
            return input.clone()
        }
    }
    
    /**
     * Check if an image needs normalization based on brightness and contrast metrics.
     * Useful for skipping normalization on already well-lit images.
     * 
     * @param input Grayscale Mat
     * @return true if normalization is recommended, false otherwise
     */
    fun needsNormalization(input: Mat): Boolean {
        try {
            if (input.empty() || input.channels() != 1) {
                return false
            }
            
            // Check brightness
            val mean = org.opencv.core.Core.mean(input)
            val brightness = mean.`val`[0]
            
            if (brightness < MIN_BRIGHTNESS || brightness > MAX_BRIGHTNESS) {
                return true
            }
            
            // Check contrast (standard deviation)
            val meanMat = org.opencv.core.MatOfDouble()
            val stdDevMat = org.opencv.core.MatOfDouble()
            org.opencv.core.Core.meanStdDev(input, meanMat, stdDevMat)
            val stdDev = stdDevMat.get(0, 0)[0]
            
            meanMat.release()
            stdDevMat.release()
            
            // Low contrast indicates need for CLAHE
            val needsContrast = stdDev < 30.0
            
            return needsContrast
            
        } catch (e: Exception) {
            Timber.e(e, "LightingNormalizer: Failed to check normalization need")
            return false
        }
    }
    
    /**
     * Get performance statistics.
     * 
     * @return Pair of (totalNormalizations, averageTimeMs)
     */
    fun getPerformanceStats(): Pair<Long, Double> {
        val avgMs = if (totalNormalizations > 0) {
            totalTimeMs.toDouble() / totalNormalizations
        } else {
            0.0
        }
        return Pair(totalNormalizations, avgMs)
    }
    
    /**
     * Reset performance statistics.
     */
    fun resetStats() {
        totalNormalizations = 0L
        totalTimeMs = 0L
    }
}
