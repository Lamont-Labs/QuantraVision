package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.system.PowerHint
import com.lamontlabs.quantravision.system.ThermalGuard
import com.lamontlabs.quantravision.security.LicenseVerifier

/**
 * OverlayHUD
 * Displays live safety and license status indicators in overlay mode.
 */
@Composable
fun OverlayHUD(context: android.content.Context) {
    var powerLow by remember { mutableStateOf(PowerHint.isLowPower()) }
    var throttled by remember { mutableStateOf(ThermalGuard.isThrottled()) }
    val licenseOk = remember { LicenseVerifier.verify(context).valid }

    Row(
        Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (throttled) {
            Icon(Icons.Default.Whatshot, contentDescription = "Thermal throttle", tint = MaterialTheme.colorScheme.error)
            Text("Thermal", style = MaterialTheme.typography.labelSmall)
        }
        if (powerLow) {
            Icon(Icons.Default.BatteryAlert, contentDescription = "Low power", tint = MaterialTheme.colorScheme.error)
            Text("Power", style = MaterialTheme.typography.labelSmall)
        }
        if (!licenseOk) {
            Icon(Icons.Default.Lock, contentDescription = "License invalid", tint = MaterialTheme.colorScheme.error)
            Text("License", style = MaterialTheme.typography.labelSmall)
        }
        if (!throttled && !powerLow && licenseOk) {
            Icon(Icons.Default.Verified, contentDescription = "System healthy", tint = MaterialTheme.colorScheme.primary)
            Text("OK", style = MaterialTheme.typography.labelSmall)
        }
    }
}
