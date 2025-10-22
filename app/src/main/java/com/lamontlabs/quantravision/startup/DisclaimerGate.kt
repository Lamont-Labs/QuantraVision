package com.lamontlabs.quantravision.startup

import android.content.Context
import com.lamontlabs.quantravision.ui.DisclaimerManager

/**
 * DisclaimerGate
 * Startup check; fail-closed if user has not accepted disclaimer.
 */
object DisclaimerGate {

    fun verifyOrExit(context: Context): Boolean {
        val ok = DisclaimerManager.isAccepted(context)
        if (!ok) {
            // Do not continue to detection; caller should open modal.
            return false
        }
        return true
    }
}
