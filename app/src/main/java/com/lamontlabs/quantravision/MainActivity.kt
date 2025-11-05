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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lamontlabs.quantravision.ml.HybridDetectorBridge
import com.lamontlabs.quantravision.onboarding.OnboardingManager
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
    
    // EMERGENCY BYPASS: Show simple screen to identify crash point
    try {
      setContent {
        MaterialTheme {
          Surface(modifier = Modifier.fillMaxSize()) {
            Column(
              modifier = Modifier.fillMaxSize().padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Text(
                text = "QuantraVision",
                style = MaterialTheme.typography.headlineMedium
              )
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "Test build - checking for crash",
                style = MaterialTheme.typography.bodyMedium
              )
              Spacer(modifier = Modifier.height(24.dp))
              Text(
                text = "If you see this, the app is working!\n\nClick below to continue",
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(24.dp))
              Button(onClick = { loadFullApp() }) {
                Text("Load Full App")
              }
            }
          }
        }
      }
      return
    } catch (e: Exception) {
      Log.e("QV-MainActivity", "EMERGENCY SCREEN FAILED: ${e.message}", e)
      // Fall through to normal initialization
    }
    
    // VERBOSE LOGGING: Track every step so user can capture logs
    Log.e("QV-MainActivity", "════════════════════════════════════════")
    Log.e("QV-MainActivity", "▶ MainActivity.onCreate() STARTED")
    Log.e("QV-MainActivity", "Android Version: ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})")
    Log.e("QV-MainActivity", "Device: ${Build.MANUFACTURER} ${Build.MODEL}")
    Log.e("QV-MainActivity", "════════════════════════════════════════")
    
    try {
      Log.e("QV-MainActivity", "Step 1: Getting OnboardingManager...")
      val onboardingManager = com.lamontlabs.quantravision.onboarding.OnboardingManager.getInstance(this)
      Log.e("QV-MainActivity", "✓ OnboardingManager obtained")
      
      val isOpenedFromOverlay = intent.getBooleanExtra("opened_from_overlay", false)
      Log.e("QV-MainActivity", "Step 2: Opened from overlay: $isOpenedFromOverlay")
      
      val completedOnboarding = onboardingManager.hasCompletedOnboarding()
      Log.e("QV-MainActivity", "Step 3: Onboarding completed: $completedOnboarding")
      
      if (completedOnboarding && !isOpenedFromOverlay) {
        Log.e("QV-MainActivity", "Step 4: Checking overlay permission...")
        val hasOverlayPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
          android.provider.Settings.canDrawOverlays(this)
        } else {
          true
        }
        Log.e("QV-MainActivity", "✓ Overlay permission: $hasOverlayPermission")
        
        if (hasOverlayPermission) {
          Log.e("QV-MainActivity", "Step 5: Attempting to start OverlayService...")
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
          Log.e("QV-MainActivity", "Step 6: Starting foreground service...")
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
            Log.e("QV-MainActivity", "✓ Called startForegroundService()")
          } else {
            startService(serviceIntent)
            Log.e("QV-MainActivity", "✓ Called startService()")
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
          Log.e("QV-MainActivity", "⚠ NO overlay permission - skipping service start")
          android.widget.Toast.makeText(
            this,
            "Please grant overlay permission to use QuantraVision",
            android.widget.Toast.LENGTH_LONG
          ).show()
        }
      } else {
        Log.e("QV-MainActivity", "Step 4: Skipping overlay service (onboarding not complete or opened from overlay)")
      }
    } catch (e: Exception) {
      Log.e("QV-MainActivity", "⚠⚠⚠ EXCEPTION in overlay service startup ⚠⚠⚠")
      Log.e("QV-MainActivity", "Exception type: ${e.javaClass.name}")
      Log.e("QV-MainActivity", "Exception message: ${e.message}")
      Log.e("QV-MainActivity", "Stack trace:", e)
      unregisterServiceReadyReceiver()
      timeoutJob?.cancel()
      
      android.widget.Toast.makeText(
        this,
        "Starting app in dashboard mode. Tap 'Start Detection' to launch overlay.",
        android.widget.Toast.LENGTH_LONG
      ).show()
    }
    
    Log.e("QV-MainActivity", "════════════════════════════════════════")
    Log.e("QV-MainActivity", "Step 7: Initializing Compose UI...")
    try {
      setContent {
        QuantraVisionApp(context = this)
      }
      Log.e("QV-MainActivity", "✓✓✓ Compose UI setContent() COMPLETED SUCCESSFULLY ✓✓✓")
    } catch (e: Exception) {
      Log.e("QV-MainActivity", "⚠⚠⚠ FATAL: Compose UI failed to initialize ⚠⚠⚠")
      Log.e("QV-MainActivity", "Exception type: ${e.javaClass.name}")
      Log.e("QV-MainActivity", "Exception message: ${e.message}")
      Log.e("QV-MainActivity", "Stack trace:", e)
      
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
        Log.e("QV-MainActivity", "⚠⚠⚠ FATAL: Even fallback UI failed ⚠⚠⚠")
        Log.e("QV-MainActivity", "Fallback exception type: ${fallbackError.javaClass.name}")
        Log.e("QV-MainActivity", "Fallback exception message: ${fallbackError.message}")
        Log.e("QV-MainActivity", "Fallback stack trace:", fallbackError)
        // DO NOT call finish() - let the app stay alive even if UI fails
      }
    }
    
    Log.e("QV-MainActivity", "════════════════════════════════════════")
    Log.e("QV-MainActivity", "✓ MainActivity.onCreate() FINISHED")
    Log.e("QV-MainActivity", "════════════════════════════════════════")
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
  
  private fun loadFullApp() {
    Log.e("QV-MainActivity", "Loading component test screen...")
    
    setContent {
      var testResult by remember { mutableStateOf("Ready to test") }
      var isSuccess by remember { mutableStateOf(true) }
      
      MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Text(
              text = "Component Tests",
              style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
              text = testResult,
              style = MaterialTheme.typography.bodyMedium,
              color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
              textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
              onClick = {
                try {
                  OnboardingManager.getInstance(this@MainActivity)
                  testResult = "✓ OnboardingManager OK"
                  isSuccess = true
                } catch (e: Exception) {
                  testResult = "✗ OnboardingManager FAILED:\n${e.message}"
                  isSuccess = false
                }
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Test OnboardingManager")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
              onClick = {
                try {
                  PatternDetector(this@MainActivity)
                  testResult = "✓ PatternDetector OK"
                  isSuccess = true
                } catch (e: Exception) {
                  testResult = "✗ PatternDetector FAILED:\n${e.message}"
                  isSuccess = false
                }
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Test PatternDetector")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
              onClick = {
                try {
                  HybridDetectorBridge(this@MainActivity)
                  testResult = "✓ HybridDetectorBridge OK"
                  isSuccess = true
                } catch (e: Exception) {
                  testResult = "✗ HybridDetectorBridge FAILED:\n${e.message}"
                  isSuccess = false
                }
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Test HybridDetectorBridge")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
              onClick = { loadRealApp() },
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Load Real App")
            }
          }
        }
      }
    }
  }
  
  private fun loadRealApp() {
    Log.e("QV-MainActivity", "Loading QuantraVisionApp with bypass...")
    
    setContent {
      QuantraVisionTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Text(
              text = "Navigation Bypass Test",
              style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
              onClick = {
                setContent {
                  QuantraVisionTheme {
                    ProfessionalOnboarding(
                      context = this@MainActivity,
                      onComplete = {
                        Log.e("QV-MainActivity", "Onboarding completed")
                      }
                    )
                  }
                }
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Test Onboarding Screen")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
              onClick = {
                // Test creating objects on background thread
                setContent {
                  QuantraVisionTheme {
                    TestBackgroundCreation()
                  }
                }
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Test Background Object Creation")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
              onClick = {
                setContent {
                  QuantraVisionApp(context = this@MainActivity)
                }
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Test Full App (with Navigation)")
            }
          }
        }
      }
    }
  }
  
  @Composable
  private fun TestBackgroundCreation() {
    val context = LocalContext.current
    var result by remember { mutableStateOf("Creating objects on background thread...") }
    var isSuccess by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
      try {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
          val appContext = context.applicationContext
          val bridge = HybridDetectorBridge(appContext)
          val detector = PatternDetector(appContext)
          
          kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            result = "✓ Objects created successfully on background thread!\n\nBridge: ${bridge.javaClass.simpleName}\nDetector: ${detector.javaClass.simpleName}"
            isSuccess = true
          }
        }
      } catch (e: Exception) {
        result = "✗ Failed to create objects:\n${e.message}\n\n${e.stackTraceToString().take(500)}"
        isSuccess = false
      }
    }
    
    Surface(modifier = Modifier.fillMaxSize()) {
      Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = "Background Creation Test",
          style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
          text = result,
          style = MaterialTheme.typography.bodyMedium,
          color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
          textAlign = TextAlign.Center
        )
      }
    }
  }
}
