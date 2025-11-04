package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Star
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
fun SettingsScreenWithNav(navController: androidx.navigation.NavHostController) {
    val context = LocalContext.current
    val logoPrefs = com.lamontlabs.quantravision.overlay.FloatingLogoPreferences(context)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxWidth().padding(padding).padding(16.dp)) {
            Text(
                text = "Floating Logo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Text(
                text = "Logo Size",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    com.lamontlabs.quantravision.overlay.FloatingLogoPreferences.LogoSize.SMALL to "Small",
                    com.lamontlabs.quantravision.overlay.FloatingLogoPreferences.LogoSize.MEDIUM to "Medium",
                    com.lamontlabs.quantravision.overlay.FloatingLogoPreferences.LogoSize.LARGE to "Large"
                ).forEach { (size, label) ->
                    Button(
                        onClick = { logoPrefs.saveLogoSize(size) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label)
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                text = "Logo Opacity",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0.5f to "50%", 0.75f to "75%", 0.85f to "85%", 1.0f to "100%").forEach { (opacity, label) ->
                    Button(
                        onClick = { logoPrefs.saveLogoOpacity(opacity) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label)
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Show pattern count badge")
                Switch(
                    checked = logoPrefs.isBadgeVisible(),
                    onCheckedChange = { logoPrefs.saveBadgeVisibility(it) }
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
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
                text = "Upgrade & Plans",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                ListItem(
                    headlineContent = { Text("View All Pricing Tiers") },
                    supportingContent = { Text("FREE • STARTER $9.99 • STANDARD $24.99 • PRO $49.99") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.clickable {
                        navController.navigate("paywall")
                    }
                )
            }
            
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
            
            Text(
                text = "Legal & Privacy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            ListItem(
                headlineContent = { Text("Privacy Policy") },
                supportingContent = { Text("How we handle your data") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null
                    )
                },
                modifier = Modifier.clickable {
                    navController.navigate("legal/privacy")
                }
            )
            
            ListItem(
                headlineContent = { Text("Terms of Use") },
                supportingContent = { Text("Conditions for using the app") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null
                    )
                },
                modifier = Modifier.clickable {
                    navController.navigate("legal/terms")
                }
            )
            
            ListItem(
                headlineContent = { Text("Disclaimer") },
                supportingContent = { Text("Educational purposes only") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null
                    )
                },
                modifier = Modifier.clickable {
                    navController.navigate("legal/disclaimer")
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
