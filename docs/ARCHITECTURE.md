# QuantraVision Architecture

<div align="center">

**Professional-Grade System Architecture Documentation**

*Version 2.0.0 | Last Updated: November 24, 2025*

[![Architecture](https://img.shields.io/badge/Architecture-Clean%20MVVM-blue?style=flat-square)](https://developer.android.com/jetpack/guide)
[![Design Patterns](https://img.shields.io/badge/Patterns-Repository%20%7C%20UseCase-green?style=flat-square)](#design-patterns)
[![Code Quality](https://img.shields.io/badge/Quality-A+-brightgreen?style=flat-square)](#code-quality)

</div>

---

## 📋 Table of Contents

- [System Overview](#-system-overview)
- [Architecture Layers](#-architecture-layers)
- [Component Diagrams](#-component-diagrams)
- [Detection Pipeline](#-detection-pipeline)
- [AI Learning System](#-ai-learning-system)
- [Data Flow](#-data-flow)
- [Technology Stack](#️-technology-stack)
- [Design Patterns](#-key-design-patterns)
- [Performance Optimizations](#-performance-optimizations)
- [Security Architecture](#-security-architecture)

---

## 🏗️ System Overview

QuantraVision is built on a **Clean Architecture** foundation with clear separation of concerns, testability, and maintainability as core principles.

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         PRESENTATION LAYER                          │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌─────────────┐  │
│  │  Screens   │  │ ViewModels │  │ Components │  │   Theme     │  │
│  │ (Compose)  │  │  (MVVM)    │  │  (UI Kit)  │  │ (Material3) │  │
│  └────────────┘  └────────────┘  └────────────┘  └─────────────┘  │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                                ↓
┌─────────────────────────────────────────────────────────────────────┐
│                          DOMAIN LAYER                               │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌─────────────┐  │
│  │  UseCases  │  │ Repository │  │  Entities  │  │   Models    │  │
│  │ (Business) │  │ Interfaces │  │            │  │             │  │
│  └────────────┘  └────────────┘  └────────────┘  └─────────────┘  │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                                ↓
┌─────────────────────────────────────────────────────────────────────┐
│                           DATA LAYER                                │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌─────────────┐  │
│  │  Room DB   │  │    DAO     │  │ Preferences│  │   OpenCV    │  │
│  │ (Patterns) │  │  (Access)  │  │  (Config)  │  │  (Detection)│  │
│  └────────────┘  └────────────┘  └────────────┘  └─────────────┘  │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌─────────────┐  │
│  │ TFLite ML  │  │   Assets   │  │   Billing  │  │   Camera    │  │
│  │ (Learning) │  │ (Templates)│  │  (Google)  │  │  (CameraX)  │  │
│  └────────────┘  └────────────┘  └────────────┘  └─────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

## 🏗️ Layer Breakdown

### 1. User Interface Layer (Compose)

**Technology**: Jetpack Compose + Material 3

**Components**:
- `DashboardScreen` - Main navigation hub
- `DetectionScreen` - Real-time pattern detection
- `AnalyticsScreen` - Performance insights
- `EducationScreen` - Interactive lessons
- `SettingsScreen` - Configuration

**Key Features**:
- Declarative UI with reactive state
- Dark theme optimized (#0A1218 background)
- Responsive layouts for various screen sizes
- Material 3 design system

---

### 2. ViewModel Layer

**Technology**: Android Architecture Components

**Responsibilities**:
- State management (UI state)
- Business logic coordination
- Lifecycle awareness
- Asynchronous operations (Coroutines)

**Example**:
```kotlin
class DetectionViewModel(
    private val detector: PatternDetector,
    private val analytics: AnalyticsTracker
) : ViewModel() {
    
    private val _detections = MutableStateFlow<List<Detection>>(emptyList())
    val detections: StateFlow<List<Detection>> = _detections.asStateFlow()
    
    fun analyzeImage(image: ImageProxy) {
        viewModelScope.launch {
            val results = detector.analyze(image)
            _detections.value = results
            analytics.track(results)
        }
    }
}
```

---

### 3. Repository Layer

**Technology**: Kotlin Coroutines + Flow

**Repositories**:
- `PatternRepository` - Detection data
- `UserRepository` - User stats & achievements
- `EducationRepository` - Lesson progress
- `AnalyticsRepository` - Performance metrics

**Responsibilities**:
- Abstract data sources
- Coordinate multiple data sources
- Cache management
- Error handling

---

### 4. Data Layer

#### Room Database

**Entities**:
- `Detection` - Pattern detection records
- `UserStats` - Usage statistics
- `Achievement` - Unlocked achievements
- `LessonProgress` - Education completion
- `PerformanceMetric` - Pattern accuracy

**DAOs**:
```kotlin
@Dao
interface DetectionDao {
    @Query("SELECT * FROM detections ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentDetections(limit: Int): Flow<List<Detection>>
    
    @Insert
    suspend fun insert(detection: Detection)
    
    @Query("DELETE FROM detections WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}
```

#### TensorFlow Lite Engine

**Purpose**: On-device AI pattern prediction

**Models**:
- `pattern_classifier.tflite` - Pattern recognition
- `formation_predictor.tflite` - Early detection

**Processing Pipeline**:
1. Image capture (CameraX)
2. Preprocessing (resize, normalize)
3. Model inference (TFLite)
4. Post-processing (NMS, filtering)
5. Result delivery

#### OpenCV Processing

**Purpose**: Computer vision operations

**Operations**:
- Template matching (pattern detection)
- Multi-scale pyramid (timeframe agnostic)
- Edge detection (chart boundary)
- Color analysis (candle classification)

---

## 🔄 Data Flow

### Pattern Detection Flow

```
User captures chart
       ↓
CameraX provides ImageProxy
       ↓
ScaleSpace generates multi-scale pyramid
       ↓
PatternDetector analyzes each scale
       ↓
ConfidenceCalibrator scores detections
       ↓
PredictionEngine estimates completion
       ↓
Results displayed + stored in Room
       ↓
Analytics updates performance metrics
```

### Education Flow

```
User selects lesson
       ↓
LessonRepository loads content
       ↓
UI displays lesson material
       ↓
User completes quiz
       ↓
GamificationEngine checks achievements
       ↓
Progress saved to Room
       ↓
Certificate generated (if applicable)
```

---

## 🎯 Key Design Patterns

### 1. Repository Pattern
- Abstraction over data sources
- Single source of truth
- Testability

### 2. Observer Pattern
- Reactive UI updates (Flow)
- State management
- Event broadcasting

### 3. Factory Pattern
- ViewModel creation
- Dependency injection
- Configuration objects

### 4. Strategy Pattern
- Detection algorithms
- Scoring strategies
- Export formats

---

## 🔌 Dependency Injection

**No DI Framework** - Manual injection for simplicity

**Example**:
```kotlin
class AppContainer(context: Context) {
    private val database = Database.getInstance(context)
    
    val detector = PatternDetector(
        context = context,
        config = DetectionConfig.default()
    )
    
    val detectionRepository = PatternRepository(
        dao = database.detectionDao(),
        detector = detector
    )
    
    fun makeDetectionViewModel(): DetectionViewModel {
        return DetectionViewModel(
            detector = detector,
            analytics = AnalyticsTracker(database.metricsDao())
        )
    }
}
```

---

## 🔒 Security Architecture

### Data Protection
- **Local Storage**: All data stored on-device (Room)
- **No Network**: Zero external communication
- **Encryption**: Sensitive data encrypted (AndroidX Security)

### Licensing
- **Offline Validation**: License check without server
- **Google Play Billing**: IAP for Standard/Pro tiers
- **Grace Period**: 72-hour validation cache

---

## ⚡ Performance Optimizations

### 1. Lazy Loading
- Patterns loaded on-demand
- Database queries paginated
- Images loaded asynchronously

### 2. Caching
- Template cache (OpenCV)
- Model cache (TFLite)
- UI component cache

### 3. Threading
- `Dispatchers.IO` for file operations
- `Dispatchers.Default` for CPU-intensive tasks
- `Dispatchers.Main` for UI updates

### 4. Memory Management
- Bitmap recycling
- Mat release (OpenCV)
- ViewModel lifecycle awareness

---

## 📦 Module Structure

```
app/
├── detection/          # Pattern detection engine
│   ├── Detector.kt
│   ├── ScaleSpace.kt
│   └── ConfidenceCalibrator.kt
├── prediction/         # Predictive intelligence
│   ├── FormationTracker.kt
│   └── CompletionEstimator.kt
├── analytics/          # Performance tracking
│   ├── PatternPerformance.kt
│   └── HotPatternTracker.kt
├── gamification/       # Achievement system
│   ├── AchievementEngine.kt
│   └── StreakTracker.kt
├── education/          # Learning system
│   ├── LessonManager.kt
│   └── QuizEngine.kt
├── billing/            # In-app purchases
│   ├── BillingManager.kt
│   └── Entitlements.kt
└── ui/                 # Compose screens
    ├── DashboardScreen.kt
    ├── DetectionScreen.kt
    └── ...
```

---

## 🔧 Configuration

**Key Configuration Files**:
- `DetectionConfig` - Pattern detection parameters
- `ScaleConfig` - Multi-scale settings
- `GameConfig` - Achievement thresholds
- `LicenseConfig` - Tier definitions

**Example**:
```kotlin
data class DetectionConfig(
    val minConfidence: Double = 0.70,
    val maxDetectionsPerFrame: Int = 10,
    val temporalStabilityFrames: Int = 3,
    val enablePredictiveMode: Boolean = true
)
```

---

## 📊 Analytics & Telemetry

**Privacy First**: Zero telemetry to external servers

**Local Analytics**:
- Detection success rate
- Pattern frequency
- User engagement metrics
- Performance benchmarks

**Purpose**: 
- Improve user experience
- Identify popular features
- Performance optimization

---

## 🧪 Testing Strategy

### Unit Tests
- Business logic
- Repository layer
- ViewModels
- Utility functions

### Instrumented Tests
- Database operations
- UI components
- Integration tests

### Manual Testing
- Pattern detection accuracy
- UI responsiveness
- Edge cases

---

## 🧠 Apex Intelligence System

**Status**: Production implementation complete (Batch 10)

The Apex Intelligence System provides deterministic, rule-based pattern validation with optional cloud-enhanced explanations.

### Apex Protocol Architecture

**109 Total Protocols** organized in three tiers:

1. **Omega Protocols (25)** - Safety and integrity guardrails
2. **Tier Protocols (60)** - Pattern detection and validation rules
3. **Learning Protocols (24)** - Adaptive learning and suppression memory

### System Diagram

```
User captures chart screenshot
       ↓
ApexEngineMobile orchestrates detection
       ↓
┌─────────────────────────────────────┐
│   OMEGA PROTOCOLS (Safety Layer)   │
│  - Health checks                    │
│  - Quota validation (QuotaGate)     │
│  - Permission verification          │
│  - Integrity validation             │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│   TIER PROTOCOLS (Detection Layer)  │
│  - Pattern matching (109 templates) │
│  - Confidence scoring               │
│  - Entropy detection                │
│  - Protocol aggregation             │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ LEARNING PROTOCOLS (Adaptive Layer) │
│  - Suppression memory               │
│  - Pattern invalidation tracking    │
│  - Drift detection                  │
│  - Real-time accuracy feedback      │
└──────────────┬──────────────────────┘
               ↓
QuantraScore (0-100) + pattern metadata
       ↓
┌─────────────────────────────────────┐
│    EXPLANATION LAYER (Optional)     │
│  Local: Template-based (<1 second)  │
│  Cloud: LLM-powered (10-30 seconds) │
│    via CloudReasoner + QuotaGate    │
└──────────────┬──────────────────────┘
               ↓
Result displayed to user + audit trail logged
```

### Quota Management System (QuotaGate)

**Purpose**: Fail-closed cloud API rate limiting

**Tier Limits**:
- FREE: 0 cloud narrations/day (100% offline)
- STARTER/PRO: 10 cloud narrations/day
- STANDARD/ULTRA: 25 cloud narrations/day

**Safety Features**:
- Fail-closed logic: Deny if quota check fails
- Min 8 seconds between API calls
- Max 3 calls per 60 seconds rolling window
- Persistent quota storage with daily reset

**Implementation**: `QuotaGate.kt`

### Cloud Narration Pipeline

**Architecture**:

```
User requests explanation
       ↓
QuotaGate validates quota (fail-closed)
       ↓
ApexEngineMobile generates Apex packet
  - QuantraScore
  - Protocol trace
  - Entropy metrics
  - Detection metadata
  (NO screenshots, NO chart data)
       ↓
CloudReasoner sends to OpenAI API
  - Structured prompt template
  - JSON schema enforcement
  - 15 second timeout
       ↓
LLMContractValidator validates response
  - Forbidden term detection (buy, sell, etc.)
  - Schema compliance check
  - Fail-closed: Reject if invalid
       ↓
If valid: Display cloud narration
If invalid: Fallback to LocalSummaryGenerator
       ↓
Explanation displayed + usage logged
```

**Components**:
- **CloudReasoner.kt**: OpenAI API integration
- **LLMContractValidator.kt**: Response validation
- **LocalSummaryGenerator.kt**: Offline fallback
- **AutoExplainManager.kt**: Orchestration logic

### Performance Guardrails (ScanThrottler)

**Purpose**: Prevent battery drain and thermal throttling

**FPS Limits**:
- Default: 2-4 FPS for continuous scanning
- Adaptive throttling based on battery/thermal state
- Minimum 250ms between scans

**PowerGuard Integration**:
- Battery level monitoring
- Thermal state detection
- Automatic scan frequency reduction
- User notification when throttling active

**Implementation**: `ScanThrottler.kt`

### Integrity Verification System

**Components**:

1. **ProofHasher** (`ProofHasher.kt`)
   - SHA-256 hash of all scan results
   - Tamper-evident audit trail
   - Deterministic proof generation

2. **IntegrityChecker** (`IntegrityChecker.kt`)
   - APK signature validation
   - Runtime integrity checks
   - Anti-tampering detection

3. **DetectionAuditTrail** (Room database)
   - Complete provenance logging
   - Hash verification chain
   - Forensic replay capability

---

## 🧪 Testing Strategy

**Test Coverage**: 120+ unit tests with 80%+ coverage

### Test Organization

**Unit Tests** (`app/src/test/`):
- `ApexEngineMobileTest.kt` - Protocol execution
- `QuotaGateTest.kt` - Quota validation logic
- `CloudReasonerTest.kt` - API integration (mocked)
- `LLMContractValidatorTest.kt` - Response validation
- `LocalSummaryGeneratorTest.kt` - Offline fallback
- `ProtocolRegistryMobileTest.kt` - Protocol management

**Instrumentation Tests** (`app/src/androidTest/`):
- `PerformanceBenchmarkTest.kt` - Detection latency benchmarks
- `QuotaGatePersistenceTest.kt` - Database integration
- End-to-end detection flow testing

### CI/CD Integration

**GitHub Actions** (`.github/workflows/ci.yml`):
- Automated build on every push
- Full test suite execution
- Lint checks (Kotlin style)
- Build APK artifacts

**Coverage Reports**:
- JaCoCo coverage reports generated
- 80%+ coverage target for business logic
- Coverage uploaded to GitHub Actions artifacts

---

## 🚀 Future Architecture

### Planned Enhancements
- **Geometric Detection**: ML-based pattern detection (70-85% accuracy target)
- **On-Device LLM**: Gemma 2B/Phi-2 integration for offline explanations
- **Modularization**: Multi-module Gradle setup
- **Dependency Injection**: Hilt integration for scalability
- **Cloud Sync**: Optional pattern history backup (Pro tier)

---

## 📚 Further Reading

- [Jetpack Compose Architecture](https://developer.android.com/jetpack/compose/architecture)
- [Android App Architecture Guide](https://developer.android.com/topic/architecture)
- [TensorFlow Lite Best Practices](https://www.tensorflow.org/lite/performance/best_practices)
- [OpenCV Android Guide](https://docs.opencv.org/master/d5/df8/tutorial_dev_with_OCV_on_Android.html)
