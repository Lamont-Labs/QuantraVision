package com.lamontlabs.quantravision.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.lamontlabs.quantravision.PatternMatch
import timber.log.Timber

enum class VoiceCommandState {
    IDLE,
    LISTENING,
    PROCESSING,
    ERROR,
    SUCCESS
}

data class VoiceCommandStatus(
    val state: VoiceCommandState,
    val message: String = "",
    val result: VoiceCommandResult? = null
)

class VoiceCommandHandler(
    private val context: Context,
    private val onStatusChange: (VoiceCommandStatus) -> Unit,
    private val onCommandExecuted: (VoiceCommandResult) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var currentDetections: List<PatternMatch> = emptyList()
    
    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            setupSpeechRecognizer()
        }
    }
    
    fun updateDetections(detections: List<PatternMatch>) {
        currentDetections = detections
    }
    
    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    onStatusChange(VoiceCommandStatus(VoiceCommandState.LISTENING, "Listening..."))
                    Timber.d("Voice: Ready for speech")
                }
                
                override fun onBeginningOfSpeech() {
                    Timber.d("Voice: Beginning of speech")
                }
                
                override fun onRmsChanged(rmsdB: Float) {
                }
                
                override fun onBufferReceived(buffer: ByteArray?) {
                }
                
                override fun onEndOfSpeech() {
                    onStatusChange(VoiceCommandStatus(VoiceCommandState.PROCESSING, "Processing..."))
                    Timber.d("Voice: End of speech")
                }
                
                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission denied"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error (not required for offline)"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try again"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                        else -> "Unknown error"
                    }
                    onStatusChange(VoiceCommandStatus(VoiceCommandState.ERROR, errorMessage))
                    Timber.e("Voice: Error $error - $errorMessage")
                }
                
                override fun onResults(results: Bundle?) {
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { matches ->
                        if (matches.isNotEmpty()) {
                            val spokenText = matches[0]
                            Timber.d("Voice: Recognized: $spokenText")
                            processRecognizedCommand(spokenText)
                        }
                    }
                }
                
                override fun onPartialResults(partialResults: Bundle?) {
                }
                
                override fun onEvent(eventType: Int, params: Bundle?) {
                }
            })
        }
    }
    
    private fun processRecognizedCommand(spokenText: String) {
        val result = VoiceCommandProcessor.processCommand(
            spokenText,
            context,
            currentDetections
        )
        
        val feedback = VoiceCommandProcessor.getCommandFeedback(result)
        
        when (result) {
            is VoiceCommandResult.CommandNotRecognized -> {
                onStatusChange(VoiceCommandStatus(VoiceCommandState.ERROR, feedback))
            }
            else -> {
                onStatusChange(VoiceCommandStatus(VoiceCommandState.SUCCESS, feedback, result))
                onCommandExecuted(result)
            }
        }
    }
    
    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onStatusChange(VoiceCommandStatus(
                VoiceCommandState.ERROR,
                "Speech recognition not available on this device"
            ))
            return
        }
        
        if (speechRecognizer == null) {
            setupSpeechRecognizer()
        }
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Timber.e(e, "Voice: Failed to start listening")
            onStatusChange(VoiceCommandStatus(
                VoiceCommandState.ERROR,
                "Failed to start voice recognition: ${e.message}"
            ))
        }
    }
    
    fun stopListening() {
        speechRecognizer?.stopListening()
        onStatusChange(VoiceCommandStatus(VoiceCommandState.IDLE))
    }
    
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}

@Composable
fun rememberVoiceCommandHandler(
    onStatusChange: (VoiceCommandStatus) -> Unit,
    onCommandExecuted: (VoiceCommandResult) -> Unit
): VoiceCommandHandler {
    val context = LocalContext.current
    val handler = remember(context) {
        VoiceCommandHandler(context, onStatusChange, onCommandExecuted)
    }
    
    DisposableEffect(handler) {
        onDispose {
            handler.destroy()
        }
    }
    
    return handler
}
