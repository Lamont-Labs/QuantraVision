# QuantraVision™ — User Guide

## 1 · Setup
1. Install from Play Store or sideload .APK.  
2. Grant overlay permission and disable battery optimization.  
3. Complete onboarding flow and accept disclaimer.  

---

## 2 · Using the Overlay
- Open any trading chart.  
- Press **Start Overlay**.  
- Patterns highlight in real time with confidence %.  
- Free tier = 3–5 total highlights.  
- After quota, Upgrade Prompt appears.  

---

## 3 · Upgrade to Pro
- Tap “Upgrade to Pro.”  
- Complete Play Billing purchase or apply offline license.  
- Unlocks full pattern library + proof exports + tuning tools.  

---

## 4 · Proof Export (Pro)
Exports deterministic bundle to:  
```
/files/export_bundle/
 ├─ manifest.json
 ├─ signature.txt
 └─ sbom.json
```
Each export includes SHA-256 hashes verifiable via `verify.sh`.

---

## 5 · Settings
- Reset highlight quota  
- Change language  
- View Privacy Policy / Terms / Disclaimer  
- Export or restore backups  

---

## 6 · Troubleshooting
| Issue | Fix |
|--------|-----|
| Overlay not visible | Re-grant overlay permission |
| Overlay stops | Quota exhausted → upgrade |
| Device hot | ThermalGuard throttling active |
| Crash on launch | Resume via CrashRecoveryDialog |

---

## 7 · Support
Email: Lamontlabs@proton.me  
Docs: /assets/legal/  
© 2025 Jesse J. Lamont / Lamont Labs
