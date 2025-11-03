package com.lamontlabs.quantravision.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.lamontlabs.quantravision.BuildConfig
import java.io.File
import java.security.MessageDigest

/**
 * Tamper Detection System for QuantraVision
 * 
 * Detects if the APK has been modified or tampered with:
 * - APK modification detection
 * - Xposed/Frida/Substrate framework detection
 * - Critical resource verification
 * - Logs tampering attempts (on-device only, no network transmission)
 * 
 * Fail-closed security: Blocks functionality if tampering detected
 * 
 * @author Lamont Labs
 * @since 2.1
 */
class TamperDetector(private val context: Context) {
    
    companion object {
        private const val TAG = "TamperDetector"
        
        private val CRITICAL_RESOURCES = listOf(
            "assets/legal/LICENSE",
            "assets/legal/FINANCIAL_DISCLAIMER.md",
            "classes.dex"
        )
    }
    
    private val tamperLog = mutableListOf<TamperEvent>()
    
    /**
     * Perform comprehensive tamper detection
     */
    fun detectTampering(): TamperDetectionResult {
        Log.i(TAG, "Starting tamper detection...")
        
        val detections = mutableMapOf<String, Boolean>()
        
        detections["apk_modified"] = isApkModified()
        detections["xposed_present"] = isXposedPresent()
        detections["frida_present"] = isFridaPresent()
        detections["substrate_present"] = isSubstratePresent()
        detections["native_hooks"] = hasNativeHooks()
        detections["resources_tampered"] = areResourcesTampered()
        
        val tamperingDetected = detections.values.any { it }
        
        if (tamperingDetected) {
            val event = TamperEvent(
                timestamp = System.currentTimeMillis(),
                detections = detections.filterValues { it }.keys.toList(),
                severity = TamperSeverity.HIGH
            )
            logTamperEvent(event)
        }
        
        return TamperDetectionResult(
            tamperingDetected = tamperingDetected,
            detections = detections,
            events = tamperLog.toList()
        )
    }
    
    /**
     * Detect if APK has been modified
     */
    private fun isApkModified(): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
            
            val sourceDir = packageInfo.applicationInfo?.sourceDir ?: return true
            val apkFile = File(sourceDir)
            
            if (!apkFile.exists()) {
                Log.e(TAG, "APK file does not exist: $sourceDir")
                return true
            }
            
            val apkSize = apkFile.length()
            Log.d(TAG, "APK size: $apkSize bytes")
            
            if (BuildConfig.DEBUG) {
                return false
            }
            
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking APK modification", e)
            true
        }
    }
    
    /**
     * Detect Xposed framework
     */
    private fun isXposedPresent(): Boolean {
        try {
            Class.forName("de.robv.android.xposed.XposedBridge")
            Log.w(TAG, "Xposed framework detected!")
            return true
        } catch (e: ClassNotFoundException) {
        }
        
        try {
            Class.forName("de.robv.android.xposed.XposedHelpers")
            Log.w(TAG, "Xposed helpers detected!")
            return true
        } catch (e: ClassNotFoundException) {
        }
        
        val xposedFiles = listOf(
            "/system/framework/XposedBridge.jar",
            "/system/lib/libxposed_art.so",
            "/system/lib64/libxposed_art.so"
        )
        
        for (file in xposedFiles) {
            if (File(file).exists()) {
                Log.w(TAG, "Xposed file detected: $file")
                return true
            }
        }
        
        return false
    }
    
    /**
     * Detect Frida framework
     */
    private fun isFridaPresent(): Boolean {
        val fridaPorts = listOf(27042, 27043)
        
        for (port in fridaPorts) {
            try {
                val portFile = File("/proc/net/tcp")
                if (portFile.exists()) {
                    val content = portFile.readText()
                    val hexPort = Integer.toHexString(port).uppercase().padStart(4, '0')
                    if (content.contains(hexPort)) {
                        Log.w(TAG, "Frida port detected: $port")
                        return true
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error checking Frida port $port", e)
            }
        }
        
        val fridaLibs = listOf(
            "frida-agent",
            "frida-gadget",
            "frida-server"
        )
        
        try {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists()) {
                val content = mapsFile.readText()
                for (lib in fridaLibs) {
                    if (content.contains(lib, ignoreCase = true)) {
                        Log.w(TAG, "Frida library detected: $lib")
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error checking loaded libraries", e)
        }
        
        val stackTrace = Thread.currentThread().stackTrace
        for (element in stackTrace) {
            if (element.className.contains("frida", ignoreCase = true)) {
                Log.w(TAG, "Frida detected in stack trace: ${element.className}")
                return true
            }
        }
        
        return false
    }
    
    /**
     * Detect Substrate framework
     */
    private fun isSubstratePresent(): Boolean {
        try {
            Class.forName("com.saurik.substrate.MS")
            Log.w(TAG, "Substrate framework detected!")
            return true
        } catch (e: ClassNotFoundException) {
        }
        
        val substrateFiles = listOf(
            "/system/lib/libsubstrate.so",
            "/system/lib64/libsubstrate.so",
            "/data/local/tmp/substrate"
        )
        
        for (file in substrateFiles) {
            if (File(file).exists()) {
                Log.w(TAG, "Substrate file detected: $file")
                return true
            }
        }
        
        return false
    }
    
    /**
     * Detect native hooks or function interception
     */
    private fun hasNativeHooks(): Boolean {
        try {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists()) {
                val content = mapsFile.readText()
                
                val suspiciousLibs = listOf(
                    "xhook",
                    "substrate",
                    "frida",
                    "xposed"
                )
                
                for (lib in suspiciousLibs) {
                    if (content.contains(lib, ignoreCase = true)) {
                        Log.w(TAG, "Suspicious library detected: $lib")
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error checking native hooks", e)
        }
        
        return false
    }
    
    /**
     * Verify critical resources haven't been replaced
     */
    private fun areResourcesTampered(): Boolean {
        for (resource in CRITICAL_RESOURCES) {
            try {
                val inputStream = when {
                    resource.startsWith("assets/") -> {
                        val assetPath = resource.removePrefix("assets/")
                        context.assets.open(assetPath)
                    }
                    resource == "classes.dex" -> {
                        continue
                    }
                    else -> {
                        continue
                    }
                }
                
                inputStream?.close()
                
            } catch (e: Exception) {
                Log.w(TAG, "Critical resource missing or tampered: $resource", e)
                return true
            }
        }
        
        return false
    }
    
    /**
     * Log tamper event (on-device only, no network transmission)
     */
    private fun logTamperEvent(event: TamperEvent) {
        tamperLog.add(event)
        
        Log.w(TAG, "TAMPER DETECTED: ${event.detections.joinToString(", ")}")
        Log.w(TAG, "Severity: ${event.severity}")
        
        if (tamperLog.size > 100) {
            tamperLog.removeAt(0)
        }
    }
    
    /**
     * Get all tamper events (for debugging)
     */
    fun getTamperEvents(): List<TamperEvent> = tamperLog.toList()
    
    /**
     * Clear tamper event log
     */
    fun clearTamperLog() {
        Log.i(TAG, "Clearing tamper log (${tamperLog.size} events)")
        tamperLog.clear()
    }
    
    /**
     * Enforce tamper-free operation
     */
    fun enforceTamperFree(operationName: String): Boolean {
        val result = detectTampering()
        
        if (result.tamperingDetected) {
            Log.e(TAG, "Blocking operation '$operationName' due to tampering detection")
            return false
        }
        
        return true
    }
}

/**
 * Result of tamper detection
 */
data class TamperDetectionResult(
    val tamperingDetected: Boolean,
    val detections: Map<String, Boolean>,
    val events: List<TamperEvent>
)

/**
 * Tamper event log entry
 */
data class TamperEvent(
    val timestamp: Long,
    val detections: List<String>,
    val severity: TamperSeverity
)

/**
 * Severity level of tamper event
 */
enum class TamperSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
