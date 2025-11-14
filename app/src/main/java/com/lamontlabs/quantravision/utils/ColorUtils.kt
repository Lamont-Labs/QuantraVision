package com.lamontlabs.quantravision.utils

import androidx.compose.ui.graphics.Color
import com.lamontlabs.quantravision.ui.theme.AppColors

object ColorUtils {
    
    fun getConfidenceColor(confidence: Float): Color {
        return when {
            confidence >= 0.8f -> AppColors.Success
            confidence >= 0.6f -> AppColors.NeonGold
            confidence >= 0.4f -> AppColors.Warning
            else -> AppColors.Error
        }
    }
    
    fun getPriceChangeColor(change: Double): Color {
        return if (change >= 0) AppColors.Success else AppColors.Error
    }
    
    fun Color.withOpacity(opacity: Float): Color {
        return this.copy(alpha = opacity)
    }
    
    fun blendColors(color1: Color, color2: Color, ratio: Float): Color {
        val clampedRatio = ratio.coerceIn(0f, 1f)
        return Color(
            red = color1.red * (1 - clampedRatio) + color2.red * clampedRatio,
            green = color1.green * (1 - clampedRatio) + color2.green * clampedRatio,
            blue = color1.blue * (1 - clampedRatio) + color2.blue * clampedRatio,
            alpha = color1.alpha * (1 - clampedRatio) + color2.alpha * clampedRatio
        )
    }
}
