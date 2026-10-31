package com.lamontlabs.quantravision.pipeline

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import androidx.test.core.app.ApplicationProvider
import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.detection.DetectionResult
import com.lamontlabs.quantravision.tiers.Tier
import com.lamontlabs.quantravision.vision.ScreenChartExtractorTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ScanPipelineTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private fun chart(): Bitmap = Bitmap.createBitmap(ScreenChartExtractorTest.fixture(), 400, 240, Bitmap.Config.ARGB_8888)
    private fun vision() = VisionManager(context, object : ChartTextReader {
        override suspend fun read(bitmap: Bitmap) = "NASDAQ: AAPL 5m"
    })
    private class MemoryQuota(var allowed: Boolean = true, var canRecord: Boolean = true) : ScanQuota {
        var recorded = 0
        override fun checkScanQuota() = QuotaGate.QuotaCheckResult(allowed, if (allowed) 3 else 0, 3, Tier.FREE)
        override fun recordScan(): Boolean { if (canRecord) recorded++; return canRecord }
    }
    @Test fun fullImagePathReturnsActualMatchesWithoutInventedPrices() = runBlocking {
        val quota = MemoryQuota()
        val expected = DetectionResult("Double Bottom", 0.91, RectF(10f, 40f, 200f, 180f))
        val coordinator = QuantraPipelineCoordinator(context, { listOf(expected) }, vision(), quota)
        val result = coordinator.executePipeline(chart()) as QuantraPipelineCoordinator.PipelineResult.Success
        assertEquals(1, result.patterns.size)
        assertEquals("Double Bottom", result.patterns.single().patternName)
        assertNull(result.patterns.single().toPatternMatch().tradeScenario)
        assertEquals(ApexStatus.WAIT, result.apexResult.status)
        assertFalse(result.apexResult.regimeOk)
        assertTrue(result.overlayData.shouldRender)
        assertEquals(1, quota.recorded)
        assertEquals("5m", result.patterns.single().timeframe)
        assertEquals(64, result.apexResult.proofHash.length)
    }
    @Test fun unreadableImageNeverCallsDetectorOrConsumesQuota() = runBlocking {
        var called = false
        val quota = MemoryQuota()
        val coordinator = QuantraPipelineCoordinator(context, { called = true; emptyList() }, vision(), quota)
        val blank = Bitmap.createBitmap(400, 240, Bitmap.Config.ARGB_8888)
        assertTrue(coordinator.executePipeline(blank) is QuantraPipelineCoordinator.PipelineResult.FailClosed)
        assertFalse(called)
        assertEquals(0, quota.recorded)
    }
    @Test fun exhaustedQuotaBlocksAllDetection() = runBlocking {
        var called = false
        val quota = MemoryQuota(allowed = false)
        val coordinator = QuantraPipelineCoordinator(context, { called = true; emptyList() }, vision(), quota)
        assertTrue(coordinator.executePipeline(chart()) is QuantraPipelineCoordinator.PipelineResult.QuotaExhausted)
        assertFalse(called)
        assertEquals(0, quota.recorded)
    }
    @Test fun storageFailureWithholdsResults() = runBlocking {
        val coordinator = QuantraPipelineCoordinator(context,
            { listOf(DetectionResult("Triangle", 0.9, RectF(20f, 50f, 100f, 150f))) }, vision(), MemoryQuota(canRecord = false))
        val result = coordinator.executePipeline(chart()) as QuantraPipelineCoordinator.PipelineResult.FailClosed
        assertEquals("quota", result.stage)
    }
    @Test fun detectorExceptionDoesNotBecomeSuccess() = runBlocking {
        val quota = MemoryQuota()
        val coordinator = QuantraPipelineCoordinator(context, { error("detector failed") }, vision(), quota)
        val result = coordinator.executePipeline(chart()) as QuantraPipelineCoordinator.PipelineResult.FailClosed
        assertEquals("templates", result.stage)
        assertEquals(0, quota.recorded)
    }
    @Test fun cancellationDuringDetectionDoesNotConsumeQuota() = runBlocking {
        val quota = MemoryQuota()
        val coordinator = QuantraPipelineCoordinator(context, {
            currentCoroutineContext().cancel()
            emptyList()
        }, vision(), quota)
        val scan = launch { coordinator.executePipeline(chart()) }
        scan.join()
        assertTrue(scan.isCancelled)
        assertEquals(0, quota.recorded)
    }
    @Test fun invalidDetectionCoordinatesFailClosed() = runBlocking {
        val coordinator = QuantraPipelineCoordinator(context,
            { listOf(DetectionResult("Triangle", 0.9, RectF(-5f, 50f, 100f, 150f))) }, vision(), MemoryQuota())
        val result = coordinator.executePipeline(chart()) as QuantraPipelineCoordinator.PipelineResult.FailClosed
        assertEquals("templates", result.stage)
    }
    @Test fun primitivesPreserveMeasuredBodiesAndExplicitlyMarkUnavailableMarketData() = runBlocking {
        val bitmap = chart()
        val measured = vision().processImage(bitmap)!!
        val p = PrimitivesBuilder().buildPrimitives(measured, bitmap)!!
        assertEquals(PrimitiveSource.SCREEN_GEOMETRY, p.source)
        assertEquals(12, p.candles.size)
        assertEquals(0L, p.candles.first().timestamp)
        assertTrue(p.candles.all { it.volume == 0.0 })
        assertEquals(241.0 - 91.0, p.candles.first().open, 0.0)
        assertEquals(241.0 - 76.0, p.candles.first().close, 0.0)
        assertEquals(241.0 - 65.0, p.candles.first().high, 0.0)
        assertEquals(241.0 - 102.0, p.candles.first().low, 0.0)
    }
    @Test fun failedAndSuppressedStatusesNeverRender() {
        assertFalse(ScanResultPolicy.canRender(ApexStatus.FAIL))
        assertFalse(ScanResultPolicy.canRender(ApexStatus.OMEGA))
        assertFalse(ScanResultPolicy.canRender(ApexStatus.SUPPRESSED))
    }
}
