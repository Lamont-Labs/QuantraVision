# Room Database Schemas

This directory contains auto-generated JSON schema files for Room database migrations. These files are used to validate database migrations and ensure schema evolution is correct.

## ⚠️ Schema Generation

**Note:** Schema JSON files are **not present in this repository** because this project is developed in Replit (web-based IDE without Android SDK). Schemas are auto-generated when building with Android Studio/Gradle on a local development machine.

### Why Schemas Are Important

Room schema exports provide:
- ✅ **Migration validation** - Verify migrations are correct between database versions
- ✅ **Schema evolution history** - Track how database structure changes over time  
- ✅ **Testing infrastructure** - Enable migration testing in instrumented tests
- ✅ **Documentation** - JSON representation of each database version's structure

### How to Generate Schemas

When you build this project on a **local machine with Android Studio**:

1. **Automatic Generation** (Recommended)
   ```bash
   ./gradlew assembleDebug
   # Schemas auto-generated in app/schemas/
   ```

2. **Schemas Location**
   - Version 1: `app/schemas/1.json`
   - Version 2: `app/schemas/2.json`
   - ...
   - Version 11: `app/schemas/11.json` (current)

3. **Gradle Configuration** (already set up in `app/build.gradle.kts`)
   ```kotlin
   ksp {
       arg("room.schemaLocation", "$projectDir/schemas")
       arg("room.incremental", "true")
       arg("room.expandProjection", "true")
   }
   ```

### Current Database Version

**Version 11** (as of November 2, 2025)

**Entities (17 total):**
- PatternMatch, PredictedPattern, InvalidatedPattern
- PatternOutcome, AchievementEntity
- ConfidenceProfile, SuppressionRule, LearningMetadata
- PatternCorrelationEntity, PatternSequenceEntity
- MarketConditionOutcomeEntity, TemporalDataEntity
- BehavioralEventEntity, StrategyMetricsEntity
- ScanHistoryEntity, PatternFrequencyEntity, PatternCooccurrenceEntity

**Migration Scripts:**
- MIGRATION_1_2 through MIGRATION_10_11 (9 migrations total)
- See `Database.kt` for implementation details

### Production Impact

**No production impact:** Schemas are development/testing tools only. The app's database migrations are defined in code (`Database.kt`) and work correctly without schema JSON files.

### Local Development Workflow

If you're developing on Android Studio:

1. Build the project once: `./gradlew assembleDebug`
2. Commit generated schemas: `git add app/schemas/*.json`
3. Use schemas for migration testing in `androidTest/` folder

---

**TL;DR:** Schemas auto-generate when building with Android Studio. Not needed in Replit. Production-ready migrations are already in `Database.kt`.
