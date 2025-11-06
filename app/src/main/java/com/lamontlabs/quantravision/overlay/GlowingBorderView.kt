package com.lamontlabs.quantravision.overlay

import android.content.Context
import android.graphics.*
import android.view.View

/**
 * QuantraCore holographic glowing border overlay.
 * Features:
 * - Vibrant cyan (#00E5FF) glow with enhanced bloom effect
 * - Multi-layer effect: outer bloom + mid glow + inner sharp line
 * - Touch-passthrough (FLAG_NOT_TOUCHABLE)
 * - Pulsing animation when patterns detected
 */
class GlowingBorderView(context: Context) : View(context) {
    
    private val outerGlowPaint = Paint().apply {
        color = Color.parseColor("#00E5FF")  // QuantraCore cyan
        style = Paint.Style.STROKE
        strokeWidth = 12f  // Wide bloom effect
        alpha = 100  // 39% opacity for soft outer glow
        isAntiAlias = true
        // Enhanced blur for holographic bloom
        maskFilter = BlurMaskFilter(24f, BlurMaskFilter.Blur.OUTER)
    }
    
    private val borderPaint = Paint().apply {
        color = Color.parseColor("#00F0FF")  // Brighter cyan for mid layer
        style = Paint.Style.STROKE
        strokeWidth = 6f  // Medium border
        alpha = 140  // 55% opacity - more visible
        isAntiAlias = true
        // Mid-range glow
        maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.OUTER)
    }
    
    private val innerBorderPaint = Paint().apply {
        color = Color.parseColor("#00FFFF")  // Brightest cyan for inner sharp line
        style = Paint.Style.STROKE
        strokeWidth = 2f  // Thin sharp line
        alpha = 180  // 70% opacity for definition
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
     * Pulse animation for pattern detection - QuantraCore intensity boost
     * @param isPulsing true for high-intensity holographic glow, false for normal state
     */
    fun setPulsing(isPulsing: Boolean) {
        if (isPulsing) {
            // High-intensity QuantraCore glow when patterns detected
            outerGlowPaint.alpha = 160  // 63% - brighter bloom
            borderPaint.alpha = 200     // 78% - strong mid layer
            innerBorderPaint.alpha = 220 // 86% - very visible sharp line
        } else {
            // Normal QuantraCore ambient glow
            outerGlowPaint.alpha = 100  // 39% - soft bloom
            borderPaint.alpha = 140     // 55% - balanced mid layer
            innerBorderPaint.alpha = 180 // 70% - clear definition
        }
        invalidate()
    }
}
