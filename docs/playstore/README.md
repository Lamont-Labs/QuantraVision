# Google Play Store Submission Materials

**QuantraVision Production Launch Documentation**

---

## 📁 What's in This Directory

This directory contains everything needed for Google Play Store submission:

### **Documentation**
- **LAUNCH_CHECKLIST.md** - Complete pre-launch checklist with timeline
- **SCREENSHOTS_GUIDE.md** - Requirements and best practices for screenshots
- **CONTENT_RATING.md** - IARC questionnaire answers
- **KEYSTORE_GUIDE.md** - Security guide for release signing

### **Generated Assets** (in `/dist/playstore/`)
- `screenshot_*.png` - Placeholder screenshots (replace with real captures)
- `feature_graphic.png` - Feature graphic (1024x500)
- `metadata/` - Store listing text (title, descriptions, changelog)

---

## 🚀 Quick Start

### **1. Review the Launch Checklist**
Read `LAUNCH_CHECKLIST.md` for complete preparation steps.

**Current Status: 85% Ready**

**✅ Complete:**
- App development & testing
- Legal compliance (Privacy Policy, Terms, Disclaimers)
- Store metadata (title, descriptions)
- Pricing structure ($2.99, $14.99, $49.99)
- Documentation

**⚠️ Pending (Critical):**
- [ ] Real device screenshots (4-8 required)
- [ ] Google Play Developer account ($25)
- [ ] Release keystore generation
- [ ] Signed AAB build
- [ ] IAP product creation

---

### **2. Capture Real Screenshots**
See `SCREENSHOTS_GUIDE.md` for detailed instructions.

**Required:**
- 4-8 phone screenshots (1080x1920 px)
- Professional quality showing actual app features

**Recommended:**
- Feature graphic (professional design, not placeholder)
- App preview video (30-60 seconds)
- Tablet screenshots (optional)

---

### **3. Create Release Keystore**
See `KEYSTORE_GUIDE.md` for security-critical setup.

**⚠️ CRITICAL:** Your keystore is the ONLY way to update your app. If lost, you can never update again.

```bash
# Run on your LOCAL machine (not Replit)
keytool -genkey -v -keystore quantravision-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias quantravision
```

**Backup your keystore in 3+ locations!**

---

### **4. Build Signed Release**

#### In Android Studio:
1. **Build → Generate Signed Bundle / APK**
2. **Select Android App Bundle (AAB)**
3. **Choose your keystore**
4. **Select release variant**
5. **Build**

Output: `app/release/app-release.aab` (upload to Play Console)

---

### **5. Create Google Play Developer Account**
1. Visit https://play.google.com/console
2. Pay $25 one-time registration fee
3. Complete tax and banking information
4. Accept developer agreement

---

### **6. Set Up Store Listing**

Use the pre-generated metadata from `dist/playstore/metadata/android/en-US/`:

- **App Name:** QuantraVision
- **Short Description:** See patterns your platform can't. Offline, private, deterministic.
- **Full Description:** (provided in full_description.txt)
- **Category:** Finance
- **Content Rating:** Everyone (E) - See CONTENT_RATING.md

**Upload Graphics:**
- App icon: 512x512 PNG
- Feature graphic: 1024x500 PNG
- Screenshots: 4-8 phone screenshots

---

### **7. Configure In-App Products**

Create these IAP items in Play Console:

| Product ID | Type | Price | Title |
|------------|------|-------|-------|
| `quantravision_book` | One-time | $2.99 | The Friendly Trader Book |
| `quantravision_standard` | One-time | $14.99 | Standard Tier |
| `quantravision_pro` | One-time | $49.99 | Pro Tier |

---

### **8. Upload & Submit**

1. Go to **Production** track
2. Create new release
3. Upload `app-release.aab`
4. Add release notes from `RELEASE_NOTES.md`
5. Review all settings
6. **Submit for review**

**Review time:** 3-7 days typically

---

## 📋 Pre-Submission Checklist

### Critical (Must Complete)
- [ ] Real screenshots captured (4-8 phone)
- [ ] Keystore generated and backed up (3+ locations)
- [ ] Signed AAB built successfully
- [ ] Google Play account created ($25 paid)
- [ ] IAP products created and tested
- [ ] Content rating submitted

### Important (Highly Recommended)
- [ ] Professional feature graphic designed
- [ ] App tested on 3+ real devices
- [ ] Pre-launch report reviewed (from Google)
- [ ] Internal testing completed
- [ ] All features working correctly

### Nice to Have
- [ ] App preview video created
- [ ] Tablet screenshots captured
- [ ] Landing page/website live
- [ ] Social media accounts created

---

## 🎯 Timeline to Launch

**If starting now:**

- **Day 1:** Capture screenshots, design feature graphic
- **Day 2:** Create Play account, generate keystore
- **Day 3:** Build signed AAB, test on devices
- **Day 4:** Upload to internal testing, create IAP products
- **Day 5:** Fix any issues, final testing
- **Day 6:** Submit for production review
- **Day 7-10:** Wait for Google approval
- **Day 11:** **LAUNCH!** 🚀

---

## 📞 Support

**Questions?**
- Email: Lamontlabs@proton.me
- GitHub: https://github.com/Lamont-Labs/QuantraVision
- Docs: See individual guide files in this directory

---

## ⚖️ Legal Compliance

All required legal documents are ready:
- Privacy Policy: `legal/PRIVACY_POLICY.md`
- Terms of Use: `legal/TERMS_OF_USE.md`
- Financial Disclaimer: `legal/FINANCIAL_DISCLAIMER.md`

URLs for Play Console:
- Privacy Policy: https://github.com/Lamont-Labs/QuantraVision/blob/main/legal/PRIVACY_POLICY.md
- Terms: https://github.com/Lamont-Labs/QuantraVision/blob/main/legal/TERMS_OF_USE.md

---

**Status:** Ready for final preparation steps → See LAUNCH_CHECKLIST.md

**Next Action:** Capture real device screenshots (SCREENSHOTS_GUIDE.md)
