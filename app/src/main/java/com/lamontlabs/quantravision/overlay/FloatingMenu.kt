package com.lamontlabs.quantravision.overlay

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import com.lamontlabs.quantravision.MainActivity
import com.lamontlabs.quantravision.ui.QuantraVisionTheme

class FloatingMenu(
    private val context: Context,
    private val windowManager: WindowManager,
    private val onStopDetection: () -> Unit
) {
    private val menuView: ComposeView
    private var isMenuVisible = mutableStateOf(false)
    private var isAdded = false
    
    private val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.FILL
    }

    init {
        menuView = ComposeView(context).apply {
            setContent {
                QuantraVisionTheme {
                    QuickActionsMenu(
                        visible = isMenuVisible.value,
                        onDismiss = { hide() },
                        actions = createActions()
                    )
                }
            }
        }
    }

    private fun createActions(): List<QuickAction> {
        return listOf(
            QuickAction(
                icon = Icons.Default.Search,
                title = "Scan Now",
                subtitle = "Trigger immediate pattern scan",
                onClick = {
                    hide()
                }
            ),
            QuickAction(
                icon = Icons.Default.Dashboard,
                title = "Dashboard",
                subtitle = "Open full app dashboard",
                onClick = {
                    openMainApp()
                    hide()
                }
            ),
            QuickAction(
                icon = Icons.Default.Notifications,
                title = "Alerts",
                subtitle = getAlertsStatus(),
                onClick = {
                    toggleAlerts()
                }
            ),
            QuickAction(
                icon = Icons.Default.School,
                title = "Learning Stats",
                subtitle = "View pattern learning progress",
                onClick = {
                    openMainApp()
                    hide()
                }
            ),
            QuickAction(
                icon = Icons.Default.Settings,
                title = "Settings",
                subtitle = "Configure overlay preferences",
                onClick = {
                    openMainApp()
                    hide()
                }
            ),
            QuickAction(
                icon = Icons.Default.Stop,
                title = "Stop Detection",
                subtitle = "Stop overlay and return to app",
                onClick = {
                    hide()
                    onStopDetection()
                }
            )
        )
    }

    private fun getAlertsStatus(): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val voiceEnabled = prefs.getBoolean("voice_alerts_enabled", true)
        val hapticEnabled = prefs.getBoolean("haptic_alerts_enabled", true)
        
        return when {
            voiceEnabled && hapticEnabled -> "Voice & Haptic ON"
            voiceEnabled -> "Voice ON, Haptic OFF"
            hapticEnabled -> "Voice OFF, Haptic ON"
            else -> "All alerts OFF"
        }
    }

    private fun toggleAlerts() {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val currentVoice = prefs.getBoolean("voice_alerts_enabled", true)
        val currentHaptic = prefs.getBoolean("haptic_alerts_enabled", true)
        
        val allEnabled = currentVoice && currentHaptic
        
        prefs.edit()
            .putBoolean("voice_alerts_enabled", !allEnabled)
            .putBoolean("haptic_alerts_enabled", !allEnabled)
            .apply()
        
        hide()
    }

    private fun openMainApp() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(intent)
    }

    fun show() {
        if (!isAdded) {
            try {
                windowManager.addView(menuView, params)
                isAdded = true
            } catch (e: Exception) {
                android.util.Log.e("FloatingMenu", "Failed to add menu view", e)
            }
        }
        isMenuVisible.value = true
    }

    fun hide() {
        isMenuVisible.value = false
    }

    fun cleanup() {
        hide()
        if (isAdded) {
            try {
                windowManager.removeView(menuView)
                isAdded = false
            } catch (e: Exception) {
                android.util.Log.e("FloatingMenu", "Failed to remove menu view", e)
            }
        }
    }
}
