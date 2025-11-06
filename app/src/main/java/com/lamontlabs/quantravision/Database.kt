package com.lamontlabs.quantravision

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lamontlabs.quantravision.achievements.data.AchievementDao
import com.lamontlabs.quantravision.achievements.data.AchievementEntity
import com.lamontlabs.quantravision.analytics.data.PatternOutcomeDao
import com.lamontlabs.quantravision.analytics.model.PatternOutcome
import com.lamontlabs.quantravision.learning.data.LearningProfileDao
import com.lamontlabs.quantravision.learning.model.ConfidenceProfile
import com.lamontlabs.quantravision.learning.model.LearningMetadata
import com.lamontlabs.quantravision.learning.model.SuppressionRule
import com.lamontlabs.quantravision.learning.advanced.data.AdvancedLearningDao
import com.lamontlabs.quantravision.learning.advanced.data.PatternCorrelationEntity
import com.lamontlabs.quantravision.learning.advanced.data.PatternSequenceEntity
import com.lamontlabs.quantravision.learning.advanced.data.MarketConditionOutcomeEntity
import com.lamontlabs.quantravision.learning.advanced.data.TemporalDataEntity
import com.lamontlabs.quantravision.learning.advanced.data.BehavioralEventEntity
import com.lamontlabs.quantravision.learning.advanced.data.StrategyMetricsEntity
import com.lamontlabs.quantravision.learning.data.ScanHistoryEntity
import com.lamontlabs.quantravision.learning.data.PatternFrequencyEntity
import com.lamontlabs.quantravision.learning.data.PatternCooccurrenceEntity

/**
 * Trade scenario information for Pattern-to-Plan integration
 * Not persisted in database - transient overlay data only
 */
data class TradeScenarioInfo(
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double
)

@Entity
data class PatternMatch(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patternName: String,
    val confidence: Double,
    val timestamp: Long,
    val timeframe: String,
    val scale: Double,
    val consensusScore: Double,   // Consensus across scales
    val windowMs: Long,           // Temporal stability window contribution
    val originPath: String = "",  // Source image path (e.g., "validation/test_1234567890.png")
    val detectionBounds: String? = null  // Bounding box as "x,y,w,h" or null if unavailable
) {
    @Ignore
    var tradeScenario: TradeScenarioInfo? = null  // Pattern-to-Plan trade info (Pro tier only, not persisted)
}

@Dao
interface PatternDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(match: PatternMatch)

    @Query("SELECT * FROM PatternMatch ORDER BY timestamp DESC")
    suspend fun getAll(): List<PatternMatch>
    
    @Query("SELECT * FROM PatternMatch WHERE timestamp > :since ORDER BY timestamp DESC")
    suspend fun getRecent(since: Long): List<PatternMatch>
}

@Entity
data class PredictedPattern(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patternName: String,
    val completionPercent: Double,
    val confidence: Double,
    val timestamp: Long,
    val timeframe: String,
    val estimatedCompletion: String,
    val stage: String,
    val formationVelocity: Double
)

@Dao
interface PredictedPatternDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prediction: PredictedPattern)

    @Query("SELECT * FROM PredictedPattern ORDER BY timestamp DESC")
    suspend fun getAll(): List<PredictedPattern>
    
    @Query("SELECT * FROM PredictedPattern WHERE timestamp > :since ORDER BY completionPercent DESC")
    suspend fun getRecent(since: Long): List<PredictedPattern>
    
    @Query("DELETE FROM PredictedPattern WHERE timestamp < :before")
    suspend fun deleteOld(before: Long)
}

@Entity
data class InvalidatedPattern(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patternName: String,
    val previousConfidence: Double,
    val finalConfidence: Double,
    val invalidationReason: String,
    val timestamp: Long,
    val timeframe: String = "unknown"
)

@Dao
interface InvalidatedPatternDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invalidation: InvalidatedPattern)

    @Query("SELECT * FROM InvalidatedPattern ORDER BY timestamp DESC")
    suspend fun getAll(): List<InvalidatedPattern>
    
    @Query("SELECT * FROM InvalidatedPattern WHERE timestamp > :since ORDER BY timestamp DESC")
    suspend fun getRecent(since: Long): List<InvalidatedPattern>
    
    @Query("DELETE FROM InvalidatedPattern WHERE timestamp < :before")
    suspend fun deleteOld(before: Long)
    
    @Query("SELECT COUNT(*) FROM InvalidatedPattern WHERE patternName = :name")
    suspend fun getInvalidationCount(name: String): Int
}

@Entity
data class TrainingExampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val features: String,
    val labelDetected: String?,
    val labelActual: String,
    val userConfidence: Float,
    val timestamp: Long
)

@Dao
interface TrainingExampleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(example: TrainingExampleEntity)

    @Query("SELECT * FROM TrainingExampleEntity ORDER BY timestamp DESC")
    suspend fun getAll(): List<TrainingExampleEntity>
    
    @Query("SELECT COUNT(*) FROM TrainingExampleEntity")
    suspend fun getCount(): Int
}

@Database(entities = [PatternMatch::class, PredictedPattern::class, InvalidatedPattern::class, PatternOutcome::class, AchievementEntity::class, ConfidenceProfile::class, SuppressionRule::class, LearningMetadata::class, PatternCorrelationEntity::class, PatternSequenceEntity::class, MarketConditionOutcomeEntity::class, TemporalDataEntity::class, BehavioralEventEntity::class, StrategyMetricsEntity::class, ScanHistoryEntity::class, PatternFrequencyEntity::class, PatternCooccurrenceEntity::class, TrainingExampleEntity::class], version = 12)
abstract class PatternDatabase : RoomDatabase() {
    abstract fun patternDao(): PatternDao
    abstract fun predictedPatternDao(): PredictedPatternDao
    abstract fun invalidatedPatternDao(): InvalidatedPatternDao
    abstract fun patternOutcomeDao(): PatternOutcomeDao
    abstract fun achievementDao(): AchievementDao
    abstract fun learningProfileDao(): LearningProfileDao
    abstract fun advancedLearningDao(): AdvancedLearningDao
    abstract fun trainingExampleDao(): TrainingExampleDao

    companion object {
        @Volatile private var INSTANCE: PatternDatabase? = null
        private const val TAG = "PatternDatabase"
        private var isUsingInMemoryFallback = false

        fun getInstance(context: Context): PatternDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        PatternDatabase::class.java,
                        "PatternMatch.db"
                    )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING) // CRITICAL: Prevents database locked errors (~0.1%)
                        .build()
                    INSTANCE = instance
                    instance
                } catch (e: Exception) {
                    Log.e(TAG, "CRITICAL: Failed to create persistent database (disk full or permissions denied), using in-memory fallback", e)
                    
                    try {
                        val fallbackInstance = Room.inMemoryDatabaseBuilder(
                            context.applicationContext,
                            PatternDatabase::class.java
                        ).build()
                        
                        isUsingInMemoryFallback = true
                        INSTANCE = fallbackInstance
                        
                        Toast.makeText(
                            context.applicationContext,
                            "Warning: Using temporary storage. Data will be lost on app restart. Please free up storage space.",
                            Toast.LENGTH_LONG
                        ).show()
                        
                        Log.w(TAG, "Successfully created in-memory fallback database")
                        fallbackInstance
                    } catch (fallbackError: Exception) {
                        Log.e(TAG, "CRITICAL: Even in-memory database creation failed", fallbackError)
                        throw RuntimeException("Cannot create database: ${fallbackError.message}", fallbackError)
                    }
                }
            }
        }
        
        fun isUsingInMemoryDatabase(): Boolean = isUsingInMemoryFallback

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // CRITICAL: Wrap each ALTER TABLE in try-catch to handle duplicate migrations
                // Prevents crash if migration runs twice (0.1-0.5% of devices)
                try {
                    database.execSQL("ALTER TABLE PatternMatch ADD COLUMN timeframe TEXT NOT NULL DEFAULT 'unknown'")
                } catch (e: Exception) {
                    Log.w(TAG, "Column 'timeframe' may already exist, skipping: ${e.message}")
                }
                try {
                    database.execSQL("ALTER TABLE PatternMatch ADD COLUMN scale REAL NOT NULL DEFAULT 1.0")
                } catch (e: Exception) {
                    Log.w(TAG, "Column 'scale' may already exist, skipping: ${e.message}")
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // CRITICAL: Wrap each ALTER TABLE in try-catch to handle duplicate migrations
                try {
                    database.execSQL("ALTER TABLE PatternMatch ADD COLUMN consensusScore REAL NOT NULL DEFAULT 0.0")
                } catch (e: Exception) {
                    Log.w(TAG, "Column 'consensusScore' may already exist, skipping: ${e.message}")
                }
                try {
                    database.execSQL("ALTER TABLE PatternMatch ADD COLUMN windowMs INTEGER NOT NULL DEFAULT 0")
                } catch (e: Exception) {
                    Log.w(TAG, "Column 'windowMs' may already exist, skipping: ${e.message}")
                }
            }
        }
        
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS PredictedPattern (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        patternName TEXT NOT NULL,
                        completionPercent REAL NOT NULL,
                        confidence REAL NOT NULL,
                        timestamp INTEGER NOT NULL,
                        timeframe TEXT NOT NULL,
                        estimatedCompletion TEXT NOT NULL,
                        stage TEXT NOT NULL,
                        formationVelocity REAL NOT NULL
                    )
                """.trimIndent())
            }
        }
        
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // CRITICAL: Wrap each ALTER TABLE in try-catch to handle duplicate migrations
                try {
                    database.execSQL("ALTER TABLE PatternMatch ADD COLUMN originPath TEXT NOT NULL DEFAULT ''")
                } catch (e: Exception) {
                    Log.w(TAG, "Column 'originPath' may already exist, skipping: ${e.message}")
                }
                try {
                    database.execSQL("ALTER TABLE PatternMatch ADD COLUMN detectionBounds TEXT DEFAULT NULL")
                } catch (e: Exception) {
                    Log.w(TAG, "Column 'detectionBounds' may already exist, skipping: ${e.message}")
                }
            }
        }
        
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS InvalidatedPattern (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        patternName TEXT NOT NULL,
                        previousConfidence REAL NOT NULL,
                        finalConfidence REAL NOT NULL,
                        invalidationReason TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        timeframe TEXT NOT NULL DEFAULT 'unknown'
                    )
                """.trimIndent())
            }
        }
        
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS PatternOutcome (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        patternMatchId INTEGER NOT NULL,
                        patternName TEXT NOT NULL,
                        outcome TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        userFeedback TEXT NOT NULL DEFAULT '',
                        profitLossPercent REAL,
                        timeframe TEXT NOT NULL DEFAULT 'unknown'
                    )
                """.trimIndent())
            }
        }
        
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS achievements (
                        id TEXT PRIMARY KEY NOT NULL,
                        isUnlocked INTEGER NOT NULL DEFAULT 0,
                        unlockedAt INTEGER,
                        progress INTEGER NOT NULL DEFAULT 0,
                        totalRequired INTEGER NOT NULL DEFAULT 1
                    )
                """.trimIndent())
            }
        }
        
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS confidence_profiles (
                        patternType TEXT PRIMARY KEY NOT NULL,
                        bucket0_30WinRate REAL NOT NULL DEFAULT 0.0,
                        bucket30_50WinRate REAL NOT NULL DEFAULT 0.0,
                        bucket50_70WinRate REAL NOT NULL DEFAULT 0.0,
                        bucket70_90WinRate REAL NOT NULL DEFAULT 0.0,
                        bucket90_100WinRate REAL NOT NULL DEFAULT 0.0,
                        bucket0_30Count INTEGER NOT NULL DEFAULT 0,
                        bucket30_50Count INTEGER NOT NULL DEFAULT 0,
                        bucket50_70Count INTEGER NOT NULL DEFAULT 0,
                        bucket70_90Count INTEGER NOT NULL DEFAULT 0,
                        bucket90_100Count INTEGER NOT NULL DEFAULT 0,
                        recommendedThreshold REAL NOT NULL DEFAULT 0.5,
                        totalOutcomes INTEGER NOT NULL DEFAULT 0,
                        lastUpdated INTEGER NOT NULL
                    )
                """.trimIndent())
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS suppression_rules (
                        patternType TEXT PRIMARY KEY NOT NULL,
                        suppressionLevel TEXT NOT NULL,
                        reason TEXT NOT NULL,
                        winRate REAL NOT NULL,
                        totalOutcomes INTEGER NOT NULL,
                        isUserOverridden INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        lastUpdated INTEGER NOT NULL
                    )
                """.trimIndent())
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS learning_metadata (
                        key TEXT PRIMARY KEY NOT NULL,
                        value TEXT NOT NULL,
                        lastUpdated INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
        
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS pattern_correlations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        pattern1 TEXT NOT NULL,
                        pattern2 TEXT NOT NULL,
                        correlation REAL NOT NULL,
                        cooccurrenceCount INTEGER NOT NULL,
                        lastUpdated INTEGER NOT NULL
                    )
                """.trimIndent())
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS pattern_sequences (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        sequencePatterns TEXT NOT NULL,
                        frequency INTEGER NOT NULL,
                        avgSuccessRate REAL NOT NULL,
                        avgTimeSpan INTEGER NOT NULL,
                        lastSeen INTEGER NOT NULL
                    )
                """.trimIndent())
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS market_condition_outcomes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        patternType TEXT NOT NULL,
                        marketCondition TEXT NOT NULL,
                        outcome TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        volatilityLevel TEXT NOT NULL,
                        trendStrength TEXT NOT NULL
                    )
                """.trimIndent())
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS temporal_outcomes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        patternType TEXT NOT NULL,
                        hourOfDay INTEGER NOT NULL,
                        dayOfWeek INTEGER NOT NULL,
                        outcome TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS behavioral_events (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        sessionId TEXT NOT NULL,
                        patternType TEXT NOT NULL,
                        outcome TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        sessionStartTime INTEGER NOT NULL,
                        patternCountInSession INTEGER NOT NULL,
                        timeSinceLastPattern INTEGER NOT NULL,
                        isAfterLoss INTEGER NOT NULL
                    )
                """.trimIndent())
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS strategy_metrics (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        portfolioPatterns TEXT NOT NULL,
                        winRate REAL NOT NULL,
                        sharpeRatio REAL NOT NULL,
                        diversification REAL NOT NULL,
                        sampleSize INTEGER NOT NULL,
                        lastUpdated INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
        
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Scan History table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS scan_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        sessionId TEXT NOT NULL,
                        patternsDetected TEXT NOT NULL,
                        confidences TEXT NOT NULL,
                        timeframe TEXT NOT NULL,
                        scanDurationMs INTEGER NOT NULL,
                        chartHash TEXT NOT NULL
                    )
                """.trimIndent())
                
                // Pattern Frequency table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS pattern_frequency (
                        patternName TEXT PRIMARY KEY NOT NULL,
                        totalScans INTEGER NOT NULL,
                        totalDetections INTEGER NOT NULL,
                        avgConfidence REAL NOT NULL,
                        lastSeen INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Pattern Co-occurrence table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS pattern_cooccurrence (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        pattern1 TEXT NOT NULL,
                        pattern2 TEXT NOT NULL,
                        cooccurrenceCount INTEGER NOT NULL,
                        totalOpportunities INTEGER NOT NULL,
                        cooccurrenceRate REAL NOT NULL,
                        lastUpdated INTEGER NOT NULL
                    )
                """.trimIndent())
                
                Log.i(TAG, "Migration 10 -> 11: Scan Learning Engine tables created successfully")
            }
        }
        
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Training Example table for Incremental Learning Engine
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS TrainingExampleEntity (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        features TEXT NOT NULL,
                        labelDetected TEXT,
                        labelActual TEXT NOT NULL,
                        userConfidence REAL NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
                
                Log.i(TAG, "Migration 11 -> 12: Incremental Learning Engine table created successfully")
            }
        }
    }
    
    suspend fun addTrainingExample(example: com.lamontlabs.quantravision.ml.learning.TrainingExample) {
        val entity = TrainingExampleEntity(
            features = example.features.joinToString(","),
            labelDetected = example.labelDetected,
            labelActual = example.labelActual,
            userConfidence = example.userConfidence,
            timestamp = example.timestamp
        )
        trainingExampleDao().insert(entity)
    }
    
    suspend fun getTrainingExampleCount(): Int {
        return trainingExampleDao().getCount()
    }
    
    suspend fun getAllTrainingExamples(): List<com.lamontlabs.quantravision.ml.learning.TrainingExample> {
        return trainingExampleDao().getAll().map { entity ->
            com.lamontlabs.quantravision.ml.learning.TrainingExample(
                features = entity.features.split(",").map { it.toFloat() }.toFloatArray(),
                labelDetected = entity.labelDetected,
                labelActual = entity.labelActual,
                userConfidence = entity.userConfidence,
                timestamp = entity.timestamp
            )
        }
    }
}
