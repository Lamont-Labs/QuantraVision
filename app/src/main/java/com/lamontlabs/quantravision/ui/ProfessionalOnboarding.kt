package com.lamontlabs.quantravision.ui

import android.app.Activity
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lamontlabs.quantravision.system.PermissionHelper

@Composable
fun ProfessionalOnboarding(context: Context, onComplete: () -> Unit) {
    val activity = context as? Activity ?: (LocalContext.current as? Activity)
    val prefs = remember { context.getSharedPreferences("qv_onboarding_prefs", Context.MODE_PRIVATE) }
    var currentStep by remember { mutableStateOf(loadOnboardingProgress(prefs)) }
    
    DisposableEffect(currentStep) {
        saveOnboardingProgress(prefs, currentStep)
        onDispose { }
    }

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                },
                label = "onboarding_slide"
            ) { step ->
                when (step) {
                    0 -> WelcomeSlide(onNext = { currentStep++ })
                    1 -> PermissionsSlide(
                        activity = activity,
                        onNext = { currentStep++ }
                    )
                    2 -> FeatureSlide1DetectionPower(onNext = { currentStep++ })
                    3 -> FeatureSlide2IntelligenceStack(onNext = { currentStep++ })
                    4 -> FeatureSlide3VoiceAlerts(onNext = { currentStep++ })
                    5 -> FeatureSlide4AILearning(onNext = { currentStep++ })
                    6 -> FeatureSlide5Gamification(onNext = { currentStep++ })
                    7 -> LegalDisclaimerSlide(
                        context = context,
                        onAccept = { currentStep++ }
                    )
                    else -> {
                        CompletionSlide(onFinish = {
                            prefs.edit().clear().apply()
                            onComplete()
                        })
                    }
                }
            }
            
            if (currentStep in 2..6) {
                ProgressIndicator(
                    currentStep = currentStep - 1,
                    totalSteps = 5,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                )
            }
        }
    }
}

@Composable
private fun WelcomeSlide(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Visibility,
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .shadow(
                    elevation = 0.dp,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.primary
                ),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Welcome to",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Text(
            text = "QuantraVision",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                shadow = CyanGlowShadow
            ),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "by Lamont Labs",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "AI-Powered Pattern Detection\nfor Professional Traders",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        ValueProposition(
            icon = Icons.Default.Psychology,
            text = "109 Patterns Detected"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ValueProposition(
            icon = Icons.Default.Lock,
            text = "100% Offline Privacy"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ValueProposition(
            icon = Icons.Default.MoneyOff,
            text = "No Subscriptions Ever"
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Get Started",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun ValueProposition(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PermissionsSlide(activity: Activity?, onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Permissions Required",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        PermissionCard(
            icon = Icons.Default.Layers,
            title = "Overlay Permission",
            description = "Display pattern highlights on your trading charts in real-time",
            required = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PermissionCard(
            icon = Icons.Default.FolderOpen,
            title = "Storage Access",
            description = "Save pattern detection reports and proof exports to your device",
            required = false
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = {
                activity?.let { PermissionHelper.requestAll(it) }
                onNext()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Grant Permissions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    required: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (required) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REQUIRED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.errorContainer,
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun FeatureSlide1DetectionPower(onNext: () -> Unit) {
    FeatureSlideTemplate(
        icon = Icons.Default.RadioButtonChecked,
        title = "Detection Power",
        description = "Detect 109 chart patterns with AI precision",
        details = "Advanced OpenCV template matching identifies Head & Shoulders, Double Tops, Triangles, and 106 more patterns in real-time on your trading charts.",
        onNext = onNext
    )
}

@Composable
private fun FeatureSlide2IntelligenceStack(onNext: () -> Unit) {
    FeatureSlideTemplate(
        icon = Icons.Default.BarChart,
        title = "Intelligence Stack",
        description = "Know WHEN to trade with market analysis",
        details = "Regime Navigator analyzes market conditions to show 'High Probability' moments. Pattern-to-Plan Engine suggests entry/exit points based on detected patterns.",
        onNext = onNext
    )
}

@Composable
private fun FeatureSlide3VoiceAlerts(onNext: () -> Unit) {
    FeatureSlideTemplate(
        icon = Icons.Default.VolumeUp,
        title = "Voice Alerts",
        description = "Hands-free pattern notifications",
        details = "Get spoken alerts like 'Head and Shoulders forming - 85% confidence' so you never miss a trading opportunity while working.",
        onNext = onNext
    )
}

@Composable
private fun FeatureSlide4AILearning(onNext: () -> Unit) {
    FeatureSlideTemplate(
        icon = Icons.Default.TrendingUp,
        title = "AI Learning",
        description = "Gets smarter as you trade",
        details = "10 learning algorithms track pattern frequency, co-occurrence, and confidence over time. The app improves detection quality with every scan.",
        onNext = onNext
    )
}

@Composable
private fun FeatureSlide5Gamification(onNext: () -> Unit) {
    FeatureSlideTemplate(
        icon = Icons.Default.EmojiEvents,
        title = "Achievements & Streaks",
        description = "Track your progress and stay motivated",
        details = "Unlock 15 achievements, maintain daily streaks, and watch your pattern detection skills grow with detailed analytics.",
        onNext = onNext
    )
}

@Composable
private fun FeatureSlideTemplate(
    icon: ImageVector,
    title: String,
    description: String,
    details: String,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .shadow(
                    elevation = 0.dp,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.primary
                ),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                shadow = CyanGlowShadow
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = details,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Next",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun LegalDisclaimerSlide(context: Context, onAccept: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "⚠️ Legal Disclaimer",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "NOT FINANCIAL ADVICE\n\n" +
                            "QuantraVision is an EDUCATIONAL tool only. It does NOT provide financial advice, investment recommendations, or trading signals.\n\n" +
                            "⚠️ TRADING IS RISKY. YOU CAN LOSE MONEY.\n\n" +
                            "• AI pattern detection may produce false positives/negatives\n" +
                            "• Past performance does NOT predict future results\n" +
                            "• You are SOLELY responsible for all trading decisions\n" +
                            "• Consult a licensed financial advisor before investing\n\n" +
                            "LIMITATION OF LIABILITY:\n" +
                            "Lamont Labs is NOT liable for any trading losses, missed opportunities, or financial damages arising from use of this app.\n\n" +
                            "By clicking 'I Agree', you acknowledge:\n" +
                            "1. You have read the full Terms of Use\n" +
                            "2. You understand this is NOT financial advice\n" +
                            "3. You accept all trading risks\n" +
                            "4. You will not hold Lamont Labs liable for losses",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                DisclaimerManager.setAccepted(context, true)
                onAccept()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                text = "I Understand the Risks & Agree",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun CompletionSlide(onFinish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "You're All Set!",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                shadow = CyanGlowShadow
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "QuantraVision is ready to detect patterns on your trading charts",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Start Detecting Patterns",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun ProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == currentStep) 12.dp else 8.dp)
                    .background(
                        color = if (index == currentStep)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }
    }
}

private fun loadOnboardingProgress(prefs: android.content.SharedPreferences): Int {
    return prefs.getInt("onboarding_step", 0)
}

private fun saveOnboardingProgress(prefs: android.content.SharedPreferences, step: Int) {
    prefs.edit().putInt("onboarding_step", step).apply()
}
