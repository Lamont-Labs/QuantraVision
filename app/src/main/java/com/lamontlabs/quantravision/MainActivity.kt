package com.lamontlabs.quantravision

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.lamontlabs.quantravision.ui.QuantraVisionApp

/**
 * Main entry point for QuantraVision
 * QuantraVisionApp handles all routing (onboarding, permissions, overlay, dashboard)
 */
class MainActivity : ComponentActivity() {
    
    // Notification permission launcher (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Notification permission granted or denied
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize crash logger
        CrashLogger.initialize(this)
        
        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        // Launch full app - QuantraVisionApp handles all routing internally:
        // - Onboarding flow (if not completed)
        // - Overlay permission request (if needed)
        // - Pattern detection dashboard (main app)
        setContent {
            QuantraVisionApp(context = this)
        }
    }
}
