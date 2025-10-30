package com.lamontlabs.quantravision.detection

import android.content.Context
import kotlinx.coroutines.delay

data class Detection(val id: String, val name: String, val confidence: Int)

class Detector {
  suspend fun demoScan(): List<Detection> {
    delay(300)
    return if (PatternLibrary.demoBoxes)
      listOf(
        Detection("FLG_BULL", "Bullish Flag", 92),
        Detection("HNS001", "Head & Shoulders", 88)
      )
    else emptyList()
  }

  fun load(context: Context) {
    PatternLibrary.load(context)
  }
}
