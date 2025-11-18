package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.PatternDetector
import com.lamontlabs.quantravision.ml.HybridDetectorBridge
import com.lamontlabs.quantravision.onboarding.OnboardingManager
import com.lamontlabs.quantravision.ui.screens.settings.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun QuantraVisionApp(context: Context) {
    QuantraVisionTheme {
        LaunchedEffect(Unit) {
            com.lamontlabs.quantravision.entitlements.EntitlementManager.initialize(context)
            com.lamontlabs.quantravision.onboarding.FeatureDiscoveryStore.initialize(context)
            com.lamontlabs.quantravision.devbot.engine.DiagnosticEngine.initialize(context)
        }
        
        val navController = rememberNavController()
        val detectorBridge = remember { HybridDetectorBridge(context) }
        val legacyDetector = remember { PatternDetector(context) }
        val scope = rememberCoroutineScope()
        val onboardingManager = remember { OnboardingManager.getInstance(context) }
        
        val startDestination = if (onboardingManager.hasCompletedOnboarding()) {
            "home"
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
    startDestination: String = "home"
) {
    val onStartScan = {
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
    }
    
    // Define main tab routes that should show the bottom bar
    val mainTabRoutes = setOf("home", "markets", "scan", "quantrabot", "devbot", "settings")
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in mainTabRoutes
    
    // ONE Scaffold wrapping ONE NavHost - correct architecture
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                com.lamontlabs.quantravision.ui.navigation.BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
        composable("onboarding") {
            com.lamontlabs.quantravision.ui.screens.onboarding.OnboardingScreen(
                onComplete = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        
        // Main tab routes - NO BottomNavScaffold wrapper (bottom bar is at top level)
        composable("home") {
            com.lamontlabs.quantravision.ui.screens.home.HomeScreen(
                onNavigateToAchievements = { navController.navigate("achievements") },
                onNavigateToAnalytics = { navController.navigate("analytics") },
                onNavigateToPaywall = { navController.navigate("paywall") }
            )
        }
        
        composable("markets") {
            com.lamontlabs.quantravision.ui.screens.markets.MarketsScreen(
                onNavigateToPaywall = { navController.navigate("paywall") }
            )
        }
        
        composable("scan") {
            com.lamontlabs.quantravision.ui.screens.scan.ScanScreen(
                onNavigateToPaywall = { navController.navigate("paywall") }
            )
        }
        
        composable("learn") {
            com.lamontlabs.quantravision.ui.screens.learn.LearnScreen(
                onNavigateToTutorials = { navController.navigate("tutorials") },
                onNavigateToBook = { navController.navigate("book") },
                onNavigateToPaywall = { navController.navigate("paywall") }
            )
        }
        
        composable("quantrabot") {
            com.lamontlabs.quantravision.ui.screens.QuantraBotScreen(
                paddingValues = PaddingValues()
            )
        }
        
        composable("devbot") {
            com.lamontlabs.quantravision.devbot.ui.DevBotScreen()
        }
        
        composable("settings") {
            SettingsScreen(
                onNavigateToPaywall = { navController.navigate("paywall") }
            )
        }

        composable("achievements") {
            com.lamontlabs.quantravision.ui.screens.achievements.AchievementsScreen(
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
                onPatternToPlan = { navController.navigate("pattern_to_plan") },
                onBehavioralGuardrails = { navController.navigate("behavioral_guardrails") },
                onProofCapsules = { navController.navigate("proof_capsules") },
                onUpgrade = { navController.navigate("paywall") }
            )
        }

        composable("regime_navigator") {
            com.lamontlabs.quantravision.ui.screens.intelligence.RegimeNavigatorScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPaywall = { navController.navigate("paywall") }
            )
        }

        composable("pattern_to_plan") {
            com.lamontlabs.quantravision.ui.screens.intelligence.PatternToPlanScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPaywall = { navController.navigate("paywall") }
            )
        }

        composable("behavioral_guardrails") {
            com.lamontlabs.quantravision.ui.screens.intelligence.BehavioralGuardrailsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPaywall = { navController.navigate("paywall") }
            )
        }

        composable("proof_capsules") {
            com.lamontlabs.quantravision.ui.screens.intelligence.ProofCapsulesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPaywall = { navController.navigate("paywall") }
            )
        }

        composable("paywall") {
            com.lamontlabs.quantravision.ui.screens.paywall.PaywallScreen(
                onBack = { navController.popBackStack() },
                onPurchaseComplete = {
                    navController.popBackStack()
                },
                navController = navController
            )
        }
        }
    }
}
