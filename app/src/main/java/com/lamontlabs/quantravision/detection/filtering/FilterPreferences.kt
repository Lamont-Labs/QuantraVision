package com.lamontlabs.quantravision.detection.filtering

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class FilterPreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "pattern_filter_prefs",
        Context.MODE_PRIVATE
    )
    
    fun saveFilter(filter: PatternFilter) {
        val json = JSONObject().apply {
            put("patternTypes", JSONArray(filter.patternTypes.map { it.name }))
            put("confidenceLevels", JSONArray(filter.confidenceLevels.map { it.name }))
            put("timeframes", JSONArray(filter.timeframes.toList()))
            put("invalidationStatus", JSONArray(filter.invalidationStatus.map { it.name }))
        }
        
        prefs.edit()
            .putString(KEY_FILTER, json.toString())
            .apply()
    }
    
    fun loadFilter(): PatternFilter {
        val jsonString = prefs.getString(KEY_FILTER, null)
        
        if (jsonString == null) {
            return PatternFilter.default()
        }
        
        return try {
            val json = JSONObject(jsonString)
            
            val patternTypes = json.optJSONArray("patternTypes")?.let { array ->
                (0 until array.length()).map { i ->
                    try {
                        PatternType.valueOf(array.getString(i))
                    } catch (e: Exception) {
                        PatternType.ALL
                    }
                }.toSet()
            } ?: setOf(PatternType.ALL)
            
            val confidenceLevels = json.optJSONArray("confidenceLevels")?.let { array ->
                (0 until array.length()).map { i ->
                    try {
                        ConfidenceLevel.valueOf(array.getString(i))
                    } catch (e: Exception) {
                        ConfidenceLevel.ALL
                    }
                }.toSet()
            } ?: setOf(ConfidenceLevel.ALL)
            
            val timeframes = json.optJSONArray("timeframes")?.let { array ->
                (0 until array.length()).map { i ->
                    array.getString(i)
                }.toSet()
            } ?: emptySet()
            
            val invalidationStatus = json.optJSONArray("invalidationStatus")?.let { array ->
                (0 until array.length()).map { i ->
                    try {
                        InvalidationStatus.valueOf(array.getString(i))
                    } catch (e: Exception) {
                        InvalidationStatus.ALL
                    }
                }.toSet()
            } ?: setOf(InvalidationStatus.ALL)
            
            PatternFilter(
                patternTypes = patternTypes,
                confidenceLevels = confidenceLevels,
                timeframes = timeframes,
                invalidationStatus = invalidationStatus
            )
        } catch (e: Exception) {
            PatternFilter.default()
        }
    }
    
    fun resetFilter() {
        prefs.edit()
            .remove(KEY_FILTER)
            .apply()
    }
    
    companion object {
        private const val KEY_FILTER = "pattern_filter"
        
        @Volatile
        private var INSTANCE: FilterPreferences? = null
        
        fun getInstance(context: Context): FilterPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FilterPreferences(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
