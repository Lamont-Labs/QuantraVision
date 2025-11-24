package com.lamontlabs.quantravision.cloud

import org.json.JSONObject
import timber.log.Timber
import java.util.Locale

/**
 * LLMContractValidator
 * Validates CloudReasoner output against strict contract rules.
 * - Checks forbidden financial advice words
 * - Verifies JSON schema completeness
 * - Validates status_echo matches input
 * - Enforces tier token limits
 */
object LLMContractValidator {

    private const val TAG = "LLMContractValidator"
    
    private val FORBIDDEN_WORDS = setOf(
        "buy", "sell", "long", "short", "enter", "exit",
        "stop loss", "stoploss", "take profit", "takeprofit",
        "target price", "prediction", "forecast", "signal",
        "trade setup", "position"
    )
    
    private val REQUIRED_FIELDS = setOf(
        "scan_id",
        "status_echo",
        "headline",
        "what_was_seen",
        "why_apex_said_this",
        "conditions_to_watch",
        "invalidation_triggers",
        "risk_caveats",
        "confidence_statement",
        "next_scan_suggestion"
    )
    
    private const val PRO_TOKEN_LIMIT = 180
    private const val ULTRA_TOKEN_LIMIT = 380
    private const val WORDS_TO_TOKENS_MULTIPLIER = 1.3

    data class ValidationResult(
        val isValid: Boolean,
        val violations: List<String>,
        val parsedData: Map<String, Any>?
    )

    /**
     * Validate LLM response against contract.
     * @param response Raw JSON string response from LLM
     * @param expectedStatus Expected status value (PASS, WAIT, FAIL, etc.)
     * @param tier User subscription tier (PRO or ULTRA)
     * @return ValidationResult with violations or parsed data
     */
    fun validate(
        response: String,
        expectedStatus: String,
        tier: String
    ): ValidationResult {
        val violations = mutableListOf<String>()
        
        Timber.d("$TAG: Validating response for status=$expectedStatus, tier=$tier")
        
        if (response.isBlank()) {
            Timber.e("$TAG: Empty response")
            return ValidationResult(false, listOf("Empty response"), null)
        }
        
        val json = try {
            JSONObject(response)
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Invalid JSON")
            return ValidationResult(false, listOf("Invalid JSON: ${e.message}"), null)
        }
        
        violations.addAll(checkSchema(json))
        
        if (!checkStatusEcho(json, expectedStatus)) {
            violations.add("status_echo mismatch: expected $expectedStatus, got ${json.optString("status_echo")}")
        }
        
        val allText = buildString {
            REQUIRED_FIELDS.forEach { field ->
                append(json.optString(field, ""))
                append(" ")
            }
        }
        
        violations.addAll(checkForbiddenWords(allText))
        
        if (!checkTokenLimit(allText, tier)) {
            violations.add("Response exceeds token limit for tier $tier")
        }
        
        val isValid = violations.isEmpty()
        
        if (isValid) {
            Timber.d("$TAG: Validation passed")
        } else {
            Timber.w("$TAG: Validation failed with ${violations.size} violations: ${violations.joinToString("; ")}")
        }
        
        val parsedData = if (isValid) {
            json.keys().asSequence().associateWith { key ->
                json.get(key)
            }
        } else {
            null
        }
        
        return ValidationResult(isValid, violations, parsedData)
    }

    private fun checkForbiddenWords(text: String): List<String> {
        val violations = mutableListOf<String>()
        val lowerText = text.lowercase(Locale.US)
        
        FORBIDDEN_WORDS.forEach { word ->
            if (lowerText.contains(word)) {
                violations.add("Forbidden word detected: '$word'")
                Timber.w("$TAG: Found forbidden word: $word")
            }
        }
        
        return violations
    }

    private fun checkSchema(json: JSONObject): List<String> {
        val violations = mutableListOf<String>()
        
        REQUIRED_FIELDS.forEach { field ->
            if (!json.has(field)) {
                violations.add("Missing required field: $field")
            } else {
                val value = json.optString(field, "")
                if (value.isBlank()) {
                    violations.add("Empty required field: $field")
                }
            }
        }
        
        return violations
    }

    private fun checkStatusEcho(json: JSONObject, expected: String): Boolean {
        val statusEcho = json.optString("status_echo", "")
        val matches = statusEcho.uppercase(Locale.US) == expected.uppercase(Locale.US)
        
        if (!matches) {
            Timber.w("$TAG: status_echo mismatch: expected=$expected, actual=$statusEcho")
        }
        
        return matches
    }

    private fun checkTokenLimit(text: String, tier: String): Boolean {
        val wordCount = text.split(Regex("\\s+")).size
        val estimatedTokens = (wordCount * WORDS_TO_TOKENS_MULTIPLIER).toInt()
        
        val limit = when (tier.uppercase(Locale.US)) {
            "PRO" -> PRO_TOKEN_LIMIT
            "ULTRA", "APEX_ULTRA" -> ULTRA_TOKEN_LIMIT
            else -> PRO_TOKEN_LIMIT
        }
        
        val withinLimit = estimatedTokens <= limit
        
        if (!withinLimit) {
            Timber.w("$TAG: Token limit exceeded: estimated=$estimatedTokens, limit=$limit")
        } else {
            Timber.v("$TAG: Token check passed: estimated=$estimatedTokens/$limit")
        }
        
        return withinLimit
    }
}
