# Privacy Policy — QuantraVision Overlay  

**Effective Date:** October 2025  
**Maintained by:** Lamont Labs — Privacy Engineering Division  

---

## 1. Core Principle
QuantraVision never transmits, stores, or shares user data.  
All operations occur **entirely on-device**, and no network access is permitted.

---

## 2. Data Collection
**Collected:**  
- None. No personal identifiers, device IDs, or trading data are collected.  

**Stored locally:**  
- Pattern detections (timestamp, pattern name, confidence).  
- Provenance logs (SHA-256 hashes for reproducibility).  
All files are stored in `/files/` under private app sandbox storage and are deletable by the user.

---

## 3. Network Access
- The `INTERNET` permission is **omitted** from the Android Manifest.  
- The `FailClosed` and `IntegrityWatcher` modules prevent runtime network attempts.  
- No telemetry, analytics, or remote update mechanisms exist.

---

## 4. User Control
Users can delete all stored detections and logs by selecting  
**Settings → Clear Local Data** within the app.  
Deletion is immediate and irreversible.

---

## 5. Third-Party Libraries
Only open-source, offline-safe dependencies are used:
- OpenCV (vision processing)
- TensorFlow Lite (local inference)
- Jetpack Compose (UI)
All operate within local sandbox contexts.

---

## 6. Contact
For privacy concerns or data-access requests:  
**Email:** Lamontlabs@proton.me  
**Subject:** Privacy Inquiry — QuantraVision
