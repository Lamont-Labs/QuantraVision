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
    val consensusScore: Double,   // NEW: consensus across scales
    val windowMs: Long            // NEW: temporal stability window contribution
)

@Dao
interface PatternDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(match: PatternMatch)

    @Query("SELECT * FROM PatternMatch ORDER BY timestamp DESC")
    suspend fun getAll(): List<PatternMatch>
}

@Database(entities = [PatternMatch::class], version = 3)
abstract class PatternDatabase : RoomDatabase() {
    abstract fun patternDao(): PatternDao

    companion object {
        @Volatile private var INSTANCE: PatternDatabase? = null

        fun getInstance(context: Context): PatternDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PatternDatabase::class.java,
                    "PatternMatch.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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
    }
}
