package com.lamontlabs.quantravision

import android.os.Build
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
import com.lamontlabs.quantravision.permissions.PermissionOrchestrator
import com.lamontlabs.quantravision.intelligence.llm.onboarding.ModelProvisionOrchestrator
import com.lamontlabs.quantravision.ui.*

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
        val context = LocalContext.current
        
        // Initialize from actual state to handle process restarts
        val hasNotificationPerm = remember {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(
              context,
              android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
          } else {
            true
          }
        }
        val hasOverlayPerm = remember {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.provider.Settings.canDrawOverlays(context)
          } else {
            true
          }
        }
        val modelExists = remember {
          com.lamontlabs.quantravision.intelligence.llm.ModelManager(context)
            .getModelState() == com.lamontlabs.quantravision.intelligence.llm.ModelState.Downloaded
        }
        
        var permissionsGranted by remember { mutableStateOf(hasNotificationPerm && hasOverlayPerm) }
        var modelReady by remember { mutableStateOf(modelExists) }
        
        when {
          !permissionsGranted -> {
            // Step 1: Request permissions first
            PermissionOrchestrator(
              onAllPermissionsGranted = {
                permissionsGranted = true
              }
            )
          }
          !modelReady -> {
            // Step 2: Ensure AI model is imported
            // This happens BEFORE OverlayService can ever start
            ModelProvisionOrchestrator(
              onModelReady = {
                modelReady = true
              }
            )
          }
          else -> {
            // Step 3: All setup complete - show main app
            QuantraVisionApp(context = this)
          }
        }
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
