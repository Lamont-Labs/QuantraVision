package com.lamontlabs.quantravision.ui

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * ThemePrefs
 * Manages deterministic theme configuration (light/dark/high-contrast).
 * Stored locally in /files/theme.json.
 */
object ThemePrefs {

    private const val FILE = "theme.json"

    data class ThemeConfig(
        val darkMode: Boolean,
        val highContrast: Boolean,
        val accentColor: String
    )

    fun load(context: Context): ThemeConfig {
        val f = File(context.filesDir, FILE)
        if (!f.exists()) return ThemeConfig(false, false, "#00FF88")
        val obj = JSONObject(f.readText())
        return ThemeConfig(
            obj.optBoolean("darkMode", false),
            obj.optBoolean("highContrast", false),
            obj.optString("accentColor", "#00FF88")
        )
    }

    fun save(context: Context, config: ThemeConfig) {
        val obj = JSONObject()
        obj.put("darkMode", config.darkMode)
        obj.put("highContrast", config.highContrast)
        obj.put("accentColor", config.accentColor)
        File(context.filesDir, FILE).writeText(obj.toString(2))
    }
}
