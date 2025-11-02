package com.lamontlabs.quantravision.ui.screens.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import com.lamontlabs.quantravision.onboarding.OnboardingManager
import com.lamontlabs.quantravision.onboarding.OnboardingState
import com.lamontlabs.quantravision.onboarding.OnboardingStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OnboardingViewModel(context: Context) : ViewModel() {
    
    private val onboardingManager = OnboardingManager.getInstance(context)
    
    private val _state = MutableStateFlow(onboardingManager.getState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()
    
    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()
    
    fun nextStep() {
        val currentState = _state.value
        if (!currentState.isLastStep()) {
            onboardingManager.advanceToNextStep()
            _state.value = onboardingManager.getState()
            _currentStepIndex.value = _state.value.currentStep.ordinal
        } else {
            completeOnboarding()
        }
    }
    
    fun skipOnboarding() {
        onboardingManager.skipOnboarding()
        _state.value = onboardingManager.getState()
    }
    
    fun completeOnboarding() {
        onboardingManager.completeOnboarding()
        _state.value = onboardingManager.getState()
    }
    
    fun goToStep(index: Int) {
        _currentStepIndex.value = index
    }
    
    fun resetOnboarding() {
        onboardingManager.resetOnboarding()
        _state.value = onboardingManager.getState()
        _currentStepIndex.value = 0
    }
}
