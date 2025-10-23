package com.lamontlabs.quantravision.tests.golden

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.security.MessageDigest

/**
 * Golden test verifying deterministic output hashes.
 */
class GoldenDeterminismTest {

    @Test
    fun test_deterministicPatternHash() {
        val f1 = File("pattern_outputs/sessionA.txt")
        val f2 = File("pattern_outputs/sessionB.txt")
        if (!f1.exists() || !f2.exists()) return

        val h1 = sha256(f1.readText())
        val h2 = sha256(f2.readText())

        assertEquals("Deterministic outputs mismatch", h1, h2)
    }

    private fun sha256(s: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(s.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
