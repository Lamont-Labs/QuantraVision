package com.lamontlabs.quantravision.vision

import org.junit.Assert.*
import org.junit.Test

class ScreenChartExtractorTest {
    @Test fun measuresColoredBodiesAndWicks() {
        val candles = ScreenChartExtractor().extract(400, 240, fixture())
        assertEquals(12, candles.size)
        val first = candles.first()
        assertEquals(20, first.left)
        assertEquals(27, first.right)
        assertEquals(65, first.highY)
        assertEquals(102, first.lowY)
        assertEquals(76, first.bodyTop)
        assertEquals(91, first.bodyBottom)
        assertTrue(first.bullish)
        assertFalse(candles[1].bullish)
    }
    @Test fun blankAndGrayscaleImagesHaveNoCandles() {
        assertTrue(ScreenChartExtractor().extract(400, 240, IntArray(400 * 240) { 0xff101010.toInt() }).isEmpty())
        val pixels = fixture().map { p -> if (p != 0xff101010.toInt()) 0xffaaaaaa.toInt() else p }.toIntArray()
        assertTrue(ScreenChartExtractor().extract(400, 240, pixels).isEmpty())
    }
    @Test fun rejectsTooFewCandlesAndVolumeBars() {
        assertTrue(ScreenChartExtractor().extract(400, 240, fixture(count = 9)).isEmpty())
        assertTrue(ScreenChartExtractor().extract(400, 240, fixture(wicks = false)).isEmpty())
    }
    @Test fun colorsAloneDoNotMakeAChart() {
        val noise = java.util.Random(42).let { random -> IntArray(400 * 240) { random.nextInt() or 0xff000000.toInt() } }
        assertTrue(ScreenChartExtractor().extract(400, 240, noise).isEmpty())
    }
    @Test fun measurementsAreDeterministic() {
        val extractor = ScreenChartExtractor()
        assertEquals(extractor.extract(400, 240, fixture()), extractor.extract(400, 240, fixture()))
    }
    @Test fun changingImageChangesMeasurements() {
        val extractor = ScreenChartExtractor()
        assertNotEquals(extractor.extract(400, 240, fixture()), extractor.extract(400, 240, fixture(offset = 20)))
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsMismatchedPixelBuffer() {
        ScreenChartExtractor().extract(400, 240, IntArray(12))
    }
    companion object {
        fun fixture(count: Int = 12, wicks: Boolean = true, offset: Int = 0): IntArray {
            val pixels = IntArray(400 * 240) { 0xff101010.toInt() }
            for (i in 0 until count) {
                val left = 20 + i * 28
                val top = 76 + (i % 3) * 9 + offset
                val color = if (i % 2 == 0) 0xff22cc88.toInt() else 0xffee4455.toInt()
                for (y in top..top + 14) for (x in left..left + 6) pixels[y * 400 + x] = color
                if (wicks) for (y in top - 11..top + 25) pixels[y * 400 + left + 3] = color
            }
            return pixels
        }
    }
}
