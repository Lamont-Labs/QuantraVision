package com.lamontlabs.quantravision.ui

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.PatternDetector
import com.lamontlabs.quantravision.billing.BillingManager
import com.lamontlabs.quantravision.ml.HybridDetectorBridge
import com.lamontlabs.quantravision.onboarding.OnboardingManager
import com.lamontlabs.quantravision.ui.capture.rememberScreenCaptureCoordinator
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.ui.screens.PerformanceDashboardScreen

@Composable
fun QuantraVisionApp(context: Context) {
    QuantraVisionTheme {
        val navController = rememberNavController()
        var detectorBridge by remember { mutableStateOf<HybridDetectorBridge?>(null) }
        var legacyDetector by remember { mutableStateOf<PatternDetector?>(null) }
        val scope = rememberCoroutineScope()
        val onboardingManager = remember { OnboardingManager.getInstance(context) }
        val activity = context as? Activity
        
        // Create heavyweight objects safely in LaunchedEffect (side effect, not during composition)
        LaunchedEffect(Unit) {
            detectorBridge = HybridDetectorBridge(context)
            legacyDetector = PatternDetector(context)
        }
        
        val hasOverlayPermission = remember {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                android.provider.Settings.canDrawOverlays(context)
            } else {
                true
            }
        }
        
        val isOpenedFromOverlay = activity?.intent?.getBooleanExtra("opened_from_overlay", false) ?: false
        
        val startDestination = when {
            !onboardingManager.hasCompletedOnboarding() -> "onboarding"
            onboardingManager.hasCompletedOnboarding() && !hasOverlayPermission && !isOpenedFromOverlay -> "auto_launch_overlay"
            else -> "dashboard"
        }
        
        // Only show navigation when objects are ready
        if (detectorBridge != null && legacyDetector != null) {
            AppNavigationHost(
                context = context,
                navController = navController,
                detectorBridge = detectorBridge!!,
                legacyDetector = legacyDetector!!,
                scope = scope,
                startDestination = startDestination
            )
        } else {
            // Loading state while objects initialize
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
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
            ProfessionalOnboarding(
                context = context,
                onComplete = {
                    OnboardingManager.getInstance(context).completeOnboarding()
                    navController.navigate("auto_launch_overlay") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        
        composable("auto_launch_overlay") {
            var overlayPermissionGranted by remember {
                mutableStateOf(
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        android.provider.Settings.canDrawOverlays(context)
                    } else {
                        true
                    }
                )
            }
            var serviceReady by remember { mutableStateOf(false) }
            var serviceFailed by remember { mutableStateOf(false) }
            val activity = context as? Activity
            
            // Register broadcast receiver to listen for service-ready signal
            DisposableEffect(Unit) {
                val receiver = object : android.content.BroadcastReceiver() {
                    override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                        if (intent?.action == "com.lamontlabs.quantravision.OVERLAY_SERVICE_READY") {
                            serviceReady = true
                        }
                    }
                }
                
                val filter = android.content.IntentFilter("com.lamontlabs.quantravision.OVERLAY_SERVICE_READY")
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(receiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
                } else {
                    context.registerReceiver(receiver, filter)
                }
                
                onDispose {
                    try {
                        context.unregisterReceiver(receiver)
                    } catch (e: IllegalArgumentException) {
                        // Already unregistered
                    }
                }
            }
            
            // Handle service ready state - show toast but DO NOT finish activity
            LaunchedEffect(serviceReady) {
                if (serviceReady) {
                    android.widget.Toast.makeText(
                        context,
                        "✅ Overlay ready! Tap the cyan Q button or use the dashboard below",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    
                    // DO NOT call activity?.finish() - keep the app open so user can see the dashboard
                }
            }
            
            // Wait for permission, start service, and set timeout
            LaunchedEffect(Unit) {
                // Wait for overlay permission to be granted
                while (!overlayPermissionGranted) {
                    kotlinx.coroutines.delay(1000)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        overlayPermissionGranted = android.provider.Settings.canDrawOverlays(context)
                    } else {
                        overlayPermissionGranted = true
                    }
                }
                
                // Start the overlay service
                val serviceIntent = android.content.Intent(context, com.lamontlabs.quantravision.overlay.OverlayService::class.java)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                
                // Set 5-second timeout for service to broadcast ready signal
                kotlinx.coroutines.delay(5000)
                if (!serviceReady) {
                    serviceFailed = true
                }
            }
            
            // UI States
            when {
                serviceFailed -> {
                    // Error screen with retry button and recovery options
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Service Failed to Start",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Failed to start overlay service. You can retry, check permissions, or continue to the main app without overlay.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = {
                                // Reset states and retry
                                serviceFailed = false
                                serviceReady = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("Retry")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                // Navigate to dashboard
                                navController.navigate("dashboard") {
                                    popUpTo("auto_launch_overlay") { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Go to Main App")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedButton(
                            onClick = {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    val intent = android.content.Intent(
                                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        android.net.Uri.parse("package:${context.packageName}")
                                    )
                                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("Check Permissions")
                        }
                    }
                }
                !overlayPermissionGranted -> {
                    // Permission request screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Overlay Permission Required",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "QuantraVision needs permission to display the pattern detection overlay on top of your trading apps. This is required for the app to function.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    val intent = android.content.Intent(
                                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        android.net.Uri.parse("package:${context.packageName}")
                                    )
                                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
                else -> {
                    // Loading state while waiting for service to be ready
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Starting overlay service...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        composable("dashboard") {
            val screenCaptureCoordinator = rememberScreenCaptureCoordinator()
            
            DashboardScreen(
                context = context,
                onBook = { navController.navigate("book") },
                onIntelligence = { navController.navigate("intelligence") },
                onStartScan = {
                    // Check overlay permission first
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        if (!android.provider.Settings.canDrawOverlays(context)) {
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
                            return@DashboardScreen
                        }
                    }
                    
                    // Request screen capture permission via MediaProjection
                    screenCaptureCoordinator.requestScreenCapture()
                },
                onStopScan = {
                    // Stop the overlay service
                    context.stopService(android.content.Intent(context, com.lamontlabs.quantravision.overlay.OverlayService::class.java))
                    android.widget.Toast.makeText(
                        context,
                        "Overlay stopped",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                },
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
                },
                onLearning = { navController.navigate("learning_dashboard") },
                onAdvancedLearning = { navController.navigate("advanced_learning") },
                onExport = { navController.navigate("export") },
                onPerformance = { navController.navigate("performance") },
                onHelp = { navController.navigate("help") },
                onAbout = { navController.navigate("about") }
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
                onBack = { navController.popBackStack() },
                onShowPaywall = { navController.navigate("paywall") },
                navController = navController
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
                onBack = { navController.popBackStack() },
                navController = navController
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
            val activity = LocalContext.current as? Activity
            if (activity != null) {
                val billingManager = remember { BillingManager(activity) }
                
                com.lamontlabs.quantravision.ui.paywall.PaywallScreen(
                    onDismiss = { navController.popBackStack() },
                    onBook = { billingManager.purchaseBook() },
                    onStandard = { billingManager.purchaseStandard() },
                    onPro = { billingManager.purchasePro() }
                )
            } else {
                Text(
                    "Error: Purchase unavailable",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        composable("learning_dashboard") {
            com.lamontlabs.quantravision.ui.screens.learning.LearningDashboardScreen()
        }

        composable("advanced_learning") {
            com.lamontlabs.quantravision.ui.screens.learning.AdvancedLearningDashboardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("export") {
            val exportViewModel = remember { com.lamontlabs.quantravision.ui.screens.export.ExportViewModel(context) }
            com.lamontlabs.quantravision.ui.screens.export.ExportScreen(
                viewModel = exportViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("performance") {
            PerformanceDashboardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("help") {
            val helpFiles = remember { emptyList<java.io.File>() }
            HelpScreen(
                helpFiles = helpFiles,
                onBack = { navController.popBackStack() }
            )
        }

        composable("about") {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        composable("advanced_settings") {
            SettingsAdvancedScreen(
                context = context,
                onBack = { navController.popBackStack() }
            )
        }

        composable("pattern_detail/{patternId}") { backStackEntry ->
            val patternId = backStackEntry.arguments?.getString("patternId")?.toLongOrNull()
            if (patternId != null) {
                val db = PatternDatabase.getInstance(context)
                var pattern by remember { mutableStateOf<PatternMatch?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                var error by remember { mutableStateOf<String?>(null) }
                
                LaunchedEffect(patternId) {
                    scope.launch {
                        try {
                            pattern = db.patternDao().getById(patternId)
                            isLoading = false
                        } catch (e: Exception) {
                            error = e.message
                            isLoading = false
                        }
                    }
                }
                
                when {
                    isLoading -> {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text("Pattern Details") },
                                    navigationIcon = {
                                        IconButton(onClick = { navController.popBackStack() }) {
                                            Icon(Icons.Default.ArrowBack, "Back")
                                        }
                                    }
                                )
                            }
                        ) { padding ->
                            Box(
                                modifier = Modifier.fillMaxSize().padding(padding),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    error != null -> {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text("Error") },
                                    navigationIcon = {
                                        IconButton(onClick = { navController.popBackStack() }) {
                                            Icon(Icons.Default.ArrowBack, "Back")
                                        }
                                    }
                                )
                            }
                        ) { padding ->
                            Box(
                                modifier = Modifier.fillMaxSize().padding(padding),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Pattern not found", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    pattern != null -> {
                        PatternDetailScreen(
                            match = pattern!!,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            } else {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Error") },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.Default.ArrowBack, "Back")
                                }
                            }
                        )
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Invalid pattern ID", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        composable("template_editor/{templateId}") { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId") ?: ""
            TemplateEditorScreen(
                context = context,
                initialTemplateId = templateId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
