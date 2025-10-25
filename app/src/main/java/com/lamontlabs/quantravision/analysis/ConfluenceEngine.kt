package com.lamontlabs.quantravision.analysis

import com.lamontlabs.quantravision.detection.Detection

data class ConfluenceResult(
  val score: Float,              // 0..1
  val agreeingTimeframes: List<String> = emptyList()
)

interface ConfluenceEngine {
  fun score(detectionsByTf: Map<String, List<Detection>>): ConfluenceResult
}
