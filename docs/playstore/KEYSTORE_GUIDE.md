# Android Keystore Management Guide

**For QuantraVision Production Release**

---

## ⚠️ CRITICAL: Keystore Security

**Your keystore is the ONLY way to update your app on Google Play.**

If you lose it, you can **NEVER** update your app again. You would have to:
- Unpublish the current app
- Create a new app with a different package name
- Lose all reviews, ratings, and downloads
- Start from zero

**BACKUP YOUR KEYSTORE IN MULTIPLE SECURE LOCATIONS.**

---

## Generate Release Keystore (One-Time Setup)

### Step 1: Generate Keystore File

Run this command on your **local machine** (NOT in Replit):

```bash
keytool -genkey -v -keystore quantravision-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias quantravision
```

### Step 2: Answer Prompts

```
Enter keystore password: [CREATE STRONG PASSWORD]
Re-enter new password: [REPEAT PASSWORD]

What is your first and last name?
  [Your Name]: Jesse Lamont

What is the name of your organizational unit?
  [Your Organization]: Lamont Labs

What is the name of your organization?
  [Your Company]: Lamont Labs

What is the name of your City or Locality?
  [Your City]: [Your City]

What is the name of your State or Province?
  [Your State]: California

What is the two-letter country code for this unit?
  [XX]: US

Is CN=Jesse Lamont, OU=Lamont Labs, O=Lamont Labs, L=[City], ST=California, C=US correct?
  [no]: yes

Enter key password for <quantravision>
  (RETURN if same as keystore password): [PRESS ENTER or create different password]
```

### Step 3: Secure the Keystore

You now have `quantravision-release.jks` file. This is **CRITICAL**.

---

## Keystore Information to Record

**Save this information in a secure password manager (1Password, LastPass, Bitwarden):**

```
Keystore File: quantravision-release.jks
Keystore Password: [YOUR_PASSWORD]
Key Alias: quantravision
Key Password: [SAME_AS_KEYSTORE or DIFFERENT]
```

---

## Backup Strategy (Critical!)

### Option 1: Cloud Storage (Encrypted)
1. Upload `quantravision-release.jks` to:
   - Google Drive (encrypted folder)
   - Dropbox (encrypted folder)
   - iCloud (encrypted folder)
2. Store passwords in separate password manager

### Option 2: Physical Backup
1. Copy to USB flash drive
2. Store in safe or secure location
3. Create second USB as backup

### Option 3: Both (Recommended)
- Cloud backup for easy access
- Physical backup for disaster recovery

---

## Configure Android Studio to Use Keystore

### Option A: Manual Signing (Recommended for First-Time)

1. **Build → Generate Signed Bundle / APK**
2. **Select: Android App Bundle (AAB)**
3. **Click Next**
4. **Key store path:** Browse to `quantravision-release.jks`
5. **Key store password:** Enter your password
6. **Key alias:** quantravision
7. **Key password:** Enter key password
8. **Remember passwords:** Check (only on trusted computer)
9. **Click Next**
10. **Build Variants:** release
11. **Signature Versions:** V1 and V2 (both checked)
12. **Click Finish**

Output: `app/release/app-release.aab`

---

### Option B: Automated Signing (gradle.properties)

**WARNING: Only use on secure, encrypted computers.**

Create `keystore.properties` (NOT committed to git):

```properties
storeFile=/absolute/path/to/quantravision-release.jks
storePassword=YOUR_KEYSTORE_PASSWORD
keyAlias=quantravision
keyPassword=YOUR_KEY_PASSWORD
```

Update `app/build.gradle.kts`:

```kotlin
// Load keystore properties
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... other release config
        }
    }
}
```

Then build with:
```bash
./gradlew bundleRelease
```

---

## Security Best Practices

### ✅ Do:
- Store keystore in **multiple secure locations**
- Use **strong passwords** (16+ characters, mixed case, numbers, symbols)
- Keep keystore **encrypted** in cloud storage
- Store passwords in **password manager** (not in text files)
- **Never** commit keystore to git
- Add `*.jks` and `keystore.properties` to `.gitignore`

### ❌ Don't:
- **Never** share keystore with anyone
- **Never** upload to GitHub, Replit, or public servers
- **Never** email keystore
- **Never** store passwords in plain text
- **Never** lose your backup
- **Never** use weak passwords

---

## .gitignore Configuration

Ensure these lines are in `.gitignore`:

```gitignore
# Keystore files (NEVER commit these)
*.jks
*.keystore
keystore.properties
key.properties

# Release outputs (optional - can commit or ignore)
/app/release/
/app/build/outputs/
```

---

## Verify Keystore

To verify your keystore is valid:

```bash
keytool -list -v -keystore quantravision-release.jks
```

Enter password when prompted. You should see:
```
Alias name: quantravision
Creation date: [DATE]
Entry type: PrivateKeyEntry
Certificate chain length: 1
Certificate[1]:
Owner: CN=Jesse Lamont, OU=Lamont Labs, O=Lamont Labs, L=[City], ST=California, C=US
Issuer: CN=Jesse Lamont, OU=Lamont Labs, O=Lamont Labs, L=[City], ST=California, C=US
Valid from: [DATE] until: [DATE 27 years from now]
```

---

## Google Play App Signing

Google offers **Play App Signing** which provides an additional layer of protection:

1. **You sign with your upload key** (your keystore)
2. **Google re-signs with Play App Signing key** (managed by Google)
3. **If you lose upload key**, Google can reset it (but only once)

**Enroll in Play App Signing:**
1. Go to Play Console → Your App → Release → Setup → App Integrity
2. Choose "Use Google Play App Signing"
3. Upload your first release signed with your keystore
4. Google will manage the final signing

**Benefits:**
- Google stores final signing key securely
- You can reset upload key if lost (one-time reset)
- More secure distribution

---

## Emergency: Lost Keystore Recovery

### If Enrolled in Play App Signing:
1. Contact Google Play support
2. Request upload key reset
3. Google will verify your identity
4. Generate new keystore
5. Continue updating app

### If NOT Enrolled:
**NO RECOVERY POSSIBLE.**
- You cannot update your app
- You must unpublish and start over with new package name

---

## Testing Before Production

### Test Signed Build on Device:

1. Build signed release APK (not AAB):
   ```bash
   ./gradlew assembleRelease
   ```

2. Install on device:
   ```bash
   adb install app/build/outputs/apk/release/app-release.apk
   ```

3. Verify:
   - App installs successfully
   - All features work
   - IAP purchases work
   - No crashes

---

## Checklist Before First Upload

- [ ] Keystore generated (`quantravision-release.jks`)
- [ ] Passwords recorded in password manager
- [ ] Keystore backed up to cloud (encrypted)
- [ ] Keystore backed up to USB drive
- [ ] `*.jks` added to `.gitignore`
- [ ] Keystore tested and verified
- [ ] Signed AAB built successfully
- [ ] Signed APK tested on device
- [ ] Enrolled in Google Play App Signing
- [ ] Ready to upload to Play Console

---

**Current Status:** ⚠️ Keystore not yet created

**Next Action:** Run keystore generation command on your local machine (NOT in Replit)
