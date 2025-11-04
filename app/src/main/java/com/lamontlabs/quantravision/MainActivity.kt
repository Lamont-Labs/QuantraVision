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
          
          Log.e("MainActivity", "OverlayService failed to start within timeout")
          unregisterServiceReadyReceiver()
          
          // Show proper UI with error instead of blank screen with toast
          runOnUiThread {
            try {
              setContent {
                MaterialTheme {
                  Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                      modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.error
                      )
                      
                      Spacer(modifier = Modifier.height(24.dp))
                      
                      Text(
                        text = "Service Failed to Start",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                      )
                      
                      Spacer(modifier = Modifier.height(16.dp))
                      
                      Text(
                        text = "Failed to start overlay service. Please check permissions and try again.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                      )
                      
                      Spacer(modifier = Modifier.height(32.dp))
                      
                      Button(
                        onClick = {
                          // Restart the activity with clear top flag to retry
                          val retryIntent = Intent(this@MainActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                          }
                          finish()
                          startActivity(retryIntent)
                        },
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(56.dp)
                      ) {
                        Text("Retry")
                      }
                      
                      Spacer(modifier = Modifier.height(16.dp))
                      
                      Button(
                        onClick = {
                          // Navigate to main app without retrying service
                          val mainAppIntent = Intent(this@MainActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra("opened_from_overlay", true)
                          }
                          finish()
                          startActivity(mainAppIntent)
                        },
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                          containerColor = MaterialTheme.colorScheme.secondary
                        )
                      ) {
                        Text("Go to Main App")
                      }
                      
                      Spacer(modifier = Modifier.height(16.dp))
                      
                      OutlinedButton(
                        onClick = {
                          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            val permIntent = android.content.Intent(
                              android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                              android.net.Uri.parse("package:$packageName")
                            )
                            permIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(permIntent)
                          }
                        },
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(56.dp)
                      ) {
                        Text("Check Permissions")
                      }
                      
                      Spacer(modifier = Modifier.height(16.dp))
                      
                      TextButton(
                        onClick = { finish() },
                        modifier = Modifier.fillMaxWidth()
                      ) {
                        Text("Close App")
                      }
                    }
                  }
                }
              }
            } catch (e: Exception) {
              Log.e("MainActivity", "Failed to show error UI", e)
              android.widget.Toast.makeText(
                this@MainActivity,
                "Failed to start overlay service. Please check permissions and try again.",
                android.widget.Toast.LENGTH_LONG
              ).show()
              finish()
            }
          }
        }
        
        return
      } else {
        android.widget.Toast.makeText(
          this,
          "Please grant overlay permission to use QuantraVision",
          android.widget.Toast.LENGTH_LONG
        ).show()
      }
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
