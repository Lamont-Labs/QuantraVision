package com.lamontlabs.quantravision.ui.navigation

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lamontlabs.quantravision.ui.screens.home.HomeScreen
import com.lamontlabs.quantravision.ui.screens.markets.MarketsScreen
import com.lamontlabs.quantravision.ui.screens.scan.ScanScreen
import com.lamontlabs.quantravision.ui.screens.learn.LearnScreen
import com.lamontlabs.quantravision.ui.SettingsScreenWithNav

/**
 * Bottom Navigation Items
 * Following Material Design 3 guidelines with 5 primary destinations
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Markets : BottomNavItem("markets", "Market", Icons.Default.TrendingUp)
    object Scan : BottomNavItem("scan", "Scan", Icons.Default.PlayArrow)
    object Learn : BottomNavItem("learn", "Learn", Icons.Default.School)
    object Settings : BottomNavItem("settings", "Config", Icons.Default.Settings)
}

/**
 * Main Bottom Navigation Scaffold
 * Professional Material 3 navigation pattern with 5 tabs
 */
@Composable
fun BottomNavScaffold(
    context: Context,
    navController: NavHostController = rememberNavController(),
    onStartScan: () -> Unit,
    onNavigateToDetections: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToIntelligence: () -> Unit,
    onNavigateToTutorials: () -> Unit,
    onNavigateToBook: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToPredictions: () -> Unit,
    onNavigateToBacktesting: () -> Unit,
    onNavigateToSimilarity: () -> Unit,
    onNavigateToMultiChart: () -> Unit,
    onClearHighlights: () -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Markets,
        BottomNavItem.Scan,
        BottomNavItem.Learn,
        BottomNavItem.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { 
                            Text(
                                item.title,
                                maxLines = 1
                            ) 
                        },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination to avoid building up a large stack
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    context = context,
                    onStartScan = onStartScan,
                    onViewDetections = onNavigateToDetections,
                    onNavigateToAnalytics = onNavigateToAnalytics
                )
            }
            
            composable(BottomNavItem.Markets.route) {
                MarketsScreen(
                    context = context,
                    onNavigateToTemplates = onNavigateToTemplates,
                    onNavigateToIntelligence = onNavigateToIntelligence,
                    onNavigateToPredictions = onNavigateToPredictions,
                    onNavigateToBacktesting = onNavigateToBacktesting,
                    onNavigateToSimilarity = onNavigateToSimilarity,
                    onNavigateToMultiChart = onNavigateToMultiChart
                )
            }
            
            composable(BottomNavItem.Scan.route) {
                ScanScreen(
                    context = context,
                    onStartScan = onStartScan
                )
            }
            
            composable(BottomNavItem.Learn.route) {
                LearnScreen(
                    context = context,
                    onNavigateToTutorials = onNavigateToTutorials,
                    onNavigateToBook = onNavigateToBook,
                    onNavigateToAchievements = onNavigateToAchievements
                )
            }
            
            composable(BottomNavItem.Settings.route) {
                SettingsScreenWithNav(
                    onClearDatabase = onClearHighlights
                )
            }
        }
    }
}
