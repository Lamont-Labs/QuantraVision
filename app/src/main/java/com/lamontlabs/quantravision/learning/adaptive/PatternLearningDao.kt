package com.lamontlabs.quantravision.learning.adaptive

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * DAO for Pattern Learning Engine
 */
@Dao
interface PatternLearningDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: PatternIndicatorProfile)
    
    @Update
    suspend fun updateProfile(profile: PatternIndicatorProfile)
    
    @Query("SELECT * FROM pattern_indicator_profiles WHERE patternName = :patternName")
    suspend fun getProfile(patternName: String): PatternIndicatorProfile?
    
    @Query("SELECT * FROM pattern_indicator_profiles")
    suspend fun getAllProfiles(): List<PatternIndicatorProfile>
    
    @Query("SELECT * FROM pattern_indicator_profiles WHERE learningPhase IN ('ADAPTIVE', 'EXPERT')")
    suspend fun getAdaptiveProfiles(): List<PatternIndicatorProfile>
    
    @Query("SELECT COUNT(*) FROM pattern_indicator_profiles WHERE learningPhase IN ('ADAPTIVE', 'EXPERT')")
    suspend fun getAdaptiveProfileCount(): Int
    
    @Query("DELETE FROM pattern_indicator_profiles WHERE patternName = :patternName")
    suspend fun deleteProfile(patternName: String)
    
    @Query("DELETE FROM pattern_indicator_profiles")
    suspend fun clearAllProfiles()
}
