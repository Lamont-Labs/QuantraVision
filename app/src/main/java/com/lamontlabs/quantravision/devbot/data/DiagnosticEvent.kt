package com.lamontlabs.quantravision.devbot.data

sealed class DiagnosticEvent {
    abstract val timestamp: Long
    abstract val message: String
    abstract val source: String
    
    data class Error(
        override val message: String,
        override val source: String,
        val stackTrace: String? = null,
        val severity: ErrorSeverity = ErrorSeverity.MEDIUM,
        override val timestamp: Long = System.currentTimeMillis()
    ) : DiagnosticEvent()
    
    data class Crash(
        override val message: String,
        val throwable: Throwable,
        val threadName: String,
        override val timestamp: Long = System.currentTimeMillis(),
        override val source: String = "CrashAnalyzer"
    ) : DiagnosticEvent()
    
    data class Performance(
        override val message: String,
        val metricType: PerformanceMetric,
        val value: Long,
        val threshold: Long,
        override val timestamp: Long = System.currentTimeMillis(),
        override val source: String = "PerformanceMonitor"
    ) : DiagnosticEvent()
    
    data class Network(
        override val message: String,
        val url: String,
        val statusCode: Int? = null,
        val errorType: NetworkErrorType,
        override val timestamp: Long = System.currentTimeMillis(),
        override val source: String = "NetworkMonitor"
    ) : DiagnosticEvent()
    
    data class Database(
        override val message: String,
        val query: String? = null,
        val duration: Long? = null,
        val issueType: DatabaseIssueType,
        override val timestamp: Long = System.currentTimeMillis(),
        override val source: String = "DatabaseMonitor"
    ) : DiagnosticEvent()
    
    data class Info(
        override val message: String,
        override val source: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : DiagnosticEvent()
    
    data class Warning(
        override val message: String,
        override val source: String,
        val details: String? = null,
        override val timestamp: Long = System.currentTimeMillis()
    ) : DiagnosticEvent()
}

enum class ErrorSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class PerformanceMetric {
    MEMORY_USAGE,
    CPU_USAGE,
    UI_THREAD_BLOCK,
    FRAME_DROP,
    GC_PAUSE,
    ANR_RISK
}

enum class NetworkErrorType {
    TIMEOUT,
    CONNECTION_FAILED,
    SSL_ERROR,
    DNS_FAILURE,
    HTTP_ERROR,
    UNKNOWN
}

enum class DatabaseIssueType {
    SLOW_QUERY,
    LOCK_CONTENTION,
    MIGRATION_FAILURE,
    CURSOR_LEAK,
    TRANSACTION_FAILURE
}
