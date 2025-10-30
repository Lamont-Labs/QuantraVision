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
import com.lamontlabs.quantravision.detection.PatternDetector
import kotlinx.coroutines.launch

@Composable
fun QuantraVisionApp(context: Context) {
    QuantraVisionTheme {
        val navController = rememberNavController()
        val detector = remember { PatternDetector(context) }
        val scope = rememberCoroutineScope()
        
        AppNavigationHost(
            context = context,
            navController = navController,
            detector = detector,
            scope = scope
        )
    }
}

@Composable
private fun AppNavigationHost(
    context: Context,
    navController: NavHostController,
    detector: PatternDetector,
    scope: kotlinx.coroutines.CoroutineScope
) {
    NavHost(navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(
                context = context,
                onStartScan = { scope.launch { detector.scanStaticAssets() } },
                onReview = { navController.navigate("detections_list") },
                onTutorials = { navController.navigate("tutorials") },
                onSettings = { navController.navigate("settings") },
                onTemplates = { navController.navigate("templates") },
                onAchievements = { navController.navigate("achievements") },
                onAnalytics = { navController.navigate("analytics") },
                onPredictions = { navController.navigate("predictions") },
                onBacktesting = { navController.navigate("backtesting") },
                onSimilarity = { navController.navigate("similarity") },
                onMultiChart = { navController.navigate("multi_chart") },
                onClearHighlights = {
                    scope.launch {
                        val db = PatternDatabase.getInstance(context)
                        db.clearAllTables()
                    }
                }
            )
        }

        composable("achievements") {
            AchievementsScreen(onBack = { navController.popBackStack() })
        }

        composable("analytics") {
            AnalyticsScreen(onBack = { navController.popBackStack() })
        }

        composable("predictions") {
            PredictionScreen(onBack = { navController.popBackStack() })
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
                onBack = { navController.popBackStack() }
            )
        }

        composable("tutorials") {
            EducationHubScreen(
                context = context,
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreenWithNav(onBack = { navController.popBackStack() })
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
    }
}
