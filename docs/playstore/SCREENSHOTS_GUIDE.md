# Google Play Store Screenshots Guide

**For QuantraVision Production Release**

---

## Required Screenshots (Real Device Captures)

### **Phone Screenshots (Required)**
- **Count:** 4-8 screenshots
- **Dimensions:** 1080 x 1920 px (9:16 ratio) or device native resolution
- **Format:** PNG or JPEG

### **Tablet Screenshots (Recommended)**
- **Count:** 4-8 screenshots  
- **Dimensions:** 1920 x 1200 px (16:10 ratio) or device native resolution
- **Format:** PNG or JPEG

---

## Screenshot Content Recommendations

### **Screenshot 1: Hero/Overview**
**What to show:**
- Dashboard screen with key metrics
- "QuantraVision - AI Pattern Detection" title overlay
- Tagline: "See patterns your platform can't"

**Annotations:**
- ✅ 108 Patterns Detected
- ✅ 100% Offline & Private
- ✅ No Subscriptions

---

### **Screenshot 2: Real-Time Detection**
**What to show:**
- Live overlay on a trading chart
- Multiple patterns highlighted
- Confidence scores visible
- Pattern names labeled

**Annotations:**
- "Real-time pattern detection"
- "Head & Shoulders - 92% confidence"
- "Triangle - 87% confidence"

---

### **Screenshot 3: Pattern Library**
**What to show:**
- Pattern catalog screen
- Categories: Reversal, Continuation, Harmonic
- Visual thumbnails of patterns

**Annotations:**
- "115 Professional Patterns"
- "6 ML-Detected + 109 Template-Matched"

---

### **Screenshot 4: Education System**
**What to show:**
- Education screen with lesson modules
- Progress bars
- Achievement badges

**Annotations:**
- "25 Interactive Lessons"
- "Learn Technical Analysis"
- "Track Your Progress"

---

### **Screenshot 5: Analytics Dashboard**
**What to show:**
- Detection history
- Accuracy metrics
- Pattern frequency charts

**Annotations:**
- "Track Detection History"
- "Measure Accuracy"
- "Analyze Performance"

---

### **Screenshot 6: Pricing/Plans**
**What to show:**
- Pricing comparison table
- Free vs Standard vs Pro
- Clear feature breakdown

**Annotations:**
- "Free: 10 patterns, 3 highlights/day"
- "Standard $14.99: 30 patterns, unlimited"
- "Pro $29.99: All 108 patterns + intelligence"

---

### **Screenshot 7: Unique Features**
**What to show:**
- Regime Navigator
- Pattern-to-Plan Engine
- Behavioral Guardrails
- Proof Capsules

**Annotations:**
- "Exclusive Intelligence Stack"
- "Know WHEN patterns matter"
- "Trade scenarios with entry/exit"

---

### **Screenshot 8: Privacy/Offline**
**What to show:**
- Settings screen with privacy toggles
- "No Network Permission" indicator
- Offline badge

**Annotations:**
- "100% Offline Processing"
- "Zero Data Collection"
- "Your Charts Stay Private"

---

## Best Practices

### Do's:
✅ Use **real device screenshots** (not emulator)  
✅ Show **actual app screens** (not mockups)  
✅ Add **text overlays** highlighting key features  
✅ Use **high resolution** images  
✅ Keep text **readable** even on small screens  
✅ Show the app in **use** (patterns detected, lessons open, etc.)

### Don'ts:
❌ Don't use placeholder/fake data  
❌ Don't show error states  
❌ Don't include device frames (Google adds them automatically)  
❌ Don't use copyrighted chart images  
❌ Don't show personal information  
❌ Don't make misleading claims

---

## Feature Graphic (1024 x 500 px)

**Current:** Auto-generated placeholder in `dist/playstore/feature_graphic.png`

**Recommended:** Professional design featuring:
- QuantraVision logo
- Tagline: "AI Pattern Detection for Traders"
- Key visual: Chart with pattern overlays
- Accent color: #00E5FF (cyan)
- Background: #0A1218 (dark)

---

## App Preview Video (Optional but Recommended)

**Duration:** 30 seconds - 2 minutes  
**Aspect Ratio:** 16:9  
**Format:** MPEG-4 or WebM

**Content:**
1. App opening (Dashboard)
2. Real-time detection demo (5-10 seconds)
3. Pattern library scroll
4. Education lesson preview
5. Analytics screen
6. Pricing screen
7. Call to action: "Download QuantraVision"

**Voiceover Script:**
```
"Introducing QuantraVision - professional AI pattern detection for retail traders.

See 108 chart patterns in real-time, powered by hybrid YOLOv8 machine learning and OpenCV computer vision.

100% offline. Zero data collection. No subscriptions.

Learn with 25 interactive lessons. Track your accuracy. Make better-informed decisions.

Free tier: 10 essential patterns.
Standard: 30 core patterns.
Pro: All 108 patterns plus exclusive intelligence features.

Download QuantraVision today. Your charts. Your privacy. Your edge."
```

---

## How to Capture Screenshots

### Option 1: Android Studio Emulator
1. Run app on emulator
2. Navigate to screen
3. Use Screenshot tool (camera icon)
4. Save to `dist/playstore/screenshots/`

### Option 2: Real Device (Recommended)
1. Enable Developer Options
2. Run app on physical device
3. Take screenshot (Power + Volume Down)
4. Transfer via USB or ADB: `adb pull /sdcard/Pictures/Screenshots/`
5. Save to `dist/playstore/screenshots/`

### Option 3: Scripted Automation
```bash
# Use Android UI Automator to navigate and capture
adb shell am start com.lamontlabs.quantravision/.MainActivity
sleep 2
adb shell screencap -p /sdcard/screenshot1.png
adb pull /sdcard/screenshot1.png dist/playstore/screenshots/
```

---

## File Organization

```
dist/playstore/
├── screenshots/
│   ├── phone/
│   │   ├── 01_hero.png
│   │   ├── 02_detection.png
│   │   ├── 03_patterns.png
│   │   ├── 04_education.png
│   │   ├── 05_analytics.png
│   │   ├── 06_pricing.png
│   │   ├── 07_intelligence.png
│   │   └── 08_privacy.png
│   └── tablet/
│       └── (same structure)
├── feature_graphic.png
└── app_icon_512.png
```

---

## Next Steps

1. **Build APK:** `./gradlew assembleDebug`
2. **Install on device:** `adb install app/build/outputs/apk/debug/app-debug.apk`
3. **Navigate screens:** Manually or with UI Automator
4. **Capture screenshots:** Use device screenshot function
5. **Transfer files:** `adb pull` or USB
6. **Optimize images:** Use ImageOptim or similar
7. **Add text overlays:** Use Photoshop, GIMP, or Canva
8. **Upload to Play Console:** In "Store listing" section

---

**Current Status:**  
✅ Placeholder screenshots generated  
⚠️ Real device screenshots needed for production release
