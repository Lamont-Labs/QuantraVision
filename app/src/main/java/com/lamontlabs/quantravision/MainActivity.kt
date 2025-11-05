package com.lamontlabs.quantravision

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider

/**
 * ULTRA MINIMAL VERSION with crash logging
 * This will capture crash details and let you share them
 */
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    setContent {
      MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Text(
              text = "✅ QuantraVision",
              style = MaterialTheme.typography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
              text = "If you see this screen, the basic app works!",
              style = MaterialTheme.typography.bodyLarge,
              textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
              text = "This is a minimal test build with crash logging.\n\n" +
                    "If the app crashes, restart it and use the button below to share crash logs.",
              style = MaterialTheme.typography.bodyMedium,
              textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(onClick = { 
              android.widget.Toast.makeText(
                this@MainActivity,
                "Button works! Basic app is functional.",
                android.widget.Toast.LENGTH_LONG
              ).show()
            }) {
              Text("Test Button")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
              onClick = { exportCrashLog() },
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
              Text("Share Crash Log")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            var crashLogPreview by remember { mutableStateOf("") }
            
            LaunchedEffect(Unit) {
              crashLogPreview = CrashLogger.readCrashLog(this@MainActivity).take(200)
            }
            
            if (crashLogPreview.isNotEmpty() && crashLogPreview != "No crash logs found") {
              Text(
                text = "Crash detected! Use button above to share.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
              )
            }
          }
        }
      }
    }
  }
  
  private fun exportCrashLog() {
    val crashLog = CrashLogger.readCrashLog(this)
    
    if (crashLog == "No crash logs found") {
      android.widget.Toast.makeText(
        this,
        "No crash logs found. If the app crashed, restart it first.",
        android.widget.Toast.LENGTH_LONG
      ).show()
      return
    }
    
    try {
      val logFile = CrashLogger.getCrashLogFile(this)
      val uri = FileProvider.getUriForFile(
        this,
        "${packageName}.fileprovider",
        logFile
      )
      
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "QuantraVision Crash Log")
        putExtra(Intent.EXTRA_TEXT, "QuantraVision crash log attached")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      
      startActivity(Intent.createChooser(shareIntent, "Share Crash Log"))
      
      android.widget.Toast.makeText(
        this,
        "Share the crash log file via email, Drive, etc.",
        android.widget.Toast.LENGTH_LONG
      ).show()
    } catch (e: Exception) {
      android.widget.Toast.makeText(
        this,
        "Error sharing crash log: ${e.message}",
        android.widget.Toast.LENGTH_LONG
      ).show()
    }
  }
}
