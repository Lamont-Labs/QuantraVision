# Development Roadmap

**Project:** QuantraVision - Apex-Inspired Intelligence System  
**Status:** On hold (November 2025)  
**Last Updated:** November 2025

## Current Reality

**What Exists:**
- Complete codebase with current implementation (template matching + OCR)
- Never compiled or tested on device
- Unknown number of bugs/issues
- NucBox K6 available with Android Studio installed
- Samsung S23 FE target device available

**What's Missing:**
- Consistent desktop access for development
- Validated accuracy metrics
- Market validation
- Production-ready pattern detection

**Prerequisites for Resuming:**
- **Time Commitment:** 40-80 hours over 6-11 weeks
- **Session Frequency:** 2-3 focused sessions per week
- **Session Duration:** 2-4 hours per session
- **Desktop Access:** Consistent NucBox availability
- **Debugging Workflow:** Real-time AI assistance via GitHub

## Phase 0: Foundation & Validation (2-4 weeks)

**Goal:** Get current codebase working and establish baseline performance

### Week 1-2: Build & Debug Current System

**Tasks:**
1. Open project in Android Studio on NucBox K6
2. Resolve compilation errors
   - Missing dependencies
   - Type mismatches
   - Import issues
   - Gradle configuration
3. Build APK successfully
4. Install on Samsung S23 FE
5. Fix runtime crashes
   - Null pointer exceptions
   - Missing initializations
   - Resource loading issues
   - Permission problems

**Deliverable:** App that launches and doesn't crash

**Estimated Time:** 10-20 hours (highly variable depending on issues)

### Week 3: Test Core Functionality

**Tasks:**
6. Test MediaProjection overlay system
7. Test screenshot capture
8. Test template matching pattern detection
   - Measure accuracy on 20+ diverse charts
   - Document platforms that work/fail
   - Establish baseline metrics
9. Test OCR indicator extraction
   - Verify RSI, MACD, volume extraction works
   - Measure accuracy across different chart styles
10. Test QuantraBot AI responses
11. Test current QuantraScore calculation

**Deliverable:** Baseline performance report with metrics

**Estimated Time:** 8-12 hours

### Week 4: Validate & Document

**Tasks:**
12. Create test chart dataset (50-100 screenshots)
   - Multiple platforms (TradingView, Robinhood, Webull)
   - Various timeframes (1min, 5min, 1hr, daily)
   - Different patterns
   - Different chart styles (candlestick, line, bar)
13. Run systematic accuracy tests
14. Document weaknesses
15. Decide: Is template matching salvageable or must it be replaced?

**Deliverable:** Go/no-go decision on geometric detection necessity

**Estimated Time:** 6-10 hours

**Phase 0 Total:** 24-42 hours

## Phase 1: Geometric Detection Foundation (2-3 weeks)

**Prerequisites:** Phase 0 complete, decision made to proceed with Apex intelligence

### Week 5-6: Implement Core Detection

**Tasks:**
1. Create `GeometricPatternDetector` architecture
2. Implement `PeakTroughFinder` using OpenCV
3. Implement `TrendlineDetector`
4. Build pattern rule engine
5. Implement 5 core patterns:
   - Head & Shoulders
   - Double Top/Bottom
   - Ascending/Descending Triangle
   - Support/Resistance
   - Flag/Pennant

**Deliverable:** Working geometric detection for 5 patterns

**Estimated Time:** 12-18 hours

### Week 7: Expand & Test

**Tasks:**
6. Add 5 more patterns (total 10)
7. Test on diverse chart dataset
8. Measure accuracy vs template matching
9. Iterate on pattern rules to improve precision
10. Optimize performance (target <1 second per scan)

**Deliverable:** 10-pattern detector with 70%+ accuracy

**Estimated Time:** 10-14 hours

**Phase 1 Total:** 22-32 hours

## Phase 2: Trait & Protocol System (2-3 weeks)

### Week 8-9: Trait System

**Tasks:**
1. Implement `TraitExtractor`
2. Convert geometric patterns → traits
3. Integrate existing OCR indicators → traits
4. Implement `MicrotraitExpander`
5. Define microtrait expansion rules for 10 patterns
6. Create `MicrotraitWeightsConfig` (JSON)

**Deliverable:** Working trait/microtrait system

**Estimated Time:** 10-15 hours

### Week 9-10: Protocol Stack

**Tasks:**
7. Create `Protocol` interface
8. Implement `ProtocolEngine`
9. Build 10 essential protocols:
   - P01 - Input Validation
   - P03 - Momentum Alignment
   - P05 - Volume Confirmation
   - P07 - Entropy Controller
   - P13 - Suppression Check
   - P15 - Drift Adjustment
   - P17 - Continuation Validator
   - P19 - Risk/Reward Validator
   - P20 - Final Verdict
   - (1 more based on needs)
10. Test protocol execution flow

**Deliverable:** Functional protocol stack

**Estimated Time:** 12-18 hours

**Phase 2 Total:** 22-33 hours

## Phase 3: Enhanced Scoring & Support Systems (1-2 weeks)

### Week 11: QuantraScore & Support

**Tasks:**
1. Implement `EntropyCalculator`
2. Build `SuppressionMemory` with Room database
3. Implement `DriftTracker` (integrates with existing `PatternLearningEngine`)
4. Rebuild `QuantraScoreCalculator` with Apex methodology
5. Test full scoring pipeline

**Deliverable:** Enhanced QuantraScore with validation

**Estimated Time:** 8-12 hours

### Week 12: Proof Logging

**Tasks:**
6. Implement `ProofLogger`
7. Add SHA-256 hashing for audit trail
8. Create proof log database schema
9. Test verification system

**Deliverable:** Working proof logging

**Estimated Time:** 6-8 hours

**Phase 3 Total:** 14-20 hours

## Phase 4: Explanation & UI (1-2 weeks)

### Week 13: Explanation System

**Tasks:**
1. Build `TemplateExplanationGenerator`
2. Create explanation templates for common scenarios
3. Implement hybrid routing logic
4. (Optional) Integrate Gemma 2B LLM
   - Download model (~800MB)
   - Add TFLite inference
   - Test performance on device
5. Test explanation quality

**Deliverable:** Hybrid explanation system (templates minimum, LLM optional)

**Estimated Time:** 8-12 hours

### Week 14: UI Enhancement

**Tasks:**
6. Update scan result screen to show:
   - Detected traits
   - Microtrait contributions
   - Protocol verdicts
   - Entropy/suppression/drift metrics
   - Score breakdown
7. Add protocol trace viewer
8. Enhance explanation display
9. Test user experience

**Deliverable:** Apex-powered UI

**Estimated Time:** 8-10 hours

**Phase 4 Total:** 16-22 hours

## Phase 5: Cleanup & Polish (1 week)

### Week 15: System Cleanup

**Tasks:**
1. Remove DevBot system
   - Delete `DiagnosticKnowledgeBase.kt`
   - Remove DevBot UI screen
   - Remove 6th navigation tab
   - Clean up DEBUG conditionals
2. Remove template matching
   - Delete 109 PNG images
   - Remove `TemplateLibrary.kt`
   - Clean up template detection code
3. Update documentation
4. Code cleanup and optimization

**Deliverable:** Clean, production-ready codebase

**Estimated Time:** 6-10 hours

**Phase 5 Total:** 6-10 hours

## Phase 6: Testing & Validation (1-2 weeks)

### Week 16-17: Comprehensive Testing

**Tasks:**
1. End-to-end testing on diverse charts
2. Accuracy validation (70-85% target)
3. Performance benchmarking
   - Scan time <3 seconds
   - Memory usage acceptable
   - Battery impact minimal
4. Bug fixes
5. Edge case handling
6. User acceptance testing (if possible)

**Deliverable:** Validated, production-ready app

**Estimated Time:** 10-15 hours

**Phase 6 Total:** 10-15 hours

## Total Timeline Summary

| Phase | Duration | Hours |
|-------|----------|-------|
| Phase 0: Foundation | 2-4 weeks | 24-42 |
| Phase 1: Geometric Detection | 2-3 weeks | 22-32 |
| Phase 2: Traits & Protocols | 2-3 weeks | 22-33 |
| Phase 3: Scoring & Support | 1-2 weeks | 14-20 |
| Phase 4: Explanation & UI | 1-2 weeks | 16-22 |
| Phase 5: Cleanup | 1 week | 6-10 |
| Phase 6: Testing | 1-2 weeks | 10-15 |
| **TOTAL** | **10-17 weeks** | **114-174 hours** |

**Optimistic:** 10 weeks, 114 hours (perfect execution, no major issues)  
**Realistic:** 14 weeks, 144 hours (normal development pace)  
**Pessimistic:** 17 weeks, 174 hours (unexpected complications)

## Session-Based Planning

**If working 2-3 sessions/week, 3 hours/session:**
- **Optimistic:** 10 weeks × 9 hours/week = 90 hours (aggressive pace)
- **Realistic:** 14 weeks × 7 hours/week = 98 hours (sustainable)
- **Pessimistic:** 17 weeks × 6 hours/week = 102 hours (relaxed pace with interruptions)

**Reality Check:** Budget 3-4 months of consistent part-time work.

## Critical Dependencies

**Must Have:**
1. Consistent desktop access (NucBox K6)
2. 2-3 hour uninterrupted sessions
3. Real-time AI debugging support via GitHub
4. Test device (Samsung S23 FE) available during sessions
5. 50-100 diverse chart screenshots for testing

**Nice to Have:**
- Access to multiple trading platforms for screenshot collection
- Real trader feedback during development
- Market data for validation testing

## Risk Mitigation

**Risk: Geometric Detection Accuracy < 70%**
- Mitigation: Start with 5 patterns, validate before expanding
- Fallback: Hybrid approach (geometric + improved template matching)

**Risk: Development Time Exceeds Estimate**
- Mitigation: Phase-based approach allows stopping at minimum viable product
- Minimum: Phase 0-2 (foundation + basic geometric detection)

**Risk: Desktop Access Inconsistent**
- Mitigation: Each phase is self-contained, can pause/resume
- Track progress carefully to minimize context-switching overhead

**Risk: Market Validation Fails**
- Mitigation: Build small increments, validate concepts before full commitment
- Early user testing after Phase 2

## Success Criteria

**Technical:**
- [ ] App compiles and runs without crashes
- [ ] Pattern detection accuracy ≥ 70%
- [ ] QuantraScore reliability validated
- [ ] Scan-to-result time < 3 seconds (templates) or < 30 seconds (LLM)
- [ ] Explanation quality: clear and actionable
- [ ] Battery impact: minimal during passive use

**User Experience:**
- [ ] Intuitive UI that shows Apex intelligence
- [ ] Clear explanations traders can understand
- [ ] Trustworthy results that build confidence

**Market (Unknown):**
- [ ] Traders willing to test the app
- [ ] Positive feedback on accuracy and usefulness
- [ ] Willingness to pay $50-200 for product

## Go/No-Go Checkpoints

**After Phase 0:** 
- If current system is >60% accurate, consider incremental improvements vs full rebuild
- If <40% accurate, proceed with geometric detection

**After Phase 1:**
- If geometric detection <60% accurate, reassess approach
- If ≥70% accurate, full steam ahead

**After Phase 2:**
- Validate that trait/protocol system adds value
- If overly complex with minimal benefit, simplify

**After Phase 4:**
- User testing checkpoint
- If feedback negative, halt before final polish

## Honest Assessment

**This roadmap assumes:**
- Perfect execution (unlikely)
- No major architectural issues (unlikely)
- Consistent desktop availability (uncertain)
- 2-3 focused sessions per week (challenging with job)
- Real-time AI debugging support (requires internet during sessions)

**Reality:**
- First-time Android development has unexpected issues
- Debugging takes longer than coding
- Life interruptions will extend timeline
- Motivation fluctuates

**Realistic expectation:** 4-6 months if truly committed, longer if desktop time remains inconsistent.

## Alternative: Minimum Viable Product (MVP)

**If full roadmap too ambitious:**

**MVP Scope (2-3 months):**
- Phase 0: Get current app working
- Phase 1 (partial): 5 geometric patterns only
- Basic trait extraction (no microtraits)
- 5 essential protocols
- Template explanations only (no LLM)
- Keep DevBot and template matching for now

**MVP Deliverable:** Working app with improved pattern detection (geometric + template hybrid), basic Apex validation, professional explanations

**MVP Timeline:** 6-10 weeks, 60-90 hours

**Decision:** Start with MVP, prove concept, then expand if successful

## Next Steps (If Resuming)

1. **Immediate (Week 1):**
   - Set aside first 3-hour desktop session
   - Open project in Android Studio
   - Document all compilation errors
   - Push error log to GitHub
   - Request AI fixes

2. **Short-term (Weeks 2-4):**
   - Fix all blocking issues
   - Get app launching on device
   - Test basic functionality
   - Create baseline performance report

3. **Medium-term (Weeks 5-8):**
   - Decide MVP vs full Apex
   - Implement geometric detection (5-10 patterns)
   - Validate accuracy
   - Go/no-go decision

4. **Long-term (Weeks 9-17):**
   - Build out full Apex intelligence
   - Polish UI/UX
   - User testing
   - Production release

## Final Note

This roadmap documents the path forward **if development resumes**. Current project status is on hold due to limited desktop access and uncertain market validation. The plan preserves the vision while being realistic about constraints.

**Success is not guaranteed.** This is a moonshot with significant time investment required and uncertain ROI. Proceed only if:
- Desktop time genuinely improves
- Passionate about the vision beyond financial return
- Willing to commit 4-6 months of part-time work
- Prepared for possibility it doesn't succeed in market

Otherwise, it's okay to leave this documented and move on.
