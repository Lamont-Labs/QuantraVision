package com.lamontlabs.quantravision.demo

import com.lamontlabs.quantravision.detection.Detector
import com.lamontlabs.quantravision.detection.Detection

/**
 * Placeholder for demo/testing sandbox.
 * Currently unused in production.
 */
class DemoSandbox(private val backend: Detector) {
    fun load(context: android.content.Context) = backend.load(context)
    suspend fun analyze(): List<Detection> = backend.demoScan()
}
