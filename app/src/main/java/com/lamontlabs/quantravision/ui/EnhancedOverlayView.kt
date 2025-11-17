package com.lamontlabs.quantravision.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.boundingBox
import timber.log.Timber

/**
 * EnhancedOverlayView - Simplified pattern highlighting with NO ANIMATIONS
 * 
 * Displays detected patterns with clean, professional highlights:
 * - Border with pattern-specific colors
 * - Semi-transparent fill
 * - Pattern label with confidence
 * - NO pulsing, fading, or any animations
 */
class EnhancedOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentMatches: List<PatternMatch> = emptyList()
    
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val accentBar = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private val labelRect = RectF()
    private val accentBarRect = RectF()
    
    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        
        // Explicitly disable touch/click handling to ensure pass-through
        isClickable = false
        isFocusable = false
        isFocusableInTouchMode = false
        
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 4f
        
        fillPaint.style = Paint.Style.FILL
        
        labelBgPaint.style = Paint.Style.FILL
        
        textPaint.textSize = 16f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        
        accentBar.style = Paint.Style.FILL
    }

    fun updateMatches(matches: List<PatternMatch>) {
        if (width <= 0 || height <= 0) {
            Timber.w("View not laid out yet, deferring pattern update")
            post { updateMatches(matches) }
            return
        }
        
        currentMatches = matches
        invalidate()
        
        Timber.d("Updated overlay with ${matches.size} patterns")
    }

    fun clearAll() {
        currentMatches = emptyList()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (width <= 0 || height <= 0) return
        
        if (currentMatches.isNotEmpty()) {
            currentMatches.forEach { match ->
                drawPattern(canvas, match)
            }
        }
    }

    private fun drawPattern(canvas: Canvas, match: PatternMatch) {
        val rect = match.boundingBox
        val confidence = match.confidence.toFloat()
        val style = PatternStyle.forPattern(match.patternName, confidence)
        
        canvas.save()
        
        drawBorderLayer(canvas, rect, style)
        drawFillLayer(canvas, rect, style)
        drawPatternLabel(canvas, rect, match, style)
        
        canvas.restore()
    }

    private fun drawBorderLayer(canvas: Canvas, rect: RectF, style: PatternStyle) {
        borderPaint.color = Color.argb(255, style.r, style.g, style.b)
        borderPaint.shader = null
        canvas.drawRoundRect(rect, style.cornerRadius, style.cornerRadius, borderPaint)
    }

    private fun drawFillLayer(canvas: Canvas, rect: RectF, style: PatternStyle) {
        fillPaint.color = Color.argb(30, style.r, style.g, style.b)
        canvas.drawRoundRect(rect, style.cornerRadius, style.cornerRadius, fillPaint)
    }

    private fun drawPatternLabel(canvas: Canvas, rect: RectF, match: PatternMatch, style: PatternStyle) {
        labelBgPaint.color = Color.argb(230, 18, 18, 24)
        labelBgPaint.setShadowLayer(4f, 0f, 2f, Color.argb(100, 0, 0, 0))
        
        textPaint.color = Color.argb(255, 255, 255, 255)
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
            
            accentBar.color = Color.argb(255, style.r, style.g, style.b)
            accentBarRect.set(labelRect.left, labelRect.top, labelRect.left + 4f, labelRect.bottom)
            canvas.drawRoundRect(accentBarRect, 2f, 2f, accentBar)
            
            lines.forEachIndexed { index, line ->
                val y = labelRect.top + labelPadding + ((index + 1) * lineHeight) - 4f
                canvas.drawText(line, labelRect.left + labelPadding + 4f, y, textPaint)
            }
        } else {
            val labelText = "${match.patternName} (${(match.confidence * 100).toInt()}%)"
            val textWidth = textPaint.measureText(labelText)
            val labelHeight = 32f
            
            labelRect.set(
                rect.left, rect.top - labelHeight - 8f,
                rect.left + textWidth + labelPadding * 2, rect.top - 8f
            )
            
            canvas.drawRoundRect(labelRect, 6f, 6f, labelBgPaint)
            
            accentBar.color = Color.argb(255, style.r, style.g, style.b)
            accentBarRect.set(labelRect.left, labelRect.top, labelRect.left + 4f, labelRect.bottom)
            canvas.drawRoundRect(accentBarRect, 2f, 2f, accentBar)
            
            canvas.drawText(labelText, labelRect.left + labelPadding + 4f, labelRect.centerY() + 6f, textPaint)
        }
    }
}
