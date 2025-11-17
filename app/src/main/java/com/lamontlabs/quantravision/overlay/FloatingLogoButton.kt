package com.lamontlabs.quantravision.overlay

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import com.lamontlabs.quantravision.MainActivity
import com.lamontlabs.quantravision.R
import kotlin.math.abs

/**
 * FloatingLogoButton - Simplified version with NO ANIMATIONS
 * 
 * Shows Q logo in bottom-right corner with:
 * - Visual feedback for detection status (static, no animations)
 * - Drag-and-drop repositioning
 * - Click and long-press callbacks
 */
class FloatingLogoButton(
    private val context: Context,
    private val windowManager: WindowManager
) {
    private val prefs = FloatingLogoPreferences(context)
    private val logoView: View
    private val logoImage: ImageView
    private val badge: LogoBadge
    private var params: WindowManager.LayoutParams
    private var isAdded = false
    
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private var dragStartTime = 0L
    
    private val longPressThreshold = 500L
    
    var onClickListener: (() -> Unit)? = null
    var onLongPressListener: (() -> Unit)? = null

    init {
        val inflater = LayoutInflater.from(context)
        logoView = inflater.inflate(R.layout.floating_logo_layout, null) as FrameLayout
        
        logoImage = logoView.findViewById(R.id.logo_image)
        badge = logoView.findViewById(R.id.logo_badge)
        
        val logoSize = prefs.getLogoSize()
        val sizePx = (logoSize.dp * context.resources.displayMetrics.density).toInt()
        
        val displayMetrics = context.resources.displayMetrics
        val defaultX = displayMetrics.widthPixels - sizePx - 20
        val defaultY = displayMetrics.heightPixels - sizePx - 100
        
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = prefs.getPositionX(defaultX)
            y = prefs.getPositionY(defaultY)
        }
        
        logoView.alpha = prefs.getLogoOpacity()
        
        setupTouchListener()
    }

    fun show() {
        android.util.Log.i("FloatingLogoButton", "show() called, isAdded=$isAdded")
        if (!isAdded) {
            try {
                android.util.Log.i("FloatingLogoButton", "Adding logo view to WindowManager at position (${params.x}, ${params.y})...")
                windowManager.addView(logoView, params)
                isAdded = true
                android.util.Log.i("FloatingLogoButton", "✓ Logo view successfully added to WindowManager")
            } catch (e: Exception) {
                android.util.Log.e("FloatingLogoButton", "CRITICAL: Failed to add logo view to WindowManager", e)
                android.util.Log.e("FloatingLogoButton", "Error details: ${e.message}")
                android.util.Log.e("FloatingLogoButton", "Stack trace:", e)
            }
        } else {
            android.util.Log.w("FloatingLogoButton", "show() called but view already added, skipping")
        }
    }

    fun hide() {
        if (isAdded) {
            try {
                windowManager.removeView(logoView)
                isAdded = false
            } catch (e: Exception) {
                android.util.Log.e("FloatingLogoButton", "Failed to remove logo view", e)
            }
        }
    }

    fun updatePatternCount(count: Int) {
        if (prefs.isBadgeVisible()) {
            badge.setPatternCount(count)
        }
    }

    fun setDetectionStatus(status: LogoBadge.DetectionStatus) {
        badge.setDetectionStatus(status)
        
        // Static visual feedback without animations
        when (status) {
            LogoBadge.DetectionStatus.SCANNING -> {
                // Slightly dim while scanning
                logoImage.alpha = 0.8f
            }
            LogoBadge.DetectionStatus.PATTERNS_FOUND,
            LogoBadge.DetectionStatus.HIGH_CONFIDENCE -> {
                // Full opacity for patterns found
                logoImage.alpha = 1f
            }
            LogoBadge.DetectionStatus.IDLE -> {
                // Normal opacity
                logoImage.alpha = 1f
            }
        }
    }

    private fun setupTouchListener() {
        logoView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    dragStartTime = System.currentTimeMillis()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    
                    if (!isDragging && (abs(deltaX) > 10 || abs(deltaY) > 10)) {
                        isDragging = true
                    }
                    
                    if (isDragging) {
                        params.x = initialX + deltaX.toInt()
                        params.y = initialY + deltaY.toInt()
                        windowManager.updateViewLayout(logoView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isDragging) {
                        snapToEdge()
                        prefs.savePosition(params.x, params.y)
                    } else {
                        val pressDuration = System.currentTimeMillis() - dragStartTime
                        if (pressDuration >= longPressThreshold) {
                            onLongPressListener?.invoke()
                        } else {
                            onClickListener?.invoke()
                        }
                    }
                    isDragging = false
                    true
                }
                else -> false
            }
        }
    }

    private fun snapToEdge() {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        
        val logoWidth = logoView.width
        val logoHeight = logoView.height
        
        val centerX = params.x + logoWidth / 2
        
        val snapMargin = 20
        
        // Snap to left or right edge based on current position
        val targetX = if (centerX < screenWidth / 2) {
            snapMargin
        } else {
            screenWidth - logoWidth - snapMargin
        }
        
        val targetY = params.y.coerceIn(snapMargin, screenHeight - logoHeight - snapMargin)
        
        // Update position immediately without animation
        params.x = targetX
        params.y = targetY
        windowManager.updateViewLayout(logoView, params)
    }

    fun cleanup() {
        logoImage.alpha = 1f
        hide()
    }
}
