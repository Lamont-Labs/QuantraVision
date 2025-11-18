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
    private val keywordIndex = mutableMapOf<String, MutableSet<String>>()
    
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
        buildKeywordIndex()
        Log.d("DiagnosticKnowledgeLoader", "Loaded ${knowledgeCache.size} error patterns with ${keywordIndex.size} indexed keywords")
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
    
    private fun tokenize(text: String): Set<String> {
        return text.lowercase()
            .replace(Regex("[^a-z0-9]+"), " ")
            .split(" ")
            .filter { it.length >= 3 }
            .toSet()
    }
    
    private fun buildKeywordIndex() {
        keywordIndex.clear()
        
        knowledgeCache.values.forEach { error ->
            val keywords = mutableSetOf<String>()
            
            keywords.addAll(tokenize(error.errorName))
            keywords.addAll(tokenize(error.description))
            keywords.addAll(tokenize(error.category))
            
            error.commonCauses.forEach { cause ->
                keywords.addAll(tokenize(cause))
            }
            
            error.solutions.forEach { solution ->
                keywords.addAll(tokenize(solution))
            }
            
            keywords.forEach { keyword ->
                keywordIndex.getOrPut(keyword) { mutableSetOf() }.add(error.errorName)
            }
        }
    }
    
    private fun extractEventTokens(event: DiagnosticEvent): Set<String> {
        val tokens = mutableSetOf<String>()
        
        tokens.addAll(tokenize(event.message))
        tokens.addAll(tokenize(event.source))
        
        when (event) {
            is DiagnosticEvent.Error -> {
                event.stackTrace?.let { tokens.addAll(tokenize(it)) }
            }
            is DiagnosticEvent.Crash -> {
                tokens.addAll(tokenize(event.throwable.javaClass.simpleName))
                tokens.addAll(tokenize(event.threadName))
            }
            is DiagnosticEvent.Performance -> {
                tokens.add(event.metricType.name.lowercase())
            }
            is DiagnosticEvent.Network -> {
                tokens.add(event.errorType.name.lowercase())
                tokens.addAll(tokenize(event.url))
            }
            is DiagnosticEvent.Database -> {
                tokens.add(event.issueType.name.lowercase())
                event.query?.let { tokens.addAll(tokenize(it)) }
            }
            is DiagnosticEvent.Warning -> {
                event.details?.let { tokens.addAll(tokenize(it)) }
            }
            else -> {}
        }
        
        return tokens
    }
    
    fun getRelevantKnowledge(
        query: String,
        recentEvents: List<DiagnosticEvent>
    ): List<ErrorKnowledge> {
        val queryTokens = tokenize(query)
        
        val eventTokens = recentEvents
            .flatMap { extractEventTokens(it) }
            .toSet()
        
        val scoredErrors = knowledgeCache.values
            .map { error ->
                val score = calculateRelevanceScore(error, queryTokens, eventTokens)
                ScoredError(error, score)
            }
            .filter { it.score > 0 }
        
        return scoredErrors
            .sortedWith(compareByDescending<ScoredError> { it.score }.thenBy { it.error.errorName })
            .take(5)
            .map { it.error }
    }
    
    private fun calculateRelevanceScore(
        error: ErrorKnowledge,
        queryTokens: Set<String>,
        eventTokens: Set<String>
    ): Int {
        var score = 0
        
        val errorKeywords = keywordIndex.filter { it.value.contains(error.errorName) }.keys
        
        val queryMatches = queryTokens.intersect(errorKeywords)
        score += queryMatches.size * 10
        
        val eventMatches = eventTokens.intersect(errorKeywords)
        score += eventMatches.size * 8
        
        val categoryTokens = tokenize(error.category)
        if (categoryTokens.any { it in queryTokens || it in eventTokens }) {
            score += 5
        }
        
        return score
    }
    
    private data class ScoredError(val error: ErrorKnowledge, val score: Int)
    
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
