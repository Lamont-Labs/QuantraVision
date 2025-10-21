# QuantraVision Overlay — v1.0-seed  
**Owner:** Jesse J. Lamont / Lamont Labs  
**Package:** com.lamontlabs.quantravision  

### Summary
QuantraVision Overlay is an on-device AI visual assistant for traders.  
It highlights known chart patterns directly over any trading platform window,  
entirely offline, never transmitting or storing private data externally.  
It functions as the “eyes” of QuantraCore, proving deterministic detection.

### Features
- Offline OpenCV + TensorFlow Lite pattern recognition  
- Semi-transparent overlay renderer (MediaProjection)  
- Pattern provenance logs (SHA-256 hashing + Room DB)  
- Deterministic template library (YAML / TFLite)  
- Greyline OS binder export pipeline  

### Build Requirements
- Android Studio Giraffe or newer  
- Kotlin 1.9 + Compose 1.6  
- OpenCV SDK 4.x  
- Target SDK 34  |  Min SDK 26  

### Quick Start
1. Clone repo  
2. Open `app/` module in Android Studio  
3. Sync Gradle and run on emulator or device  
4. In app settings, enable overlay permission  
5. Load sample charts from `assets/demo_charts/`  

### Security & Privacy
No network access. All processing local. Logs are hashed and stored in Room DB.  
Deterministic replay guarantees same input → same output.

### License
Apache 2.0 — see LICENSE.
