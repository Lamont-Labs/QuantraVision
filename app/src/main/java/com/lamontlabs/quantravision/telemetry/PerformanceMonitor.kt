package com.lamontlabs.quantravision.telemetry

import android.os.SystemClock
import android.view.Choreographer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Local performance HUD metrics (offline only).
 * - FPS via Choreographer
 * - Detection latency via begin/end hooks
 * - CPU time approximation via uptime diffs
 */
object PerformanceMonitor : Choreographer.FrameCallback {

    private val choreographer by lazy { Choreographer.getInstance() }
    private val frameCount = AtomicInteger(0)
    private val lastSec = AtomicLong(SystemClock.elapsedRealtime())

    private val _fps = MutableStateFlow(0)
    private val _avgLatencyMs = MutableStateFlow(0L)
    private val _lastLatencyMs = MutableStateFlow(0L)

    private val latSum = AtomicLong(0)
    private val latN = AtomicInteger(0)

    val fps: StateFlow<Int> = _fps
    val avgLatencyMs: StateFlow<Long> = _avgLatencyMs
    val lastLatencyMs: StateFlow<Long> = _lastLatencyMs

    fun start() {
        choreographer.postFrameCallback(this)
    }

    fun stop() {
        choreographer.removeFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        frameCount.incrementAndGet()
        val now = SystemClock.elapsedRealtime()
        val prev = lastSec.get()
        if (now - prev >= 1000) {
            val frames = frameCount.getAndSet(0)
            _fps.value = frames
            lastSec.set(now)
        }
        choreographer.postFrameCallback(this)
    }

    fun detectionBegin(): Long = SystemClock.elapsedRealtime()

    fun detectionEnd(startMs: Long) {
        val dur = SystemClock.elapsedRealtime() - startMs
        _lastLatencyMs.value = dur
        latSum.addAndGet(dur)
        val n = latN.incrementAndGet()
        _avgLatencyMs.value = (latSum.get() / n)
    }
}
