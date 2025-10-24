package com.lamontlabs.quantravision.detection

import androidx.camera.core.ImageProxy
import com.lamontlabs.quantravision.billing.Entitlements
import com.lamontlabs.quantravision.core.PatternQuota

/**
 * Wrap your existing Detector. Blocks highlights when quota is exhausted.
 */
class DetectorGateWrapper(
    private val inner: Detector,
    private val quota: PatternQuota,
    private val ent: Entitlements
) : Detector {

    override fun load(context: android.content.Context)
