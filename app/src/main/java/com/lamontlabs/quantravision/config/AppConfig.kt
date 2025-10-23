package com.lamontlabs.quantravision.config

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * AppConfig
 * Central adjustable settings persisted locally.
 * Everything is tunable at runtime and reloadable without network.
 */
object AppConfig {

    private const val FILE = "app_config.json"

    data class Overlay(
        val opacity: Float = 0.35f,
        val labelTextSizeSp: Float = 12f,
        val showHeatmap: Boolean = true
    )

    data class Performance(
        val targetFps: Int = 12,
        val roiMaxRegions: Int = 8
    )

    data class Detection(
        val globalThreshold: Double = 0.72,
        val scaleMin: Double = 0.6,
        val scaleMax: Double = 1.8,
        val scaleStride: Double = 0.15,
        val temporalHalfLifeMs: Long = 7000,
        val consensusSigma: Double = 0.2
    )

    data class Config(
        val overlay: Overlay = Overlay(),
        val performance: Performance = Performance(),
        val detection: Detection = Detection()
    )

    fun load(context: Context): Config {
        val f = File(context.filesDir, FILE)
        if (!f.exists()) return Config()
        val o = JSONObject(f.readText())

        val ovr = o.optJSONObject("overlay") ?: JSONObject()
        val perf = o.optJSONObject("performance") ?: JSONObject()
        val det = o.optJSONObject("detection") ?: JSONObject()

        return Config(
            overlay = Overlay(
                opacity = ovr.optDouble("opacity", 0.35).toFloat().coerceIn(0f, 1f),
                labelTextSizeSp = ovr.optDouble("labelTextSizeSp", 12.0).toFloat().coerceAtLeast(8f),
                showHeatmap = ovr.optBoolean("showHeatmap", true)
            ),
            performance = Performance(
                targetFps = perf.optInt("targetFps", 12).coerceIn(4, 30),
                roiMaxRegions = perf.optInt("roiMaxRegions", 8).coerceIn(1, 64)
            ),
            detection = Detection(
                globalThreshold = det.optDouble("globalThreshold", 0.72).coerceIn(0.0, 0.99),
                scaleMin = det.optDouble("scaleMin", 0.6).coerceAtLeast(0.2),
                scaleMax = det.optDouble("scaleMax", 1.8).coerceAtMost(4.0),
                scaleStride = det.optDouble("scaleStride", 0.15).coerceIn(0.02, 0.5),
                temporalHalfLifeMs = det.optLong("temporalHalfLifeMs", 7000L).coerceAtLeast(1000L),
                consensusSigma = det.optDouble("consensusSigma", 0.2).coerceIn(0.05, 1.0)
            )
        )
    }

    fun save(context: Context, cfg: Config) {
        val o = JSONObject().apply {
            put("overlay", JSONObject().apply {
                put("opacity", cfg.overlay.opacity.toDouble())
                put("labelTextSizeSp", cfg.overlay.labelTextSizeSp.toDouble())
                put("showHeatmap", cfg.overlay.showHeatmap)
            })
            put("performance", JSONObject().apply {
                put("targetFps", cfg.performance.targetFps)
                put("roiMaxRegions", cfg.performance.roiMaxRegions)
            })
            put("detection", JSONObject().apply {
                put("globalThreshold", cfg.detection.globalThreshold)
                put("scaleMin", cfg.detection.scaleMin)
                put("scaleMax", cfg.detection.scaleMax)
                put("scaleStride", cfg.detection.scaleStride)
                put("temporalHalfLifeMs", cfg.detection.temporalHalfLifeMs)
                put("consensusSigma", cfg.detection.consensusSigma)
            })
        }
        File(context.filesDir, FILE).writeText(o.toString(2))
    }
}
