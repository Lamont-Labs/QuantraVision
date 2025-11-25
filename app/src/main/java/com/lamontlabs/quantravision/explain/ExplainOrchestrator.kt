package com.lamontlabs.quantravision.explain

import android.content.Context
import com.lamontlabs.quantravision.apex.models.ApexResult
import com.lamontlabs.quantravision.cloud.CloudReasoner
import com.lamontlabs.quantravision.cloud.LLMContractValidator
import com.lamontlabs.quantravision.cloud.LocalSummaryGenerator
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import com.lamontlabs.quantravision.quota.QuotaGate
import timber.log.Timber

class ExplainOrchestrator(private val context: Context) {
    
    companion object {
        private const val TAG = "ExplainOrchestrator"
    }
    
    suspend fun explainPattern(apexResult: ApexResult): String {
        return try {
            Timber.d("$TAG: Explaining pattern for scan ${apexResult.scanId}, status=${apexResult.status}")
            
            val tier = getTierString()
            
            if (tier == "FREE") {
                Timber.d("$TAG: FREE tier - using local summary")
                return LocalSummaryGenerator.generate(apexResult)
            }
            
            if (!QuotaGate.canMakeCloudCall(context, tier)) {
                Timber.w("$TAG: Quota exhausted or rate limited - using local summary")
                return LocalSummaryGenerator.generate(apexResult)
            }
            
            val cloudResult = tryCloudExplanation(apexResult, tier)
            
            cloudResult ?: LocalSummaryGenerator.generate(apexResult)
            
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Explanation failed - using local summary")
            LocalSummaryGenerator.generate(apexResult)
        }
    }
    
    private fun buildPrimitivePacket(apexResult: ApexResult): Map<String, Any> {
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

    private suspend fun tryCloudExplanation(
        apexResult: ApexResult,
        tier: String
    ): String? {
        return try {
            Timber.d("$TAG: Attempting cloud explanation for tier=$tier")
            
            val cloudReasoner = CloudReasoner(context)
            val primitivePacket = buildPrimitivePacket(apexResult)
            when (val result = cloudReasoner.narrate(primitivePacket, tier)) {
                is CloudReasoner.NarrationResult.Success -> {
                    Timber.d("$TAG: Cloud narration received, validating...")
                    
                    val validation = LLMContractValidator.validate(
                        response = result.explanation,
                        expectedStatus = apexResult.status.name,
                        tier = tier
                    )
                    
                    if (validation.isValid) {
                        Timber.i("$TAG: Cloud explanation validated successfully")
                        formatCloudExplanation(validation.parsedData!!)
                    } else {
                        Timber.w("$TAG: LLM response violated contract: ${validation.violations.joinToString("; ")}")
                        null
                    }
                }
                is CloudReasoner.NarrationResult.Failure -> {
                    Timber.w("$TAG: Cloud reasoning failed: ${result.reason}")
                    null
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Cloud explanation error")
            null
        }
    }
    
    private fun formatCloudExplanation(data: Map<String, Any>): String {
        return buildString {
            appendLine("📊 ${data["headline"]}")
            appendLine()
            appendLine("🔍 What Was Seen:")
            appendLine(data["what_was_seen"])
            appendLine()
            appendLine("💡 Why Apex Said This:")
            appendLine(data["why_apex_said_this"])
            appendLine()
            appendLine("👀 Conditions to Watch:")
            appendLine(data["conditions_to_watch"])
            appendLine()
            appendLine("⚠️ Invalidation Triggers:")
            appendLine(data["invalidation_triggers"])
            appendLine()
            appendLine("🎯 ${data["confidence_statement"]}")
            appendLine()
            appendLine("📈 ${data["next_scan_suggestion"]}")
            appendLine()
            appendLine("⚖️ ${data["risk_caveats"]}")
        }
    }
    
    private fun getTierString(): String {
        return when (EntitlementManager.currentTier.value) {
            SubscriptionTier.FREE -> "FREE"
            SubscriptionTier.BASIC -> "BASIC"
            SubscriptionTier.PRO -> "PRO"
            SubscriptionTier.APEX -> "APEX"
        }
    }
}
