package com.lamontlabs.quantravision.ui

import android.content.Context
import android.content.SharedPreferences

/**
 * OverlayVisualConfig - User-customizable visual settings for pattern highlighting
 * 
 * Provides professional visual presets and granular control over:
 * - Glow intensity
 * - Animation speed
 * - Color schemes
 * - Border thickness
 * - Corner accent style
 */
object OverlayVisualConfig {
    
    private const val PREFS_NAME = "overlay_visual_config"
    private const val KEY_PRESET = "visual_preset"
    private const val KEY_GLOW_INTENSITY = "glow_intensity"
    private const val KEY_ANIMATION_SPEED = "animation_speed"
    private const val KEY_SHOW_CONFIDENCE_BADGE = "show_confidence_badge"
    private const val KEY_SHOW_CORNER_ACCENTS = "show_corner_accents"
    private const val KEY_BORDER_THICKNESS = "border_thickness"
    
    enum class VisualPreset {
        SUBTLE,
        BALANCED,
        VIBRANT,
        PROFESSIONAL,
        NEON
    }
    
    data class VisualSettings(
        val preset: VisualPreset = VisualPreset.PROFESSIONAL,
        val glowIntensity: Float = 1.0f,
        val animationSpeed: Float = 1.0f,
        val showConfidenceBadge: Boolean = true,
        val showCornerAccents: Boolean = true,
        val borderThickness: Float = 3f
    )
    
    fun getSettings(context: Context): VisualSettings {
        val prefs = getPrefs(context)
        
        val presetName = prefs.getString(KEY_PRESET, VisualPreset.PROFESSIONAL.name) ?: VisualPreset.PROFESSIONAL.name
        val preset = try {
            VisualPreset.valueOf(presetName)
        } catch (e: Exception) {
            VisualPreset.PROFESSIONAL
        }
        
        return VisualSettings(
            preset = preset,
            glowIntensity = prefs.getFloat(KEY_GLOW_INTENSITY, 1.0f),
            animationSpeed = prefs.getFloat(KEY_ANIMATION_SPEED, 1.0f),
            showConfidenceBadge = prefs.getBoolean(KEY_SHOW_CONFIDENCE_BADGE, true),
            showCornerAccents = prefs.getBoolean(KEY_SHOW_CORNER_ACCENTS, true),
            borderThickness = prefs.getFloat(KEY_BORDER_THICKNESS, 3f)
        )
    }
    
    fun saveSettings(context: Context, settings: VisualSettings) {
        getPrefs(context).edit().apply {
            putString(KEY_PRESET, settings.preset.name)
            putFloat(KEY_GLOW_INTENSITY, settings.glowIntensity)
            putFloat(KEY_ANIMATION_SPEED, settings.animationSpeed)
            putBoolean(KEY_SHOW_CONFIDENCE_BADGE, settings.showConfidenceBadge)
            putBoolean(KEY_SHOW_CORNER_ACCENTS, settings.showCornerAccents)
            putFloat(KEY_BORDER_THICKNESS, settings.borderThickness)
            apply()
        }
    }
    
    fun applyPreset(context: Context, preset: VisualPreset) {
        val settings = when (preset) {
            VisualPreset.SUBTLE -> VisualSettings(
                preset = preset,
                glowIntensity = 0.5f,
                animationSpeed = 0.7f,
                showConfidenceBadge = true,
                showCornerAccents = false,
                borderThickness = 2f
            )
            
            VisualPreset.BALANCED -> VisualSettings(
                preset = preset,
                glowIntensity = 0.8f,
                animationSpeed = 1.0f,
                showConfidenceBadge = true,
                showCornerAccents = true,
                borderThickness = 3f
            )
            
            VisualPreset.VIBRANT -> VisualSettings(
                preset = preset,
                glowIntensity = 1.2f,
                animationSpeed = 1.3f,
                showConfidenceBadge = true,
                showCornerAccents = true,
                borderThickness = 4f
            )
            
            VisualPreset.PROFESSIONAL -> VisualSettings(
                preset = preset,
                glowIntensity = 1.0f,
                animationSpeed = 1.0f,
                showConfidenceBadge = true,
                showCornerAccents = true,
                borderThickness = 3f
            )
            
            VisualPreset.NEON -> VisualSettings(
                preset = preset,
                glowIntensity = 1.5f,
                animationSpeed = 1.5f,
                showConfidenceBadge = true,
                showCornerAccents = true,
                borderThickness = 4f
            )
        }
        
        saveSettings(context, settings)
    }
    
    fun getPresetDescription(preset: VisualPreset): String {
        return when (preset) {
            VisualPreset.SUBTLE -> "Minimal glow, clean and understated"
            VisualPreset.BALANCED -> "Moderate effects, good for most users"
            VisualPreset.VIBRANT -> "Enhanced colors and animations"
            VisualPreset.PROFESSIONAL -> "Sharp, polished, and visually appealing (recommended)"
            VisualPreset.NEON -> "Maximum glow and intensity, cyberpunk style"
        }
    }
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
