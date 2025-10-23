package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.settings.UserModeManager

/**
 * ModeSwitchBanner
 * Visible at dashboard top to toggle between Beginner/Pro.
 */
@Composable
fun ModeSwitchBanner(context: Context, onModeChanged: () -> Unit) {
    var mode by remember { mutableStateOf(UserModeManager.get(context)) }
    Card(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (mode == UserModeManager.Mode.BEGINNER)
                MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Mode: ${mode.name}", style = MaterialTheme.typography.bodyLarge)
            Button(
                onClick = {
                    UserModeManager.toggle(context)
                    mode = UserModeManager.get(context)
                    onModeChanged()
                },
                modifier = Modifier.height(36.dp)
            ) { Text("Switch") }
        }
    }
}
