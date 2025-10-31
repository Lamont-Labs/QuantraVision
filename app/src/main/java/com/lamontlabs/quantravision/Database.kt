package com.lamontlabs.quantravision

import android.content.Context
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

@Database(entities = [PatternMatch::class, PredictedPattern::class], version = 5)
abstract class PatternDatabase : RoomDatabase() {
    abstract fun patternDao(): PatternDao
    abstract fun predictedPatternDao(): PredictedPatternDao

    companion object {
        @Volatile private var INSTANCE: PatternDatabase? = null

        fun getInstance(context: Context): PatternDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PatternDatabase::class.java,
                    "PatternMatch.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE PatternMatch ADD COLUMN timeframe TEXT NOT NULL DEFAULT 'unknown'")
                database.execSQL("ALTER TABLE PatternMatch ADD COLUMN scale REAL NOT NULL DEFAULT 1.0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE PatternMatch ADD COLUMN consensusScore REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE PatternMatch ADD COLUMN windowMs INTEGER NOT NULL DEFAULT 0")
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
                database.execSQL("ALTER TABLE PatternMatch ADD COLUMN originPath TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE PatternMatch ADD COLUMN detectionBounds TEXT DEFAULT NULL")
            }
        }
    }
}
