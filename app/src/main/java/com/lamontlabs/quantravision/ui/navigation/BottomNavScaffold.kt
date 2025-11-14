package com.lamontlabs.quantravision.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

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
 * Bottom Navigation Bar Component
 * Standalone navigation bar to be used at the top level of the app
 * This should be placed in a Scaffold's bottomBar slot, NOT wrapping individual screens
 */
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Markets,
        BottomNavItem.Scan,
        BottomNavItem.Learn,
        BottomNavItem.Settings
    )
    
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { 
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    ) 
                },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * @deprecated Use BottomNavigationBar directly in the top-level Scaffold instead.
 * This wrapper creates duplicate bottom bars when used inside NavHost routes.
 */
@Deprecated(
    message = "Use BottomNavigationBar in top-level Scaffold instead",
    replaceWith = ReplaceWith("BottomNavigationBar(navController)")
)
@Composable
fun BottomNavScaffold(
    navController: NavHostController,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}
