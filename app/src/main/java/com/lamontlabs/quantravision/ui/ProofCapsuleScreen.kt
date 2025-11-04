package com.lamontlabs.quantravision.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.proof.ProofCapsuleGenerator
import com.lamontlabs.quantravision.ui.components.AdvancedFeaturesDisclaimerCard
import kotlinx.coroutines.launch
import java.io.File

/**
 * ProofCapsuleScreen
 * 
 * View and share tamper-proof detection capsules
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProofCapsuleScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val generator = remember { ProofCapsuleGenerator(context) }
    
    var capsules by remember { mutableStateOf<List<File>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCapsule by remember { mutableStateOf<ProofCapsuleGenerator.ProofCapsule?>(null) }
    var qrCode by remember { mutableStateOf<Bitmap?>(null) }
    var showDemo by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        capsules = generator.listCapsules()
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Proof Capsules",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            shadow = SubtleGlowShadow
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDemo = true }) {
                        Icon(
                            Icons.Default.Add,
                            "Generate Demo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // MANDATORY LEGAL DISCLAIMER
            item {
                AdvancedFeaturesDisclaimerCard(collapsible = true)
            }
            
            item {
                Column {
                    Text(
                        "🛡️ Tamper-Proof Detection Logs",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            shadow = CyanGlowShadow
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Package pattern detections into shareable, tamper-evident capsules with SHA-256 hashes and complete audit trails.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Generate demo button
            if (capsules.isEmpty() && !isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(tonalElevation = 8.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "No Capsules Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Generate a demo proof capsule to see how tamper-evident packaging works.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { showDemo = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.Default.Science,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Generate Demo Capsule")
                            }
                        }
                    }
                }
            }
            
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(capsules) { file ->
                    CapsuleListItem(
                        file = file,
                        onClick = {
                            scope.launch {
                                try {
                                    val loaded = generator.loadCapsule(file)
                                    selectedCapsule = loaded
                                    qrCode = generator.generateQRCode(loaded, 512)
                                } catch (e: Exception) {
                                    // Error loading capsule
                                }
                            }
                        }
                    )
                }
            }
            
            // Selected capsule details
            selectedCapsule?.let { capsule ->
                item {
                    CapsuleDetailCard(capsule, qrCode, generator, scope)
                }
            }
            
            // Educational info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(tonalElevation = 8.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "How It Works",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Proof Capsules package pattern detections with:\n\n" +
                            "• ISO 8601 timestamps (UTC)\n" +
                            "• Complete detection metadata\n" +
                            "• SHA-256 tamper-evident hash\n" +
                            "• QR code for easy verification\n" +
                            "• Android share integration\n\n" +
                            "Capsules can be exported, shared, and verified to ensure detection integrity.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Disclaimer
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    elevation = CardDefaults.cardElevation(tonalElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "⚠️ EDUCATIONAL LOGS ONLY\n\n" +
                            "Proof Capsules are EDUCATIONAL DETECTION LOGS. They are NOT proof of trading results, verified trading records, or investment recommendations. Sharing capsules does NOT constitute financial advice.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
    
    // Demo generation dialog
    if (showDemo) {
        AlertDialog(
            onDismissRequest = { showDemo = false },
            title = { Text("Generate Demo Capsule") },
            text = { Text("This will create a demo proof capsule for a sample pattern detection.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                val samplePattern = createSamplePattern()
                                val capsule = generator.generateCapsule(samplePattern)
                                capsules = generator.listCapsules()
                                selectedCapsule = capsule
                                qrCode = generator.generateQRCode(capsule, 512)
                                showDemo = false
                            } catch (e: Exception) {
                                // Error generating demo
                            }
                        }
                    }
                ) {
                    Text("Generate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDemo = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CapsuleListItem(
    file: File,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    file.nameWithoutExtension.replace("capsule_", "").replace("_", " "),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Size: ${file.length() / 1024} KB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun CapsuleDetailCard(
    capsule: ProofCapsuleGenerator.ProofCapsule,
    qrCode: Bitmap?,
    generator: ProofCapsuleGenerator,
    scope: kotlinx.coroutines.CoroutineScope
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "📦 Capsule Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Divider(Modifier.padding(vertical = 12.dp))
            
            DetailRow("Pattern", capsule.patternId)
            DetailRow("Confidence", "${"%.1f".format(capsule.confidence * 100)}%")
            DetailRow("Timestamp", capsule.timestamp)
            DetailRow("Timeframe", capsule.timeframe)
            DetailRow("Consensus", "${"%.2f".format(capsule.consensusScore)}")
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                "Hash (SHA-256):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                capsule.sha256Hash,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // QR Code
            qrCode?.let { bitmap ->
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(200.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            generator.shareCapsule(capsule, includeQR = true)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Share")
                }
                
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val isValid = generator.verifyCapsule(capsule)
                            // Show verification result
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Verify")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
    Spacer(Modifier.height(4.dp))
}

private fun createSamplePattern(): PatternMatch {
    return PatternMatch(
        id = 0,
        patternName = "Double Top",
        confidence = 0.78,
        timestamp = System.currentTimeMillis(),
        originPath = "template",
        scale = 1.0,
        timeframe = "4H",
        consensusScore = 0.78,
        detectionBounds = null,
        windowMs = 14400000
    )
}
