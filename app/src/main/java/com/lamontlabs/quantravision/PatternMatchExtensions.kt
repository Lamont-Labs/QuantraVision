package com.lamontlabs.quantravision

import android.graphics.RectF

/**
 * Extension property to parse detectionBounds string into RectF
 * detectionBounds format: "x,y,w,h" or null
 */
val PatternMatch.boundingBox: RectF
    get() {
        val bounds = detectionBounds
        if (bounds.isNullOrEmpty()) {
            return RectF(0f, 0f, 0f, 0f)
        }
        
        return try {
            val parts = bounds.split(",").map { it.toFloat() }
            if (parts.size == 4) {
                val (x, y, w, h) = parts
                RectF(x, y, x + w, y + h)
            } else {
                RectF(0f, 0f, 0f, 0f)
            }
        } catch (e: Exception) {
            RectF(0f, 0f, 0f, 0f)
        }
    }

/**
 * Extension property to provide explanation for patterns
 * Returns a standard explanation text for pattern detections
 */
val PatternMatch.explanation: String
    get() = "Pattern identified deterministically via template matching. No predictive modeling used."
