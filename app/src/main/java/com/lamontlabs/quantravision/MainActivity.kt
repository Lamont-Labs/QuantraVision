package com.lamontlabs.quantravision

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lamontlabs.quantravision.onboarding.OnboardingManager
import com.lamontlabs.quantravision.ui.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Enable immersive mode - hide navigation bar throughout entire app
    // Navigation bar only appears when user swipes up from bottom
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, window.decorView).apply {
      hide(WindowInsetsCompat.Type.navigationBars())
      systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    
    try {
      setContent {
        QuantraVisionApp(context = this)
      }
    } catch (e: Exception) {
      Log.e("MainActivity", "Fatal error initializing Compose UI", e)
      
      // Fallback error screen if Compose fails to initialize
      try {
        setContent {
          MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
              Column(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Text(
                  text = "⚠️ Startup Error",
                  style = MaterialTheme.typography.headlineMedium,
                  color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                  text = "QuantraVision failed to start. Please try:\n\n" +
                         "1. Restart the app\n" +
                         "2. Clear app cache\n" +
                         "3. Reinstall if problem persists\n\n" +
                         "Error: ${e.message ?: "Unknown error"}",
                  style = MaterialTheme.typography.bodyMedium,
                  textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { finish() }) {
                  Text("Close App")
                }
              }
            }
          }
        }
      } catch (fallbackError: Exception) {
        Log.e("MainActivity", "Fatal error in fallback UI", fallbackError)
        finish()
      }
    }
  }
}
