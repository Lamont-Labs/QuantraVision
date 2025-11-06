package com.lamontlabs.quantravision.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.lamontlabs.quantravision.ui.MetallicButton
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.MetallicText
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
            .background(MaterialTheme.colorScheme.background)
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
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
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
                            onClick = { viewModel.skipOnboarding() },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Skip", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    MetallicButton(
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
                        showTopStrip = pagerState.currentPage == 4
                    ) {
                        Text(
                            if (pagerState.currentPage < 4) "Next" else "Get Started",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
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
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = getStepIcon(step),
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MetallicText(
            text = step.title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 26.sp,
                textAlign = TextAlign.Center
            ),
            glowIntensity = 0.8f,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = step.description,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MetallicCard(
            modifier = Modifier.fillMaxWidth(),
            enableShimmer = step == OnboardingStep.PRO_FEATURES,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                getStepFeatures(step).forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
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
