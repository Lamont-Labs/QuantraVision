package com.lamontlabs.quantravision.security

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Log
import com.lamontlabs.quantravision.BuildConfig
import java.security.MessageDigest

/**
 * Enhanced Integrity Checker for QuantraVision
 * 
 * Strengthened Play Integrity checks with more frequent verification:
 * - On app startup
 * - Before purchases
 * - Before detection operations
 * 
 * Verifies:
 * - App signature hasn't changed
 * - APK hasn't been tampered with
 * - No debugging/rooting/hooking frameworks
 * - Blocks functionality if integrity fails (fail-closed security)
 * 
 * @author Lamont Labs
 * @since 2.1
 */
class IntegrityChecker(private val context: Context) {
    
    companion object {
        private const val TAG = "IntegrityChecker"
        
        /**
         * Expected release signature SHA-256 hash.
         * 
         * IMPORTANT: Replace with your actual release key signature hash before publishing!
         * 
         * To get your release signature hash:
         * 1. Build a signed release APK with your release keystore
         * 2. Run: keytool -list -v -keystore /path/to/your/release.keystore -alias your_key_alias
         * 3. Copy the SHA-256 certificate fingerprint (remove colons, lowercase)
         * 4. Replace PLACEHOLDER_SIGNATURE_HASH below with that value
         * 
         * For debug builds, signature verification is automatically skipped.
         */
        private const val EXPECTED_SIGNATURE_HASH = "PLACEHOLDER_SIGNATURE_HASH"
        
        const val INTEGRITY_CHECK_INTERVAL_MS = 300000L
    }
    
    private var lastIntegrityCheck: Long = 0
    private var lastIntegrityResult: IntegrityResult? = null
    
    /**
     * Comprehensive integrity check - verify all security aspects
     */
    fun performIntegrityCheck(force: Boolean = false): IntegrityResult {
        val currentTime = System.currentTimeMillis()
        
        if (!force && currentTime - lastIntegrityCheck < INTEGRITY_CHECK_INTERVAL_MS) {
            lastIntegrityResult?.let { 
                Log.d(TAG, "Using cached integrity result (${it.passed})")
                return it 
            }
        }
        
        Log.i(TAG, "Performing comprehensive integrity check...")
        
        val checks = mutableMapOf<String, Boolean>()
        
        checks["signature_verification"] = verifyAppSignature()
        checks["debug_detection"] = !isDebuggerAttached()
        checks["root_detection"] = !isDeviceRooted()
        checks["hooking_detection"] = !isHookingFrameworkPresent()
        checks["installer_verification"] = verifyInstaller()
        checks["build_integrity"] = verifyBuildIntegrity()
        
        val allChecksPassed = checks.values.all { it }
        
        val result = IntegrityResult(
            passed = allChecksPassed,
            timestamp = currentTime,
            checks = checks,
            reason = if (!allChecksPassed) getFailureReason(checks) else null
        )
        
        lastIntegrityCheck = currentTime
        lastIntegrityResult = result
        
        if (!allChecksPassed) {
            Log.w(TAG, "Integrity check FAILED: ${result.reason}")
        } else {
            Log.i(TAG, "Integrity check PASSED")
        }
        
        return result
    }
    
    /**
     * Verify app signature matches expected hash
     */
    private fun verifyAppSignature(): Boolean {
        return try {
            val signatures = getAppSignatures()
            
            if (signatures.isEmpty()) {
                Log.e(TAG, "No signatures found")
                return false
            }
            
            // Debug builds: Skip signature verification (debug keys change frequently)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Signature verification skipped in debug build")
                signatures.forEach { signature ->
                    val signatureHash = computeSignatureHash(signature)
                    Log.d(TAG, "Debug signature hash: $signatureHash")
                }
                return true
            }
            
            // Release builds: Verify signature matches expected hash
            val actualHash = computeSignatureHash(signatures[0])
            Log.d(TAG, "Checking release signature: $actualHash")
            
            // Fail-closed security: If placeholder not replaced, fail in release builds
            if (EXPECTED_SIGNATURE_HASH == "PLACEHOLDER_SIGNATURE_HASH") {
                Log.e(TAG, "SECURITY ERROR: Release signature hash not configured! " +
                           "Replace EXPECTED_SIGNATURE_HASH with your actual release key hash.")
                // In release builds with unconfigured hash, log actual hash for developer
                Log.e(TAG, "Your release signature hash is: $actualHash")
                Log.e(TAG, "Update EXPECTED_SIGNATURE_HASH in IntegrityChecker.kt before publishing")
                return false
            }
            
            // Compare actual signature to expected signature
            val signatureValid = actualHash.equals(EXPECTED_SIGNATURE_HASH, ignoreCase = true)
            
            if (!signatureValid) {
                Log.e(TAG, "SECURITY ALERT: App signature mismatch!")
                Log.e(TAG, "Expected: $EXPECTED_SIGNATURE_HASH")
                Log.e(TAG, "Actual:   $actualHash")
                Log.e(TAG, "APK may have been tampered with or re-signed")
            } else {
                Log.i(TAG, "Signature verification passed")
            }
            
            signatureValid
        } catch (e: Exception) {
            Log.e(TAG, "Signature verification failed", e)
            false
        }
    }
    
    /**
     * Get app signatures (handles API level differences)
     */
    private fun getAppSignatures(): List<Signature> {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.let { signingInfo ->
                    if (signingInfo.hasMultipleSigners()) {
                        signingInfo.apkContentsSigners.toList()
                    } else {
                        signingInfo.signingCertificateHistory.toList()
                    }
                } ?: emptyList()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures?.toList() ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get app signatures", e)
            emptyList()
        }
    }
    
    /**
     * Compute SHA-256 hash of signature
     */
    private fun computeSignatureHash(signature: Signature): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(signature.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Check if debugger is attached
     */
    private fun isDebuggerAttached(): Boolean {
        val isDebuggable = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val debuggerConnected = android.os.Debug.isDebuggerConnected()
        
        if (debuggerConnected) {
            Log.w(TAG, "Debugger is attached!")
        }
        
        return if (BuildConfig.DEBUG) {
            false
        } else {
            isDebuggable || debuggerConnected
        }
    }
    
    /**
     * Detect if device is rooted
     */
    private fun isDeviceRooted(): Boolean {
        val rootIndicators = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        
        for (path in rootIndicators) {
            if (java.io.File(path).exists()) {
                Log.w(TAG, "Root indicator found: $path")
                return true
            }
        }
        
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            Log.w(TAG, "Test keys detected in build tags")
            return true
        }
        
        return false
    }
    
    /**
     * Detect hooking frameworks (Xposed, Frida, Substrate)
     */
    private fun isHookingFrameworkPresent(): Boolean {
        val hookingIndicators = listOf(
            "de.robv.android.xposed.XposedBridge",
            "de.robv.android.xposed.XposedHelpers",
            "com.saurik.substrate.MS",
            "com.android.internal.util.Predicate"
        )
        
        for (className in hookingIndicators) {
            try {
                Class.forName(className)
                Log.w(TAG, "Hooking framework detected: $className")
                return true
            } catch (e: ClassNotFoundException) {
            }
        }
        
        try {
            val stackTrace = Thread.currentThread().stackTrace
            for (element in stackTrace) {
                val className = element.className
                if (className.contains("frida", ignoreCase = true) ||
                    className.contains("xposed", ignoreCase = true) ||
                    className.contains("substrate", ignoreCase = true)) {
                    Log.w(TAG, "Hooking framework detected in stack trace: $className")
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking stack trace", e)
        }
        
        return false
    }
    
    /**
     * Verify app was installed from Google Play Store
     */
    private fun verifyInstaller(): Boolean {
        val validInstallers = listOf(
            "com.android.vending",
            "com.google.android.feedback"
        )
        
        val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(context.packageName)
        }
        
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Installer verification skipped in debug build (installer: $installer)")
            return true
        }
        
        val isValid = installer in validInstallers
        if (!isValid) {
            Log.w(TAG, "Invalid installer: $installer")
        }
        
        return isValid
    }
    
    /**
     * Verify build integrity (build type, application ID)
     */
    private fun verifyBuildIntegrity(): Boolean {
        if (BuildConfig.APPLICATION_ID != "com.lamontlabs.quantravision") {
            Log.e(TAG, "Application ID mismatch: ${BuildConfig.APPLICATION_ID}")
            return false
        }
        
        return true
    }
    
    /**
     * Get detailed failure reason
     */
    private fun getFailureReason(checks: Map<String, Boolean>): String {
        val failedChecks = checks.filterValues { !it }.keys
        return "Failed checks: ${failedChecks.joinToString(", ")}"
    }
    
    /**
     * Block operation if integrity check fails
     */
    fun enforceIntegrity(operationName: String): Boolean {
        val result = performIntegrityCheck()
        
        if (!result.passed) {
            Log.e(TAG, "Blocking operation '$operationName' due to integrity failure: ${result.reason}")
            return false
        }
        
        return true
    }
}

/**
 * Result of integrity check
 */
data class IntegrityResult(
    val passed: Boolean,
    val timestamp: Long,
    val checks: Map<String, Boolean>,
    val reason: String?
)
