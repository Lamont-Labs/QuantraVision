# Pull Request

## 📋 Description

<!-- Provide a clear and concise description of what this PR accomplishes -->

**Type of Change:**
- [ ] 🐛 Bug fix (non-breaking change fixing an issue)
- [ ] ✨ New feature (non-breaking change adding functionality)
- [ ] 💥 Breaking change (fix or feature causing existing functionality to break)
- [ ] 📚 Documentation update
- [ ] 🎨 Code refactoring (no functional changes)
- [ ] ⚡ Performance improvement
- [ ] 🧪 Test additions/improvements

**Related Issues:**
<!-- Link related issues using #issue_number -->
Fixes #(issue)
Closes #(issue)
Related to #(issue)

---

## 🎯 Motivation and Context

<!-- Why is this change needed? What problem does it solve? -->

**Problem Statement:**
<!-- Describe the problem or limitation -->

**Solution Approach:**
<!-- Explain your solution and why this approach was chosen -->

---

## 🧪 Testing

### Test Environment
- **Device**: <!-- e.g., Google Pixel 8 Pro -->
- **Android Version**: <!-- e.g., Android 14 (API 34) -->
- **QuantraVision Tier**: <!-- Free / Standard / Pro -->
- **Trading App**: <!-- e.g., TradingView (if applicable) -->

### Testing Performed
<!-- Describe the tests you ran to verify your changes -->

**Unit Tests:**
- [ ] All existing tests pass
- [ ] New tests added for new functionality
- [ ] Test coverage: ____%

**Manual Testing:**
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Lint check
./gradlew lint

# Build debug APK
./gradlew assembleDebug
```

**Test Scenarios:**
<!-- List specific test cases you executed -->
1. 
2. 
3. 

---

## 📸 Screenshots / Screen Recording

<!-- If this PR includes UI changes, provide before/after screenshots or screen recordings -->

**Before:**
<!-- Drag image here or paste link -->

**After:**
<!-- Drag image here or paste link -->

**Screen Recording:**
<!-- Optional: Add video showing the feature in action -->

---

## 🔍 Code Quality Checklist

### General
- [ ] Code follows the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [ ] Self-review completed (read through all changes)
- [ ] Code is well-commented (KDoc for public APIs, inline comments for complex logic)
- [ ] No debug/console.log statements left in code
- [ ] No hardcoded strings (all strings in `strings.xml`)
- [ ] No magic numbers (use constants with descriptive names)

### Performance
- [ ] No memory leaks (proper resource cleanup)
- [ ] No blocking operations on main thread
- [ ] Efficient algorithms used (O(n) complexity documented if >O(n log n))
- [ ] Images properly recycled (Bitmap.recycle() where needed)

### Android Best Practices
- [ ] Follows MVVM architecture pattern
- [ ] Uses Kotlin Coroutines for async operations
- [ ] Proper lifecycle awareness (ViewModel, LiveData/Flow)
- [ ] Resource files organized (drawables, layouts, strings)
- [ ] Backward compatibility considered (minimum API 24)

### Security & Privacy
- [ ] No sensitive data logged
- [ ] No API keys or secrets in code
- [ ] Proper data validation and sanitization
- [ ] User privacy maintained (offline-first principle)

### Documentation
- [ ] KDoc comments added for all public APIs
- [ ] README.md updated (if applicable)
- [ ] CHANGELOG.md updated (user-facing changes)
- [ ] Architecture docs updated (if structural changes)

---

## 💥 Breaking Changes

<!-- Does this PR introduce any breaking changes? If yes, describe them and the migration path -->

- [ ] This PR contains breaking changes

**Breaking Changes Description:**
<!-- If applicable, describe what breaks and how users should migrate -->

**Migration Guide:**
<!-- Step-by-step guide for updating existing code/configurations -->

---

## 🚀 Deployment Notes

<!-- Any special considerations for deployment? -->

**Database Migrations:**
- [ ] No database changes
- [ ] Database schema updated (migration script included)

**Configuration Changes:**
- [ ] No configuration changes
- [ ] Requires new permissions: <!-- List permissions -->
- [ ] Requires new dependencies: <!-- List dependencies -->

**Rollback Plan:**
<!-- How can this change be reverted if issues arise? -->

---

## 📝 Additional Notes

<!-- Any additional context, trade-offs, or follow-up tasks -->

**Known Limitations:**
<!-- List any known issues or limitations -->

**Follow-Up Tasks:**
<!-- List related tasks to be done in future PRs -->
- [ ] 
- [ ] 

**Questions for Reviewers:**
<!-- Specific areas you'd like feedback on -->

---

## ✅ Pre-Merge Checklist

### Build & Tests
- [ ] ✅ Code compiles without errors (`./gradlew clean build`)
- [ ] ✅ All unit tests pass (`./gradlew test`)
- [ ] ✅ All instrumented tests pass (`./gradlew connectedAndroidTest`)
- [ ] ✅ Lint checks pass with no new warnings (`./gradlew lint`)
- [ ] ✅ Zero LSP errors in Android Studio
- [ ] ✅ Validation script passes (`bash scripts/validate-project.sh`)

### Code Review
- [ ] ✅ Self-review completed
- [ ] ✅ Code follows style guidelines
- [ ] ✅ All review comments addressed
- [ ] ✅ Requested changes implemented

### Documentation
- [ ] ✅ Code is properly documented
- [ ] ✅ CHANGELOG.md updated (if user-facing)
- [ ] ✅ README.md updated (if applicable)
- [ ] ✅ Migration guide provided (if breaking changes)

### Testing
- [ ] ✅ Manual testing completed on real device
- [ ] ✅ Edge cases tested
- [ ] ✅ Performance tested (no regressions)
- [ ] ✅ Screenshots/videos attached (if UI changes)

---

## 🎉 Reviewer Notes

<!-- Optional: Add specific notes for reviewers -->

**Areas of Focus:**
<!-- Guide reviewers to critical sections -->

**Performance Concerns:**
<!-- Highlight any performance-sensitive code -->

**Security Considerations:**
<!-- Call out security-related changes -->

---

**By submitting this PR, I confirm that:**
- I have read and followed the [Contributing Guidelines](../../CONTRIBUTING.md)
- My code follows the project's coding standards
- I have tested my changes thoroughly
- I am willing to address review feedback

---

<div align="center">

**Thank you for contributing to QuantraVision!** 🚀

Your efforts help make professional pattern detection accessible to traders worldwide.

</div>
