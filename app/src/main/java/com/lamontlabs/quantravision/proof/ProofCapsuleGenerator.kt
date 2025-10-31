package com.lamontlabs.quantravision.proof

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.licensing.AdvancedFeatureGate
import com.lamontlabs.quantravision.regime.RegimeNavigator
import com.lamontlabs.quantravision.storage.AtomicFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

/**
 * ProofCapsuleGenerator
 * 
 * Packages pattern detections into tamper-proof, shareable capsules for
 * educational record-keeping.
 * 
 * ⚠️ LEGAL NOTICE ⚠️
 * Proof Capsules are EDUCATIONAL DETECTION LOGS ONLY.
 * They are NOT:
 * - Proof of trading results or profitability
 * - Verified trading records
 * - Investment recommendations
 * - Endorsements of any kind
 * 
 * Sharing Proof Capsules does NOT constitute investment advice.
 * See legal/ADVANCED_FEATURES_DISCLAIMER.md for full legal terms.
 * 
 * Features:
 * - Tamper-evident packaging (SHA-256 hash)
 * - Timestamped detection records (ISO 8601)
 * - JSON export with all metadata
 * - QR code generation (hash + basic metadata)
 * - Android share intent integration
 * - Hash verification
 * 
 * Storage: app/filesDir/proof_capsules/
 */
class ProofCapsuleGenerator(private val context: Context) {

    private val capsulesDir = File(context.filesDir, "proof_capsules").apply {
        if (!exists()) mkdirs()
    }
    
    companion object {
        private const val CAPSULE_VERSION = "1.0"
        private val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    
    /**
     * Proof Capsule
     * 
     * Immutable, tamper-evident package of pattern detection data
     */
    data class ProofCapsule(
        val version: String,
        val timestamp: String,
        val timestampMs: Long,
        val patternId: String,
        val confidence: Double,
        val timeframe: String,
        val scale: Double,
        val consensusScore: Double,
        val regimeContext: Map<String, Any>?,
        val detectionMetadata: Map<String, Any>,
        val sha256Hash: String,
        val disclaimer: String = "⚠️ Educational detection log only - NOT proof of trading results"
    )
    
    /**
     * Generate proof capsule from pattern match
     * 
     * @param patternMatch Pattern detection to package
     * @param screenshot Optional screenshot of detection
     * @param regimeContext Optional market regime context
     * @return Generated proof capsule
     */
    suspend fun generateCapsule(
        patternMatch: PatternMatch,
        screenshot: Bitmap? = null,
        regimeContext: RegimeNavigator.MarketRegime? = null
    ): ProofCapsule = withContext(Dispatchers.IO) {
        
        AdvancedFeatureGate.requireAcceptance(context, "Proof Capsules")
        
        try {
            val timestampMs = patternMatch.timestamp
            val timestamp = iso8601Format.format(Date(timestampMs))
            
            val metadata = buildMetadata(patternMatch, screenshot)
            val regimeData = regimeContext?.let { buildRegimeData(it) }
            
            val dataForHash = buildDataForHash(
                timestamp = timestamp,
                patternId = patternMatch.patternName,
                confidence = patternMatch.confidence,
                timeframe = patternMatch.timeframe,
                scale = patternMatch.scale,
                consensusScore = patternMatch.consensusScore,
                metadata = metadata,
                regimeData = regimeData
            )
            
            val hash = calculateSHA256(dataForHash)
            
            val capsule = ProofCapsule(
                version = CAPSULE_VERSION,
                timestamp = timestamp,
                timestampMs = timestampMs,
                patternId = patternMatch.patternName,
                confidence = patternMatch.confidence,
                timeframe = patternMatch.timeframe,
                scale = patternMatch.scale,
                consensusScore = patternMatch.consensusScore,
                regimeContext = regimeData,
                detectionMetadata = metadata,
                sha256Hash = hash
            )
            
            saveCapsule(capsule, screenshot)
            
            Timber.i("Proof capsule generated: ${patternMatch.patternName} (hash: ${hash.take(8)}...)")
            
            capsule
            
        } catch (e: Exception) {
            Timber.e(e, "Error generating proof capsule")
            throw e
        }
    }
    
    /**
     * Export capsule to JSON file
     * 
     * @param capsule Proof capsule to export
     * @return File path of exported JSON
     */
    suspend fun exportToJson(capsule: ProofCapsule): File = withContext(Dispatchers.IO) {
        try {
            val filename = "capsule_${capsule.patternId}_${capsule.timestampMs}.json"
            val file = File(capsulesDir, filename)
            
            val json = serializeCapsule(capsule)
            AtomicFile.write(file, json)
            
            Timber.i("Capsule exported to: ${file.absolutePath}")
            file
            
        } catch (e: Exception) {
            Timber.e(e, "Error exporting capsule to JSON")
            throw e
        }
    }
    
    /**
     * Generate QR code containing capsule hash and basic metadata
     * 
     * @param capsule Proof capsule
     * @param size QR code size in pixels (default 512)
     * @return QR code bitmap
     */
    suspend fun generateQRCode(capsule: ProofCapsule, size: Int = 512): Bitmap = withContext(Dispatchers.Default) {
        try {
            val qrData = buildString {
                append("QuantraVision Proof Capsule\n")
                append("Pattern: ${capsule.patternId}\n")
                append("Time: ${capsule.timestamp}\n")
                append("Confidence: ${"%.2f".format(capsule.confidence * 100)}%\n")
                append("Hash: ${capsule.sha256Hash.take(16)}...\n")
                append("⚠️ Educational log only - NOT trading results")
            }
            
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(qrData, BarcodeFormat.QR_CODE, size, size)
            
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            
            Timber.i("QR code generated for capsule: ${capsule.patternId}")
            bitmap
            
        } catch (e: Exception) {
            Timber.e(e, "Error generating QR code")
            throw e
        }
    }
    
    /**
     * Share capsule via Android share intent
     * 
     * @param capsule Proof capsule to share
     * @param includeQR Include QR code in share (default true)
     */
    suspend fun shareCapsule(capsule: ProofCapsule, includeQR: Boolean = true) = withContext(Dispatchers.IO) {
        try {
            val jsonFile = exportToJson(capsule)
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    jsonFile
                )
                
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "QuantraVision Detection Log - ${capsule.patternId}")
                putExtra(Intent.EXTRA_TEXT, buildShareText(capsule))
                
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(Intent.createChooser(shareIntent, "Share Proof Capsule").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            
            Timber.i("Capsule shared: ${capsule.patternId}")
            
        } catch (e: Exception) {
            Timber.e(e, "Error sharing capsule")
            throw e
        }
    }
    
    /**
     * Verify capsule integrity
     * 
     * @param capsule Proof capsule to verify
     * @return true if hash matches, false if tampered
     */
    suspend fun verifyCapsule(capsule: ProofCapsule): Boolean = withContext(Dispatchers.Default) {
        try {
            val dataForHash = buildDataForHash(
                timestamp = capsule.timestamp,
                patternId = capsule.patternId,
                confidence = capsule.confidence,
                timeframe = capsule.timeframe,
                scale = capsule.scale,
                consensusScore = capsule.consensusScore,
                metadata = capsule.detectionMetadata,
                regimeData = capsule.regimeContext
            )
            
            val expectedHash = calculateSHA256(dataForHash)
            val isValid = expectedHash == capsule.sha256Hash
            
            if (isValid) {
                Timber.i("Capsule verified: ${capsule.patternId}")
            } else {
                Timber.w("Capsule TAMPERED: ${capsule.patternId} (hash mismatch)")
            }
            
            isValid
            
        } catch (e: Exception) {
            Timber.e(e, "Error verifying capsule")
            false
        }
    }
    
    /**
     * Load capsule from JSON file
     * 
     * @param file JSON file to load
     * @return Parsed proof capsule
     */
    suspend fun loadCapsule(file: File): ProofCapsule = withContext(Dispatchers.IO) {
        try {
            val json = file.readText()
            deserializeCapsule(json)
        } catch (e: Exception) {
            Timber.e(e, "Error loading capsule from file")
            throw e
        }
    }
    
    /**
     * List all saved capsules
     * 
     * @return List of capsule files
     */
    suspend fun listCapsules(): List<File> = withContext(Dispatchers.IO) {
        try {
            capsulesDir.listFiles { file ->
                file.extension == "json" && file.name.startsWith("capsule_")
            }?.sortedByDescending { it.lastModified() } ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Error listing capsules")
            emptyList()
        }
    }
    
    private fun buildMetadata(patternMatch: PatternMatch, screenshot: Bitmap?): Map<String, Any> {
        return buildMap {
            put("id", patternMatch.id)
            put("originPath", patternMatch.originPath)
            put("detectionBounds", patternMatch.detectionBounds ?: "null")
            put("windowMs", patternMatch.windowMs)
            put("hasScreenshot", screenshot != null)
            if (screenshot != null) {
                put("screenshotWidth", screenshot.width)
                put("screenshotHeight", screenshot.height)
            }
        }
    }
    
    private fun buildRegimeData(regime: RegimeNavigator.MarketRegime): Map<String, Any> {
        return mapOf(
            "volatility" to regime.volatility.name,
            "trendStrength" to regime.trendStrength.name,
            "liquidity" to regime.liquidity.name,
            "overallQuality" to regime.overallQuality.name,
            "educationalContext" to regime.educationalContext
        )
    }
    
    private fun buildDataForHash(
        timestamp: String,
        patternId: String,
        confidence: Double,
        timeframe: String,
        scale: Double,
        consensusScore: Double,
        metadata: Map<String, Any>,
        regimeData: Map<String, Any>?
    ): String {
        return buildString {
            append("version=$CAPSULE_VERSION")
            append("|timestamp=$timestamp")
            append("|pattern=$patternId")
            append("|confidence=$confidence")
            append("|timeframe=$timeframe")
            append("|scale=$scale")
            append("|consensus=$consensusScore")
            append("|metadata=${metadata.entries.sortedBy { it.key }.joinToString(",") { "${it.key}=${it.value}" }}")
            regimeData?.let {
                append("|regime=${it.entries.sortedBy { it.key }.joinToString(",") { "${it.key}=${it.value}" }}")
            }
        }
    }
    
    private fun calculateSHA256(data: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(data.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    private fun serializeCapsule(capsule: ProofCapsule): String {
        val json = JSONObject().apply {
            put("version", capsule.version)
            put("timestamp", capsule.timestamp)
            put("timestampMs", capsule.timestampMs)
            put("patternId", capsule.patternId)
            put("confidence", capsule.confidence)
            put("timeframe", capsule.timeframe)
            put("scale", capsule.scale)
            put("consensusScore", capsule.consensusScore)
            
            capsule.regimeContext?.let { regime ->
                put("regimeContext", JSONObject(regime))
            }
            
            put("detectionMetadata", JSONObject(capsule.detectionMetadata))
            put("sha256Hash", capsule.sha256Hash)
            put("disclaimer", capsule.disclaimer)
        }
        
        return json.toString(2)
    }
    
    private fun deserializeCapsule(json: String): ProofCapsule {
        val obj = JSONObject(json)
        
        val regimeContext = obj.optJSONObject("regimeContext")?.let { regime ->
            regime.keys().asSequence().associateWith { key ->
                regime.get(key)
            }
        }
        
        val metadata = obj.getJSONObject("detectionMetadata")
        val metadataMap = metadata.keys().asSequence().associateWith { key ->
            metadata.get(key)
        }
        
        return ProofCapsule(
            version = obj.getString("version"),
            timestamp = obj.getString("timestamp"),
            timestampMs = obj.getLong("timestampMs"),
            patternId = obj.getString("patternId"),
            confidence = obj.getDouble("confidence"),
            timeframe = obj.getString("timeframe"),
            scale = obj.getDouble("scale"),
            consensusScore = obj.getDouble("consensusScore"),
            regimeContext = regimeContext,
            detectionMetadata = metadataMap,
            sha256Hash = obj.getString("sha256Hash"),
            disclaimer = obj.optString("disclaimer", "⚠️ Educational detection log only - NOT proof of trading results")
        )
    }
    
    private fun saveCapsule(capsule: ProofCapsule, screenshot: Bitmap?) {
        val filename = "capsule_${capsule.patternId}_${capsule.timestampMs}.json"
        val file = File(capsulesDir, filename)
        
        val json = serializeCapsule(capsule)
        AtomicFile.write(file, json)
        
        screenshot?.let { bmp ->
            val screenshotFile = File(capsulesDir, "screenshot_${capsule.timestampMs}.png")
            FileOutputStream(screenshotFile).use { out ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
    }
    
    private fun buildShareText(capsule: ProofCapsule): String {
        return buildString {
            appendLine("📚 QuantraVision Detection Log")
            appendLine()
            appendLine("Pattern: ${capsule.patternId}")
            appendLine("Time: ${capsule.timestamp}")
            appendLine("Confidence: ${"%.1f".format(capsule.confidence * 100)}%")
            appendLine("Timeframe: ${capsule.timeframe}")
            appendLine()
            appendLine("Hash: ${capsule.sha256Hash.take(16)}...")
            appendLine()
            appendLine("⚠️ EDUCATIONAL LOG ONLY")
            appendLine("This is NOT proof of trading results.")
            appendLine("NOT investment advice or recommendations.")
            appendLine()
            appendLine("Full data in attached JSON file.")
        }
    }
}
