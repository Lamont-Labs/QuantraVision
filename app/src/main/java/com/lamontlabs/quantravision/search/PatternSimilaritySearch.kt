package com.lamontlabs.quantravision.search

import android.content.Context
import android.graphics.Bitmap
import com.lamontlabs.quantravision.PatternMatch
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

/**
 * PatternSimilaritySearch
 * Find patterns similar to a given pattern using visual similarity
 */
object PatternSimilaritySearch {

    data class SimilarPattern(
        val patternName: String,
        val similarity: Double,
        val matches: List<PatternMatch>
    )

    /**
     * Find patterns visually similar to the given pattern
     */
    fun findSimilar(
        context: Context,
        targetPattern: String,
        allMatches: List<PatternMatch>,
        topN: Int = 5
    ): List<SimilarPattern> {
        // Load pattern templates
        val targetTemplate = loadPatternTemplate(context, targetPattern)
        if (targetTemplate == null) return emptyList()

        val similarities = mutableListOf<SimilarPattern>()

        // Group matches by pattern name
        val byPattern = allMatches.groupBy { it.patternName }

        byPattern.forEach { (patternName, matches) ->
            if (patternName != targetPattern) {
                val compareTemplate = loadPatternTemplate(context, patternName)
                if (compareTemplate != null) {
                    val similarity = calculateSimilarity(targetTemplate, compareTemplate)
                    similarities.add(
                        SimilarPattern(
                            patternName = patternName,
                            similarity = similarity,
                            matches = matches
                        )
                    )
                }
            }
        }

        return similarities
            .sortedByDescending { it.similarity }
            .take(topN)
    }

    /**
     * Search by uploading an image
     */
    fun searchByImage(
        context: Context,
        queryImage: Bitmap,
        allPatterns: List<String>
    ): List<SimilarPattern> {
        val queryMat = Mat()
        Utils.bitmapToMat(queryImage, queryMat)
        Imgproc.cvtColor(queryMat, queryMat, Imgproc.COLOR_RGBA2GRAY)

        val results = mutableListOf<SimilarPattern>()

        allPatterns.forEach { pattern ->
            val template = loadPatternTemplate(context, pattern)
            if (template != null) {
                val similarity = calculateSimilarity(queryMat, template)
                if (similarity > 0.5) {
                    results.add(
                        SimilarPattern(
                            patternName = pattern,
                            similarity = similarity,
                            matches = emptyList()
                        )
                    )
                }
            }
        }

        return results.sortedByDescending { it.similarity }
    }

    /**
     * Find pattern variations (different scales, rotations)
     */
    fun findVariations(
        context: Context,
        pattern: String,
        allMatches: List<PatternMatch>
    ): List<PatternMatch> {
        return allMatches
            .filter { it.patternName == pattern }
            .groupBy { it.scale }
            .entries
            .sortedBy { it.key }
            .flatMap { it.value }
    }

    /**
     * Calculate visual similarity between two pattern templates
     */
    private fun calculateSimilarity(template1: Mat, template2: Mat): Double {
        // Resize to same size for comparison
        val size = template1.size()
        val resized = Mat()
        Imgproc.resize(template2, resized, size)

        // Calculate structural similarity
        val result = Mat()
        Imgproc.matchTemplate(template1, resized, result, Imgproc.TM_CCOEFF_NORMED)
        
        val mmr = Core.minMaxLoc(result)
        return mmr.maxVal
    }

    /**
     * Load pattern template from assets
     */
    private fun loadPatternTemplate(context: Context, patternName: String): Mat? {
        // Simplified - in production, load from actual pattern template files
        return try {
            val inputStream = context.assets.open("pattern_templates/${patternName}_ref.png")
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            val mat = Mat()
            Utils.bitmapToMat(bitmap, mat)
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY)
            mat
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculate pattern feature similarity (shape, trend, volatility)
     */
    fun calculateFeatureSimilarity(pattern1: String, pattern2: String): Double {
        // Define pattern characteristics
        val bullishPatterns = setOf("bull_flag", "inverse_hs", "ascending_triangle", "double_bottom")
        val bearishPatterns = setOf("bear_flag", "head_shoulders", "descending_triangle", "double_top")
        val continuationPatterns = setOf("bull_flag", "bear_flag", "pennant")
        val reversalPatterns = setOf("head_shoulders", "inverse_hs", "double_top", "double_bottom")

        var similarity = 0.0

        // Check directional similarity
        if ((pattern1 in bullishPatterns && pattern2 in bullishPatterns) ||
            (pattern1 in bearishPatterns && pattern2 in bearishPatterns)) {
            similarity += 0.4
        }

        // Check type similarity
        if ((pattern1 in continuationPatterns && pattern2 in continuationPatterns) ||
            (pattern1 in reversalPatterns && pattern2 in reversalPatterns)) {
            similarity += 0.3
        }

        // Check structural similarity (simplified)
        if (pattern1.contains("flag") && pattern2.contains("flag")) {
            similarity += 0.3
        } else if (pattern1.contains("triangle") && pattern2.contains("triangle")) {
            similarity += 0.3
        } else if (pattern1.contains("double") && pattern2.contains("double")) {
            similarity += 0.3
        }

        return similarity.coerceAtMost(1.0)
    }
}
