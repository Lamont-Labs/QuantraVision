# QuantraVision™ — Deterministic AI Trading Overlay

**Owner:** Jesse J. Lamont / Lamont Labs  
**Version:** 1.1 (2025-10-23)  
**Stage:** Production / Play-Store Ready  

QuantraVision™ is an **on-device deterministic AI overlay** that identifies and highlights chart patterns — triangles, double tops, wedges, RSI divergences — directly on any trading screen.  
It observes only. It never trades, predicts, or connects online.

---

## Core Capabilities
- Offline AI vision (OpenCV + TensorFlow Lite)  
- Deterministic pattern templates (YAML + TFLite)  
- 3–5 highlight events free → Pro unlock required thereafter  
- ProofGate + BundleSigner + LicenseVerifier integrity chain  
- SafeBoot, ThermalGuard, and PowerHint runtime protection  
- Zero-network design (fail-closed privacy)  
- SBOM + manifest + signature export (Pro mode)

---

## Build Instructions
```bash
./gradlew clean assembleRelease
adb install app/build/outputs/apk/release/app-release.apk
```

### Requirements
- Android SDK 34+  
- Kotlin 1.9+  
- Jetpack Compose 1.6+  
- OpenCV 4.8+

---

## Directory Layout
```
/src/main/java/com/lamontlabs/quantravision/   # Kotlin sources
/res/values/                                   # Themes + resources
/assets/legal/                                 # Offline legal bundle
/docs/                                         # Binder + investor documentation
```

---

## Usage Summary
1. Grant overlay + battery optimization exemption  
2. Complete onboarding flow  
3. Open trading app  
4. Press **Start Overlay**  

---

## License Tiers
| Tier | Highlights | Features |
|------|-------------|-----------|
| Free | 5 events total | Overlay visualization only |
| Pro | Unlimited | Full templates, exports, provenance logging |

---

© 2025 Jesse J. Lamont / Lamont Labs  
_All rights reserved — educational use only._
