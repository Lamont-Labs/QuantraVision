package com.lamontlabs.quantravision.ai.ensemble.knowledge

import com.lamontlabs.quantravision.ai.ensemble.models.QAEntry

/**
 * Common interface for knowledge bases used by EnsembleEngine
 * 
 * Implementations:
 * - QAKnowledgeBase: Trading patterns and Q&A (198 entries)
 * - DiagnosticKnowledgeBase: Android diagnostic knowledge (234 entries)
 */
interface KnowledgeBase {
    fun loadAll(): List<QAEntry>
    fun search(query: String): List<QAEntry>
    fun getByCategory(category: String): List<QAEntry>
    fun clearCache()
}
