package com.lamontlabs.quantravision.tuning

import android.content.Context
import com.lamontlabs.quantravision.config.AppConfig
import com.lamontlabs.quantravision.detection.ConsensusEngineTunable
import com.lamontlabs.quantravision.detection.TemporalTrackerTunable
import com.lamontlabs.quantravision.roi.ROIProposerTunable
import com.lamontlabs.quantravision.capture.LiveOverlayControllerTunable

/**
 * RuntimeTuner
 * Bridges AppConfig into subsystems that support tuning.
 * Each tunable is optional; if not present, no-ops.
 */
object RuntimeTuner {

    fun apply(context: Context, cfg: AppConfig.Config) {
        // Detection knobs
        TemporalTrackerTunable.setHalfLife(cfg.detection.temporalHalfLifeMs)
        ConsensusEngineTunable.setSigma(cfg.detection.consensusSigma)

        // ROI proposal
        ROIProposerTunable.setMaxRois(cfg.performance.roiMaxRegions)

        // Capture
        LiveOverlayControllerTunable.setTargetFps(cfg.performance.targetFps)

        // Overlay UI knobs are applied by screens when rendering (opacity, label size)
        // Global thresholds and scale ranges are applied via TemplateEditor (below) or read during matching.
    }
}
