package com.lamontlabs.quantravision.cloud

import org.junit.Assert.*
import org.junit.Test

/**
 * BATCH 10: LLMContractValidator Unit Tests
 * 
 * Comprehensive test suite for LLM response validation.
 * Tests forbidden word detection, schema validation, status echo, and token limits.
 * 
 * Test Coverage:
 * 1. Forbidden Words: buy, sell, long, short, enter, exit, stop loss, take profit, etc.
 * 2. Schema Validation: all 10 required fields, missing fields, empty fields
 * 3. Status Echo: must match input status
 * 4. Token Limits: PRO=180, ULTRA=380 (words × 1.3)
 */
class LLMContractValidatorTest {
    
    // ============================================================
    // FORBIDDEN WORDS TESTS
    // ============================================================
    
    @Test
    fun `detects forbidden word buy in response`() {
        val response = """
            {
                "scan_id": "test",
                "status_echo": "PASS",
                "headline": "You should buy this stock",
                "what_was_seen": "Pattern detected",
                "why_apex_said_this": "Protocol passed",
                "conditions_to_watch": "Volume",
                "invalidation_triggers": "Break below",
                "risk_caveats": "Educational only",
                "confidence_statement": "High confidence",
                "next_scan_suggestion": "Re-scan later"
            }
        """.trimIndent()
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse("Response with 'buy' should be invalid", result.isValid)
        assertTrue(
            "Violations should mention 'buy'",
            result.violations.any { it.contains("buy", ignoreCase = true) }
        )
    }
    
    @Test
    fun `detects forbidden word sell in response`() {
        val response = validJsonWithField("headline", "Consider selling at resistance")
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("sell", ignoreCase = true) })
    }
    
    @Test
    fun `detects forbidden word long in response`() {
        val response = validJsonWithField("what_was_seen", "Go long on this pattern")
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("long", ignoreCase = true) })
    }
    
    @Test
    fun `detects forbidden word short in response`() {
        val response = validJsonWithField("headline", "Short opportunity detected")
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("short", ignoreCase = true) })
    }
    
    @Test
    fun `detects forbidden word enter in response`() {
        val response = validJsonWithField("conditions_to_watch", "Enter position near support")
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("enter", ignoreCase = true) })
    }
    
    @Test
    fun `detects forbidden word exit in response`() {
        val response = validJsonWithField("invalidation_triggers", "Exit if broken")
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("exit", ignoreCase = true) })
    }
    
    @Test
    fun `detects forbidden phrase stop loss in response`() {
        val response = validJsonWithField("risk_caveats", "Use a stop loss at support")
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("stop loss", ignoreCase = true) })
    }
    
    @Test
    fun `detects forbidden phrase take profit in response`() {
        val response = validJsonWithField("conditions_to_watch", "Set take profit at resistance")
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("take profit", ignoreCase = true) })
    }
    
    @Test
    fun `detects forbidden phrase target price in response`() {
        val response = validJsonWithField("headline", "Target price is $150")
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("target price", ignoreCase = true) })
    }
    
    @Test
    fun `detects forbidden word prediction in response`() {
        val response = validJsonWithField("why_apex_said_this", "My prediction is bullish")
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("prediction", ignoreCase = true) })
    }
    
    @Test
    fun `detects forbidden word forecast in response`() {
        val response = validJsonWithField("confidence_statement", "Forecast suggests upward movement")
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("forecast", ignoreCase = true) })
    }
    
    @Test
    fun `detects forbidden word signal in response`() {
        val response = validJsonWithField("headline", "Strong signal to act")
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("signal", ignoreCase = true) })
    }
    
    @Test
    fun `detects forbidden phrase trade setup in response`() {
        val response = validJsonWithField("what_was_seen", "Perfect trade setup forming")
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("trade setup", ignoreCase = true) })
    }
    
    @Test
    fun `forbidden word detection is case-insensitive`() {
        val responseLower = validJsonWithField("headline", "you should buy now")
        val responseUpper = validJsonWithField("headline", "YOU SHOULD BUY NOW")
        val responseMixed = validJsonWithField("headline", "You Should Buy Now")
        
        assertFalse(LLMContractValidator.validate(responseLower, "PASS", "PRO").isValid)
        assertFalse(LLMContractValidator.validate(responseUpper, "PASS", "PRO").isValid)
        assertFalse(LLMContractValidator.validate(responseMixed, "PASS", "PRO").isValid)
    }
    
    @Test
    fun `detects partial word matches - buying contains buy`() {
        val response = validJsonWithField("headline", "Consider buying opportunities")
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse("'buying' contains forbidden 'buy'", result.isValid)
    }
    
    @Test
    fun `detects partial word matches - selling contains sell`() {
        val response = validJsonWithField("headline", "Selling pressure detected")
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse("'selling' contains forbidden 'sell'", result.isValid)
    }
    
    // ============================================================
    // SCHEMA VALIDATION TESTS
    // ============================================================
    
    @Test
    fun `rejects response with missing scan_id field`() {
        val response = """
            {
                "status_echo": "PASS",
                "headline": "Pattern confirmed",
                "what_was_seen": "Signals detected",
                "why_apex_said_this": "Protocols validated",
                "conditions_to_watch": "Volume",
                "invalidation_triggers": "Break below",
                "risk_caveats": "Educational only",
                "confidence_statement": "High",
                "next_scan_suggestion": "Re-scan"
            }
        """.trimIndent()
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("scan_id", ignoreCase = true) })
    }
    
    @Test
    fun `rejects response with missing headline field`() {
        val response = """
            {
                "scan_id": "test",
                "status_echo": "PASS",
                "what_was_seen": "Signals detected",
                "why_apex_said_this": "Protocols validated",
                "conditions_to_watch": "Volume",
                "invalidation_triggers": "Break below",
                "risk_caveats": "Educational only",
                "confidence_statement": "High",
                "next_scan_suggestion": "Re-scan"
            }
        """.trimIndent()
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("headline", ignoreCase = true) })
    }
    
    @Test
    fun `rejects response with empty required fields`() {
        val response = """
            {
                "scan_id": "",
                "status_echo": "PASS",
                "headline": "",
                "what_was_seen": "",
                "why_apex_said_this": "Protocol passed",
                "conditions_to_watch": "Volume",
                "invalidation_triggers": "Break below",
                "risk_caveats": "Educational only",
                "confidence_statement": "High confidence",
                "next_scan_suggestion": "Re-scan later"
            }
        """.trimIndent()
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse("Empty fields should be rejected", result.isValid)
        assertTrue(result.violations.size >= 3)
    }
    
    @Test
    fun `validates correct response with all 10 required fields`() {
        val response = """
            {
                "scan_id": "test-123",
                "status_echo": "PASS",
                "headline": "Strong continuation pattern confirmed",
                "what_was_seen": "Multiple alignment indicators detected",
                "why_apex_said_this": "Protocols T45 and T50 validated structure",
                "conditions_to_watch": "Monitor volume stability and trend strength",
                "invalidation_triggers": "Pattern invalidates if support breaks",
                "risk_caveats": "Educational analysis only, not financial advice",
                "confidence_statement": "Apex confidence: 82%",
                "next_scan_suggestion": "Re-scan if price approaches key levels"
            }
        """.trimIndent()
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertTrue("Valid response should pass", result.isValid)
        assertTrue("No violations expected", result.violations.isEmpty())
        assertNotNull("Parsed data should be available", result.parsedData)
        assertEquals(10, result.parsedData?.size)
    }
    
    @Test
    fun `allows extra fields beyond required 10`() {
        val response = """
            {
                "scan_id": "test-123",
                "status_echo": "PASS",
                "headline": "Pattern confirmed",
                "what_was_seen": "Signals detected",
                "why_apex_said_this": "Protocols validated",
                "conditions_to_watch": "Volume stability",
                "invalidation_triggers": "Support break",
                "risk_caveats": "Educational only",
                "confidence_statement": "High confidence",
                "next_scan_suggestion": "Re-scan later",
                "extra_field_1": "Extra data",
                "extra_field_2": "More extra data"
            }
        """.trimIndent()
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertTrue("Extra fields should not break validation", result.isValid)
    }
    
    // ============================================================
    // STATUS ECHO TESTS
    // ============================================================
    
    @Test
    fun `status_echo must match input status - PASS`() {
        val response = validJsonWithStatus("PASS")
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertTrue(result.isValid)
    }
    
    @Test
    fun `status_echo must match input status - WAIT`() {
        val response = validJsonWithStatus("WAIT")
        
        val result = LLMContractValidator.validate(response, "WAIT", "PRO")
        
        assertTrue(result.isValid)
    }
    
    @Test
    fun `status_echo must match input status - FAIL`() {
        val response = validJsonWithStatus("FAIL")
        
        val result = LLMContractValidator.validate(response, "FAIL", "PRO")
        
        assertTrue(result.isValid)
    }
    
    @Test
    fun `status_echo mismatch is rejected - expected PASS got WAIT`() {
        val response = validJsonWithStatus("WAIT")
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("status_echo", ignoreCase = true) })
    }
    
    @Test
    fun `status_echo matching is case-insensitive`() {
        val responseLower = validJsonWithStatus("pass")
        val responseUpper = validJsonWithStatus("PASS")
        
        assertTrue(LLMContractValidator.validate(responseLower, "PASS", "PRO").isValid)
        assertTrue(LLMContractValidator.validate(responseUpper, "pass", "PRO").isValid)
    }
    
    // ============================================================
    // TOKEN LIMIT TESTS
    // ============================================================
    
    @Test
    fun `PRO tier enforces 180 token limit`() {
        val response = validJsonWithLongText(150)
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertTrue("150 words should pass PRO limit (180 tokens)", result.isValid)
    }
    
    @Test
    fun `PRO tier rejects response exceeding 180 token limit`() {
        val response = validJsonWithLongText(200)
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse("200 words (~260 tokens) should exceed PRO limit", result.isValid)
        assertTrue(result.violations.any { it.contains("token", ignoreCase = true) })
    }
    
    @Test
    fun `ULTRA tier enforces 380 token limit`() {
        val response = validJsonWithLongText(280)
        
        val result = LLMContractValidator.validate(response, "PASS", "ULTRA")
        
        assertTrue("280 words should pass ULTRA limit (380 tokens)", result.isValid)
    }
    
    @Test
    fun `ULTRA tier rejects response exceeding 380 token limit`() {
        val response = validJsonWithLongText(350)
        
        val result = LLMContractValidator.validate(response, "PASS", "ULTRA")
        
        assertFalse("350 words (~455 tokens) should exceed ULTRA limit", result.isValid)
        assertTrue(result.violations.any { it.contains("token", ignoreCase = true) })
    }
    
    @Test
    fun `APEX_ULTRA maps to ULTRA token limit`() {
        val response = validJsonWithLongText(280)
        
        val result = LLMContractValidator.validate(response, "PASS", "APEX_ULTRA")
        
        assertTrue("APEX_ULTRA should use ULTRA limit", result.isValid)
    }
    
    // ============================================================
    // EDGE CASES
    // ============================================================
    
    @Test
    fun `rejects empty response string`() {
        val result = LLMContractValidator.validate("", "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("empty", ignoreCase = true) })
    }
    
    @Test
    fun `rejects blank response string`() {
        val result = LLMContractValidator.validate("   ", "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("empty", ignoreCase = true) })
    }
    
    @Test
    fun `rejects invalid JSON`() {
        val response = "{ invalid json {{{ "
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("JSON", ignoreCase = true) })
    }
    
    @Test
    fun `multiple violations reported together`() {
        val response = """
            {
                "scan_id": "test",
                "status_echo": "WAIT",
                "headline": "You should buy this stock"
            }
        """.trimIndent()
        
        val result = LLMContractValidator.validate(response, "PASS", "PRO")
        
        assertFalse(result.isValid)
        assertTrue("Should have multiple violations", result.violations.size >= 2)
    }
    
    // ============================================================
    // HELPER FUNCTIONS
    // ============================================================
    
    private fun validJsonWithField(fieldName: String, fieldValue: String): String {
        return """
            {
                "scan_id": "test-123",
                "status_echo": "PASS",
                "headline": ${if (fieldName == "headline") "\"$fieldValue\"" else "\"Valid headline\""},
                "what_was_seen": ${if (fieldName == "what_was_seen") "\"$fieldValue\"" else "\"Signals detected\""},
                "why_apex_said_this": ${if (fieldName == "why_apex_said_this") "\"$fieldValue\"" else "\"Protocols validated\""},
                "conditions_to_watch": ${if (fieldName == "conditions_to_watch") "\"$fieldValue\"" else "\"Monitor volume\""},
                "invalidation_triggers": ${if (fieldName == "invalidation_triggers") "\"$fieldValue\"" else "\"Support break\""},
                "risk_caveats": ${if (fieldName == "risk_caveats") "\"$fieldValue\"" else "\"Educational only\""},
                "confidence_statement": ${if (fieldName == "confidence_statement") "\"$fieldValue\"" else "\"High confidence\""},
                "next_scan_suggestion": ${if (fieldName == "next_scan_suggestion") "\"$fieldValue\"" else "\"Re-scan later\""}
            }
        """.trimIndent()
    }
    
    private fun validJsonWithStatus(status: String): String {
        return """
            {
                "scan_id": "test-123",
                "status_echo": "$status",
                "headline": "Pattern confirmed",
                "what_was_seen": "Signals detected",
                "why_apex_said_this": "Protocols validated",
                "conditions_to_watch": "Monitor volume",
                "invalidation_triggers": "Support break",
                "risk_caveats": "Educational only",
                "confidence_statement": "High confidence",
                "next_scan_suggestion": "Re-scan later"
            }
        """.trimIndent()
    }
    
    private fun validJsonWithLongText(wordCount: Int): String {
        val longText = (1..wordCount).joinToString(" ") { "word$it" }
        return """
            {
                "scan_id": "test-123",
                "status_echo": "PASS",
                "headline": "Pattern confirmed",
                "what_was_seen": "$longText",
                "why_apex_said_this": "Protocols validated",
                "conditions_to_watch": "Monitor volume",
                "invalidation_triggers": "Support break",
                "risk_caveats": "Educational only",
                "confidence_statement": "High confidence",
                "next_scan_suggestion": "Re-scan later"
            }
        """.trimIndent()
    }
}
