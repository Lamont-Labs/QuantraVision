package com.lamontlabs.quantravision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.lamontlabs.quantravision.startup.DisclaimerGate
import com.lamontlabs.quantravision.ui.DisclaimerManager
import com.lamontlabs.quantravision.ui.MainScreen

/**
 * MainActivity
 * Entry point for QuantraVision.
 * Enforces disclaimer acknowledgment before loading UI.
 * Fails closed if user declines.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (!DisclaimerGate.verifyOrExit(this)) {
                DisclaimerManager.ShowIfNeeded(this) {
                    recreate() // reload after acceptance
                }
            } else {
                MainScreen()
            }
        }
    }
}
