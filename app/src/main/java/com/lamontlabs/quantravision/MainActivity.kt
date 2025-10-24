package com.lamontlabs.quantravision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.lamontlabs.quantravision.detection.Detector
import com.lamontlabs.quantravision.detection.PatternLibrary
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { QuantraVisionApp() }
  }

  @Composable
  fun QuantraVisionApp() {
    MaterialTheme {
      val ctx = LocalContext.current
      val detector = remember { Detector() }
      LaunchedEffect(Unit) { detector.load(ctx) }

      Surface(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
          // Camera preview container (for demo input)
          AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { PreviewView(it).apply { implementationMode = PreviewView.ImplementationMode.PERFORMANCE } }
          )
          // Minimal HUD
          Column(
            Modifier
              .fillMaxWidth()
              .align(Alignment.TopCenter)
              .padding(12.dp)) {
            Text("QuantraVision Overlay — Demo", style = MaterialTheme.typography.titleMedium)
            Text("Watermark: QuantraVision / Lamont Labs", style = MaterialTheme.typography.labelSmall)
          }
          // Action bar
          Row(
            Modifier
              .align(Alignment.BottomCenter)
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            val scope = rememberCoroutineScope()
            Button(onClick = { scope.launch { detector.demoScan() } }) { Text("Scan") }
            Spacer(Modifier.width(12.dp))
            Button(onClick = { PatternLibrary.toggleDemoBoxes() }) { Text("Toggle Boxes") }
          }
        }
      }
    }
  }
}
