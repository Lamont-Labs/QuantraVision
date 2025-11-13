package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.PatternDetector
import com.lamontlabs.quantravision.ml.HybridDetectorBridge
import com.lamontlabs.quantravision.onboarding.OnboardingManager
import kotlinx.coroutines.launch

@Composable
fun QuantraVisionApp(context: Context) {
    QuantraVisionTheme {
        val navController = rememberNavController()
        val detectorBridge = remember { HybridDetectorBridge(context) }
        val legacyDetector = remember { PatternDetector(context) }
        val scope = rememberCoroutineScope()
        val onboardingManager = remember { OnboardingManager.getInstance(context) }
        
        val startDestination = if (onboardingManager.hasCompletedOnboarding()) {
            "dashboard"
        } else {
            "onboarding"
        }
        
        AppNavigationHost(
            context = context,
            navController = navController,
            detectorBridge = detectorBridge,
            legacyDetector = legacyDetector,
            scope = scope,
            startDestination = startDestination
        )
    }
}

@Composable
private fun AppNavigationHost(
    context: Context,
    navController: NavHostController,
    detectorBridge: HybridDetectorBridge,
    legacyDetector: PatternDetector,
    scope: kotlinx.coroutines.CoroutineScope,
    startDestination: String = "dashboard"
) {
    NavHost(navController, startDestination = startDestination) {
        composable("onboarding") {
            com.lamontlabs.quantravision.ui.screens.onboarding.OnboardingScreen(
                onComplete = {
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("dashboard") {
            com.lamontlabs.quantravision.ui.navigation.BottomNavScaffold(
                context = context,
                onStartScan = {
                    // Start the overlay service for real-time detection
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        if (!android.provider.Settings.canDrawOverlays(context)) {
                            // Request overlay permission
                            android.widget.Toast.makeText(
                                context,
                                "Please grant overlay permission to start detection",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            val intent = android.content.Intent(
                                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:${context.packageName}")
                            )
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } else {
                            // Start overlay service
                            val serviceIntent = android.content.Intent(context, com.lamontlabs.quantravision.overlay.OverlayService::class.java)
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                context.startForegroundService(serviceIntent)
                            } else {
                                context.startService(serviceIntent)
                            }
                            android.widget.Toast.makeText(
                                context,
                                "Overlay started! Open your trading app to see pattern detection",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        // For older Android versions
                        val serviceIntent = android.content.Intent(context, com.lamontlabs.quantravision.overlay.OverlayService::class.java)
                        context.startService(serviceIntent)
                        android.widget.Toast.makeText(
                            context,
                            "Overlay started! Open your trading app to see pattern detection",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                },
                onNavigateToDetections = { navController.navigate("detections_list") },
                onNavigateToTemplates = { navController.navigate("templates") },
                onNavigateToIntelligence = { navController.navigate("intelligence") },
                onNavigateToTutorials = { navController.navigate("tutorials") },
                onNavigateToBook = { navController.navigate("book") },
                onNavigateToAchievements = { navController.navigate("achievements") },
                onNavigateToAnalytics = { navController.navigate("analytics") },
                onNavigateToPredictions = { navController.navigate("predictions") },
                onNavigateToBacktesting = { navController.navigate("backtesting") },
                onNavigateToSimilarity = { navController.navigate("similarity") },
                onNavigateToMultiChart = { navController.navigate("multi_chart") },
                onClearHighlights = {
                    scope.launch {
                        try {
                            val db = PatternDatabase.getInstance(context)
                            db.clearAllTables()
                            android.widget.Toast.makeText(
                                context,
                                "Database cleared successfully",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            timber.log.Timber.e(e, "Failed to clear database (likely locked)")
                            android.widget.Toast.makeText(
                                context,
                                "Failed to clear database. Please try again.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            )
        }

        composable("achievements") {
            com.lamontlabs.quantravision.ui.screens.achievements.NewAchievementsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("analytics") {
            com.lamontlabs.quantravision.ui.screens.analytics.AnalyticsDashboardScreen(
                context = context,
                onBack = { navController.popBackStack() }
            )
        }

        composable("predictions") {
            PredictionScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPaywall = { navController.navigate("paywall") }
            )
        }

        composable("backtesting") {
            BacktestScreen(onBack = { navController.popBackStack() })
        }

        composable("similarity") {
            SimilaritySearchScreen(onBack = { navController.popBackStack() })
        }

        composable("multi_chart") {
            MultiChartScreen(onBack = { navController.popBackStack() })
        }

        composable("detections_list") {
            DetectionListScreen(
                db = PatternDatabase.getInstance(context),
                onBack = { navController.popBackStack() },
                onShowPaywall = { navController.navigate("paywall") }
            )
        }

        composable("tutorials") {
            EducationHubScreen(
                context = context,
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreenWithNav(navController = navController)
        }

        composable("legal/{documentType}") { backStackEntry ->
            val documentTypeString = backStackEntry.arguments?.getString("documentType") ?: "privacy"
            val documentType = when (documentTypeString) {
                "privacy" -> com.lamontlabs.quantravision.ui.legal.DocumentType.PRIVACY_POLICY
                "terms" -> com.lamontlabs.quantravision.ui.legal.DocumentType.TERMS_OF_USE
                "disclaimer" -> com.lamontlabs.quantravision.ui.legal.DocumentType.DISCLAIMER
                else -> com.lamontlabs.quantravision.ui.legal.DocumentType.PRIVACY_POLICY
            }
            com.lamontlabs.quantravision.ui.legal.LegalDocumentScreen(
                documentType = documentType,
                onBack = { navController.popBackStack() }
            )
        }

        composable("templates") {
            TemplateManagerScreen(
                context = context,
                onBack = { navController.popBackStack() }
            )
        }

        composable("replay") {
            ReplayScreen(
                context = context,
                onBack = { navController.popBackStack() }
            )
        }

        composable("book") {
            com.lamontlabs.quantravision.ui.screens.BookViewerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("intelligence") {
            IntelligenceScreen(
                onBack = { navController.popBackStack() },
                onRegimeNavigator = { navController.navigate("regime_navigator") },
                onPatternToPlan = { navController.navigate("pattern_plan") },
                onBehavioralGuardrails = { navController.navigate("behavioral_guardrails") },
                onProofCapsules = { navController.navigate("proof_capsules") },
                onUpgrade = { navController.navigate("paywall") }
            )
        }

        composable("regime_navigator") {
            RegimeNavigatorScreen(onBack = { navController.popBackStack() })
        }

        composable("pattern_plan") {
            PatternPlanScreen(onBack = { navController.popBackStack() })
        }

        composable("behavioral_guardrails") {
            BehavioralGuardrailsScreen(onBack = { navController.popBackStack() })
        }

        composable("proof_capsules") {
            ProofCapsuleScreen(onBack = { navController.popBackStack() })
        }

        composable("paywall") {
            com.lamontlabs.quantravision.ui.paywall.PaywallScreen(
                onDismiss = { navController.popBackStack() },
                onBook = {},
                onStandard = {},
                onPro = {}
            )
        }
    }
}
