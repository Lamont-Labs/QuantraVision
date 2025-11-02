package com.lamontlabs.quantravision.onboarding

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lamontlabs.quantravision.onboarding.OnboardingManager
import com.lamontlabs.quantravision.onboarding.OnboardingStep
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class OnboardingFlowTest {
    
    private lateinit var context: Context
    private lateinit var onboardingManager: OnboardingManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        onboardingManager = OnboardingManager.getInstance(context)
        onboardingManager.resetOnboarding()
    }
    
    @Test
    fun testInitialOnboardingState() {
        val state = onboardingManager.getState()
        
        assertFalse(state.isCompleted)
        assertEquals(OnboardingStep.WELCOME, state.currentStep)
        assertTrue(state.completedSteps.isEmpty())
    }
    
    @Test
    fun testAdvanceToNextStep() {
        onboardingManager.advanceToNextStep()
        
        val state = onboardingManager.getState()
        assertEquals(OnboardingStep.DETECTION, state.currentStep)
    }
    
    @Test
    fun testMarkStepComplete() {
        onboardingManager.markStepComplete(OnboardingStep.WELCOME)
        
        val state = onboardingManager.getState()
        assertTrue(state.hasSeenStep(OnboardingStep.WELCOME))
    }
    
    @Test
    fun testCompleteOnboarding() {
        onboardingManager.completeOnboarding()
        
        val state = onboardingManager.getState()
        assertTrue(state.isCompleted)
        assertTrue(onboardingManager.hasCompletedOnboarding())
    }
    
    @Test
    fun testSkipOnboarding() {
        onboardingManager.skipOnboarding()
        
        val state = onboardingManager.getState()
        assertTrue(state.isCompleted)
        assertEquals(OnboardingStep.PRO_FEATURES, state.currentStep)
    }
    
    @Test
    fun testResetOnboarding() {
        onboardingManager.completeOnboarding()
        onboardingManager.resetOnboarding()
        
        val state = onboardingManager.getState()
        assertFalse(state.isCompleted)
        assertEquals(OnboardingStep.WELCOME, state.currentStep)
    }
    
    @Test
    fun testOnboardingPersistence() {
        onboardingManager.advanceToNextStep()
        onboardingManager.advanceToNextStep()
        
        val newManager = OnboardingManager.getInstance(context)
        val state = newManager.getState()
        
        assertEquals(OnboardingStep.INTELLIGENCE, state.currentStep)
    }
}
