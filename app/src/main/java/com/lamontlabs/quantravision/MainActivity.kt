package com.lamontlabs.quantravision

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.lamontlabs.quantravision.onboarding.OnboardingManager
import com.lamontlabs.quantravision.ui.ProfessionalOnboarding
import com.lamontlabs.quantravision.ui.QuantraVisionApp
import com.lamontlabs.quantravision.ui.theme.QuantraVisionTheme

/**
 * Main entry point for QuantraVision
 * Handles crash logging, onboarding, and launches the full app
 */
class MainActivity : ComponentActivity() {
    
    private lateinit var onboardingManager: OnboardingManager
    
    // Overlay permission launcher
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { 
        // Overlay permission result - will trigger recomposition
    }
    
    // Notification permission launcher (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Notification permission result
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize crash logger first
        CrashLogger.initialize(this)
        
        // Initialize onboarding manager
        onboardingManager = OnboardingManager.getInstance(this)
        
        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        setContent {
            QuantraVisionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    
                    // Mutable state that triggers recomposition when changed
                    var hasCompletedOnboarding by remember { 
                        mutableStateOf(onboardingManager.hasCompletedOnboarding()) 
                    }
                    
                    // Main app routing
                    if (!hasCompletedOnboarding) {
                        // Show full professional onboarding flow
                        ProfessionalOnboarding(
                            context = context,
                            onComplete = {
                                onboardingManager.completeOnboarding()
                                hasCompletedOnboarding = true  // Triggers recomposition
                            }
                        )
                    } else {
                        // Launch full QuantraVision app with all features
                        QuantraVisionApp(context = context)
                    }
                }
            }
        }
    }
    
    /**
     * Request overlay permission
     * Called from UI when needed
     */
    fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Force recomposition when returning from permission settings
        window.decorView.invalidate()
    }
}
