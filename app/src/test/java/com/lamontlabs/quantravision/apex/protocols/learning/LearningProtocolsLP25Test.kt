package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.models.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class LearningProtocolsLP25Test {
    private val context = ApexScanContext("TEST", "5m", "Candlestick", 1L, "unit", SubscriptionTier.PRO)
    private val primitives = ChartPrimitives("a".repeat(64), List(50) { i -> Candle(i.toLong(), 10.0, 12.0, 9.0, 11.0) })
    private fun readyState() = mutableMapOf<String, Any>("suppressionMemoryScore" to 0.7,
        "driftAdaptationScore" to 0.6, "patternEffectivenessScore" to 0.8,
        "adaptiveConfidenceModifier" to 1.0, "learningProofDigest" to "a".repeat(64), "freshnessTokenValid" to true)
    @Test fun completeArtifactsCanFinalize() = runBlocking {
        val state = readyState()
        val verdict = LP25LearningStateFinalizer().evaluate(context, primitives, state)
        assertTrue(verdict.passed)
        assertEquals(true, state["learningStateReady"])
        assertTrue(verdict.reason.contains("a".repeat(16)))
    }
    @Test fun missingArtifactsCannotFinalize() = runBlocking {
        for (key in readyState().keys) {
            val state = readyState().apply { remove(key) }
            val verdict = LP25LearningStateFinalizer().evaluate(context, primitives, state)
            assertFalse("Missing $key must fail", verdict.passed)
            assertEquals(false, state["learningStateReady"])
        }
    }
    @Test fun invalidFreshnessCannotPreserveAReadyState() = runBlocking {
        val state = readyState().apply { put("freshnessTokenValid", false); put("learningStateReady", true) }
        val verdict = LP25LearningStateFinalizer().evaluate(context, primitives, state)
        assertFalse(verdict.passed)
        assertEquals(false, state["learningStateReady"])
    }
    @Test fun malformedDigestsAreRejected() = runBlocking {
        for (digest in listOf("", "unknown", "digest_error", "abc123", "z".repeat(64))) {
            val state = readyState().apply { put("learningProofDigest", digest) }
            assertFalse(LP25LearningStateFinalizer().evaluate(context, primitives, state).passed)
        }
    }
    @Test fun insufficientCandlesCannotFinalize() = runBlocking {
        val state = readyState()
        assertFalse(LP25LearningStateFinalizer().evaluate(context, primitives.copy(candles = emptyList()), state).passed)
        assertEquals(false, state["learningStateReady"])
    }
    @Test fun identicalArtifactsProduceIdenticalVerdicts() = runBlocking {
        val protocol = LP25LearningStateFinalizer()
        assertEquals(protocol.evaluate(context, primitives, readyState()), protocol.evaluate(context, primitives, readyState()))
    }
}
