package com.lamontlabs.quantravision.templates

import android.content.Context
import com.lamontlabs.quantravision.Template
import com.lamontlabs.quantravision.TemplateLibrary

/**
 * TemplateSelector
 * Filters TemplateLibrary output to only include enabled templates from PatternCatalog.
 * Drop-in call from detection pipeline before matching.
 */
object TemplateSelector {

    fun loadEnabledTemplates(context: Context, templates: List<Template>): List<Template> {
        val enabled = PatternCatalog.enabledIds(context)
        // Keep any template whose name is in enabled set
        val filtered = templates.filter { t ->
            val id = t.name.replace("\\s+".toRegex(), "_").lowercase()
            enabled.contains(id) || enabled.isEmpty() // if catalog not initialized yet, default to all
        }
        return filtered
    }
}
