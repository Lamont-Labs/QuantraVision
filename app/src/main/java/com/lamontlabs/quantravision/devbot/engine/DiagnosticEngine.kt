package com.lamontlabs.quantravision.devbot.engine

import android.content.Context
import com.lamontlabs.quantravision.BuildConfig
import com.lamontlabs.quantravision.devbot.monitors.*
import com.lamontlabs.quantravision.devbot.data.DiagnosticEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
                    "DevBot initialized - diagnostic monitoring active",
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
    
    fun shutdown() {
        if (!isInitialized) return
        
        stopMonitoring()
        scope.cancel()
        isInitialized = false
        isEnabled = false
    }
}
