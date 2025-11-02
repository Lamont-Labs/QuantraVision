package com.lamontlabs.quantravision.achievements.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    suspend fun getAll(): List<AchievementEntity>
    
    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getById(id: String): AchievementEntity?
    
    @Query("SELECT * FROM achievements WHERE isUnlocked = 1")
    suspend fun getUnlocked(): List<AchievementEntity>
    
    @Query("SELECT COUNT(*) FROM achievements WHERE isUnlocked = 1")
    suspend fun getUnlockedCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(achievement: AchievementEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<AchievementEntity>)
    
    @Update
    suspend fun update(achievement: AchievementEntity)
    
    @Query("UPDATE achievements SET isUnlocked = 1, unlockedAt = :timestamp WHERE id = :id")
    suspend fun unlock(id: String, timestamp: Long)
    
    @Query("UPDATE achievements SET progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int)
    
    @Query("DELETE FROM achievements")
    suspend fun deleteAll()
}
