package com.lamontlabs.quantravision.apex

import com.lamontlabs.quantravision.apex.models.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class ApexGuardRegressionTest {
    private val context = ApexScanContext("TEST", "5m", "Candlestick", 1L, "test-user", SubscriptionTier.FREE)
    private fun observations() = ChartPrimitives("a".repeat(64), List(12) { i ->
        Candle(i.toLong(), 12.0, 15.0, 10.0, 14.0)
    }, chartType = "Candlestick", source = PrimitiveSource.SCREEN_GEOMETRY, imageWidth = 400, imageHeight = 240)
    @Test fun allRegistriesInitializeBeforeRegistration() {
        assertEquals(4, ProtocolRegistryMobile.getOmegaProtocols().size)
        assertEquals(80, ProtocolRegistryMobile.getTierProtocols().size)
        assertEquals(25, ProtocolRegistryMobile.getLearningProtocols().size)
    }
    @Test fun missingCandlesEngageSafetyLock() = runBlocking {
        val result = ApexEngineMobile.runScan(context, observations().copy(candles = emptyList()), 1)
        assertEquals(ApexStatus.OMEGA, result.status)
        assertTrue(result.omegaLock)
        assertTrue(result.protocolTrace.any { !it.passed })
    }
    @Test fun nanAndInvalidBodiesCannotPass() = runBlocking {
        for (bad in listOf(Double.NaN, Double.POSITIVE_INFINITY, -1.0, 25.0)) {
            val p = observations().let { it.copy(candles = it.candles.map { c -> c.copy(open = bad) }) }
            assertEquals(ApexStatus.OMEGA, ApexEngineMobile.runScan(context, p, 1).status)
        }
    }
    @Test fun validScreenGeometryRemainsUnconfirmed() = runBlocking {
        val result = ApexEngineMobile.runScan(context, observations(), 1)
        assertEquals(ApexStatus.WAIT, result.status)
        assertFalse(result.omegaLock)
        assertFalse(result.regimeOk)
        assertEquals(0.0, result.confidenceApex, 0.0)
        assertTrue(result.protocolTrace.any { it.protocolId == "IMG02" && !it.passed })
    }
    @Test fun weightedScoreUsesWeightsAndConfidence() {
        val verdicts = listOf(ProtocolVerdict("A", "A", true, 0.8, "", 3.0),
            ProtocolVerdict("B", "B", false, 0.9, "", 1.0))
        assertEquals(0.6, ApexEngineMobile.weightedScore(verdicts), 0.000001)
        assertEquals(0.0, ApexEngineMobile.weightedScore(listOf(verdicts.first().copy(confidence = Double.NaN))), 0.0)
    }
    @Test fun proofBindsInputAndDoesNotDependOnWallClockOrLocale() = runBlocking {
        val old = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            val a = ApexEngineMobile.runScan(context, observations(), 1)
            Locale.setDefault(Locale.GERMANY)
            val b = ApexEngineMobile.runScan(context, observations(), 999)
            assertEquals(a.proofHash, b.proofHash)
            val c = ApexEngineMobile.runScan(context, observations().copy(rawImageHash = "b".repeat(64)), 1)
            assertNotEquals(a.proofHash, c.proofHash)
        } finally { Locale.setDefault(old) }
    }
}
