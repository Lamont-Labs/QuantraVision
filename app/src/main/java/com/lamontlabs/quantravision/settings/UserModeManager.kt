package com.lamontlabs.quantravision.settings

import android.content.Context
import android.content.SharedPreferences

/**
 * UserModeManager
 * Controls Beginner/Pro mode visibility.
 * Stored locally; defaults to Beginner.
 */
object UserModeManager {

    private const val PREF_NAME = "user_mode"
    private const val KEY_MODE = "mode"

    enum class Mode { BEGINNER, PRO }

    fun get(context: Context): Mode {
        val prefs = prefs(context)
        return if (prefs.getString(KEY_MODE, "BEGINNER") == "PRO") Mode.PRO else Mode.BEGINNER
    }

    fun toggle(context: Context) {
        val current = get(context)
        val newMode = if (current == Mode.BEGINNER) Mode.PRO else Mode.BEGINNER
        prefs(context).edit().putString(KEY_MODE, newMode.name).apply()
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isBeginner(context: Context) = get(context) == Mode.BEGINNER
}
