package com.lamontlabs.quantravision.apex

import com.lamontlabs.quantravision.apex.models.ApexScanContext
import com.lamontlabs.quantravision.apex.models.ChartPrimitives
import com.lamontlabs.quantravision.apex.models.ProtocolVerdict
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T01InputSanitization
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T02ChartGeometryValidation
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T03CandleDataQuality
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T04TimeframeConsistency
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T05PriceRangeNormalization
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T06VolatilityAssessment
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T07TrendStrengthGate
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T08VolumeProfileCheck
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T09SupportResistanceDetection
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T10StructureCompleteness
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T11MomentumAlignment
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T12VolumeConfirmation
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T13VolatilityAlignment
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T14PriceActionQuality
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T15MultiTimeframeCoherence
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T16EntropyGateEarly
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T17ConflictDetection
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T18RegimeValidation
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T19NoiseCancellation
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T20FinalEntropyCheck
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T21EntropyThreshold
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T22SignalClarity
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T23ConflictResolution
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T24EntropyDecay
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T25SignalStrength
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T26PatternContinuation
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T27TrendContinuation
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T28MomentumContinuation
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T29VolumeContinuation
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T30BreakoutValidation
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T31DriftDetectionEarly
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T32RegimeShift
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T33AdaptiveThreshold
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T34HistoricalContext
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T35MarketCondition
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T36TimeframeAlignment
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T37CrossTimeframeValidation
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T38ScaleInvariance
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T39TemporalCoherence
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T40MultiFrameEntropy
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T41ContinuationFusion
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T42ContinuationSmoothing
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T43ContinuationConsistency
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T44ContinuationStrength
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T45ContinuationValidation
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T46RegimeAlignment
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T47SectorCompatibility
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T48VolatilityRegimeCheck
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T49TemporalRegimeStability
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T50CrossRegimeCoherence
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T51FalsePositiveSuppression
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T52PatternSuppression
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T53NoiseSuppression
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T54ConflictSuppression
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T55AdaptiveSuppressionThreshold
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T56ExtremeVolatilityDetection
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T57VolatilitySpikeGuard
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T58AbnormalMovementDetection
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T59VolumeAnomalyGuard
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T60MarketStressIndicator
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T61CrossLayerFusion
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T62RegimeContinuationBridge
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T63SuppressionVolatilityGate
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T64MultiSignalIntegration
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T65CoherenceValidator
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T66SectorTrendValidator
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T67MultiFrameContinuationFusion
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T68SectorTimeframeConsistency
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T69CrossSectorValidation
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T70TemporalSectorAlignment
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T71ExoticVolatilityRejection
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T72RiskAmplificationDetector
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T73FlashCrashGuard
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T74LiquidityStressDetector
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T75CascadingFailureGuard
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T76PreVerdictValidation
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T77ConfidenceAggregator
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T78ScoreNormalizer
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T79ProofLogSimilarityHooks
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T80FinalVerdictFusion
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP01SuppressionMemoryLoader
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP02FalsePositiveReconciler
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP03SuppressionDecayCalculator
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP04SuppressionScoreAggregator
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP05SuppressionStateWriter
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP06DriftHistoryLoader
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP07RegimeShiftAnalyzer
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP08VolatilityDriftTracker
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP09TrendDriftCalculator
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP10DriftAdaptationAggregator
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP11PatternHistoryLoader
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP12EffectivenessAggregator
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP13ContextualReliabilityMapper
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP14TierWeightedAdjuster
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP15PatternLearningFinalizer
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP16ConfidenceModifierCalculator
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP17AdaptiveClampEnforcer
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP18OverrideFlagSetter
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP19SensitivityAdjuster
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP20AdaptiveRefinementFinalizer
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP21LearningStateValidator
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP22IntegrityChecker
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP23ProofDigestGenerator
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP24FreshnessTokenValidator
import com.lamontlabs.quantravision.apex.protocols.learning.mobile.LP25LearningStateFinalizer
import com.lamontlabs.quantravision.apex.protocols.omega.mobile.Omega01StructuralAnomalyGuard
import com.lamontlabs.quantravision.apex.protocols.omega.mobile.Omega02RiskCapEnforcer
import com.lamontlabs.quantravision.apex.protocols.omega.mobile.Omega03SecurityValidator
import com.lamontlabs.quantravision.apex.protocols.omega.mobile.Omega04ComplianceGuard

/**
 * BATCH 2-8: Protocol Registry Mobile
 * 
 * Central registry for Apex Engine protocols.
 * Protocols are organized into three categories:
 * - Tier Protocols (T01-T80): Core pattern validation gates
 * - Learning Protocols (LP01-LP25): Adaptive learning and context gates
 * - Omega Protocols (Omega01-04): Safety and sanity check gates
 * 
 * Architecture:
 * - Sealed interface ensures type safety
 * - Deterministic iteration order (LinkedHashMap)
 * - Side-effect free evaluation
 * - State sharing via MutableMap parameter
 * 
 * Execution order (from master spec):
 * 1. Omega protocols (safety checks)
 * 2. Tier protocols (pattern validation)
 * 3. Learning protocols (adaptive refinement)
 */

/**
 * Apex Protocol interface.
 * All protocols must implement this interface.
 * 
 * Protocols are pure functions: given context and primitives, return a verdict.
 * No side effects, no external state mutation.
 * 
 * NOTE: Changed from sealed interface to regular interface to allow
 * implementations in different packages (tier, learning, omega).
 */
interface ApexProtocol {
    
    /**
     * Protocol unique identifier (e.g., "T01", "LP05", "Omega02").
     */
    val protocolId: String
    
    /**
     * Human-readable protocol name for logging/tracing.
     */
    val protocolName: String
    
    /**
     * Protocol weight in final score aggregation.
     * Higher weight = more influence on QuantraScore.
     */
    val weight: Double
    
    /**
     * Evaluate protocol against chart context and primitives.
     * 
     * @param context Scan context (ticker, timeframe, tier, etc.)
     * @param primitives Vision model outputs
     * @param state Mutable state map for protocol coordination and data sharing
     * @return Protocol verdict with pass/fail, confidence, and reason
     */
    suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict
}

/**
 * Protocol Registry singleton.
 * Manages registration and retrieval of all Apex protocols.
 * 
 * BATCH 3-6: T01-T80 implemented and registered.
 * BATCH 7: LP01-LP25 to be implemented.
 * BATCH 8: Omega01-04 to be implemented.
 */
object ProtocolRegistryMobile {
    
    /**
     * Tier protocols registry (T01-T80).
     * Deterministic iteration order via LinkedHashMap.
     * 
     * BATCH 3: T01-T20 implemented
     * - T01-T05: Input Validation & Sanitization
     * - T06-T10: Structural Quality
     * - T11-T15: Momentum & Alignment
     * - T16-T20: Entropy & Conflict Detection
     * 
     * BATCH 4: T21-T40 implemented
     * - T21-T25: Entropy Control Expansion
     * - T26-T30: Continuation Validation
     * - T31-T35: Drift Preliminary Gates
     * - T36-T40: Multi-Frame Scaffolding
     * 
     * BATCH 5: T41-T60 implemented
     * - T41-T45: Continuation Fusion
     * - T46-T50: Regime Alignment Hooks
     * - T51-T55: Suppression Triggers
     * - T56-T60: Volatility Exception Guards
     * 
     * BATCH 6: T61-T80 implemented
     * - T61-T65: Advanced Fusion Layer
     * - T66-T70: Sector & Multi-Frame Analysis
     * - T71-T75: Exotic Volatility & Risk Detection
     * - T76-T80: Final Validation & Verdict
     */
    private val tierProtocols = linkedMapOf<String, ApexProtocol>()
    
    init {
        // BATCH 8: Register Omega01-Omega04 protocols in strict order (BEFORE Tier protocols)
        // Omega Protocols (Omega01-Omega04): Safety hard locks
        registerOmegaProtocol(Omega01StructuralAnomalyGuard())
        registerOmegaProtocol(Omega02RiskCapEnforcer())
        registerOmegaProtocol(Omega03SecurityValidator())
        registerOmegaProtocol(Omega04ComplianceGuard())
        
        // BATCH 3: Register T01-T20 protocols in strict order
        registerTierProtocol(T01InputSanitization())
        registerTierProtocol(T02ChartGeometryValidation())
        registerTierProtocol(T03CandleDataQuality())
        registerTierProtocol(T04TimeframeConsistency())
        registerTierProtocol(T05PriceRangeNormalization())
        registerTierProtocol(T06VolatilityAssessment())
        registerTierProtocol(T07TrendStrengthGate())
        registerTierProtocol(T08VolumeProfileCheck())
        registerTierProtocol(T09SupportResistanceDetection())
        registerTierProtocol(T10StructureCompleteness())
        registerTierProtocol(T11MomentumAlignment())
        registerTierProtocol(T12VolumeConfirmation())
        registerTierProtocol(T13VolatilityAlignment())
        registerTierProtocol(T14PriceActionQuality())
        registerTierProtocol(T15MultiTimeframeCoherence())
        registerTierProtocol(T16EntropyGateEarly())
        registerTierProtocol(T17ConflictDetection())
        registerTierProtocol(T18RegimeValidation())
        registerTierProtocol(T19NoiseCancellation())
        registerTierProtocol(T20FinalEntropyCheck())
        
        // BATCH 4: Register T21-T40 protocols in strict order
        registerTierProtocol(T21EntropyThreshold())
        registerTierProtocol(T22SignalClarity())
        registerTierProtocol(T23ConflictResolution())
        registerTierProtocol(T24EntropyDecay())
        registerTierProtocol(T25SignalStrength())
        registerTierProtocol(T26PatternContinuation())
        registerTierProtocol(T27TrendContinuation())
        registerTierProtocol(T28MomentumContinuation())
        registerTierProtocol(T29VolumeContinuation())
        registerTierProtocol(T30BreakoutValidation())
        registerTierProtocol(T31DriftDetectionEarly())
        registerTierProtocol(T32RegimeShift())
        registerTierProtocol(T33AdaptiveThreshold())
        registerTierProtocol(T34HistoricalContext())
        registerTierProtocol(T35MarketCondition())
        registerTierProtocol(T36TimeframeAlignment())
        registerTierProtocol(T37CrossTimeframeValidation())
        registerTierProtocol(T38ScaleInvariance())
        registerTierProtocol(T39TemporalCoherence())
        registerTierProtocol(T40MultiFrameEntropy())
        
        // BATCH 5: Register T41-T60 protocols in strict order
        registerTierProtocol(T41ContinuationFusion())
        registerTierProtocol(T42ContinuationSmoothing())
        registerTierProtocol(T43ContinuationConsistency())
        registerTierProtocol(T44ContinuationStrength())
        registerTierProtocol(T45ContinuationValidation())
        registerTierProtocol(T46RegimeAlignment())
        registerTierProtocol(T47SectorCompatibility())
        registerTierProtocol(T48VolatilityRegimeCheck())
        registerTierProtocol(T49TemporalRegimeStability())
        registerTierProtocol(T50CrossRegimeCoherence())
        registerTierProtocol(T51FalsePositiveSuppression())
        registerTierProtocol(T52PatternSuppression())
        registerTierProtocol(T53NoiseSuppression())
        registerTierProtocol(T54ConflictSuppression())
        registerTierProtocol(T55AdaptiveSuppressionThreshold())
        registerTierProtocol(T56ExtremeVolatilityDetection())
        registerTierProtocol(T57VolatilitySpikeGuard())
        registerTierProtocol(T58AbnormalMovementDetection())
        registerTierProtocol(T59VolumeAnomalyGuard())
        registerTierProtocol(T60MarketStressIndicator())
        
        // BATCH 6: Register T61-T80 protocols in strict order
        registerTierProtocol(T61CrossLayerFusion())
        registerTierProtocol(T62RegimeContinuationBridge())
        registerTierProtocol(T63SuppressionVolatilityGate())
        registerTierProtocol(T64MultiSignalIntegration())
        registerTierProtocol(T65CoherenceValidator())
        registerTierProtocol(T66SectorTrendValidator())
        registerTierProtocol(T67MultiFrameContinuationFusion())
        registerTierProtocol(T68SectorTimeframeConsistency())
        registerTierProtocol(T69CrossSectorValidation())
        registerTierProtocol(T70TemporalSectorAlignment())
        registerTierProtocol(T71ExoticVolatilityRejection())
        registerTierProtocol(T72RiskAmplificationDetector())
        registerTierProtocol(T73FlashCrashGuard())
        registerTierProtocol(T74LiquidityStressDetector())
        registerTierProtocol(T75CascadingFailureGuard())
        registerTierProtocol(T76PreVerdictValidation())
        registerTierProtocol(T77ConfidenceAggregator())
        registerTierProtocol(T78ScoreNormalizer())
        registerTierProtocol(T79ProofLogSimilarityHooks())
        registerTierProtocol(T80FinalVerdictFusion())
        
        // BATCH 7: Register LP01-LP25 protocols in strict order
        // LP01-LP05: Suppression Memory Foundation
        registerLearningProtocol(LP01SuppressionMemoryLoader())
        registerLearningProtocol(LP02FalsePositiveReconciler())
        registerLearningProtocol(LP03SuppressionDecayCalculator())
        registerLearningProtocol(LP04SuppressionScoreAggregator())
        registerLearningProtocol(LP05SuppressionStateWriter())
        
        // LP06-LP10: Drift Detection
        registerLearningProtocol(LP06DriftHistoryLoader())
        registerLearningProtocol(LP07RegimeShiftAnalyzer())
        registerLearningProtocol(LP08VolatilityDriftTracker())
        registerLearningProtocol(LP09TrendDriftCalculator())
        registerLearningProtocol(LP10DriftAdaptationAggregator())
        
        // LP11-LP15: Pattern Learning
        registerLearningProtocol(LP11PatternHistoryLoader())
        registerLearningProtocol(LP12EffectivenessAggregator())
        registerLearningProtocol(LP13ContextualReliabilityMapper())
        registerLearningProtocol(LP14TierWeightedAdjuster())
        registerLearningProtocol(LP15PatternLearningFinalizer())
        
        // LP16-LP20: Adaptive Refinement
        registerLearningProtocol(LP16ConfidenceModifierCalculator())
        registerLearningProtocol(LP17AdaptiveClampEnforcer())
        registerLearningProtocol(LP18OverrideFlagSetter())
        registerLearningProtocol(LP19SensitivityAdjuster())
        registerLearningProtocol(LP20AdaptiveRefinementFinalizer())
        
        // LP21-LP25: Learning State Finalization
        registerLearningProtocol(LP21LearningStateValidator())
        registerLearningProtocol(LP22IntegrityChecker())
        registerLearningProtocol(LP23ProofDigestGenerator())
        registerLearningProtocol(LP24FreshnessTokenValidator())
        registerLearningProtocol(LP25LearningStateFinalizer())
    }
    
    /**
     * Learning protocols registry (LP01-LP25).
     * Adaptive learning and context-aware gates.
     * 
     * BATCH 7: LP01-LP25 implemented and registered.
     * - LP01-LP05: Suppression Memory Foundation
     * - LP06-LP10: Drift Detection
     * - LP11-LP15: Pattern Learning
     * - LP16-LP20: Adaptive Refinement
     * - LP21-LP25: Learning State Finalization
     */
    private val learningProtocols = linkedMapOf<String, ApexProtocol>()
    
    /**
     * Omega protocols registry (Omega01-04).
     * Safety and sanity check gates.
     * 
     * TODO BATCH 8: Implement Omega01-04 and register here.
     * Expected protocols:
     * - Omega01: Chart health check (image quality, resolution)
     * - Omega02: Data completeness check (sufficient price bars)
     * - Omega03: Extreme volatility guard (flash crash detection)
     * - Omega04: Entropy threshold guard (too much uncertainty)
     */
    private val omegaProtocols = linkedMapOf<String, ApexProtocol>()
    
    /**
     * Register a tier protocol.
     * 
     * @param protocol Protocol implementation
     * @throws IllegalStateException if protocol ID already registered
     */
    fun registerTierProtocol(protocol: ApexProtocol) {
        require(protocol.protocolId.startsWith("T")) {
            "Tier protocol ID must start with 'T': ${protocol.protocolId}"
        }
        
        if (tierProtocols.containsKey(protocol.protocolId)) {
            throw IllegalStateException("Tier protocol already registered: ${protocol.protocolId}")
        }
        
        tierProtocols[protocol.protocolId] = protocol
    }
    
    /**
     * Register a learning protocol.
     * 
     * @param protocol Protocol implementation
     * @throws IllegalStateException if protocol ID already registered
     */
    fun registerLearningProtocol(protocol: ApexProtocol) {
        require(protocol.protocolId.startsWith("LP")) {
            "Learning protocol ID must start with 'LP': ${protocol.protocolId}"
        }
        
        if (learningProtocols.containsKey(protocol.protocolId)) {
            throw IllegalStateException("Learning protocol already registered: ${protocol.protocolId}")
        }
        
        learningProtocols[protocol.protocolId] = protocol
    }
    
    /**
     * Register an Omega protocol.
     * 
     * @param protocol Protocol implementation
     * @throws IllegalStateException if protocol ID already registered
     */
    fun registerOmegaProtocol(protocol: ApexProtocol) {
        require(protocol.protocolId.startsWith("Omega")) {
            "Omega protocol ID must start with 'Omega': ${protocol.protocolId}"
        }
        
        if (omegaProtocols.containsKey(protocol.protocolId)) {
            throw IllegalStateException("Omega protocol already registered: ${protocol.protocolId}")
        }
        
        omegaProtocols[protocol.protocolId] = protocol
    }
    
    /**
     * Get all tier protocols in registration order.
     * Deterministic iteration via LinkedHashMap.
     */
    fun getTierProtocols(): List<ApexProtocol> {
        return tierProtocols.values.toList()
    }
    
    /**
     * Get all learning protocols in registration order.
     */
    fun getLearningProtocols(): List<ApexProtocol> {
        return learningProtocols.values.toList()
    }
    
    /**
     * Get all Omega protocols in registration order.
     */
    fun getOmegaProtocols(): List<ApexProtocol> {
        return omegaProtocols.values.toList()
    }
    
    /**
     * Get protocol by ID across all registries.
     * 
     * @param protocolId Protocol identifier
     * @return Protocol instance or null if not found
     */
    fun getProtocolById(protocolId: String): ApexProtocol? {
        return when {
            protocolId.startsWith("T") -> tierProtocols[protocolId]
            protocolId.startsWith("LP") -> learningProtocols[protocolId]
            protocolId.startsWith("Omega") -> omegaProtocols[protocolId]
            else -> null
        }
    }
    
    /**
     * Get total registered protocol count.
     */
    fun getTotalProtocolCount(): Int {
        return tierProtocols.size + learningProtocols.size + omegaProtocols.size
    }
    
    /**
     * Clear all registries (for testing only).
     */
    fun clearAll() {
        tierProtocols.clear()
        learningProtocols.clear()
        omegaProtocols.clear()
    }
}
