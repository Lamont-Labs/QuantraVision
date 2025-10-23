package com.lamontlabs.quantravision.templates

import android.content.Context
import com.lamontlabs.quantravision.TemplateLibrary

/**
 * TemplateSelector
 * Filters TemplateLibrary output to only include enabled templates from PatternCatalog.
 * Drop-in call from detection pipeline before matching.
 */
object TemplateSelector {

    fun loadEnabledTemplates(context: Context, library: TemplateLibrary.TemplateSet): TemplateLibrary.TemplateSet {
        val enabled = PatternCatalog.enabledIds(context)
        // Keep any template whose file id (filename without extension) is in enabled set
        val filtered = library.templates.filter { t ->
            val id = t.sourceId ?: t.name.replace("\\s+".toRegex(), "_").lowercase()
            enabled.contains(id) || enabled.isEmpty() // if catalog not initialized yet, default to all
        }
        return library.copy(templates = filtered)
    }
}
