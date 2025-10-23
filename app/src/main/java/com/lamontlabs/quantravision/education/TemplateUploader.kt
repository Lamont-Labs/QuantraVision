package com.lamontlabs.quantravision.education

import android.content.Context
import java.io.File
import java.util.Base64

/**
 * TemplateUploader
 * Allows importing signed pattern templates via QR or file.
 * All templates must include matching .sig files.
 */
object TemplateUploader {

    fun import(context: Context, name: String, yaml: String, sig: String) {
        val dir = File(context.filesDir, "pattern_templates").apply { mkdirs() }
        val yamlFile = File(dir, "$name.yaml")
        val sigFile = File(dir, "$name.sig")
        yamlFile.writeText(yaml.trim())
        sigFile.writeText(sig.trim())
    }

    fun export(context: Context, name: String): Pair<String, String>? {
        val dir = File(context.filesDir, "pattern_templates")
        val yamlFile = File(dir, "$name.yaml")
        val sigFile = File(dir, "$name.sig")
        if (!yamlFile.exists() || !sigFile.exists()) return null
        return yamlFile.readText() to sigFile.readText()
    }

    fun encodeForQr(name: String, yaml: String, sig: String): String {
        val payload = "$name\n$yaml\n$sig"
        return Base64.getEncoder().encodeToString(payload.toByteArray())
    }
}
