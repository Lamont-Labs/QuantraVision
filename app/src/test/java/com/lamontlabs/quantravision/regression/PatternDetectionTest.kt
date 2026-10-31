package com.lamontlabs.quantravision.regression

import com.lamontlabs.quantravision.detection.SourceBounds
import org.junit.Assert.*
import org.junit.Test

/** Regression tests for the coordinate transform used by live template matching. */
class PatternDetectionTest {
    @Test fun mapsDownscaledMatchToOriginalScreenshot() {
        val b = SourceBounds.fromScaled(50.0, 75.0, 40.0, 20.0, 0.5)
        assertEquals(SourceBounds(100.0, 150.0, 180.0, 190.0), b)
    }
    @Test fun mapsUpscaledMatchToOriginalScreenshot() {
        val b = SourceBounds.fromScaled(200.0, 100.0, 80.0, 40.0, 2.0)
        assertEquals(SourceBounds(100.0, 50.0, 140.0, 70.0), b)
    }
    @Test(expected = IllegalArgumentException::class) fun zeroScaleIsRejected() {
        SourceBounds.fromScaled(1.0, 1.0, 10.0, 10.0, 0.0)
    }
    @Test(expected = IllegalArgumentException::class) fun nonFiniteCoordinatesAreRejected() {
        SourceBounds.fromScaled(Double.NaN, 1.0, 10.0, 10.0, 1.0)
    }
}
