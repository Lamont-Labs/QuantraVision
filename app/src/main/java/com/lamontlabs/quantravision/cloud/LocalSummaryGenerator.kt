package com.lamontlabs.quantravision.cloud

import com.lamontlabs.quantravision.apex.models.ApexResult
import com.lamontlabs.quantravision.apex.models.ApexStatus
import timber.log.Timber

object LocalSummaryGenerator {

    private const val TAG = "LocalSummaryGenerator"

    fun generate(apexResult: ApexResult): String {
        Timber.d("$TAG: Generating local summary for status=${apexResult.status}, score=${apexResult.quantraScore.normalizedScore}")
        
        val header = buildHeader(apexResult)
        val statusSection = when (apexResult.status) {
            ApexStatus.PASS -> buildPassTemplate(apexResult)
            ApexStatus.WAIT -> buildWaitTemplate(apexResult)
            ApexStatus.FAIL -> buildFailTemplate(apexResult)
            ApexStatus.SUPPRESSED -> buildSuppressedTemplate(apexResult)
            ApexStatus.OMEGA -> buildOmegaTemplate(apexResult)
        }
        
        val summary = buildString {
            append(header)
            append("\n\n")
            append(statusSection)
        }
        
        Timber.v("$TAG: Generated summary (${summary.length} chars)")
        return summary
    }

    private fun buildHeader(result: ApexResult): String {
        val confidencePct = (result.confidenceApex * 100).toInt()
        val entropyPct = (result.entropyScore * 100).toInt()
        val regime = if (result.regimeOk) "OK" else "MISMATCH"
        
        return buildString {
            appendLine("📊 APEX VERDICT: ${result.status.name}")
            appendLine("QuantraScore: ${result.quantraScore.normalizedScore}/100")
            appendLine("Confidence: $confidencePct%")
            appendLine("Entropy: $entropyPct%")
            appendLine("Regime: $regime")
        }
    }

    private fun buildPassTemplate(result: ApexResult): String {
        val topGates = formatTraceTop(result.protocolTrace.map { it.protocolId }, 2)
        val invalidations = formatList(result.invalidationPoints, 2)
        
        return buildString {
            appendLine("✅ Structure confirmed: ${determineStructureType(result)}")
            appendLine("Volume/volatility alignment: confirmed")
            appendLine("Top gates: $topGates")
            appendLine("Overlay: solid teal at approved anchors")
            appendLine("Invalidation: $invalidations")
        }
    }

    private fun buildWaitTemplate(result: ApexResult): String {
        val primaryBlocker = result.protocolTrace.firstOrNull()?.protocolId ?: "Unknown"
        val invalidation = result.invalidationPoints.firstOrNull() ?: "Not specified"
        
        return buildString {
            appendLine("⏳ Early structure detected; not confirmed")
            appendLine("Primary blocker: $primaryBlocker")
            appendLine("Overlay: dashed amber ghost geometry")
            appendLine("Confirm if: structure completes or confidence increases")
            appendLine("Breaks if: $invalidation")
        }
    }

    private fun buildFailTemplate(result: ApexResult): String {
        val blockingGates = formatTraceTop(result.protocolTrace.map { it.protocolId }, 2)
        val reason = determineFailureReason(result)
        
        return buildString {
            appendLine("❌ Candidate rejected due to $reason")
            appendLine("Blocking gates: $blockingGates")
            appendLine("Overlay: none/fade")
        }
    }

    private fun buildSuppressedTemplate(result: ApexResult): String {
        val suppressionCause = result.protocolTrace.firstOrNull()?.protocolId ?: "Memory conflict"
        
        return buildString {
            appendLine("🔇 Detected but suppressed by Apex memory")
            appendLine("Suppression cause: $suppressionCause")
            appendLine("Overlay: faint violet broken geometry")
        }
    }

    private fun buildOmegaTemplate(result: ApexResult): String {
        val omegaReason = result.protocolTrace
            .firstOrNull { it.protocolId.startsWith("Omega") }
            ?.reason ?: "Safety threshold exceeded"
        
        return buildString {
            appendLine("🔒 Apex Omega Safety Lock active")
            appendLine("Reason: $omegaReason")
            appendLine("Overlays disabled; cloud disabled")
            appendLine("Fix via Settings → Health Check")
        }
    }

    private fun formatTraceTop(trace: List<String>, count: Int = 2): String {
        return trace.take(count).joinToString(", ").ifEmpty { "None" }
    }

    private fun formatList(items: List<String>, count: Int): String {
        return items.take(count).joinToString(", ").ifEmpty { "None specified" }
    }

    private fun determineStructureType(result: ApexResult): String {
        val topProtocol = result.protocolTrace.firstOrNull()?.protocolName ?: ""
        return when {
            topProtocol.contains("trend", ignoreCase = true) -> "trend"
            topProtocol.contains("continuation", ignoreCase = true) -> "continuation"
            topProtocol.contains("breakout", ignoreCase = true) -> "breakout"
            else -> "pattern structure"
        }
    }

    private fun determineFailureReason(result: ApexResult): String {
        return when {
            result.entropyScore > 0.60 -> "high entropy"
            !result.regimeOk -> "regime mismatch"
            else -> "protocol conflict"
        }
    }
}
