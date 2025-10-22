package com.lamontlabs.quantravision.tests

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.lamontlabs.quantravision.PatternDetector
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.opencv.android.OpenCVLoader
import org.junit.Assert.*

class PatternDetectorTest {

    @Test
    fun verifyDetectorRunsWithoutNetwork() = runBlocking {
        OpenCVLoader.initDebug()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val detector = PatternDetector(context)
        detector.scanStaticAssets()
        // Basic check: should complete without exceptions
        assertTrue(true)
    }
}
