package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.onboarding.OnboardingManager

@Composable
fun SettingsScreen() {
    QuantraVisionTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Text("Theme: Follows system (Dark optimized)")
                Text("Overlay opacity: Adjustable in Quick Controls")
                Spacer(Modifier.height(24.dp))
                Divider()
                Spacer(Modifier.height(12.dp))
                Text("Lamont Labs", color = MaterialTheme.colorScheme.primary)
                Text("QuantraVision Overlay • v2.x")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenWithNav(onBack: () -> Unit) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxWidth().padding(padding).padding(16.dp)) {
            Text(
                text = "General",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text("Theme: Follows system (Dark optimized)")
            Text("Overlay opacity: Adjustable in Quick Controls")
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                text = "Help & Learning",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            ListItem(
                headlineContent = { Text("Replay Onboarding Tour") },
                supportingContent = { Text("Review the app tutorial again") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null
                    )
                },
                modifier = Modifier.clickable {
                    val onboardingManager = OnboardingManager.getInstance(context)
                    onboardingManager.resetOnboarding()
                    android.widget.Toast.makeText(
                        context,
                        "Onboarding reset. Restart the app to view the tour.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            )
            
            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(12.dp))
            Text("Lamont Labs", color = MaterialTheme.colorScheme.primary)
            Text("QuantraVision Overlay • v2.x")
        }
    }
}
