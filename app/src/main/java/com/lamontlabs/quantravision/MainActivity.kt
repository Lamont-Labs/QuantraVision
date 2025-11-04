package com.lamontlabs.quantravision

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lamontlabs.quantravision.ui.*
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {

  companion object {
    private const val SERVICE_READY_ACTION = "com.lamontlabs.quantravision.OVERLAY_SERVICE_READY"
    private const val SERVICE_START_TIMEOUT_MS = 5000L
  }

  private var serviceReadyReceiver: BroadcastReceiver? = null
  private var timeoutJob: Job? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    try {
      val onboardingManager = com.lamontlabs.quantravision.onboarding.OnboardingManager.getInstance(this)
      val isOpenedFromOverlay = intent.getBooleanExtra("opened_from_overlay", false)
      
      if (onboardingManager.hasCompletedOnboarding() && !isOpenedFromOverlay) {
        val hasOverlayPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
          android.provider.Settings.canDrawOverlays(this)
        } else {
          true
        }
        
        if (hasOverlayPermission) {
          serviceReadyReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
              if (intent?.action == SERVICE_READY_ACTION) {
                Log.i("MainActivity", "OverlayService started successfully")
                timeoutJob?.cancel()
                unregisterServiceReadyReceiver()
                
                android.widget.Toast.makeText(
                  this@MainActivity,
                  "Tap the cyan Q button to access QuantraVision",
                  android.widget.Toast.LENGTH_SHORT
                ).show()
                
                finish()
              }
            }
          }
          
          val filter = IntentFilter(SERVICE_READY_ACTION)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(serviceReadyReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
          } else {
            registerReceiver(serviceReadyReceiver, filter)
          }
          
          val serviceIntent = android.content.Intent(this, com.lamontlabs.quantravision.overlay.OverlayService::class.java)
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
          } else {
            startService(serviceIntent)
          }
          
          timeoutJob = lifecycleScope.launch {
            delay(SERVICE_START_TIMEOUT_MS)
            
            Log.e("MainActivity", "OverlayService failed to start within timeout - user can still use main UI")
            unregisterServiceReadyReceiver()
            
            runOnUiThread {
              android.widget.Toast.makeText(
                this@MainActivity,
                "Overlay service timeout. Using main UI mode.",
                android.widget.Toast.LENGTH_SHORT
              ).show()
            }
          }
        } else {
          android.widget.Toast.makeText(
            this,
            "Please grant overlay permission to use QuantraVision",
            android.widget.Toast.LENGTH_LONG
          ).show()
        }
      }
    } catch (e: Exception) {
      Log.e("MainActivity", "Auto-launch overlay failed (likely ForegroundServiceStartNotAllowedException on Android 12+), falling back to Compose UI", e)
      unregisterServiceReadyReceiver()
      timeoutJob?.cancel()
      
      android.widget.Toast.makeText(
        this,
        "Starting app in dashboard mode. Tap 'Start Detection' to launch overlay.",
        android.widget.Toast.LENGTH_LONG
      ).show()
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

  private fun unregisterServiceReadyReceiver() {
    serviceReadyReceiver?.let {
      try {
        unregisterReceiver(it)
      } catch (e: IllegalArgumentException) {
        Log.w("MainActivity", "Receiver already unregistered", e)
      }
      serviceReadyReceiver = null
    }
  }

  override fun onDestroy() {
    timeoutJob?.cancel()
    unregisterServiceReadyReceiver()
    super.onDestroy()
  }
}
