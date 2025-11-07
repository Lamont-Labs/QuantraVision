package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Composable
fun AlertSettingsCard() {
    val context = LocalContext.current
    val alertManager = remember { AlertManager.getInstance(context) }
    var voiceEnabled by remember { mutableStateOf(alertManager.isVoiceEnabled()) }
    var hapticEnabled by remember { mutableStateOf(alertManager.isHapticEnabled()) }
    
    GlassMorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = "Alert Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    GlowingIcon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        size = 28.dp,
                        glowColor = NeonCyan,
                        glowIntensity = 0.6f
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Voice Announcements", fontWeight = FontWeight.Bold)
                        Text(
                            "Announce detected patterns",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            
            Spacer(Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    GlowingIcon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = null,
                        size = 28.dp,
                        glowColor = NeonCyan,
                        glowIntensity = 0.6f
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Haptic Feedback", fontWeight = FontWeight.Bold)
                        Text(
                            "Vibrate on pattern detection",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenWithNav(
    navController: androidx.navigation.NavHostController? = null,
    onClearDatabase: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val logoPrefs = com.lamontlabs.quantravision.overlay.FloatingLogoPreferences(context)
    var selectedSize by remember { mutableStateOf(logoPrefs.getLogoSize()) }
    var selectedOpacity by remember { mutableStateOf(logoPrefs.getLogoOpacity()) }
    var badgeVisible by remember { mutableStateOf(logoPrefs.isBadgeVisible()) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Quantum Grid Background - lower opacity for readability
        QuantumGridBackground(
            modifier = Modifier.fillMaxSize(),
            gridColor = NeonCyan.copy(alpha = 0.08f),
            animateGrid = true
        )
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (navController != null) {
                    TopAppBar(
                        title = { Text("Settings") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                } else {
                    TopAppBar(
                        title = { Text("Settings") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            }
        ) { padding ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                AlertSettingsCard()
                
                Spacer(Modifier.height(20.dp))
                
                // Floating Logo Settings - Enhanced MetallicCard with shimmer
                MetallicCard(
                    modifier = Modifier.fillMaxWidth(),
                    enableShimmer = true
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
                        
                        Spacer(Modifier.height(20.dp))
                        
                        Text(
                            text = "Logo Opacity",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
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
                        
                        Spacer(Modifier.height(20.dp))
                
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show pattern count badge", fontWeight = FontWeight.Bold)
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
            
                
                Spacer(Modifier.height(28.dp))
                
                // General Settings - GlassMorphicCard for non-critical settings
                GlassMorphicCard(
                    modifier = Modifier.fillMaxWidth()
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
                
                // Help & Learning - MetallicCard with shimmer and GlowingIcon
                MetallicCard(
                    modifier = Modifier.fillMaxWidth(),
                    enableShimmer = true,
                    onClick = {
                        val onboardingManager = OnboardingManager.getInstance(context)
                        onboardingManager.resetOnboarding()
                        android.widget.Toast.makeText(
                            context,
                            "Onboarding reset. Restart the app to view the tour.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            GlowingIcon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = null,
                                size = 32.dp,
                                glowColor = NeonCyan,
                                glowIntensity = 0.7f
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Replay Onboarding Tour", 
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    "Review the app tutorial again",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        // Neon chevron
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                }
            
                
                Spacer(Modifier.height(28.dp))
                
                // Developer section - Dark GlassMorphicCard with technical aesthetic
                if (onClearDatabase != null) {
                    Text(
                        text = "Developer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    GlassMorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        isDark = true,
                        borderColor = Color(0xFFFF4444),
                        onClick = { onClearDatabase() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                GlowingIcon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    size = 32.dp,
                                    glowColor = Color(0xFFFF4444),
                                    iconColor = Color(0xFFFF4444),
                                    glowIntensity = 0.8f
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        "Clear Database", 
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        "Delete all pattern detections",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFFFF4444),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(28.dp))
                }
            
                
                // Legal & Privacy section - Standard GlassMorphicCard
                if (navController != null) {
                    Text(
                        text = "Legal & Privacy",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    GlassMorphicCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Privacy Policy
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate("legal/privacy") }
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    GlowingIcon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        size = 32.dp,
                                        glowColor = NeonCyan,
                                        glowIntensity = 0.6f
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            "Privacy Policy", 
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            "How we handle your data",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = NeonCyan.copy(alpha = 0.7f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                            
                            // Terms of Use
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate("legal/terms") }
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    GlowingIcon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        size = 32.dp,
                                        glowColor = NeonCyan,
                                        glowIntensity = 0.6f
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            "Terms of Use", 
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            "Conditions for using the app",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = NeonCyan.copy(alpha = 0.7f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                            
                            // Disclaimer
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate("legal/disclaimer") }
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    GlowingIcon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        size = 32.dp,
                                        glowColor = NeonCyan,
                                        glowIntensity = 0.6f
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            "Disclaimer", 
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            "Educational purposes only",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = NeonCyan.copy(alpha = 0.7f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(28.dp))
                }
                
                // Footer
                Spacer(Modifier.height(28.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                Text("Lamont Labs", color = NeonCyan, fontWeight = FontWeight.Bold)
                Text("QuantraVision Overlay • v2.x", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
