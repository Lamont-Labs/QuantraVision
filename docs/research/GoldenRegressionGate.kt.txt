package com.lamontlabs.quantravision.golden

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.max

/**
 * Golden-set defender:
 * Fails the build if F1 drops by >2% versus baseline recorded in golden/metrics_baseline.txt
 *
 * File formats:
 *  - app/src/test/resources/golden/metrics_baseline.txt:
 *        precision=0.91
 *        recall=0.89
 *        f1=0.90
 *  - app/build/golden/metrics_current.txt (produced by your detector eval task):
 *        precision=0.92
 *        recall=0.88
 *        f1=0.90
 */
class GoldenRegressionGate {

    private fun readMetrics(file: File): Map<String, Float> =
        file.readLines()
            .mapNotNull {
                val p = it.split("=")
                if (p.size == 2) p[0].trim() to p[1].trim().toFloat() else null
            }.toMap()

    @Test
    fun f1_must_not_regress_more_than_2pct() {
        val root = File(".")
        val baseline = File(root, "app/src/test/resources/golden/metrics_baseline.txt")
        val current  = File(root, "app/build/golden/metrics_current.txt")

        require(baseline.exists()) { "Missing baseline metrics at ${baseline.path}" }
        require(current.exists())  { "Missing current metrics at ${current.path}. Generate via eval task." }

        val b = readMetrics(baseline)
        val c = readMetrics(current)

        val f1Base = b["f1"] ?: error("Baseline missing f1")
        val f1Now  = c["f1"] ?: error("Current missing f1")

        val drop = (f1Base - f1Now)
        val maxAllowedDrop = 0.02f // 2%
        val ok = drop <= maxAllowedDrop

        val msg = "Golden regression: f1_base=%.3f f1_now=%.3f drop=%.3f allowed=%.3f".format(f1Base, f1Now, drop, maxAllowedDrop)
        assertTrue(msg, ok)
    }
}
