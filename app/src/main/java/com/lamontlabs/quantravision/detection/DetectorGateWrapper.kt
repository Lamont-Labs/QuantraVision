package com.lamontlabs.quantravision.detection

import androidx.camera.core.ImageProxy
import com.lamontlabs.quantravision.billing.Entitlements
import com.lamontlabs.quantravision.billing.Tier
import com.lamontlabs.quantravision.core.PatternQuota

class DetectorGateWrapper(
    private val inner: Detector,
    private val quota: PatternQuota,
    private val ent: Entitlements
) {
    
    fun load(context: android.content.Context) {
        inner.load(context)
    }

    suspend fun analyze(image: ImageProxy): List<Detection> {
        val hasPro = ent.tier == Tier.PRO
        val hasHighlights = quota.remaining(ent) > 0
        
        return if (hasPro || hasHighlights) {
            val results = inner.demoScan()
            if (!hasPro && results.isNotEmpty()) {
                quota.consume(ent)
            }
            results
        } else {
            emptyList()
        }
    }
}
