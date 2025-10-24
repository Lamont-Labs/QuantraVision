package com.lamontlabs.quantravision.detection

import android.content.Context
import com.google.gson.Gson
import java.io.InputStreamReader

object PatternLibrary {
  private val gson = Gson()
  private var loaded = false
  var demoBoxes: Boolean = true
    private set

  data class PatternDef(val id: String, val name: String, val threshold: Float)

  lateinit var patterns: List<PatternDef>; private set

  fun load(context: Context) {
    if (loaded) return
    val stream = context.assets.open("patterns.json")
    patterns = gson.fromJson(InputStreamReader(stream), Array<PatternDef>::class.java).toList()
    loaded = true
  }

  fun toggleDemoBoxes() { demoBoxes = !demoBoxes }
}
