package com.lamontlabs.quantravision.cache

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import timber.log.Timber

/**
 * Perceptual hashing for chart images.
 * Uses DCT-based pHash algorithm for robust similarity detection.
 */
object PerceptualHasher {
    
    private const val HASH_SIZE = 8  // 8x8 DCT hash
    private const val GRID_SIZE = 3  // 3x3 regional grid
    
    /**
     * Compute perceptual hash for an image.
     * Returns 64-bit hash as Long.
     */
    fun computeHash(bitmap: Bitmap): Long {
        var mat: Mat? = null
        var gray: Mat? = null
        var resized: Mat? = null
        var dct: Mat? = null
        
        try {
            // Convert to grayscale Mat
            mat = Mat()
            Utils.bitmapToMat(bitmap, mat)
            
            gray = Mat()
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGBA2GRAY)
            
            // Resize to 32x32 for DCT
            resized = Mat()
            Imgproc.resize(gray, resized, Size(32.0, 32.0), 0.0, 0.0, Imgproc.INTER_AREA)
            
            // Convert to float for DCT
            resized.convertTo(resized, org.opencv.core.CvType.CV_32F)
            
            // Compute DCT
            dct = Mat()
            Core.dct(resized, dct)
            
            // Extract top-left 8x8 corner (low frequencies)
            val hash = computeHashFromDCT(dct)
            
            return hash
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to compute perceptual hash")
            return 0L
        } finally {
            mat?.release()
            gray?.release()
            resized?.release()
            dct?.release()
        }
    }
    
    /**
     * Compute hash from DCT matrix.
     */
    private fun computeHashFromDCT(dct: Mat): Long {
        // Extract 8x8 top-left corner
        val values = FloatArray(HASH_SIZE * HASH_SIZE)
        var idx = 0
        for (y in 0 until HASH_SIZE) {
            for (x in 0 until HASH_SIZE) {
                values[idx++] = dct.get(y, x)[0].toFloat()
            }
        }
        
        // Compute median (excluding DC coefficient at 0,0)
        val sorted = values.copyOfRange(1, values.size).sorted()
        val median = sorted[sorted.size / 2]
        
        // Build hash: 1 if > median, 0 otherwise
        var hash = 0L
        for (i in values.indices) {
            if (values[i] > median) {
                hash = hash or (1L shl i)
            }
        }
        
        return hash
    }
    
    /**
     * Compute regional hashes for grid-based comparison.
     * Divides image into 3x3 grid and computes hash for each region.
     */
    fun computeRegionalHashes(bitmap: Bitmap): List<Long> {
        val regionHashes = mutableListOf<Long>()
        val regionWidth = bitmap.width / GRID_SIZE
        val regionHeight = bitmap.height / GRID_SIZE
        
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                try {
                    val x = col * regionWidth
                    val y = row * regionHeight
                    
                    val region = Bitmap.createBitmap(
                        bitmap,
                        x,
                        y,
                        regionWidth.coerceAtMost(bitmap.width - x),
                        regionHeight.coerceAtMost(bitmap.height - y)
                    )
                    
                    val hash = computeHash(region)
                    regionHashes.add(hash)
                    
                    region.recycle()
                } catch (e: Exception) {
                    Timber.e(e, "Failed to compute regional hash at ($row, $col)")
                    regionHashes.add(0L)
                }
            }
        }
        
        return regionHashes
    }
    
    /**
     * Compute Hamming distance between two hashes.
     * Returns number of differing bits (0-64).
     */
    fun hammingDistance(hash1: Long, hash2: Long): Int {
        var xor = hash1 xor hash2
        var distance = 0
        
        while (xor != 0L) {
            distance++
            xor = xor and (xor - 1)  // Clear least significant bit
        }
        
        return distance
    }
    
    /**
     * Compare two images using perceptual hashing.
     * Returns similarity score (0.0 to 1.0, where 1.0 is identical).
     */
    fun compareImages(bitmap1: Bitmap, bitmap2: Bitmap): Double {
        val hash1 = computeHash(bitmap1)
        val hash2 = computeHash(bitmap2)
        
        val distance = hammingDistance(hash1, hash2)
        
        // Normalize to 0-1 similarity (64 bits max distance)
        return 1.0 - (distance.toDouble() / 64.0)
    }
    
    /**
     * Compare using regional hashes for more accurate detection.
     * Returns true if images are similar (>90% similarity).
     */
    fun areSimilar(bitmap1: Bitmap, bitmap2: Bitmap, threshold: Double = 0.9): Boolean {
        val hashes1 = computeRegionalHashes(bitmap1)
        val hashes2 = computeRegionalHashes(bitmap2)
        
        if (hashes1.size != hashes2.size) {
            return false
        }
        
        // Count matching regions
        var matchingRegions = 0
        for (i in hashes1.indices) {
            val distance = hammingDistance(hashes1[i], hashes2[i])
            val similarity = 1.0 - (distance.toDouble() / 64.0)
            
            if (similarity >= threshold) {
                matchingRegions++
            }
        }
        
        // Require at least 80% of regions to match
        val matchRatio = matchingRegions.toDouble() / hashes1.size
        return matchRatio >= 0.8
    }
    
    /**
     * Compute histogram-based similarity (alternative to pHash).
     * Faster but less robust to geometric transformations.
     */
    fun computeHistogramSimilarity(bitmap1: Bitmap, bitmap2: Bitmap): Double {
        var mat1: Mat? = null
        var mat2: Mat? = null
        var gray1: Mat? = null
        var gray2: Mat? = null
        var hist1: Mat? = null
        var hist2: Mat? = null
        
        try {
            // Convert to grayscale
            mat1 = Mat()
            mat2 = Mat()
            Utils.bitmapToMat(bitmap1, mat1)
            Utils.bitmapToMat(bitmap2, mat2)
            
            gray1 = Mat()
            gray2 = Mat()
            Imgproc.cvtColor(mat1, gray1, Imgproc.COLOR_RGBA2GRAY)
            Imgproc.cvtColor(mat2, gray2, Imgproc.COLOR_RGBA2GRAY)
            
            // Compute histograms
            hist1 = Mat()
            hist2 = Mat()
            
            val histSize = org.opencv.core.MatOfInt(256)
            val ranges = org.opencv.core.MatOfFloat(0f, 256f)
            
            Imgproc.calcHist(
                listOf(gray1),
                org.opencv.core.MatOfInt(0),
                Mat(),
                hist1,
                histSize,
                ranges
            )
            
            Imgproc.calcHist(
                listOf(gray2),
                org.opencv.core.MatOfInt(0),
                Mat(),
                hist2,
                histSize,
                ranges
            )
            
            // Normalize histograms
            Core.normalize(hist1, hist1, 0.0, 1.0, Core.NORM_MINMAX)
            Core.normalize(hist2, hist2, 0.0, 1.0, Core.NORM_MINMAX)
            
            // Compare using correlation
            val correlation = Imgproc.compareHist(
                hist1,
                hist2,
                Imgproc.HISTCMP_CORREL
            )
            
            return correlation
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to compute histogram similarity")
            return 0.0
        } finally {
            mat1?.release()
            mat2?.release()
            gray1?.release()
            gray2?.release()
            hist1?.release()
            hist2?.release()
        }
    }
}
