package com.lamontlabs.quantravision.learning.advanced.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.learning.advanced.model.MarketCondition

@Dao
interface AdvancedLearningDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorrelation(entity: PatternCorrelationEntity)
    
    @Query("SELECT * FROM pattern_correlations WHERE pattern1 = :pattern1 AND pattern2 = :pattern2")
    suspend fun getCorrelation(pattern1: String, pattern2: String): PatternCorrelationEntity?
    
    @Query("SELECT * FROM pattern_correlations ORDER BY correlation DESC LIMIT :limit")
    suspend fun getTopCorrelations(limit: Int): List<PatternCorrelationEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSequence(entity: PatternSequenceEntity)
    
    @Query("SELECT * FROM pattern_sequences ORDER BY frequency DESC LIMIT :limit")
    suspend fun getCommonSequences(limit: Int): List<PatternSequenceEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketConditionOutcome(entity: MarketConditionOutcomeEntity)
    
    @Query("SELECT * FROM market_condition_outcomes WHERE patternType = :patternType AND marketCondition = :condition")
    suspend fun getOutcomesByCondition(patternType: String, condition: MarketCondition): List<MarketConditionOutcomeEntity>
    
    @Query("SELECT * FROM market_condition_outcomes WHERE marketCondition = :condition")
    suspend fun getAllOutcomesForCondition(condition: MarketCondition): List<MarketConditionOutcomeEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemporalData(entity: TemporalDataEntity)
    
    @Query("SELECT * FROM temporal_outcomes WHERE patternType = :patternType")
    suspend fun getTemporalData(patternType: String): List<TemporalDataEntity>
    
    @Query("SELECT * FROM temporal_outcomes WHERE patternType = :patternType AND hourOfDay = :hour")
    suspend fun getTemporalDataByHour(patternType: String, hour: Int): List<TemporalDataEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBehavioralEvent(entity: BehavioralEventEntity)
    
    @Query("SELECT * FROM behavioral_events WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getSessionEvents(sessionId: String): List<BehavioralEventEntity>
    
    @Query("SELECT * FROM behavioral_events WHERE timestamp > :since ORDER BY timestamp DESC")
    suspend fun getRecentBehavioralEvents(since: Long): List<BehavioralEventEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStrategyMetrics(entity: StrategyMetricsEntity)
    
    @Query("SELECT * FROM strategy_metrics ORDER BY sharpeRatio DESC LIMIT :limit")
    suspend fun getTopStrategies(limit: Int): List<StrategyMetricsEntity>
    
    @Query("DELETE FROM pattern_correlations WHERE lastUpdated < :before")
    suspend fun deleteOldCorrelations(before: Long)
    
    @Query("DELETE FROM pattern_sequences WHERE lastSeen < :before")
    suspend fun deleteOldSequences(before: Long)
    
    @Query("DELETE FROM market_condition_outcomes WHERE timestamp < :before")
    suspend fun deleteOldConditionOutcomes(before: Long)
    
    @Query("DELETE FROM temporal_outcomes WHERE timestamp < :before")
    suspend fun deleteOldTemporalData(before: Long)
    
    @Query("DELETE FROM behavioral_events WHERE timestamp < :before")
    suspend fun deleteOldBehavioralEvents(before: Long)
}
