package com.lamontlabs.quantravision.devbot.diagnostics

import android.content.Context
import com.lamontlabs.quantravision.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StartupEvent(
    val timestamp: Long,
    val timestampReadable: String,
    val component: String,
    val event: String,
    val status: StartupStatus,
    val details: String? = null,
    val error: String? = null
)

enum class StartupStatus {
    STARTED,
    IN_PROGRESS,
    SUCCESS,
    WARNING,
    FAILED
}

data class StartupTimeline(
    val appLaunchTime: Long,
    val buildFingerprint: String,
    val events: List<StartupEvent>,
    val totalDuration: Long,
    val failedComponents: List<String>,
    val warningComponents: List<String>
)

object StartupDiagnosticCollector {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val events = mutableListOf<StartupEvent>()
    private var appLaunchTime: Long = System.currentTimeMillis()
    private var isCollecting = true
    
    private val _timeline = MutableStateFlow<StartupTimeline?>(null)
    val timeline: StateFlow<StartupTimeline?> = _timeline.asStateFlow()
    
    fun start() {
        appLaunchTime = System.currentTimeMillis()
        events.clear()
        isCollecting = true
        
        logEvent(
            component = "Application",
            event = "App Launch",
            status = StartupStatus.STARTED,
            details = "Build: ${BuildConfig.BUILD_FINGERPRINT}"
        )
        
        Timber.i("🔍 StartupDiagnosticCollector: Started collecting startup diagnostics")
    }
    
    fun logEvent(
        component: String,
        event: String,
        status: StartupStatus,
        details: String? = null,
        error: String? = null
    ) {
        if (!isCollecting && status != StartupStatus.FAILED) return
        
        val timestamp = System.currentTimeMillis()
        val startupEvent = StartupEvent(
            timestamp = timestamp,
            timestampReadable = dateFormat.format(Date(timestamp)),
            component = component,
            event = event,
            status = status,
            details = details,
            error = error
        )
        
        synchronized(events) {
            events.add(startupEvent)
        }
        
        val emoji = when (status) {
            StartupStatus.STARTED -> "▶️"
            StartupStatus.IN_PROGRESS -> "⏳"
            StartupStatus.SUCCESS -> "✅"
            StartupStatus.WARNING -> "⚠️"
            StartupStatus.FAILED -> "❌"
        }
        
        val logMessage = "$emoji STARTUP [$component] $event" + 
            (details?.let { " - $it" } ?: "") +
            (error?.let { " ERROR: $it" } ?: "")
        
        when (status) {
            StartupStatus.FAILED -> Timber.e(logMessage)
            StartupStatus.WARNING -> Timber.w(logMessage)
            else -> Timber.i(logMessage)
        }
        
        updateTimeline()
    }
    
    fun completeStartup() {
        isCollecting = false
        val totalDuration = System.currentTimeMillis() - appLaunchTime
        
        logEvent(
            component = "Application",
            event = "Startup Complete",
            status = StartupStatus.SUCCESS,
            details = "Total duration: ${totalDuration}ms"
        )
        
        updateTimeline()
        Timber.i("🔍 StartupDiagnosticCollector: Startup complete in ${totalDuration}ms")
    }
    
    private fun updateTimeline() {
        val failed = events.filter { it.status == StartupStatus.FAILED }.map { it.component }.distinct()
        val warnings = events.filter { it.status == StartupStatus.WARNING }.map { it.component }.distinct()
        
        _timeline.value = StartupTimeline(
            appLaunchTime = appLaunchTime,
            buildFingerprint = BuildConfig.BUILD_FINGERPRINT,
            events = events.toList(),
            totalDuration = System.currentTimeMillis() - appLaunchTime,
            failedComponents = failed,
            warningComponents = warnings
        )
    }
    
    fun getTimeline(): StartupTimeline {
        val failed = events.filter { it.status == StartupStatus.FAILED }.map { it.component }.distinct()
        val warnings = events.filter { it.status == StartupStatus.WARNING }.map { it.component }.distinct()
        
        return StartupTimeline(
            appLaunchTime = appLaunchTime,
            buildFingerprint = BuildConfig.BUILD_FINGERPRINT,
            events = events.toList(),
            totalDuration = System.currentTimeMillis() - appLaunchTime,
            failedComponents = failed,
            warningComponents = warnings
        )
    }
    
    fun reset() {
        events.clear()
        appLaunchTime = System.currentTimeMillis()
        _timeline.value = null
    }
}
