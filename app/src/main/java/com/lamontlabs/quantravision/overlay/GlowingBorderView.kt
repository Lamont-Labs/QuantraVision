package com.lamontlabs.quantravision.overlay

import android.content.Context
import android.graphics.*
import android.view.View

/**
 * A minimal, beautiful glowing border overlay that draws around screen edges.
 * Features:
 * - Faint cyan (#00E5FF) glow with very low opacity (23-39%)
 * - Two-layer effect: outer blur + inner sharp line for depth
 * - Touch-passthrough (FLAG_NOT_TOUCHABLE)
 * - Optional pulsing animation when patterns detected
 */
class GlowingBorderView(context: Context) : View(context) {
    
    private val borderPaint = Paint().apply {
        color = Color.parseColor("#00E5FF")  // Brand cyan color
        style = Paint.Style.STROKE
        strokeWidth = 8f  // Thin border
        alpha = 60  // Very subtle - 23% opacity
        isAntiAlias = true
        // Blur effect for soft glow
        maskFilter = BlurMaskFilter(16f, BlurMaskFilter.Blur.OUTER)
    }
    
    private val innerBorderPaint = Paint().apply {
        color = Color.parseColor("#00E5FF")
        style = Paint.Style.STROKE
        strokeWidth = 2f  // Even thinner inner line
        alpha = 100  // Slightly more visible - 39% opacity
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
        
        // Draw outer glowing border
        canvas.drawRect(
            margin, 
            margin, 
            width - margin, 
            height - margin, 
            borderPaint
        )
        
        // Draw inner sharp border for definition
        canvas.drawRect(
            margin + 6f, 
            margin + 6f, 
            width - margin - 6f, 
            height - margin - 6f, 
            innerBorderPaint
        )
    }
    
    /**
     * Optional: Add pulsing animation when patterns detected
     * @param isPulsing true to show high-intensity glow, false for normal subtle glow
     */
    fun setPulsing(isPulsing: Boolean) {
        if (isPulsing) {
            // Animate alpha to be more visible when patterns detected
            borderPaint.alpha = 120
            innerBorderPaint.alpha = 150
        } else {
            // Return to subtle default state
            borderPaint.alpha = 60
            innerBorderPaint.alpha = 100
        }
        invalidate()
    }
}
