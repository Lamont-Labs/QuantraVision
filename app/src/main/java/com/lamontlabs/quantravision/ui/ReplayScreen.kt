package com.lamontlabs.quantravision.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.replay.SessionPlayback
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * ReplayScreen (production-ready)
 * - Session selection and playback UI
 * - Playback controls (play, pause, stop, seek)
 * - Frame timeline with scrubbing
 * - Detection overlay display during playback
 * - Session metadata and statistics
 */
@Composable
fun ReplayScreen(
    context: Context,
    onBack: () -> Unit
) {
    var selectedSession by remember { mutableStateOf<File?>(null) }
    val sessionPlayback = remember { SessionPlayback(context) }
    
    DisposableEffect(Unit) {
        onDispose {
            sessionPlayback.release()
        }
    }
    
    if (selectedSession == null) {
        SessionSelectionScreen(
            context = context,
            onSessionSelected = { session ->
                selectedSession = session
                sessionPlayback.loadFromFolder(session)
            },
            onBack = onBack
        )
    } else {
        PlaybackScreen(
            sessionPlayback = sessionPlayback,
            onBack = {
                sessionPlayback.clear()
                selectedSession = null
            }
        )
    }
}

@Composable
private fun SessionSelectionScreen(
    context: Context,
    onSessionSelected: (File) -> Unit,
    onBack: () -> Unit
) {
    val sessions = remember { SessionPlayback.getAvailableSessions(context) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Replay Sessions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.VideoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No replay sessions found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Start recording to create sessions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sessions) { session ->
                    SessionCard(
                        session = session,
                        onClick = { onSessionSelected(session) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionCard(
    session: File,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = session.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            val frameCount = session.listFiles { f -> 
                f.name.startsWith("shot_") && f.name.endsWith(".png")
            }?.size ?: 0
            
            Text(
                text = "$frameCount frames",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlaybackScreen(
    sessionPlayback: SessionPlayback,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val playbackState by sessionPlayback.playbackState.collectAsState()
    val currentFrameIndex by sessionPlayback.currentFrameIndex.collectAsState()
    val currentBitmap by sessionPlayback.currentBitmap.collectAsState()
    val metadata = sessionPlayback.getMetadata()
    val totalFrames = sessionPlayback.getTotalFrames()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(metadata?.sessionId ?: "Replay")
                        if (metadata != null) {
                            Text(
                                "${metadata.detectionCount} detections • ${metadata.frameCount} frames",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                currentBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Replay frame",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } ?: run {
                    CircularProgressIndicator()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${currentFrameIndex + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(40.dp)
                    )
                    
                    Slider(
                        value = currentFrameIndex.toFloat(),
                        onValueChange = { value ->
                            sessionPlayback.seekTo(value.toInt())
                        },
                        valueRange = 0f..(totalFrames - 1).toFloat().coerceAtLeast(0f),
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        "$totalFrames",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { sessionPlayback.stop() }
                    ) {
                        Icon(Icons.Default.Stop, "Stop")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { 
                            sessionPlayback.seekTo((currentFrameIndex - 1).coerceAtLeast(0))
                        }
                    ) {
                        Icon(Icons.Default.SkipPrevious, "Previous frame")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FloatingActionButton(
                        onClick = {
                            when (playbackState) {
                                SessionPlayback.PlaybackState.PLAYING -> sessionPlayback.pause()
                                else -> sessionPlayback.play()
                            }
                        }
                    ) {
                        Icon(
                            if (playbackState == SessionPlayback.PlaybackState.PLAYING) 
                                Icons.Default.Pause 
                            else 
                                Icons.Default.PlayArrow,
                            contentDescription = if (playbackState == SessionPlayback.PlaybackState.PLAYING) "Pause" else "Play"
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { 
                            sessionPlayback.seekTo((currentFrameIndex + 1).coerceAtMost(totalFrames - 1))
                        }
                    ) {
                        Icon(Icons.Default.SkipNext, "Next frame")
                    }
                }

                if (metadata != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetadataItem(
                            label = "Start",
                            value = formatTimestamp(metadata.startTime)
                        )
                        MetadataItem(
                            label = "Duration",
                            value = formatDuration(metadata.duration)
                        )
                        MetadataItem(
                            label = "Detections",
                            value = "${metadata.detectionCount}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val fmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return fmt.format(Date(timestamp))
}

private fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60)
        minutes > 0 -> String.format("%d:%02d", minutes, seconds % 60)
        else -> String.format("0:%02d", seconds)
    }
}
