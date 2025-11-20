package com.lamontlabs.quantravision.devbot.ai

import android.content.Context
import com.google.gson.Gson
import com.lamontlabs.quantravision.ai.ensemble.knowledge.KnowledgeBase
import com.lamontlabs.quantravision.ai.ensemble.models.QAEntry
import timber.log.Timber
import java.io.InputStreamReader

class DiagnosticKnowledgeBase(private val context: Context) : KnowledgeBase {
    
    private val gson = Gson()
    private var cachedEntries: List<QAEntry>? = null
    
    private val categories = listOf(
        "crashes",
        "memory",
        "ui_threading",
        "network",
        "database",
        "quantravision",
        "framework",
        "coroutines",
        "build",
        "compose"
    )
    
    override fun loadAll(): List<QAEntry> {
        if (cachedEntries != null) {
            Timber.d("📚 Returning ${cachedEntries!!.size} cached diagnostic Q&A entries")
            return cachedEntries!!
        }
        
        return try {
            Timber.i("📚 Loading diagnostic knowledge base from assets/diagnostic_knowledge/...")
            val allEntries = mutableListOf<QAEntry>()
            
            categories.forEach { category ->
                val categoryEntries = loadCategoryEntries(category)
                allEntries.addAll(categoryEntries)
                Timber.d("📚 Loaded ${categoryEntries.size} entries from $category")
            }
            
            if (allEntries.isEmpty()) {
                Timber.e("📚 Diagnostic knowledge base loaded but is empty!")
                return emptyList()
            }
            
            cachedEntries = allEntries
            Timber.i("📚 Successfully loaded ${allEntries.size} diagnostic Q&A entries from knowledge base")
            allEntries
        } catch (e: Exception) {
            Timber.e(e, "📚 CRITICAL ERROR: Failed to load diagnostic knowledge base")
            emptyList()
        }
    }
    
    private fun loadCategoryEntries(category: String): List<QAEntry> {
        val entries = mutableListOf<QAEntry>()
        
        try {
            val assetPath = "diagnostic_knowledge/$category"
            val files = context.assets.list(assetPath) ?: emptyArray()
            val jsonFiles = files.filter { it.endsWith(".json") }
            
            jsonFiles.forEach { file ->
                try {
                    val inputStream = context.assets.open("$assetPath/$file")
                    val reader = InputStreamReader(inputStream)
                    val errorKnowledge = gson.fromJson(reader, ErrorKnowledge::class.java)
                    reader.close()
                    
                    val qaEntry = convertToQAEntry(errorKnowledge, category)
                    entries.add(qaEntry)
                    
                } catch (e: Exception) {
                    Timber.w(e, "📚 Failed to load diagnostic file: $category/$file")
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "📚 Failed to load category: $category")
        }
        
        return entries
    }
    
    private fun convertToQAEntry(error: ErrorKnowledge, category: String): QAEntry {
        val question = "What is ${error.errorName}?"
        
        val answer = buildString {
            appendLine(error.description)
            appendLine()
            
            if (error.commonCauses.isNotEmpty()) {
                appendLine("Common Causes:")
                error.commonCauses.forEach { cause ->
                    appendLine("• $cause")
                }
                appendLine()
            }
            
            if (error.solutions.isNotEmpty()) {
                appendLine("Solutions:")
                error.solutions.forEach { solution ->
                    appendLine("• $solution")
                }
                appendLine()
            }
            
            if (error.prevention.isNotEmpty()) {
                appendLine("Prevention:")
                error.prevention.forEach { prevention ->
                    appendLine("• $prevention")
                }
            }
            
            if (error.examples.isNotEmpty()) {
                appendLine()
                appendLine("Examples:")
                error.examples.forEach { example ->
                    appendLine("• $example")
                }
            }
        }.trim()
        
        val keywords = extractKeywords(error)
        
        return QAEntry(
            question = question,
            answer = answer,
            category = category,
            keywords = keywords
        )
    }
    
    private fun extractKeywords(error: ErrorKnowledge): List<String> {
        val keywords = mutableSetOf<String>()
        
        keywords.add(error.errorName.lowercase())
        
        val nameTokens = tokenize(error.errorName)
        keywords.addAll(nameTokens)
        
        val descTokens = tokenize(error.description)
        keywords.addAll(descTokens.take(10))
        
        keywords.add(error.category.lowercase())
        
        error.commonCauses.forEach { cause ->
            val tokens = tokenize(cause)
            keywords.addAll(tokens.take(3))
        }
        
        error.solutions.forEach { solution ->
            val tokens = tokenize(solution)
            keywords.addAll(tokens.take(3))
        }
        
        error.relatedErrors.forEach { related ->
            keywords.add(related.lowercase())
        }
        
        return keywords.filter { it.length >= 3 }.distinct()
    }
    
    private fun tokenize(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^a-z0-9]+"), " ")
            .split(" ")
            .filter { it.length >= 3 }
    }
    
    override fun search(query: String): List<QAEntry> {
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
    
    override fun getByCategory(category: String): List<QAEntry> {
        val entries = loadAll()
        return entries.filter { it.category.equals(category, ignoreCase = true) }
    }
    
    override fun clearCache() {
        cachedEntries = null
    }
}
