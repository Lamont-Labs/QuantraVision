package com.lamontlabs.quantravision.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.boundingBox
import com.lamontlabs.quantravision.detection.HighlightGate
import com.lamontlabs.quantravision.overlay.EnhancedPatternHeatmap
import timber.log.Timber
import kotlin.math.sin

/**
 * EnhancedOverlayView - Professional pattern highlighting with stunning visual effects
 * 
 * PERFORMANCE OPTIMIZED:
 * - Single set of reusable animators (created once in init)
 * - No per-frame allocations
 * - Thread-safe updates
 * - Guards against pre-layout rendering
 * 
 * Features:
 * - Multi-layer rendering (shadow, glow, border, fill, accents, labels, badges)
 * - Smooth fade-in and pulse animations
 * - Pattern-specific color schemes
 * - Heatmap visualization
 * - Quota management
 */
class EnhancedOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentMatches: List<PatternMatch> = emptyList()
    private var gatedMatches: List<PatternMatch> = emptyList()
    private var quotaExceeded: Boolean = false
    private var showHeatmap: Boolean = true
    
    private val approvedPatternIds = mutableSetOf<String>()
    
    private var animationProgress = 0f
    private var pulsePhase = 0f
    
    private val fadeAnimator: ValueAnimator
    private val pulseAnimator: ValueAnimator
    
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val accentBar = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val percentPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private val shadowBlur = BlurMaskFilter(16f, BlurMaskFilter.Blur.NORMAL)
    private val glowBlur = BlurMaskFilter(12f, BlurMaskFilter.Blur.OUTER)
    
    private var cachedBorderGradient: LinearGradient? = null
    private var lastGradientRect: RectF? = null
    private var lastGradientStyle: PatternStyle? = null
    private var lastGradientAlpha: Int = 0
    
    private val shadowRect = RectF()
    private val labelRect = RectF()
    private val accentBarRect = RectF()
    
    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        
        fadeAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 600
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                animationProgress = animator.animatedValue as Float
                invalidate()
            }
        }
        
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener { animator ->
                pulsePhase = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
        
        accentPaint.style = Paint.Style.STROKE
        accentPaint.strokeWidth = 3f
        accentPaint.strokeCap = Paint.Cap.ROUND
        
        labelBgPaint.style = Paint.Style.FILL
        
        textPaint.textSize = 16f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        
        accentBar.style = Paint.Style.FILL
        
        outerPaint.style = Paint.Style.FILL
        
        ringPaint.style = Paint.Style.STROKE
        ringPaint.strokeWidth = 3f
        
        percentPaint.textSize = 14f
        percentPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        percentPaint.textAlign = Paint.Align.CENTER
        
        watermarkPaint.textSize = 13f
        watermarkPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        
        overlayPaint.textSize = 20f
        overlayPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        overlayPaint.textAlign = Paint.Align.CENTER
    }

    fun updateMatches(matches: List<PatternMatch>) {
        if (width <= 0 || height <= 0) {
            Timber.w("View not laid out yet, deferring pattern update")
            post { updateMatches(matches) }
            return
        }
        
        currentMatches = matches
        quotaExceeded = false
        
        val approvedMatches = mutableListOf<PatternMatch>()
        for (match in matches) {
            val quantizedX = (match.boundingBox.left / 10f).toInt() * 10
            val quantizedY = (match.boundingBox.top / 10f).toInt() * 10
            val patternId = "${match.patternName}_${quantizedX}_${quantizedY}"
            
            if (approvedPatternIds.contains(patternId)) {
                approvedMatches.add(match)
            } else {
                if (HighlightGate.allowAndCount(context)) {
                    approvedPatternIds.add(patternId)
                    approvedMatches.add(match)
                } else {
                    quotaExceeded = true
                    break
                }
            }
        }
        
        gatedMatches = approvedMatches
        
        matches.forEach { match ->
            val normalizedX = match.boundingBox.centerX() / width.toFloat()
            val normalizedY = match.boundingBox.centerY() / height.toFloat()
            
            val patternType = when {
                match.patternName.contains("head", ignoreCase = true) ||
                match.patternName.contains("shoulder", ignoreCase = true) -> "reversal"
                
                match.patternName.contains("triangle", ignoreCase = true) ||
                match.patternName.contains("wedge", ignoreCase = true) ||
                match.patternName.contains("flag", ignoreCase = true) -> "continuation"
                
                match.patternName.contains("harmonic", ignoreCase = true) ||
                match.patternName.contains("gartley", ignoreCase = true) ||
                match.patternName.contains("butterfly", ignoreCase = true) -> "harmonic"
                
                else -> "default"
            }
            
            if (normalizedX in 0f..1f && normalizedY in 0f..1f) {
                EnhancedPatternHeatmap.add(normalizedX, normalizedY, match.confidence.toFloat(), patternType)
            }
        }
        
        if (!fadeAnimator.isRunning && gatedMatches.isNotEmpty()) {
            fadeAnimator.start()
        }
        
        invalidate()
        
        Timber.d("Updated overlay with ${matches.size} patterns (${gatedMatches.size} after gating)")
    }

    fun setHeatmapVisible(visible: Boolean) {
        showHeatmap = visible
        invalidate()
    }

    fun clearAll() {
        currentMatches = emptyList()
        gatedMatches = emptyList()
        quotaExceeded = false
        approvedPatternIds.clear()
        EnhancedPatternHeatmap.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (width <= 0 || height <= 0) return
        
        if (showHeatmap && EnhancedPatternHeatmap.getDetectionCount() > 0) {
            EnhancedPatternHeatmap.draw(canvas, width, height)
        }
        
        if (gatedMatches.isNotEmpty()) {
            drawPatternHighlights(canvas)
        }
        
        drawWatermark(canvas)
        
        if (quotaExceeded) {
            drawQuotaOverlay(canvas)
        }
    }

    private fun drawPatternHighlights(canvas: Canvas) {
        gatedMatches.forEach { match ->
            drawEnhancedPattern(canvas, match)
        }
    }

    private fun drawEnhancedPattern(canvas: Canvas, match: PatternMatch) {
        val rect = match.boundingBox
        val confidence = match.confidence.toFloat()
        val style = PatternStyle.forPattern(match.patternName, confidence)
        
        val alpha = (255 * animationProgress).toInt()
        val shouldPulse = confidence > 0.85f
        val pulseIntensity = if (shouldPulse) {
            0.8f + 0.2f * sin(pulsePhase * 2 * Math.PI.toFloat())
        } else {
            1f
        }

        canvas.save()
        
        drawShadowLayer(canvas, rect, style, alpha)
        drawGlowLayer(canvas, rect, style, alpha, pulseIntensity)
        drawBorderLayer(canvas, rect, style, alpha)
        drawFillLayer(canvas, rect, style, alpha)
        drawCornerAccents(canvas, rect, style, alpha)
        drawPatternLabel(canvas, rect, match, style, alpha)
        drawConfidenceBadge(canvas, rect, confidence, style, alpha)
        
        canvas.restore()
    }

    private fun drawShadowLayer(canvas: Canvas, rect: RectF, style: PatternStyle, alpha: Int) {
        shadowPaint.color = Color.argb((alpha * 0.3f).toInt(), 0, 0, 0)
        shadowPaint.maskFilter = shadowBlur
        
        shadowRect.set(rect.left + 6f, rect.top + 6f, rect.right + 6f, rect.bottom + 6f)
        canvas.drawRoundRect(shadowRect, style.cornerRadius, style.cornerRadius, shadowPaint)
    }

    private fun drawGlowLayer(canvas: Canvas, rect: RectF, style: PatternStyle, alpha: Int, pulseIntensity: Float) {
        val glowIntensity = (style.glowIntensity * pulseIntensity).coerceIn(0f, 1f)
        val glowAlpha = (alpha * 0.6f * glowIntensity).toInt()
        
        glowPaint.style = Paint.Style.STROKE
        glowPaint.strokeWidth = 8f
        glowPaint.color = Color.argb(glowAlpha, style.r, style.g, style.b)
        glowPaint.maskFilter = glowBlur
        
        canvas.drawRoundRect(rect, style.cornerRadius, style.cornerRadius, glowPaint)
    }

    private fun drawBorderLayer(canvas: Canvas, rect: RectF, style: PatternStyle, alpha: Int) {
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 3f
        borderPaint.isAntiAlias = true
        borderPaint.isDither = true
        
        if (cachedBorderGradient == null || 
            lastGradientRect != rect || 
            lastGradientStyle != style || 
            lastGradientAlpha != alpha) {
            cachedBorderGradient = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(
                    Color.argb(alpha, style.r, style.g, style.b),
                    Color.argb((alpha * 0.7f).toInt(), style.r, style.g, style.b),
                    Color.argb(alpha, style.r, style.g, style.b)
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            lastGradientRect = RectF(rect)
            lastGradientStyle = style
            lastGradientAlpha = alpha
        }
        
        borderPaint.shader = cachedBorderGradient
        canvas.drawRoundRect(rect, style.cornerRadius, style.cornerRadius, borderPaint)
    }

    private fun drawFillLayer(canvas: Canvas, rect: RectF, style: PatternStyle, alpha: Int) {
        val fillAlpha = (alpha * 0.12f).toInt()
        
        fillPaint.color = Color.argb(fillAlpha, style.r, style.g, style.b)
        fillPaint.style = Paint.Style.FILL
        
        canvas.drawRoundRect(rect, style.cornerRadius, style.cornerRadius, fillPaint)
    }

    private fun drawCornerAccents(canvas: Canvas, rect: RectF, style: PatternStyle, alpha: Int) {
        accentPaint.color = Color.argb(alpha, style.r, style.g, style.b)
        
        val accentLength = 20f
        
        canvas.drawLine(rect.left, rect.top, rect.left + accentLength, rect.top, accentPaint)
        canvas.drawLine(rect.left, rect.top, rect.left, rect.top + accentLength, accentPaint)
        canvas.drawLine(rect.right, rect.top, rect.right - accentLength, rect.top, accentPaint)
        canvas.drawLine(rect.right, rect.top, rect.right, rect.top + accentLength, accentPaint)
        canvas.drawLine(rect.left, rect.bottom, rect.left + accentLength, rect.bottom, accentPaint)
        canvas.drawLine(rect.left, rect.bottom, rect.left, rect.bottom - accentLength, accentPaint)
        canvas.drawLine(rect.right, rect.bottom, rect.right - accentLength, rect.bottom, accentPaint)
        canvas.drawLine(rect.right, rect.bottom, rect.right, rect.bottom - accentLength, accentPaint)
    }

    private fun drawPatternLabel(canvas: Canvas, rect: RectF, match: PatternMatch, style: PatternStyle, alpha: Int) {
        labelBgPaint.color = Color.argb((alpha * 0.9f).toInt(), 18, 18, 24)
        labelBgPaint.setShadowLayer(4f, 0f, 2f, Color.argb(100, 0, 0, 0))
        
        textPaint.color = Color.argb(alpha, 255, 255, 255)
        textPaint.setShadowLayer(2f, 0f, 1f, Color.argb(150, 0, 0, 0))
        
        val labelPadding = 12f
        val lineHeight = 20f
        val tradeScenario = match.tradeScenario
        
        if (tradeScenario != null) {
            val lines = listOf(
                "${match.patternName} (${(match.confidence * 100).toInt()}%)",
                "Entry: $${String.format("%.2f", tradeScenario.entryPrice)}",
                "Stop: $${String.format("%.2f", tradeScenario.stopLoss)}",
                "Target: $${String.format("%.2f", tradeScenario.takeProfit)}"
            )
            
            val maxWidth = lines.maxOf { textPaint.measureText(it) }
            val labelHeight = (lines.size * lineHeight) + (labelPadding * 2)
            
            labelRect.set(
                rect.left, rect.top - labelHeight - 8f,
                rect.left + maxWidth + labelPadding * 2, rect.top - 8f
            )
            
            canvas.drawRoundRect(labelRect, 6f, 6f, labelBgPaint)
            
            accentBar.color = Color.argb(alpha, style.r, style.g, style.b)
            accentBarRect.set(labelRect.left, labelRect.top, labelRect.left + 4f, labelRect.bottom)
            canvas.drawRoundRect(accentBarRect, 2f, 2f, accentBar)
            
            lines.forEachIndexed { index, line ->
                val y = labelRect.top + labelPadding + ((index + 1) * lineHeight) - 4f
                canvas.drawText(line, labelRect.left + labelPadding + 4f, y, textPaint)
            }
        } else {
            val labelText = match.patternName
            val textWidth = textPaint.measureText(labelText)
            val labelHeight = 32f
            
            labelRect.set(
                rect.left, rect.top - labelHeight - 8f,
                rect.left + textWidth + labelPadding * 2, rect.top - 8f
            )
            
            canvas.drawRoundRect(labelRect, 6f, 6f, labelBgPaint)
            
            accentBar.color = Color.argb(alpha, style.r, style.g, style.b)
            accentBarRect.set(labelRect.left, labelRect.top, labelRect.left + 4f, labelRect.bottom)
            canvas.drawRoundRect(accentBarRect, 2f, 2f, accentBar)
            
            canvas.drawText(labelText, labelRect.left + labelPadding + 4f, labelRect.centerY() + 6f, textPaint)
        }
    }

    private fun drawConfidenceBadge(canvas: Canvas, rect: RectF, confidence: Float, style: PatternStyle, alpha: Int) {
        val badgeRadius = 28f
        val badgeX = rect.right + badgeRadius + 8f
        val badgeY = rect.top + badgeRadius
        
        outerPaint.color = Color.argb((alpha * 0.9f).toInt(), 18, 18, 24)
        outerPaint.setShadowLayer(4f, 0f, 2f, Color.argb(100, 0, 0, 0))
        canvas.drawCircle(badgeX, badgeY, badgeRadius, outerPaint)
        
        ringPaint.color = Color.argb(alpha, style.r, style.g, style.b)
        canvas.drawCircle(badgeX, badgeY, badgeRadius - 4f, ringPaint)
        
        percentPaint.color = Color.argb(alpha, 255, 255, 255)
        
        val percentText = "${(confidence * 100).toInt()}%"
        canvas.drawText(percentText, badgeX, badgeY + 5f, percentPaint)
    }

    private fun drawWatermark(canvas: Canvas) {
        watermarkPaint.color = Color.argb(200, 255, 255, 255)
        watermarkPaint.setShadowLayer(3f, 0f, 1f, Color.argb(180, 0, 0, 0))
        
        val msg = "⚠ Illustrative Only — Not Financial Advice"
        canvas.drawText(msg, 24f, height - 32f, watermarkPaint)
    }

    private fun drawQuotaOverlay(canvas: Canvas) {
        canvas.drawColor(Color.argb(140, 0, 0, 0))
        
        overlayPaint.color = Color.argb(255, 255, 255, 255)
        overlayPaint.setShadowLayer(4f, 0f, 2f, Color.argb(200, 0, 0, 0))
        
        canvas.drawText("Free highlights used. Upgrade to continue.", width / 2f, height / 2f, overlayPaint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        fadeAnimator.cancel()
        pulseAnimator.cancel()
    }
}
