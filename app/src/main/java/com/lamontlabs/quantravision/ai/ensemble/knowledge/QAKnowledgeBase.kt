package com.lamontlabs.quantravision.ai.ensemble.knowledge

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lamontlabs.quantravision.ai.ensemble.models.QAEntry
import java.io.InputStreamReader

class QAKnowledgeBase(private val context: Context) {
    
    private val gson = Gson()
    private var cachedEntries: List<QAEntry>? = null
    
    fun loadAll(): List<QAEntry> {
        if (cachedEntries != null) {
            return cachedEntries!!
        }
        
        return try {
            val inputStream = context.assets.open("knowledge/qa_knowledge_base.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<QAEntry>>() {}.type
            val entries = gson.fromJson<List<QAEntry>>(reader, type)
            reader.close()
            cachedEntries = entries
            entries
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun search(query: String): List<QAEntry> {
        val entries = loadAll()
        val queryLower = query.lowercase().trim()
        
        if (queryLower.isEmpty()) {
            return emptyList()
        }
        
        return entries.filter { entry ->
            entry.question.lowercase().contains(queryLower) ||
            entry.answer.lowercase().contains(queryLower) ||
            entry.keywords.any { keyword -> 
                keyword.lowercase().contains(queryLower) || 
                queryLower.contains(keyword.lowercase())
            }
        }.sortedByDescending { entry ->
            var score = 0
            if (entry.question.lowercase().contains(queryLower)) score += 3
            if (entry.keywords.any { it.lowercase() == queryLower }) score += 2
            if (entry.answer.lowercase().contains(queryLower)) score += 1
            score
        }
    }
    
    fun getByCategory(category: String): List<QAEntry> {
        val entries = loadAll()
        return entries.filter { it.category.equals(category, ignoreCase = true) }
    }
    
    fun clearCache() {
        cachedEntries = null
    }
}
