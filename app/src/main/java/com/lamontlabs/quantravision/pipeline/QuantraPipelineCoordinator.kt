package com.lamontlabs.quantravision.pipeline

import android.content.Context
import android.graphics.Bitmap
import com.lamontlabs.quantravision.apex.ApexEngineMobile
import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.detection.DetectionResult
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.tiers.Tier
import com.lamontlabs.quantravision.tiers.TierRegistry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID

/** One authoritative route: quota -> vision -> measurements -> guards -> templates -> results. */
class QuantraPipelineCoordinator(
    private val context: Context,
    private val detectPatterns: suspend (Bitmap) -> List<DetectionResult>,
    private val visionManager: VisionManager = VisionManager(context),
    private val quotaGate: ScanQuota = QuotaGate(context)
) {
    private val primitivesBuilder = PrimitivesBuilder()
    private val scanMutex = Mutex()

    sealed class PipelineResult {
        data class Success(val apexResult: ApexResult, val overlayData: OverlayData,
            val narration: NarrationData?, val patterns: List<DetectionResult> = emptyList(),
            val source: PrimitiveSource = PrimitiveSource.SCREEN_GEOMETRY) : PipelineResult()
        data class QuotaExhausted(val scanLimit: Int, val narrationLimit: Int, val tier: Tier) : PipelineResult()
        data class FailClosed(val stage: String, val reason: String, val proofHash: String) : PipelineResult()
    }
    data class OverlayData(val status: ApexStatus, val score: Int, val verdict: String,
        val shouldRender: Boolean, val overlayStyle: OverlayStyle)
    enum class OverlayStyle { SOLID_TEAL, AMBER_DASHED, VIOLET_BROKEN, NONE }
    data class NarrationData(val headline: String, val whatWasSeen: String, val whyApexSaidThis: String,
        val conditionsToWatch: String, val riskCaveats: String, val validated: Boolean)

    suspend fun executePipeline(bitmap: Bitmap, ticker: String? = null, timeframe: String? = null,
        @Suppress("UNUSED_PARAMETER") requestNarration: Boolean = false): PipelineResult = withContext(Dispatchers.Default) {
        scanMutex.withLock {
            var stage = "quota"
            val pipelineId = UUID.randomUUID().toString()
            fun fail(reason: String): PipelineResult.FailClosed = PipelineResult.FailClosed(stage, reason,
                com.lamontlabs.quantravision.apex.ProofHasher.hashFailClosed(pipelineId, stage, reason, System.currentTimeMillis()))
            try {
                val quota = quotaGate.checkScanQuota()
                if (!quota.allowed) return@withLock PipelineResult.QuotaExhausted(quota.limit,
                    TierRegistry.getAIExplanationLimit(quota.tier), quota.tier)
                stage = "vision"
                val vision = visionManager.processImage(bitmap)
                    ?: return@withLock fail("No readable red/green candlestick series. Use a clear chart with at least 10 visible candles.")
                stage = "primitives"
                val primitives = primitivesBuilder.buildPrimitives(vision, bitmap)
                    ?: return@withLock fail("Candle measurements could not be validated")
                stage = "apex"
                val now = System.currentTimeMillis()
                val tier = quota.tier
                val scanContext = ApexScanContext(ticker ?: vision.detectedTicker, timeframe ?: vision.detectedTimeframe,
                    vision.chartType, now, EntitlementManager.getAnonymousUserId(context), when (tier) {
                        Tier.FREE -> SubscriptionTier.FREE
                        Tier.BASIC -> SubscriptionTier.BASIC
                        Tier.PRO -> SubscriptionTier.PRO
                        Tier.APEX -> SubscriptionTier.APEX
                    })
                val apex = ApexEngineMobile.runScan(scanContext, primitives, now)
                if (!ScanResultPolicy.canRender(apex.status)) {
                    return@withLock fail(apex.invalidationPoints.firstOrNull() ?: "Chart analysis was rejected")
                }
                stage = "templates"
                val patterns = detectPatterns(bitmap)
                if (patterns.any { !ScanResultPolicy.validDetection(it, bitmap.width, bitmap.height) })
                    return@withLock fail("Detector returned invalid confidence or coordinates")
                val observations = patterns.map { it.copy(
                    source = "Template", reasoning = "Visual template match. Market prices, volume and candle times are unavailable.",
                    timeframe = vision.detectedTimeframe ?: "unknown") }
                stage = "quota"
                if (!quotaGate.recordScan()) return@withLock fail("Scan usage could not be recorded; results withheld")
                PipelineResult.Success(apex, OverlayData(apex.status, 0, "Visual analysis · prices unavailable",
                    observations.isNotEmpty(), if (observations.isEmpty()) OverlayStyle.NONE else OverlayStyle.AMBER_DASHED),
                    narration = null, patterns = observations, source = primitives.source)
            } catch (cancel: CancellationException) { throw cancel
            } catch (error: Exception) {
                fail("Scan could not complete (${error.javaClass.simpleName})")
            }
        }
    }

    fun release() = visionManager.release()
}

interface ScanQuota {
    fun checkScanQuota(): QuotaGate.QuotaCheckResult
    fun recordScan(): Boolean
}

object ScanResultPolicy {
    fun canRender(status: ApexStatus): Boolean = status == ApexStatus.PASS || status == ApexStatus.WAIT
    fun validDetection(result: DetectionResult, width: Int, height: Int): Boolean {
        val b = result.bbox ?: return false
        return result.patternName.isNotBlank() && result.confidence.isFinite() && result.confidence in 0.0..1.0 &&
            listOf(b.left, b.top, b.right, b.bottom).all { it.isFinite() } &&
            b.left >= 0 && b.top >= 0 && b.right <= width && b.bottom <= height && b.width() > 0 && b.height() > 0
    }
}
