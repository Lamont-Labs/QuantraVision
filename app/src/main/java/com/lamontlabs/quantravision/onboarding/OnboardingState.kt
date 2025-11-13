package com.lamontlabs.quantravision.onboarding

import android.content.Context
import android.content.SharedPreferences
import com.lamontlabs.quantravision.ui.DisclaimerManager

sealed class OnboardingStep(val ordinal: Int, val title: String, val description: String) {
    object DISCLAIMER : OnboardingStep(
        ordinal = 0,
        title = "Legal Disclaimer",
        description = "Please read and accept to continue"
    )
    
    object WELCOME : OnboardingStep(
        ordinal = 1,
        title = "Welcome to QuantraVision",
        description = "AI-powered pattern detection for smarter trading"
    )
    
    object DETECTION : OnboardingStep(
        ordinal = 2,
        title = "Pattern Detection",
        description = "Detect 100+ chart patterns instantly with AI"
    )
    
    object INTELLIGENCE : OnboardingStep(
        ordinal = 3,
        title = "Intelligence Features",
        description = "4 advanced features for better trading decisions"
    )
    
    object EDUCATION : OnboardingStep(
        ordinal = 4,
        title = "Learn & Grow",
        description = "25 interactive lessons + comprehensive trading book"
    )
    
    object PRO_FEATURES : OnboardingStep(
        ordinal = 5,
        title = "Choose Your Plan",
        description = "One-time payment • Lifetime access • No subscriptions"
    )
    
    companion object {
        fun fromOrdinal(ordinal: Int): OnboardingStep = when (ordinal) {
            0 -> DISCLAIMER
            1 -> WELCOME
            2 -> DETECTION
            3 -> INTELLIGENCE
            4 -> EDUCATION
            5 -> PRO_FEATURES
            else -> DISCLAIMER
        }
        
        fun all(): List<OnboardingStep> = listOf(
            DISCLAIMER, WELCOME, DETECTION, INTELLIGENCE, EDUCATION, PRO_FEATURES
        )
    }
}

data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.DISCLAIMER,
    val isCompleted: Boolean = false,
    val completedSteps: Set<Int> = emptySet()
) {
    fun hasSeenStep(step: OnboardingStep): Boolean {
        return completedSteps.contains(step.ordinal)
    }
    
    fun isLastStep(): Boolean {
        return currentStep == OnboardingStep.PRO_FEATURES
    }
    
    fun getProgress(): Float {
        return (completedSteps.size + 1) / OnboardingStep.all().size.toFloat()
    }
    
    companion object {
        private const val PREFS_NAME = "qv_onboarding_state"
        private const val KEY_CURRENT_STEP = "current_step"
        private const val KEY_IS_COMPLETED = "is_completed"
        private const val KEY_COMPLETED_STEPS = "completed_steps"
        private const val KEY_SCHEMA_VERSION = "schema_version"
        private const val CURRENT_SCHEMA_VERSION = 2
        
        fun load(context: Context): OnboardingState {
            val prefs = getPrefs(context)
            val schemaVersion = prefs.getInt(KEY_SCHEMA_VERSION, 1)
            val currentStepOrdinal = prefs.getInt(KEY_CURRENT_STEP, 0)
            val isCompleted = prefs.getBoolean(KEY_IS_COMPLETED, false)
            val completedStepsString = prefs.getString(KEY_COMPLETED_STEPS, "") ?: ""
            val completedSteps = if (completedStepsString.isNotEmpty()) {
                completedStepsString.split(",").mapNotNull { it.toIntOrNull() }.toSet()
            } else {
                emptySet()
            }
            
            // MIGRATION: Only migrate if schema version is 1 (legacy) AND we have actual saved data
            val hasLegacyData = (schemaVersion == 1) && (currentStepOrdinal > 0 || completedSteps.isNotEmpty())
            val disclaimerAccepted = DisclaimerManager.isAccepted(context)
            
            val migratedCurrentStep = when {
                hasLegacyData && !disclaimerAccepted -> 0
                hasLegacyData && disclaimerAccepted -> currentStepOrdinal + 1
                else -> currentStepOrdinal
            }
            
            val migratedCompletedSteps = if (hasLegacyData) {
                completedSteps.map { it + 1 }.toSet()
            } else {
                completedSteps
            }
            
            val migratedIsCompleted = if (hasLegacyData) {
                isCompleted && disclaimerAccepted
            } else {
                isCompleted
            }
            
            return OnboardingState(
                currentStep = OnboardingStep.fromOrdinal(migratedCurrentStep),
                isCompleted = migratedIsCompleted,
                completedSteps = migratedCompletedSteps
            )
        }
        
        fun save(context: Context, state: OnboardingState) {
            val prefs = getPrefs(context)
            prefs.edit()
                .putInt(KEY_SCHEMA_VERSION, CURRENT_SCHEMA_VERSION)
                .putInt(KEY_CURRENT_STEP, state.currentStep.ordinal)
                .putBoolean(KEY_IS_COMPLETED, state.isCompleted)
                .putString(KEY_COMPLETED_STEPS, state.completedSteps.joinToString(","))
                .apply()
        }
        
        private fun getPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }
}
