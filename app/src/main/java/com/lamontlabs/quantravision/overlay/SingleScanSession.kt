package com.lamontlabs.quantravision.overlay

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Keeps ownership through cancellation and releases native resources only after work stops. */
internal class SingleScanSession(private val scope: CoroutineScope) {
    private var current: Job? = null
    private var closed = false

    @Synchronized
    fun start(work: suspend () -> Unit): Boolean {
        if (closed || current != null) return false
        val job = scope.launch(start = CoroutineStart.LAZY) { work() }
        current = job
        job.invokeOnCompletion {
            synchronized(this) { if (current === job) current = null }
        }
        job.start()
        return true
    }

    fun cancel() {
        val job = synchronized(this) { current }
        job?.cancel()
    }

    fun close(release: () -> Unit) {
        val job = synchronized(this) {
            if (closed) return
            closed = true
            current
        }
        if (job == null) release() else {
            job.invokeOnCompletion { release() }
            job.cancel()
        }
    }
}
