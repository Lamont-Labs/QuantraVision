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

package com.lamontlabs.quantravision.explain

import android.content.Context
import com.lamontlabs.quantravision.apex.models.ApexResult
import com.lamontlabs.quantravision.apex.models.ApexStatus
import com.lamontlabs.quantravision.cloud.CloudReasoner
import com.lamontlabs.quantravision.cloud.LLMContractValidator
import com.lamontlabs.quantravision.cloud.LocalSummaryGenerator
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import com.lamontlabs.quantravision.quota.QuotaGate
import timber.log.Timber

class AutoExplainManager(private val context: Context) {

    companion object {
        private const val TAG = "AutoExplainManager"
        private const val PREFS_NAME = "auto_explain_prefs"
        private const val KEY_AUTO_EXPLAIN_ENABLED = "auto_explain_enabled"
        
        private const val ENTROPY_MAX = 0.60
        private const val WAIT_CONFIDENCE_MIN = 0.55
        private const val PASS_CONFIDENCE_MIN = 0.55
        private const val PASS_CONFIDENCE_MAX = 0.80
        private const val PASS_ENTROPY_MAX = 0.30
        
        private val NEVER_AUTO_EXPLAIN = setOf(
            ApexStatus.FAIL,
            ApexStatus.SUPPRESSED,
            ApexStatus.OMEGA
        )
    }

    private val cloudReasoner = CloudReasoner(context)

    fun shouldAutoExplain(apexResult: ApexResult): Boolean {
        Timber.d("$TAG: Checking auto-explain for status=${apexResult.status}, confidence=${apexResult.confidenceApex}")
        
        if (!checkEligibility()) {
            Timber.d("$TAG: Not eligible (tier check failed)")
            return false
        }
        
        if (!checkGlobalPreconditions(apexResult)) {
            Timber.d("$TAG: Global preconditions not met")
            return false
        }
        
        if (apexResult.status in NEVER_AUTO_EXPLAIN) {
            Timber.d("$TAG: Status ${apexResult.status} never triggers auto-explain")
            return false
        }
        
        val shouldTrigger = checkTriggers(apexResult)
        Timber.d("$TAG: Auto-explain decision: $shouldTrigger")
        return shouldTrigger
    }

    private fun checkEligibility(): Boolean {
        val tier = EntitlementManager.currentTier.value
        // Eligibility: BASIC, PRO, and APEX tiers only
        val eligible = tier == SubscriptionTier.BASIC ||
                       tier == SubscriptionTier.PRO ||
                       tier == SubscriptionTier.APEX
        
        if (!eligible) {
            Timber.v("$TAG: Tier $tier not eligible for auto-explain (paid tiers required)")
        }
        
        return eligible
    }

    private fun checkGlobalPreconditions(apexResult: ApexResult): Boolean {
        if (apexResult.omegaLock) {
            Timber.d("$TAG: Omega lock active")
            return false
        }
        
        if (apexResult.suppressionActive) {
            Timber.d("$TAG: Suppression active")
            return false
        }
        
        if (apexResult.entropyScore > ENTROPY_MAX) {
            Timber.d("$TAG: Entropy too high: ${apexResult.entropyScore} > $ENTROPY_MAX")
            return false
        }
        
        val tier = getTierString()
        if (!QuotaGate.canMakeCloudCall(context, tier)) {
            Timber.d("$TAG: Quota exhausted for tier $tier")
            return false
        }
        
        Timber.v("$TAG: All global preconditions passed")
        return true
    }

    private fun checkTriggers(apexResult: ApexResult): Boolean {
        return when (apexResult.status) {
            ApexStatus.WAIT -> {
                val trigger = apexResult.confidenceApex >= WAIT_CONFIDENCE_MIN
                if (trigger) {
                    Timber.d("$TAG: WAIT trigger: confidence ${apexResult.confidenceApex} >= $WAIT_CONFIDENCE_MIN")
                }
                trigger
            }
            
            ApexStatus.PASS -> {
                val confidenceInRange = apexResult.confidenceApex >= PASS_CONFIDENCE_MIN && 
                                       apexResult.confidenceApex < PASS_CONFIDENCE_MAX
                val entropyLow = apexResult.entropyScore <= PASS_ENTROPY_MAX
                val userToggleOn = isUserToggleEnabled()
                
                val trigger = confidenceInRange && entropyLow && userToggleOn
                
                if (trigger) {
                    Timber.d("$TAG: PASS trigger: confidence=${apexResult.confidenceApex}, entropy=${apexResult.entropyScore}, toggle=$userToggleOn")
                } else if (confidenceInRange && entropyLow && !userToggleOn) {
                    Timber.v("$TAG: PASS conditions met but user toggle disabled")
                }
                
                trigger
            }
            
            else -> {
                Timber.v("$TAG: No triggers for status ${apexResult.status}")
                false
            }
        }
    }

    fun isUserToggleEnabled(): Boolean {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getBoolean(KEY_AUTO_EXPLAIN_ENABLED, false)
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Error reading user toggle")
            false
        }
    }

    fun setUserToggle(enabled: Boolean) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_AUTO_EXPLAIN_ENABLED, enabled).apply()
            Timber.d("$TAG: User toggle set to $enabled")
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Error setting user toggle")
        }
    }

    suspend fun triggerAutoExplain(apexResult: ApexResult): String {
        Timber.i("$TAG: Triggering auto-explain for scan ${apexResult.scanId}")
        
        val tier = getTierString()
        
        return try {
            val result = cloudReasoner.narrate(apexResult, tier)
            
            when (result) {
                is CloudReasoner.NarrationResult.Success -> {
                    Timber.d("$TAG: Cloud narration received, validating...")
                    
                    val validation = LLMContractValidator.validate(
                        response = result.explanation,
                        expectedStatus = apexResult.status.name,
                        tier = tier
                    )
                    
                    if (validation.isValid) {
                        Timber.i("$TAG: Cloud narration validated successfully")
                        formatCloudResponse(validation.parsedData)
                    } else {
                        Timber.w("$TAG: Cloud narration validation failed: ${validation.violations.joinToString()}")
                        Timber.d("$TAG: Falling back to local summary")
                        LocalSummaryGenerator.generate(apexResult)
                    }
                }
                
                is CloudReasoner.NarrationResult.Failure -> {
                    Timber.w("$TAG: Cloud narration failed: ${result.reason}")
                    Timber.d("$TAG: Falling back to local summary")
                    LocalSummaryGenerator.generate(apexResult)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Auto-explain failed, using local fallback")
            LocalSummaryGenerator.generate(apexResult)
        }
    }

    private fun formatCloudResponse(parsedData: Map<String, Any>?): String {
        if (parsedData == null) {
            return LocalSummaryGenerator.generate(
                com.lamontlabs.quantravision.apex.models.ApexResult(
                    scanId = "error",
                    status = ApexStatus.FAIL,
                    quantraScore = com.lamontlabs.quantravision.apex.models.QuantraScoreSnapshot(0.0, 0, com.lamontlabs.quantravision.apex.models.QuantraBand.FAIL),
                    protocolTrace = emptyList(),
                    entropyScore = 1.0,
                    suppressionActive = false,
                    omegaLock = false,
                    regimeOk = false,
                    invalidationPoints = emptyList(),
                    confidenceApex = 0.0,
                    proofHash = "",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        return buildString {
            parsedData["headline"]?.toString()?.let { appendLine(it) }
            appendLine()
            parsedData["what_was_seen"]?.toString()?.let { appendLine(it) }
            appendLine()
            parsedData["why_apex_said_this"]?.toString()?.let { appendLine(it) }
            appendLine()
            parsedData["conditions_to_watch"]?.toString()?.let { appendLine("Watch: $it") }
            parsedData["invalidation_triggers"]?.toString()?.let { appendLine("Breaks if: $it") }
            appendLine()
            parsedData["risk_caveats"]?.toString()?.let { appendLine("⚠️ $it") }
            appendLine()
            parsedData["confidence_statement"]?.toString()?.let { appendLine(it) }
            parsedData["next_scan_suggestion"]?.toString()?.let { appendLine("Next: $it") }
        }.trim()
    }

    private fun getTierString(): String {
        // Map EntitlementManager tiers to QuotaGate tier strings
        return when (EntitlementManager.currentTier.value) {
            SubscriptionTier.FREE -> "FREE"
            SubscriptionTier.BASIC -> "BASIC"
            SubscriptionTier.PRO -> "PRO"
            SubscriptionTier.APEX -> "APEX"
        }
    }
}
