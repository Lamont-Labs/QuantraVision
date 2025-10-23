package com.lamontlabs.quantravision.system

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast

/**
 * PermissionHelper
 * Handles overlay, battery optimization exemption, and settings intents.
 * Storage and billing have no runtime prompts on modern Android.
 */
object PermissionHelper {

    fun hasOverlayPermission(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            Settings.canDrawOverlays(context)
        else true

    fun requestOverlayPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${activity.packageName}")
            )
            activity.startActivity(intent)
        }
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun requestIgnoreBatteryOptimizations(activity: Activity) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                .setData(Uri.parse("package:${activity.packageName}"))
            activity.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(activity, "Open battery settings and exclude QuantraVision", Toast.LENGTH_LONG).show()
        }
    }

    fun requestAll(activity: Activity) {
        requestOverlayPermission(activity)
        if (!isIgnoringBatteryOptimizations(activity)) requestIgnoreBatteryOptimizations(activity)
    }
}
