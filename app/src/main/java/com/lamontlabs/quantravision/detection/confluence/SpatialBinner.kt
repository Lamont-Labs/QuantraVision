package com.lamontlabs.quantravision.detection.confluence

import com.lamontlabs.quantravision.PatternMatch
import kotlin.math.sqrt

object SpatialBinner {
    
    data class Point(val x: Double, val y: Double)
    
    data class GridCell(val row: Int, val col: Int) {
        override fun hashCode(): Int = row * 31 + col
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is GridCell) return false
            return row == other.row && col == other.col
        }
    }
    
    fun clusterPatterns(
        patterns: List<PatternMatch>,
        gridSize: Int = 50
    ): List<List<PatternMatch>> {
        if (patterns.isEmpty()) return emptyList()
        
        val grid = mutableMapOf<GridCell, MutableList<PatternMatch>>()
        
        patterns.forEach { pattern ->
            val center = extractCenter(pattern)
            if (center != null) {
                val cell = GridCell(
                    row = (center.y / gridSize).toInt(),
                    col = (center.x / gridSize).toInt()
                )
                grid.getOrPut(cell) { mutableListOf() }.add(pattern)
            }
        }
        
        return grid.values
            .filter { it.size >= 2 }
            .map { it.toList() }
    }
    
    fun findNearbyPatterns(
        targetPattern: PatternMatch,
        allPatterns: List<PatternMatch>,
        radius: Double = 50.0
    ): List<PatternMatch> {
        val targetCenter = extractCenter(targetPattern) ?: return emptyList()
        
        return allPatterns
            .filter { it.id != targetPattern.id }
            .mapNotNull { pattern ->
                val center = extractCenter(pattern)
                if (center != null && distance(targetCenter, center) <= radius) {
                    pattern
                } else null
            }
    }
    
    private fun extractCenter(pattern: PatternMatch): Point? {
        val bounds = pattern.detectionBounds ?: return null
        val parts = bounds.split(",").mapNotNull { it.toDoubleOrNull() }
        if (parts.size != 4) return null
        
        val x = parts[0]
        val y = parts[1]
        val width = parts[2]
        val height = parts[3]
        
        return Point(x + width / 2, y + height / 2)
    }
    
    private fun distance(p1: Point, p2: Point): Double {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return sqrt(dx * dx + dy * dy)
    }
    
    fun calculateClusterCenter(patterns: List<PatternMatch>): Point? {
        val centers = patterns.mapNotNull { extractCenter(it) }
        if (centers.isEmpty()) return null
        
        val avgX = centers.map { it.x }.average()
        val avgY = centers.map { it.y }.average()
        
        return Point(avgX, avgY)
    }
}
