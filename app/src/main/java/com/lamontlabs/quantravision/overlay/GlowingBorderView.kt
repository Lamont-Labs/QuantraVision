package com.lamontlabs.quantravision.overlay

import android.content.Context
import android.graphics.*
import android.view.View
import com.lamontlabs.quantravision.ui.QuantraColors

/**
 * QuantraCore HD SHARP glowing border overlay.
 * Features:
 * - CRISP cyan (#00F0FF) glow with REDUCED blur for sharpness
 * - 3-tier effect: soft outer bloom (≤10px) + SOLID mid layer + SHARP 1px white edge
 * - Touch-passthrough (FLAG_NOT_TOUCHABLE)
 * - High-contrast pulsing animation
 */
class GlowingBorderView(context: Context) : View(context) {
    
    private val outerGlowPaint = Paint().apply {
        color = QuantraColors.cyanInt
        style = Paint.Style.STROKE
        strokeWidth = 10f  // REDUCED for sharpness
        alpha = 120  // 47% opacity - more visible
        isAntiAlias = true
        // REDUCED blur for HD sharpness
        maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.OUTER)
    }
    
    private val borderPaint = Paint().apply {
        color = QuantraColors.cyanBrightInt
        style = Paint.Style.STROKE
        strokeWidth = 4f  // Solid border
        alpha = 200  // 78% opacity - SOLID and VISIBLE
        isAntiAlias = true
        // REDUCED blur for crisp mid-layer
        maskFilter = BlurMaskFilter(6f, BlurMaskFilter.Blur.OUTER)
    }
    
    private val innerBorderPaint = Paint().apply {
        color = QuantraColors.whiteInt  // Pure WHITE for max sharpness
        style = Paint.Style.STROKE
        strokeWidth = 1f  // SHARP 1px inner edge
        alpha = 255  // 100% opacity - CRISP definition
        isAntiAlias = true
    }
    
    init {
        // Disable hardware acceleration to allow blur effects
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val width = width.toFloat()
        val height = height.toFloat()
        val margin = 4f  // Small margin from screen edge
        
        // Layer 1: Outer bloom (holographic effect)
        canvas.drawRect(
            margin, 
            margin, 
            width - margin, 
            height - margin, 
            outerGlowPaint
        )
        
        // Layer 2: Mid glow border
        canvas.drawRect(
            margin + 4f, 
            margin + 4f, 
            width - margin - 4f, 
            height - margin - 4f, 
            borderPaint
        )
        
        // Layer 3: Inner sharp border (definition)
        canvas.drawRect(
            margin + 8f, 
            margin + 8f, 
            width - margin - 8f, 
            height - margin - 8f, 
            innerBorderPaint
        )
    }
    
    /**
     * Pulse animation for pattern detection - HD SHARP contrast boost
     * @param isPulsing true for HIGH-INTENSITY crisp glow, false for normal sharp state
     */
    fun setPulsing(isPulsing: Boolean) {
        if (isPulsing) {
            // HIGH-INTENSITY HD glow when patterns detected
            outerGlowPaint.alpha = 180  // 71% - strong bloom
            borderPaint.alpha = 240     // 94% - NEARLY SOLID mid layer
            innerBorderPaint.alpha = 255 // 100% - SHARP white edge
        } else {
            // Normal HD SHARP ambient state
            outerGlowPaint.alpha = 120  // 47% - visible bloom
            borderPaint.alpha = 200     // 78% - SOLID mid layer
            innerBorderPaint.alpha = 255 // 100% - ALWAYS CRISP white edge
        }
        invalidate()
    }
}
