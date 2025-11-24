# QuantraVision Development History

This document preserves the detailed historical record of QuantraVision's development batches for future reference.

---

## November 24, 2025 - Batch 9 Complete: Cloud Narration Pipeline

**New Components (7 files):**
- QuotaGate.kt: Tier-based quota enforcement (FREE=0, PRO=10/day, ULTRA=25/day)
- CloudReasoner.kt: OpenAI API integration with 15s timeout
- LLMContractValidator.kt: Forbidden words filtering, schema validation
- LocalSummaryGenerator.kt: Deterministic template-based explanations
- AutoExplainManager.kt: Auto-trigger logic for smart explanations
- PrimitiveExtractor.kt: Vision extraction with deterministic hashing
- ExplainOrchestrator.kt: Complete cloud pipeline wiring

**Critical Fixes:**
- Tier mapping consistency (STARTER→PRO, STANDARD/PRO→ULTRA)
- Deterministic pixel-based SHA-256 hashing
- Quota counts ALL cloud attempts (fail-closed)
- All paid tiers eligible for auto-explain

**Architect Approval:** ✅ PASS

---

## Historical Architecture Notes

### Core Intelligence System (Legacy)
The system initially used OpenCV for template matching with 109 PNG reference images, coordinated by `HybridDetectorBridge` and scored by `BayesianFusionEngine`. Multi-signal analysis was performed by combining pattern detection with OCR-extracted indicators (RSI, MACD, volume) using Google ML Kit Text Recognition, with `IndicatorExtractor`, `ContextAnalyzer`, and `QuantraScorer`. A `SmartFilter` handled quality thresholding, and `HistoricalAnalyzer` provided self-improvement. An Ensemble AI Engine facilitated retrieval-based Q&A via `QuantraBot AI Assistant` using all-MiniLM-L6-v2 sentence embeddings, and an AI Explanation Engine generated natural language explanations.

---

*For current operational documentation, see `replit.md`.*
