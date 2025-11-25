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
import com.lamontlabs.quantravision.apex.models.ApexResult
import com.lamontlabs.quantravision.cloud.CloudReasoner
import com.lamontlabs.quantravision.cloud.LLMContractValidator
import com.lamontlabs.quantravision.tiers.Tier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber

/**
 * CloudNarrationOrchestrator
 * 
 * Master Spec v2.0 Cloud Narration Layer
 * 
 * Orchestrates cloud LLM narration with safety validation:
 * 1. Builds primitive-only request (never sends images)
 * 2. Calls CloudReasoner for narration
 * 3. Validates response through LLMContractValidator
 * 4. Returns validated narration or null on validation failure
 * 
 * Security: 
 * - No image/screenshot data ever sent to cloud
 * - Only structured text primitives from Apex result
 * - Blocks financial advice terms (buy, sell, etc.)
 * - Fail-closed on validation failure
 */
class CloudNarrationOrchestrator(private val context: Context) {

    companion object {
        private const val TAG = "CloudNarrationOrchestrator"
    }

    private val cloudReasoner = CloudReasoner(context)

    /**
     * Request cloud narration for Apex result.
     * 
     * @param apexResult Apex Engine output to narrate
     * @param tier User subscription tier (determines model and token limits)
     * @return NarrationData if successful and validated, null otherwise
     */
    suspend fun narrate(
        apexResult: ApexResult,
        tier: Tier
    ): QuantraPipelineCoordinator.NarrationData? = withContext(Dispatchers.IO) {
        
        Timber.d("$TAG: Starting narration for scan ${apexResult.scanId}")

        if (tier == Tier.FREE) {
            Timber.d("$TAG: Free tier - narration not available")
            return@withContext null
        }

        try {
            val tierString = tier.name
            val primitivePacket = buildPrimitivePacket(apexResult)
            Timber.d("$TAG: Built sanitized primitive packet with ${primitivePacket.size} fields")
            val result = cloudReasoner.narrate(primitivePacket, tierString)

            when (result) {
                is CloudReasoner.NarrationResult.Success -> {
                    val validatedNarration = validateAndParse(
                        result.explanation,
                        apexResult.status.name,
                        tierString
                    )
                    
                    if (validatedNarration != null) {
                        Timber.d("$TAG: Narration validated successfully")
                    } else {
                        Timber.w("$TAG: Narration failed validation, returning null")
                    }
                    
                    return@withContext validatedNarration
                }
                is CloudReasoner.NarrationResult.Failure -> {
                    Timber.w("$TAG: Cloud narration failed: ${result.reason}")
                    return@withContext null
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Narration error")
            return@withContext null
        }
    }

    private fun validateAndParse(
        response: String,
        expectedStatus: String,
        tier: String
    ): QuantraPipelineCoordinator.NarrationData? {
        val validationResult = LLMContractValidator.validate(
            response = response,
            expectedStatus = expectedStatus,
            tier = tier
        )

        if (!validationResult.isValid) {
            Timber.w("$TAG: Validation failed with ${validationResult.violations.size} violations")
            validationResult.violations.forEach { violation ->
                Timber.w("$TAG: Violation: $violation")
            }
            return null
        }

        return try {
            val json = JSONObject(response)
            
            QuantraPipelineCoordinator.NarrationData(
                headline = json.optString("headline", "Analysis Complete"),
                whatWasSeen = json.optString("what_was_seen", ""),
                whyApexSaidThis = json.optString("why_apex_said_this", ""),
                conditionsToWatch = json.optString("conditions_to_watch", ""),
                riskCaveats = json.optString("risk_caveats", "Educational use only."),
                validated = true
            )
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Failed to parse validated response")
            null
        }
    }

    /**
     * Build the primitive packet that gets sent to cloud.
     * This is what the cloud sees - structured text only, never images.
     */
    fun buildPrimitivePacket(apexResult: ApexResult): Map<String, Any> {
        return mapOf(
            "scan_id" to apexResult.scanId,
            "status" to apexResult.status.name,
            "quantra_score" to apexResult.quantraScore.normalizedScore,
            "score_band" to apexResult.quantraScore.band.name,
            "confidence" to apexResult.confidenceApex,
            "entropy" to apexResult.entropyScore,
            "regime_ok" to apexResult.regimeOk,
            "suppression_active" to apexResult.suppressionActive,
            "omega_lock" to apexResult.omegaLock,
            "invalidation_points" to apexResult.invalidationPoints,
            "top_protocols" to apexResult.protocolTrace.take(5).map { it.protocolId },
            "proof_hash" to apexResult.proofHash
        )
    }
}
