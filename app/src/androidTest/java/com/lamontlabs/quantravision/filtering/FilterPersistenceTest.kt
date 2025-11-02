package com.lamontlabs.quantravision.filtering

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lamontlabs.quantravision.detection.filtering.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class FilterPersistenceTest {
    
    private lateinit var context: Context
    private lateinit var filterPreferences: FilterPreferences
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        filterPreferences = FilterPreferences.getInstance(context)
        filterPreferences.resetFilter()
    }
    
    @Test
    fun testSaveAndLoadFilter() {
        val filter = PatternFilter(
            patternTypes = setOf(PatternType.REVERSAL, PatternType.CONTINUATION),
            confidenceLevels = setOf(ConfidenceLevel.HIGH),
            timeframes = setOf("1h", "4h"),
            invalidationStatus = setOf(InvalidationStatus.ACTIVE)
        )
        
        filterPreferences.saveFilter(filter)
        
        val loadedFilter = filterPreferences.loadFilter()
        
        assertEquals(filter.patternTypes, loadedFilter.patternTypes)
        assertEquals(filter.confidenceLevels, loadedFilter.confidenceLevels)
        assertEquals(filter.timeframes, loadedFilter.timeframes)
        assertEquals(filter.invalidationStatus, loadedFilter.invalidationStatus)
    }
    
    @Test
    fun testDefaultFilter() {
        val defaultFilter = filterPreferences.loadFilter()
        
        assertTrue(defaultFilter.patternTypes.contains(PatternType.ALL))
        assertTrue(defaultFilter.confidenceLevels.contains(ConfidenceLevel.ALL))
        assertTrue(defaultFilter.timeframes.isEmpty())
    }
    
    @Test
    fun testResetFilter() {
        val filter = PatternFilter(
            patternTypes = setOf(PatternType.BREAKOUT),
            confidenceLevels = setOf(ConfidenceLevel.MEDIUM),
            timeframes = setOf("15m"),
            invalidationStatus = setOf(InvalidationStatus.ACTIVE)
        )
        
        filterPreferences.saveFilter(filter)
        filterPreferences.resetFilter()
        
        val loadedFilter = filterPreferences.loadFilter()
        assertTrue(loadedFilter.isEmpty())
    }
    
    @Test
    fun testMultipleTimeframesFilter() {
        val filter = PatternFilter(
            patternTypes = setOf(PatternType.ALL),
            confidenceLevels = setOf(ConfidenceLevel.ALL),
            timeframes = setOf("1m", "5m", "15m", "1h", "4h", "daily"),
            invalidationStatus = setOf(InvalidationStatus.ALL)
        )
        
        filterPreferences.saveFilter(filter)
        val loadedFilter = filterPreferences.loadFilter()
        
        assertEquals(6, loadedFilter.timeframes.size)
        assertTrue(loadedFilter.timeframes.contains("1m"))
        assertTrue(loadedFilter.timeframes.contains("daily"))
    }
}
