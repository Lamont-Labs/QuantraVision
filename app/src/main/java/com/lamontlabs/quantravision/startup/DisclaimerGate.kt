package com.lamontlabs.quantravision.startup

import android.content.Context
import android.util.Log
import com.lamontlabs.quantravision.ui.DisclaimerManager

/**
 * DisclaimerGate
 * Startup check; fail-closed if user has not accepted disclaimer.
 */
object DisclaimerGate {
    private const val TAG = "DisclaimerGate"

    fun verifyOrExit(context: Context): Boolean {
        return try {
            val ok = DisclaimerManager.isAccepted(context)
            if (!ok) {
                // Do not continue to detection; caller should open modal.
                return false
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL: Exception in disclaimer verification, failing closed for legal compliance", e)
            false
        }
    }
}
