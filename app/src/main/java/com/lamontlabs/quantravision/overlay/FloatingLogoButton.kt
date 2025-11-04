package com.lamontlabs.quantravision.overlay

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import com.lamontlabs.quantravision.MainActivity
import com.lamontlabs.quantravision.R
import kotlin.math.abs

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
    
    private var pulseAnimator: ObjectAnimator? = null
    private var ringRotationAnimator: ValueAnimator? = null
    
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
        // Position in very bottom right corner with small margin
        val defaultX = displayMetrics.widthPixels - sizePx - 30
        val defaultY = displayMetrics.heightPixels - sizePx - 30
        
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
        if (!isAdded) {
            try {
                windowManager.addView(logoView, params)
                isAdded = true
            } catch (e: Exception) {
                android.util.Log.e("FloatingLogoButton", "Failed to add logo view", e)
            }
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

    private fun openMainApp() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        context.startActivity(intent)
    }

    fun setDetectionStatus(status: LogoBadge.DetectionStatus) {
        badge.setDetectionStatus(status)
        
        when (status) {
            LogoBadge.DetectionStatus.SCANNING -> startScanningAnimation()
            LogoBadge.DetectionStatus.PATTERNS_FOUND -> stopScanningAnimation()
            LogoBadge.DetectionStatus.HIGH_CONFIDENCE -> startPulseAnimation()
            LogoBadge.DetectionStatus.IDLE -> stopAllAnimations()
        }
    }

    private fun startScanningAnimation() {
        stopAllAnimations()
        ringRotationAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                badge.setRingRotation(animation.animatedValue as Float)
            }
            start()
        }
    }

    private fun startPulseAnimation() {
        stopAllAnimations()
        pulseAnimator = ObjectAnimator.ofFloat(logoImage, "alpha", 1f, 0.6f, 1f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
    }

    private fun stopScanningAnimation() {
        ringRotationAnimator?.cancel()
        ringRotationAnimator = null
    }

    private fun stopAllAnimations() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        ringRotationAnimator?.cancel()
        ringRotationAnimator = null
        logoImage.alpha = 1f
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
        val centerY = params.y + logoHeight / 2
        
        val snapMargin = 30
        
        val targetX: Int
        val targetY: Int
        
        // Snap horizontally - prefer right side for trading apps
        if (centerX < screenWidth / 2) {
            // Left half of screen - snap to left edge
            targetX = snapMargin
        } else {
            // Right half of screen - snap to right edge (preferred for trading)
            targetX = screenWidth - logoWidth - snapMargin
        }
        
        // Snap vertically - prefer bottom for easy thumb access
        if (centerY < screenHeight / 2) {
            // Top half - snap to top
            targetY = snapMargin
        } else {
            // Bottom half - snap to bottom (preferred position)
            targetY = screenHeight - logoHeight - snapMargin
        }
        
        // Animate horizontal movement
        val animatorX = ValueAnimator.ofInt(params.x, targetX)
        animatorX.addUpdateListener { animation ->
            params.x = animation.animatedValue as Int
            windowManager.updateViewLayout(logoView, params)
        }
        animatorX.duration = 200
        animatorX.start()
        
        // Animate vertical movement
        val animatorY = ValueAnimator.ofInt(params.y, targetY)
        animatorY.addUpdateListener { animation ->
            params.y = animation.animatedValue as Int
            windowManager.updateViewLayout(logoView, params)
        }
        animatorY.duration = 200
        animatorY.start()
        
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(20)
        }
    }

    fun cleanup() {
        stopAllAnimations()
        hide()
    }
}
