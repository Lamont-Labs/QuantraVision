package com.lamontlabs.quantravision.devbot.ai

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lamontlabs.quantravision.devbot.data.DiagnosticEvent
import java.io.InputStreamReader

data class LoaderStats(
    val totalPatterns: Int = 0,
    val failedPatterns: Int = 0,
    val failedCategories: Int = 0,
    val loadedKeywords: Int = 0,
    val errors: List<LoadError> = emptyList()
)

data class LoadError(
    val category: String?,
    val file: String?,
    val errorMessage: String,
    val exception: Exception
)

class DiagnosticKnowledgeLoader(private val context: Context) {
    private val gson = Gson()
    private val knowledgeCache = mutableMapOf<String, ErrorKnowledge>()
    private val categoryIndex = mutableMapOf<String, List<String>>()
    private val keywordIndex = mutableMapOf<String, MutableSet<String>>()
    
    private val loadErrors = mutableListOf<LoadError>()
    private var totalFilesAttempted = 0
    private var totalFilesSucceeded = 0
    
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
    
    suspend fun loadKnowledge(): LoaderStats {
        loadErrors.clear()
        totalFilesAttempted = 0
        totalFilesSucceeded = 0
        
        Log.i("DiagnosticKnowledgeLoader", "Starting diagnostic knowledge loading for ${categories.size} categories")
        
        categories.forEach { category ->
            loadCategoryKnowledge(category)
        }
        
        buildKeywordIndex()
        
        val stats = LoaderStats(
            totalPatterns = knowledgeCache.size,
            failedPatterns = totalFilesAttempted - totalFilesSucceeded,
            failedCategories = loadErrors.map { it.category }.distinct().count(),
            loadedKeywords = keywordIndex.size,
            errors = loadErrors.toList()
        )
        
        Log.i("DiagnosticKnowledgeLoader", 
            "Knowledge loading complete: ${stats.totalPatterns} patterns loaded, " +
            "${stats.failedPatterns} failures, ${stats.loadedKeywords} keywords indexed"
        )
        
        if (stats.failedPatterns > 0) {
            Log.w("DiagnosticKnowledgeLoader", 
                "WARNING: ${stats.failedPatterns} pattern(s) failed to load across ${stats.failedCategories} category(ies)"
            )
            loadErrors.forEach { error ->
                Log.e("DiagnosticKnowledgeLoader", 
                    "Failed to load ${error.category}/${error.file}: ${error.errorMessage}",
                    error.exception
                )
            }
        }
        
        if (stats.totalPatterns == 0) {
            val criticalError = "CRITICAL: No diagnostic patterns loaded! DevBot will not function properly."
            Log.e("DiagnosticKnowledgeLoader", criticalError)
            throw IllegalStateException(criticalError)
        }
        
        return stats
    }
    
    fun getLoadStats(): LoaderStats {
        return LoaderStats(
            totalPatterns = knowledgeCache.size,
            failedPatterns = totalFilesAttempted - totalFilesSucceeded,
            failedCategories = loadErrors.map { it.category }.distinct().count(),
            loadedKeywords = keywordIndex.size,
            errors = loadErrors.toList()
        )
    }
    
    private fun loadCategoryKnowledge(category: String) {
        try {
            val assetPath = "diagnostic_knowledge/$category"
            val files = context.assets.list(assetPath) ?: emptyArray()
            
            if (files.isEmpty()) {
                Log.w("DiagnosticKnowledgeLoader", "No files found in category: $category")
            }
            
            val categoryErrors = mutableListOf<String>()
            val jsonFiles = files.filter { it.endsWith(".json") }
            
            Log.d("DiagnosticKnowledgeLoader", "Loading $category: ${jsonFiles.size} patterns")
            
            jsonFiles.forEach { file ->
                totalFilesAttempted++
                try {
                    val inputStream = context.assets.open("$assetPath/$file")
                    val reader = InputStreamReader(inputStream)
                    
                    val error = gson.fromJson(reader, ErrorKnowledge::class.java)
                    
                    if (error.errorName.isBlank()) {
                        throw IllegalArgumentException("Pattern has blank errorName")
                    }
                    
                    knowledgeCache[error.errorName] = error
                    categoryErrors.add(error.errorName)
                    totalFilesSucceeded++
                    
                    reader.close()
                    inputStream.close()
                    
                    Log.v("DiagnosticKnowledgeLoader", "Loaded: $category/${error.errorName}")
                    
                } catch (e: Exception) {
                    val errorMessage = e.message ?: "Unknown error"
                    loadErrors.add(
                        LoadError(
                            category = category,
                            file = file,
                            errorMessage = errorMessage,
                            exception = e
                        )
                    )
                    Log.e("DiagnosticKnowledgeLoader", 
                        "Failed to load pattern $category/$file: $errorMessage", e
                    )
                }
            }
            
            categoryIndex[category] = categoryErrors
            
            if (categoryErrors.isEmpty() && jsonFiles.isNotEmpty()) {
                Log.w("DiagnosticKnowledgeLoader", 
                    "WARNING: Category '$category' has ${jsonFiles.size} files but all failed to load!"
                )
            }
            
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Unknown error"
            loadErrors.add(
                LoadError(
                    category = category,
                    file = null,
                    errorMessage = "Category loading failed: $errorMessage",
                    exception = e
                )
            )
            Log.e("DiagnosticKnowledgeLoader", 
                "CRITICAL: Failed to load category $category: $errorMessage", e
            )
            categoryIndex[category] = emptyList()
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
