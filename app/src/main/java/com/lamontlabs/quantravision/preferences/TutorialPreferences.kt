package com.lamontlabs.quantravision.preferences

import android.content.Context
import android.content.SharedPreferences

/**
 * TutorialPreferences - Manages tutorial state
 * 
 * Tracks whether user has:
 * - Completed first-time walkthrough
 * - Dismissed quick start guide
 * - Wants to see quick tips
 */
object TutorialPreferences {
    
    private const val PREFS_NAME = "tutorial_prefs"
    private const val KEY_COMPLETED_WALKTHROUGH = "completed_walkthrough"
    private const val KEY_SHOW_QUICK_GUIDE = "show_quick_guide"
    private const val KEY_WALKTHROUGH_COMPLETION_DATE = "walkthrough_completion_date"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Check if user has completed first-time walkthrough
     */
    fun hasCompletedWalkthrough(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_COMPLETED_WALKTHROUGH, false)
    }
    
    /**
     * Mark walkthrough as completed
     */
    fun markWalkthroughCompleted(context: Context) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_COMPLETED_WALKTHROUGH, true)
            putLong(KEY_WALKTHROUGH_COMPLETION_DATE, System.currentTimeMillis())
            apply()
        }
    }
    
    /**
     * Reset walkthrough (for Settings → Replay Tutorial)
     */
    fun resetWalkthrough(context: Context) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_COMPLETED_WALKTHROUGH, false)
            apply()
        }
    }
    
    /**
     * Check if quick start guide should be shown
     */
    fun shouldShowQuickGuide(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SHOW_QUICK_GUIDE, true)
    }
    
    /**
     * Dismiss quick start guide
     */
    fun dismissQuickGuide(context: Context) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_SHOW_QUICK_GUIDE, false)
            apply()
        }
    }
    
    /**
     * Re-enable quick start guide
     */
    fun enableQuickGuide(context: Context) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_SHOW_QUICK_GUIDE, true)
            apply()
        }
    }
    
    /**
     * Get walkthrough completion date (for stats)
     */
    fun getWalkthroughCompletionDate(context: Context): Long {
        return getPrefs(context).getLong(KEY_WALKTHROUGH_COMPLETION_DATE, 0L)
    }
}
