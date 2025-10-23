package com.lamontlabs.quantravision.tests.instrumented

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lamontlabs.quantravision.overlay.OverlayRenderer
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test verifying overlay rendering
 * under static chart image input.
 */
@RunWith(AndroidJUnit4::class)
class TestOverlayRender {

    @Test
    fun test_overlayHighlightsAppear() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val renderer = OverlayRenderer(appContext)
        val result = renderer.testRender("sample_chart.png")
        assertTrue(result.success)
        assertTrue(result.confidence > 0.5)
    }

    @Test
    fun test_overlaySafetySwitch() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val renderer = OverlayRenderer(appContext)
        renderer.forceSafeMode()
        assertTrue(renderer.isSafeMode)
    }
}
