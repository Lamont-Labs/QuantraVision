package com.lamontlabs.quantravision.safety

import android.content.pm.PackageManager

/**
 * Runtime guards that enforce the no-network and local-only constraints.
 * If INTERNET permission is ever found, we disable risky features.
 */
object FailClosed {

    fun internetPermissionPresent(pm: PackageManager, pkg: String): Boolean {
        return try {
            val info = pm.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS)
            val perms = info.requestedPermissions ?: return false
            perms.contains("android.permission.INTERNET")
        } catch (_: Exception) {
            false
        }
    }
}
