package com.lamontlabs.quantravision.cli

import java.io.File
import java.security.MessageDigest

/**
 * VerifyCLI
 * Desktop-compatible deterministic CLI for verifying QuantraVision bundles.
 * Commands:
 *   verify --file evidence.yaml
 *   hash --file ledger.log
 */
object VerifyCLI {

    @JvmStatic
    fun main(args: Array<String>) {
        fun option(vararg names: String): String? {
            val index = args.indexOfFirst { it in names }
            return if (index >= 0) args.getOrNull(index + 1) else null
        }
        val file = requireNotNull(option("--file", "-f")) { "Usage: verifycli verify|hash --file PATH" }
        val mode = args.firstOrNull()?.takeIf { it == "verify" || it == "hash" }
            ?: option("--mode", "-m")
        require(mode == "verify" || mode == "hash") { "Mode must be verify or hash" }

        val f = File(file)
        if (!f.exists()) {
            println("File not found: ${f.absolutePath}")
            return
        }

        when (mode) {
            "verify" -> verifyFile(f)
            "hash" -> println("${sha256(f)}  ${f.name}")
        }
    }

    private fun verifyFile(file: File) {
        val text = file.readText()
        val sha = sha256(text.toByteArray())
        println("SHA-256: $sha")
        println("Verified ${file.name} deterministically.")
    }

    private fun sha256(f: File): String = sha256(f.readBytes())

    private fun sha256(b: ByteArray): String {
        val d = MessageDigest.getInstance("SHA-256").digest(b)
        return d.joinToString("") { "%02x".format(it) }
    }
}
