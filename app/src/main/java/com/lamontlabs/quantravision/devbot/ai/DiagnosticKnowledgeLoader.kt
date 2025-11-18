package com.lamontlabs.quantravision.devbot.ai

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lamontlabs.quantravision.devbot.data.DiagnosticEvent
import java.io.InputStreamReader

class DiagnosticKnowledgeLoader(private val context: Context) {
    private val gson = Gson()
    private val knowledgeCache = mutableMapOf<String, ErrorKnowledge>()
    private val categoryIndex = mutableMapOf<String, List<String>>()
    
    private val categories = listOf(
        "crashes",
        "memory",
        "ui_threading",
        "network",
        "database",
        "quantravision",
        "framework",
        "coroutines",
        "build"
    )
    
    suspend fun loadKnowledge() {
        categories.forEach { category ->
            loadCategoryKnowledge(category)
        }
        Log.d("DiagnosticKnowledgeLoader", "Loaded ${knowledgeCache.size} error patterns")
    }
    
    private fun loadCategoryKnowledge(category: String) {
        try {
            val assetPath = "diagnostic_knowledge/$category"
            val files = context.assets.list(assetPath) ?: emptyArray()
            
            val categoryErrors = mutableListOf<String>()
            
            files.filter { it.endsWith(".json") }.forEach { file ->
                try {
                    val inputStream = context.assets.open("$assetPath/$file")
                    val reader = InputStreamReader(inputStream)
                    
                    val error = gson.fromJson(reader, ErrorKnowledge::class.java)
                    knowledgeCache[error.errorName] = error
                    categoryErrors.add(error.errorName)
                    
                    reader.close()
                } catch (e: Exception) {
                    Log.e("DiagnosticKnowledgeLoader", "Error loading $file", e)
                }
            }
            
            categoryIndex[category] = categoryErrors
            
        } catch (e: Exception) {
            Log.e("DiagnosticKnowledgeLoader", "Error loading category $category", e)
        }
    }
    
    fun getRelevantKnowledge(
        query: String,
        recentEvents: List<DiagnosticEvent>
    ): List<ErrorKnowledge> {
        val lowerQuery = query.lowercase()
        val relevantKnowledge = mutableListOf<ErrorKnowledge>()
        
        knowledgeCache.values.forEach { error ->
            val score = calculateRelevanceScore(error, lowerQuery, recentEvents)
            if (score > 0) {
                relevantKnowledge.add(error)
            }
        }
        
        return relevantKnowledge
            .sortedByDescending { calculateRelevanceScore(it, lowerQuery, recentEvents) }
            .take(5)
    }
    
    private fun calculateRelevanceScore(
        error: ErrorKnowledge,
        query: String,
        events: List<DiagnosticEvent>
    ): Int {
        var score = 0
        
        if (error.errorName.lowercase().contains(query)) score += 10
        if (error.description.lowercase().contains(query)) score += 5
        if (error.category.lowercase().contains(query)) score += 3
        
        events.forEach { event ->
            val eventText = event.message.lowercase()
            if (error.errorName.lowercase() in eventText) score += 8
            if (error.description.lowercase().any { it.toString() in eventText }) score += 2
        }
        
        return score
    }
    
    fun getErrorByName(errorName: String): ErrorKnowledge? {
        return knowledgeCache[errorName]
    }
    
    fun getAllInCategory(category: String): List<ErrorKnowledge> {
        val errorNames = categoryIndex[category] ?: return emptyList()
        return errorNames.mapNotNull { knowledgeCache[it] }
    }
    
    fun searchErrors(query: String): List<ErrorKnowledge> {
        val lowerQuery = query.lowercase()
        return knowledgeCache.values.filter { error ->
            error.errorName.lowercase().contains(lowerQuery) ||
            error.description.lowercase().contains(lowerQuery) ||
            error.category.lowercase().contains(lowerQuery)
        }
    }
}
