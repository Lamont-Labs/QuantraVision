package com.lamontlabs.quantravision.compliance

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

/**
 * SBOMGenerator — deterministic Software Bill of Materials generator.
 * Produces a CycloneDX-like JSON manifest for legal and investor compliance.
 * Every artifact hash is reproducible and logged under dist/sbom.json.
 */
object SBOMGenerator {

    private val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    data class Component(val name: String, val version: String, val sha256: String, val path: String)

    fun generate(context: Context): File {
        val base = File(context.filesDir, "")
        val outDir = File(context.filesDir, "dist").apply { mkdirs() }
        val outFile = File(outDir, "sbom.json")

        val components = collectComponents(base)
        val sbom = JSONObject()
        sbom.put("bomFormat", "CycloneDX")
        sbom.put("specVersion", "1.5")
        sbom.put("serialNumber", "urn:uuid:${UUID.nameUUIDFromBytes("quantravision".toByteArray())}")
        sbom.put("version", 1)
        sbom.put("metadata", JSONObject().apply {
            put("timestamp", fmt.format(Date()))
            put("tools", JSONArray().put(JSONObject().apply {
                put("vendor", "Lamont Labs")
                put("name", "SBOMGenerator")
                put("version", "1.0")
            }))
            put("component", JSONObject().apply {
                put("type", "application")
                put("name", "QuantraVision Overlay")
                put("version", "v1.0")
            })
        })
        sbom.put("components", JSONArray().apply {
            components.sortedBy { it.name.lowercase(Locale.US) }.forEach { c ->
                put(JSONObject().apply {
                    put("name", c.name)
                    put("version", c.version)
                    put("hashes", JSONArray().put(JSONObject().apply {
                        put("alg", "SHA-256")
                        put("content", c.sha256)
                    }))
                    put("purl", "pkg:internal/${c.path}")
                })
            }
        })

        outFile.writeText(sbom.toString(2))
        return outFile
    }

    private fun collectComponents(root: File): List<Component> {
        val list = mutableListOf<Component>()
        root.walkTopDown()
            .filter { it.isFile && (it.extension in listOf("kt", "yaml", "yml", "xml", "json")) }
            .forEach {
                val hash = sha256(it)
                list.add(Component(it.nameWithoutExtension, "1.0", hash, it.relativeTo(root).path))
            }
        return list
    }

    private fun sha256(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buf = ByteArray(65536)
            while (true) {
                val r = fis.read(buf)
                if (r <= 0) break
                md.update(buf, 0, r)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
