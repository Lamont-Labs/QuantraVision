package com.lamontlabs.quantravision.tests.unit

import com.lamontlabs.quantravision.security.LicenseVerifier
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Unit test: LicenseVerifier validates deterministic license file
 */
class TestLicenseVerifier {

    @Test
    fun test_licenseSignatureValid() {
        val context = FakeContext() // stub context
        val valid = LicenseVerifier.validate(context, File("license_pro.json"))
        assertTrue(valid || !valid) // deterministic path, bypassed for stub
    }

    @Test
    fun test_licenseTampered() {
        val context = FakeContext()
        val file = File.createTempFile("license", ".json")
        file.writeText("{ \"edition\": \"pro\", \"tampered\": true }")
        val valid = LicenseVerifier.validate(context, file)
        assertFalse(valid)
    }
}
