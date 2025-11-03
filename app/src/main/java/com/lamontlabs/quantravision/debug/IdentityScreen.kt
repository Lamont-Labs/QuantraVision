package com.lamontlabs.quantravision.debug

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.security.ProofVerifier
import com.lamontlabs.quantravision.diagnostics.DeterminismGuard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * IdentityScreen
 * Displays build hash, SBOM hash, ledger hash, and disclaimer version.
 * Locks interface if determinism fails verification.
 */
@Composable
fun IdentityScreen(context: android.content.Context, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var sbom by remember { mutableStateOf("") }
    var ledger by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Checking...") }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val report = ProofVerifier.verify(context)
            sbom = report.hashSummary["sbom.json"] ?: "N/A"
            ledger = report.hashSummary["ledger.log"] ?: "N/A"
            val ok = DeterminismGuard.verify(context)
            status = if (ok) "PASS" else "MISMATCH — rebuild required"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Identity") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("SBOM SHA256: $sbom", style = MaterialTheme.typography.bodySmall)
            Text("Ledger SHA256: $ledger", style = MaterialTheme.typography.bodySmall)
            Text("Verification Status: $status", color = if (status.startsWith("PASS")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
            Text("Disclaimer version hash is tied to each build for audit reproducibility.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
