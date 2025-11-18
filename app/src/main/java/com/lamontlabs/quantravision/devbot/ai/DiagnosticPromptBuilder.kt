package com.lamontlabs.quantravision.devbot.ai

import com.lamontlabs.quantravision.devbot.data.DiagnosticEvent

class DiagnosticPromptBuilder {
    
    fun buildDiagnosticPrompt(
        userQuery: String,
        recentEvents: List<DiagnosticEvent>,
        errorKnowledge: List<ErrorKnowledge>
    ): String {
        val systemPrompt = buildSystemPrompt()
        val contextSection = buildContextSection(recentEvents)
        val knowledgeSection = buildKnowledgeSection(errorKnowledge)
        
        return """
            |$systemPrompt
            |
            |$knowledgeSection
            |
            |$contextSection
            |
            |User Question: $userQuery
            |
            |Provide a clear, helpful response explaining the issue and suggesting concrete fixes.
        """.trimMargin()
    }
    
    private fun buildSystemPrompt(): String {
        return """
            |You are DevBot, an expert Android diagnostic assistant. You help developers understand and fix errors in their Android apps.
            |
            |Your expertise includes:
            |• Crash analysis (NullPointerException, IndexOutOfBounds, etc.)
            |• Memory management (leaks, OutOfMemory, GC issues)
            |• Performance optimization (ANR, UI thread blocking, frame drops)
            |• Database issues (slow queries, locks, migrations)
            |• Network errors (timeouts, SSL, HTTP errors)
            |• Jetpack Compose and modern Android development
            |• OpenCV and TensorFlow Lite specific issues
            |
            |INSTRUCTIONS:
            |1. Analyze the error context and recent events
            |2. Use the error knowledge base to understand the issue
            |3. Explain in plain English what went wrong
            |4. Provide specific, actionable fixes
            |5. Be concise but thorough
            |6. Use bullet points for clarity
        """.trimMargin()
    }
    
    private fun buildContextSection(events: List<DiagnosticEvent>): String {
        if (events.isEmpty()) {
            return "Recent Events: No recent errors detected."
        }
        
        val eventSummaries = events.take(10).joinToString("\n") { event ->
            when (event) {
                is DiagnosticEvent.Crash -> 
                    "• CRASH: ${event.message} (thread: ${event.threadName})"
                is DiagnosticEvent.Error -> 
                    "• ERROR [${event.severity}]: ${event.message}"
                is DiagnosticEvent.Performance -> 
                    "• PERFORMANCE: ${event.message} (${event.metricType})"
                is DiagnosticEvent.Network -> 
                    "• NETWORK: ${event.message} (${event.errorType})"
                is DiagnosticEvent.Database -> 
                    "• DATABASE: ${event.message} (${event.issueType})"
                is DiagnosticEvent.Warning -> 
                    "• WARNING: ${event.message}"
                is DiagnosticEvent.Info -> 
                    "• INFO: ${event.message}"
            }
        }
        
        return """
            |Recent Events:
            |$eventSummaries
        """.trimMargin()
    }
    
    private fun buildKnowledgeSection(knowledge: List<ErrorKnowledge>): String {
        if (knowledge.isEmpty()) {
            return ""
        }
        
        val knowledgeText = knowledge.take(3).joinToString("\n\n") { error ->
            """
                |Error: ${error.errorName}
                |Category: ${error.category}
                |
                |Description: ${error.description}
                |
                |Common Causes:
                |${error.commonCauses.joinToString("\n") { "• $it" }}
                |
                |Solutions:
                |${error.solutions.joinToString("\n") { "• $it" }}
                |
                |Prevention:
                |${error.prevention.joinToString("\n") { "• $it" }}
            """.trimMargin()
        }
        
        return """
            |Error Knowledge Base:
            |$knowledgeText
        """.trimMargin()
    }
    
    fun buildCrashAnalysisPrompt(crashEvent: DiagnosticEvent.Crash): String {
        return """
            |Analyze this crash:
            |
            |Message: ${crashEvent.message}
            |Thread: ${crashEvent.threadName}
            |Exception: ${crashEvent.throwable.javaClass.simpleName}
            |
            |Stack trace:
            |${crashEvent.throwable.stackTraceToString()}
            |
            |Provide:
            |1. What caused this crash
            |2. Where in the code it occurred
            |3. How to fix it
            |4. How to prevent it in the future
        """.trimMargin()
    }
    
    fun buildPerformanceAnalysisPrompt(issues: List<DiagnosticEvent.Performance>): String {
        val issuesSummary = issues.joinToString("\n") {
            "• ${it.message} (value: ${it.value}, threshold: ${it.threshold})"
        }
        
        return """
            |Analyze these performance issues:
            |
            |$issuesSummary
            |
            |Provide optimization recommendations with specific code changes.
        """.trimMargin()
    }
}

data class ErrorKnowledge(
    val errorName: String,
    val category: String,
    val description: String,
    val commonCauses: List<String>,
    val solutions: List<String>,
    val prevention: List<String>,
    val examples: List<String> = emptyList(),
    val relatedErrors: List<String> = emptyList()
)
