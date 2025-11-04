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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.onboarding.OnboardingManager

@Composable
fun SettingsScreen() {
    QuantraVisionTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(Modifier.fillMaxWidth().padding(24.dp)) {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(shadow = CyanGlowShadow),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "General",
                            style = MaterialTheme.typography.titleLarge.copy(shadow = SubtleGlowShadow),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Theme: Follows system (Dark optimized)")
                        Spacer(Modifier.height(8.dp))
                        Text("Overlay opacity: Adjustable in Quick Controls")
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                Spacer(Modifier.height(24.dp))
                
                Text("Lamont Labs", color = MaterialTheme.colorScheme.metallic)
                Text("QuantraVision Overlay • v2.x", color = MaterialTheme.colorScheme.textSecondary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenWithNav(navController: androidx.navigation.NavHostController) {
    val context = LocalContext.current
    val logoPrefs = com.lamontlabs.quantravision.overlay.FloatingLogoPreferences(context)
    var selectedLogoSize by remember { 
        mutableStateOf(logoPrefs.getLogoSize())
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineMedium.copy(shadow = SubtleGlowShadow)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Floating Logo",
                style = MaterialTheme.typography.headlineSmall.copy(shadow = CyanGlowShadow),
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Logo Size",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
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
                            val isSelected = selectedLogoSize == size
                            Button(
                                onClick = { 
                                    logoPrefs.saveLogoSize(size)
                                    selectedLogoSize = size
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.metallic.copy(alpha = 0.3f),
                                    contentColor = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.metallic
                                )
                            ) {
                                Text(label)
                            }
                        }
                    }
                    
                    Text(
                        text = "Logo Opacity",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0.5f to "50%", 0.75f to "75%", 0.85f to "85%", 1.0f to "100%").forEach { (opacity, label) ->
                            OutlinedButton(
                                onClick = { logoPrefs.saveLogoOpacity(opacity) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(label)
                            }
                        }
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            "Show pattern count badge",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = logoPrefs.isBadgeVisible(),
                            onCheckedChange = { logoPrefs.saveBadgeVisibility(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
            
            Text(
                text = "General",
                style = MaterialTheme.typography.headlineSmall.copy(shadow = CyanGlowShadow),
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Theme: Follows system (Dark optimized)")
                    Spacer(Modifier.height(8.dp))
                    Text("Overlay opacity: Adjustable in Quick Controls")
                }
            }
            
            Text(
                text = "Upgrade & Plans",
                style = MaterialTheme.typography.headlineSmall.copy(shadow = CyanGlowShadow),
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                ListItem(
                    headlineContent = { 
                        Text(
                            "View All Pricing Tiers",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.amber
                        ) 
                    },
                    supportingContent = { 
                        Text(
                            "FREE • STARTER $9.99 • STANDARD $24.99 • PRO $49.99",
                            color = MaterialTheme.colorScheme.metallic
                        ) 
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.amber,
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    modifier = Modifier.clickable {
                        navController.navigate("paywall")
                    }
                )
            }
            
            Text(
                text = "Help & Learning",
                style = MaterialTheme.typography.headlineSmall.copy(shadow = CyanGlowShadow),
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                ListItem(
                    headlineContent = { 
                        Text(
                            "Replay Onboarding Tour",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.amber
                        ) 
                    },
                    supportingContent = { Text("Review the app tutorial again") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.amber,
                            modifier = Modifier.size(28.dp)
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
            
            Text(
                text = "Legal & Privacy",
                style = MaterialTheme.typography.headlineSmall.copy(shadow = CyanGlowShadow),
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Privacy Policy") },
                        supportingContent = { Text("How we handle your data") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        modifier = Modifier.clickable {
                            navController.navigate("legal/privacy")
                        }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                    
                    ListItem(
                        headlineContent = { Text("Terms of Use") },
                        supportingContent = { Text("Conditions for using the app") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        modifier = Modifier.clickable {
                            navController.navigate("legal/terms")
                        }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                    
                    ListItem(
                        headlineContent = { Text("Disclaimer") },
                        supportingContent = { Text("Educational purposes only") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        modifier = Modifier.clickable {
                            navController.navigate("legal/disclaimer")
                        }
                    )
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            
            Text("Lamont Labs", color = MaterialTheme.colorScheme.metallic)
            Text("QuantraVision Overlay • v2.x", color = MaterialTheme.colorScheme.textSecondary)
        }
    }
}
