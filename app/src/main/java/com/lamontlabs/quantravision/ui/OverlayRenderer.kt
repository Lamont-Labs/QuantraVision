package com.lamontlabs.quantravision.ui

import android.content.Context
import android.graphics.*
import android.view.View
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.boundingBox
import com.lamontlabs.quantravision.detection.HighlightGate
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import com.lamontlabs.quantravision.quota.QuotaGate

/**
 * OverlayRenderer (gated)
 * Renders matches but enforces free highlight quota for non-Pro users.
 * Dims overlays when FREE tier or quota exhausted.
 */
class OverlayRenderer(
    private val appContext: Context,
    matches: List<PatternMatch>
) : View(appContext) {

    private val paint = Paint().apply {
        color = Color.argb(150, 0, 255, 0)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 24f
        isAntiAlias = true
    }

    private val gated = HighlightGate.filterForRender(appContext, matches)
    var quotaExceeded: Boolean = gated.isEmpty() && matches.isNotEmpty()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val tier = EntitlementManager.currentTier.value
        // Map EntitlementManager tiers to QuotaGate tier strings
        // STARTER ($9.99) → PRO (10 calls), STANDARD ($24.99) → ULTRA (25 calls), PRO ($49.99) → ULTRA
        val tierString = when (tier) {
            SubscriptionTier.STARTER -> "PRO"
            SubscriptionTier.STANDARD -> "ULTRA"
            SubscriptionTier.PRO -> "ULTRA"
            SubscriptionTier.FREE -> "FREE"
        }
        val shouldDim = tier == SubscriptionTier.FREE || 
                       QuotaGate.getRemainingCalls(appContext, tierString) <= 0

        val dimPaint = if (shouldDim) {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                alpha = 128
                color = Color.argb(150, 0, 255, 0)
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
        } else {
            paint
        }

        gated.forEach { m ->
            if (!HighlightGate.allowAndCount(appContext)) {
                quotaExceeded = true
                return@forEach
            }
            val rect = m.boundingBox
            canvas.drawRect(rect, dimPaint)
            
            val textPaintForPattern = if (shouldDim) {
                Paint(textPaint).apply { alpha = 128 }
            } else {
                textPaint
            }
            canvas.drawText("${m.patternName} (${(m.confidence * 100).toInt()}%)",
                rect.left.toFloat(), rect.top - 8f, textPaintForPattern)
        }

        val msg = "⚠ Illustrative Only — Not Financial Advice"
        textPaint.color = Color.argb(190, 255, 255, 255)
        textPaint.textSize = 22f
        canvas.drawText(msg, 24f, height - 32f, textPaint)

        if (shouldDim) {
            canvas.drawColor(Color.argb(120, 0, 0, 0))
            textPaint.textSize = 20f
            textPaint.color = Color.WHITE
            if (tier == SubscriptionTier.FREE) {
                canvas.drawText(
                    "Upgrade to PRO for cloud AI explanations", 
                    24f, 
                    height / 2f, 
                    textPaint
                )
            } else {
                canvas.drawText(
                    "Daily cloud quota exhausted. Resets at midnight.", 
                    24f, 
                    height / 2f, 
                    textPaint
                )
            }
        } else if (quotaExceeded) {
            canvas.drawColor(Color.argb(120, 0, 0, 0))
            textPaint.textSize = 20f
            canvas.drawText("Free highlights used. Upgrade to continue.", 24f, height / 2f, textPaint)
        }
    }
}
