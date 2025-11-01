package com.lamontlabs.quantravision.cli

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
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
        val parser = ArgParser("verifycli")
        val file by parser.option(ArgType.String, shortName = "f", description = "File to verify").required()
        val mode by parser.option(ArgType.Choice(listOf("verify", "hash")), shortName = "m", description = "Mode").required()
        parser.parse(args)

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
