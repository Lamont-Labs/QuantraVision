package com.lamontlabs.quantravision.devbot.monitors

import com.lamontlabs.quantravision.devbot.data.DatabaseIssueType
import kotlinx.coroutines.flow.*

data class DatabaseIssue(
    val description: String,
    val query: String? = null,
    val duration: Long? = null,
    val type: DatabaseIssueType,
    val timestamp: Long = System.currentTimeMillis()
)

class DatabaseMonitor {
    private val _issues = MutableSharedFlow<DatabaseIssue>(
        replay = 20,
        extraBufferCapacity = 100
    )
    val issues: SharedFlow<DatabaseIssue> = _issues.asSharedFlow()
    
    private val SLOW_QUERY_THRESHOLD_MS = 100L
    
    fun reportSlowQuery(query: String, durationMs: Long) {
        if (durationMs > SLOW_QUERY_THRESHOLD_MS) {
            _issues.tryEmit(
                DatabaseIssue(
                    description = "Slow query: ${durationMs}ms (threshold: ${SLOW_QUERY_THRESHOLD_MS}ms)",
                    query = query.take(200),
                    duration = durationMs,
                    type = DatabaseIssueType.SLOW_QUERY
                )
            )
        }
    }
    
    fun reportLockContention(tableName: String, waitTimeMs: Long) {
        _issues.tryEmit(
            DatabaseIssue(
                description = "Lock contention on table '$tableName' - waited ${waitTimeMs}ms",
                duration = waitTimeMs,
                type = DatabaseIssueType.LOCK_CONTENTION
            )
        )
    }
    
    fun reportMigrationFailure(errorMessage: String) {
        _issues.tryEmit(
            DatabaseIssue(
                description = "Database migration failed: $errorMessage",
                type = DatabaseIssueType.MIGRATION_FAILURE
            )
        )
    }
    
    fun reportCursorLeak(query: String) {
        _issues.tryEmit(
            DatabaseIssue(
                description = "Potential cursor leak detected",
                query = query.take(200),
                type = DatabaseIssueType.CURSOR_LEAK
            )
        )
    }
    
    fun reportTransactionFailure(errorMessage: String) {
        _issues.tryEmit(
            DatabaseIssue(
                description = "Transaction failed: $errorMessage",
                type = DatabaseIssueType.TRANSACTION_FAILURE
            )
        )
    }
}
