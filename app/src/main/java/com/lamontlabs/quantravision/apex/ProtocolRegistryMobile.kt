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

/**
 * BATCH 2-4: Protocol Registry Mobile
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
 * Apex Protocol sealed interface.
 * All protocols must implement this interface.
 * 
 * Protocols are pure functions: given context and primitives, return a verdict.
 * No side effects, no external state mutation.
 */
sealed interface ApexProtocol {
    
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
 * BATCH 3: T01-T20 implemented and registered.
 * BATCH 4-6: T21-T80 to be implemented.
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
     */
    private val tierProtocols = linkedMapOf<String, ApexProtocol>()
    
    init {
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
        
        // TODO BATCH 5-6: Register T41-T80 protocols here
    }
    
    /**
     * Learning protocols registry (LP01-LP25).
     * Adaptive learning and context-aware gates.
     * 
     * TODO BATCH 7: Implement LP01-LP25 and register here.
     * Expected protocols include:
     * - LP01: Historical pattern success rate
     * - LP02: User feedback integration
     * - LP03: Market regime adaptation
     * - LP04-LP25: Additional adaptive gates
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

/**
 * Stub protocol for Batch 2 testing.
 * Returns a passing verdict with fixed confidence.
 * 
 * TODO BATCH 3+: Remove this stub once real protocols are implemented.
 */
class StubProtocol(
    override val protocolId: String,
    override val protocolName: String,
    override val weight: Double = 1.0,
    private val shouldPass: Boolean = true,
    private val stubConfidence: Double = 0.75
) : ApexProtocol {
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        return ProtocolVerdict(
            protocolId = protocolId,
            protocolName = protocolName,
            passed = shouldPass,
            confidence = stubConfidence,
            reason = "STUB: Batch 2 placeholder verdict",
            weight = weight
        )
    }
}
