package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
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
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(20.dp))
                Text("Theme: Follows system (Dark optimized)", fontWeight = FontWeight.Bold)
                Text("Overlay opacity: Adjustable in Quick Controls", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(28.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                Text("Lamont Labs", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("QuantraVision Overlay • v2.x", fontWeight = FontWeight.Bold)
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
        Column(Modifier.fillMaxWidth().padding(padding).padding(20.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        text = "Floating Logo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "Logo Size",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(
                            com.lamontlabs.quantravision.overlay.FloatingLogoPreferences.LogoSize.SMALL to "Small",
                            com.lamontlabs.quantravision.overlay.FloatingLogoPreferences.LogoSize.MEDIUM to "Medium",
                            com.lamontlabs.quantravision.overlay.FloatingLogoPreferences.LogoSize.LARGE to "Large"
                        ).forEach { (size, label) ->
                            Button(
                                onClick = { logoPrefs.saveLogoSize(size) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(label, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    
                    Text(
                        text = "Logo Opacity",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(0.5f to "50%", 0.75f to "75%", 0.85f to "85%", 1.0f to "100%").forEach { (opacity, label) ->
                            Button(
                                onClick = { logoPrefs.saveLogoOpacity(opacity) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(label, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(20.dp))
            
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text("Show pattern count badge", fontWeight = FontWeight.Bold)
                        Switch(
                            checked = logoPrefs.isBadgeVisible(),
                            onCheckedChange = { logoPrefs.saveBadgeVisibility(it) }
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(28.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        text = "General",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text("Theme: Follows system (Dark optimized)", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text("Overlay opacity: Adjustable in Quick Controls", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(28.dp))
            
            Text(
                text = "Help & Learning",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                ListItem(
                    headlineContent = { Text("Replay Onboarding Tour", fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("Review the app tutorial again", fontWeight = FontWeight.Bold) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
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
            }
            
            Spacer(Modifier.height(28.dp))
            
            Text(
                text = "Legal & Privacy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("How we handle your data", fontWeight = FontWeight.Bold) },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                        },
                        modifier = Modifier.clickable {
                            navController.navigate("legal/privacy")
                        }
                    )
                    
                    HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                    
                    ListItem(
                        headlineContent = { Text("Terms of Use", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Conditions for using the app", fontWeight = FontWeight.Bold) },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                        },
                        modifier = Modifier.clickable {
                            navController.navigate("legal/terms")
                        }
                    )
                    
                    HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                    
                    ListItem(
                        headlineContent = { Text("Disclaimer", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Educational purposes only", fontWeight = FontWeight.Bold) },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                        },
                        modifier = Modifier.clickable {
                            navController.navigate("legal/disclaimer")
                        }
                    )
                }
            }
            
            Spacer(Modifier.height(28.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Text("Lamont Labs", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("QuantraVision Overlay • v2.x", fontWeight = FontWeight.Bold)
        }
    }
}
