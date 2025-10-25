package com.lamontlabs.quantravision.analysis

import androidx.camera.core.ImageProxy

enum class ChartType { CANDLE, BAR, LINE, HEIKIN_ASHI, RENKO, UNKNOWN }

interface ChartTypeClassifier {
  fun classify(frame: ImageProxy): ChartType
}
