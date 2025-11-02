package com.lamontlabs.quantravision.analytics.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.analytics.model.PatternOutcome

@Dao
interface PatternOutcomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(outcome: PatternOutcome)
    
    @Query("SELECT * FROM PatternOutcome WHERE patternMatchId = :patternMatchId")
    suspend fun getByPatternId(patternMatchId: Int): PatternOutcome?
    
    @Query("SELECT * FROM PatternOutcome WHERE patternName = :patternName ORDER BY timestamp DESC")
    suspend fun getByPatternType(patternName: String): List<PatternOutcome>
    
    @Query("SELECT * FROM PatternOutcome WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getByDateRange(startTime: Long, endTime: Long): List<PatternOutcome>
    
    @Query("SELECT * FROM PatternOutcome ORDER BY timestamp DESC")
    suspend fun getAll(): List<PatternOutcome>
    
    @Query("SELECT COUNT(*) FROM PatternOutcome WHERE patternName = :patternName AND outcome = :outcome")
    suspend fun getOutcomeCount(patternName: String, outcome: Outcome): Int
    
    @Query("SELECT COUNT(*) FROM PatternOutcome WHERE patternName = :patternName")
    suspend fun getTotalCount(patternName: String): Int
    
    @Query("SELECT AVG(profitLossPercent) FROM PatternOutcome WHERE patternName = :patternName AND profitLossPercent IS NOT NULL")
    suspend fun getAvgProfitLoss(patternName: String): Double?
    
    @Query("DELETE FROM PatternOutcome WHERE timestamp < :before")
    suspend fun deleteOld(before: Long)
}
