package com.lamontlabs.quantravision.core

import com.lamontlabs.quantravision.detection.Detector
import com.lamontlabs.quantravision.detection.Detection

/**
 * Placeholder for future ultra-fast detection wrapper.
 * Currently unused in production.
 */
class UltraFastDetector(private val backend: Detector) {
    fun load(context: android.content.Context) = backend.load(context)
    suspend fun analyze(): List<Detection> = backend.demoScan()
}
