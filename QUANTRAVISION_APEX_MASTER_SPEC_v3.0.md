QUANTRAVISION_APEX_MASTER_SPEC_v3.0_WITH_BATCHED_BUILD_NOTICE:
  product: "QuantraVision Apex™"
  engine: "QuantraCore Apex™"
  owner: "Lamont Labs"
  version: "3.0"
  purpose: >
    Full unified specification of QuantraVision Apex™ hybrid local-vision + Apex-logic
    + optional cloud narration system, including:
    architecture, models, overlays, UI/UX, tiers/quotas, LLM contract/validator,
    local summaries, legal/privacy/TOS, Google Play compliance, and Replit batched build plan.

  # ============================================================
  # 0. RUNTIME ARCHITECTURE — CANONICAL PIPELINE
  # ============================================================
  runtime_chain:
    canonical_flow:
      - "Screen Capture → Local Vision Model → Primitive Extraction → Apex Engine → Overlay Rendering → Quota Gate → (Optional Cloud Reasoning) → Validator → Copilot Output"
    deterministic: true
    fail_closed: true
    strictly_local_vision: true
    cloud_only_narration: true
    never_send_images_to_cloud: true

  # ============================================================
  # 1. LOCAL VISION MODELS (ON DEVICE)
  # ============================================================
  local_models:
    primary_mobile_detector:
      name: "YOLOv8n or YOLOv5n (TFLite/NNAPI)"
      size_mb: "5–12"
      license: "Permissive"
      role:
        - "Detect chart window"
        - "Detect candlestick bodies/wicks"
        - "Detect drawn lines/indicators"
    secondary_segmenter:
      name: "MobileSAM-tiny or FastSAM-s (TFLite)"
      size_mb: "8–20"
      license: "Permissive"
      role:
        - "Segment candles vs background"
        - "Isolate zones/blocks"
    line_edge_extractor:
      name: "OpenCV Canny + Hough"
      license: "BSD"
      role:
        - "Extract trendlines"
        - "Detect channels/triangles/flags"
    optional_ocr:
      name: "MLKit Text Recognition OR Tesseract-lite"
      license: "Apache 2.0 / Open"
      role:
        - "Read ticker"
        - "Read timeframe"

  local_llm_optional:
    note: >
      Optional offline narrative helper for paid tiers only. Free tier stays deterministic-summary-only.
    candidates:
      phi_3_5_mini:
        name: "Phi-3.5-mini (gguf 4-bit)"
        license: "Permissive"
        role: "Short offline narration fallback"
        ram_gb: "1.5–2.5"
      qwen2_7b:
        name: "Qwen2-7B-Instruct (gguf 4-bit)"
        license: "Permissive"
        role: "Ultra offline narrative fallback"
        ram_gb: "4–6"
    routing:
      if_cloud_blocked_and_user_is_paid: "may_use_local_llm"
      otherwise: "use_local_deterministic_summaries_only"

  # ============================================================
  # 2. CLOUD LLM MODELS (PAID ONLY)
  # ============================================================
  cloud_models:
    pro:
      model: "gpt-4.1-mini OR grok-fast"
      tokens_out_max: 180
      role:
        - "Narrate Apex verdict"
        - "Explain gates/invalidation"
    ultra:
      model: "gpt-4.1 OR grok-fast"
      tokens_out_max: 380
      role:
        - "Deeper Apex narration"
        - "Monster Runner textual reasoning"
    forbidden:
      - "cloud vision models"
      - "sending screenshots"
    send_to_cloud: "structured Apex packets only"

  # ============================================================
  # 3. APEX ENGINE (MOBILE PORT)
  # ============================================================
  apex_engine:
    quantrascore:
      scale: "0–100 integer"
      normalization: "final_score = int(clamp(raw_score * 100, 0, 100))"
      bands:
        FAIL: "0–49"
        WAIT: "50–69"
        PASS: "70–84"
        STRONG_PASS: "85–100"
    protocols:
      tier_protocols_count: 80
      learning_protocols_count: 25
      omega_protocols_count: 4
      strict_order: true
    overrides:
      - "omega_lock → status OMEGA"
      - "suppression_active → status SUPPRESSED (unless Omega)"
      - "entropy_high → WAIT/FAIL override"

  # ============================================================
  # 4. OVERLAY SYSTEM (SOFT INSTITUTIONAL)
  # ============================================================
  overlays:
    palette:
      teal: "#30D6F5"
      amber: "#FFB237"
      violet: "#9A76FF"
      red: "#E84545"
      core_blue: "#0B1E33"
      deep_graphite_black: "#020508"
      neutral_steel: "#A7B4C3"
    rules:
      PASS_high:
        line: "solid teal"
        width_px: 2.5
        opacity: 0.70
        glow_px: 1
      PASS_mid:
        line: "solid light teal"
        width_px: 2.0
        opacity: 0.70
        glow_px: 0
      WAIT:
        line: "amber dashed"
        width_px: 1.6
        opacity: 0.60
        dash_pattern_px: [8, 8]
      FAIL:
        overlays: "hidden/fade"
        fadeout_ms: 400
      SUPPRESSED:
        line: "violet broken"
        width_px: 1.8
        opacity: 0.55
        hatch_fill_opacity: 0.12
      OMEGA:
        disable_overlays: true
        banner_text: "APEX OMEGA SAFETY: ANALYSIS DISABLED."
    auto_dim:
      enabled: true
      triggers:
        - "OS night mode"
        - "ambient light low"
        - "brightness < 30%"
        - "blue light filter"
      effects:
        brightness_drop_pct: 30
        saturation_drop_pct: 12
        glow_disabled: true
        transition_ms: 350

  # ============================================================
  # 5. FLOATING Q TOGGLE
  # ============================================================
  q_toggle:
    icon: "Metallic Q only (transparent background, no border)"
    size_dp: 48
    states:
      idle:
        opacity: 0.70
        tint: "metallic blue"
      active:
        opacity: 1.0
        tint: "teal"
        pulse: true
      error:
        flash_red_ms: 1000
        shake_px: 4
    gestures:
      tap: "toggle scanning"
      long_press: "open AI Drawer"
      double_tap: "cycle mode Vision/Trend/Predict"

  # ============================================================
  # 6. TIERS + QUOTAS (FINAL)
  # ============================================================
  tiers:
    FREE:
      price_usd_month: 0
      cloud_calls_per_day: 0
      features:
        - "Unlimited local scan + overlays"
        - "Local Apex verdict + QuantraScore"
        - "Deterministic local summaries"
      locked:
        - "Cloud reasoning"
        - "Copilot"
        - "Monster Runner narration"
    PRO:
      price_usd_month: 4.99
      cloud_calls_per_day: 10
      features:
        - "Cloud Apex narration (short)"
        - "Auto-Explain optional"
        - "Multi-frame logic narration"
        - "Sector context narration"
    APEX_ULTRA:
      price_usd_month: 9.99
      cloud_calls_per_day: 25
      features:
        - "Cloud Apex narration (deep)"
        - "Monster Runner reasoning (text)"
        - "Batch snapshots 3/day"
        - "Local PDF daily summary"
        - "Auto-Explain ON for WAIT by default"

  quota_gate:
    reset:
      at_local_midnight: true
      no_double_reset_hours: 20
      backward_clock_guard_hours: 2
      reset_lockout_hours_if_guard_tripped: 24
    rate_limits:
      min_seconds_between_calls: 8
      max_calls_per_60_seconds: 3
    upgrade_rules:
      upgrade_midday:
        - "tier switches instantly"
        - "calls_today carries over"
        - "new limit applies"
      expiry_midday:
        - "tier reverts to FREE"
        - "cloud blocked immediately"

  # ============================================================
  # 7. AUTO-EXPLAIN MATRIX
  # ============================================================
  auto_explain:
    eligibility: "PRO and APEX_ULTRA only"
    global_preconditions:
      - "omega_lock == false"
      - "suppression_active == false"
      - "entropy_score <= 0.60"
      - "calls_today < limit"
      - "chart_health.frame_ok == true"
    triggers:
      WAIT:
        confidence_min: 0.55
        action: "auto_explain"
      PASS_mid_conf_low_entropy:
        confidence_range: [0.55, 0.80)
        entropy_max: 0.30
        user_toggle_required: true
        action: "auto_explain"
    never_for:
      - "FAIL"
      - "SUPPRESSED"
      - "OMEGA"
      - "ENTROPY_HIGH"

  # ============================================================
  # 8. LLM CONTRACT + VALIDATOR
  # ============================================================
  llm_contract:
    role: "ApexNarrator"
    authority: "ApexVerdictIsFinal"
    forbidden_words:
      - "buy"
      - "sell"
      - "long"
      - "short"
      - "enter"
      - "exit"
      - "stop loss"
      - "take profit"
      - "target price"
      - "prediction"
      - "forecast"
      - "signal"
      - "trade setup"
    input_schema:
      fields:
        - "scan_id"
        - "tier"
        - "status"
        - "quantra_score_0_100"
        - "confidence_apex"
        - "entropy_score"
        - "suppression_active"
        - "regime_ok"
        - "pattern_candidates[]"
        - "primitives_summary{}"
        - "trace_top[]"
        - "invalidation_points[]"
    output_schema_json:
      required_fields:
        - "scan_id"
        - "status_echo"
        - "headline"
        - "what_was_seen"
        - "why_apex_said_this"
        - "conditions_to_watch"
        - "invalidation_triggers"
        - "risk_caveats"
        - "confidence_statement"
        - "next_scan_suggestion"
    validator_rules:
      - "status_echo must match input status"
      - "no invented patterns/labels"
      - "reject forbidden words"
      - "exact schema match"
      - "tier token caps enforced"
    fallback_on_violation: "use local summary; still count call"

  # ============================================================
  # 9. LOCAL SUMMARY ENGINE
  # ============================================================
  local_summaries:
    universal_header:
      - "Apex Verdict: {status}"
      - "QuantraScore: {score}/100"
      - "Confidence: {confidence_apex%}"
      - "Entropy: {entropy_score%}"
      - "Regime: {OK|MISMATCH}"
    templates:
      PASS:
        - "Structure confirmed: {trend/continuation/breakout}."
        - "Volume/volatility alignment: {volume_state}/{volatility_state}."
        - "Top gates: {trace_top[0..1]}."
        - "Overlay: solid teal at approved anchors."
        - "Invalidation: {invalidation_points[0..1]}."
      WAIT:
        - "Early structure detected; not confirmed."
        - "Primary blocker: {trace_top[0]}."
        - "Overlay: dashed amber ghost geometry."
        - "Confirm if: {confirm_conditions[0..1]}."
        - "Breaks if: {invalidation_points[0]}."
      FAIL:
        - "Candidate rejected due to conflict/entropy/regime mismatch."
        - "Blocking gates: {trace_top[0..1]}."
        - "Overlay: none/fade."
      SUPPRESSED:
        - "Detected but suppressed by Apex memory."
        - "Suppression cause: {trace_top[0]}."
        - "Overlay: faint violet broken geometry."
      OMEGA:
        - "Apex Omega Safety Lock active."
        - "Reason: {omega_reason}."
        - "Overlays disabled; cloud disabled."
        - "Fix via Settings → Health Check."

  # ============================================================
  # 10. LEGAL + COMPLIANCE (FULL)
  # ============================================================
  legal_suite:
    master_disclaimer: >
      QuantraVision Apex™ and QuantraCore Apex™ are analytical and educational tools only.
      They do not provide financial, investment, trading, or securities advice. Outputs,
      overlays, scores, explanations, and Apex reasoning are algorithmic interpretations and
      must not be interpreted as recommendations to buy, sell, enter, exit, or take any position.
      All decisions are solely your responsibility. Past performance does not guarantee future results.
      Lamont Labs is not a registered investment advisor, broker-dealer, or CTA. Use is "as-is" without warranty.
      Lamont Labs is not liable for losses or damages arising from use.

    app_store_disclaimer_short: >
      Not financial advice. Educational chart analysis only. Trading involves risk.

    first_launch_modal_text: >
      QuantraVision Apex™ is not a trading system or financial advisor. Outputs are for educational
      purposes only and must not be interpreted as trade recommendations. By continuing, you accept
      full responsibility for decisions and agree Lamont Labs is not liable for financial outcomes.

    ai_explanation_warning_text: >
      AI explanations narrate Apex Engine reasoning for educational use only.
      They are not trading instructions or advice. Never trade solely on AI explanations.

    quantrascore_disclosure_text: >
      QuantraScore (0–100) is internal Apex confidence in detected structure, not a prediction.

    monster_runner_disclaimer_text: >
      Monster Runner analysis is experimental and probabilistic; not a forecast or signal.

    llm_safety_disclaimer_text: >
      Cloud AI narration may be imperfect and cannot evaluate your risk tolerance or financial situation.

    privacy_disclosure_text: >
      Screen captures are processed locally on-device. No screenshots or images are transmitted to cloud.
      Only anonymized text-based Apex packets may be sent for paid-tier narration. No brokerage or portfolio data is collected.

    liability_shield_text: >
      TO THE MAXIMUM EXTENT PERMITTED BY LAW, LAMONT LABS DISCLAIMS ALL LIABILITY FOR LOSSES OR DAMAGES.
      USE IS ENTIRELY AT YOUR OWN RISK.

    no_regulatory_designation_text: >
      Lamont Labs is not a broker-dealer, registered investment advisor (RIA), commodity trading advisor (CTA),
      portfolio manager, or financial professional. Software is not intended for regulated financial use.

    copilot_disclaimer_text: >
      Copilot summarizes chart context only. It cannot recommend trades or assess position sizing or risk.

    anti_improper_use_text: >
      Do not use this software for leveraged or automated trading decisions. Consult a licensed professional.

    terms_of_service_full_text: |
      TERMS OF SERVICE — QUANTRAVISION APEX™
      ---------------------------------------------------
      1. ACCEPTANCE OF TERMS
      By using QuantraVision Apex™, you agree to these Terms. If you do not agree, discontinue use.

      2. LICENSE
      Lamont Labs grants a limited, nonexclusive, non-transferable license to use the app for personal,
      educational chart analysis only. Commercial redistribution is prohibited.

      3. NO FINANCIAL ADVICE
      QuantraVision Apex™ is not a trading system, investment advisor, or signal provider. Outputs
      must not be interpreted as recommendations.

      4. SUBSCRIPTION TERMS
      Paid plans are billed through Google Play. Subscription changes, refunds, and cancellations
      follow Google Play policies.

      5. CLOUD USAGE
      Only anonymized Apex packets (text metadata) are sent to cloud services for narration.
      No screenshots or financial data are transmitted.

      6. USER RESPONSIBILITIES
      You assume full responsibility for trading decisions and acknowledge market risk.

      7. RISK NOTICE
      Markets are volatile and can cause substantial loss. Past performance does not predict future outcomes.

      8. LIMITATION OF LIABILITY
      To the fullest extent permitted by law, Lamont Labs disclaims liability for damages, losses, or claims.

      9. PROHIBITED USES
      Includes: reverse engineering; using outputs for automated trading; providing financial services;
      reselling access; circumventing quotas or billing.

      10. ARBITRATION
      Disputes resolved via binding arbitration in the user's jurisdiction, to the maximum extent permitted by law.

      11. TERMINATION
      Lamont Labs may revoke access for misuse or violation.

      12. CHANGES
      Terms may update; continued use constitutes agreement.

    privacy_policy_full_text: |
      PRIVACY POLICY — QUANTRAVISION APEX™
      ---------------------------------------------------
      1. DATA WE COLLECT
      - On-device screen capture (processed locally, not uploaded)
      - Anonymized Apex packets for cloud narration (paid tiers only)
      - Subscription and Google Play purchase tokens
      - Anonymous usage analytics

      2. DATA WE DO NOT COLLECT
      - Brokerage credentials
      - Trades or portfolio data
      - Personal identifying information beyond platform billing
      - GPS/location data

      3. DATA USE
      - Provide overlays and Apex reasoning
      - Enforce subscription quotas
      - Improve stability and performance

      4. DATA SHARING
      No data sharing, except encrypted narration requests to cloud LLM for paid tiers.

      5. USER RIGHTS
      Users may request deletion of analytics data.

      6. CHILDREN
      Not intended for users under 18.

      7. SECURITY
      Local-only vision processing; encrypted communication for cloud text packets.

      8. CHANGES
      Policy may update. Continued use means acceptance.

    google_play_data_safety:
      collected:
        - "subscription status"
        - "anonymous usage metrics"
      shared: []
      processed_locally:
        - "screen images"
        - "vision outputs"
        - "apex analysis"
      sent_to_cloud:
        - "Apex structured packets (text only)"
      encryption_in_transit: true
      deletion_requests_supported: true

    dmca_policy:
      email: "legal@lamontlabs.ai"

    refund_policy:
      note: "Refunds handled through Google Play only."

    arbitration_clause:
      text: >
        Any dispute arising from use shall be resolved by binding arbitration in the user's state/country,
        to the maximum extent permitted by law.

  # ============================================================
  # 11. ANDROID REPO MAP — REQUIRED FILES
  # ============================================================
  android_repo_map:
    package: "com.lamontlabs.quantravision"
    files_required:
      services:
        - "ScreenCaptureService.kt"
        - "QToggleService.kt"
      local_ai:
        - "LocalModelRunner.kt"
        - "PrimitiveExtractor.kt"
        - "OpenCVLineExtractor.kt"
        - "OCRTickerReader.kt"
      apex_engine_mobile:
        - "ApexEngineMobile.kt"
        - "ProtocolRegistryMobile.kt"
        - "TierProtocolsMobile/T01.kt .. T80.kt"
        - "LearningProtocolsMobile/LP01.kt .. LP25.kt"
        - "OmegaProtocolsMobile/Omega01.kt .. Omega04.kt"
        - "QuantraScoreMobile.kt"
        - "ProofHasher.kt"
      overlays_ui:
        - "OverlayRenderer.kt"
        - "overlay_layout.xml"
        - "drawer_layout.xml"
        - "colors.xml"
        - "theme.xml"
      cloud:
        - "QuotaGate.kt"
        - "CloudReasoner.kt"
        - "LLMContractValidator.kt"
      billing_and_settings:
        - "BillingManager.kt"
        - "SubscriptionActivity.kt"
        - "SettingsActivity.kt"
        - "AboutActivity.kt"
        - "OnboardingActivity.kt"
        - "HealthCheck.kt"
        - "AIDrawerFragment.kt"
      assets:
        - "local_model.tflite"
        - "class_labels.json"
        - "protocol_thresholds.json"
      docs:
        - "QUANTRAVISION_APEX_MASTER_SPEC_v3.0.md"
        - "llm_contract.md"
        - "quota_logic.md"
        - "local_summaries.md"
        - "TermsOfService.md"
        - "PrivacyPolicy.md"

  # ============================================================
  # 12. REPLIT — BATCHED BUILD NOTICE (EMBEDDED)
  # ============================================================
  replit:
    must_follow_master_spec: true
    no_feature_invention: true
    no_path_renames: true

    replit_batched_build_notice:
      intent: >
        This build will be delivered in multiple sequential batches. Batch_0 locks architecture and skeleton.
        Later batches inject Apex Engine logic/protocols, then wiring and hardening.
      why_batches:
        - "Token/context stability: prevents truncation and drift."
        - "Engine purity: Apex logic stays a clean library."
        - "Deterministic verification per tranche."
      strict_obedience:
        - "Do not invent missing files; create stubs only if referenced early."
        - "Do not rename/refactor previous batches unless told later."
        - "Assume more logic/protocols are coming; leave seams stable."
      batch_sequence_canonical:
        - "batch_0_master_spec_lock"
        - "batch_1_shell_repo"
        - "batch_2_apex_core"
        - "batch_3_tier_protocols_T01_T20"
        - "batch_4_tier_protocols_T21_T40"
        - "batch_5_tier_protocols_T41_T60"
        - "batch_6_tier_protocols_T61_T80"
        - "batch_7_learning_protocols_LP01_LP25"
        - "batch_8_omega_protocols_Omega01_Omega04"
        - "batch_9_wiring_integration"
        - "batch_10_hardening_and_green_ci"
      completion_condition: >
        Build is complete only after batch_10 passes and repo is green with end-to-end demo mode.
