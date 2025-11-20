package com.lamontlabs.quantravision.devbot.engine

import android.content.Context
import com.lamontlabs.quantravision.BuildConfig
import com.lamontlabs.quantravision.devbot.diagnostics.ComponentHealthMonitor
import com.lamontlabs.quantravision.devbot.diagnostics.ModelDiagnostics
import com.lamontlabs.quantravision.devbot.diagnostics.StartupDiagnosticCollector
import com.lamontlabs.quantravision.devbot.monitors.*
import com.lamontlabs.quantravision.devbot.data.DiagnosticEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

object DiagnosticEngine {
    private var isInitialized = false
    private var isMonitoring = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val collectionJobs = mutableListOf<Job>()
    
    private lateinit var logcatMonitor: LogcatMonitor
    private lateinit var crashAnalyzer: CrashAnalyzer
    private lateinit var performanceMonitor: PerformanceMonitor
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var databaseMonitor: DatabaseMonitor
    
    private val _events = MutableSharedFlow<DiagnosticEvent>(
        replay = 100,
        extraBufferCapacity = 1000
    )
    val events: SharedFlow<DiagnosticEvent> = _events.asSharedFlow()
    
    private val recentErrors = ConcurrentLinkedQueue<DiagnosticEvent>()
    private const val MAX_RECENT_ERRORS = 500
    
    var isEnabled: Boolean = false
        private set
    
    fun initialize(context: Context) {
        if (isInitialized) return
        if (!BuildConfig.DEBUG) return
        
        isEnabled = true
        isInitialized = true
        
        logcatMonitor = LogcatMonitor(context)
        crashAnalyzer = CrashAnalyzer(context)
        performanceMonitor = PerformanceMonitor(context)
        networkMonitor = NetworkMonitor()
        databaseMonitor = DatabaseMonitor()
        
        startMonitoring()
        
        scope.launch {
            logEvent(
                DiagnosticEvent.Info(
                    "DevBot initialized - diagnostic monitoring active\n" +
                    "Build: ${BuildConfig.BUILD_FINGERPRINT}\n" +
                    "Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    "DiagnosticEngine"
                )
            )
        }
    }
    
    private fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        
        collectionJobs.clear()
        
        collectionJobs.add(scope.launch {
            logcatMonitor.errors.collect { error ->
                handleLogcatError(error)
            }
        })
        
        collectionJobs.add(scope.launch {
            crashAnalyzer.crashes.collect { crash ->
                handleCrash(crash)
            }
        })
        
        collectionJobs.add(scope.launch {
            performanceMonitor.issues.collect { issue ->
                handlePerformanceIssue(issue)
            }
        })
        
        collectionJobs.add(scope.launch {
            networkMonitor.failures.collect { failure ->
                handleNetworkFailure(failure)
            }
        })
        
        collectionJobs.add(scope.launch {
            databaseMonitor.issues.collect { issue ->
                handleDatabaseIssue(issue)
            }
        })
        
        logcatMonitor.startMonitoring()
        crashAnalyzer.installHandler()
        performanceMonitor.startMonitoring()
    }
    
    fun stopMonitoring() {
        if (!isMonitoring) return
        isMonitoring = false
        
        collectionJobs.forEach { it.cancel() }
        collectionJobs.clear()
        
        if (::logcatMonitor.isInitialized) {
            logcatMonitor.stop()
        }
        
        if (::performanceMonitor.isInitialized) {
            performanceMonitor.stopMonitoring()
        }
        
        if (::crashAnalyzer.isInitialized) {
            crashAnalyzer.uninstallHandler()
        }
    }
    
    private suspend fun handleLogcatError(error: LogcatError) {
        val event = DiagnosticEvent.Error(
            message = error.message,
            source = "Logcat",
            stackTrace = error.stackTrace,
            severity = error.severity
        )
        logEvent(event)
    }
    
    private suspend fun handleCrash(crash: CrashInfo) {
        val event = DiagnosticEvent.Crash(
            message = crash.message,
            throwable = crash.throwable,
            threadName = crash.threadName,
            timestamp = crash.timestamp
        )
        logEvent(event)
    }
    
    private suspend fun handlePerformanceIssue(issue: PerformanceIssue) {
        val event = DiagnosticEvent.Performance(
            message = issue.description,
            metricType = issue.type,
            value = issue.value,
            threshold = issue.threshold
        )
        logEvent(event)
    }
    
    private suspend fun handleNetworkFailure(failure: NetworkFailure) {
        val event = DiagnosticEvent.Network(
            message = failure.description,
            url = failure.url,
            statusCode = failure.statusCode,
            errorType = failure.type
        )
        logEvent(event)
    }
    
    private suspend fun handleDatabaseIssue(issue: DatabaseIssue) {
        val event = DiagnosticEvent.Database(
            message = issue.description,
            query = issue.query,
            duration = issue.duration,
            issueType = issue.type
        )
        logEvent(event)
    }
    
    private suspend fun logEvent(event: DiagnosticEvent) {
        _events.emit(event)
        
        recentErrors.offer(event)
        while (recentErrors.size > MAX_RECENT_ERRORS) {
            recentErrors.poll()
        }
    }
    
    fun getRecentErrors(limit: Int = 50): List<DiagnosticEvent> {
        return recentErrors.toList().takeLast(limit)
    }
    
    fun getErrorsByType(type: String): List<DiagnosticEvent> {
        return recentErrors.filter { event ->
            when (event) {
                is DiagnosticEvent.Error -> type == "Error"
                is DiagnosticEvent.Crash -> type == "Crash"
                is DiagnosticEvent.Performance -> type == "Performance"
                is DiagnosticEvent.Network -> type == "Network"
                is DiagnosticEvent.Database -> type == "Database"
                is DiagnosticEvent.Info -> type == "Info"
                is DiagnosticEvent.Warning -> type == "Warning"
            }
        }
    }
    
    fun clearHistory() {
        recentErrors.clear()
    }
    
    suspend fun exportDiagnostics(
        context: Context,
        filterTypes: Set<String>? = null,
        maxAge: Long? = null
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val exportTime = System.currentTimeMillis()
        val exportTimeReadable = dateFormat.format(Date(exportTime))
        
        val allEvents = recentErrors.toList()
        val filteredEvents = allEvents.filter { event ->
            val typeMatch = filterTypes == null || when (event) {
                is DiagnosticEvent.Error -> filterTypes.contains("Error")
                is DiagnosticEvent.Crash -> filterTypes.contains("Crash")
                is DiagnosticEvent.Performance -> filterTypes.contains("Performance")
                is DiagnosticEvent.Network -> filterTypes.contains("Network")
                is DiagnosticEvent.Database -> filterTypes.contains("Database")
                is DiagnosticEvent.Info -> filterTypes.contains("Info")
                is DiagnosticEvent.Warning -> filterTypes.contains("Warning")
            }
            
            val ageMatch = maxAge == null || (exportTime - event.timestamp) <= maxAge
            
            typeMatch && ageMatch
        }
        
        val json = JSONObject().apply {
            put("export_time_readable", exportTimeReadable)
            put("export_time_epoch_ms", exportTime)
            put("app_version", BuildConfig.VERSION_NAME)
            put("app_version_code", BuildConfig.VERSION_CODE)
            
            // Build Information
            put("build_info", JSONObject().apply {
                put("fingerprint", BuildConfig.BUILD_FINGERPRINT)
                put("timestamp", BuildConfig.BUILD_TIMESTAMP)
                put("git_hash", BuildConfig.GIT_HASH)
                put("build_id", BuildConfig.BUILD_ID)
                put("build_type", BuildConfig.BUILD_TYPE)
                put("debug", BuildConfig.DEBUG)
            })
            
            put("total_events_in_export", filteredEvents.size)
            put("total_events_in_memory", allEvents.size)
            put("max_events_stored", MAX_RECENT_ERRORS)
            
            if (filterTypes != null) {
                put("filtered_types", JSONArray(filterTypes.toList()))
            }
            if (maxAge != null) {
                put("max_age_ms", maxAge)
            }
            
            val eventsByType = JSONObject().apply {
                put("crashes", filteredEvents.count { it is DiagnosticEvent.Crash })
                put("errors", filteredEvents.count { it is DiagnosticEvent.Error })
                put("performance", filteredEvents.count { it is DiagnosticEvent.Performance })
                put("network", filteredEvents.count { it is DiagnosticEvent.Network })
                put("database", filteredEvents.count { it is DiagnosticEvent.Database })
                put("warnings", filteredEvents.count { it is DiagnosticEvent.Warning })
                put("info", filteredEvents.count { it is DiagnosticEvent.Info })
            }
            put("summary", eventsByType)
            
            // Startup Timeline
            put("startup_timeline", JSONObject().apply {
                val timeline = StartupDiagnosticCollector.timeline.value
                val events = timeline?.events ?: emptyList()
                val timelineArray = JSONArray()
                events.forEach { event ->
                    timelineArray.put(JSONObject().apply {
                        put("component", event.component)
                        put("event", event.event)
                        put("status", event.status.name)
                        put("timestamp_readable", dateFormat.format(Date(event.timestamp)))
                        put("timestamp_epoch_ms", event.timestamp)
                        event.duration?.let { put("duration_ms", it) }
                        event.details?.let { put("details", it) }
                        event.error?.let { put("error", it) }
                    })
                }
                put("events", timelineArray)
                put("total_events", events.size)
                timeline?.let {
                    put("total_startup_time_ms", it.totalDuration)
                    put("app_launch_time", it.appLaunchTime)
                    put("build_fingerprint", it.buildFingerprint)
                    if (it.failedComponents.isNotEmpty()) {
                        put("failed_components", JSONArray(it.failedComponents))
                    }
                    if (it.warningComponents.isNotEmpty()) {
                        put("warning_components", JSONArray(it.warningComponents))
                    }
                }
            })
            
            // Component Health
            put("component_health", JSONObject().apply {
                val healthStatuses = ComponentHealthMonitor.getAllHealth()
                val componentsArray = JSONArray()
                healthStatuses.forEach { (componentName, status) ->
                    componentsArray.put(JSONObject().apply {
                        put("name", componentName)
                        put("status", status.status.name)
                        put("message", status.message)
                        put("last_updated_readable", dateFormat.format(Date(status.lastUpdated)))
                        put("last_updated_epoch_ms", status.lastUpdated)
                        if (status.details.isNotEmpty()) {
                            put("details", JSONObject(status.details))
                        }
                    })
                }
                put("components", componentsArray)
                put("total_components", healthStatuses.size)
                put("healthy_count", healthStatuses.values.count { it.status.name == "HEALTHY" })
                put("degraded_count", healthStatuses.values.count { it.status.name == "DEGRADED" })
                put("failed_count", healthStatuses.values.count { it.status.name == "FAILED" })
            })
            
            // Model Diagnostics
            put("model_diagnostics", JSONObject().apply {
                val modelDiag = ModelDiagnostics.diagnose(context)
                
                // Assets models
                val assetsArray = JSONArray()
                modelDiag.filesInAssets.forEach { model ->
                    assetsArray.put(JSONObject().apply {
                        put("model_type", model.modelType.name)
                        put("file_name", model.fileName)
                        put("exists", model.exists)
                        put("size", model.size)
                        put("size_readable", model.sizeReadable)
                        put("location", model.location)
                        put("is_valid", model.isValid)
                        model.validationError?.let { put("validation_error", it) }
                    })
                }
                put("bundled_assets", assetsArray)
                
                // Internal storage models
                val internalArray = JSONArray()
                modelDiag.filesInInternalStorage.forEach { model ->
                    internalArray.put(JSONObject().apply {
                        put("model_type", model.modelType.name)
                        put("file_name", model.fileName)
                        put("exists", model.exists)
                        put("size", model.size)
                        put("size_readable", model.sizeReadable)
                        put("checksum", model.checksum)
                        put("location", model.location)
                        put("is_valid", model.isValid)
                        put("last_modified_readable", model.lastModifiedReadable)
                        put("last_modified_epoch_ms", model.lastModified)
                        model.validationError?.let { put("validation_error", it) }
                    })
                }
                put("internal_storage", internalArray)
                
                // Initialization statuses
                val initArray = JSONArray()
                modelDiag.initializationStatuses.forEach { status ->
                    initArray.put(JSONObject().apply {
                        put("model_type", status.modelType.name)
                        put("initialized", status.initialized)
                        status.initializationTime?.let { put("initialization_time_ms", it) }
                        status.error?.let { put("error", it) }
                        status.stackTrace?.let { put("stack_trace", it) }
                    })
                }
                put("initialization_statuses", initArray)
                
                // Recommendations
                val recommendationsArray = JSONArray(modelDiag.recommendations)
                put("recommendations", recommendationsArray)
                
                put("total_assets_size", modelDiag.totalAssetsSize)
                put("total_internal_size", modelDiag.totalInternalSize)
                put("total_assets_size_readable", formatBytes(modelDiag.totalAssetsSize))
                put("total_internal_size_readable", formatBytes(modelDiag.totalInternalSize))
            })
            
            val eventsArray = JSONArray()
            filteredEvents.forEach { event ->
                eventsArray.put(serializeEvent(event, dateFormat))
            }
            put("events", eventsArray)
        }
        
        return json.toString(2)
    }
    
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
    
    private fun serializeEvent(event: DiagnosticEvent, dateFormat: SimpleDateFormat): JSONObject {
        return JSONObject().apply {
            put("timestamp_readable", dateFormat.format(Date(event.timestamp)))
            put("timestamp_epoch_ms", event.timestamp)
            put("source", event.source)
            put("message", event.message)
            
            when (event) {
                is DiagnosticEvent.Error -> {
                    put("type", "Error")
                    put("severity", event.severity.name)
                    put("severity_level", event.severity.ordinal)
                    event.stackTrace?.let { put("stack_trace", it) }
                }
                is DiagnosticEvent.Crash -> {
                    put("type", "Crash")
                    put("thread", event.threadName)
                    put("exception_class", event.throwable.javaClass.name)
                    put("exception_message", event.throwable.message ?: "No message")
                    put("stack_trace", event.throwable.stackTraceToString())
                    event.throwable.cause?.let { cause ->
                        put("caused_by", JSONObject().apply {
                            put("class", cause.javaClass.name)
                            put("message", cause.message ?: "No message")
                        })
                    }
                }
                is DiagnosticEvent.Performance -> {
                    put("type", "Performance")
                    put("metric_type", event.metricType.name)
                    put("metric_ordinal", event.metricType.ordinal)
                    put("actual_value", event.value)
                    put("threshold_value", event.threshold)
                    put("exceeds_threshold", event.value > event.threshold)
                    put("percentage_over_threshold", 
                        if (event.threshold > 0) ((event.value - event.threshold) * 100.0 / event.threshold) else 0.0
                    )
                }
                is DiagnosticEvent.Network -> {
                    put("type", "Network")
                    put("url", event.url)
                    put("error_type", event.errorType.name)
                    put("error_ordinal", event.errorType.ordinal)
                    event.statusCode?.let { 
                        put("status_code", it)
                        put("is_client_error", it in 400..499)
                        put("is_server_error", it in 500..599)
                    }
                }
                is DiagnosticEvent.Database -> {
                    put("type", "Database")
                    put("issue_type", event.issueType.name)
                    put("issue_ordinal", event.issueType.ordinal)
                    event.query?.let { put("query", it) }
                    event.duration?.let { 
                        put("duration_ms", it)
                        put("is_slow", it > 100)
                    }
                }
                is DiagnosticEvent.Warning -> {
                    put("type", "Warning")
                    event.details?.let { put("details", it) }
                }
                is DiagnosticEvent.Info -> {
                    put("type", "Info")
                }
            }
        }
    }
    
    fun shutdown() {
        if (!isInitialized) return
        
        stopMonitoring()
        scope.cancel()
        isInitialized = false
        isEnabled = false
    }
}
