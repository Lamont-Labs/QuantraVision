package com.lamontlabs.quantravision.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamontlabs.quantravision.onboarding.OnboardingStep
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { OnboardingViewModel(context) }
    val state by viewModel.state.collectAsState()
    val currentStepIndex by viewModel.currentStepIndex.collectAsState()
    
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 5 }
    )
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(pagerState.currentPage) {
        viewModel.goToStep(pagerState.currentPage)
    }
    
    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) {
            onComplete()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavyBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPage(step = OnboardingStep.all()[page])
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(5) { index ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(if (index == pagerState.currentPage) 12.dp else 8.dp)
                                .background(
                                    color = if (index == pagerState.currentPage)
                                        ElectricCyan
                                    else
                                        DarkSurface,
                                    shape = MaterialTheme.shapes.small
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AnimatedVisibility(
                        visible = pagerState.currentPage < 4,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        TextButton(
                            onClick = { viewModel.skipOnboarding() }
                        ) {
                            Text("Skip", color = MetallicSilver)
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = {
                            if (pagerState.currentPage < 4) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                viewModel.completeOnboarding()
                            }
                        },
                        modifier = Modifier.widthIn(min = 120.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan
                        )
                    ) {
                        Text(
                            if (pagerState.currentPage < 4) "Next" else "Get Started",
                            color = DeepNavyBackground
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(step: OnboardingStep) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = getStepIcon(step),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = ElectricCyan
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = step.title,
            style = MaterialTheme.typography.headlineLarge.copy(
                shadow = CyanGlowShadow
            ),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = ElectricCyan
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = step.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MetallicSilver
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = DarkSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                getStepFeatures(step).forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CrispWhite
                        )
                    }
                }
            }
        }
    }
}

fun getStepIcon(step: OnboardingStep): ImageVector {
    return when (step) {
        OnboardingStep.WELCOME -> Icons.Default.Star
        OnboardingStep.DETECTION -> Icons.Default.Search
        OnboardingStep.INTELLIGENCE -> Icons.Default.Psychology
        OnboardingStep.EDUCATION -> Icons.Default.School
        OnboardingStep.PRO_FEATURES -> Icons.Default.Upgrade
    }
}

fun getStepFeatures(step: OnboardingStep): List<String> {
    return when (step) {
        OnboardingStep.WELCOME -> listOf(
            "AI-powered pattern recognition",
            "100+ chart patterns supported",
            "Real-time analysis and alerts",
            "100% offline, privacy-first"
        )
        OnboardingStep.DETECTION -> listOf(
            "📱 HOW TO USE: Tap 'Start Detection' on dashboard",
            "✅ Grant screen capture permission when prompted",
            "📊 Open your trading app (TradingView, etc.)",
            "💎 Cyan border appears when pattern is detected!"
        )
        OnboardingStep.INTELLIGENCE -> listOf(
            "Regime Navigator - Market condition analysis",
            "Pattern Planner - Actionable trade plans",
            "Behavioral Guardrails - Risk management",
            "Proof Capsules - Document your analysis"
        )
        OnboardingStep.EDUCATION -> listOf(
            "25 interactive lessons on technical analysis",
            "Comprehensive trading book included",
            "Pattern-specific tutorials",
            "Risk management and psychology"
        )
        OnboardingStep.PRO_FEATURES -> listOf(
            "Unlock all 100+ patterns",
            "Unlimited pattern detections",
            "Advanced intelligence features",
            "Priority support and updates"
        )
    }
}
