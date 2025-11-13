package com.lamontlabs.quantravision.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.lamontlabs.quantravision.ui.screens.onboarding.OnboardingScreen

/**
 * OnboardingActivity - Standalone activity for onboarding flow
 * 
 * Note: This activity is not currently registered in AndroidManifest.xml.
 * The main onboarding flow is handled by AppScaffold via MainActivity.
 * This activity exists for potential future use or standalone onboarding scenarios.
 */
class OnboardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide system navigation bar for immersive onboarding experience
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            QuantraVisionTheme {
                OnboardingScreen(
                    onComplete = {
                        finish()
                    }
                )
            }
        }
    }
}
