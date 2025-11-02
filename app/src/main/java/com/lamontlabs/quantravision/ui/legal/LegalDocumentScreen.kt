package com.lamontlabs.quantravision.ui.legal

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

enum class DocumentType(val filename: String, val title: String) {
    PRIVACY_POLICY("PRIVACY_POLICY.html", "Privacy Policy"),
    TERMS_OF_USE("TERMS_OF_USE.html", "Terms of Use"),
    DISCLAIMER("DISCLAIMER.txt", "Disclaimer")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalDocumentScreen(
    documentType: DocumentType,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var content by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(documentType) {
        try {
            val inputStream = context.assets.open("legal/${documentType.filename}")
            content = inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            error = "Failed to load document: ${e.message}"
            timber.log.Timber.e(e, "Error loading legal document: ${documentType.filename}")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(documentType.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                content == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    if (documentType == DocumentType.DISCLAIMER) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Text(
                                text = content ?: "",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.apply {
                                        javaScriptEnabled = false
                                        loadWithOverviewMode = true
                                        useWideViewPort = true
                                        setSupportZoom(true)
                                        builtInZoomControls = true
                                        displayZoomControls = false
                                    }
                                }
                            },
                            update = { webView ->
                                content?.let { htmlContent ->
                                    webView.loadDataWithBaseURL(
                                        null,
                                        htmlContent,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
