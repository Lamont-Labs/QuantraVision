package com.lamontlabs.quantravision.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamontlabs.quantravision.licensing.BookFeatureGate
import com.lamontlabs.quantravision.utils.BookmarkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookViewerScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val hasBookAccess = remember { BookFeatureGate.hasAccess(context) }
    
    if (!hasBookAccess) {
        BookLockedScreen(onNavigateBack = onNavigateBack)
    } else {
        BookReaderScreen(context = context, onNavigateBack = onNavigateBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookLockedScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("The Friendly Trader") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Trading Book Locked",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "The Friendly Trader: Your Beginner's Guide to Confidence in the Markets",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Unlock This Book:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("📚 Purchase for $4.99 (standalone)", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("⭐ FREE with Standard tier ($24.99)", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🚀 FREE with Pro tier ($49.99)", style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Complete 10-chapter guide covering:\n• Market basics\n• Chart reading\n• Risk management\n• Pattern recognition\n• Trading psychology\n• First demo trades\n• And more!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { 
                    // TODO: Navigate to upgrade/billing screen once integrated with AppScaffold
                    // For now, users can access upgrades through Settings or Dashboard
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upgrade to Unlock")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Note: Upgrade functionality will be available in Settings once billing is fully integrated.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * UI state for book viewer with proper loading/error handling
 */
sealed class BookUiState {
    object Loading : BookUiState()
    data class Success(val content: String, val coverBitmap: android.graphics.Bitmap?) : BookUiState()
    data class Error(val message: String) : BookUiState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookReaderScreen(
    context: Context,
    onNavigateBack: () -> Unit
) {
    // Scroll state with bookmark support
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    var hasBookmark by remember { mutableStateOf(BookmarkManager.hasBookmark(context)) }
    var isRestoringBookmark by remember { mutableStateOf(false) }
    
    // Load saved bookmark position after content is loaded
    LaunchedEffect(scrollState.maxValue) {
        if (scrollState.maxValue > 0 && hasBookmark) {
            val savedPosition = BookmarkManager.getBookmark(context)
            if (savedPosition > 0) {
                isRestoringBookmark = true
                scrollState.scrollTo(savedPosition)
                kotlinx.coroutines.delay(2000)
                isRestoringBookmark = false
            }
        }
    }
    
    // Auto-save bookmark as user scrolls (with debouncing)
    LaunchedEffect(scrollState.value) {
        if (scrollState.value > 0) {
            kotlinx.coroutines.delay(500)
            BookmarkManager.saveBookmark(context, scrollState.value)
            hasBookmark = true
        }
    }
    
    // Save bookmark when navigating away
    DisposableEffect(Unit) {
        onDispose {
            if (scrollState.value > 0) {
                BookmarkManager.saveBookmark(context, scrollState.value)
            }
        }
    }
    
    // Use produceState for proper coroutine-based state management
    // This ensures loading happens on background thread without blocking composition
    val bookState by produceState<BookUiState>(initialValue = BookUiState.Loading, context) {
        value = try {
            // Load book content and cover in parallel on IO dispatcher
            val contentDeferred = async(Dispatchers.IO) { loadBookContent(context) }
            val coverDeferred = async(Dispatchers.IO) { loadBookCover(context) }
            
            val content = contentDeferred.await()
            val cover = coverDeferred.await()
            
            BookUiState.Success(content, cover)
        } catch (e: OutOfMemoryError) {
            // CRITICAL: Book content too large for low-end devices (~2-5%)
            android.util.Log.e("BookViewer", "OOM loading book content", e)
            BookUiState.Error("Book too large for this device. Please try on a device with more memory.")
        } catch (e: Exception) {
            android.util.Log.e("BookViewer", "Error loading book", e)
            BookUiState.Error("Error loading book: ${e.message}")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("The Friendly Trader", fontSize = 18.sp)
                        Text(
                            "by Jesse J. Lamont",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    val progress = if (scrollState.maxValue > 0) {
                        BookmarkManager.getProgressPercentage(scrollState.value, scrollState.maxValue)
                    } else 0
                    
                    Icon(
                        imageVector = if (hasBookmark) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    if (progress > 0) {
                        Text(
                            text = "$progress%",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                    
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Menu")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear bookmark & start over") },
                            onClick = {
                                BookmarkManager.clearBookmark(context)
                                hasBookmark = false
                                coroutineScope.launch {
                                    scrollState.scrollTo(0)
                                }
                                showMenu = false
                            },
                            enabled = hasBookmark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        when (val state = bookState) {
            is BookUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading book...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            is BookUiState.Error -> {
                // Error state - show error message to user
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            is BookUiState.Success -> {
                // Success state - display book content
                // Use Column with verticalScroll (not LazyColumn) for text-heavy content
                // LazyColumn would be better for structured chapters, but single scroll works for continuous text
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Show bookmark indicator when actively restoring saved position
                    if (isRestoringBookmark) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Resuming from bookmark...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                            .verticalScroll(scrollState)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                    // Premium book cover with QUANTRACORE aesthetic
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .padding(bottom = 32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkSurface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            ElectricCyan.copy(alpha = 0.15f),
                                            DeepNavyBackground,
                                            DarkSurface
                                        )
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            ElectricCyan.copy(alpha = 0.5f),
                                            ElectricCyan.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "THE FRIENDLY TRADER",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        shadow = CyanGlowShadow,
                                        letterSpacing = 2.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "Your Beginner's Guide to\nConfidence in the Markets",
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center,
                                    color = MetallicSilver,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    text = "by Jesse J. Lamont",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = CrispWhite,
                                    fontWeight = FontWeight.Light
                                )
                            }
                        }
                    }
                    
                    // Book content text
                    Text(
                        text = state.content,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 28.sp,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Divider()
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "End of Book",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Thank you for reading The Friendly Trader!\nPart of QuantraVision Standard/Pro",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

/**
 * Load book content from assets on IO dispatcher
 * Uses BufferedReader with efficient text reading
 * Throws exception on failure (handled by caller)
 */
private suspend fun loadBookContent(context: Context): String = withContext(Dispatchers.IO) {
    context.assets.open("book/the_friendly_trader.txt").use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            // Use StringBuilder for efficient string concatenation
            val content = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                content.append(line).append("\n")
            }
            content.toString()
        }
    }
}

/**
 * Load book cover image from assets on IO dispatcher
 * Returns null if cover not found (non-critical failure)
 */
private suspend fun loadBookCover(context: Context): android.graphics.Bitmap? = withContext(Dispatchers.IO) {
    try {
        context.assets.open("book/cover.png").use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        android.util.Log.w("BookViewer", "Book cover not found (optional)", e)
        null // Cover is optional, book still works without it
    }
}
