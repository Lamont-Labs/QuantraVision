package com.lamontlabs.quantravision

import android.content.Context
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import timber.log.Timber
import java.io.File
import java.security.MessageDigest

class PatternDetector(private val context: Context) {

    private val templateLibrary = TemplateLibrary(context)
    private val db = PatternDatabase.getInstance(context)
    private val provenance = Provenance(context)

    suspend fun scanStaticAssets() = withContext(Dispatchers.Default) {
        val dir = File(context.filesDir, "demo_charts")
        if (!dir.exists()) return@withContext
        dir.listFiles()?.forEach { imageFile ->
            try {
                val bmp = BitmapFactory.decodeFile(imageFile.absolutePath)
                val mat = Mat()
                Utils.bitmapToMat(bmp, mat)
                val matches = detectPatterns(mat)
                for (match in matches) {
                    db.patternDao().insert(match)
                    provenance.logHash(imageFile, match.patternName)
                }
                Timber.i("Detected ${matches.size} patterns in ${imageFile.name}")
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun detectPatterns(input: Mat): List<PatternMatch> {
        val matches = mutableListOf<PatternMatch>()
        val templates = templateLibrary.loadTemplates()
        templates.forEach { tpl ->
            val result = Mat()
            Imgproc.matchTemplate(input, tpl.image, result, Imgproc.TM_CCOEFF_NORMED)
            val mmr = Core.minMaxLoc(result)
            if (mmr.maxVal > tpl.threshold) {
                matches.add(
                    PatternMatch(
                        patternName = tpl.name,
                        confidence = mmr.maxVal,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
        return matches
    }
}
