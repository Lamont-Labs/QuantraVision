# QuantraVision Demo Verification Guide

## Overview

This guide provides step-by-step instructions for verifying the QuantraVision Apex™ build and testing core functionality. Use this after building the APK to ensure all systems are operational.

---

## Prerequisites

### Required Tools
- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: Version 17
- **Gradle**: 8.7+ (included via wrapper)
- **Test Device**: Physical Android device (API 28+) or emulator

### Repository Setup
```bash
# Clone repository
git clone https://github.com/yourusername/quantravision.git
cd quantravision

# Verify project structure
bash scripts/validate-project.sh
```

---

## Build Verification

### 1. GitHub Actions CI (Automated)

The CI pipeline runs automatically on push/PR to `main` or `develop` branches.

**Pipeline Steps:**
1. Checkout code
2. Set up JDK 17
3. Cache Gradle packages
4. Run lint
5. Run unit tests
6. Build debug APK
7. Upload APK artifact

**Verify CI Status:**
- Navigate to **Actions** tab in GitHub repository
- Check latest workflow run status
- Download APK artifact from successful builds

### 2. Local Build (Manual)

```bash
# Grant execute permission
chmod +x gradlew

# Clean build
./gradlew clean

# Run lint
./gradlew lint

# Run unit tests
./gradlew test

# Build debug APK
./gradlew assembleDebug

# Locate APK
ls -lh app/build/outputs/apk/debug/
```

**Expected Output:**
```
app-debug.apk (50-70 MB)
```

---

## Unit Test Verification

### Run All Tests

```bash
./gradlew test --info
```

### Test Coverage

**Batch 10 Test Suites (120+ tests):**

1. **QuotaGateTest** (40+ tests)
   - Tier limits: FREE=0, PRO=10, ULTRA=25 calls/day
   - Rate limiting: 8s minimum, max 3 per 60s
   - Daily reset at midnight
   - State persistence across restarts

2. **LLMContractValidatorTest** (35+ tests)
   - Forbidden words detection (13 words/phrases)
   - JSON schema validation (10 required fields)
   - Status echo verification
   - Token limits: PRO=180, ULTRA=380

3. **LocalSummaryGeneratorTest** (25+ tests)
   - Golden tests for all 5 statuses (PASS, WAIT, FAIL, SUPPRESSED, OMEGA)
   - Universal header validation
   - Template variable substitution
   - Deterministic output

4. **VerdictMappingTest** (20+ tests)
   - Protocol execution order: Omega → Tier → Learning
   - Verdict aggregation logic
   - QuantraScore calculation
   - Proof hash determinism

### Expected Results

```
BUILD SUCCESSFUL
Total tests: 120+
Passed: 120+
Failed: 0
Skipped: 0
```

---

## Functional Testing

### 1. Install APK on Device

```bash
# Using ADB
adb install app/build/outputs/apk/debug/app-debug.apk

# Or drag-and-drop in Android Studio
```

### 2. First Launch Checklist

**Initial Setup:**
- [ ] App launches without crash
- [ ] Splash screen displays
- [ ] Privacy disclaimer shows
- [ ] Terms acceptance UI appears
- [ ] Home screen loads

**UI Navigation:**
- [ ] Bottom navigation works (5 tabs: Home, Markets, Scan, QuantraBot, Settings)
- [ ] Material 3 dark theme renders correctly
- [ ] Chrome/steel metallic brand colors visible
- [ ] No UI layout issues or overlaps

### 3. Core Feature Testing

#### A. Screen Capture Permission

**Steps:**
1. Navigate to **Scan** tab
2. Tap **Enable Overlay** button
3. Grant MediaProjection permission

**Expected:**
- [ ] Permission dialog appears
- [ ] Overlay service starts
- [ ] Toast confirmation shows
- [ ] No crash or error

#### B. Chart Pattern Scanning (Local Mode - FREE Tier)

**Setup:**
1. Open any trading app (TradingView, Webull, etc.)
2. Display candlestick chart (any timeframe)
3. Return to QuantraVision
4. Enable overlay if not active

**Scan Test:**
1. Tap chart overlay icon
2. Observe scan trigger

**Expected:**
- [ ] Overlay appears at ~3 FPS (2-4 FPS target)
- [ ] ScanThrottler enforces 333ms interval
- [ ] LocalSummaryGenerator produces explanation
- [ ] No cloud call attempted (FREE tier)
- [ ] Status shows: PASS, WAIT, FAIL, SUPPRESSED, or OMEGA
- [ ] QuantraScore displayed (0-100)

**Throttle Test:**
1. Rapidly tap overlay multiple times (< 333ms interval)

**Expected:**
- [ ] Toast shows "Scan throttled, please wait"
- [ ] Frame rate stays 2-4 FPS
- [ ] No excessive CPU usage

#### C. Quota Gate Verification (Paid Tier Simulation)

**Note:** Requires tier upgrade (not FREE) to test cloud features.

**PRO Tier Test:**
1. Simulate PRO tier via Settings (if debug mode available)
2. Perform 10 scans with WAIT status (confidence ≥55%)

**Expected:**
- [ ] First 10 scans trigger CloudReasoner
- [ ] QuotaGate allows calls 1-10
- [ ] Call 11 blocked by quota gate
- [ ] LocalSummaryGenerator fallback on call 11
- [ ] Overlay dims to 50% opacity after quota exhausted

**Rate Limit Test:**
1. Trigger 3 scans rapidly (< 8 seconds apart)

**Expected:**
- [ ] First scan succeeds
- [ ] Scans 2-3 blocked by rate limiter (8s minimum)
- [ ] 4th scan after 60s reset window succeeds

### 4. Crash Hardening Validation

**MediaProjection Edge Cases:**
1. Enable overlay
2. Lock device screen
3. Unlock device
4. Trigger scan

**Expected:**
- [ ] No crash on screen lock/unlock
- [ ] LiveOverlayController recovers gracefully
- [ ] Error logs show recovery attempts

**Memory Pressure:**
1. Open multiple apps (create memory pressure)
2. Return to QuantraVision
3. Trigger scan

**Expected:**
- [ ] OutOfMemoryError handled in SingleFrameCapture
- [ ] Bitmap conversion fails gracefully
- [ ] User sees error toast, app doesn't crash

### 5. Performance Validation

**Frame Rate Monitoring:**
```bash
# Enable verbose logging
adb logcat | grep ScanThrottler
```

**Expected Logs:**
```
ScanThrottler: Frame rate: 3.01 FPS (target: 2-4 FPS)
ScanThrottler: Frame rate: 2.98 FPS (target: 2-4 FPS)
```

**Performance Metrics:**
- [ ] Frame rate: 2-4 FPS (avg ~3 FPS)
- [ ] CPU usage: < 15% during idle overlay
- [ ] Battery drain: < 5% per hour with active overlay
- [ ] No ANR (Application Not Responding) warnings

### 6. Protocol Execution Verification

**Omega → Tier → Learning Order:**
1. Trigger scan with valid chart
2. Check logs for protocol execution

**Expected Logs:**
```
ProtocolRegistryMobile: Executing Omega01StructuralAnomalyGuard
ProtocolRegistryMobile: Executing Omega02RiskCapEnforcer
ProtocolRegistryMobile: Executing Omega03SecurityAuthValidator
ProtocolRegistryMobile: Executing Omega04ComplianceGuard
ProtocolRegistryMobile: Executing T01TrendStrengthConfirmation
...
ProtocolRegistryMobile: Executing LP01AdaptiveLearning
```

**Verification:**
- [ ] All 4 Omega protocols execute first
- [ ] Tier protocols (T01-T80) execute second
- [ ] Learning protocols (LP01-LP25) execute last
- [ ] Total: 109 protocols in correct order

---

## Test Fixtures Usage

### Using TestFixtures in Unit Tests

```kotlin
import com.lamontlabs.quantravision.fixtures.TestFixtures

@Test
fun `test pattern detection with mock chart`() {
    // Create test bitmap
    val testBitmap = TestFixtures.createTestChartBitmap(1080, 1920)
    
    // Create mock ChartPrimitives
    val primitives = TestFixtures.createMockChartPrimitives()
    
    // Run scan
    val result = ApexEngineMobile.runScan(context, primitives)
    
    // Verify
    assertNotNull(result)
    assertEquals(109, result.protocolTrace.size)
}
```

### Available Test Fixtures

1. **createTestChartBitmap()**: Simple dark background bitmap
2. **createValidChartBitmap()**: Simulated candle drawing
3. **createMockChartPrimitives()**: Generic mock data
4. **createMockBullishChartPrimitives()**: Bullish pattern data
5. **createMockBearishChartPrimitives()**: Bearish pattern data
6. **createEmptyChartPrimitives()**: Empty/minimal data

---

## Troubleshooting

### Build Failures

**Issue:** Gradle sync fails
```bash
# Clear cache
./gradlew clean --no-daemon
rm -rf ~/.gradle/caches
```

**Issue:** Lint errors
```bash
# Generate lint report
./gradlew lint
# Check: app/build/reports/lint-results.html
```

### Test Failures

**Issue:** Tests fail with Android context errors
```
Solution: Ensure Robolectric is configured correctly
- SDK version in @Config matches target SDK
- ApplicationProvider.getApplicationContext() used
```

**Issue:** QuotaGate tests fail with file not found
```
Solution: Clear test state before each test
context.getFileStreamPath("quota_state.json").delete()
```

### Runtime Issues

**Issue:** App crashes on scan
```bash
# Check logs
adb logcat | grep -E "QuantraVision|AndroidRuntime"
```

**Issue:** Overlay doesn't appear
```
- Verify MediaProjection permission granted
- Check Settings → Apps → QuantraVision → Permissions
- Restart overlay service via Settings tab
```

**Issue:** Frame rate too high (> 4 FPS)
```
- ScanThrottler not integrated
- Check OverlayService.handleTap() includes shouldScan() check
```

---

## Success Criteria

### Build Status
- ✅ GitHub Actions CI pipeline green
- ✅ Local build succeeds without errors
- ✅ All 120+ unit tests pass
- ✅ Lint report shows zero critical issues

### Functional Status
- ✅ App installs and launches
- ✅ UI navigation works
- ✅ Overlay captures screen
- ✅ Pattern detection triggers
- ✅ Throttling enforces 2-4 FPS
- ✅ FREE tier blocked from cloud
- ✅ PRO/ULTRA tiers respect quota

### Quality Status
- ✅ No crashes during testing
- ✅ Crash hardening recovers from MediaProjection failures
- ✅ Protocol execution follows Omega → Tier → Learning order
- ✅ Deterministic proof hashing works
- ✅ Performance metrics within targets

---

## Next Steps

After successful verification:

1. **Production Testing:**
   - Test on multiple devices (Samsung, Pixel, OnePlus)
   - Test with real trading apps (TradingView, Webull, Robinhood)
   - Monitor battery and performance over extended use

2. **Cloud Integration Testing:**
   - Add OpenAI API key to secrets
   - Test CloudReasoner narration
   - Validate LLMContractValidator filtering

3. **User Acceptance Testing:**
   - Recruit beta testers
   - Gather pattern detection accuracy feedback
   - Iterate on UI/UX based on feedback

4. **Google Play Release:**
   - Sign release APK
   - Upload to Play Console
   - Complete Play Store listing

---

## Support

For issues or questions:
- **GitHub Issues**: [Repository Issues](https://github.com/yourusername/quantravision/issues)
- **Documentation**: See `/docs` directory
- **Build Logs**: Check `.github/workflows/ci.yml` runs

---

**Last Updated:** November 24, 2025  
**Batch Version:** Batch 10 (Hardening & Green CI)  
**Verification Level:** Acquisition-Grade
