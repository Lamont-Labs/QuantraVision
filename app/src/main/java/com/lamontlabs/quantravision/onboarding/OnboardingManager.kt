package com.lamontlabs.quantravision.onboarding

import android.content.Context
import android.util.Log

class OnboardingManager(private val context: Context) {
    
    private val TAG = "OnboardingManager"
    
    fun getState(): OnboardingState {
        return OnboardingState.load(context)
    }
    
    fun hasCompletedOnboarding(): Boolean {
        return getState().isCompleted
    }
    
    fun markStepComplete(step: OnboardingStep) {
        val currentState = getState()
        val updatedCompletedSteps = currentState.completedSteps + step.ordinal
        val newState = currentState.copy(
            completedSteps = updatedCompletedSteps
        )
        OnboardingState.save(context, newState)
        Log.d(TAG, "Marked step ${step.title} as complete")
    }
    
    fun getNextStep(): OnboardingStep? {
        val currentState = getState()
        if (currentState.isCompleted) {
            return null
        }
        
        val currentOrdinal = currentState.currentStep.ordinal
        val allSteps = OnboardingStep.all()
        
        return if (currentOrdinal < allSteps.size - 1) {
            allSteps[currentOrdinal + 1]
        } else {
            null
        }
    }
    
    fun advanceToNextStep() {
        val currentState = getState()
        val nextStep = getNextStep()
        
        if (nextStep != null) {
            val newState = currentState.copy(
                currentStep = nextStep,
                completedSteps = currentState.completedSteps + currentState.currentStep.ordinal
            )
            OnboardingState.save(context, newState)
            Log.d(TAG, "Advanced to step: ${nextStep.title}")
        } else {
            completeOnboarding()
        }
    }
    
    fun skipOnboarding() {
        val allSteps = OnboardingStep.all()
        val allOrdinals = allSteps.map { it.ordinal }.toSet()
        val newState = OnboardingState(
            currentStep = OnboardingStep.PRO_FEATURES,
            isCompleted = true,
            completedSteps = allOrdinals
        )
        OnboardingState.save(context, newState)
        Log.d(TAG, "Onboarding skipped")
    }
    
    fun completeOnboarding() {
        val currentState = getState()
        val allSteps = OnboardingStep.all()
        val allOrdinals = allSteps.map { it.ordinal }.toSet()
        val newState = currentState.copy(
            isCompleted = true,
            completedSteps = allOrdinals
        )
        OnboardingState.save(context, newState)
        Log.d(TAG, "Onboarding completed")
    }
    
    fun resetOnboarding() {
        val newState = OnboardingState(
            currentStep = OnboardingStep.WELCOME,
            isCompleted = false,
            completedSteps = emptySet()
        )
        OnboardingState.save(context, newState)
        Log.d(TAG, "Onboarding reset")
    }
    
    companion object {
        @Volatile
        private var INSTANCE: OnboardingManager? = null
        
        fun getInstance(context: Context): OnboardingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OnboardingManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
