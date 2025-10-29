package com.lamontlabs.quantravision.gamification

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * BonusHighlights
 * Manages bonus highlight credits earned through achievements
 * Only available for Standard and Pro tiers (NOT free tier)
 */
object BonusHighlights {

    private const val FILE = "bonus_highlights.json"

    data class BonusEntry(
        val amount: Int,
        val reason: String,
        val date: String
    )

    fun add(context: Context, amount: Int, reason: String) {
        // Free tier users don't earn bonus highlights
        val prefs = context.getSharedPreferences("quantravision_prefs", Context.MODE_PRIVATE)
        val tier = prefs.getString("tier", "FREE") ?: "FREE"
        if (tier == "FREE") return
        
        val state = loadState(context)
        val current = state.optInt("total", 0)
        state.put("total", current + amount)

        val history = state.optJSONArray("history") ?: JSONArray()
        val today = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        history.put(JSONObject().apply {
            put("amount", amount)
            put("reason", reason)
            put("date", today)
        })
        state.put("history", history)

        saveState(context, state)
    }

    fun use(context: Context, amount: Int = 1): Boolean {
        // Free tier users can't use bonus highlights
        val prefs = context.getSharedPreferences("quantravision_prefs", Context.MODE_PRIVATE)
        val tier = prefs.getString("tier", "FREE") ?: "FREE"
        if (tier == "FREE") return false
        
        val state = loadState(context)
        val current = state.optInt("total", 0)
        if (current < amount) return false

        state.put("total", current - amount)
        saveState(context, state)
        return true
    }

    fun available(context: Context): Int {
        val state = loadState(context)
        return state.optInt("total", 0)
    }

    fun getHistory(context: Context): List<BonusEntry> {
        val state = loadState(context)
        val history = state.optJSONArray("history") ?: return emptyList()
        val entries = mutableListOf<BonusEntry>()
        
        for (i in 0 until history.length()) {
            val obj = history.getJSONObject(i)
            entries.add(
                BonusEntry(
                    amount = obj.getInt("amount"),
                    reason = obj.getString("reason"),
                    date = obj.getString("date")
                )
            )
        }
        
        return entries
    }

    private fun loadState(context: Context): JSONObject {
        val f = File(context.filesDir, FILE)
        return if (f.exists()) JSONObject(f.readText()) else JSONObject()
    }

    private fun saveState(context: Context, obj: JSONObject) {
        File(context.filesDir, FILE).writeText(obj.toString(2))
    }
}
