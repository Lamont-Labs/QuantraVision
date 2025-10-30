# QuantraVision Architecture

This document provides a high-level overview of the QuantraVision application architecture.

## 📐 Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        User Interface                        │
│                    (Jetpack Compose)                         │
├─────────────────────────────────────────────────────────────┤
│                        ViewModels                            │
│                  (State Management)                          │
├─────────────────────────────────────────────────────────────┤
│                     Repository Layer                         │
│              (Data Access Abstraction)                       │
├──────────────┬──────────────┬──────────────┬────────────────┤
│   Room DB    │  TensorFlow  │   OpenCV     │   Billing      │
│  (Storage)   │  (AI Engine) │ (CV Processing)│ (IAP)        │
└──────────────┴──────────────┴──────────────┴────────────────┘
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

## 🚀 Future Architecture

### Planned Enhancements
- **Modularization**: Multi-module Gradle setup
- **Dependency Injection**: Hilt/Koin integration
- **Cloud Sync**: Optional cloud backup (Pro tier)
- **Wear OS**: Companion app for smartwatches

---

## 📚 Further Reading

- [Jetpack Compose Architecture](https://developer.android.com/jetpack/compose/architecture)
- [Android App Architecture Guide](https://developer.android.com/topic/architecture)
- [TensorFlow Lite Best Practices](https://www.tensorflow.org/lite/performance/best_practices)
- [OpenCV Android Guide](https://docs.opencv.org/master/d5/df8/tutorial_dev_with_OCV_on_Android.html)
