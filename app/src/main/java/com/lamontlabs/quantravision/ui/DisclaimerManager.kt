package com.lamontlabs.quantravision.ui

import android.content.Context

object DisclaimerManager {
    private const val PREFS_NAME = "qv_legal_prefs"
    private const val KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted"
    
    fun isAccepted(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DISCLAIMER_ACCEPTED, false)
    }
    
    fun setAccepted(context: Context, accepted: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DISCLAIMER_ACCEPTED, accepted)
            .apply()
    }
}
