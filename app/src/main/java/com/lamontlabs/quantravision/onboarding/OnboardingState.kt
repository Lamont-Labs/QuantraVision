package com.lamontlabs.quantravision.onboarding

import android.content.Context
import android.content.SharedPreferences

sealed class OnboardingStep(val ordinal: Int, val title: String, val description: String) {
    object WELCOME : OnboardingStep(
        ordinal = 0,
        title = "Welcome to QuantraVision",
        description = "AI-powered pattern detection for smarter trading"
    )
    
    object DETECTION : OnboardingStep(
        ordinal = 1,
        title = "Pattern Detection",
        description = "Detect 100+ chart patterns instantly with AI"
    )
    
    object INTELLIGENCE : OnboardingStep(
        ordinal = 2,
        title = "Intelligence Features",
        description = "4 advanced features for better trading decisions"
    )
    
    object EDUCATION : OnboardingStep(
        ordinal = 3,
        title = "Learn & Grow",
        description = "25 interactive lessons + comprehensive trading book"
    )
    
    object PRO_FEATURES : OnboardingStep(
        ordinal = 4,
        title = "Choose Your Plan",
        description = "One-time payment • Lifetime access • No subscriptions"
    )
    
    companion object {
        fun fromOrdinal(ordinal: Int): OnboardingStep = when (ordinal) {
            0 -> WELCOME
            1 -> DETECTION
            2 -> INTELLIGENCE
            3 -> EDUCATION
            4 -> PRO_FEATURES
            else -> WELCOME
        }
        
        fun all(): List<OnboardingStep> = listOf(
            WELCOME, DETECTION, INTELLIGENCE, EDUCATION, PRO_FEATURES
        )
    }
}

data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
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
        
        fun load(context: Context): OnboardingState {
            val prefs = getPrefs(context)
            val currentStepOrdinal = prefs.getInt(KEY_CURRENT_STEP, 0)
            val isCompleted = prefs.getBoolean(KEY_IS_COMPLETED, false)
            val completedStepsString = prefs.getString(KEY_COMPLETED_STEPS, "") ?: ""
            val completedSteps = if (completedStepsString.isNotEmpty()) {
                completedStepsString.split(",").mapNotNull { it.toIntOrNull() }.toSet()
            } else {
                emptySet()
            }
            
            return OnboardingState(
                currentStep = OnboardingStep.fromOrdinal(currentStepOrdinal),
                isCompleted = isCompleted,
                completedSteps = completedSteps
            )
        }
        
        fun save(context: Context, state: OnboardingState) {
            val prefs = getPrefs(context)
            prefs.edit()
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
