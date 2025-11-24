# Security Policy

## Overview

QuantraVision is built with security and privacy as foundational principles. This document outlines our security practices, vulnerability reporting process, and commitment to protecting user data.

## 🔒 Core Security Principles

**Privacy-First Architecture:**
- **FREE Tier**: 100% offline processing with zero cloud dependencies
- **Paid Tiers**: Optional cloud narration sends only structured Apex packets (JSON metadata), never screenshots or chart data
- **No PII Collection**: Zero personally identifiable information collected across all tiers
- **On-Device Processing**: All pattern detection, scoring, and local summaries execute on-device
- **No Analytics/Telemetry**: No tracking, no crash reporting, no third-party analytics

**Fail-Closed Design Philosophy:**
- **QuotaGate**: Tier-based API limits enforced with fail-closed logic (deny access if quota check fails)
- **LLM Contract Validation**: Cloud responses validated against strict schema; forbidden financial advice terms cause automatic rejection with fallback to local summary
- **Omega Protocol Safety Locks**: Anomaly detection disables all overlays and cloud calls until manual health check performed
- **Encrypted Local Storage**: Room database with SQLCipher support for sensitive data
- **Permission Gating**: MediaProjection and billing features fail gracefully when permissions denied

## 📋 Supported Versions

We actively support the following versions with security updates:

| Version | Supported          | Android API Level |
| ------- | ------------------ | ----------------- |
| 1.x.x   | ✅ Yes             | API 26+ (Android 8.0+) |
| < 1.0   | ❌ No (Beta only)  | N/A               |

**Current Release**: Check [CHANGELOG.md](docs/CHANGELOG.md) for the latest version.

## 🚨 Reporting a Vulnerability

We take security vulnerabilities seriously. If you discover a security issue in QuantraVision, please report it responsibly.

### How to Report

**🔐 Email**: security@lamontlabs.com  
**Subject**: `[SECURITY] QuantraVision Vulnerability Report`

### What to Include

Please provide the following information in your report:

1. **Description**: Clear description of the vulnerability
2. **Impact**: Potential security impact (data exposure, privilege escalation, etc.)
3. **Steps to Reproduce**: Detailed steps to reproduce the issue
4. **Affected Versions**: Which app versions are affected
5. **Environment**: Android version, device model, and any relevant configuration
6. **Proof of Concept**: Code, screenshots, or videos demonstrating the issue (if applicable)
7. **Suggested Fix**: Any recommendations for remediation (optional)

### Response Timeline

We are committed to addressing security issues promptly:

- **Acknowledgment**: Within **48 hours** of receiving your report
- **Initial Assessment**: Within **5 business days** we'll provide an initial impact assessment
- **Progress Updates**: Weekly updates on remediation progress
- **Resolution**: Critical vulnerabilities will be patched within **30 days** when possible
- **Disclosure**: Coordinated disclosure after patch release (typically 90 days)

### What Happens Next

1. **Triage**: Our security team will assess the severity and impact
2. **Validation**: We'll attempt to reproduce the issue
3. **Fix Development**: Our developers will create a patch
4. **Testing**: Thorough testing of the fix across supported devices
5. **Release**: Security patch released via Google Play Store
6. **Disclosure**: Public disclosure with credit to the reporter (if desired)

## 🏆 Recognition

We value the security research community's contributions. Researchers who report valid security issues will be:

- **Publicly Acknowledged**: In our security advisories (with permission)
- **Listed**: In our [CONTRIBUTORS.md](docs/CONTRIBUTORS.md) file
- **Credited**: In release notes for the patched version

## ⚠️ Responsible Disclosure Guidelines

To protect users, we ask that you:

- ✅ **Do**: Report vulnerabilities privately via email first
- ✅ **Do**: Give us reasonable time to fix the issue before public disclosure (90 days)
- ✅ **Do**: Provide detailed information to help us reproduce and fix the issue
- ❌ **Don't**: Publicly disclose vulnerabilities before we've issued a patch
- ❌ **Don't**: Exploit vulnerabilities beyond what's necessary to demonstrate the issue
- ❌ **Don't**: Access or modify user data without explicit permission

## 🔍 Security Best Practices for Users

To maximize your security when using QuantraVision:

1. **Download from Official Sources**: Only install QuantraVision from Google Play Store or verified APK sources
2. **Keep Updated**: Enable automatic updates to receive security patches promptly
3. **Review Permissions**: QuantraVision only requests necessary permissions (overlay, storage)
4. **Device Security**: Use device encryption and lock screen protection
5. **Avoid Rooted Devices**: Rooting compromises Android's security model and may expose app data

## 🛡️ Security Features

### Data Protection

**Encryption:**
- Local database encryption using SQLCipher (Room integration)
- Secure SharedPreferences for sensitive configuration
- API key storage via Android Keystore System (for paid tier cloud access)
- No plaintext storage of credentials or secrets

**Access Control:**
- MediaProjection permission required for screen capture with user-facing explanations
- Runtime permission checks with graceful degradation
- Billing entitlement verification before feature unlock
- Quota enforcement prevents abuse of cloud API calls (tier-based limits)

### Application Security

**Integrity Verification:**
- **ProofHasher**: SHA-256 hash verification for all Apex scan results
- **DetectionAuditTrail**: Complete provenance logging of all pattern detections
- Google Play Integrity API integration (planned for release builds)
- APK signature validation in IntegrityChecker.kt

**Input Validation:**
- LLM contract validation filters forbidden financial advice terms (buy, sell, long, short, stop loss, etc.)
- Pattern detection input sanitization (bitmap validation, size checks)
- OCR output validation and sanitization
- Billing SKU verification against hardcoded whitelist in billing_skus.json

**Network Security (Paid Tiers Only):**
- Cloud API calls only for paid tiers with explicit user consent
- HTTPS-only communication (enforced via Network Security Config)
- Request timeout limits (15 seconds for cloud narration, 10 seconds for connections)
- No screenshot/chart data transmission (only structured Apex packets)

### Denial of Service Protection

**Rate Limiting:**
- **QuotaGate**: Tier-based daily limits (0 for FREE, 10 for PRO/STARTER, 25 for ULTRA/STANDARD/PRO)
- Minimum 8 seconds between cloud API calls
- Maximum 3 calls per 60 seconds rolling window
- **ScanThrottler**: 2-4 FPS performance guardrails to prevent battery drain

**Resource Management:**
- Bitmap memory pooling to prevent OutOfMemoryError
- Pattern detection cache with LRU eviction
- Coroutine cancellation on lifecycle events (prevents memory leaks)
- PowerGuard: Battery/thermal throttling automatically reduces scan frequency

## 🔍 Third-Party Dependencies

### Dependency Management Policy

**Audit Process:**
- Regular dependency scans using GitHub Dependabot (automated alerts)
- Manual security review of all new dependencies before adoption
- License compatibility verification (Apache 2.0, MIT, BSD-3-Clause only)
- Security advisory monitoring via GitHub Security tab

**Current Production Dependencies:**
- **Android Jetpack**: Google-maintained, auto-updated, Apache 2.0
- **OpenCV 4.10.0**: Apache 2.0, active security patches from opencv.org
- **TensorFlow Lite 2.17.0**: Apache 2.0, Google-maintained with quarterly updates
- **Google ML Kit Text Recognition**: Proprietary but Google-maintained
- **Timber Logging**: Apache 2.0, minimal attack surface (logging only)

**Dependency Update Timeline:**
- **Critical security patches**: Applied within 7 days
- **High severity vulnerabilities**: Applied within 30 days
- **Routine updates**: Quarterly review cycle
- **Breaking changes**: Evaluated for cost/benefit before adoption

### Known Limitations

- **Google ML Kit**: Proprietary library with limited transparency; mitigated by sandboxed operation
- **Google Play Billing**: Closed-source SDK, mandatory for Play Store distribution
- **Future ML Models**: Only Apache 2.0 licensed models will be integrated (Gemma 2B, Phi-2)

## 🚑 Incident Response Process

### In Case of Active Security Breach

If a security vulnerability is actively being exploited in the wild:

**1. Immediate Response (0-24 hours):**
- Incident commander assigned from security team
- Assess scope, impact, and number of affected users
- Deploy emergency hotfix if available
- Disable affected feature via remote config (if applicable)

**2. Containment (24-72 hours):**
- Revoke compromised API keys if cloud infrastructure affected
- Issue updated APK with security patch via Google Play emergency update
- Notify affected users via in-app notification (if contact info available)
- Document incident timeline and actions taken

**3. Communication (Within 24 hours of confirmation):**
- Public security advisory published to GitHub Security tab
- Incident summary posted to project README
- Email notification to users who reported the vulnerability
- Transparent disclosure of what data (if any) was affected

**4. Post-Incident Analysis (Within 7 days):**
- Detailed post-mortem report published
- Root cause analysis to identify systemic issues
- Additional test coverage for vulnerability class
- Architecture review if design flaw identified
- Update security training for development team

## 🏛️ Compliance & Privacy

### Data Collection by Tier

**FREE Tier:**
- **Data Collection**: ZERO (100% offline)
- **Network Requests**: NONE
- **Analytics**: Disabled
- **Crash Reporting**: Disabled
- **Compliance**: No GDPR/CCPA obligations (no data collected)

**Paid Tiers (STARTER, STANDARD, PRO):**
- **Cloud Narration**: Sends structured Apex packets (JSON) only when user explicitly requests explanation
- **Transmitted Data**: QuantraScore, protocol trace, entropy metrics, status flags
- **NOT Transmitted**: Screenshots, chart images, ticker symbols, trading activity, timestamps, device IDs
- **User Control**: User can disable cloud narration entirely via settings
- **Audit Trail**: All network requests logged locally for user inspection

### Regulatory Compliance

- **No Financial Advice**: All disclaimers prominently displayed in app and legal documents
- **GDPR Compliance**: No personal data collection = no data subject access requests
- **CCPA Compliance**: No data sale, no tracking, user privacy by default
- **Google Play Policies**: Full compliance with Play Store security and privacy requirements
- **Educational Use Only**: Clear disclaimers that all output is educational, not investment advice

### Open Source Transparency

- All source code available for public audit (Apache 2.0 license)
- Build reproducibility via GitHub Actions CI/CD (public logs)
- No obfuscated behavior in FREE tier
- ProGuard rules documented and auditable in RELEASE_PLAYBOOK.md
- Security architecture fully documented in ARCHITECTURE.md

## 🔐 Security Audit History

| Date       | Scope                  | Findings | Status    |
|------------|------------------------|----------|-----------|
| 2025-10-31 | Initial security review| 0 High   | ✅ Passed |

## 📞 Contact

For non-security inquiries:
- **Support**: support@lamontlabs.com
- **General**: info@lamontlabs.com

For security issues **only**:
- **Security**: security@lamontlabs.com

## 📚 Additional Resources

- **Security Architecture**: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) - Security architecture section
- **Privacy Policy**: [app/src/main/assets/legal/PRIVACY_POLICY.html](app/src/main/assets/legal/PRIVACY_POLICY.html)
- **Terms of Use**: [app/src/main/assets/legal/TERMS_OF_USE.html](app/src/main/assets/legal/TERMS_OF_USE.html)
- **Release Playbook**: [RELEASE_PLAYBOOK.md](RELEASE_PLAYBOOK.md) - ProGuard configuration and security hardening
- **Contributing Guide**: [CONTRIBUTING.md](CONTRIBUTING.md) - Secure development practices

## 🙏 Acknowledgments

We thank the security research community for responsible disclosure and collaboration in keeping QuantraVision secure.

**Security Hall of Fame:** Researchers who have responsibly disclosed vulnerabilities will be listed here (with permission).

---

**Last Updated**: November 24, 2025  
**Version**: 2.0  
**Contact**: security@lamontlabs.com
