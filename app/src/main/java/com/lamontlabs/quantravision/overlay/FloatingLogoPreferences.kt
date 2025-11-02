package com.lamontlabs.quantravision.overlay

import android.content.Context
import android.content.SharedPreferences

class FloatingLogoPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "floating_logo_prefs",
        Context.MODE_PRIVATE
    )

    enum class LogoSize(val dp: Int) {
        SMALL(50),
        MEDIUM(60),
        LARGE(70)
    }

    fun savePosition(x: Int, y: Int) {
        prefs.edit()
            .putInt(KEY_POSITION_X, x)
            .putInt(KEY_POSITION_Y, y)
            .apply()
    }

    fun getPositionX(defaultX: Int): Int {
        return prefs.getInt(KEY_POSITION_X, defaultX)
    }

    fun getPositionY(defaultY: Int): Int {
        return prefs.getInt(KEY_POSITION_Y, defaultY)
    }

    fun saveLogoSize(size: LogoSize) {
        prefs.edit()
            .putString(KEY_LOGO_SIZE, size.name)
            .apply()
    }

    fun getLogoSize(): LogoSize {
        val sizeName = prefs.getString(KEY_LOGO_SIZE, LogoSize.MEDIUM.name)
        return try {
            LogoSize.valueOf(sizeName ?: LogoSize.MEDIUM.name)
        } catch (e: IllegalArgumentException) {
            LogoSize.MEDIUM
        }
    }

    fun saveLogoOpacity(opacity: Float) {
        prefs.edit()
            .putFloat(KEY_LOGO_OPACITY, opacity)
            .apply()
    }

    fun getLogoOpacity(): Float {
        return prefs.getFloat(KEY_LOGO_OPACITY, 0.85f)
    }

    fun saveBadgeVisibility(visible: Boolean) {
        prefs.edit()
            .putBoolean(KEY_BADGE_VISIBLE, visible)
            .apply()
    }

    fun isBadgeVisible(): Boolean {
        return prefs.getBoolean(KEY_BADGE_VISIBLE, true)
    }

    fun saveAutoHideDelay(seconds: Int) {
        prefs.edit()
            .putInt(KEY_AUTO_HIDE_DELAY, seconds)
            .apply()
    }

    fun getAutoHideDelay(): Int {
        return prefs.getInt(KEY_AUTO_HIDE_DELAY, 0)
    }

    companion object {
        private const val KEY_POSITION_X = "logo_position_x"
        private const val KEY_POSITION_Y = "logo_position_y"
        private const val KEY_LOGO_SIZE = "logo_size"
        private const val KEY_LOGO_OPACITY = "logo_opacity"
        private const val KEY_BADGE_VISIBLE = "badge_visible"
        private const val KEY_AUTO_HIDE_DELAY = "auto_hide_delay"
    }
}
