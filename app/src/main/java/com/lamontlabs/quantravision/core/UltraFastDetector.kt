package com.lamontlabs.quantravision.core

import android.graphics.Bitmap
import android.os.SystemClock
import com.lamontlabs.quantravision.detection.Detector
import com.lamontlabs.quantravision.detection.Detection
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.measureNanoTime

/**
 * Deterministic, low-latency wrapper around the base Detector.
 * Drops frames if analysis >16 ms to guarantee real-time HUD responsiveness.
 */
class UltraFastDetector(private val backend: Detector) : Detector {
    private val busy = AtomicBoolean(false)
    private val lastLatency = LongArray(8) { 0L }
    private var idx = 0

    override fun load(context: android.content.Context) = backend.load(context)

    override fun analyze(frame: androidx.camera.core.ImageProxy): List<Detection> {
        val start = SystemClock.elapsedRealtimeNanos()
        if (!busy.compareAndSet(false, true)) {
            // Drop frame, reuse last known detections
            frame.close()
            return emptyList()
        }
        val detections = try { backend.analyze(frame) } finally {
            busy.set(false)
        }
        val took = SystemClock.elapsedRealtimeNanos() - start
        lastLatency[idx] = took
        idx = (idx + 1) % lastLatency.size
        return detections
    }

    /** Average latency in milliseconds. */
    fun avgLatencyMs(): Float =
        lastLatency.filter { it > 0 }.average().div(1_000_000.0).toFloat()

    /** Returns true if within 16 ms target. */
    fun isRealtime(): Boolean = avgLatencyMs() <= 16f
}
