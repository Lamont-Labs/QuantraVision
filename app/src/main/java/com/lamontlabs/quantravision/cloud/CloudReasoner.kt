package com.lamontlabs.quantravision.cloud

import android.content.Context
import com.lamontlabs.quantravision.apex.models.ApexResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * CloudReasoner
 * Handles cloud LLM API calls for Apex narration.
 * - Uses OpenAI GPT-4 API for paid tiers only
 * - Never sends images/screenshots, only structured Apex packets
 * - Tier-based token limits: PRO=180, ULTRA=380
 * - 15 second timeout enforced
 */
class CloudReasoner(private val context: Context) {

    companion object {
        private const val TAG = "CloudReasoner"
        private const val OPENAI_API_ENDPOINT = "https://api.openai.com/v1/chat/completions"
        private const val REQUEST_TIMEOUT_MS = 15_000L
        private const val PRO_MAX_TOKENS = 180
        private const val ULTRA_MAX_TOKENS = 380
        private const val MODEL_PRO = "gpt-4o-mini"
        private const val MODEL_ULTRA = "gpt-4o"
    }

    sealed class NarrationResult {
        data class Success(val explanation: String) : NarrationResult()
        data class Failure(val reason: String) : NarrationResult()
    }

    /**
     * Generate cloud narration for Apex result.
     * @param apexResult The Apex Engine output to narrate
     * @param tier User subscription tier (PRO or ULTRA)
     * @return NarrationResult with explanation or failure reason
     */
    suspend fun narrate(
        apexResult: ApexResult,
        tier: String
    ): NarrationResult = withContext(Dispatchers.IO) {
        try {
            Timber.d("$TAG: Starting cloud narration for tier=$tier, status=${apexResult.status}")
            
            val apiKey = getOpenAIApiKey()
            if (apiKey.isNullOrBlank()) {
                Timber.e("$TAG: OpenAI API key not configured")
                return@withContext NarrationResult.Failure("API key not configured")
            }
            
            val maxTokens = getMaxTokensForTier(tier)
            val model = getModelForTier(tier)
            val payload = buildPayload(apexResult, tier, model, maxTokens)
            
            Timber.v("$TAG: Calling OpenAI API (model=$model, maxTokens=$maxTokens)")
            
            val response = withTimeout(REQUEST_TIMEOUT_MS) {
                callOpenAI(payload, apiKey)
            }
            
            Timber.d("$TAG: Cloud narration received (${response.length} chars)")
            NarrationResult.Success(response)
            
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Cloud narration failed")
            NarrationResult.Failure(e.message ?: "Unknown error")
        }
    }

    private suspend fun callOpenAI(payload: String, apiKey: String): String {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(OPENAI_API_ENDPOINT)
                connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
                connection.doOutput = true
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(payload)
                    writer.flush()
                }
                
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    Timber.e("$TAG: HTTP error $responseCode: $errorStream")
                    throw Exception("HTTP $responseCode: ${errorStream ?: "Unknown error"}")
                }
                
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val responseJson = JSONObject(responseText)
                
                val choices = responseJson.optJSONArray("choices")
                if (choices == null || choices.length() == 0) {
                    throw Exception("No choices in response")
                }
                
                val message = choices.getJSONObject(0)
                    .optJSONObject("message")
                    ?: throw Exception("No message in response")
                
                message.optString("content", "").also {
                    if (it.isBlank()) {
                        throw Exception("Empty content in response")
                    }
                }
                
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun buildPayload(
        apexResult: ApexResult,
        tier: String,
        model: String,
        maxTokens: Int
    ): String {
        val topProtocols = apexResult.protocolTrace
            .take(5)
            .map { it.protocolId }
        
        val inputData = JSONObject().apply {
            put("scan_id", apexResult.scanId)
            put("tier", tier.uppercase(Locale.US))
            put("status", apexResult.status.name)
            put("quantra_score", apexResult.quantraScore.normalizedScore)
            put("confidence_apex", apexResult.confidenceApex)
            put("entropy_score", apexResult.entropyScore)
            put("suppression_active", apexResult.suppressionActive)
            put("regime_ok", apexResult.regimeOk)
            put("trace_top", JSONArray(topProtocols))
            put("invalidation_points", JSONArray(apexResult.invalidationPoints))
        }
        
        val systemPrompt = """You are ApexNarrator, an educational chart analysis assistant. Your role is to explain the Apex Engine's reasoning in clear, educational language.

CRITICAL RULES:
- Never use financial advice words: buy, sell, long, short, enter, exit, stop loss, take profit, target price, prediction, forecast, signal, trade setup
- Only narrate what the Apex Engine detected and why
- Educational context only, never trading instructions
- Output must be valid JSON matching the exact schema

Required JSON schema:
{
  "scan_id": "string",
  "status_echo": "PASS|WAIT|FAIL|SUPPRESSED|OMEGA",
  "headline": "string",
  "what_was_seen": "string",
  "why_apex_said_this": "string",
  "conditions_to_watch": "string",
  "invalidation_triggers": "string",
  "risk_caveats": "string",
  "confidence_statement": "string",
  "next_scan_suggestion": "string"
}"""

        val userPrompt = "Narrate this Apex analysis:\n${inputData.toString(2)}"
        
        return JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userPrompt)
                })
            })
            put("max_tokens", maxTokens)
            put("temperature", 0.3)
            put("response_format", JSONObject().apply {
                put("type", "json_object")
            })
        }.toString()
    }

    private fun getOpenAIApiKey(): String? {
        return try {
            val prefs = context.getSharedPreferences("cloud_config", Context.MODE_PRIVATE)
            prefs.getString("openai_api_key", null)
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Failed to get API key")
            null
        }
    }

    private fun getMaxTokensForTier(tier: String): Int {
        return when (tier.uppercase(Locale.US)) {
            "PRO" -> PRO_MAX_TOKENS
            "ULTRA", "APEX_ULTRA" -> ULTRA_MAX_TOKENS
            else -> PRO_MAX_TOKENS
        }
    }

    private fun getModelForTier(tier: String): String {
        return when (tier.uppercase(Locale.US)) {
            "PRO" -> MODEL_PRO
            "ULTRA", "APEX_ULTRA" -> MODEL_ULTRA
            else -> MODEL_PRO
        }
    }
}
