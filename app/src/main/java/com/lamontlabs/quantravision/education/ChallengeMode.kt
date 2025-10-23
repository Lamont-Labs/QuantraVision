package com.lamontlabs.quantravision.education

import android.content.Context
import com.lamontlabs.quantravision.PatternMatch
import java.io.File
import kotlin.random.Random

/**
 * ChallengeMode
 * Interactive learning: presents random chart images from /files/challenges/,
 * user guesses pattern, system reveals answer.
 * Offline, deterministic scoring.
 */
object ChallengeMode {

    data class Challenge(val imageFile: File, val correctPattern: String)

    fun getRandomChallenge(context: Context): Challenge? {
        val dir = File(context.filesDir, "challenges")
        if (!dir.exists()) return null
        val imgs = dir.listFiles { f -> f.extension in listOf("png", "jpg") } ?: return null
        if (imgs.isEmpty()) return null
        val chosen = imgs.random()
        val answer = chosen.nameWithoutExtension.substringAfter("_", "Unknown")
        return Challenge(chosen, answer)
    }

    fun scoreGuess(challenge: Challenge, guess: String): Int {
        return if (guess.trim().equals(challenge.correctPattern, true)) 100 else 0
    }
}
