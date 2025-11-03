# Codemagic CI/CD Setup for QuantraVision

## Quick Start

1. **Sign up for Codemagic**: https://codemagic.io/signup
   - Free tier includes 500 build minutes/month
   - Pro tier: $99/month for unlimited builds

2. **Connect your repository**:
   - Go to Codemagic dashboard → Add application
   - Select your Git provider (GitHub, GitLab, Bitbucket)
   - Authorize access and select QuantraVision repository

3. **Select workflow**:
   - Codemagic will auto-detect `codemagic.yaml`
   - Choose workflow: `quantravision-android` (full build) or `quantravision-quick` (debug only)

4. **Start build**:
   - Click "Start new build"
   - Wait 5-10 minutes for first build (subsequent builds are faster due to caching)
   - Download APK from Artifacts section

---

## Two Workflows Included

### 1. `quantravision-android` (Full Production Build)
- **Duration**: ~10-15 minutes
- **Machine**: Mac Mini M2 (fastest)
- **Builds**:
  - Debug APK
  - Release APK (signed if keystore configured)
  - Release AAB (Android App Bundle for Google Play)
- **Tests**: Runs lint and unit tests
- **Artifacts**: All APKs, AABs, ProGuard mapping files
- **Use for**: Production releases, Google Play submissions

### 2. `quantravision-quick` (Fast Debug Build)
- **Duration**: ~5-7 minutes
- **Machine**: Linux x2 (faster for debug builds)
- **Builds**: Debug APK only
- **Tests**: None
- **Artifacts**: Debug APK
- **Use for**: Quick testing, development iterations

---

## Optional: Code Signing for Release Builds

To build **signed release APKs**, you need to create and upload a keystore.

### Step 1: Create Keystore (if you don't have one)

```bash
keytool -genkey -v -keystore quantravision.keystore -storetype JKS \
  -keyalg RSA -keysize 2048 -validity 10000 -alias quantravision

# Enter details when prompted:
# - Password: (choose a strong password)
# - Name: Your name or company
# - Organization: Lamont Labs
# - City: Your city
# - State: CA
# - Country: US
```

**Save these details** (you'll need them):
- Keystore file: `quantravision.keystore`
- Keystore password: (what you entered)
- Key alias: `quantravision`
- Key password: (what you entered)

### Step 2: Upload Keystore to Codemagic

1. Go to Codemagic → **Teams** → **Code signing identities**
2. Select **Android keystores**
3. Click **Upload keystore**
4. Upload `quantravision.keystore`
5. Enter keystore password, key alias, and key password
6. Give it reference name: `quantravision_keystore` (already configured in YAML)

### Step 3: Update build.gradle.kts (Already Done)

Your `app/build.gradle.kts` already has the signing configuration:

```kotlin
signingConfigs {
    create("release") {
        if (System.getenv("CI") != null) {
            // Codemagic will auto-populate these
            storeFile = file(System.getenv("CM_KEYSTORE_PATH") ?: "")
            storePassword = System.getenv("CM_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("CM_KEY_ALIAS")
            keyPassword = System.getenv("CM_KEY_PASSWORD")
        }
    }
}
```

✅ **Done!** Codemagic will now build signed release APKs.

---

## Optional: Google Play Publishing

To automatically publish to Google Play:

### Step 1: Create Google Cloud Service Account

1. Go to Google Play Console → Setup → API access
2. Create service account in Google Cloud
3. Download JSON key file
4. Grant permissions: Release to production, testing tracks

### Step 2: Add to Codemagic

1. Codemagic → **Teams** → **Integrations** → **Google Play**
2. Upload service account JSON
3. Save as environment variable group: `google_play`

### Step 3: Uncomment Google Play section in codemagic.yaml

In `codemagic.yaml`, uncomment these lines:

```yaml
google_play:
  credentials: $GCLOUD_SERVICE_ACCOUNT_CREDENTIALS
  track: internal  # Change to: alpha, beta, or production
  submit_as_draft: true
```

✅ **Done!** Builds will auto-publish to Google Play.

---

## Environment Setup

### Required (Already Configured):
- ✅ JDK 17 (set in YAML)
- ✅ Android SDK 35 (Codemagic provides)
- ✅ Gradle 8.9 (from gradle-wrapper.properties)
- ✅ 6GB RAM allocation for Gradle (handles OpenCV + TensorFlow)

### Optional Configuration:

**Change email notifications**:
Edit `codemagic.yaml` line 119:
```yaml
recipients:
  - your-actual-email@example.com  # Replace this
```

**Add Slack notifications**:
Uncomment lines 127-133 in `codemagic.yaml`:
```yaml
slack:
  channel: '#builds'
  notify:
    success: true
    failure: true
```

---

## Build Minutes Usage

### Free Tier (500 min/month):
- Full build (`quantravision-android`): ~15 min
  - **~33 builds/month** free
- Quick build (`quantravision-quick`): ~7 min
  - **~71 builds/month** free

### Pro Tier ($99/month):
- Unlimited builds
- Faster machines (Mac Mini M2)
- Priority build queue

---

## Troubleshooting

### Build Fails with "Out of Memory"
Increase heap size in `codemagic.yaml`:
```yaml
GRADLE_OPTS: "-Dorg.gradle.jvmargs=-Xmx8g"
```

### Build Takes Too Long
Use `quantravision-quick` workflow for development:
- Skip tests: Remove test scripts
- Skip release builds: Only build debug
- Use Linux machines: Faster for debug builds

### Keystore Not Found
1. Verify keystore uploaded to Codemagic
2. Check reference name matches: `quantravision_keystore`
3. Ensure it's in the `android_signing` section of YAML

### APK Not Signed
Release APK will only be signed if:
1. Keystore uploaded to Codemagic
2. Referenced in YAML (`android_signing: - quantravision_keystore`)
3. Build runs successfully

Check build logs for:
```
Using keystore: /path/to/keystore
Signing APK...
```

---

## What Codemagic Provides

✅ **Pre-installed**:
- Android SDK 26-35
- Build Tools 35.0.0
- JDK 8, 11, 17, 21
- Gradle (all versions)
- Kotlin compiler
- Android emulators
- Git, curl, wget

✅ **Already Has Android SDK 35** - No setup needed!

✅ **Automatic caching** - Subsequent builds are 3-5x faster

✅ **Parallel builds** - Multiple workflows can run simultaneously

---

## Cost Estimate

### Your Usage Pattern:
- **Development (testing)**: Use `quantravision-quick` (7 min/build)
- **Weekly releases**: Use `quantravision-android` (15 min/build)

**Monthly estimate**:
- 10 quick builds/week = 280 min/month
- 1 full build/week = 60 min/month
- **Total: 340 min/month** (fits in free tier!)

---

## Next Steps

1. **Sign up**: https://codemagic.io/signup
2. **Connect repo**: Link your Git repository
3. **Start build**: Select `quantravision-quick` workflow
4. **Download APK**: Get it from Artifacts tab
5. **Install on Android device**: Test immediately

**You'll have a working APK in ~7 minutes** without installing Android SDK 35 locally!

---

## Support

- **Codemagic Docs**: https://docs.codemagic.io
- **Android Native Guide**: https://docs.codemagic.io/yaml-quick-start/building-a-native-android-app/
- **Community Slack**: https://codemagic.io/slack
- **Support Email**: support@codemagic.io

---

**Your code is ready for Codemagic.**  
No changes needed - just push and build! 🚀
