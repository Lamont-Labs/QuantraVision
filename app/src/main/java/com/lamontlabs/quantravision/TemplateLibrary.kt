package com.lamontlabs.quantravision

import android.content.Context
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream

data class Template(val name: String, val path: String, val threshold: Double, val image: Mat)

class TemplateLibrary(private val context: Context) {

    fun loadTemplates(): List<Template> {
        val yaml = Yaml()
        val dir = File(context.filesDir, "pattern_templates")
        val templates = mutableListOf<Template>()
        dir.listFiles()?.filter { it.extension == "yaml" }?.forEach { file ->
            val data = yaml.load<Map<String, Any>>(FileInputStream(file))
            val name = data["name"] as String
            val path = data["image"] as String
            val threshold = (data["threshold"] as Double)
            val imageFile = File(context.filesDir, path)
            val imageMat = Imgcodecs.imread(imageFile.absolutePath)
            templates.add(Template(name, path, threshold, imageMat))
        }
        return templates
    }
}
