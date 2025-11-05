package com.lamontlabs.quantravision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * ULTRA MINIMAL VERSION - No database, no services, no complex code
 * This will tell us if the crash is in basic structure or complex features
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
              text = "This is a minimal test build with NO complex features.\n\n" +
                    "No database, no pattern detection, no services - just UI.",
              style = MaterialTheme.typography.bodyMedium,
              textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(onClick = { 
              // Just show a toast - no complex code
              android.widget.Toast.makeText(
                this@MainActivity,
                "Button works! Basic app is functional.",
                android.widget.Toast.LENGTH_LONG
              ).show()
            }) {
              Text("Test Button")
            }
          }
        }
      }
    }
  }
}
