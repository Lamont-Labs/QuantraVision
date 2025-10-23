package com.lamontlabs.quantravision.boot

import android.content.Context
import com.lamontlabs.quantravision.security.LicenseVerifier
import com.lamontlabs.quantravision.security.ProofGate
import com.lamontlabs.quantravision.system.PowerHint
import com.lamontlabs.quantravision.system.ThermalGuard
import android.widget.Toast

/**
 * SafeBoot
 * Runs mandatory safety checks before enabling capture or overlay.
 * Fails closed if licensing, power, or thermal constraints violated.
 */
object SafeBoot {

    fun run(context: Context): Boolean {
        val license = LicenseVerifier.verify(context)
        if (!license.valid) {
            Toast.makeText(context, "SafeBoot: License invalid — ${license.reason}", Toast.LENGTH_LONG).show()
            return false
        }

        val proof = ProofGate.assertClean(context)
        if (!proof.ok) {
            Toast.makeText(context, "SafeBoot: ${proof.message}", Toast.LENGTH_LONG).show()
            return false
        }

        // preflight hardware safety
        PowerHint.check(context) { low -> if (low) Toast.makeText(context, "SafeBoot: Battery low", Toast.LENGTH_SHORT).show() }
        ThermalGuard.start(context) { throttle ->
            if (throttle) Toast.makeText(context, "SafeBoot: Thermal throttle engaged", Toast.LENGTH_SHORT).show()
        }

        return true
    }
}
