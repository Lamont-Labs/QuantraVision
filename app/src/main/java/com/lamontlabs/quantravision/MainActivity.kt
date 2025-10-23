package com.lamontlabs.quantravision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.lamontlabs.quantravision.boot.SafeBoot
import com.lamontlabs.quantravision.ui.DashboardScreen
import com.lamontlabs.quantravision.ui.OverlayHUD
import com.lamontlabs.quantravision.ui.TemplateManagerScreen

/**
 * MainActivity
 * Entry point. Runs SafeBoot before dashboard.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ok = SafeBoot.run(this)
        setContent {
            MaterialTheme {
                if (ok) {
                    var screen by remember { mutableStateOf("dashboard") }
                    when (screen) {
                        "dashboard" -> DashboardScreen(
                            context = this@MainActivity,
                            onStartScan = { screen = "overlay" },
                            onReview = {},
                            onTutorials = {},
                            onSettings = {},
                            onTemplates = { screen = "templates" }
                        )
                        "templates" -> TemplateManagerScreen(this@MainActivity) { screen = "dashboard" }
                        "overlay" -> OverlayHUD(this@MainActivity)
                    }
                } else {
                    InvalidLicenseScreen()
                }
            }
        }
    }
}

@Composable
private fun InvalidLicenseScreen() {
    Surface {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("License check failed — overlay locked", style = MaterialTheme.typography.titleMedium)
        }
    }
}
