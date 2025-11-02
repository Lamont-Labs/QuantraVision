package com.lamontlabs.quantravision.detection.confluence

import com.lamontlabs.quantravision.PatternMatch
import timber.log.Timber

object ConfluenceEngine {
    
    fun findConfluenceZones(
        patterns: List<PatternMatch>,
        gridSize: Int = 50,
        minPatterns: Int = 2
    ): List<ConfluenceZone> {
        if (patterns.size < minPatterns) {
            Timber.d("Insufficient patterns for confluence detection: ${patterns.size} < $minPatterns")
            return emptyList()
        }
        
        val clusters = SpatialBinner.clusterPatterns(patterns, gridSize)
        
        val zones = clusters
            .filter { it.size >= minPatterns }
            .mapNotNull { cluster ->
                val center = SpatialBinner.calculateClusterCenter(cluster) ?: return@mapNotNull null
                val strength = calculateStrength(cluster)
                
                ConfluenceZone(
                    patterns = cluster,
                    centerX = center.x,
                    centerY = center.y,
                    strength = strength
                )
            }
        
        Timber.i("Confluence detection: Found ${zones.size} zones from ${patterns.size} patterns")
        
        return zones.sortedByDescending { it.strength }
    }
    
    fun calculateStrength(patterns: List<PatternMatch>): Float {
        if (patterns.isEmpty()) return 0.0f
        
        val patternCount = patterns.size
        val uniquePatternTypes = patterns.map { it.patternName }.toSet().size
        val avgConfidence = patterns.map { it.confidence }.average().toFloat()
        
        val baseStrength = when (patternCount) {
            0, 1 -> 1.0f
            2 -> 1.5f
            else -> 2.0f
        }
        
        val diversityBonus = if (uniquePatternTypes > 1) 0.2f else 0.0f
        val confidenceBonus = (avgConfidence - 0.7f).coerceAtLeast(0.0f) * 0.5f
        
        return (baseStrength + diversityBonus + confidenceBonus).coerceAtMost(3.0f)
    }
    
    fun isHighStrengthZone(zone: ConfluenceZone): Boolean {
        return zone.strength >= 1.8f && zone.patternCount >= 2
    }
    
    fun getConfluenceDescription(zone: ConfluenceZone): String {
        val typeCount = zone.uniquePatternTypes
        val patternCount = zone.patternCount
        
        return when {
            patternCount == 0 -> "No confluence"
            patternCount == 1 -> "Single pattern"
            typeCount == 1 && patternCount > 1 -> "${patternCount}× Same Pattern"
            typeCount > 1 -> "${patternCount}× Multi-Pattern Confluence"
            else -> "${patternCount}× Confluence"
        }
    }
}
