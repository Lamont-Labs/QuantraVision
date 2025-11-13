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
import com.lamontlabs.quantravision.licensing.ProFeatureGate
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
 * TIER REQUIREMENT: Requires Pro tier ($49.99) only - Intelligence Stack exclusive
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
    
    private val auditTrailFile = File(context.filesDir, "proof_audit_trail.json")
    
    companion object {
        private const val CAPSULE_VERSION = "2.0"  // Updated for blockchain chaining
        private const val APP_VERSION = "2.1"
        private val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    
    /**
     * Proof Capsule with blockchain-style chaining
     * 
     * Immutable, tamper-evident package of pattern detection data
     * Chains to previous capsule for audit trail integrity
     */
    data class ProofCapsule(
        val version: String,
        val appVersion: String,
        val timestamp: String,
        val timestampMs: Long,
        val patternId: String,
        val confidence: Double,
        val timeframe: String,
        val scale: Double,
        val consensusScore: Double,
        val regimeContext: Map<String, Any>?,
        val detectionMetadata: Map<String, Any>,
        val previousHash: String?,  // Blockchain-style chaining
        val chainIndex: Int,        // Position in chain
        val sha256Hash: String,     // Hash of ALL data including previousHash
        val disclaimer: String = "⚠️ Educational detection log only - NOT proof of trading results"
    )
    
    /**
     * Audit trail entry
     */
    data class AuditEntry(
        val capsuleHash: String,
        val timestamp: Long,
        val patternId: String,
        val chainIndex: Int,
        val verified: Boolean
    )
    
    /**
     * Generate proof capsule with blockchain-style chaining
     * 
     * @param patternMatch Pattern detection to package
     * @param screenshot Optional screenshot of detection
     * @param regimeContext Optional market regime context
     * @return Generated proof capsule (chained to previous)
     */
    suspend fun generateCapsule(
        patternMatch: PatternMatch,
        screenshot: Bitmap? = null,
        regimeContext: RegimeNavigator.MarketRegime? = null
    ): ProofCapsule = withContext(Dispatchers.IO) {
        
        // CRITICAL TIER GATE: Proof Capsules requires Pro tier ($49.99) - Intelligence Stack exclusive
        if (!ProFeatureGate.isActive(context)) {
            throw IllegalStateException(
                "Proof Capsules requires Pro tier ($49.99). " +
                "Upgrade to unlock Intelligence Stack features."
            )
        }
        
        // CRITICAL LEGAL GATE: Enforce disclaimer acceptance
        AdvancedFeatureGate.requireAcceptance(context, "Proof Capsules")
        
        try {
            val timestampMs = patternMatch.timestamp
            val timestamp = iso8601Format.format(Date(timestampMs))
            
            val previousCapsule = getLatestCapsule()
            val previousHash = previousCapsule?.sha256Hash
            val chainIndex = (previousCapsule?.chainIndex ?: -1) + 1
            
            val metadata = buildMetadata(patternMatch, screenshot)
            val regimeData = regimeContext?.let { buildRegimeData(it) }
            
            val dataForHash = buildDataForHash(
                appVersion = APP_VERSION,
                timestamp = timestamp,
                patternId = patternMatch.patternName,
                confidence = patternMatch.confidence,
                timeframe = patternMatch.timeframe,
                scale = patternMatch.scale,
                consensusScore = patternMatch.consensusScore,
                metadata = metadata,
                regimeData = regimeData,
                previousHash = previousHash,
                chainIndex = chainIndex
            )
            
            val hash = calculateSHA256(dataForHash)
            
            val capsule = ProofCapsule(
                version = CAPSULE_VERSION,
                appVersion = APP_VERSION,
                timestamp = timestamp,
                timestampMs = timestampMs,
                patternId = patternMatch.patternName,
                confidence = patternMatch.confidence,
                timeframe = patternMatch.timeframe,
                scale = patternMatch.scale,
                consensusScore = patternMatch.consensusScore,
                regimeContext = regimeData,
                detectionMetadata = metadata,
                previousHash = previousHash,
                chainIndex = chainIndex,
                sha256Hash = hash
            )
            
            saveCapsule(capsule, screenshot)
            addToAuditTrail(capsule)
            
            Timber.i("Proof capsule generated: ${patternMatch.patternName} (chain #$chainIndex, hash: ${hash.take(8)}...)")
            
            capsule
            
        } catch (e: Exception) {
            Timber.e(e, "Error generating proof capsule")
            throw e
        }
    }
    
    /**
     * Get latest capsule in chain
     */
    private suspend fun getLatestCapsule(): ProofCapsule? = withContext(Dispatchers.IO) {
        try {
            val capsules = listCapsules()
            if (capsules.isEmpty()) return@withContext null
            
            loadCapsule(capsules.first())
        } catch (e: Exception) {
            Timber.w(e, "Could not load latest capsule")
            null
        }
    }
    
    /**
     * Add capsule to audit trail
     */
    private suspend fun addToAuditTrail(capsule: ProofCapsule) = withContext(Dispatchers.IO) {
        try {
            val trail = loadAuditTrail().toMutableList()
            trail.add(AuditEntry(
                capsuleHash = capsule.sha256Hash,
                timestamp = capsule.timestampMs,
                patternId = capsule.patternId,
                chainIndex = capsule.chainIndex,
                verified = true
            ))
            saveAuditTrail(trail)
        } catch (e: Exception) {
            Timber.e(e, "Error adding to audit trail")
        }
    }
    
    /**
     * Get full audit trail
     */
    suspend fun getAuditTrail(): List<AuditEntry> = withContext(Dispatchers.IO) {
        loadAuditTrail()
    }
    
    /**
     * Verify entire proof chain integrity
     */
    suspend fun verifyChain(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val capsules = listCapsules().reversed() // Oldest first
            if (capsules.isEmpty()) {
                return@withContext Pair(true, "No capsules to verify")
            }
            
            var previousHash: String? = null
            for ((index, file) in capsules.withIndex()) {
                val capsule = loadCapsule(file)
                
                if (capsule.chainIndex != index) {
                    return@withContext Pair(false, "Chain index mismatch at #$index")
                }
                
                if (capsule.previousHash != previousHash) {
                    return@withContext Pair(false, "Chain broken at #$index (hash mismatch)")
                }
                
                if (!verifyCapsule(capsule)) {
                    return@withContext Pair(false, "Capsule #$index failed verification")
                }
                
                previousHash = capsule.sha256Hash
            }
            
            Pair(true, "All ${capsules.size} capsules verified successfully")
            
        } catch (e: Exception) {
            Timber.e(e, "Error verifying chain")
            Pair(false, "Verification error: ${e.message}")
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
                appVersion = capsule.appVersion,
                timestamp = capsule.timestamp,
                patternId = capsule.patternId,
                confidence = capsule.confidence,
                timeframe = capsule.timeframe,
                scale = capsule.scale,
                consensusScore = capsule.consensusScore,
                metadata = capsule.detectionMetadata,
                regimeData = capsule.regimeContext,
                previousHash = capsule.previousHash,
                chainIndex = capsule.chainIndex
            )
            
            val expectedHash = calculateSHA256(dataForHash)
            val isValid = expectedHash == capsule.sha256Hash
            
            if (isValid) {
                Timber.i("Capsule verified: ${capsule.patternId} (chain #${capsule.chainIndex})")
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
        appVersion: String,
        timestamp: String,
        patternId: String,
        confidence: Double,
        timeframe: String,
        scale: Double,
        consensusScore: Double,
        metadata: Map<String, Any>,
        regimeData: Map<String, Any>?,
        previousHash: String?,
        chainIndex: Int
    ): String {
        return buildString {
            append("version=$CAPSULE_VERSION")
            append("|appVersion=$appVersion")
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
            append("|previousHash=${previousHash ?: "null"}")
            append("|chainIndex=$chainIndex")
        }
    }
    
    private fun calculateSHA256(data: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(data.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    private fun serializeCapsule(capsule: ProofCapsule): String {
        val json = JSONObject().apply {
            put("version", capsule.version)
            put("appVersion", capsule.appVersion)
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
            put("previousHash", capsule.previousHash ?: JSONObject.NULL)
            put("chainIndex", capsule.chainIndex)
            put("sha256Hash", capsule.sha256Hash)
            put("disclaimer", capsule.disclaimer)
        }
        
        return json.toString(2)
    }
    
    private fun loadAuditTrail(): List<AuditEntry> {
        if (!auditTrailFile.exists()) return emptyList()
        
        return try {
            val json = org.json.JSONArray(auditTrailFile.readText())
            (0 until json.length()).map { i ->
                val obj = json.getJSONObject(i)
                AuditEntry(
                    capsuleHash = obj.getString("capsuleHash"),
                    timestamp = obj.getLong("timestamp"),
                    patternId = obj.getString("patternId"),
                    chainIndex = obj.getInt("chainIndex"),
                    verified = obj.getBoolean("verified")
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading audit trail")
            emptyList()
        }
    }
    
    private fun saveAuditTrail(trail: List<AuditEntry>) {
        try {
            val json = org.json.JSONArray()
            trail.forEach { entry ->
                json.put(JSONObject().apply {
                    put("capsuleHash", entry.capsuleHash)
                    put("timestamp", entry.timestamp)
                    put("patternId", entry.patternId)
                    put("chainIndex", entry.chainIndex)
                    put("verified", entry.verified)
                })
            }
            AtomicFile.write(auditTrailFile, json.toString(2))
        } catch (e: Exception) {
            Timber.e(e, "Error saving audit trail")
        }
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
            appVersion = obj.optString("appVersion", "2.1"),
            timestamp = obj.getString("timestamp"),
            timestampMs = obj.getLong("timestampMs"),
            patternId = obj.getString("patternId"),
            confidence = obj.getDouble("confidence"),
            timeframe = obj.getString("timeframe"),
            scale = obj.getDouble("scale"),
            consensusScore = obj.getDouble("consensusScore"),
            regimeContext = regimeContext,
            detectionMetadata = metadataMap,
            previousHash = if (obj.isNull("previousHash")) null else obj.getString("previousHash"),
            chainIndex = obj.optInt("chainIndex", 0),
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
