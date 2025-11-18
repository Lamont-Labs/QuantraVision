package com.lamontlabs.quantravision.ai.quantrabot

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import timber.log.Timber
import java.io.InputStreamReader

/**
 * Loads pattern knowledge from JSON files in assets/pattern_knowledge/
 * 
 * This knowledge base teaches QuantraBot expert-level pattern validation
 * without requiring expensive model fine-tuning.
 */
class PatternKnowledgeLoader(private val context: Context) {
    
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    
    // Cache loaded knowledge to avoid repeated file I/O
    private var knowledgeCache: Map<String, PatternKnowledge>? = null
    private val cacheLock = Any()
    
    /**
     * Load all pattern knowledge from assets.
     * Returns map of pattern_id -> PatternKnowledge.
     * Results are cached for performance.
     */
    fun loadAll(): Map<String, PatternKnowledge> {
        knowledgeCache?.let { return it }
        
        synchronized(cacheLock) {
            knowledgeCache?.let { return it }
            
            val knowledge = mutableMapOf<String, PatternKnowledge>()
            val assetManager = context.assets
            val knowledgePath = "pattern_knowledge"
            
            try {
                // List all JSON files in pattern_knowledge directory
                val files = assetManager.list(knowledgePath)
                    ?.filter { it.endsWith(".json") && it != "schema.json" }
                    ?: emptyList()
                
                Timber.i("📚 Loading pattern knowledge: ${files.size} files found")
                
                files.forEach { filename ->
                    try {
                        val fullPath = "$knowledgePath/$filename"
                        assetManager.open(fullPath).use { inputStream ->
                            val reader = InputStreamReader(inputStream)
                            val entry = gson.fromJson(reader, PatternKnowledge::class.java)
                            knowledge[entry.patternId] = entry
                            Timber.d("✓ Loaded knowledge for: ${entry.patternName}")
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to load pattern knowledge file: $filename")
                    }
                }
                
                Timber.i("✅ Loaded knowledge for ${knowledge.size} patterns")
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to load pattern knowledge")
            }
            
            knowledgeCache = knowledge
            return knowledge
        }
    }
    
    /**
     * Get knowledge for a specific pattern by ID.
     * Returns null if no knowledge available for this pattern.
     */
    fun getKnowledge(patternId: String): PatternKnowledge? {
        return loadAll()[patternId]
    }
    
    /**
     * Get knowledge for a pattern by name (converts to ID).
     * Example: "Head and Shoulders" -> "head_and_shoulders"
     */
    fun getKnowledgeByName(patternName: String): PatternKnowledge? {
        val patternId = patternName.replace("\\s+".toRegex(), "_").lowercase()
        return getKnowledge(patternId)
    }
    
    /**
     * Check if knowledge exists for a pattern.
     */
    fun hasKnowledge(patternId: String): Boolean {
        return loadAll().containsKey(patternId)
    }
    
    /**
     * Get all pattern IDs that have knowledge entries.
     */
    fun getAvailablePatternIds(): Set<String> {
        return loadAll().keys
    }
    
    /**
     * Clear cache (useful for testing or if knowledge files are updated).
     */
    fun clearCache() {
        synchronized(cacheLock) {
            knowledgeCache = null
        }
    }
}
