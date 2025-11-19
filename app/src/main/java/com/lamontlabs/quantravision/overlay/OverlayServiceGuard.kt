package com.lamontlabs.quantravision.overlay

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import timber.log.Timber

/**
 * Controls whether OverlayService can be started by disabling/enabling the component at OS level.
 * 
 * This prevents Android from auto-restarting the service via:
 * - Quick Settings tile
 * - Pending notification actions
 * - System service revival (START_STICKY)
 * - Any other system-level triggers
 * 
 * ## Usage
 * - Call `disable()` before importing AI model (prevents tap-jacking crashes)
 * - Call `enable()` after model is imported and ready
 */
object OverlayServiceGuard {
    
    /**
     * Completely disable OverlayService at OS level.
     * Service cannot be started by ANY means until re-enabled.
     */
    fun disable(context: Context) {
        try {
            val componentName = ComponentName(context, OverlayService::class.java)
            context.packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            Timber.i("🔒 OverlayService DISABLED at OS level - cannot be started")
        } catch (e: Exception) {
            Timber.e(e, "Failed to disable OverlayService")
        }
    }
    
    /**
     * Re-enable OverlayService at OS level.
     * Service can now be started normally.
     */
    fun enable(context: Context) {
        try {
            val componentName = ComponentName(context, OverlayService::class.java)
            context.packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            Timber.i("🔓 OverlayService ENABLED at OS level - can be started")
        } catch (e: Exception) {
            Timber.e(e, "Failed to enable OverlayService")
        }
    }
    
    /**
     * Check if OverlayService is currently enabled at OS level.
     */
    fun isEnabled(context: Context): Boolean {
        return try {
            val componentName = ComponentName(context, OverlayService::class.java)
            val state = context.packageManager.getComponentEnabledSetting(componentName)
            
            // Default state is ENABLED, so we check for explicit DISABLED
            state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } catch (e: Exception) {
            Timber.e(e, "Failed to check OverlayService state")
            true // Assume enabled on error
        }
    }
}
