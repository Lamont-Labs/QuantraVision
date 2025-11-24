package com.lamontlabs.quantravision.apex

import com.lamontlabs.quantravision.apex.models.ApexScanContext
import com.lamontlabs.quantravision.apex.models.ChartPrimitives
import com.lamontlabs.quantravision.apex.models.ProtocolVerdict

/**
 * BATCH 2: Protocol Registry Mobile
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
 * - TODO markers for future batch implementation
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
     * @return Protocol verdict with pass/fail, confidence, and reason
     */
    suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives
    ): ProtocolVerdict
}

/**
 * Protocol Registry singleton.
 * Manages registration and retrieval of all Apex protocols.
 * 
 * TODO BATCH 3-8: Implement actual protocol classes and register them here.
 */
object ProtocolRegistryMobile {
    
    /**
     * Tier protocols registry (T01-T80).
     * Deterministic iteration order via LinkedHashMap.
     * 
     * TODO BATCH 3-6: Implement T01-T80 and register here.
     * Expected protocols include:
     * - T01: Trend alignment
     * - T02: Volume confirmation
     * - T03: Volatility gate
     * - T04: Support/resistance validation
     * - T05-T80: Additional pattern-specific gates
     */
    private val tierProtocols = linkedMapOf<String, ApexProtocol>()
    
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
        primitives: ChartPrimitives
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
