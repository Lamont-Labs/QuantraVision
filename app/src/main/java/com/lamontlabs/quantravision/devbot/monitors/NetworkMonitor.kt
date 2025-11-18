package com.lamontlabs.quantravision.devbot.monitors

import com.lamontlabs.quantravision.devbot.data.NetworkErrorType
import kotlinx.coroutines.flow.*

data class NetworkFailure(
    val description: String,
    val url: String,
    val statusCode: Int? = null,
    val type: NetworkErrorType,
    val timestamp: Long = System.currentTimeMillis()
)

class NetworkMonitor {
    private val _failures = MutableSharedFlow<NetworkFailure>(
        replay = 20,
        extraBufferCapacity = 100
    )
    val failures: SharedFlow<NetworkFailure> = _failures.asSharedFlow()
    
    fun reportTimeout(url: String, timeoutMs: Long) {
        _failures.tryEmit(
            NetworkFailure(
                description = "Network timeout after ${timeoutMs}ms",
                url = url,
                type = NetworkErrorType.TIMEOUT
            )
        )
    }
    
    fun reportConnectionFailed(url: String, reason: String) {
        _failures.tryEmit(
            NetworkFailure(
                description = "Connection failed: $reason",
                url = url,
                type = NetworkErrorType.CONNECTION_FAILED
            )
        )
    }
    
    fun reportHttpError(url: String, statusCode: Int) {
        _failures.tryEmit(
            NetworkFailure(
                description = "HTTP error $statusCode",
                url = url,
                statusCode = statusCode,
                type = NetworkErrorType.HTTP_ERROR
            )
        )
    }
    
    fun reportSslError(url: String, reason: String) {
        _failures.tryEmit(
            NetworkFailure(
                description = "SSL/TLS error: $reason",
                url = url,
                type = NetworkErrorType.SSL_ERROR
            )
        )
    }
    
    fun reportDnsFailure(url: String) {
        _failures.tryEmit(
            NetworkFailure(
                description = "DNS resolution failed",
                url = url,
                type = NetworkErrorType.DNS_FAILURE
            )
        )
    }
}
