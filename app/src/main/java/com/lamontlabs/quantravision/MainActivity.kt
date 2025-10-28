package com.lamontlabs.quantravision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lamontlabs.quantravision.ui.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { QuantraVisionApp() }
  }

  @Composable
  fun QuantraVisionApp() {
    MaterialTheme {
      val ctx = LocalContext.current
      val navController = rememberNavController()
      
      val detector = remember { PatternDetector(ctx) }
      val scope = rememberCoroutineScope()

      NavHost(navController, startDestination = "dashboard") {
        composable("dashboard") {
          DashboardScreen(
            context = ctx,
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
                val db = PatternDatabase.getInstance(ctx)
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
            db = PatternDatabase.getInstance(ctx),
            onBack = { navController.popBackStack() }
          )
        }
        
        composable("tutorials") {
          HelpScreen(
            helpFiles = emptyList(),
            onBack = { navController.popBackStack() }
          )
        }
        
        composable("settings") {
          SettingsScreenWithNav(onBack = { navController.popBackStack() })
        }
        
        composable("templates") {
          TemplateManagerScreen(
            context = ctx,
            onBack = { navController.popBackStack() }
          )
        }
      }
    }
  }
}
