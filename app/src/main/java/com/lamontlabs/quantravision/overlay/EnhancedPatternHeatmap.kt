package com.lamontlabs.quantravision.overlay

import android.graphics.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import kotlin.math.sin

/**
 * EnhancedPatternHeatmap - Professional heatmap visualization with stunning visuals
 * 
 * Features:
 * - Multi-color gradient based on confidence
 * - Animated pulse effect
 * - Smooth fade transitions
 * - Layered glow with depth
 * - Pattern density visualization
 */
object EnhancedPatternHeatmap {
    private val detections = CopyOnWriteArrayList<HeatEntry>()
    private const val MAX_POINTS = 500
    private const val TTL_MS = 3_600_000L
    private var animationPhase = 0f

    fun add(x: Float, y: Float, conf: Float, patternType: String = "default") {
        if (detections.size > MAX_POINTS) detections.removeFirst()
        detections.add(HeatEntry(System.currentTimeMillis(), x, y, conf, patternType))
        purgeOld()
    }

    fun updateAnimationPhase(deltaTime: Float) {
        animationPhase = (animationPhase + deltaTime * 0.5f) % (2f * Math.PI.toFloat())
    }

    fun draw(canvas: Canvas, w: Int, h: Int) {
        purgeOld()
        if (detections.isEmpty()) return
        
        updateAnimationPhase(0.016f)
        
        detections.forEach { entry ->
            drawEnhancedHeatPoint(canvas, entry, w, h)
        }
    }

    private fun drawEnhancedHeatPoint(canvas: Canvas, entry: HeatEntry, w: Int, h: Int) {
        val ageFactor = 1f - ((System.currentTimeMillis() - entry.ts).toFloat() / TTL_MS)
        val basealpha = max(0f, ageFactor) * entry.conf
        
        val pulseEffect = if (entry.conf > 0.75f) {
            1f + 0.15f * sin(animationPhase + entry.x * 10f)
        } else {
            1f
        }
        
        val colors = getColorScheme(entry.patternType, entry.conf)
        
        val centerX = entry.x * w
        val centerY = entry.y * h
        val baseRadius = (25f + 70f * entry.conf) * pulseEffect
        
        drawGlowLayers(canvas, centerX, centerY, baseRadius, basealpha, colors)
        
        drawCoreGradient(canvas, centerX, centerY, baseRadius * 0.6f, basealpha, colors)
        
        if (entry.conf > 0.85f) {
            drawHighConfidenceRing(canvas, centerX, centerY, baseRadius, basealpha, colors)
        }
    }

    private fun drawGlowLayers(canvas: Canvas, x: Float, y: Float, radius: Float, alpha: Float, colors: IntArray) {
        val outerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            val outerAlpha = (alpha * 0.3f * 255).toInt()
            shader = RadialGradient(
                x, y,
                radius * 1.5f,
                intArrayOf(
                    Color.argb(outerAlpha, Color.red(colors[0]), Color.green(colors[0]), Color.blue(colors[0])),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(x, y, radius * 1.5f, outerGlowPaint)
        
        val middleGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            val middleAlpha = (alpha * 0.5f * 255).toInt()
            shader = RadialGradient(
                x, y,
                radius * 1.2f,
                intArrayOf(
                    Color.argb(middleAlpha, Color.red(colors[1]), Color.green(colors[1]), Color.blue(colors[1])),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(x, y, radius * 1.2f, middleGlowPaint)
    }

    private fun drawCoreGradient(canvas: Canvas, x: Float, y: Float, radius: Float, alpha: Float, colors: IntArray) {
        val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            val coreAlpha = (alpha * 0.8f * 255).toInt()
            shader = RadialGradient(
                x, y,
                radius,
                intArrayOf(
                    Color.argb(coreAlpha, Color.red(colors[2]), Color.green(colors[2]), Color.blue(colors[2])),
                    Color.argb((coreAlpha * 0.3f).toInt(), Color.red(colors[1]), Color.green(colors[1]), Color.blue(colors[1])),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.6f, 1f),
                Shader.TileMode.CLAMP
            )
            isDither = true
        }
        canvas.drawCircle(x, y, radius, corePaint)
    }

    private fun drawHighConfidenceRing(canvas: Canvas, x: Float, y: Float, radius: Float, alpha: Float, colors: IntArray) {
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            val ringAlpha = (alpha * 255).toInt()
            color = Color.argb(ringAlpha, Color.red(colors[2]), Color.green(colors[2]), Color.blue(colors[2]))
            maskFilter = BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawCircle(x, y, radius * 0.9f, ringPaint)
    }

    private fun getColorScheme(patternType: String, confidence: Float): IntArray {
        return when {
            patternType.contains("reversal", ignoreCase = true) -> {
                intArrayOf(
                    Color.rgb(255, 64, 129),
                    Color.rgb(255, 128, 171),
                    Color.rgb(255, 193, 213)
                )
            }
            patternType.contains("continuation", ignoreCase = true) -> {
                intArrayOf(
                    Color.rgb(0, 229, 255),
                    Color.rgb(128, 242, 255),
                    Color.rgb(200, 250, 255)
                )
            }
            patternType.contains("harmonic", ignoreCase = true) -> {
                intArrayOf(
                    Color.rgb(156, 39, 176),
                    Color.rgb(186, 104, 200),
                    Color.rgb(225, 190, 231)
                )
            }
            confidence > 0.85f -> {
                intArrayOf(
                    Color.rgb(76, 175, 80),
                    Color.rgb(129, 199, 132),
                    Color.rgb(200, 230, 201)
                )
            }
            else -> {
                intArrayOf(
                    Color.rgb(0, 229, 255),
                    Color.rgb(128, 242, 255),
                    Color.rgb(200, 250, 255)
                )
            }
        }
    }

    private fun purgeOld() {
        val now = System.currentTimeMillis()
        detections.removeIf { now - it.ts > TTL_MS }
    }

    fun clear() {
        detections.clear()
    }

    fun getDetectionCount(): Int = detections.size

    private data class HeatEntry(
        val ts: Long,
        val x: Float,
        val y: Float,
        val conf: Float,
        val patternType: String
    )
}
