/*
 * Copyright (c) 2025 Lamont Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lamontlabs.quantravision.pipeline

import android.content.Context
import android.graphics.Bitmap
import com.lamontlabs.quantravision.apex.ApexEngineMobile
import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.cloud.CloudReasoner
import com.lamontlabs.quantravision.cloud.LLMContractValidator
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.tiers.Tier
import com.lamontlabs.quantravision.tiers.TierRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID

/**
 * QuantraPipelineCoordinator
 * 
 * Master Spec v2.0 Pipeline Orchestrator
 * 
 * Enforces the fixed, non-negotiable pipeline:
 * Screen Capture → Vision Models → Primitive Extraction → Apex Engine →
 * QuantraScore → Overlay Rendering → Quota Gate → Cloud Narration → LLM Validator
 * 
 * Design principles:
 * - Fail-closed: Any error results in safe fallback, never false positives
 * - Deterministic: Same input always produces same output and proof hash
 * - Privacy-first: Never sends images to cloud, only text primitives
 * - Tier-gated: Scans and narrations are hard-capped per subscription tier
 */
class QuantraPipelineCoordinator(private val context: Context) {

    companion object {
        private const val TAG = "QuantraPipelineCoordinator"
    }

    private val visionManager = VisionManager(context)
    private val primitivesBuilder = PrimitivesBuilder()
    private val quotaGate = QuotaGate(context)
    private val cloudNarrationOrchestrator = CloudNarrationOrchestrator(context)

    sealed class PipelineResult {
        data class Success(
            val apexResult: ApexResult,
            val overlayData: OverlayData,
            val narration: NarrationData?
        ) : PipelineResult()

        data class QuotaExhausted(
            val scanLimit: Int,
            val narrationLimit: Int,
            val tier: Tier
        ) : PipelineResult()

        data class FailClosed(
            val stage: String,
            val reason: String,
            val proofHash: String
        ) : PipelineResult()
    }

    data class OverlayData(
        val status: ApexStatus,
        val score: Int,
        val verdict: String,
        val shouldRender: Boolean,
        val overlayStyle: OverlayStyle
    )

    enum class OverlayStyle {
        SOLID_TEAL,      // PASS - solid teal overlays
        AMBER_DASHED,    // WAIT - amber dashed overlays
        VIOLET_BROKEN,   // SUPPRESSED - violet broken overlays
        NONE             // FAIL/OMEGA - no overlays
    }

    data class NarrationData(
        val headline: String,
        val whatWasSeen: String,
        val whyApexSaidThis: String,
        val conditionsToWatch: String,
        val riskCaveats: String,
        val validated: Boolean
    )

    /**
     * Execute the complete v2.0 pipeline.
     * 
     * @param bitmap Screen capture bitmap
     * @param ticker Optional ticker symbol (from OCR or user input)
     * @param timeframe Optional timeframe (from OCR or user input)
     * @param requestNarration Whether to request cloud narration (tier-gated)
     * @return PipelineResult with Apex result, overlay data, and optional narration
     */
    suspend fun executePipeline(
        bitmap: Bitmap,
        ticker: String? = null,
        timeframe: String? = null,
        requestNarration: Boolean = false
    ): PipelineResult = withContext(Dispatchers.Default) {
        val pipelineStartTime = System.currentTimeMillis()
        val pipelineId = generatePipelineId()
        
        Timber.i("$TAG: [$pipelineId] Starting v2.0 pipeline")

        try {
            val tier = EntitlementManager.getCurrentTier(context)
            Timber.d("$TAG: [$pipelineId] User tier: $tier")

            val canScanResult = quotaGate.checkScanQuota()
            if (!canScanResult.allowed) {
                Timber.w("$TAG: [$pipelineId] Scan quota exhausted")
                return@withContext PipelineResult.QuotaExhausted(
                    scanLimit = TierRegistry.getScanLimit(tier),
                    narrationLimit = TierRegistry.getAIExplanationLimit(tier),
                    tier = tier
                )
            }

            Timber.d("$TAG: [$pipelineId] Stage 1: Vision processing")
            val visionOutput = try {
                visionManager.processImage(bitmap)
            } catch (e: Exception) {
                Timber.e(e, "$TAG: [$pipelineId] Vision processing failed")
                return@withContext createFailClosedResult("vision", e.message ?: "Vision error", pipelineId)
            }
            
            if (visionOutput == null) {
                Timber.w("$TAG: [$pipelineId] Vision returned null - fail-closed")
                return@withContext createFailClosedResult("vision", "Vision processing unavailable", pipelineId)
            }

            Timber.d("$TAG: [$pipelineId] Stage 2: Primitive extraction")
            val primitives = try {
                primitivesBuilder.buildPrimitives(visionOutput, bitmap)
            } catch (e: Exception) {
                Timber.e(e, "$TAG: [$pipelineId] Primitive extraction failed")
                return@withContext createFailClosedResult("primitives", e.message ?: "Primitive error", pipelineId)
            }
            
            if (primitives == null) {
                Timber.w("$TAG: [$pipelineId] Primitives returned null - fail-closed")
                return@withContext createFailClosedResult("primitives", "Primitive extraction unavailable", pipelineId)
            }

            Timber.d("$TAG: [$pipelineId] Stage 3: Apex Engine scan")
            val scanContext = ApexScanContext(
                ticker = ticker ?: visionOutput.detectedTicker,
                timeframe = timeframe ?: visionOutput.detectedTimeframe,
                chartType = visionOutput.chartType,
                timestamp = System.currentTimeMillis(),
                userId = EntitlementManager.getAnonymousUserId(context),
                tier = mapTierToSubscriptionTier(tier)
            )

            val apexResult = try {
                ApexEngineMobile.runScan(scanContext, primitives, System.currentTimeMillis())
            } catch (e: Exception) {
                Timber.e(e, "$TAG: [$pipelineId] Apex Engine failed")
                return@withContext createFailClosedResult("apex", e.message ?: "Apex error", pipelineId)
            }

            quotaGate.recordScan()
            Timber.d("$TAG: [$pipelineId] Scan recorded. Score: ${apexResult.quantraScore.normalizedScore}")

            Timber.d("$TAG: [$pipelineId] Stage 4: Overlay data generation")
            val overlayData = generateOverlayData(apexResult)

            var narrationData: NarrationData? = null
            if (requestNarration && shouldAttemptNarration(tier, apexResult)) {
                Timber.d("$TAG: [$pipelineId] Stage 5: Cloud narration")
                val canNarrateResult = quotaGate.checkNarrationQuota()
                
                if (canNarrateResult.allowed) {
                    narrationData = try {
                        cloudNarrationOrchestrator.narrate(apexResult, tier)
                    } catch (e: Exception) {
                        Timber.w(e, "$TAG: [$pipelineId] Cloud narration failed (non-fatal)")
                        null
                    }
                    
                    if (narrationData != null) {
                        quotaGate.recordNarration()
                    }
                } else {
                    Timber.d("$TAG: [$pipelineId] Narration quota exhausted")
                }
            }

            val pipelineTime = System.currentTimeMillis() - pipelineStartTime
            Timber.i("$TAG: [$pipelineId] Pipeline complete in ${pipelineTime}ms")

            PipelineResult.Success(
                apexResult = apexResult,
                overlayData = overlayData,
                narration = narrationData
            )

        } catch (e: Exception) {
            Timber.e(e, "$TAG: [$pipelineId] Unexpected pipeline error")
            createFailClosedResult("pipeline", e.message ?: "Unknown error", pipelineId)
        }
    }

    private fun generateOverlayData(apexResult: ApexResult): OverlayData {
        val (shouldRender, style) = when (apexResult.status) {
            ApexStatus.PASS -> true to OverlayStyle.SOLID_TEAL
            ApexStatus.WAIT -> true to OverlayStyle.AMBER_DASHED
            ApexStatus.SUPPRESSED -> true to OverlayStyle.VIOLET_BROKEN
            ApexStatus.FAIL -> false to OverlayStyle.NONE
            ApexStatus.OMEGA -> false to OverlayStyle.NONE
        }

        val verdict = when (apexResult.status) {
            ApexStatus.PASS -> "Structure Confirmed"
            ApexStatus.WAIT -> "Early Formation"
            ApexStatus.SUPPRESSED -> "Suppressed"
            ApexStatus.FAIL -> "Not Detected"
            ApexStatus.OMEGA -> "Safety Lock"
        }

        return OverlayData(
            status = apexResult.status,
            score = apexResult.quantraScore.normalizedScore,
            verdict = verdict,
            shouldRender = shouldRender,
            overlayStyle = style
        )
    }

    private fun shouldAttemptNarration(tier: Tier, apexResult: ApexResult): Boolean {
        if (tier == Tier.FREE) return false
        if (apexResult.status == ApexStatus.OMEGA) return false
        return true
    }

    private fun mapTierToSubscriptionTier(tier: Tier): SubscriptionTier {
        return when (tier) {
            Tier.FREE -> SubscriptionTier.FREE
            Tier.BASIC -> SubscriptionTier.BASIC
            Tier.PRO -> SubscriptionTier.PRO
            Tier.APEX -> SubscriptionTier.APEX
        }
    }

    private fun createFailClosedResult(stage: String, reason: String, pipelineId: String): PipelineResult.FailClosed {
        val proofHash = com.lamontlabs.quantravision.apex.ProofHasher.hashFailClosed(
            pipelineId = pipelineId,
            stage = stage,
            reason = reason,
            timestamp = System.currentTimeMillis()
        )
        
        return PipelineResult.FailClosed(
            stage = stage,
            reason = reason,
            proofHash = proofHash
        )
    }

    private fun generatePipelineId(): String {
        return "PL_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
    }
}
