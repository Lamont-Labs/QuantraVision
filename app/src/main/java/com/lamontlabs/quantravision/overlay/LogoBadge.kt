package com.lamontlabs.quantravision.overlay

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator

class LogoBadge @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var patternCount = 0
    private var detectionStatus = DetectionStatus.IDLE
    private var ringRotation = 0f

    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF5252")
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 24f
        isFakeBoldText = true
    }

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    enum class DetectionStatus {
        IDLE, SCANNING, PATTERNS_FOUND, HIGH_CONFIDENCE
    }

    fun setPatternCount(count: Int) {
        if (count != patternCount) {
            patternCount = count
            animateBadge()
            invalidate()
        }
    }

    fun setDetectionStatus(status: DetectionStatus) {
        if (status != detectionStatus) {
            detectionStatus = status
            invalidate()
        }
    }

    fun setRingRotation(rotation: Float) {
        ringRotation = rotation
        invalidate()
    }

    private fun animateBadge() {
        val scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.3f, 1f)
        val scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.3f, 1f)
        scaleX.duration = 300
        scaleY.duration = 300
        scaleX.interpolator = OvershootInterpolator()
        scaleY.interpolator = OvershootInterpolator()
        scaleX.start()
        scaleY.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (width.coerceAtMost(height) / 2f) - 10f

        drawDetectionRing(canvas, centerX, centerY, radius)
        
        if (detectionStatus == DetectionStatus.HIGH_CONFIDENCE) {
            drawGlowEffect(canvas, centerX, centerY, radius)
        }

        if (patternCount > 0) {
            drawBadge(canvas, centerX, centerY, radius)
        }
    }

    private fun drawDetectionRing(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        when (detectionStatus) {
            DetectionStatus.SCANNING -> {
                ringPaint.color = Color.parseColor("#FFC107")
                canvas.save()
                canvas.rotate(ringRotation, cx, cy)
                val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
                canvas.drawArc(rect, 0f, 270f, false, ringPaint)
                canvas.restore()
            }
            DetectionStatus.PATTERNS_FOUND -> {
                ringPaint.color = Color.parseColor("#4CAF50")
                canvas.drawCircle(cx, cy, radius, ringPaint)
            }
            DetectionStatus.IDLE -> {
                ringPaint.color = Color.parseColor("#9E9E9E")
                canvas.drawCircle(cx, cy, radius, ringPaint)
            }
            DetectionStatus.HIGH_CONFIDENCE -> {
            }
        }
    }

    private fun drawGlowEffect(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val alpha = (Math.sin(System.currentTimeMillis() / 300.0) * 127 + 128).toInt()
        glowPaint.alpha = alpha
        canvas.drawCircle(cx, cy, radius + 8f, glowPaint)
        invalidate()
    }

    private fun drawBadge(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val badgeRadius = 20f
        val badgeX = cx + radius - badgeRadius
        val badgeY = cy - radius + badgeRadius

        canvas.drawCircle(badgeX, badgeY, badgeRadius, badgePaint)

        val text = if (patternCount > 9) "9+" else patternCount.toString()
        val textY = badgeY - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(text, badgeX, textY, textPaint)
    }
}
