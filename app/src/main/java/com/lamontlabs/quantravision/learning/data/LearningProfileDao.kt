package com.lamontlabs.quantravision.learning.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lamontlabs.quantravision.learning.model.ConfidenceProfile
import com.lamontlabs.quantravision.learning.model.LearningMetadata
import com.lamontlabs.quantravision.learning.model.SuppressionRule

@Dao
interface LearningProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfidenceProfile(profile: ConfidenceProfile)
    
    @Update
    suspend fun updateConfidenceProfile(profile: ConfidenceProfile)
    
    @Query("SELECT * FROM confidence_profiles WHERE patternType = :patternType")
    suspend fun getConfidenceProfile(patternType: String): ConfidenceProfile?
    
    @Query("SELECT * FROM confidence_profiles WHERE totalOutcomes >= :minOutcomes")
    suspend fun getProfilesWithMinData(minOutcomes: Int): List<ConfidenceProfile>
    
    @Query("SELECT * FROM confidence_profiles")
    suspend fun getAllProfiles(): List<ConfidenceProfile>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppressionRule(rule: SuppressionRule)
    
    @Update
    suspend fun updateSuppressionRule(rule: SuppressionRule)
    
    @Query("SELECT * FROM suppression_rules WHERE patternType = :patternType")
    suspend fun getSuppressionRule(patternType: String): SuppressionRule?
    
    @Query("SELECT * FROM suppression_rules WHERE isUserOverridden = 0")
    suspend fun getActiveSuppressionRules(): List<SuppressionRule>
    
    @Query("SELECT * FROM suppression_rules")
    suspend fun getAllSuppressionRules(): List<SuppressionRule>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: LearningMetadata)
    
    @Query("SELECT * FROM learning_metadata WHERE key = :key")
    suspend fun getMetadata(key: String): LearningMetadata?
    
    @Query("SELECT * FROM learning_metadata")
    suspend fun getAllMetadata(): List<LearningMetadata>
    
    @Query("DELETE FROM confidence_profiles")
    suspend fun clearAllProfiles()
    
    @Query("DELETE FROM suppression_rules WHERE isUserOverridden = 0")
    suspend fun clearNonOverriddenRules()
    
    // Scan History
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanHistory(scan: ScanHistoryEntity)
    
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentScans(limit: Int = 100): List<ScanHistoryEntity>
    
    @Query("SELECT COUNT(*) FROM scan_history WHERE timestamp > :since")
    suspend fun getTotalScans(since: Long): Int
    
    // Pattern Frequency
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatternFrequency(freq: PatternFrequencyEntity)
    
    @Query("SELECT * FROM pattern_frequency ORDER BY totalDetections DESC")
    suspend fun getAllPatternFrequencies(): List<PatternFrequencyEntity>
    
    @Query("SELECT * FROM pattern_frequency WHERE patternName = :pattern")
    suspend fun getPatternFrequency(pattern: String): PatternFrequencyEntity?
    
    // Pattern Co-occurrence
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCooccurrence(cooccurrence: PatternCooccurrenceEntity)
    
    @Query("SELECT * FROM pattern_cooccurrence WHERE pattern1 = :pattern OR pattern2 = :pattern ORDER BY cooccurrenceRate DESC LIMIT 10")
    suspend fun getCooccurrencesFor(pattern: String): List<PatternCooccurrenceEntity>
    
    @Query("SELECT * FROM pattern_cooccurrence ORDER BY cooccurrenceRate DESC LIMIT 20")
    suspend fun getTopCooccurrences(): List<PatternCooccurrenceEntity>
}
