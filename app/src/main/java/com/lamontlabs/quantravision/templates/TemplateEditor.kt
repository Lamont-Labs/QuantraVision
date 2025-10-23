package com.lamontlabs.quantravision.templates

import android.content.Context
import java.io.File
import java.util.Locale

/**
 * TemplateEditor
 * Programmatic adjustments to per-pattern YAML thresholds and scale ranges.
 * Edits in-place deterministically with minimal diff.
 */
object TemplateEditor {

    data class Edit(
        val threshold: Double? = null,
        val scaleMin: Double? = null,
        val scaleMax: Double? = null,
        val scaleStride: Double? = null
    )

    fun apply(context: Context, templateId: String, edit: Edit): Boolean {
        val f = File(context.filesDir, "pattern_templates/$templateId.yaml")
        if (!f.exists()) return false
        var text = f.readText()

        fun repl(key: String, value: String) {
            val rx = Regex("""(^\s*$key\s*:\s*)([^\n#]+)""", RegexOption.MULTILINE)
            text = if (rx.containsMatchIn(text)) {
                text.replace(rx) { m -> m.groupValues[1] + value }
            } else {
                // append under end; keep deterministic order
                text += "\n$key: $value"
                text
            }
        }

        edit.threshold?.let { repl("threshold", String.format(Locale.US, "%.3f", it)) }
        if (edit.scaleMin != null || edit.scaleMax != null || edit.scaleStride != null) {
            // Ensure scale block exists
            if (!Regex("""^\s*scale\s*:\s*$""", RegexOption.MULTILINE).containsMatchIn(text)) {
                text += "\nscale:\n  min: 0.6\n  max: 1.8\n  stride: 0.15"
            }
            if (edit.scaleMin != null) {
                val rx = Regex("""(^\s*min\s*:\s*)([^\n#]+)""", RegexOption.MULTILINE)
                text = text.replace(rx) { m ->
                    if (m.range.first < text.indexOf("scale:")) m.value else m.groupValues[1] + String.format(Locale.US, "%.2f", edit.scaleMin)
                }
            }
            if (edit.scaleMax != null) {
                val rx = Regex("""(^\s*max\s*:\s*)([^\n#]+)""", RegexOption.MULTILINE)
                text = text.replace(rx) { m ->
                    if (m.range.first < text.indexOf("scale:")) m.value else m.groupValues[1] + String.format(Locale.US, "%.2f", edit.scaleMax)
                }
            }
            if (edit.scaleStride != null) {
                val rx = Regex("""(^\s*stride\s*:\s*)([^\n#]+)""", RegexOption.MULTILINE)
                text = text.replace(rx) { m ->
                    if (m.range.first < text.indexOf("scale:")) m.value else m.groupValues[1] + String.format(Locale.US, "%.2f", edit.scaleStride)
                }
            }
        }

        f.writeText(text.trim() + "\n")
        return true
    }
}
