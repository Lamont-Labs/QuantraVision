package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.alerts.AlertManager
import com.lamontlabs.quantravision.onboarding.OnboardingManager

/**
 * Settings Screen - Clean configuration interface
 * Streamlined design matching home screen style
 */
@Composable
fun SettingsScreen() {
    // Delegate to full implementation with no navigation
    SettingsScreenWithNav(navController = null, onClearDatabase = null)
}

@Composable
fun SettingsScreenWithNav(
    navController: androidx.navigation.NavHostController? = null,
    onClearDatabase: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val alertManager = remember { AlertManager.getInstance(context) }
    val logoPrefs = com.lamontlabs.quantravision.overlay.FloatingLogoPreferences(context)
    
    var voiceEnabled by remember { mutableStateOf(alertManager.isVoiceEnabled()) }
    var hapticEnabled by remember { mutableStateOf(alertManager.isHapticEnabled()) }
    var selectedSize by remember { mutableStateOf(logoPrefs.getLogoSize()) }
    var selectedOpacity by remember { mutableStateOf(logoPrefs.getLogoOpacity()) }
    var badgeVisible by remember { mutableStateOf(logoPrefs.isBadgeVisible()) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Static brand background
        StaticBrandBackground(modifier = Modifier.fillMaxSize())
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ALERT SETTINGS
            item {
                NeonText(
                    text = "ALERT SETTINGS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    glowColor = NeonCyan,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            item {
                GlassMorphicCard(backgroundColor = Color(0xFF0D1219).copy(alpha = 0.7f)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = NeonCyan
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Voice Announcements", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(
                                        "Announce detected patterns",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            NeonSwitch(
                                checked = voiceEnabled,
                                onCheckedChange = { enabled ->
                                    voiceEnabled = enabled
                                    alertManager.setVoiceEnabled(enabled)
                                }
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Vibration,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = NeonCyan
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Haptic Feedback", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(
                                        "Vibrate on pattern detection",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            NeonSwitch(
                                checked = hapticEnabled,
                                onCheckedChange = { enabled ->
                                    hapticEnabled = enabled
                                    alertManager.setHapticEnabled(enabled)
                                }
                            )
                        }
                    }
                }
            }
            
            // FLOATING LOGO
            item {
                NeonText(
                    text = "FLOATING LOGO",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    glowColor = NeonCyan,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            item {
                GlassMorphicCard(backgroundColor = Color(0xFF0D1219).copy(alpha = 0.7f)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        // Logo Size
                        Column {
                            Text(
                                text = "Logo Size",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
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
                                    val isSelected = selectedSize == size
                                    NeonBorderButton(
                                        onClick = { 
                                            selectedSize = size
                                            logoPrefs.saveLogoSize(size)
                                        },
                                        isSelected = isSelected,
                                        modifier = Modifier.weight(1f),
                                        glowColor = NeonCyan,
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Logo Opacity
                        Column {
                            Text(
                                text = "Logo Opacity",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(0.5f to "50%", 0.75f to "75%", 0.85f to "85%", 1.0f to "100%").forEach { (opacity, label) ->
                                    val isSelected = (selectedOpacity - opacity).let { kotlin.math.abs(it) < 0.01f }
                                    ShimmerButton(
                                        onClick = { 
                                            selectedOpacity = opacity
                                            logoPrefs.saveLogoOpacity(opacity)
                                        },
                                        isSelected = isSelected,
                                        modifier = Modifier.weight(1f),
                                        enableShimmer = true,
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Badge Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show pattern count badge", fontWeight = FontWeight.Bold, color = Color.White)
                            NeonSwitch(
                                checked = badgeVisible,
                                onCheckedChange = { 
                                    badgeVisible = it
                                    logoPrefs.saveBadgeVisibility(it) 
                                }
                            )
                        }
                    }
                }
            }
            
            // GENERAL
            item {
                NeonText(
                    text = "GENERAL",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    glowColor = NeonCyan,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            item {
                GlassMorphicCard(backgroundColor = Color(0xFF0D1219).copy(alpha = 0.7f)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Theme: Follows system (Dark optimized)", fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(12.dp))
                        Text("Overlay opacity: Adjustable in Quick Controls", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            
            // HELP & LEARNING
            item {
                NeonText(
                    text = "HELP & LEARNING",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    glowColor = NeonCyan,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            item {
                MetallicButton(
                    onClick = {
                        val onboardingManager = OnboardingManager.getInstance(context)
                        onboardingManager.resetOnboarding()
                        android.widget.Toast.makeText(
                            context,
                            "Onboarding reset. Restart the app to view the tour.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Replay,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Replay Onboarding Tour",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Review the app tutorial again",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
            
            // DEVELOPER SECTION (conditional)
            if (onClearDatabase != null) {
                item {
                    NeonText(
                        text = "DEVELOPER",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        glowColor = Color(0xFFFF4444),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                item {
                    MenuItemCard(
                        title = "Clear Database",
                        subtitle = "Delete all pattern detections",
                        onClick = { onClearDatabase() },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF4444),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // LEGAL & PRIVACY (conditional)
            if (navController != null) {
                item {
                    NeonText(
                        text = "LEGAL & PRIVACY",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        glowColor = NeonCyan,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MenuItemCard(
                            title = "Privacy Policy",
                            subtitle = "How we handle your data",
                            onClick = { navController.navigate("legal/privacy") },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        MenuItemCard(
                            title = "Terms of Use",
                            subtitle = "Conditions for using the app",
                            onClick = { navController.navigate("legal/terms") },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        MenuItemCard(
                            title = "Disclaimer",
                            subtitle = "Educational purposes only",
                            onClick = { navController.navigate("legal/disclaimer") },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // FOOTER
            item {
                GlassMorphicCard(backgroundColor = Color(0xFF0D1219).copy(alpha = 0.7f)) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text("Lamont Labs", color = NeonCyan, fontWeight = FontWeight.Bold)
                            Text("QuantraVision Overlay • v2.x", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
