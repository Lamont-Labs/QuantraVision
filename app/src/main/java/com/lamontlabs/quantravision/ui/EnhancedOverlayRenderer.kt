package com.lamontlabs.quantravision.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.boundingBox
import com.lamontlabs.quantravision.detection.HighlightGate
import kotlin.math.min

/**
 * EnhancedOverlayRenderer - Professional, visually stunning pattern highlighting
 * 
 * Features:
 * - Multi-layer rendering (shadow, glow, border, fill, accents)
 * - Smooth fade-in animations
 * - Confidence-based visual intensity
 * - Pattern-specific color schemes
 * - Corner accent markers
 * - Professional typography with shadows
 * - Glass morphism effect
 * - Animated pulse for high-confidence patterns
 */
class EnhancedOverlayRenderer(
    private val appContext: Context,
    matches: List<PatternMatch>
) : View(appContext) {

    private val gated = HighlightGate.filterForRender(appContext, matches)
    var quotaExceeded: Boolean = gated.isEmpty() && matches.isNotEmpty()
    
    private var animationProgress = 0f
    private var pulsePhase = 0f
    
    init {
        startFadeInAnimation()
    }

    private fun startFadeInAnimation() {
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 600
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                animationProgress = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
        
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener { animator ->
                pulsePhase = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        gated.forEach { match ->
            if (!HighlightGate.allowAndCount(appContext)) {
                quotaExceeded = true
                return@forEach
            }
            
            drawEnhancedPattern(canvas, match)
        }

        drawWatermark(canvas)

        if (quotaExceeded) {
            drawQuotaOverlay(canvas)
        }
    }

    private fun drawEnhancedPattern(canvas: Canvas, match: PatternMatch) {
        val rect = match.boundingBox
        val confidence = match.confidence.toFloat()
        val style = PatternStyle.forPattern(match.patternName, confidence)
        
        val alpha = (255 * animationProgress).toInt()
        val shouldPulse = confidence > 0.85f
        val pulseIntensity = if (shouldPulse) {
            0.8f + 0.2f * kotlin.math.sin(pulsePhase * 2 * Math.PI.toFloat())
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
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb((alpha * 0.3f).toInt(), 0, 0, 0)
            maskFilter = BlurMaskFilter(16f, BlurMaskFilter.Blur.NORMAL)
        }
        
        val shadowRect = RectF(
            rect.left + 6f,
            rect.top + 6f,
            rect.right + 6f,
            rect.bottom + 6f
        )
        
        canvas.drawRoundRect(shadowRect, style.cornerRadius, style.cornerRadius, shadowPaint)
    }

    private fun drawGlowLayer(canvas: Canvas, rect: RectF, style: PatternStyle, alpha: Int, pulseIntensity: Float) {
        val glowIntensity = (style.glowIntensity * pulseIntensity).coerceIn(0f, 1f)
        val glowAlpha = (alpha * 0.6f * glowIntensity).toInt()
        
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
            strokeWidth = 8f
            color = Color.argb(glowAlpha, style.r, style.g, style.b)
            maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.OUTER)
        }
        
        canvas.drawRoundRect(rect, style.cornerRadius, style.cornerRadius, glowPaint)
    }

    private fun drawBorderLayer(canvas: Canvas, rect: RectF, style: PatternStyle, alpha: Int) {
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
            isDither = true
            
            shader = LinearGradient(
                rect.left, rect.top,
                rect.right, rect.bottom,
                intArrayOf(
                    Color.argb(alpha, style.r, style.g, style.b),
                    Color.argb((alpha * 0.7f).toInt(), style.r, style.g, style.b),
                    Color.argb(alpha, style.r, style.g, style.b)
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        
        canvas.drawRoundRect(rect, style.cornerRadius, style.cornerRadius, borderPaint)
    }

    private fun drawFillLayer(canvas: Canvas, rect: RectF, style: PatternStyle, alpha: Int) {
        val fillAlpha = (alpha * 0.12f).toInt()
        
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(fillAlpha, style.r, style.g, style.b)
            style = Paint.Style.FILL
        }
        
        canvas.drawRoundRect(rect, style.cornerRadius, style.cornerRadius, fillPaint)
    }

    private fun drawCornerAccents(canvas: Canvas, rect: RectF, style: PatternStyle, alpha: Int) {
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(alpha, style.r, style.g, style.b)
            style = Paint.Style.STROKE
            strokeWidth = 3f
            strokeCap = Paint.Cap.ROUND
        }
        
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
        val labelBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb((alpha * 0.9f).toInt(), 18, 18, 24)
            style = Paint.Style.FILL
            setShadowLayer(4f, 0f, 2f, Color.argb(100, 0, 0, 0))
        }
        
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(alpha, 255, 255, 255)
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(2f, 0f, 1f, Color.argb(150, 0, 0, 0))
        }
        
        val labelText = match.patternName
        val textWidth = textPaint.measureText(labelText)
        val labelPadding = 12f
        val labelHeight = 32f
        
        val labelRect = RectF(
            rect.left,
            rect.top - labelHeight - 8f,
            rect.left + textWidth + labelPadding * 2,
            rect.top - 8f
        )
        
        canvas.drawRoundRect(labelRect, 6f, 6f, labelBgPaint)
        
        val accentBar = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(alpha, style.r, style.g, style.b)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(labelRect.left, labelRect.top, labelRect.left + 4f, labelRect.bottom),
            2f, 2f, accentBar
        )
        
        canvas.drawText(
            labelText,
            labelRect.left + labelPadding + 4f,
            labelRect.centerY() + 6f,
            textPaint
        )
    }

    private fun drawConfidenceBadge(canvas: Canvas, rect: RectF, confidence: Float, style: PatternStyle, alpha: Int) {
        val badgeRadius = 28f
        val badgeX = rect.right + badgeRadius + 8f
        val badgeY = rect.top + badgeRadius
        
        val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb((alpha * 0.9f).toInt(), 18, 18, 24)
            style = Paint.Style.FILL
            setShadowLayer(4f, 0f, 2f, Color.argb(100, 0, 0, 0))
        }
        canvas.drawCircle(badgeX, badgeY, badgeRadius, outerPaint)
        
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.argb(alpha, style.r, style.g, style.b)
        }
        canvas.drawCircle(badgeX, badgeY, badgeRadius - 4f, ringPaint)
        
        val percentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(alpha, 255, 255, 255)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        
        val percentText = "${(confidence * 100).toInt()}%"
        canvas.drawText(percentText, badgeX, badgeY + 5f, percentPaint)
    }

    private fun drawWatermark(canvas: Canvas) {
        val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(200, 255, 255, 255)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            setShadowLayer(3f, 0f, 1f, Color.argb(180, 0, 0, 0))
        }
        
        val msg = "⚠ Illustrative Only — Not Financial Advice"
        canvas.drawText(msg, 24f, height - 32f, watermarkPaint)
    }

    private fun drawQuotaOverlay(canvas: Canvas) {
        canvas.drawColor(Color.argb(140, 0, 0, 0))
        
        val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(255, 255, 255, 255)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(4f, 0f, 2f, Color.argb(200, 0, 0, 0))
        }
        
        canvas.drawText(
            "Free highlights used. Upgrade to continue.",
            width / 2f,
            height / 2f,
            overlayPaint
        )
    }
}

data class PatternStyle(
    val r: Int,
    val g: Int,
    val b: Int,
    val glowIntensity: Float,
    val cornerRadius: Float
) {
    companion object {
        fun forPattern(patternName: String, confidence: Float): PatternStyle {
            val intensity = 0.4f + 0.6f * confidence
            
            return when {
                patternName.contains("head", ignoreCase = true) ||
                patternName.contains("shoulder", ignoreCase = true) -> {
                    PatternStyle(255, 64, 129, intensity, 8f)
                }
                
                patternName.contains("triangle", ignoreCase = true) ||
                patternName.contains("wedge", ignoreCase = true) -> {
                    PatternStyle(0, 229, 255, intensity, 8f)
                }
                
                patternName.contains("double", ignoreCase = true) ||
                patternName.contains("triple", ignoreCase = true) -> {
                    PatternStyle(156, 39, 176, intensity, 8f)
                }
                
                patternName.contains("flag", ignoreCase = true) ||
                patternName.contains("pennant", ignoreCase = true) -> {
                    PatternStyle(255, 193, 7, intensity, 8f)
                }
                
                patternName.contains("cup", ignoreCase = true) ||
                patternName.contains("handle", ignoreCase = true) -> {
                    PatternStyle(76, 175, 80, intensity, 8f)
                }
                
                else -> {
                    PatternStyle(0, 229, 255, intensity, 8f)
                }
            }
        }
    }
}
