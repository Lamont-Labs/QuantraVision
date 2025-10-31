package com.lamontlabs.quantravision.ui

import android.content.Context
import android.util.Log
import android.widget.Toast

object DisclaimerManager {
    private const val PREFS_NAME = "qv_legal_prefs"
    private const val KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted"
    private const val TAG = "DisclaimerManager"
    
    fun isAccepted(context: Context): Boolean {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_DISCLAIMER_ACCEPTED, false)
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL: SharedPreferences corrupted, failing closed for legal compliance", e)
            false
        }
    }
    
    fun setAccepted(context: Context, accepted: Boolean): Boolean {
        return try {
            val success = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DISCLAIMER_ACCEPTED, accepted)
                .commit()
            
            if (!success) {
                Log.e(TAG, "CRITICAL: Failed to save disclaimer acceptance (disk full or I/O error)")
                Toast.makeText(
                    context,
                    "Failed to save acceptance. Please free up storage space and try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL: Exception while saving disclaimer acceptance", e)
            Toast.makeText(
                context,
                "Error saving acceptance: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }
}
