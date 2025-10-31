package com.lamontlabs.quantravision

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase

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
)

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

@Database(entities = [PatternMatch::class, PredictedPattern::class, InvalidatedPattern::class], version = 6)
abstract class PatternDatabase : RoomDatabase() {
    abstract fun patternDao(): PatternDao
    abstract fun predictedPatternDao(): PredictedPatternDao
    abstract fun invalidatedPatternDao(): InvalidatedPatternDao

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
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
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
    }
}
