package com.lamontlabs.quantravision.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * Centralized permission orchestrator that requests critical permissions at app launch.
 * 
 * Permissions requested in order:
 * 1. POST_NOTIFICATIONS (Android 13+) - Required to show pattern detection notifications
 * 2. SYSTEM_ALERT_WINDOW - Required to show floating overlay button
 * 
 * Note: MediaProjection permission is NOT requested here because it must be requested
 * when the user actually starts the scanner (ScanViewModel handles this).
 * 
 * Once all permissions are granted, calls onAllPermissionsGranted()
 */
@Composable
fun PermissionOrchestrator(
    onAllPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    
    // Track which permissions are granted
    var notificationPermissionGranted by remember { 
        mutableStateOf(hasNotificationPermission(context))
    }
    var overlayPermissionGranted by remember { 
        mutableStateOf(hasOverlayPermission(context))
    }
    
    // Track current permission being requested
    var currentStep by remember { mutableStateOf(determineCurrentStep(
        notificationPermissionGranted,
        overlayPermissionGranted
    )) }
    
    // Notification permission launcher
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermissionGranted = isGranted
        currentStep = determineCurrentStep(notificationPermissionGranted, overlayPermissionGranted)
    }
    
    // Overlay permission launcher
    val overlayLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        overlayPermissionGranted = hasOverlayPermission(context)
        currentStep = determineCurrentStep(notificationPermissionGranted, overlayPermissionGranted)
    }
    
    // Check if all permissions granted
    LaunchedEffect(notificationPermissionGranted, overlayPermissionGranted) {
        if (notificationPermissionGranted && overlayPermissionGranted) {
            onAllPermissionsGranted()
        }
    }
    
    // Show permission request UI
    when (currentStep) {
        PermissionStep.NOTIFICATION -> {
            PermissionRequestScreen(
                title = "Enable Notifications",
                description = "QuantraVision needs notification permission to alert you when patterns are detected on your charts.",
                icon = "🔔",
                stepNumber = 1,
                totalSteps = 2,
                onGrantClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        notificationPermissionGranted = true
                    }
                },
                onSkipClick = {
                    notificationPermissionGranted = true // Allow skip - notifications still work on older Android
                }
            )
        }
        PermissionStep.OVERLAY -> {
            PermissionRequestScreen(
                title = "Enable Screen Overlay",
                description = "The floating Q button appears over other apps so you can scan charts from any trading platform.",
                icon = "🔘",
                stepNumber = 2,
                totalSteps = 2,
                onGrantClick = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    overlayLauncher.launch(intent)
                },
                onSkipClick = null // Overlay is required, can't skip
            )
        }
        PermissionStep.COMPLETE -> {
            // All permissions granted - this triggers onAllPermissionsGranted via LaunchedEffect
        }
    }
}

@Composable
private fun PermissionRequestScreen(
    title: String,
    description: String,
    icon: String,
    stepNumber: Int,
    totalSteps: Int,
    onGrantClick: () -> Unit,
    onSkipClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.displayLarge
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onGrantClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Permission")
            }
            
            if (onSkipClick != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onSkipClick) {
                    Text("Skip (Not Recommended)")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Step $stepNumber of $totalSteps",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private enum class PermissionStep {
    NOTIFICATION,
    OVERLAY,
    COMPLETE
}

private fun determineCurrentStep(
    notificationGranted: Boolean,
    overlayGranted: Boolean
): PermissionStep {
    return when {
        !notificationGranted -> PermissionStep.NOTIFICATION
        !overlayGranted -> PermissionStep.OVERLAY
        else -> PermissionStep.COMPLETE
    }
}

private fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // Not required on older Android versions
    }
}

private fun hasOverlayPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(context)
    } else {
        true // Not required on older Android versions
    }
}
