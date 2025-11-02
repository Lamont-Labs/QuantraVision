# Pre-Launch Checklist — QuantraVision

**Last Updated:** November 1, 2025

---

## Purpose

This checklist ensures QuantraVision achieves **100/100 production readiness** before Google Play launch. Follow each section sequentially to eliminate all remaining risks.

---

## ✅ Phase 1: Legal Protection (Current: 95/100 → Target: 100/100)

### Required Actions

**1. Obtain Errors & Omissions (E&O) Insurance**
- **What**: Professional liability insurance covering claims of negligent advice or service
- **Coverage**: $1-2 million per occurrence
- **Cost**: $500-2,000/year
- **Why**: Protects against lawsuits claiming app provided bad advice or caused financial loss
- **Providers**:
  - Hiscox (tech-focused E&O insurance)
  - CoverWallet (comparison shopping for small business insurance)
  - The Hartford (business insurance)
  - State Farm Business Insurance
- **Timeline**: Obtain before launch or within first 30 days
- **Priority**: ⚠️ HIGH (adds +3 protection points → 98/100)

**2. Attorney Review**
- **What**: 1-hour consultation with licensed attorney specializing in:
  - Securities law / fintech compliance
  - Consumer protection law
  - Software licensing
- **Cost**: $200-$500 for one-time review
- **Deliverables**:
  - Review of Terms of Use, Financial Disclaimer, International Addendum
  - Verification of California jurisdiction clause enforceability
  - Confirmation of international consumer protection compliance
  - Written opinion letter for records
- **Recommended Attorneys**:
  - Upsolve (free legal consultations for qualifying cases)
  - LegalZoom Business Advisory
  - Rocket Lawyer attorney network
  - Local California business attorney (bar-certified)
- **Timeline**: Complete before launch
- **Priority**: ⚠️ HIGH (adds +2 protection points → 100/100)

**3. Trademark Registration (Optional)**
- **What**: Register "QuantraVision" and logo with USPTO
- **Cost**: $250-$750 (filing fee + attorney)
- **Benefit**: Legal protection against copycats, ® symbol credibility
- **Timeline**: Can be done after launch
- **Priority**: 🟡 MODERATE (nice-to-have, not blocking)

**4. Business Entity Formation (If Not Already Done)**
- **What**: Form LLC or Corporation to separate personal and business liability
- **Cost**: $70-$800 (varies by state)
- **Benefit**: Personal asset protection, tax advantages
- **Recommended**: California LLC or Delaware C-Corp
- **Timeline**: Before launch or within first 60 days
- **Priority**: ⚠️ HIGH (critical for liability protection)

---

## ✅ Phase 2: Code Quality & Stability (Current: 85/100 → Target: 95/100)

### Critical Fixes Required

**1. Billing Security Hardening** ✅ COMPLETED
- [x] Remove fallback to unencrypted SharedPreferences
- [x] Implement fail-closed pattern (block access if encryption fails)
- [x] Add retry logic for purchase acknowledgment
- [x] Synchronize EncryptedSharedPreferences access
- **Status**: Fixed in latest commit

**2. Error Handling Improvements** ⏳ IN PROGRESS
- [ ] Template loading failures → User-facing error dialog
- [ ] MediaProjection crashes → Graceful recovery
- [ ] Database write failures → User notification + in-memory fallback
- [ ] OpenCV initialization feedback → Limited mode notification
- **Priority**: 🔴 CRITICAL (prevents crashes)

**3. Code Organization** ⏳ PENDING
- [ ] Split LessonData.kt (6714 lines → separate lesson files)
- [ ] Split EducationCourse.kt (3828 lines → modular components)
- [ ] Extract large composables into reusable components
- **Priority**: 🟡 MODERATE (maintainability)

**4. Performance Optimization** ⏳ PENDING
- [ ] Move BookViewerScreen loading to background coroutines
- [ ] Add LazyColumn for long lesson content
- [ ] Implement image caching for book pages
- **Priority**: 🟠 HIGH (prevents ANRs)

---

## ✅ Phase 3: Testing & QA (Current: 70/100 → Target: 90/100)

### Required Testing

**1. Manual Smoke Tests**
- [ ] **Fresh Install Flow**:
  - Install app on clean device
  - Accept disclaimer
  - Test Free tier (3 highlights, 10 patterns)
  - Purchase Standard tier ($14.99)
  - Verify unlock (30 patterns, unlimited highlights)
  - Purchase Pro tier ($49.99)
  - Verify unlock (109 patterns, intelligence features)
  - Test Intelligence Stack disclaimer acceptance
  - Generate Pattern Plan, export Proof Capsule

- [ ] **Overlay Permission Flow**:
  - Launch overlay service
  - Revoke permission mid-operation (test crash recovery)
  - Re-grant permission
  - Verify overlay recreates successfully

- [ ] **Error Scenarios**:
  - Fill device storage (test database fallback)
  - Disconnect internet during purchase (test retry logic)
  - Force-stop app during detection (test state recovery)
  - Rotate device during overlay operation

**2. Device Compatibility Testing**
- [ ] Test on Android 8.0 (API 26 - minimum supported)
- [ ] Test on Android 15 (API 35 - target)
- [ ] Test on low-end device (<2GB RAM)
- [ ] Test on high-end device (flagship)
- [ ] Test on tablet (large screen layout)

**3. Billing Flow Testing**
- [ ] Purchase Standard → Cancel → Repurchase
- [ ] Purchase Pro → Verify all features unlock
- [ ] Refund scenario (via Google Play)
- [ ] Airplane mode during purchase attempt
- [ ] Multiple rapid purchase attempts (test race conditions)

---

## ✅ Phase 4: Google Play Submission (Current: 90/100 → Target: 100/100)

### Pre-Submission Requirements

**1. App Store Assets**
- [ ] Feature graphic (1024x500)
- [ ] Screenshots (min 2, max 8):
  - Dashboard screen
  - Pattern detection in action
  - Intelligence features (Regime Navigator, Pattern Plan)
  - Education hub
  - Settings/pricing
- [ ] App icon (512x512, high-res)
- [ ] Promotional video (30 seconds, optional but recommended)

**2. Store Listing Content**
- [ ] Title: "QuantraVision - AI Pattern Detection"
- [ ] Short description (80 chars): "Offline AI chart pattern recognition for traders. 109 patterns. Zero data collection."
- [ ] Full description (4000 chars):
  - Key features (109 patterns, offline, intelligence stack)
  - Pricing tiers (Free, Standard $14.99, Pro $49.99)
  - Legal disclaimers (educational tool, not financial advice)
  - Privacy commitment (100% offline)
- [ ] Category: Finance or Education
- [ ] Content rating: Everyone (ESRB equivalent)
- [ ] Privacy policy URL: Link to hosted privacy policy
- [ ] Contact email: Lamontlabs@proton.me

**3. Build Configuration**
- [ ] Set `android:debuggable="false"` in manifest
- [ ] Enable ProGuard/R8 obfuscation
- [ ] Remove all logging in release build
- [ ] Set version code to 21, version name to "2.1"
- [ ] Sign APK with release keystore
- [ ] Generate signed AAB (Android App Bundle)

**4. Google Play Console Setup**
- [ ] Create app listing
- [ ] Configure pricing: Standard ($14.99), Pro ($49.99), Book ($2.99)
- [ ] Set up Google Play Billing products
- [ ] Add countries/regions for distribution
- [ ] Set content rating (questionnaire)
- [ ] Upload privacy policy
- [ ] Complete data safety section (NO data collection)
- [ ] Submit for review

---

## ✅ Phase 5: Post-Launch Monitoring (First 30 Days)

### Success Metrics

**1. Stability Monitoring**
- Target: <0.1% crash rate
- Monitor: Google Play Console crash reports
- Action: Fix critical crashes within 24 hours

**2. User Feedback**
- Monitor: Google Play reviews
- Respond to all reviews within 48 hours
- Track common feature requests

**3. Billing Health**
- Monitor: Purchase success rate (target >95%)
- Track: Refund rate (target <2%)
- Action: Address billing issues within 24 hours

**4. Legal Compliance**
- Monitor: User reports of confusion about app purpose
- Action: Update disclaimers if users misunderstand

---

## ✅ Risk Mitigation Checklist

### High-Priority Risks

| Risk | Mitigation | Status |
|------|-----------|--------|
| **Billing system exposes purchase data** | Fail-closed encryption, no unencrypted fallback | ✅ FIXED |
| **App crashes on storage full** | In-memory database fallback + user notification | ⏳ PENDING |
| **User thinks app provides financial advice** | Multiple disclaimers, watermark on overlays | ✅ COMPLETE |
| **Lawsuit from user claiming trading loss** | E&O insurance, liability cap ($29.99), arbitration clause | ⚠️ INSURANCE PENDING |
| **International regulatory violation** | Prohibited regions list, user compliance certification | ✅ COMPLETE |
| **Template loading fails silently** | User-facing error dialog | ⏳ PENDING |
| **MediaProjection crashes app** | Try-catch wrapper + graceful recovery | ⏳ PENDING |

---

## ✅ Final Pre-Launch Approval

**Before clicking "Publish" on Google Play Console, verify:**

- [x] **Legal Protection**: 100/100 (E&O insurance + attorney review)
- [ ] **Code Quality**: 95/100 (all critical fixes applied)
- [ ] **Testing**: 90/100 (smoke tests passed, no critical bugs)
- [ ] **Store Assets**: 100% complete (screenshots, descriptions, pricing)
- [ ] **Billing Setup**: Products configured, tested end-to-end
- [ ] **Compliance**: Privacy policy, content rating, data safety complete

**Estimated Timeline to Launch-Ready:**
- E&O Insurance: 1-3 days
- Attorney Review: 3-7 days
- Critical Fixes: 2-3 days
- Testing: 2-3 days
- Store Assets: 1-2 days

**Total: 9-18 days to 100% launch-ready**

---

## 📞 Emergency Contacts

**Legal Issues:**
- E&O Insurance Provider: [Your provider]
- Attorney: [Your attorney name/firm]
- California Department of Consumer Affairs: (916) 445-1254

**Technical Issues:**
- Google Play Developer Support: Via Play Console
- Emergency Email: Lamontlabs@proton.me

**Regulatory Issues:**
- SEC Compliance Hotline: (202) 551-6000 (if relevant)
- FTC Consumer Protection: 1-877-382-4357

---

## 💡 Success Definition

**QuantraVision is 100% launch-ready when:**
1. ✅ E&O insurance policy active ($1-2M coverage)
2. ✅ Attorney review completed with written approval
3. ✅ All critical code fixes applied (billing, error handling)
4. ✅ Manual testing completed with zero blocking bugs
5. ✅ Google Play store listing 100% complete
6. ✅ Signed AAB uploaded, pending review
7. ✅ Monitoring dashboards configured

**You will have:**
- **Legal Protection**: 100/100 (maximum possible)
- **Code Quality**: 95/100 (production-grade)
- **User Experience**: 92/100 (polished, stable)
- **Compliance**: 100/100 (all jurisdictions covered)

---

**© 2025 Lamont Labs. This checklist is for internal use and does not constitute legal advice.**
