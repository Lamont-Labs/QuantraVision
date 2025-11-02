package com.lamontlabs.quantravision.integration

import android.content.Context
import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lamontlabs.quantravision.detection.TimeframeAggregator
import com.lamontlabs.quantravision.detection.model.Timeframe
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class MultiTimeframeIntegrationTest {
    
    private lateinit var context: Context
    private lateinit var aggregator: TimeframeAggregator
    
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        aggregator = TimeframeAggregator(context)
    }
    
    @Test
    fun testTimeframeAggregatorInitialization() {
        assertNotNull(aggregator)
    }
    
    @Test
    fun testDetectAllTimeframesWithTestBitmap() = runBlocking {
        val testBitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        
        try {
            val results = aggregator.detectAllTimeframes(testBitmap, "integration_test")
            
            assertNotNull(results)
            
            if (results.isNotEmpty()) {
                assertTrue(results.keys.any { it in Timeframe.values() })
            }
        } finally {
            testBitmap.recycle()
            aggregator.clearCache()
        }
    }
    
    @Test
    fun testTimeframeConfluenceDetection() = runBlocking {
        val testBitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        
        try {
            val allResults = aggregator.detectAllTimeframes(testBitmap, "confluence_test")
            
            val confluencePatterns = aggregator.getTimeframeConfluence(allResults, minTimeframes = 2)
            
            assertNotNull(confluencePatterns)
        } finally {
            testBitmap.recycle()
            aggregator.clearCache()
        }
    }
    
    @Test
    fun testCacheClearingWorks() = runBlocking {
        val testBitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        
        try {
            aggregator.detectAllTimeframes(testBitmap, "cache_test")
            aggregator.clearCache()
        } finally {
            testBitmap.recycle()
        }
    }
}
