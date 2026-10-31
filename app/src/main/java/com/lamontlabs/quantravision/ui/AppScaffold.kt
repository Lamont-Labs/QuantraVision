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
import com.lamontlabs.quantravision.onboarding.OnboardingManager
import com.lamontlabs.quantravision.ui.screens.settings.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun QuantraVisionApp(context: Context) {
    QuantraVisionTheme {
        LaunchedEffect(Unit) {
            com.lamontlabs.quantravision.entitlements.EntitlementManager.initialize(context)
            com.lamontlabs.quantravision.onboarding.FeatureDiscoveryStore.initialize(context)
        }
        
        val navController = rememberNavController()
        val onboardingManager = remember { OnboardingManager.getInstance(context) }
        
        val startDestination = if (onboardingManager.hasCompletedOnboarding()) {
            "home"
        } else {
            "onboarding"
        }
        
        AppNavigationHost(
            context = context,
            navController = navController,
            startDestination = startDestination
        )
    }
}

@Composable
private fun AppNavigationHost(
    context: Context,
    navController: NavHostController,
    startDestination: String = "home"
) {
    // Define main tab routes that should show the bottom bar
    val mainTabRoutes = setOf("home", "markets", "scan", "quantrabot", "settings")
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
