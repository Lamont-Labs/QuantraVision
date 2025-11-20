package com.lamontlabs.quantravision.devbot.diagnostics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class HealthStatus {
    HEALTHY,      // Component working perfectly
    DEGRADED,     // Component working but with issues
    FAILED,       // Component not working
    UNKNOWN       // Component status not yet determined
}

data class ComponentHealth(
    val componentName: String,
    val status: HealthStatus,
    val message: String,
    val details: Map<String, String> = emptyMap(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val lastUpdatedReadable: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
)

object ComponentHealthMonitor {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    
    private val _healthStates = MutableStateFlow<Map<String, ComponentHealth>>(emptyMap())
    val healthStates: StateFlow<Map<String, ComponentHealth>> = _healthStates.asStateFlow()
    
    private val components = mutableMapOf<String, ComponentHealth>()
    
    init {
        // Initialize all components as UNKNOWN
        val initialComponents = listOf(
            "Ensemble AI Engine",
            "Pattern Detection Engine",
            "Database",
            "ML Kit OCR",
            "Alert System",
            "Learning Engine",
            "QuantraCore Intelligence"
        )
        
        initialComponents.forEach { componentName ->
            components[componentName] = ComponentHealth(
                componentName = componentName,
                status = HealthStatus.UNKNOWN,
                message = "Not initialized",
                details = emptyMap()
            )
        }
        
        _healthStates.value = components.toMap()
    }
    
    fun updateComponentHealth(
        componentName: String,
        status: HealthStatus,
        message: String,
        details: Map<String, String> = emptyMap()
    ) {
        val health = ComponentHealth(
            componentName = componentName,
            status = status,
            message = message,
            details = details,
            lastUpdated = System.currentTimeMillis(),
            lastUpdatedReadable = dateFormat.format(Date())
        )
        
        synchronized(components) {
            components[componentName] = health
            _healthStates.value = components.toMap()
        }
        
        val emoji = when (status) {
            HealthStatus.HEALTHY -> "✅"
            HealthStatus.DEGRADED -> "⚠️"
            HealthStatus.FAILED -> "❌"
            HealthStatus.UNKNOWN -> "❓"
        }
        
        Timber.i("💊 HEALTH [$componentName] $emoji $status: $message")
    }
    
    fun getComponentHealth(componentName: String): ComponentHealth? {
        return components[componentName]
    }
    
    fun getAllHealth(): Map<String, ComponentHealth> {
        return components.toMap()
    }
    
    fun getFailedComponents(): List<ComponentHealth> {
        return components.values.filter { it.status == HealthStatus.FAILED }
    }
    
    fun getDegradedComponents(): List<ComponentHealth> {
        return components.values.filter { it.status == HealthStatus.DEGRADED }
    }
    
    fun getHealthySummary(): String {
        val total = components.size
        val healthy = components.values.count { it.status == HealthStatus.HEALTHY }
        val degraded = components.values.count { it.status == HealthStatus.DEGRADED }
        val failed = components.values.count { it.status == HealthStatus.FAILED }
        val unknown = components.values.count { it.status == HealthStatus.UNKNOWN }
        
        return "✅ $healthy | ⚠️ $degraded | ❌ $failed | ❓ $unknown (of $total)"
    }
    
    fun reset() {
        components.keys.forEach { componentName ->
            components[componentName] = ComponentHealth(
                componentName = componentName,
                status = HealthStatus.UNKNOWN,
                message = "Reset - not initialized",
                details = emptyMap()
            )
        }
        _healthStates.value = components.toMap()
    }
}
